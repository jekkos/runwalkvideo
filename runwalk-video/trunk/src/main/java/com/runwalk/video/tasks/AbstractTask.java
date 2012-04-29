package com.runwalk.video.tasks;

import java.awt.KeyboardFocusManager;
import java.awt.Window;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;

public abstract class AbstractTask<T, V> extends Task<T, V> {
	private static final String ERROR_MESSAGE = "errorMessage";
	
	private final Logger logger;
	
	@SuppressWarnings("deprecation")
	public AbstractTask(String name) {
		super(Application.getInstance(), name);
		logger = Logger.getLogger(getClass());
	}

	protected void errorMessage(String formatResourceKey, Object... args) { 
    	ResourceMap resourceMap = getResourceMap();
    	if (resourceMap != null) {
    		firePropertyChange(ERROR_MESSAGE, getMessage(), getResourceString(formatResourceKey, args));
    	}
    }
	
	protected String getResourceString(String string, Object... args) {
		ResourceMap resourceMap = getResourceMap();
		String result = null;
    	if (resourceMap != null) {
    		result = getResourceMap().getString(resourceName(string), args);
    	}
    	return result;
	}
	
	@Override
	protected void failed(Throwable throwable) {
		super.failed(throwable);
		Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		if (activeWindow == null || !activeWindow.getName().equals("mainFrame")) {
			activeWindow = null;
		}
		getLogger().error(throwable.getMessage(), throwable);
		JOptionPane.showMessageDialog(
				activeWindow, 
				getErrorMessage(throwable),
				getResourceString(ERROR_MESSAGE), 
				JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Fetch a human readable error description from the {@link Task}'s property file.
	 * 
	 * @param throwable The throwable to find an error description for
	 * @return The error description
	 */
	private Object getErrorMessage(Throwable throwable) {
		String simpleClassName = throwable.getClass().getSimpleName();
		String resourceSuffix = Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
		String errorMessage = getResourceString(resourceName(resourceSuffix));
		return errorMessage == null ? throwable : errorMessage;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
}
