package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.AppWindowWrapper.AppWindowWrapperListener;
import com.runwalk.video.gui.media.VideoComponent;
import com.runwalk.video.gui.media.VideoComponent.State;
import com.runwalk.video.util.ResourceInjector;
import com.tomtessier.scrollabledesktop.BaseInternalFrame;

@SuppressWarnings("serial")
public class VideoMenuBar extends JMenuBar implements AppComponent, PropertyChangeListener {

	private BiMap<AppWindowWrapper, JCheckBoxMenuItem> windowBoxMap = HashBiMap.create();
	private JMenu windowMenu;
	private JDialog aboutBox;

	public VideoMenuBar() {

		JSeparator separator = new JSeparator();
		JMenu fileMenu = new  JMenu(getResourceMap().getString("fileMenu.text"));
		JMenuItem newClientMenuItem = new JMenuItem(getApplication().getClientTablePanel().getAction("addClient"));
		fileMenu.add(newClientMenuItem);
		JMenuItem deleteClientMenuItem = new JMenuItem(getApplication().getClientTablePanel().getAction("deleteClient"));
		fileMenu.add(deleteClientMenuItem);
		fileMenu.add(separator);

		JMenuItem createAnalysisItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("addAnalysis"));
		fileMenu.add(createAnalysisItem);
		JMenuItem deleteAnalysisItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("deleteAnalysis"));
		fileMenu.add(deleteAnalysisItem);

		fileMenu.add(new JSeparator());
		JMenuItem showVideoFileItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("showVideoFile"));
		fileMenu.add(showVideoFileItem);
		//		JMenuItem openVideoFileItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("openVideoFile"));
		//		fileMenu.add(openVideoFileItem);

		fileMenu.add(new JSeparator());
		JMenuItem refreshMenuItem = new JMenuItem( getContext().getActionMap().get("refresh"));
		fileMenu.add(refreshMenuItem);
		JMenuItem saveMenuItem = new JMenuItem( getApplication().getClientTablePanel().getAction("save"));
		fileMenu.add(saveMenuItem);
		JMenuItem saveSettingsMenuItem = new JMenuItem( getContext().getActionMap().get("saveSettings"));
		fileMenu.add(saveSettingsMenuItem);

		fileMenu.add(new JSeparator());
		JMenuItem selectVideoDir = new JMenuItem( getApplication().getAnalysisOverviewTablePanel().getAction("selectVideoDir"));
		fileMenu.add(selectVideoDir);
		JMenuItem selectUncompressedVideoDir = new JMenuItem( getApplication().getAnalysisOverviewTablePanel().getAction("selectUncompressedVideoDir"));
		fileMenu.add(selectUncompressedVideoDir);

		fileMenu.add(new JSeparator());
		JMenuItem organiseVideoFiles = new JMenuItem( getApplication().getAnalysisOverviewTablePanel().getAction("organiseVideoFiles"));
		fileMenu.add(organiseVideoFiles);

		fileMenu.add(new JSeparator());
		JMenuItem exitMenuItem = new JMenuItem( getContext().getActionMap().get(RunwalkVideoApp.EXIT_ACTION));
		fileMenu.add(exitMenuItem);
		add(fileMenu);

		//the edit menu?
		JMenu editMenu = new JMenu(getResourceMap().getString("editMenu.text"));
		JMenuItem undo = new JMenuItem( getApplication().getApplicationActionMap().get("undo"));
		editMenu.add(undo);
		JMenuItem redo = new JMenuItem( getApplication().getApplicationActionMap().get("redo"));
		editMenu.add(redo);
		editMenu.add(new JSeparator());
		JMenuItem cut = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.CutAction(), "cut"));
		editMenu.add(cut);
		JMenuItem copy = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.CopyAction(), "copy"));
		editMenu.add(copy);
		JMenuItem paste = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.PasteAction(), "paste"));
		editMenu.add(paste);
		add(editMenu);

		//		JMenu videoMenu = new JMenu(getResourceMap().getString("videoMenu.text"));
		//		getComponent().add(videoMenu);

		windowMenu = new JMenu(getResourceMap().getString("windowMenu.text"));
		add(windowMenu);

		JMenu helpMenu = new  JMenu(getResourceMap().getString("helpMenu.text"));
		JMenuItem aboutMenuItem = new JMenuItem( getAction("about"));
		JMenuItem uploadLogFiles = new JMenuItem( getContext().getActionMap().get("uploadLogFiles"));
		helpMenu.add(uploadLogFiles);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutMenuItem);
		add(helpMenu);
	}

	@Action
	public void about() {
		if (aboutBox == null) {
			aboutBox = new RunwalkVideoAboutDialog(getApplication().getMainFrame());
		}
		aboutBox.setLocationRelativeTo(getApplication().getMainFrame());
		getApplication().show(aboutBox);
	}

	public void addWindow(final AppWindowWrapper appComponent) {
		if (!windowBoxMap.containsKey(appComponent)) {
			appComponent.addAppWindowWrapperListener(new AppWindowWrapperListener() {

				@Override
				public void windowClosed(WindowEvent e) {
					setCheckboxSelection(appComponent);
				}

				@Override
				public void windowOpened(WindowEvent e) {
					setCheckboxSelection(appComponent);
				}

				@Override
				public void internalFrameDeactivated(InternalFrameEvent e) {
					setCheckboxSelection(appComponent);
				}

				@Override
				public void internalFrameActivated(InternalFrameEvent e) {
					setCheckboxSelection(appComponent);
				}

			});
			appComponent.addPropertyChangeListener(this);
			JMenu menu = createMenu(appComponent);
			//TODO add internal frame instance at the end of the menu and after a separator..
			windowMenu.add(menu);
		} 
	}

	private JMenu createMenu(AppWindowWrapper appComponent) {
		JMenu result = new JMenu(appComponent.getTitle());
		JCheckBoxMenuItem checkedItem = new JCheckBoxMenuItem(getAction("showWindow"));
		Component component = appComponent.getHolder();
		checkedItem.setSelected(component.isVisible());
		windowBoxMap.put(appComponent, checkedItem);
		windowBoxMap.inverse().put(checkedItem, appComponent);
		char shortcut = Character.forDigit(windowBoxMap.size(), 9);
		KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut, ActionEvent.CTRL_MASK);
		checkedItem.setAccelerator(keyStroke);
		result.add(checkedItem);

		ActionMap actionMap = appComponent.getApplicationActionMap();
		if (actionMap != null && actionMap.allKeys() != null && actionMap.allKeys().length > 0) {
			result.add(new JSeparator());
			for (Object key : actionMap.allKeys()) {
				javax.swing.Action action = actionMap.get(key);
				if (getContext().getActionMap().get(key) == null) {
					JMenuItem item = new JMenuItem(action);
					if (action.getValue(javax.swing.Action.NAME) == null) {
						item.setText(action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString());
					}
					result.add(item);
				}
			}
		}
		return result;
	}

	@Action
	public void showWindow(ActionEvent e) {
		JCheckBoxMenuItem selectedItem = (JCheckBoxMenuItem) e.getSource();
		AppWindowWrapper component  = windowBoxMap.inverse().get(selectedItem);
		component.getHolder().setVisible(selectedItem.isSelected());
	}

	private void removeWindow(AppWindowWrapper appComponent) {
		JCheckBoxMenuItem boxItem = windowBoxMap.get(appComponent);
		if (boxItem != null) {
			for (int i = 0; i < windowMenu.getMenuComponentCount(); i++) {
				Component menuComponent = windowMenu.getMenuComponent(i);
				if (menuComponent instanceof JMenuItem ) {
					JMenuItem menuItem = (JMenuItem) menuComponent;
					if (menuItem.getText().equals(appComponent.getTitle())) {
						windowMenu.remove(menuItem);
					}
				}
			}
			windowBoxMap.remove(appComponent);
			windowMenu.updateUI();
			windowMenu.revalidate();
		}
	}

	private void hideWindow(AppWindowWrapper appComponent) {
		Container container = appComponent == null ? null : appComponent.getHolder();
		if (container != null) {
			if (container instanceof BaseInternalFrame) {
				BaseInternalFrame baseInternalFrame = (BaseInternalFrame) container;
				baseInternalFrame.getAssociatedButton().setEnabled(false);
//				baseInternalFrame.getAssociatedMenuButton().setEnabled(false);
			}
			container.setVisible(false);
		}
	}

	private void setCheckboxSelection(AppWindowWrapper appComponent) {
		JCheckBoxMenuItem checkBox = windowBoxMap.get(appComponent);
		if (checkBox != null) {
			checkBox.setSelected(appComponent.getHolder().isVisible());
		}
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
		return getContext().getResourceMap(getClass(), VideoMenuBar.class);
	}

	public ActionMap getApplicationActionMap() {
		return getContext().getActionMap(VideoMenuBar.class, this);
	}

	public String getTitle() {
		return "Menu Bar";
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// hiding and closing windows is handled by listening to the components' events
		if (VideoComponent.STATE.equals(evt.getPropertyName())) {
			VideoComponent.State newState = (State) evt.getNewValue();
			if (VideoComponent.State.DISPOSED.equals(newState)) {
				AppWindowWrapper appComponent = (AppWindowWrapper) evt.getSource();
				removeWindow(appComponent);
				appComponent.removePropertyChangeListener(this);
			}
		} else if (VideoComponent.FULL_SCREEN.equals(evt.getPropertyName())) {
			Boolean fullScreen = (Boolean) evt.getNewValue();
			if (fullScreen) {
				AppWindowWrapper appComponent = (AppWindowWrapper) evt.getSource();
				hideWindow(appComponent);
			}
		}
	}

}
