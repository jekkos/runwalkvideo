package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.image.BufferedImage;

import javax.swing.ActionMap;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;

import com.runwalk.video.core.AppComponent;
import com.runwalk.video.core.Containable;
import com.runwalk.video.core.OnEdt;
import com.runwalk.video.core.PropertyChangeSupport;
import com.runwalk.video.core.SelfContained;

/**
 * This abstraction allows you to make easy reuse of the common video UI functionality  
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen Peelaerts
 *
 */
@AppComponent
public abstract class VideoComponent implements PropertyChangeSupport {

	public static final String STATE = "state";
	// different states for the component
	public static final String IDLE = "idle";
	public static final String DISPOSED = "disposed";
	public static final String STOPPED = "stopped";
	
	public static final String DISPOSE_ON_EXIT_ACTION = "disposeOnExit";

	private ApplicationActionMap actionMap;
	private volatile State state;
	private boolean overlayed;
	
	private String videoPath;
	
	protected VideoComponent() {
		setIdle(false);
	}

	public abstract IVideoComponent getVideoImpl();
	
	/**
	 * Return <code>true</code> if the capturer implementation's 
	 * focus state can be tracked by the {@link KeyboardFocusManager}.
	 * @return <code>true</code> if the window's focus state can be tracked
	 */
	public boolean isFocusable() {
		return getVideoImpl() instanceof Containable && !getVideoImpl().isNativeWindowing();
	}
	
	/**
	 * Returns <code>true</code> if the component is currently visible on screen.
	 * 
	 * @return <code>true</code> if component is visible on screen
	 */
	public boolean isVisible() {
		return getVideoImpl() != null ? getVideoImpl().isVisible() : false;
	}

	/**
	 * Returns the monitor ID on which this component is shown.
	 * Should return <code>null</code> in case the component is running
	 * on the default (main) monitor.
	 * 
	 * @return The monitor number
	 */
	public Integer getMonitorId() {
		return getVideoImpl().getMonitorId();
	}
	
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

	protected void setState(State state) {
		firePropertyChange(STATE, this.state, this.state = state);
	}

	public State getState() {
		return state;
	}

	public void setIdle(boolean idle) {
		State oldState = state;
		state = idle ? State.IDLE : State.STOPPED;
		firePropertyChange(IDLE, oldState == State.IDLE, isIdle());
		firePropertyChange(STOPPED, oldState == State.STOPPED, isStopped());
	}
	
	public boolean isIdle() {
		return getState() == State.IDLE;
	}

	public void setStopped(boolean stopped) {
		State oldState = state;
		state = stopped ? State.STOPPED : State.IDLE;
		firePropertyChange(STOPPED, oldState == State.STOPPED, isStopped());
		firePropertyChange(IDLE, oldState == State.IDLE, isIdle());
	}
	
	public boolean isStopped() {
		return getState() == State.STOPPED;
	}

	public boolean isDisposed() {
		return getState() == State.DISPOSED;
	}
	
	/**
	 * Dispose the current video implementation and remove the component from the
	 * application's windowing system. This method should be called on the Event Dispatching
	 * Thread, as it will fire a couple of listeners that need to be executed synchronously. 
	 * 
	 * Be aware that you will need to use the {@link OnEdt} annotation on this method again 
	 * when overriding.
	 */
	@OnEdt
	@Action(enabledProperty = IDLE)
	public void dispose() {
		if (!isDisposed()) {
			// fire a PCE before disposing the implementation
			setState(State.DISPOSED);
			if (getVideoImpl() != null) {			
				// dispose on the video implementation will dispose resources for the full screen frame
				getVideoImpl().dispose();
			}
			setVideoPath(null);
			actionMap = null;
		}
	}
	
	public String getTitle() {
		return getVideoImpl().getTitle();
	}

	public boolean isActive() {
		return getVideoImpl().isActive();
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
	
	/**
	 * Merge the {@link ActionMap} of the implementation with the one of this instance.
	 * The result will be cached and wrapped using a WeakReference.
	 * 
	 * @return The merged ActionMap
	 */
	public ApplicationActionMap getApplicationActionMap() {
		if (actionMap == null && getVideoImpl() != null) {
			// get the action map of the abstractions
			ApplicationActionMap actionMap = getContext().getActionMap(VideoComponent.class, this);
			// get the action map of the implementations
			ApplicationActionMap implActionMap = null, parentImplActionMap = null;
			boolean isSelfContained = getVideoImpl() instanceof SelfContained;
			Class<?> startClass = isSelfContained ? SelfContained.class : Containable.class;
			implActionMap = parentImplActionMap = getApplicationActionMap(startClass, getVideoImpl());
			while (parentImplActionMap.getParent() != null && parentImplActionMap.getParent() != getContext().getActionMap()) {
				parentImplActionMap = (ApplicationActionMap) parentImplActionMap.getParent();
			}
			parentImplActionMap.setParent(actionMap);			
			this.actionMap = implActionMap;
		}
		return actionMap != null ? actionMap : getContext().getActionMap(VideoComponent.class, this);
	}

	public void startRunning() {
		if (getVideoImpl() != null) {
			getVideoImpl().startRunning();
			setIdle(true);
		}
	}

	public void stopRunning() {
		if (getVideoImpl() != null) {
			getVideoImpl().stopRunning();
			setStopped(true);
		}
	}

	/**
	 * This method simply invokes {@link #startRunning()} if the video component is stopped 
	 * or {@link #stopRunning()} if the component is running at invocation time.
	 */
	@Action(selectedProperty = IDLE)
	public void togglePreview() {
		if (!isIdle()) {
			stopRunning();
		} else {
			startRunning();
		}
	}

	public enum State {
		PLAYING, RECORDING, IDLE, DISPOSED, STOPPED;
	}

}