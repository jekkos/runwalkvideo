package com.runwalk.video.util;


import org.slf4j.LoggerFactory;

/**
 * This class will serve as an exception handler for the Event Dispatching Thread.
 * It will prevent the application from locking up completely when an event goes awry and is
 * unable to complete.
 * 
 * @author Jeroen Peelaerts
 *
 */
public class AWTExceptionHandler implements Thread.UncaughtExceptionHandler {

	public void handle(Throwable t) {
		try {
			LoggerFactory.getLogger(getClass()).error(t.getMessage(), t);
			// insert your exception handling code here
			// or do nothing to make it go away
		} catch (Throwable t1) {
			LoggerFactory.getLogger(getClass()).error("Second exception on EDT", t1);
			// don't let the exception get thrown out, will cause infinite looping!
		}
	}

	public static void registerExceptionHandler() {
		System.setProperty("sun.awt.exception.handler", AWTExceptionHandler.class.getName());
		Thread.setDefaultUncaughtExceptionHandler(new AWTExceptionHandler());
	}

	public void uncaughtException(Thread t, Throwable e) {
		LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
}