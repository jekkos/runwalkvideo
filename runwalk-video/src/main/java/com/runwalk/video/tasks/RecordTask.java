package com.runwalk.video.tasks;

import java.io.File;
import java.util.Date;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.VideoCapturer;
import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.util.AppUtil;

public class RecordTask extends AbstractTask<Boolean, Void> {

	private final DaoService daoService;
	private final VideoFileManager videoFileManager;
	private final Iterable<VideoCapturer> capturers;
	private final AnalysisModel analysisModel;

	private volatile boolean recording = false;

	public RecordTask(VideoFileManager videoFileManager, DaoService daoService, 
			Iterable<VideoCapturer> capturers, AnalysisModel analysisModel) {
		super("record");
		this.daoService = daoService;
		this.videoFileManager = videoFileManager;
		this.capturers = capturers;
		this.analysisModel = analysisModel;
	}

	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		startRecording();
		synchronized(this) {
			while (isRecording()) {
				publish();
				wait(250);
			}
		}
		return stopRecording();
	}

	public String buildFileName(Analysis analysis) {
		String date = AppUtil.formatDate(analysis.getCreationDate(), AppUtil.FILENAME_DATE_FORMATTER);
		String prefix = analysis.getRecordings().size() == 0 ? "" : analysis.getRecordings().size() + "_";
		Customer customer = analysis.getCustomer();
		return new StringBuilder(prefix).append(customer.getName()).append("_")
				.append(customer.getFirstname()).append("_").append(date)
				.append(Recording.VIDEO_CONTAINER_FORMAT)
				.toString().replaceAll(" ", "_");
	}
	
	/**
	 * Start recording.
	 */
	private void startRecording() {
		for (VideoCapturer capturer : getCapturers()) {
			Analysis analysis = getAnalysisModel().getEntity();
			Recording recording = new Recording(analysis, buildFileName(analysis));
			// persist recording first, then add it to the analysis
			getDaoService().getDao(Recording.class).persist(recording);
			getAnalysisModel().addRecording(recording);
			getVideoFileManager().addToCache(recording, RecordingStatus.RECORDING);
			File videoFile = getVideoFileManager().getUncompressedVideoFile(recording);
			if (!"none".equals(capturer.getVideoImpl().getCaptureEncoderName())) {
				videoFile = getVideoFileManager().getCompressedVideoFile(recording);
			}
			File parentDir = videoFile.getParentFile();
			if (!parentDir.exists()) {
				boolean mkdirs = parentDir.mkdirs();
				getLogger().debug("Directory creation result for " + parentDir.getAbsolutePath() + " is " + mkdirs);
			}
			capturer.startRecording(videoFile.getAbsolutePath());
			// set recording to true if recording / file key value pair added to file manager
			setRecording(true);
		}
		message("recordingMessage", getAnalysisModel().getEntity().getCustomer().toString());
	}

	/**
	 * Stop recording.
	 * 
	 * @return return <code>true</code> if recording succeeded
	 */
	private Boolean stopRecording() {
		boolean result = true;
		for (VideoCapturer capturer : getCapturers()) {
			capturer.stopRecording();
			for (Recording recording : getAnalysisModel().getEntity().getRecordings()) {
				if ("none".equals(capturer.getCaptureEncoderName())) {
					getVideoFileManager().updateCache(recording, RecordingStatus.UNCOMPRESSED);
				} else {
					getVideoFileManager().updateCache(recording, RecordingStatus.COMPRESSED);
				}
				if (!getVideoFileManager().canReadAndExists(recording)) {
					errorMessage("errorMessage", recording);
				}
			}
		}
		message("endMessage", getAnalysisModel().getEntity().getCustomer());
		return result;
	}

	public boolean isRecording() {
		return recording;
	}
	
	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public Iterable<VideoCapturer> getCapturers() {
		return capturers;
	}

	public AnalysisModel getAnalysisModel() {
		return analysisModel;
	}

}
