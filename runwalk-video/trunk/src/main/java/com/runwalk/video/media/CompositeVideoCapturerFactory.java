package com.runwalk.video.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.runwalk.video.settings.VideoCapturerFactorySettings;
import com.runwalk.video.settings.VideoCapturerSettings;

public class CompositeVideoCapturerFactory extends VideoCapturerFactory.Adapter {

	private final List<VideoCapturerFactory<?>> videoCapturerFactories = new ArrayList<VideoCapturerFactory<?>>();
	
	public CompositeVideoCapturerFactory() { }
	
	/**
	 * Create a composite factory using the given {@link List} of settings. 
	 * A unchecked warning had to be suppressed because of the inability
	 * to specify parameter type information inside a class constant in java.
	 * 
	 * @param videoCapturerFactorySettingsList The list with factory setting beans
	 * @return The instantiated factory
	 */
	@SuppressWarnings("unchecked")
	public static <V extends VideoCapturerSettings> CompositeVideoCapturerFactory
		createInstance(List<VideoCapturerFactorySettings<?>> videoCapturerFactorySettingsList) {
		CompositeVideoCapturerFactory result = new CompositeVideoCapturerFactory();
		for (VideoCapturerFactorySettings<?> videoCapturerFactorySettings : videoCapturerFactorySettingsList) {
			createInstance(videoCapturerFactorySettings, VideoCapturerFactory.class); 
		}
		return result;
	}	
	
	@Override
	protected IVideoCapturer initializeCapturer(VideoCapturerSettings videoCapturerSettings) {
		// iterate over the capturer factories, find the first one and initialize
		for(VideoCapturerFactory<?> videoCapturerFactory : videoCapturerFactories) {
			if (videoCapturerFactory.getVideoCapturerNames().contains(videoCapturerSettings.getName())) {
				return videoCapturerFactory.initializeCapturer(videoCapturerSettings);
			}
		}
		return null;
	}

	@Override
	public Collection<String> getVideoCapturerNames() {
		List<String> capturerNames = new ArrayList<String>();
		for (VideoCapturerFactory<?> videoCapturerFactory : videoCapturerFactories) {
			if (capturerNames.addAll(videoCapturerFactory.getVideoCapturerNames())) {
				// TODO add some sort of separator item?? maybe later
			}
		}
		return capturerNames;
	}

}
