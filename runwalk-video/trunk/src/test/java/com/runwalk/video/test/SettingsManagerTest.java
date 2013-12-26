package com.runwalk.video.test;

import java.io.File;

import junit.framework.TestCase;

import com.runwalk.video.settings.DatabaseSettings;

/**
 * A {@link TestCase} for the {@link getSettingsManager()} functionality.
 * 
 * @author Jeroen Peelaerts
 */
public class SettingsManagerTest extends BaseTestCase {
		
	public void testSettingsFileCreation() {
		File expectedFile = new File(getStorageDir(), SETTINGS_FILE_NAME);
		File settingsFile = new File(getSettingsManager().getLocalStorageDir(), SETTINGS_FILE_NAME);
		assertEquals(expectedFile, settingsFile);
		// delete the settings file if it exists
		if (settingsFile.exists()) {
			assertTrue(settingsFile.delete());
		}
		// create jaxb context
		getSettingsManager().loadSettings();
	
		// test load/save functionality
		DatabaseSettings databaseSettings = new DatabaseSettings("dbUser", "dbPwd", "dbUrl");
		getSettingsManager().setDatabaseSettings(databaseSettings);
		getSettingsManager().saveSettings();
		
		// create a new settings manager and load those settings again
		getSettingsManager().loadSettings();
		assertEquals(databaseSettings.getUrl(), getSettingsManager().getDatabaseSettings().getUrl());
		assertEquals(databaseSettings.getUserName(), getSettingsManager().getDatabaseSettings().getUserName());
		assertEquals(databaseSettings.getPassword(), getSettingsManager().getDatabaseSettings().getPassword());
	}
	
	
}
