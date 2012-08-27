package com.runwalk.video.media;

import org.apache.log4j.Logger;

import com.runwalk.video.settings.VideoComponentFactorySettings;
import com.runwalk.video.settings.VideoComponentSettings;

public class VideoComponentFactory<T extends VideoComponentSettings> {
	
	protected final static Logger LOGGER = Logger.getLogger(VideoCapturerFactory.class);
	
	private final Class<T> videoComponentSettingsClass;

	private VideoComponentFactorySettings<T> videoComponentFactorySettings;
	
	public VideoComponentFactory(Class<T> videoComponentSettingsClass) {
		this.videoComponentSettingsClass = videoComponentSettingsClass;
	}

	/**
	 * Create a factory with explicitly type information. Use this method whenever possible as it typesafer in comparison to
	 * its overloaded one argument version.
	 * 
	 * @param videoComponentFactorySettings The factory settings bean
	 * @param theClass Type information of the factory to be created
	 * @return The instantiated factory
	 */
	public static <T extends VideoComponentFactory<V>, V extends VideoComponentSettings> T 
			createInstance(VideoComponentFactorySettings<V> videoComponentFactorySettings, Class<? extends T> theClass) {
		T result = null;
		try {
			Class<?> factoryClass = Class.forName(videoComponentFactorySettings.getVideoComponentFactoryClassName());
			result = factoryClass.asSubclass(theClass).newInstance();
			// apply settings to the factory..
			result.loadVideoCapturerFactorySettings(videoComponentFactorySettings);
		} catch (Throwable e) {
			// any kind of error during initialization..
			// return a dummy factory if fails
			LOGGER.error("Exception while instantiating factory", e);
		}
		return result;
	}
	
	protected T createSettingsBean(String videoCapturerName) {
		try {
			T newInstance = getVideoComponentSettingsClass().newInstance();
			// eventueel een setter die de waarde van t op de bean gooit
			newInstance.setName(videoCapturerName);
			return newInstance;
		} catch (InstantiationException e) {
			LOGGER.error("Exception while instantiating settings bean", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Exception while instantiating settings bean", e);
		}
		return null;
	}
	
	public void loadVideoCapturerFactorySettings(VideoComponentFactorySettings<T> videoCapturerFactorySettings) {
		this.videoComponentFactorySettings = videoCapturerFactorySettings;
	}

	public VideoComponentFactorySettings<T> getVideoComponentFactorySettings() {
		return videoComponentFactorySettings;
	}

	public Class<T> getVideoComponentSettingsClass() {
		return videoComponentSettingsClass;
	}

}