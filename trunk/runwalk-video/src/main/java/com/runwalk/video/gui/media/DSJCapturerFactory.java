package com.runwalk.video.gui.media;

import java.util.List;

import com.google.common.collect.Lists;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;

public class DSJCapturerFactory extends VideoCapturerFactory {

	private DSFiltergraph getFiltergraphByFilter(String filterInfo) {
		if (filterInfo != null && !filterInfo.equals("none")) {
			DSFilterInfo oldDevice = DSFilterInfo.filterInfoForName(filterInfo);
			DSFiltergraph[] activeGraphs = DSEnvironment.getActiveGraphs();
			for (int i = 0; activeGraphs != null && i < activeGraphs.length; i++) {
				DSFiltergraph graph = activeGraphs[i];
				DSFilter[] filters = graph.listFilters();
				for (DSFilter filter : filters) {
					String filterInfoName = filter.getFilterInfo().getName();
					if (filterInfoName.equals(oldDevice.getPath())) {
						return graph;
					}
				}
			}
		}
		return null;
	}
	
	public void disposeCapturer(String capturerName) {
		// dispose filtergraph if there was already one started by the camera dialog
		DSFiltergraph activeFiltergraph = getFiltergraphByFilter(capturerName);
		if (activeFiltergraph != null) {
			activeFiltergraph.dispose();
		}
	}

	public IVideoCapturer initializeCapturer(String capturerName) {
		DSFilterInfo selectedDevice = DSFilterInfo.filterInfoForName(capturerName);
		// initialize the capturer's native resources for the chosen device so settings
		DSJCapturer dsjCapturer = new DSJCapturer(selectedDevice);
		return dsjCapturer;
	}

	public String[] getCaptureDevices() {
		List<String> result = Lists.newArrayList();
		// query first with bit set to 0 to get quick listing of available capture devices
		DSFilterInfo[][] dsi = DSCapture.queryDevices(0 | DSCapture.SKIP_AUDIO);
		for (int i = 0; i < dsi[0].length; i++) {
			DSFilterInfo dsFilterInfo = dsi[0][i];
			String filterName = dsFilterInfo.getName();
			// remove filters that are already added to a filtergraph
			if (getFiltergraphByFilter(filterName) == null) {
				result.add(filterName);
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
}
