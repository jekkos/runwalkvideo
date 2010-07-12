package com.runwalk.video.gui.panels;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.beans.BeanConnector;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.google.common.collect.Iterables;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.gui.AppComponent;
import com.runwalk.video.util.AppSettings;

@SuppressWarnings("serial")
public abstract class AbstractTablePanel<T extends SerializableEntity<T>> extends AppPanel implements AppComponent {
	
	private static final String SELECTED_ITEM = "selectedItem";
	private static final String EVENT_LIST = "itemList";
	protected static final String ROW_SELECTED = "rowSelected";

	private JTable table;
	private JButton firstButton, secondButton;
	private Boolean rowSelected = false;
	private EventList<T> itemList;
	private EventSelectionModel<T> eventSelectionModel;
	private MouseListener jTableMouseListener;
	
	public AbstractTablePanel(LayoutManager mgr) {
		setLayout(mgr);
		table = new JTable();
		getTable().getTableHeader().setFont(AppSettings.MAIN_FONT);
		getTable().setShowGrid(false);
		getTable().setFont(AppSettings.MAIN_FONT);
		jTableMouseListener = new JTableButtonMouseListener();
	}
	
	public AbstractTablePanel() {
		this(null);
	}
	
	public boolean isRowSelected() {
		return this.rowSelected;
	}
	
	public void setRowSelected(boolean rowSelected) {
		this.firePropertyChange(ROW_SELECTED, this.rowSelected, this.rowSelected = rowSelected);
	}
	
	/**
	 * Verwijder het huidig geselecteerde item.
	 */
	public void clearSelectedItem() {
		getEventSelectionModel().clearSelection();
	}
	
	public T getSelectedItem() {
		T selectedItem = null;
		if (getEventSelectionModel() != null) {
			EventList<T> selected = getEventSelectionModel().getTogglingSelected();
			if (!selected.isEmpty()) {
				selectedItem = Iterables.getOnlyElement(selected);
			}
		}
		return selectedItem;
	}
	
	public void setSelectedItem(T item) {
		getEventSelectionModel().getTogglingSelected().add(item);
		int rowIndex = getItemList().indexOf(item);
		getTable().scrollRectToVisible(getTable().getCellRect(rowIndex, 0, true));
	}
	
	public void setSelectedItem(int row) {
		if (row > -1) {
			setSelectedItem(getItemList().get(row));
		}
	}

	public JTable getTable() {
		return table;
	}
	
	/**
	 * This method will add a {@link MouseListener} to the contained {@link JTable} in case it wasn't already added.
	 */
	public void addMouseListenerToTable() {
		List<MouseListener> mouseListeners = Arrays.asList(getTable().getMouseListeners());
		if (!mouseListeners.contains(jTableMouseListener)) {
			getTable().addMouseListener(jTableMouseListener);
		}
	}

	public JButton getFirstButton() {
		return firstButton;
	}

	public JButton getSecondButton() {
		return secondButton;
	}

	public void setFirstButton(JButton deleteButton) {
		this.firstButton = deleteButton;
	}

	public void setSecondButton(JButton newButton) {
		this.secondButton = newButton;
	}
	
