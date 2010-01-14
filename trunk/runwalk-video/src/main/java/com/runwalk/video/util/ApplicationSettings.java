package com.runwalk.video.util;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.beansbinding.ELProperty;

import com.runwalk.video.RunwalkVideoApp;

import de.humatic.dsj.DSFilterInfo;

@SuppressWarnings("serial")
public class ApplicationSettings implements Serializable {

	public final static float[] PLAY_RATES = new float[] {0.05f, 0.10f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.50f, 1.75f, 2.0f};
	
	public final static DSFilterInfo[] VIDEO_ENCODERS = {
					DSFilterInfo.doNotRender(), 
					DSFilterInfo.filterInfoForProfile("RunwalkVideoApp"),
					DSFilterInfo.filterInfoForName("XviD MPEG-4 Codec")
	};
	
	//FIXME dit zou terug uit een resourceMap moeten gehaald worden.
	public static Font MAIN_FONT = new Font("Geneva", Font.PLAIN, 11);  //= ApplicationUtil.getResourceMap(ApplicationSettings.class).getFont("Application.mainFont").deriveFont(11f);

	public final static String FILE_ENCODING = "UTF-8";
	
	private final static String FILE_APPENDER_NAME = "A1";
	//FIXME logger is instantiated too early..
	private static Logger logger;

	private final static String CAMERADIR_RESOURCENAME = "Application.cameraDir";

	private final static String VIDEODIR_RESOURCENAME = "Application.videoDir";

	private final static String SETTINGS_XML = "settings.xml";

	private final static String TEMP_VIDEO_DIRNAME = "uncompressed";

	private final static ApplicationSettings INSTANCE = new ApplicationSettings();

	private File logFile;

	private JAXBContext jaxbContext;
	
	private Settings settings;
	
	private ApplicationSettings() {
		settings = new Settings();
	}

	public synchronized static ApplicationSettings getInstance() {
		return INSTANCE;
	}
	
	public void loadSettings() {
		logger.debug("Initializing ApplicationSettings..");
		ApplicationContext appContext = RunwalkVideoApp.getApplication().getContext();
		try {
			if (jaxbContext == null) {
				logger.debug("Instantiating JAXB context..");
				jaxbContext = JAXBContext.newInstance( Settings.class );
			}
			File settingsFile = new File(appContext.getLocalStorage().getDirectory(), SETTINGS_XML);
			logger.debug("Loading application settings from file " + settingsFile.getAbsolutePath());
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			settings = (Settings) unmarshaller.unmarshal(settingsFile);
		} catch(JAXBException jaxbExc) {
			logger.error("Exception while instantiating JAXB context", jaxbExc);
		} catch(Exception exc) {
			logger.error("Exception thrown while loading settings from file " + SETTINGS_XML, exc);
			if (exc.getMessage() != null) {
				logger.error("Settings file " + SETTINGS_XML + " seems to be corrupt. Attempting to delete..", exc);
				try {
					appContext.getLocalStorage().deleteFile(SETTINGS_XML);
					logger.warn("Settings file deleted. Default settings will be applied");
				} catch (IOException e) {
					logger.error("Removing corrupt settings file failed", e);
				}
			}
		} finally {
			settings = settings == null ? new Settings() : settings;
		}
		Logger.getLogger(getClass()).debug("Found videodir: " + getVideoDir().getAbsolutePath());
	}

	public void saveSettings() {
		OutputStream fos = null;
		ApplicationContext appContext = RunwalkVideoApp.getApplication().getContext();
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, FILE_ENCODING);
			File settingsFile = new File(appContext.getLocalStorage().getDirectory(), SETTINGS_XML);
			fos = appContext.getLocalStorage().openOutputFile(settingsFile.getName());
			logger.debug("Writing application settings to file " + settingsFile.getName());
			marshaller.marshal(settings, fos);
		} catch (Exception e) {
			logger.error("Exception thrown while saving settings to file " + SETTINGS_XML, e);
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					logger.error("Exception thrown while flushing or closing outputstream for file " + SETTINGS_XML, e);
				}
			}
		}
	}

	public void setVideoDir(File dir) {
		settings.setVideoDir(dir.getAbsolutePath());
	}

	public File getDirectory(String path, String resourceName) {
		File dir = ApplicationUtil.parseDir(path);
		if (path == null || dir == null) {
			String defaultDir = ApplicationUtil.getString(this, resourceName);
			dir = ApplicationUtil.parseDir(defaultDir);
			if (dir == null) {
				dir = new File(System.getProperty("user.dir"));
			}
			settings.setVideoDir(dir.getAbsolutePath());
		}
		return dir;
	}

	public File getCameraDir() {
		return getDirectory(settings.getCameraDir(), CAMERADIR_RESOURCENAME);
	}

	public File getVideoDir() {
		return getDirectory(settings.getVideoDir(), VIDEODIR_RESOURCENAME);
	}

	public File getUncompressedVideoDir() {
		File tempDir = new File(getVideoDir(), TEMP_VIDEO_DIRNAME);
		if (!ApplicationUtil.parseDir(tempDir)) {
			try {
				boolean success = tempDir.mkdir();
				if (success) {
					logger.debug("Directory " + tempDir.getAbsolutePath() + " created.");
				}
			} catch(SecurityException excp) {
				logger.error("Directory " + tempDir.getAbsolutePath() + " couldn't be created.", excp);
			}
		}
		return tempDir;
	}
	
	public File getLogFile() {
		if (logFile == null || !logFile.exists()) {
			FileAppender appndr = (FileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
			String fileName = appndr.getFile();
			logFile = new File(fileName);
		}
		return logFile;
	}
	
	public static void configureLog4j() {
		PropertyConfigurator.configure(Thread.currentThread().getContextClassLoader().getResource("META-INF/log4j.properties"));
		FileAppender appndr = (FileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
		org.jdesktop.beansbinding.util.logging.Logger.getLogger(ELProperty.class.getName()).setLevel(Level.SEVERE);
		logger = Logger.getLogger(ApplicationSettings.class);
		logger.debug("Logging to file with location " +appndr.getFile());
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	@XmlRootElement
	public static class Settings {
		@XmlElement
		private String videoDir, cameraDir;
		
		@XmlElement
		private int rateIndex = 3;
		
		private float savedVolume;
		//TODO hoe dit naar xml schrijven?
		@XmlTransient
		private DSFilterInfo transcoder = ApplicationSettings.VIDEO_ENCODERS[2];
		
		public float getSavedVolume() {
			return savedVolume;
		}

		public void setSavedVolume(float savedVolume) {
			this.savedVolume = savedVolume;
		}

		private String getVideoDir() {
			return videoDir;
		}
		
		private String getCameraDir() {
			return cameraDir;
		}

		private void setVideoDir(String videoDir) {
			this.videoDir = videoDir;
		}
		
		@XmlTransient
		public int getRateIndex() {
			return rateIndex;
		}

		public void setRateIndex(int rateIndex) {
			this.rateIndex = rateIndex;
		}

		@XmlTransient
		public DSFilterInfo getTranscoder() {
			return transcoder;
		}
		
	}
}
