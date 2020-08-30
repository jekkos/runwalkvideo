package com.runwalk.video.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.odell.glazedlists.ObservableElementChangeHandler;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;

import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.model.AbstractEntityModel;
import com.runwalk.video.panels.AbstractTablePanel;

public class AbstractEntityModelConnector<T extends AbstractEntityModel<? extends SerializableEntity<?>>> implements Connector<T> {

	/** The change handler which contains the elements being observed via this {@link ObservableElementList.Connector}. */
	private ObservableElementChangeHandler<? extends T> changeHandler;

	/** The PropertyChangeListener to install on each list element. */
	protected final PropertyChangeListener propertyChangeListener = createPropertyChangeListener();

	protected final AbstractTablePanel<?> tablePanel;

	public AbstractEntityModelConnector(AbstractTablePanel<?> tablePanel) {
		this.tablePanel = tablePanel;
	}

	public EventListener installListener(T element) {
		element.addPropertyChangeListener(propertyChangeListener);
		return propertyChangeListener;
	}

	public void uninstallListener(T element, EventListener listener) {
		element.removePropertyChangeListener(propertyChangeListener);
	}

	@Override
	public void setObservableElementList(ObservableElementChangeHandler<? extends T> observableElementChangeHandler) {
		this.changeHandler = observableElementChangeHandler;
	}

	/**
	 * A local factory method to produce the PropertyChangeListener which will
	 * be installed on list elements.
	 */
	protected PropertyChangeListener createPropertyChangeListener() {
		return new PropertyChangeHandler();
	}

	/**
	 * This inner class notifies the {@link ObservableElementChangeHandler} about changes to list elements.
	 */
	public class PropertyChangeHandler implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			AbstractEntityModel<?> sourceModel = (AbstractEntityModel<?>) event.getSource();;
			sourceModel.setDirty(true);
			tablePanel.setDirty(true);
			changeHandler.elementChanged(sourceModel);
		}
		
	}

}
