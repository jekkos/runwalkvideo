package com.runwalk.video;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditListener;

import com.sun.jna.internal.ReflectionUtils;
import com.sun.jnlp.JNLPClassLoader;
import com.sun.jnlp.JNLPClassLoaderUtil;
import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;
import org.jdesktop.application.utils.AppHelper;

import com.runwalk.video.core.Containable;
import com.runwalk.video.dao.CompositeDaoService;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.JpaDaoService;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.CompositeVideoCapturerFactory;
import com.runwalk.video.media.CompositeVideoPlayerFactory;
import com.runwalk.video.media.MediaControls;
import com.runwalk.video.media.settings.VideoComponentFactorySettings;
import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.model.RecordingModel;
import com.runwalk.video.panels.AbstractPanel;
import com.runwalk.video.panels.AbstractTablePanel;
import com.runwalk.video.panels.AbstractTablePanel.ClickHandler;
import com.runwalk.video.panels.AnalysisTablePanel;
import com.runwalk.video.panels.CustomerInfoPanel;
import com.runwalk.video.panels.CustomerTablePanel;
import com.runwalk.video.panels.RecordingTablePanel;
import com.runwalk.video.panels.StatusPanel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.AbstractTask;
import com.runwalk.video.tasks.RefreshTask;
import com.runwalk.video.tasks.UploadLogFilesTask;
import com.runwalk.video.ui.AnalysisModelTableFormat;
import com.runwalk.video.ui.AppInternalFrame;
import com.runwalk.video.ui.CustomerModelTableFormat;
import com.runwalk.video.ui.RecordingModelTableFormat;
import com.runwalk.video.ui.VideoMenuBar;
import com.runwalk.video.ui.WindowManager;
import com.runwalk.video.ui.actions.ApplicationActionConstants;
import com.runwalk.video.ui.actions.ApplicationActions;
import com.runwalk.video.ui.actions.MediaActionConstants;
import com.runwalk.video.util.AWTExceptionHandler;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;
import org.jdesktop.el.impl.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class of the application.
 */
public class RunwalkVideoApp extends SingleFrameApplication implements ApplicationActionConstants, MediaActionConstants {

	public static final String APP_VERSION = "Application.version";
	public static final String APP_TITLE = "Application.title";
	public static final String APP_NAME = "Application.name";
	public static final String APP_BUILD_DATE = "Application.build.date";
	public static final String APP_MAIN_FONT = "Application.mainFont";

	private final static Logger LOGGER = LoggerFactory.getLogger(RunwalkVideoApp.class);

	private static final String SAVE_NEEDED = "saveNeeded";

	private static final String DIRTY = "dirty";

	private List<AbstractPanel> panels = new ArrayList<AbstractPanel>();
	private CustomerTablePanel customerTablePanel;
	private AnalysisTablePanel analysisTablePanel;
	private RecordingTablePanel recordingTablePanel;
	private CustomerInfoPanel customerInfoPanel;
	private MediaControls mediaControls;
	private Containable customerMainView;
	private VideoMenuBar menuBar;
	private StatusPanel statusPanel;
	private ApplicationActions applicationActions;
	private JScrollableDesktopPane scrollableDesktopPane;
	private VideoFileManager videoFileManager;
	private DaoService daoService;
	private SettingsManager settingsManager;

	private boolean saveNeeded = false;

