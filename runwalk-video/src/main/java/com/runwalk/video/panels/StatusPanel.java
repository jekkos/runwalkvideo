package com.runwalk.video.panels;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.TaskMonitor;

import com.runwalk.video.core.AppComponent;
import com.runwalk.video.settings.SettingsManager;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
@AppComponent
public class StatusPanel extends JPanel {
	
	private int busyIconIndex = 0;
	private final Icon[] busyIcons = new Icon[15];
	private final Timer busyIconTimer;

	private final Icon idleIcon;
	private final Timer messageTimer;
	private final JProgressBar progressBar;
	private final JLabel statusAnimationLabel;
	private JLabel statusMessageLabel;
	
	private TaskMonitor taskMonitor;

	public StatusPanel() {
		setLayout(new MigLayout("nogrid, fill", "", "rel[fill]"));
		statusMessageLabel = new JLabel();
		statusMessageLabel.setFont(SettingsManager.MAIN_FONT);
		statusMessageLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusAnimationLabel = new JLabel();
		progressBar = new JProgressBar();
		// status bar initialization - message timeout, idle icon and busy
		// animation, etc
		add(statusMessageLabel, "grow");
		add(statusAnimationLabel, "gapleft, push");
		add(progressBar, "width :140:");
		
		final int messageTimeout = getResourceMap().getInteger("StatusBar.messageTimeout");
		messageTimer = new Timer(messageTimeout, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessageLabel.setText("");
			}
		});
		messageTimer.setRepeats(false);
		final int busyAnimationRate = getResourceMap().getInteger("StatusBar.busyAnimationRate");

		for (int i = 0; i < busyIcons.length; i++) {
			busyIcons[i] = getResourceMap().getIcon("StatusBar.busyIcons[" + i + "]");
		}

		busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
				statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
			}
		});
		idleIcon = getResourceMap().getIcon("StatusBar.idleIcon");
		statusAnimationLabel.setIcon(idleIcon);
		
		// connecting action tasks to status bar via TaskMonitor
		taskMonitor = new TaskMonitor(getContext());
		taskMonitor.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				final String propertyName = evt.getPropertyName();
				if ("started".equals(propertyName)) {
					if (!busyIconTimer.isRunning()) {
						statusAnimationLabel.setIcon(busyIcons[0]);
						busyIconIndex = 0;
						busyIconTimer.start();
					}
					progressBar.setIndeterminate(true);
				} else if ("done".equals(propertyName)) {
					busyIconTimer.stop();
					statusAnimationLabel.setIcon(idleIcon);
					progressBar.setIndeterminate(false);
					progressBar.setValue(0);
				} else if ("message".equals(propertyName)) {
					final String text = (String) evt.getNewValue();
					showMessage(text == null ? "" : text);
					messageTimer.restart();
				} else if ("errorMessage".equals(propertyName)) {
					final String text = (String) evt.getNewValue();
					showErrorMessage(text == null ? "" : text);
				} else if ("progress".equals(propertyName)) {
					final int value = (Integer) evt.getNewValue();
					progressBar.setIndeterminate(false);
					progressBar.setValue(value);
				}
			}
		});
	}

	private void showMessage(Color theColor, String msg) {
		statusMessageLabel.setText(msg);
		statusMessageLabel.setForeground(theColor);
		messageTimer.restart();
	}
	
	public void showMessage(String msg) {
		LoggerFactory.getLogger(getClass()).info(msg);
		showMessage(Color.black, msg);
	}
	
	public void showErrorMessage(String error) {
		LoggerFactory.getLogger(getClass()).error(error);
		showMessage(Color.red, error);
	}
	
	public void setIndeterminate(boolean indeterminate) {
		getProgressBar().setIndeterminate(indeterminate);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

}
