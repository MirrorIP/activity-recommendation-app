package de.imc.mirror.arapp.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.user.server.Base64Utils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.imc.mirror.arapp.client.service.ARAppService;


public class ARAppServiceImpl extends RemoteServiceServlet implements
		ARAppService {
	
	private static boolean fileserviceAvailable = false;

	private static String fileServiceURL;

	private static final long serialVersionUID = 1L;
	
	private Map<String, Long> startingTimes = new HashMap<String, Long>();
	private Map<String, Set<EvidenceFile>> evidenceFiles = new HashMap<String, Set<EvidenceFile>>();
	

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		String fileserviceLocation = servletConfig.getInitParameter("fileServiceHost");		
		if (fileserviceLocation == null || fileserviceLocation.length() == 0) {
			return;
		} else if (fileserviceLocation.endsWith("/")) {
			fileserviceLocation.substring(0, fileserviceLocation.length() - 1);
		}
		String fileServicePath = servletConfig.getInitParameter("fileServicePath");
		if (fileServicePath == null || fileServicePath.length() == 0) {
			return;
		}
		int fileServiceMethod = Integer.parseInt(servletConfig.getInitParameter("useHTTPS"));
		String method;
		if (fileServiceMethod == 1) {
			method = "https://";
		} else {
			method = "http://";
		}
		
		int fileservicePort = Integer.parseInt(servletConfig.getInitParameter("fileservicePort"));
		StringBuilder builder = new StringBuilder();
		builder.append(method).append(fileserviceLocation);
		if (fileservicePort != -1 && fileservicePort != 80) {
			builder.append(":").append(fileservicePort);
		}
		builder.append("/").append(fileServicePath);
		if (!fileServicePath.endsWith("/")) {
			builder.append("/");
		}
		fileServiceURL = builder.toString();
		fileserviceAvailable = true;
	}
	
	public boolean isFileServiceAvailable() {
		return fileserviceAvailable;
	}
	
	public Long getTime() {
		return new Date().getTime();
	}
	
	public Long discussionStartingTime(String discussion) {
		long time;
		if (this.startingTimes.get(discussion) != null) {
			time = this.startingTimes.get(discussion);
		}
		else {
			time = new Date().getTime();
			this.startingTimes.put(discussion, time);
		}
		return time;
	}
	
	public Map<String, Long> startingTimes(List<String> discussions) {
		Map<String, Long> result = new HashMap<String, Long>();
		for (String discussion:discussions) {
			result.put(discussion, discussionStartingTime(discussion));
		}
		return result;
	}
	
	public void endDiscussion(String discussion) {
		this.startingTimes.remove(discussion);
		this.evidenceFiles.remove(discussion);
	}
	
	public void saveFilesOnSpaces(String discussionId, String auth, List<String> spaceIds) {
		if (!fileserviceAvailable) {
			return;
		}
		Set<EvidenceFile> files = evidenceFiles.get(discussionId);
		for (String spaceId:spaceIds) {
			for (EvidenceFile file:files) {
				saveFile(file, auth, spaceId);
			}
		}
	}
	
	public Boolean fileAlreadyExists(String discussionId, String filename) {		
		Set<EvidenceFile> files = evidenceFiles.get(discussionId);		
		if (files != null) {			
			for (EvidenceFile file:files) {				
				if (file.getFilename().equals(filename)) {					
					return true;
				}
			}
		}		
		return false;
	}
	
	@Override
    protected void service(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!fileserviceAvailable) {
			return;
		}
		if (request.getMethod().equals("POST")) {			
			if (request.getContentType().contains("multipart/form-data")) {				
				String boundary = request.getContentType().split("boundary=")[1];				
				InputStream in = request.getInputStream();				
				int length;				
				byte[] cbuf = new byte[1024];
				List<Byte> byteArray = new ArrayList<Byte>();				
				while ((length = in.read(cbuf, 0, 1024)) != -1) {
					for (int i=0; i<length; i++) {
						byteArray.add(cbuf[i]);
					}
				}				
				if (!getInfos(boundary, byteArray)) {					
					response.sendError(404);
//					response.getOutputStream().write("400".getBytes());
					response.flushBuffer();
				} else {					
					response.getOutputStream().write("200".getBytes());
					response.flushBuffer();
				}
				return;
			}
		} else if (request.getMethod().equals("GET")) {
			getFile(request.getParameter("auth"), request.getParameter("nameOnServer"), request.getParameter("filename"), request.getParameter("location"), Boolean.parseBoolean(request.getParameter("download")), response);
			return;
		}
		super.service(request, response);
    }
	
	private void saveFileLocal(List<Byte> content, String filename, String discussion) {		
		if (!evidenceFiles.containsKey(discussion) || evidenceFiles.get(discussion) == null) {
			Set<EvidenceFile> files = new HashSet<EvidenceFile>();
			evidenceFiles.put(discussion, files);
		}		
		Set<EvidenceFile> files = evidenceFiles.get(discussion);
		EvidenceFile evFile = new EvidenceFile(content, filename);
		files.add(evFile);
		evidenceFiles.put(discussion, files);
		
	} 
	
	private void getFile(String auth, String nameOnServer, String filename, String location, boolean download, HttpServletResponse response) {
		try {	
			byte[] bytes = null;
			FileTypeMap fileType = MimetypesFileTypeMap.getDefaultFileTypeMap();
			if (evidenceFiles.containsKey(location) && evidenceFiles.get(location) != null) {
				Set<EvidenceFile> files = evidenceFiles.get(location);
				for (EvidenceFile file:files) {
					if (file.getFilename().equals(nameOnServer)) {
						bytes = new byte[file.getContent().size()];
						for (int i=0; i<file.getContent().size(); i++) {
							bytes[i] = file.getContent().get(i);
						}
					}
				}
			}
			if (bytes == null) {
				location = URLEncoder.encode(location, "UTF-8");
				String filenameEnc = URLEncoder.encode(nameOnServer, "UTF-8");	
				String urlString = fileServiceURL + location + "/" + filenameEnc;
				URL url = new URL(urlString);				
				HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
				httpCon.setDoInput(true);
				httpCon.setDoOutput(true);
				httpCon.setRequestMethod("GET");
				httpCon.setRequestProperty("Content-type", fileType.getContentType(filename));
				httpCon.setRequestProperty("Authorization", "Basic " + auth);				
				httpCon.connect();
				if (httpCon.getResponseCode() != 200) {
					httpCon.disconnect();
					response.sendError(404);
					response.flushBuffer();
					return;
				}
				httpCon.getResponseMessage();
				
				InputStream in = httpCon.getInputStream();
				byte[] b = new byte[512];
				List<Byte> bytesList = new ArrayList<Byte>();
				int read = -1;
				while ((read = in.read(b, 0, 512)) != -1) {
					for (int i=0; i<read; i++) {
						bytesList.add(b[i]);
					}
				}
				bytes = new byte[bytesList.size()];
				for (int i=0; i<bytesList.size(); i++) {
					bytes[i] = bytesList.get(i);
				}
				in.close();
			}
			
			if (!download) {
				String base = Base64Utils.toBase64(bytes);
				bytes = base.getBytes();
			}
			response.setContentType(fileType.getContentType(filename));
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			OutputStream out = response.getOutputStream();
			out.write(bytes);
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void deleteFile(String discussionId, String filename) {
		Set<EvidenceFile> files = evidenceFiles.get(discussionId);
		EvidenceFile fileToRemove = null;
		if (files != null) {
			for (EvidenceFile file:files) {
				if (file.getFilename().equals(filename)) {
					fileToRemove = file;
					break;
				}
			}
		}
		if (fileToRemove != null) {
			files.remove(fileToRemove);
			evidenceFiles.put(discussionId, files);
		}
	}
	
	public void deleteFiles(String discussionId) {
		evidenceFiles.remove(discussionId);
	}
	
	public void deleteFilesOnFileService(Map<String, String> list, String auth) {
		if (!fileserviceAvailable) {
			return;
		}
		for (String filename:list.keySet()) {
			deleteFileOnFileService(list.get(filename), auth, filename);
		}
	}
	
	private void deleteFileOnFileService(String space, String auth, String filename) {
		try {		
			space = URLEncoder.encode(space, "UTF-8");
			filename = URLEncoder.encode(filename, "UTF-8");	
			String urlString = fileServiceURL + space + "/" + filename;
			URL url = new URL(urlString);			
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoInput(true);
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("DELETE");
			httpCon.setRequestProperty("Content-type", "multipart/form-data");
			httpCon.setRequestProperty("Authorization", "Basic " + auth);
			httpCon.connect();
			httpCon.getResponseCode();
			httpCon.getResponseMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveFile(EvidenceFile file, String auth, String space) {
		try {		
			space = URLEncoder.encode(space, "UTF-8");
			String filename = URLEncoder.encode(file.getFilename(), "UTF-8");	
			String urlString = fileServiceURL + space + "/" + filename;
			URL url = new URL(urlString);
			
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoInput(true);
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			httpCon.setRequestProperty("Content-type", "multipart/form-data");
			httpCon.setRequestProperty("Authorization", "Basic " + auth);

			OutputStream out = httpCon.getOutputStream();
			for (Byte bit:file.getContent()) {
				out.write(bit);
			}
			out.close();
			httpCon.connect();
			httpCon.getResponseCode();
			httpCon.getResponseMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean getInfos(String boundaryString, List<Byte> array) {		
		byte[] seperator = "\r\n\r\n".getBytes();
		byte[] endseperator = ("\r\n--" + boundaryString).getBytes();
		byte[] boundary = boundaryString.getBytes();
		
		List<Byte> filecontent = null;
		String filename = null;
		String discussion = null;
		
		for (int i=0; i<array.size(); i++) {
			
			int index = -1;
			boolean contained = false;
			Byte arrayBit = array.get(i);
			if (arrayBit.equals(boundary[0])) {
				
				for (int j=0; j<boundary.length; j++) {
					if (contained) break;
					contained = true;
					if (array.size()<=i+j) break;
					if (array.get(i+j).equals(boundary[j])) {
						contained = false;
						index = i + j + 1;
					}
				}
				if (contained) continue;
				List<Byte> subarray = array.subList(index, array.size());
				for (int j=0; j<subarray.size(); j++) {
					contained = false;
					Byte subArrayBit = subarray.get(j); 

					if (subArrayBit.equals(seperator[0])) {				
						
						for (int k=1; k<seperator.length; k++) {
							if (contained) break; 
							contained = true;
							if (subarray.size()<=j+k) break;
							if (subarray.get(j+k).equals(seperator[k])) {
								contained = false;
								index = j + k + 1;
							}
						}
						
						if (contained) continue;
						
						List<Byte> header = subarray.subList(0, index);
						List<Byte> content = subarray.subList(index, subarray.size());

						StringBuilder builder = new StringBuilder();
						for (Byte bit:header) {
							byte[] bi = new byte[1];
							bi[0] = bit;
							builder.append(new String(bi));
						}
						String headerString = builder.toString();
						
						int lineIndex = headerString.contains("Content-Disposition") ? headerString.indexOf("Content-Disposition"):headerString.indexOf("content-disposition");
						if (lineIndex == -1) continue;
						
						String line = headerString.substring(lineIndex);
						line = line.substring(0, line.indexOf("\n"));
						if (line.contains("filename=\"")) {
							if (line.indexOf("filename=\"") == line.indexOf("name=\"") - 4) {
								line = line.substring(line.indexOf("name=\"") + 6);
							}
						}
						String name = line.substring(line.indexOf("name=\"") + 6);
						name = name.substring(0, name.indexOf("\""));
						
						StringBuilder fileNameBuilder = new StringBuilder();
						StringBuilder discussionBuilder = new StringBuilder();
						for (int k=0; k<content.size(); k++) {
							Byte contentBit = content.get(k);
							contained = false;
							if (contentBit.equals(endseperator[0])) {
								for (int l=1; l<endseperator.length; l++) {
									if (contained) break;
									contained = true;
									if (content.size()<=k+l) break;
									if (content.get(k+l).equals(endseperator[l])) {
										contained = false;
									}
								}
								if (!contained) {
									if (name.equals("file")) {
										filecontent = content.subList(0, k);
									}
									
									if (filecontent != null && filename != null && discussion != null) {										
										saveFileLocal(filecontent, filename, discussion);										
										return true;
									}
									i= i + header.size() + k;
									break;
								}
							}
							
							if (name.equals("filename")) {
								byte[] bi = new byte[1];
								bi[0] = contentBit;
								fileNameBuilder.append(new String(bi));
								filename = fileNameBuilder.toString();								
							} else if (name.equals("discussion")) {
								byte[] bi = new byte[1];
								bi[0] = contentBit;
								discussionBuilder.append(new String(bi));
								discussion = discussionBuilder.toString();	
							}
							
						}
						break;
					}
				}
			}
		}
		return false;
	}
}
