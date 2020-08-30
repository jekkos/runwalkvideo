package com.runwalk.video.media.dsj;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.JOptionPane;

import org.jdesktop.application.Action;

import com.runwalk.video.core.Containable;
import com.runwalk.video.core.FullScreenSupport;
import com.runwalk.video.core.OnEdt;
import com.runwalk.video.core.PropertyChangeSupport;
import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.media.IVideoComponent;
import com.runwalk.video.media.IVideoPlayer;
import com.runwalk.video.media.settings.VideoComponentSettings;

import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.rc.RendererControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class bundles all common DSJ functionality for the {@link IVideoCapturer} and {@link IVideoPlayer} implementations.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <T> The specific DSFiltergraph subclass used by this component
 */
public abstract class DSJComponent<T extends DSFiltergraph, V extends VideoComponentSettings> implements IVideoComponent, 
	PropertyChangeSupport, ComponentListener, FullScreenSupport, Containable {

	private static final String REJECT_PAUSE_FILTER = "rejectPauseFilter";

	/**
	 * D3D9 renderer uses newer DirectX API and less CPU than the former when it can work on a capable GPU.
	 * On the other hand, overlays can only be drawn using DD7's {@link RendererControls}.
	 * All filtergraphs are initialized in the paused state.
	 */
	protected static final int FLAGS = DSFiltergraph.D3D9 | DSFiltergraph.INIT_PAUSED;
	
	private volatile boolean visible;

	private volatile boolean fullScreen;

	private V videoComponentSettings;

	private T filtergraph;

	private boolean rejectPauseFilter = false;

	private boolean toggleFullScreenEnabled = true;

	protected DSJComponent(V videoComponentSettings) {
		this.videoComponentSettings = videoComponentSettings;
	}
	
	protected DSJComponent() { }

	public void startRunning() {
		// fire a graph changed so all settings made to the filtergraph will be applied
		getFiltergraph().play();
	}

	/** {@inheritDoc} */
	public void stopRunning() {
		// stop the filtergraph so we can configure or rewire as needed
		getFiltergraph().stop();
		getLogger().debug("Filtergraph for " + getTitle() + " stopped");
	}

	public T getFiltergraph() {
		return filtergraph;
	}

	public void setFiltergraph(T filtergraph) {
		this.filtergraph = filtergraph;
	}

	public boolean getRejectPauseFilter() {
		return rejectPauseFilter;
	}

	public void setRejectPauseFilter(boolean rejectPauseFilter) {
		// a PCE will be fired to make the MenuItem respond to changes to this property
		firePropertyChange(REJECT_PAUSE_FILTER, this.rejectPauseFilter, this.rejectPauseFilter = rejectPauseFilter);
		getLogger().debug("Pause filter rejection for filtergraph " + getTitle() + " now set to " + rejectPauseFilter);
	}

	@Action(selectedProperty = REJECT_PAUSE_FILTER)
	public void toggleRejectPauseFilter() {
		// this action will be shown as a JCheckBoxMenuItem in the menu bar
	}

	public Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

	@Action
	public void viewFilterProperties() {
		DSFilter[] filters = getFiltergraph().listFilters();
		DSFilter selectedFilter =  (DSFilter) JOptionPane.showInputDialog(
				null,
				getResourceMap().getString("viewFilterProperties.Action.shortDescription"),
				getResourceMap().getString("viewFilterProperties.Action.text"),
				JOptionPane.PLAIN_MESSAGE,
				null,
				filters,
				filters[0]);
		if (selectedFilter != null) {
			selectedFilter.showPropertiesDialog();
		}
	}

	//@Action
	//TODO hier een actie van maken.. toon een input dialog of iets dergelijks
	public void insertFilter(String name) {
		DSFilterInfo filterinfo = DSFilterInfo.filterInfoForName(name);
		if (getFiltergraph() != null) {
			DSFilter[] installedFilters = getFiltergraph().listFilters();
			// stop filtergraph to add filters
			getFiltergraph().stop();
			DSFilter filter = getFiltergraph().addFilterToGraph(filterinfo);
			filter.connectDownstream(filter.getPin(0,0), installedFilters[1].getPin(1, 0), true);
			filter.dumpConnections();
			getLogger().debug("Inserting filter before " + installedFilters[1].getName() + " after " + installedFilters[3].getName());
			getFiltergraph().insertFilter(installedFilters[1], installedFilters[3], filterinfo);
			filter.showPropertiesDialog();
			// wiring has changed, notify graph
			getFiltergraph().graphChanged();
			// start playing again
			getFiltergraph().play();
		}
	}

	public void dispose() {
		if (getFiltergraph() != null) {
			Frame fullScreenFrame = getFiltergraph().getFullScreenWindow();
			if (fullScreenFrame != null) {
				fullScreenFrame.removeComponentListener(this);
				// this call is really needed to reopen a new video
				fullScreenFrame.dispose();
			}
			//getFiltergraph().dispose();
		}
	}

	public boolean isActive() {
		return getFiltergraph() != null && getFiltergraph().getActive();
	}

	public Component getComponent() {
		return getFiltergraph().asComponent();
	}

	public boolean isResizable() {
		return true;
	}

	public Frame getFullscreenFrame() {
		return getFiltergraph().getFullScreenWindow();
	}

	@OnEdt
	public void setVisible(final boolean visible) {
		// TODO adhere a default way for invoking @Actions and their selectedProperties
		if (this.visible != visible) {
			// just leave this empty, selected property will be called..
			if (isFullScreen() && getFullscreenFrame() != null) {
				// setVisibility will be called first, as it is the selectedProperty for this Action
				getFullscreenFrame().setVisible(visible);
			}
			firePropertyChange(VISIBLE, this.visible, this.visible = visible);
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}
	
	public void setFullScreen(boolean fullScreen) {
		firePropertyChange(FULL_SCREEN, this.fullScreen, this.fullScreen = fullScreen);
	}

	@OnEdt
	public void enterFullScreen() {
		GraphicsDevice foundDevice = getGraphicsDevice();
		if (foundDevice != null) {
			// disable toggling while switching full screen
			setToggleFullScreenEnabled(false);
			getFiltergraph().goFullScreen(foundDevice, 1);
			getFullscreenFrame().addComponentListener(this);
			getFullscreenFrame().setTitle(getTitle());
			getFullscreenFrame().setName(getTitle());
		}
		setToggleFullScreenEnabled(true);
		setFullScreen(true);
	}
	
	public void leaveFullScreen() {
		if (getFullscreenFrame() != null) {
			getFullscreenFrame().removeComponentListener(this);
		}
		getFiltergraph().leaveFullScreen();
		setFullScreen(false);
	}

	@OnEdt
	public void toFront() {
		if (getFullscreenFrame() != null) {
			getFullscreenFrame().toFront();
		}
	}

	public Integer getMonitorId() {
		String monitorIdString = videoComponentSettings.getMonitorId();
		return monitorIdString != null && !monitorIdString.isEmpty() ? 
				Integer.parseInt(monitorIdString) : null;
	}

	public void setMonitorId(Integer monitorId) {
		videoComponentSettings.setMonitorId(Integer.toString(monitorId));
	}

	public boolean isToggleFullScreenEnabled() {
		return false;
	}

	public void setToggleFullScreenEnabled(boolean toggleFullScreenEnabled) {
		firePropertyChange(TOGGLE_FULL_SCREEN_ENABLED, this.toggleFullScreenEnabled, this.toggleFullScreenEnabled = toggleFullScreenEnabled);
	}

	private GraphicsDevice getGraphicsDevice() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice foundDevice = null;
		for (GraphicsDevice device : graphicsEnvironment.getScreenDevices()) {
			if (getMonitorId() != null && device.getIDstring().endsWith(getMonitorId().toString())) {
				foundDevice = device;
			}
		}
		return foundDevice;
	}

	@Action(selectedProperty = FULL_SCREEN, enabledProperty = TOGGLE_FULL_SCREEN_ENABLED)
	public void toggleFullScreen(ActionEvent event) {
		// check if event is originating from a component that has selected state
		if (event.getSource() instanceof AbstractButton) {
			AbstractButton source = (AbstractButton) event.getSource();
			if (source.isSelected()) {
				enterFullScreen();
			} else {
				leaveFullScreen();
			}
		}
	}

	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility(ActionEvent event) {
		// check if event is originating from a component that has selected state
		if (event.getSource() instanceof AbstractButton) {
			AbstractButton source = (AbstractButton) event.getSource();
			setVisible(source.isSelected());
		}
	}

	public void componentShown(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}

	public void componentHidden(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}

	public void componentResized(ComponentEvent e) { }

	public void componentMoved(ComponentEvent e) { }
	
	public void hierarchyChanged(HierarchyEvent e) {
		// visibility of the container changed
		if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED) {
			setVisible(e.getChanged().isVisible());
		}
	}

	public BufferedImage getImage() {
		return getFiltergraph().getImage();
	}

	public Dimension getDimension() {
		return getFiltergraph().getDisplaySize();
	}

	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		int width = image == null ? getFiltergraph().getWidth() : image.getWidth();
		int height = image == null ? getFiltergraph().getHeight() : image.getHeight();
		int[] rectangle = new int[] {0, 0, width, height};
		getFiltergraph().getRendererControls().setOverlayImage(image, rectangle, alphaColor, 1f);
	}

	public boolean isNativeWindowing() {
		return false;
	}
	
	public String getTitle() {
		return videoComponentSettings.getName();
	}
	
	protected V getVideoComponentSettings() {
		return videoComponentSettings;
	}

}
