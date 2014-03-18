package de.imc.mirror.arapp.client.service;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ARAppServiceAsync {
	void getTime(AsyncCallback<Long> callback);
	void discussionStartingTime(String discussion, AsyncCallback<Long> callback);
	void endDiscussion(String discussion, AsyncCallback<Void> callback);
	void saveFilesOnSpaces(String discussionId, String auth, List<String> spaceIds, AsyncCallback<Void> callback);
	void fileAlreadyExists(String discussionId, String filename, AsyncCallback<Boolean> callback);
	void deleteFile(String discussionId, String filename, AsyncCallback<Void> callback);
	void deleteFiles(String discussionId, AsyncCallback<Void> callback);
	void startingTimes(List<String> discussions, AsyncCallback<Map<String, Long>> callback);
	void deleteFilesOnFileService(Map<String, String> list, String auth, AsyncCallback<Void> callback);
}
