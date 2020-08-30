package com.runwalk.video.panels;

import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.model.AbstractEntityModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.ui.AbstractEntityModelConnector;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class AbstractTablePanel<T extends AbstractEntityModel<? extends SerializableEntity<?>>> extends AbstractPanel {

	public static final String ROW_SELECTED = "rowSelected";
	public static final String CLIENT_SELECTED = "customerSelected";

	private static final String SELECTED_ITEM = "selectedItem";
	private static final String EVENT_LIST = "itemList";

	private final JTable table;
	private final JTableMouseListener jTableMouseListener = new JTableMouseListener();

	private JButton firstButton, secondButton;
	private Boolean rowSelected = false;
	/** The observable source list */
	private ObservableElementList<T> observableElementList;
	/** The transformed list */
	private EventList<T> itemList;
	/** The original, unfiltered list */
	private EventList<T> sourceList;
	/** The sorted list */
	private SortedList<T> sortedList;
	private DefaultEventSelectionModel<T> eventSelectionModel;
	private T selectedItem;
	private TableFormat<T> tableFormat;
	private DefaultEventTableModel<T> eventTableModel;
	
	private final ListEventListener<T> listEventListener = new ListEventListener<T>() {

		public void listChanged(ListEvent<T> listChanges) {

			while(listChanges.next()) {
				int changeType = listChanges.getType();
				if (changeType == ListEvent.DELETE) {
					setRowSelected(!eventSelectionModel.getSelected().isEmpty());
				} else if (changeType == ListEvent.INSERT) {
					T newValue = null;
					EventList<T> sourceList = listChanges.getSourceList();
					if (!sourceList.isEmpty()) {
						newValue = Iterables.getOnlyElement(sourceList);
					}
					setSelectedItem(newValue);
					LoggerFactory.getLogger(getClass()).debug("Selected {}", selectedItem.toString());
					setRowSelected(!eventSelectionModel.getSelected().isEmpty());
				}
			}
		}
	};

	protected AbstractTablePanel(LayoutManager mgr) {
		setLayout(mgr);
		table = new JTable() {
			
			@Override
			public void editingStopped(ChangeEvent e) {
				if (getItemList().size() <= editingRow) {
					removeEditor();
				} else {
					super.editingStopped(e);
				}
			}
			
		};
		getTable().getTableHeader().setFont(SettingsManager.MAIN_FONT);
		getTable().setShowGrid(false);
		getTable().setFont(SettingsManager.MAIN_FONT);
	}

	public AbstractTablePanel() {
		this(null);
	}

	public abstract void initialiseTableColumnModel();

	public boolean isRowSelected() {
		return rowSelected;
	}

	public void setRowSelected(boolean rowSelected) {
		firePropertyChange(ROW_SELECTED, this.rowSelected, this.rowSelected = rowSelected);
	}

	/**
	 * Verwijder het huidig geselecteerde item.
	 */
	public void clearSelectedItem() {
		getEventSelectionModel().clearSelection();
	}

	public int setSelectedItemRow(T selectedItem) {
		int rowIndex = getItemList().indexOf(selectedItem);
		if (rowIndex > -1) {
			getEventSelectionModel().getTogglingSelected().add(selectedItem);
			getTable().scrollRectToVisible(getTable().getCellRect(rowIndex, 0, true));
		}
		return rowIndex;
	}

	public void setSelectedItemRow(int row) {
		T item = getItemList().get(row);
		if (row > -1 && item != null) {
			setSelectedItemRow(getItemList().get(row));
		}
	}

	protected void setSelectedItem(T selectedItem) {
		if (selectedItem != this.selectedItem && selectedItem != null && selectedItem.equals(this.selectedItem)) {
			this.selectedItem = null;
		}
		firePropertyChange(SELECTED_ITEM, this.selectedItem, this.selectedItem = selectedItem);
	}
	
	public void updateRow(T element) {
		getObservableElementList().elementChanged(element);
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	public JTable getTable() {
		return table;
	}

	/**
	 * This method will add a {@link MouseListener} to the contained {@link JTable} in case it wasn't already there.
	 */
	public void registerClickHandler(ClickHandler<T> clickHandler) {
		List<MouseListener> mouseListeners = Arrays.asList(getTable().getMouseListeners());
		if (!mouseListeners.contains(jTableMouseListener)) {
			getTable().addMouseListener(jTableMouseListener);
		}
		jTableMouseListener.setClickHandler(clickHandler);
	}

	public JButton getFirstButton() {
		return firstButton;
	}

	public void setFirstButton(JButton deleteButton) {
		this.firstButton = deleteButton;
	}

	public JButton getSecondButton() {
		return secondButton;
	}

	public void setSecondButton(JButton newButton) {
		this.secondButton = newButton;
	}
	
	public void dispose() {
		if (sourceList != null && itemList != null) {
			eventTableModel.dispose();
			eventSelectionModel.getTogglingSelected().removeListEventListener(listEventListener);
			eventSelectionModel.dispose();
			sortedList.dispose();
			observableElementList.dispose();
			sourceList.dispose();
			itemList.dispose();
		}
	}

	/**
	 * The {@link EventList} will be injected from the outside. This method will further prepare it to 
	 * use it with a {@link JTable}.
	 * 
	 * @param itemList The list
	 * @param itemConnector The connector that will forward changeEvents to the list.
	 */
	public void setItemList(EventList<T> itemList, ObservableElementList.Connector<? super T> itemConnector) {
		// FIXME lists should be disposed first when calling this method
		itemList.getReadWriteLock().writeLock().lock();
		try {
			sourceList = itemList;
			EventList<T> specializedList;
			// make a proxy if the list wasn't already proxied
			observableElementList = new ObservableElementList<T>(itemList, itemConnector);
			sortedList = SortedList.create(observableElementList);
			sortedList.setMode(SortedList.AVOID_MOVING_ELEMENTS);
			specializedList = specializeItemList(sortedList);
			firePropertyChange(EVENT_LIST, this.itemList, this.itemList = specializedList); 
			eventSelectionModel = new DefaultEventSelectionModel<T>(specializedList);
			eventSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
			eventSelectionModel.getTogglingSelected().addListEventListener(listEventListener);
			eventTableModel = new DefaultEventTableModel<T>(specializedList, getTableFormat());
			getTable().setModel(eventTableModel);
			TableComparatorChooser.install(getTable(), sortedList, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO);
			getTable().setSelectionModel(eventSelectionModel);
			getTable().setColumnSelectionAllowed(false);
			initialiseTableColumnModel();
		} finally {
			itemList.getReadWriteLock().writeLock().unlock();
		}
	}

	public void setItemList(EventList<T> itemList, AbstractTablePanel<?> panel) {
		setItemList(itemList, new AbstractEntityModelConnector<T>(panel));
	}
	
	public void setItemList(EventList<T> itemList) {
		setItemList(itemList, this);
	}

	/**
	 * Specialization hook for the set {@link ObservableElementList}. 
	 * You can override the exact type of the set {@link EventList} by implementing this method.
	 * 
	 * @param eventList The observable eventlist
	 * @return A specialized version of the observable eventlist
	 */
	protected EventList<T> specializeItemList(EventList<T> eventList) {
		return eventList;
	}

	public TableFormat<T> getTableFormat() {
		return tableFormat;
	}

	public void setTableFormat(TableFormat<T> tableFormat) {
		this.tableFormat = tableFormat;
	}

	public EventList<T> getItemList() {
		return itemList;
	}

	public ObservableElementList<T> getObservableElementList() {
		return observableElementList;
	}

	public DefaultEventSelectionModel<T> getEventSelectionModel() {
		return eventSelectionModel;
	}

	protected DefaultEventTableModel<T> getEventTableModel() {
		return eventTableModel;
	}

	public interface ClickHandler<E> {

		void handleClick(E element);

	}
	
	private class JTableMouseListener extends MouseAdapter {

		private ClickHandler<T> clickHandler;

		public JTableMouseListener() { }

		protected void setClickHandler(ClickHandler<T> clickHandler) {
			this.clickHandler = clickHandler;
		}

		public void mouseClicked(MouseEvent e) {
			TableColumnModel columnModel = getTable().getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row    = e.getY() / getTable().getRowHeight();

			if(row >= getTable().getRowCount() || row < 0 ||
					column >= getTable().getColumnCount() || column < 0)
				return;
			// clicks will be handled if a jbutton renderer is installed on the column
			TableCellRenderer cellRenderer = getTable().getColumnModel().getColumn(column).getCellRenderer();
			if (cellRenderer instanceof AbstractButton) {
				clickHandler.handleClick(getEventTableModel().getElementAt(row));
			}
		}

	}


}