	/**
	 * This listener will listen to the table panel's event firing
	 */
	private final PropertyChangeListener dirtyListener = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (DIRTY.equals(event.getPropertyName())) {
				setSaveNeeded((Boolean) event.getNewValue() || isSaveNeeded());
			}
		}

	};

	/**
	 * A convenient static getter for the application instance.
	 * @return the instance of RunwalkVideoApp
	 */
	public static RunwalkVideoApp getApplication() {
		return Application.getInstance(RunwalkVideoApp.class);
	}

	/*
	 * Main method launching the application. 
	 * After logging has been set up, the application will launch using the swing application framework (SAF).
	 */
	public static void main(String[] args) {
		SettingsManager.configureLogging();
		LOGGER.info("Detected platform is " + AppHelper.getPlatform());
		launch(RunwalkVideoApp.class, args);
	}

	/**
	 *  sets the default font for all Swing components.
	 *   ex. 
	 *     setUIFont (new javax.swing.plaf.FontUIResource
	 *        ("Serif",Font.ITALIC,12));
	 * @param f the font resource
	 */
	public static void setUIFont (javax.swing.plaf.FontUIResource f){
		java.util.Enumeration<?> keys = UIManager.getLookAndFeelDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put (key, f);
		}
	}    

	/*
	 * Application lifecycle methods
	 */

	/** {@inheritDoc} */
	@Override
	protected void ready() {
		// load data from the db using the BSAF task mechanism
		executeAction(getContext().getActionMap(), REFRESH_ACTION);
	}

	/** {@inheritDoc} */ 
	@Override
	protected void initialize(String[] args) { 
		LOGGER.info("Starting {}", getTitle());
		// register an exception handler on the EDT
		AWTExceptionHandler.registerExceptionHandler();
		ApplicationContext appContext = Application.getInstance().getContext();
		//setUIFont(new FontUIResource(appContext.getResourceMap().getFont(APP_MAIN_FONT)));
		settingsManager = new SettingsManager(appContext.getLocalStorage().getDirectory());
		getSettingsManager().loadSettings();
		// set application default font
		// create daoServices and add them to the composite
		DaoService jpaDaoService = new JpaDaoService(getSettingsManager().getDatabaseSettings(), getName());
		daoService = new CompositeDaoService(jpaDaoService);
		// create video file manager
		videoFileManager = new VideoFileManager(getSettingsManager());
	}

	/**
	 * Initialize and show the application GUI.
	 */
	protected void startup() {
		// create common application actions class
		applicationActions = new ApplicationActions();
		statusPanel = new StatusPanel();
		customerTablePanel = new CustomerTablePanel(getVideoFileManager(), getDaoService());
		addTablePanel(customerTablePanel);
		customerInfoPanel = new CustomerInfoPanel(getCustomerTablePanel(), createUndoableEditListener());
		addTablePanel(customerInfoPanel);
		analysisTablePanel = new AnalysisTablePanel(getCustomerTablePanel(), createUndoableEditListener(), 
				settingsManager, getVideoFileManager(), getDaoService());
		addTablePanel(analysisTablePanel);
		recordingTablePanel = new RecordingTablePanel(getSettingsManager(), getVideoFileManager(), getDaoService());
		addTablePanel(recordingTablePanel);
		// create main desktop scrollpane
		scrollableDesktopPane = new JScrollableDesktopPane();

		getMainFrame().add(getScrollableDesktopPane());
		// create menu bar
		menuBar = new VideoMenuBar();
		// create window manager
		WindowManager windowManager = new WindowManager(getMenuBar(), getScrollableDesktopPane());
		// create mediaplayer controls
		List<VideoComponentFactorySettings<?>> videoCapturerFactorySettingsList = getSettingsManager().getVideoCapturerFactorySettings();
		List<VideoComponentFactorySettings<?>> videoPlayerFactorySettingsList = getSettingsManager().getVideoPlayerFactorySettings();
		// create video capturer factory
		CompositeVideoCapturerFactory videoCapturerFactory = CompositeVideoCapturerFactory.createInstance(videoCapturerFactorySettingsList);
		CompositeVideoPlayerFactory videoPlayerFactory = CompositeVideoPlayerFactory.createInstance(videoPlayerFactorySettingsList);
		mediaControls = new MediaControls(getSettingsManager(), getVideoFileManager(), 
				windowManager, getDaoService(), videoCapturerFactory, videoPlayerFactory,
				getAnalysisTablePanel());
		mediaControls.startVideoCapturer();
		// set tableformats for the two last panels
		customerTablePanel.setTableFormat(new CustomerModelTableFormat(customerTablePanel.getResourceMap()));
		analysisTablePanel.setTableFormat(new AnalysisModelTableFormat(analysisTablePanel.getResourceMap(), getVideoFileManager()));
		analysisTablePanel.registerClickHandler(new ClickHandler<AnalysisModel>() {
			
			public void handleClick(AnalysisModel element) {
				if (getVideoFileManager().isRecorded(element.getRecordings())) {
					getContext().getTaskService().execute(mediaControls.openRecordings(element.getRecordings()));
				}
			}
			
		});
		recordingTablePanel.setTableFormat(new RecordingModelTableFormat(recordingTablePanel.getResourceMap(), getVideoFileManager()));
		recordingTablePanel.registerClickHandler(new ClickHandler<RecordingModel>() {
			
			public void handleClick(RecordingModel element) {
				if (getVideoFileManager().isRecorded(element.getEntity())) {
					List<Recording> recordings = Collections.singletonList(element.getEntity());
					getContext().getTaskService().execute(mediaControls.openRecordings(recordings));
				}
			}
			
		});
		//recordingTablePanel.registerClickHandler(getMediaControls().getClickHandler());
		// create the main panel that holds customer and analysis controls & info
		customerMainView = createMainView();
		// add all internal frames here!!!
		getMainFrame().setJMenuBar(getMenuBar());
		// add the window to the WINDOW menu
		windowManager.addWindow(getMediaControls());
		windowManager.addWindow(getCustomerMainView());
		// create a custom property to support saving internalframe sessions state
		getContext().getSessionStorage().putProperty(AppInternalFrame.class, new AppInternalFrame.InternalFrameProperty());
		// show the main frame, all its session settings from the last time will be restored
		show(getMainFrame());
	}

	@Override
	public void exit(final EventObject event) {
		if (getContext().getTaskService().isTerminated()) {
			LOGGER.debug("Taskservice terminated. byebye...");
			super.exit(event);
		} else {
			ResourceMap resourceMap = getContext().getResourceMap();
			int result = JOptionPane.showConfirmDialog(getMainFrame(), 
					resourceMap.getString("quit.confirmDialog.text"), 
					resourceMap.getString("quit.Action.text"), JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				LOGGER.debug("Shutdown initiated...");
				//executeAction(getApplicationActionMap(), "uploadLogFiles");
				executeAction(getApplicationActionMap(), SAVE_SETTINGS_ACTION);
				if (isSaveNeeded()) {
					executeAction(getApplicationActionMap(), SAVE_ACTION);
				}
				executeAction(getMediaControls().getApplicationActionMap(), DISPOSE_VIDEO_COMPONENTS_ACTION);
				awaitShutdown(event);
			}
		}
	}

	/**
	 * Start a new {@link Thread} and wait until the {@link TaskService} is completely 
	 * terminated before exiting the application.
	 */
	private void awaitShutdown(final EventObject event) {
		new Thread(new Runnable() {

			public void run() {
				LOGGER.debug("Taskservice shutting down...");
				getContext().getTaskService().shutdown();
				while(!getContext().getTaskService().getTasks().isEmpty()) {
					try {
						getContext().getTaskService().awaitTermination(10, TimeUnit.SECONDS);
						LOGGER.debug("Waiting for tasks on EDT to end...");
						new Robot().waitForIdle();
					} catch (AWTException e) {
						LOGGER.error("Awt exception occurred", e);
					} catch (InterruptedException e) {
						LOGGER.error("Task interrupted", e);
					}
				}
				LOGGER.debug("DaoService shutting down...");
				getDaoService().shutdown();
				exit(event);
			}

		}, "AwaitShutdownThread").start();
	}

	public String getTitle() {
		return getResourceString(APP_TITLE);
	}

	public String getName() {
		return getResourceString(APP_NAME);
	}

	public String getVersionString() {
		return getResourceString(APP_NAME) + "-" + getResourceString(APP_VERSION) + "-" + getResourceString(APP_BUILD_DATE);
	}

	private String getResourceString(String resourceName) {
		return getContext().getResourceMap().getString(resourceName);
	}

	/**
	 * Add the given {@link AbstractTablePanel} to the list of panels so it's dirty state can be tracked.
	 * Setting a panel's dirty state to <code>true</code> will enable the save action throughout the application.
	 * @param panel The panel to add to the list
	 */
	private void addTablePanel(AbstractPanel panel) {
		panel.addPropertyChangeListener(dirtyListener);
		panels.add(panel);
	}

	/*
	 * Global application actions
	 */

	@org.jdesktop.application.Action
	public void saveSettings() {
		settingsManager.saveSettings();
	}

	public boolean isSaveNeeded() {
		return saveNeeded;
	}

	public void setSaveNeeded(boolean saveNeeded) {
		this.firePropertyChange(SAVE_NEEDED, this.saveNeeded, this.saveNeeded = saveNeeded);
	}

	@org.jdesktop.application.Action(enabledProperty=SAVE_NEEDED, block = Task.BlockingScope.WINDOW)
	public Task<Boolean, Void> save() {

		return new AbstractTask<Boolean, Void>(SAVE_ACTION) {

			protected Boolean doInBackground() throws Exception {
				boolean result = true;
				message("startMessage");
				for(AbstractPanel panel : panels) {
					if (panel.isDirty()) {
						result &= panel.save();
						panel.setDirty(false);
					}
				}
				message("endMessage");
				return result;
			}

			@Override
			protected void succeeded(Boolean result) {
				setSaveNeeded(!result);
			}

			@Override
			protected void failed(Throwable throwable) {
				String entityName = throwable.toString().replaceAll("[\\s\\S]*\\[(.+)\\][\\s\\S]*", "$1");
				super.failed(throwable, entityName);
			}

		};

	}

	@org.jdesktop.application.Action(block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> refresh() {
		return new RefreshTask(getDaoService(), getCustomerTablePanel(), getAnalysisTablePanel(), getVideoFileManager());
	}

	@org.jdesktop.application.Action
	public Task<Void, Void> uploadLogFiles() {
		return new UploadLogFilesTask(settingsManager.getLogFile(), settingsManager.getLogFileUploadUrl(), getName());
	}

	private Containable createMainView() {
		final JPanel mainPanel = new JPanel();
		ResourceMap resourceMap = getContext().getResourceMap();
		mainPanel.setName(resourceMap.getString("mainView.title"));
		// create the tabpanel
		JTabbedPane tabPanel = new  JTabbedPane();
		tabPanel.setName("detailTabbedPane");
		int minimumWidth = 0;
		for(AbstractPanel panel : panels) {
			String tabTitle = panel.getResourceMap().getString("tabConstraints.tabTitle");
			if (tabTitle != null) {
				tabPanel.addTab(tabTitle, panel);
			}
			int minimumPanelWidth = panel.getPreferredSize().width;
			minimumWidth = minimumWidth < minimumPanelWidth ? minimumPanelWidth : minimumWidth;
		}

		// set layout and add everything to the frame
		mainPanel.setLayout(new MigLayout("fill, nogrid, flowy, insets 10"));
		mainPanel.add(getCustomerTablePanel(), "growx");
		mainPanel.add(tabPanel, "height :280:, growx");
		mainPanel.add(getStatusPanel(), "height 30!, gapleft push");
		//mainPanel.setMinimumSize(new Dimension(minimumWidth, MAIN_PANEL_MIN_HEIGHT));
		return new Containable() {

			public Component getComponent() {
				return mainPanel;
			}

			public String getTitle() {
				return getResourceMap().getString("mainView.title");
			}

			public boolean isResizable() {
				return true;
			}

		};
	}

	/*
	 * Getters and Setters for the main objects in this application
	 */

	public ApplicationActions getApplicationActions() {
		return applicationActions;
	}

	public Containable getCustomerMainView() {
		return customerMainView;
	}

	public VideoMenuBar getMenuBar() {
		return menuBar;
	}

	public JScrollableDesktopPane getScrollableDesktopPane() {
		return scrollableDesktopPane;
	}

	public CustomerTablePanel getCustomerTablePanel() {
		return customerTablePanel;
	}

	public StatusPanel getStatusPanel() {
		return statusPanel;
	}

	public AnalysisTablePanel getAnalysisTablePanel() {
		return analysisTablePanel;
	}

	public RecordingTablePanel getRecordingTablePanel() {
		return recordingTablePanel;
	}

	public CustomerInfoPanel getCustomerInfoPanel() {
		return customerInfoPanel;
	}

	public MediaControls getMediaControls() {
		return mediaControls;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	private DaoService getDaoService() {
		return daoService;
	}

	private SettingsManager getSettingsManager() {
		return settingsManager;
	}

	/*
	 * Convenience methods
	 */

	/**
	 * This method will look for an {@link Action} specified with the given key in the given {@link ActionMap} 
	 * and invoke its {@link Action#actionPerformed(ActionEvent)} method.
	 * 
	 * @param actionMap The {@link ActionMap} containing the {@link Action} to be executed
	 * @param actionKey The key of the {@link Action} to be executed
	 */
	public void executeAction(ActionMap actionMap, String actionKey) {
		Action action = actionMap.get(actionKey);
		if (action != null) {
			ActionEvent actionEvent = new ActionEvent(getMainFrame(), ActionEvent.ACTION_PERFORMED, actionKey);
			action.actionPerformed(actionEvent);
		}
	}

	private UndoableEditListener createUndoableEditListener() {
		return getApplicationActions().getUndoableEditListener();
	}

	//getters for action maps in this application
	public ActionMap getActionMap(Object obj) {
		return getContext().getActionMap(obj);
	}

	public ActionMap getApplicationActionMap() {
		return getActionMap(getApplicationActions());
	}

}
