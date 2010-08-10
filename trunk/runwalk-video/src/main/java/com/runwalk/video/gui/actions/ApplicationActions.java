package com.runwalk.video.gui.actions;

import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.tasks.UploadLogFilesTask;
import com.runwalk.video.util.AppSettings;

public class ApplicationActions extends AbstractBean {
	private static final String REDO_ENABLED = "redoEnabled";
	private static final String UNDO_ENABLED = "undoEnabled";
	private UndoManager undo = new UndoManager();
	private boolean undoEnabled;
	private boolean redoEnabled;
	
	private final AppSettings appSettings;

	public ApplicationActions(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

	private class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			setUndoEnabled(e.getEdit().canUndo());
			setRedoEnabled(e.getEdit().canRedo());
		}
	}
	
	public MyUndoableEditListener getUndoableEditListener() {
		return new MyUndoableEditListener();
	}

	private void updateUndoActions() {
		setRedoEnabled(undo.canRedo());
		setUndoEnabled(undo.canUndo());
	}

	@Action(enabledProperty=UNDO_ENABLED)
	public void undo() {
		try {
			undo.undo();
		}
		catch( CannotUndoException ex) {
			Toolkit.getDefaultToolkit().beep();
		}
		updateUndoActions();
	}

	@Action(enabledProperty=REDO_ENABLED)
	public void redo() {
		try {
			undo.redo();
		}
		catch( CannotUndoException ex) {
			Toolkit.getDefaultToolkit().beep();
		}
		updateUndoActions();
	}

	public boolean isUndoEnabled() {
		return undoEnabled;
	}

	public boolean isRedoEnabled() {
		return redoEnabled;
	}

	public void discardAllEdits() {
		undo.discardAllEdits();
	}

	public void setUndoEnabled(boolean undoEnabled) {
		this.firePropertyChange(UNDO_ENABLED, this.undoEnabled, this.undoEnabled = undoEnabled);
	}

	public void setRedoEnabled(boolean redoEnabled) {
		this.firePropertyChange(REDO_ENABLED, this.redoEnabled, this.redoEnabled = redoEnabled);
	}

	@Action
	public void selectVideoDir() {
		File chosenDir = getAppSettings().getVideoDir();
		File result = selectDirectory(chosenDir);
		getAppSettings().setVideoDir(result);
	}
	
	@Action
	public void selectUncompressedVideoDir() {
		File chosenDir = getAppSettings().getUncompressedVideoDir();
		File result = selectDirectory(chosenDir);
		getAppSettings().setUncompressedVideoDir(result);
	}
	
	private File selectDirectory(File chosenDir) {
		final JFileChooser chooser = chosenDir == null ? new JFileChooser() : new JFileChooser(chosenDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showDialog(null, "Selecteer");
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	return chooser.getSelectedFile();
	    }
	    return chosenDir;
	}
	
	@Action
	public Task<Void, Void> uploadLogFiles() {
		return new UploadLogFilesTask(getAppSettings().getLogFile());
	}

	public AppSettings getAppSettings() {
		return appSettings;
	}
	
	@Action
	public void exit() {
		RunwalkVideoApp.getApplication().exit();
	}

}
