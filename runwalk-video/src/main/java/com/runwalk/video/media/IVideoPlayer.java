package com.runwalk.video.media;

public interface IVideoPlayer extends IVideoComponent {

	/**
	 * Open a video file in and prepare it for playback. An implementation should return <code>true</code> if the native
	 * resources behind the player were disposed and rebuilt. For example with DirectShow it's possible to reuse an existing filtergraph
	 * by reconnecting it to a new source filter. Still, it may take a tiny change in the source media type to make the reconnection of an 
	 * existing graph to a new source filter fail. This will require the code to dispose resources and rebuild the filtergraph to open another videofile.
	 * 
	 * @see DSJPlayer#loadVideo(String)
	 * @param path The path to the file or url to open
	 * @return <code>true</code> if the native resources were disposed and rebuilt
	 */
	boolean loadVideo(String path);


	/**
	 * Return the current playback position in milliseconds.
	 * 
	 * @return The playback position
	 */
	int getPosition();

	/**
	 * Set the current playback position for this player in milliseconds.
	 * 
	 * @param pos The playback position
	 */
	void setPosition(int pos);

	/**
	 * Return the duration of the current openend video file in milliseconds.
	 * 
	 * @return The duration
	 */
	int getDuration();
	
	/**
	 * Stop playback and reset the playback position to zero.
	 */
	void stop();

	/**
	 * Start playback.
	 */
	void play();

	/**
	 * Pause playback.
	 */
	void pause();
	
	/**
	 * Set the volume level for playback, currenly not used as no sound is recorded anyway.
	 * 
	 * @param volume The volume level
	 */
	void setVolume(float volume);
	
	/**
	 * Return the volume level for the current openend video.
	 * 
	 * @return The volume level
	 */
	float getVolume();
	
	/**
	 * Set the playback rate. The default rate is <code>1.0f</code>. Some predefined playback rates are defined in {@link SettingsManager#PLAY_RATES}
	 * 
	 * @param rate The playback rate
	 */
	void setPlayRate(float rate);
	
	/**
	 * Return the current playback rate.
	 * 
	 * @return The playback rate
	 */
	float getPlayRate();
	
}