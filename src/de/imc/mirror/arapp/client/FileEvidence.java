package de.imc.mirror.arapp.client;

import java.util.Date;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;

public class FileEvidence extends Evidence {
	
	private String fileName;
	private String location;
	private String text;
	private String prefix;


	public FileEvidence(String id, String type, String timestamp, String text, String fileName) {
		super();
		this.id = id;
		this.type = type;
		this.timestamp = timestamp;
		this.fileName = fileName;
		this.text = text;
	}
	
	/**
	 * @return the filename of the attached file.
	 */
	public String getFileName() {
		if (prefix != null && fileName.startsWith(prefix)) {
			return fileName.replace(prefix + "_", "");
		}
		return fileName;
	}
	
	/**
	 * When saved the filename consists of the original filename and a prefix.
	 * @return the original filename and the set prefix.
	 */
	public String getFileNameWithPrefix() {
		if (prefix != null && !fileName.startsWith(prefix)) {
			return prefix + "_" + fileName;
		}
		return fileName;
	}
	
	public String getType() {
		String[] array = fileName.split("\\.");
		String fileType = array[array.length - 1].toUpperCase();
		return "File: " + fileType;
	}
	
	/**
	 * Sets the prefix to be used.
	 * @param prefix the prefix to be set.
	 */
	public void setFileNamePrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * @return the space the file is saved on.
	 */
	public String getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		String xml = createEvidenceXML(text, id, timestamp, fileName, location);
		return xml;
	}
	
	private String createEvidenceXML(String text, String id, String timestamp, String fileName, String fileLocation) {
		String namespace = "mirror:application:activityrecommendationapp:textevidence";
		Document doc = XMLParser.createDocument();
		Element root = Parser.createElement("textevidence", namespace);
		root.setAttribute("id", id);
		if (super.getPublisher() != null) {
			root.setAttribute("publisher", super.getPublisher());
		}
		root.setAttribute("timestamp", timestamp);
		root.setAttribute("modelVersion", "1.0");
		root.setAttribute("cdmVersion", "2.0");
		
		Element creationInfo = Parser.createElement("creationInfo", namespace);
		Element personElement = Parser.createElement("person", namespace);
		personElement.appendChild(doc.createTextNode(super.getPublisher()));
		
		Element dateElement = Parser.createElement("date", namespace);
		dateElement.appendChild(doc.createTextNode(HasTimestamp.TIMESTAMPFORMAT.format(new Date())));

		creationInfo.appendChild(dateElement);
		creationInfo.appendChild(personElement);
		
		Element content = Parser.createElement("content", namespace);
		content.appendChild(doc.createCDATASection(text));
		
		String[] split = text.split("( |\n)");
		Element urls = null;
		for (int i=0; i<split.length; ++i) {
			if (split[i].toLowerCase().startsWith("http://") || split[i].toLowerCase().startsWith("https://")) {
				
				Element url = Parser.createElement("url", namespace);
				url.appendChild(doc.createCDATASection(split[i]));
				
				if (urls == null) {
					urls = Parser.createElement("urls", namespace);
				}
				urls.appendChild(url);
			}
		}
		
		root.appendChild(creationInfo);
		root.appendChild(content);
		if (urls != null) {
			root.appendChild(urls);
		}

		if (fileName != null && fileLocation != null) {
			Element attachment = Parser.createElement("attachment", "mirror:common:datatypes");
			Element link = Parser.createElement("link", "mirror:common:datatypes");
			StringBuilder url = new StringBuilder();
			url.append(Window.Location.getProtocol()).append("//").append(Window.Location.getHost()).append("/fileservice/").append(URL.encodeQueryString(fileLocation)).append("/");
			if (prefix != null && !fileName.startsWith(prefix)) {
				StringBuilder filenameBuilder = new StringBuilder(prefix);
				filenameBuilder.append("_").append(fileName);
				url.append(URL.encodeQueryString(filenameBuilder.toString()));
				link.setAttribute("id", filenameBuilder.toString());
//				attachment.setAttribute("filename", prefix + "_" + fileName);				
			} else {
				url.append(fileName);
				link.setAttribute("id", fileName);
//				attachment.setAttribute("filename", fileName);
			}
//			attachment.setAttribute("location", fileLocation);
			link.setAttribute("url", url.toString());
			attachment.appendChild(link);
			root.appendChild(attachment);
		}

		return root.toString();
	}
}