	/**
	 * Lijst met items wordt van buitenaf op de panel gezet. Deze zal dan klaar gemaakt worden voor 
	 * gebruik met een {@link JTable} 
	 * 
	 * @param itemList the list
	 * @param itemConnector the connector that will forward changeEvents to the list.
	 */
	public void setItemList(EventList<T> itemList, ObservableElementList.Connector<T> itemConnector) {
        EventList<T> observedItems = new ObservableElementList<T>(itemList, itemConnector);
        SortedList<T> sortedItems = SortedList.create(observedItems);
        EventList<T> specializedList = specializeItemList(sortedItems);
        firePropertyChange(EVENT_LIST, this.itemList, this.itemList = specializedList); 
        eventSelectionModel = new EventSelectionModel<T>(specializedList);
        eventSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
        eventSelectionModel.getTogglingSelected().addListEventListener(new ListEventListener<T>() {
        	
        	@SuppressWarnings("deprecation")
			public void listChanged(ListEvent<T> listChanges) {
        		while(listChanges.next()) {
        			int changeType = listChanges.getType();
        			if (changeType == ListEvent.DELETE) {
        				if (listChanges.getOldValue() != ListEvent.UNKNOWN_VALUE) {
        					T oldValue = listChanges.getOldValue();
        					T newValue = getSelectedItem();
        					firePropertyChange(SELECTED_ITEM, oldValue, newValue);
        					setRowSelected(!eventSelectionModel.getSelected().isEmpty());
        				}
        			} else if (changeType == ListEvent.INSERT) {
        				if (listChanges.getOldValue() == ListEvent.UNKNOWN_VALUE) {
        					firePropertyChange(SELECTED_ITEM, null, getSelectedItem());
            				setRowSelected(!eventSelectionModel.getSelected().isEmpty());
        				}
        			}
        		}
        	}
        });
        EventTableModel<T> dataModel = new EventTableModel<T>(specializedList, getTableFormat());
		getTable().setModel(dataModel);
        TableComparatorChooser.install(getTable(), sortedItems, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
        getTable().setSelectionModel(eventSelectionModel);
        getTable().setColumnSelectionAllowed(false);
	}
	
	public void setItemList(EventList<T> itemList, Class<T> itemClass) {
		Connector<T> beanConnector = new BeanConnector<T>(itemClass);
		setItemList(itemList, beanConnector);
	}
	
	/**
	 * Specialization hook for the set {@link ObservableElementList}. You can override the exact type of the set {@link EventList} by implementing this method.
	 * @param eventList The observable eventlist
	 * @return A specialized version of the observable eventlist
	 */
	protected EventList<T> specializeItemList(EventList<T> eventList) {
		return eventList;
	}
	
	public abstract TableFormat<T> getTableFormat();
	
	public EventList<T> getItemList() {
		return itemList;
	}
	
	public EventSelectionModel<T> getEventSelectionModel() {
		return eventSelectionModel;
	}
	
	public javax.swing.Action getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public ApplicationContext getContext() {
		return getApplication().getContext();
	}

	public Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(getClass(), AbstractTablePanel.class);
	}
	
	public ActionMap getApplicationActionMap() {
		return getContext().getActionMap(AbstractTablePanel.class, this);
	}

	
	protected class CustomJTableRenderer implements TableCellRenderer {
		private TableCellRenderer __defaultRenderer;

		public CustomJTableRenderer(TableCellRenderer renderer) {
			__defaultRenderer = renderer;
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected,
				boolean hasFocus,
				int row, int column)
		{
			if(value instanceof Component)
				return (Component)value;
			return __defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
	
	class JTableButtonMouseListener extends MouseAdapter {

		  private void __forwardEventToButton(MouseEvent e) {
		    TableColumnModel columnModel = getTable().getColumnModel();
		    int column = columnModel.getColumnIndexAtX(e.getX());
		    int row    = e.getY() / getTable().getRowHeight();
		    Object value;
		    JButton button;
		    MouseEvent buttonEvent;

		    if(row >= getTable().getRowCount() || row < 0 ||
		       column >= getTable().getColumnCount() || column < 0)
		      return;

		    value = getTable().getValueAt(row, column);

		    if(!(value instanceof JButton))
		      return;

		    button = (JButton)value;

		    buttonEvent =
		      (MouseEvent)SwingUtilities.convertMouseEvent(getTable(), e, button);
		    button.dispatchEvent(buttonEvent);
		    // This is necessary so that when a button is pressed and released
		    // it gets rendered properly.  Otherwise, the button may still appear
		    // pressed down when it has been released.
		    getTable().repaint();
		  }

		  public void mouseClicked(MouseEvent e) {
		    __forwardEventToButton(e);
		  }
		  
		}


}