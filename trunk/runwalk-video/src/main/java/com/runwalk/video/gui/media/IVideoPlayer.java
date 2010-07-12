package com.runwalk.video.gui.media;

import java.io.File;

public interface IVideoPlayer extends IVideoComponent {

	public boolean loadFile(File videoFile);

	public int getPosition();

	public void setPosition(int pos);

	public int getDuration();

	public void stop();

	public void play();

	public void pause();
	
	public void setVolume(float volume);
	
	public float getVolume();
	
	public void setPlayRate(float rate);
	
	public float getPlayRate();

}