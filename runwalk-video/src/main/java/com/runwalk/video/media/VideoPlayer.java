package com.runwalk.video.media;

import java.awt.Color;
import java.util.Arrays;
import java.util.TreeSet;

import org.jdesktop.application.Action;

import com.google.common.collect.Sets;
import com.runwalk.video.core.OnEdt;

public class VideoPlayer extends VideoComponent {
	
	public final static TreeSet<Float> PLAY_RATES = Sets.newTreeSet(Arrays.asList(0.05f, 0.10f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.50f, 1.75f, 2.0f));

	public static final String POSITION = "position";

	private static final String MUTED = "muted";

	private int position;

	private float volume;

	private IVideoPlayer playerImpl;

	private boolean muted = false;

	VideoPlayer(String path, IVideoPlayer playerImpl) {
		this.playerImpl = playerImpl;
		loadVideo(path);
		setIdle(true);
	}

	public boolean loadVideo(String videoPath) {
		boolean result = getVideoImpl().loadVideo(videoPath);
		setVideoPath(videoPath);
		setPosition(0);
		return result;
	}

	public void pause() {
		setState(State.IDLE);
		getVideoImpl().pause();
	}

	public void play() {
		setState(State.PLAYING);
		// clear all previously drawn overlays
		clearOverlayImage();
		getVideoImpl().startRunning();
		getVideoImpl().play();
	}

	public void stop() {
		setState(State.IDLE);
		// clear all previously drawn overlays
		clearOverlayImage();
		getVideoImpl().stop();
		// set position to 0 here and for player this instance and its 'native' implementation
		setPosition(0);
	}

	private void clearOverlayImage() {
		if (isOverlayed()) {
			getVideoImpl().setOverlayImage(null, Color.white);
		}
	}

	/**
	 * Set the current playback position of the player. If the position is greater than the duration of the
	 * currently loaded video, then it will be set to 0.
	 * 
	 * @param position The playback position
	 */
	public void setPosition(int position) {
		if (position >= getDuration()) {
			getLogger().debug("Position is larger than duration, resetting to 0");
			position = 0;
		}
		getVideoImpl().setPosition(position);
		firePropertyChange(POSITION, this.position, this.position = position);
	}

	public float slower() {
		float playRate = getPlayRate();
		Float newPlayRate = PLAY_RATES.lower(playRate);
		if (newPlayRate != null) {
			playRate = newPlayRate;
			setPlayRate(newPlayRate);
		}
		return playRate;
	}

	public float faster() {
		Float playRate = getPlayRate();
		Float newPlayRate = PLAY_RATES.higher(playRate);
		if (newPlayRate != null) {
			playRate = newPlayRate;
			setPlayRate(newPlayRate);
		}
		return playRate;
	}

	public float getPlayRate() {
		return getVideoImpl().getPlayRate();
	}

	private void setPlayRate(float rate) {
		getVideoImpl().startRunning();
		getVideoImpl().setPlayRate(rate);
	}

	public void pauseIfPlaying() {
		if (isPlaying()) {
			pause();
		}
	}

	public int getKeyframePosition() {
		int position = getPosition();
		getLogger().debug("Position found: " + position);
		setPosition(position);
		position = getPosition();
		getLogger().debug("Final position: " + position);
		return position;
	}

	public void increaseVolume() {
		getVideoImpl().setVolume(1.25f * getVideoImpl().getVolume());
	}

	public void decreaseVolume() {
		getVideoImpl().setVolume( getVideoImpl().getVolume() / 1.25f);
	}

	public boolean isMuted() {
		return getVideoImpl().getVolume() > 0;
	}

	public void setMuted(boolean muted) {
		firePropertyChange(MUTED, this.muted, this.muted = muted);
	}

	@Action(selectedProperty = MUTED)
	public void toggleMuted() {
		// save volume settings
		volume = getVideoImpl().getVolume();
		getVideoImpl().setVolume(isMuted() ? 0f : volume);
	}

	public int getDuration() {
		return getVideoImpl().getDuration();
	}

	public int getPosition() {
		return getVideoImpl().getPosition();
	}

	public IVideoPlayer getVideoImpl() {
		return playerImpl;
	}

	public void setVideoImpl(IVideoPlayer playerImpl) {
		this.playerImpl = playerImpl;
	}

	@OnEdt
	@Override
	public void dispose() {
		super.dispose();
		setVideoImpl(null);
	}

	public boolean isPlaying() {
		return getState() == VideoComponent.State.PLAYING;
	}

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", getMonitorId());
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

	public void startRunning() {
		if (getVideoImpl() != null) {
			getVideoImpl().startRunning();
			setIdle(true);
		}
	}

	public void stopRunning() {
		if (getVideoImpl() != null) {
			getVideoImpl().stopRunning();
			setIdle(false);
		}
	}

}