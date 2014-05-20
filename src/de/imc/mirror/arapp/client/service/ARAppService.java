package de.imc.mirror.arapp.client.service;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("server")
public interface ARAppService extends RemoteService {
	Long getTime();
	Long discussionStartingTime(String discussion);
	void endDiscussion(String discussion);
	void saveFilesOnSpaces(String discussionId, String auth, List<String> spaceIds);
	Boolean fileAlreadyExists(String discussionId, String filename);
	void deleteFile(String discussionId, String filename);
	void deleteFiles(String discussionId);
	void deleteFilesOnFileService(Map<String, String> list, String auth);
	Map<String, Long> startingTimes(List<String> discussions);
	boolean isFileServiceAvailable();
}
