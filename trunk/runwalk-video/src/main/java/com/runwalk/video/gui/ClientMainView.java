package com.runwalk.video.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public class ClientMainView extends MyInternalFrame {
	
	private JTabbedPane tabPanel;

	public ClientMainView() {
		//TODO haal naam van de frame uit resourceMap
		super("Klanten en analyses", true);
		setName(getResourceMap().getString("mainView.title"));
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new AbsoluteLayout());
		contentPanel.add(getApplication().getClientTablePanel().getComponent(), new AbsoluteConstraints(10, 10, 580, 370));
		
		tabPanel = new  JTabbedPane();
		tabPanel.setName("detailTabbedPane");
		tabPanel.addTab(getResourceMap().getString("infoPanel.TabConstraints.tabTitle"),  getApplication().getClientInfoPanel().getComponent()); // NOI18N
		tabPanel.addTab(getResourceMap().getString("analysisPanel.TabConstraints.tabTitle"),  getApplication().getAnalysisTablePanel().getComponent()); // NOI18N
		tabPanel.addTab(getResourceMap().getString("conversionPanel.TabConstraints.tabTitle"),  getApplication().getAnalysisOverviewTable().getComponent()); // NOI18N
		contentPanel.add(tabPanel, new AbsoluteConstraints(0, 380, 590, 280));
		
		setLayout(new BorderLayout());
		getComponent().add(contentPanel, BorderLayout.CENTER);
		getComponent().add(getApplication().getStatusPanel().getComponent(), BorderLayout.SOUTH);
	}


}
