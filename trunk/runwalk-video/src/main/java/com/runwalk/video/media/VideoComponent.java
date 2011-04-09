package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;

import com.google.common.collect.Lists;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.ui.AppComponent;
import com.runwalk.video.ui.PropertyChangeSupport;
import com.runwalk.video.ui.SelfContained;

/**
 * This abstraction allows you to make easy reuse of the common video UI functionality  
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen Peelaerts
 *
 */
@AppComponent
public abstract class VideoComponent implements PropertyChangeSupport {

	public static final String IDLE = "idle";
	public static final String STATE = "state";
	public static final String MONITOR_ID = "monitorId";
	public static final String DISPOSED = "disposed";

	private Recording recording;
	private Timer timer;
	private WeakReference<ActionMap> actionMap;
	private State state;
	private Integer monitorId;
	private boolean overlayed;

	/**
	 * This variable is used to determine the default monitor on which this component will be shown.
	 */
	private final int componentId;


	protected VideoComponent(int componentId) {
		this.componentId = componentId;
		setIdle(true);
	}

	public abstract IVideoComponent getVideoImpl();

	/**
	 * This method simply invokes {@link #startRunning()} if the video component is stopped 
	 * or {@link #stopRunning()} if the component is running at invocation time.
	 */
	@Action(selectedProperty = IDLE)
	public void togglePreview() {
		if (isIdle()) {
			getVideoImpl().stopRunning();
		} else {
			getVideoImpl().startRunning();
		}
	}

	public Recording getRecording() {
		return recording;
	}

	public void setRecording(Recording recording) {
		this.recording = recording;
	}

	protected Timer getTimer() {
		return timer;
	}

	protected void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * Returns an unique id for this concrete {@link VideoComponent} type.
	 * Implementations can be numbered independently from each other.
	 * Most of the time a static counter will be used at creation time, that 
	 * is incremented in the subclass.
	 * 
	 * @return The number
	 */
	public int getComponentId() {
		return componentId;
	}

	protected void setMonitorId(int monitorId) {
		this.monitorId = monitorId;
	}

/*	public void setFullScreen(boolean fullScreen, int monitorId) {
		monitorId = this.monitorId == null ? monitorId : this.monitorId;
		getVideoImpl().setFullScreen(fullScreen, monitorId);
	}
	*/
/*	public boolean isVisible() {
		return getVideoImpl().isVisible();
	}

	public void setVisible(boolean visible) {
		getVideoImpl().setVisible(visible);
	}

	public void toggleVisibility() {
		getVideoImpl().setVisible(isVisible());
	}*/

	public BufferedImage getImage() {
		return getVideoImpl().getImage();
	}

	public void setBlackOverlayImage() {
		Dimension dimension = getVideoImpl().getDimension();
		if (dimension != null) {
			final BufferedImage newOverlay = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_4BYTE_ABGR); 
			setOverlayImage(newOverlay, Color.white);
		}
	}

	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		getVideoImpl().setOverlayImage(image, alphaColor);
		setOverlayed(true);
	}

	private void setOverlayed(boolean overlayed) {
		this.overlayed = overlayed;
	}

	public boolean isOverlayed() {
		return overlayed;
	}

/*	protected void showComponent() {
		// TODO monitorId property is set by creation factory.. maybe it can be passed as an argument, too?
		showComponent(true, this.monitorId);
	}*/

	/*protected void showComponent(boolean goFullScreen, Integer monitorId) {
		// get the graphicsdevice corresponding with the given monitor id
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		GraphicsDevice defaultGraphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		monitorId = checkMonitorId(monitorId, graphicsDevices.length);
		GraphicsDevice graphicsDevice = graphicsDevices[monitorId];
		// get a reference to the previously visible component
		Container oldContainer = getHolder();
		// go fullscreen if the selected monitor is not the default one
		if (getVideoImpl().isActive()) {
			fullScreen = getVideoImpl().isFullScreen();
		} else {
			fullScreen = graphicsDevice != defaultGraphicsDevice;
		}
		if (fullScreen) {
			boolean frameInitialized = getFullScreenFrame() != null;
			getVideoImpl().setFullScreen(graphicsDevice, true);
			if (frameInitialized) {
				getFullScreenFrame().addComponentListener(this);
			}
		} else {
			getVideoImpl().setFullScreen(graphicsDevice, false);
			if (getInternalFrame() == null && getVideoImpl().getComponent() != null) {
				createInternalFrame();
			}
		}
		if (oldContainer != null) {
			oldContainer.setVisible(false);
		}
		setFullScreen(fullScreen);
		if (getHolder() != null) {
			setComponentTitle(getTitle());
			setVisible(true);
		}
	}*/

	/*@org.jdesktop.application.Action(enabledProperty = FULL_SCREEN_ENABLED, selectedProperty = FULL_SCREEN, block = BlockingScope.ACTION)
	public Task<Void, Void> toggleFullScreen() {
		return new AbstractTask<Void, Void>("toggleFullScreen") {

			protected Void doInBackground() throws Exception {
				// go fullscreen if component is displaying on the primary device, otherwise apply windowed mode
				monitorId = monitorId != null && monitorId == 0 ? null : 0;
				showComponent(isFullScreen(), monitorId);
				return null;
			}

		};
	}*/

	protected void setState(State state) {
		firePropertyChange(STATE, this.state, this.state = state);
		// full screen mode is enabled for this component if there are at least 2 monitors connected and the component is idle
		//GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		//setFullScreenEnabled(isIdle() && graphicsDevices.length > 1);
	}

	public State getState() {
		return state;
	}

	public boolean isIdle() {
		return getState() == State.IDLE;
	}

	public void setIdle(boolean idle) {
		boolean wasIdle = isIdle();
		setState(idle ? State.IDLE : State.STOPPED);
		firePropertyChange(IDLE, wasIdle, isIdle());
	}

	public boolean isStopped() {
		return getState() == State.STOPPED;
	}

	/*private void maybeRemoveComponentListener(Component comp, ComponentListener l) {
		if (comp != null) {
			comp.addComponentListener(l);
		}
	}*/

	public void stopRunning() {
		getVideoImpl().stopRunning();
	}

	@Action(enabledProperty = IDLE)
	public void dispose() {
		// fire event before removing listeners
		setState(State.DISPOSED);
		// no componentlisteners left here??
		// no propertyChangeListener left here??
		if (getVideoImpl() != null) {			
			// dispose on the video implementation will dispose resources for the full screen frame
			// maybeRemoveComponentListener(getFullScreenFrame(), this);
			getVideoImpl().dispose();
			/*if (getInternalFrame() != null) {
				getInternalFrame().removeComponentListener(this);
				getInternalFrame().dispose();
			}*/
		}
		setRecording(null);
	}

	public String getTitle() {
		return getVideoImpl().getTitle();
	}

