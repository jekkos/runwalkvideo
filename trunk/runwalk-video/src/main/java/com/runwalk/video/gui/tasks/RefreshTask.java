package com.runwalk.video.gui.tasks;

import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.jdesktop.application.Task;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CompositeList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.AnalysisConnector;
import com.runwalk.video.gui.panels.AnalysisOverviewTablePanel;
import com.runwalk.video.gui.panels.AnalysisTablePanel;
import com.runwalk.video.gui.panels.ClientTablePanel;

/**
 * This {@link Task} handles all database lookups and injects the results in the appropriate application component.
 * 
 * @author Jeroen Peelaerts
 *
 */
public class RefreshTask extends AbstractTask<Boolean, Void> {

	private final DaoService daoService;
	private final VideoFileManager videoFileManager;

	public RefreshTask(VideoFileManager videoFileManager, DaoService daoService) {
		super("refresh");
		this.videoFileManager = videoFileManager;
		this.daoService = daoService;
	}

	protected Boolean doInBackground() {
		boolean success = true;
		try {
			message("startMessage");
			// get all clients from the db
			List<Client> allClients = getDaoService().getDao(Client.class).getAll();
			final EventList<Client> clientList = GlazedLists.threadSafeList(GlazedLists.eventList(allClients));
			// get all analyses from the db (not using derived list to enable multiple threads to work simultaneously)
			List<Analysis> analysisList = getDaoService().getDao(Analysis.class).getAll();
			// get all cities from the db
			List<City> allCities = getDaoService().getDao(City.class).getAll();
			final EventList<City> cityList = GlazedLists.eventList(allCities);
			// get all articles from the db
			List<Article> allArticles = getDaoService().getDao(Article.class).getAll();
			final EventList<Article> articleList = GlazedLists.eventList(allArticles);
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					RunwalkVideoApp.getApplication().getClientInfoPanel().setItemList(cityList);
					// get client table panel and inject data
					ClientTablePanel clientTablePanel = RunwalkVideoApp.getApplication().getClientTablePanel();
					clientTablePanel.setItemList(clientList, Client.class);
					final EventList<Client> selectedClients = clientTablePanel.getEventSelectionModel().getSelected();
					CollectionList<Client, Analysis> selectedClientAnalyses = new CollectionList<Client, Analysis>(selectedClients, new CollectionList.Model<Client, Analysis>() {

						public List<Analysis> getChildren(Client parent) {
							return parent.getAnalyses();
						}

					});
					// get analysis tablepanel and inject data
					AnalysisTablePanel analysisTablePanel = RunwalkVideoApp.getApplication().getAnalysisTablePanel();
					analysisTablePanel.setArticleList(articleList);
					analysisTablePanel.setItemList(selectedClientAnalyses, new AnalysisConnector());
					final EventList<Client> deselectedClients = clientTablePanel.getEventSelectionModel().getDeselected();
					final CollectionList<Client, Analysis> deselectedClientAnalyses = new CollectionList<Client, Analysis>(deselectedClients, new CollectionList.Model<Client, Analysis>() {

						public List<Analysis> getChildren(Client parent) {
							return parent.getAnalyses();
						}

					});
					// get analysis overview tablepanel and inject data
					final CompositeList<Analysis> analysesOverview = new CompositeList<Analysis>(selectedClientAnalyses.getPublisher(), selectedClientAnalyses.getReadWriteLock());
					analysesOverview.addMemberList(selectedClientAnalyses);
					analysesOverview.addMemberList(deselectedClientAnalyses);
					// create the overview with unfinished analyses
					AnalysisOverviewTablePanel analysisOverviewTablePanel = RunwalkVideoApp.getApplication().getAnalysisOverviewTablePanel();
					analysisOverviewTablePanel.setItemList(analysesOverview, new AnalysisConnector());
				}

			});
			refreshVideoFiles(analysisList);
		} catch(Exception ignore) {
			getLogger().error(Level.SEVERE, ignore);
			success = false;
		}
		return success;
	}
	
	private void refreshVideoFiles(List<Analysis> analysisList) {
		// some not so beautiful way to refresh the cache
		message("loadingVideoFilesMessage");
		int filesMissing = 0;
		for (Analysis analysis : analysisList) {
			filesMissing = filesMissing + getVideoFileManager().refreshCache(analysis);
			setProgress(analysisList.indexOf(analysis) + 1, 0, analysisList.size());
		}
		// check whether compressing should be enabled
		RunwalkVideoApp.getApplication().getAnalysisOverviewTablePanel().setCompressionEnabled(true);
		message("endMessage", filesMissing);
	}

	private VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	private DaoService getDaoService() {
		return daoService;
	}

}