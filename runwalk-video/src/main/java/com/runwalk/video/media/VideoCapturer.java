package com.runwalk.video.media;

import java.awt.KeyboardFocusManager;
import java.awt.Robot;
import java.awt.Window;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

import com.google.common.collect.Iterables;
import com.runwalk.video.core.OnEdt;
import com.runwalk.video.tasks.AbstractTask;
import com.runwalk.video.ui.actions.ApplicationActionConstants;

public class VideoCapturer extends VideoComponent {

	public static final String TIME_RECORDING = "timeRecorded";

	public static final String CAPTURE_ENCODER_NAME = "captureEncoderName";

	private IVideoCapturer videoImpl;
	
	VideoCapturer() { }
	
	@Action
	public void setCaptureEncoderName() {
		List<String> captureEncoderNames = getVideoImpl().getCaptureEncoderNames();
		Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		String captureEncoderName =  (String) JOptionPane.showInputDialog(
				activeWindow,
				getResourceMap().getString("setCaptureEncoder.dialog.text"),
				getResourceMap().getString("setCaptureEncoder.dialog.title"),
				JOptionPane.PLAIN_MESSAGE,
				null,
				Iterables.toArray(captureEncoderNames, String.class),
				getCaptureEncoderName());
		if (captureEncoderName != null) {
			firePropertyChange(CAPTURE_ENCODER_NAME, getCaptureEncoderName(), captureEncoderName);
			getVideoImpl().setCaptureEncoderName(captureEncoderName);
			getLogger().debug("Video encoder for " + getTitle() + " changed to " + getCaptureEncoderName());
		}
	}

	public IVideoCapturer getVideoImpl() {
		return videoImpl;
	}
	
	public void setVideoImpl(IVideoCapturer videoImpl) {
		this.videoImpl = videoImpl;
	}

	@OnEdt
	@Override
	public void dispose() {
		super.dispose();
		setVideoImpl(null);
	}
	
	@Action(enabledProperty=STOPPED)
	public Task<Void, Void> disposeOnExit() {
		return new AbstractTask<Void, Void>(DISPOSE_ON_EXIT_ACTION) {

			protected Void doInBackground() throws Exception {
				String componentTitle = VideoCapturer.this.getTitle();
				getLogger().info("Waiting for " + componentTitle + " to become visible..");
				while(!isVisible() && !isDisposed() && getVideoImpl() != null) {
					// continue waiting for component activation or disposal
					if (getTaskService() != null && getTaskService().isShutdown()) {
						dispose();
					}
					Thread.yield();
				}
				getLogger().info("Waiting for " + componentTitle + " has ended. State is now " + VideoCapturer.this.getState());
				return null;
			}
			
		};
	}

	public void startRecording(String videoPath) {
		if (videoPath == null) {
			throw new IllegalArgumentException("No valid file or recording specified");
		} 
		setVideoPath(videoPath);
		getVideoImpl().startRecording(videoPath);
		getLogger().debug("Recording to file " + videoPath );
		setState(State.RECORDING);
	}

	public void stopRecording() {
		getVideoImpl().stopRecording();
		setIdle(true);
	}
	
	/**
	 * Return the currently used capture encoder name.
	 * @return The name of the encoder
	 */
	public String getCaptureEncoderName() {
		return getVideoImpl().getCaptureEncoderName();
	}
	
	@Action
	public Task<?, ?> showCapturerSettings() {
		return new AbstractTask<Void, Void>(ApplicationActionConstants.SHOW_CAPTURER_SETTINGS_ACTION) {

			protected Void doInBackground() throws Exception {
				message("startMessage");
				if (getVideoImpl().showCapturerSettings()) {
					setIdle(true);
				}
				return null;
			}
		};
	}

	@Action
	public Task<?, ?> showCameraSettings() {
		return new AbstractTask<Void, Void>(ApplicationActionConstants.SHOW_CAMERA_SETTINGS_ACTION) {

			protected Void doInBackground() throws Exception {
				message("startMessage");
				if (getVideoImpl().showCameraSettings()) {
					setIdle(true);
				}
				return null;
			}

		};
		
	}

	public boolean isRecording() {
		return getState() == State.RECORDING;
	}

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", super.getTitle());
	}

}