/*	public void toFront() {
		getVideoImpl().toFront();
	}*/

	public boolean isActive() {
		return getVideoImpl().isActive();
	}

	/**
	 * Merge the {@link ActionMap} of the implementation with the one of this instance..
	 */
	public ActionMap getApplicationActionMap() {
		if (actionMap == null && getVideoImpl() != null) {
			// get the action map of the abstractions
			ActionMap actionMap = getContext().getActionMap(SelfContained.class, this);
			ActionMap insertionReference = actionMap;
			// get the action map of the implementations
			Class<?> firstImplementor = getFirstImplementor(getVideoImpl().getClass(), IVideoComponent.class);
			ApplicationActionMap videoImplActionMap = getContext().getActionMap(firstImplementor, getVideoImpl());
			// the lastImplementor is the class whose hierarchy will be searched for IVideoComponent implementors
			Class<?> lastImplementor = getVideoImpl().getClass();
			Class<?> abstractionClass = getClass();
			// loop over all classes derived from VideoComponent
			while (VideoComponent.class.isAssignableFrom(abstractionClass)) {
				// get the next implementor for IVideoComponent in the hierarchy of startClass
				lastImplementor = getLastImplementor(lastImplementor, IVideoComponent.class);
				// this loop is necessary if there are more classes in the implementors hierarchy than in that of the abstraction
				while(videoImplActionMap.getActionsClass() != lastImplementor) {
					videoImplActionMap = (ApplicationActionMap) videoImplActionMap.getParent();
				}
				// now insert the found part of the implementation action map in the action map of the abstraction
				ActionMap oldParent = insertionReference.getParent();
				insertionReference.setParent(videoImplActionMap);
				// save a reference to the parent of the implementation's action map
				ApplicationActionMap tail = (ApplicationActionMap) videoImplActionMap.getParent();
				videoImplActionMap.setParent(oldParent);
				// set the implementation's action map to the parent of the action map that was inserted into the abstractions' action map
				videoImplActionMap = tail;
				// the next insertion point of the abstraction's action map will be it's parent before the insertion
				insertionReference = oldParent;
				lastImplementor = lastImplementor.getSuperclass();
				abstractionClass = abstractionClass.getSuperclass();
			}
			this.actionMap = new WeakReference<ActionMap>(actionMap);
		}
		return actionMap != null ? actionMap.get() : getContext().getActionMap(this);
	}

	/**
	 * Search a class hierarchy from bottom to top and return the first {@link Class} that implements the given interface.
	 * 
	 * @param theClass The {@link Class} whose hierarchy will be searched
	 * @param interf The interface
	 * @return The first {@link Class} implementing the given interface
	 */
	private Class<?> getFirstImplementor(Class<?> theClass, Class<?> interf) {
		List<Class<?>> allClasses = Lists.newArrayList();
		while(theClass != null) {
			allClasses.add(theClass);
			theClass = theClass.getSuperclass();
		}
		Collections.reverse(allClasses);
		for (Class<?> firstClass : allClasses) {
			if (implementsInterface(firstClass, interf)) {
				return firstClass;
			}
		}
		return null;
	}

	/**
	 * Check whether a {@link Class} implements the given interface.
	 * 
	 * @param theClass The class
	 * @param interf The interface
	 * @return <code>true</code> if the {@link Class} implements the interface
	 */
	private boolean implementsInterface(Class<?> theClass, Class<?> interf) {
		for (Class<?> implementedInterface : theClass.getInterfaces()) {
			if (interf.isAssignableFrom(implementedInterface)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Search a class hierarchy from top to bottom and return the first {@link Class} that implements the given interface.
	 * 
	 * @param theClass The {@link Class} whose hierarchy will be searched
	 * @param interf The interface
	 * @return The first {@link Class} implementing the given interface
	 */
	private Class<?> getLastImplementor(Class<?> theClass, Class<?> interf) {
		boolean recurse = !implementsInterface(theClass, interf);
		// if no result here then recurse
		return recurse ? getLastImplementor(theClass.getSuperclass(), interf) : theClass;
	}

	public enum State {
		PLAYING, RECORDING, IDLE, DISPOSED, STOPPED
	}

}