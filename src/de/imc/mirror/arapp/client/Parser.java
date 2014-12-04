package de.imc.mirror.arapp.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.URL;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.RecommendationObject.TaskType;
import de.imc.mirror.arapp.client.RecommendationStatus.Status;

public class Parser {
	
	public static String decodeString(String text) {
		if (text == null) {
			return "";
		}
		return text.replaceAll("&apos;", "'")
				   .replaceAll("&quot;", "\"")
				   .replaceAll("&gt;", ">")
				   .replaceAll("&lt;", "<")
				   .replaceAll("&amp;", "&");
	}
	
	/**
	 * Creates a element with the given tagname and namespace.
	 * @param tagname The tagname of the new element.
	 * @param namespace The namespace of the new element.
	 * @return The newly created element.
	 */
	public static Element createElement(String tagname, String namespace) {
		StringBuilder builder = new StringBuilder();
		builder.append("<")
			   .append(tagname)
			   .append(" xmlns='")
			   .append(namespace)
			   .append("' />");
		Document doc = XMLParser.parse(builder.toString());
		return doc.getDocumentElement();
	}	
	
	/**
	 * Parses an xml-string to an element.
	 * @param docXML The xml-string.
	 * @return The new element.
	 */
	public static Element parseXMLStringToElement(String docXML) {
		try {
			Document doc = XMLParser.parse(docXML);
			return doc.getDocumentElement();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Checks if the given xml denotes a sessionobject.
	 * @param xml The xml to check.
	 * @return if the given xml denotes a sessionobject.
	 */
	public static boolean isSessionObject(String xml) {
		Element parsed = parseXMLStringToElement(xml);
		if (parsed != null && parsed.getTagName().equals("session")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the given xml contains a sessionobject.
	 * @param xml The xml to check.
	 * @return if the given xml contains a sessionobject.
	 */
	public static boolean hasSessionObject(String xml) {
		Element elem = parseXMLStringToElement(xml);
		if (elem != null) {
			NodeList nodes = elem.getElementsByTagName("session");
			return nodes.getLength() > 0;
		}
		return false;
	}
	
	/**
	 * Checks if the given xml denotes a recommendationobject.
	 * @param xml The xml to check.
	 * @return if the given xml denotes a recommendationobject.
	 */
	public static boolean isRecommendationObject(String xml) {
		Element elem = parseXMLStringToElement(xml);
		if (elem != null && elem.getTagName().equals("recommendation")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the given xml denotes a experienceobject.
	 * @param xml The xml to check.
	 * @return if the given xml denotes a experienceobject.
	 */
	public static boolean isExperienceObject(String xml) {
		Element parsed = parseXMLStringToElement(xml);
		if (parsed != null && parsed.getTagName().equals("experience")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the given xml denotes a Recommendationstatusobject.
	 * @param xml The xml to check.
	 * @return if the given xml denotes a Recommendationstatusobject.
	 */
	public static boolean isRecommendationStatusObject(String xml) {
		Element parsed = parseXMLStringToElement(xml);
		if (parsed != null && parsed.getTagName().equals("recommendationstatus")) {
			return true;
		}
		return false;
	}
	
	private static String getInnerText(Element elem, String tagName) {
    	NodeList list = elem.getElementsByTagName(tagName);
    	if (list == null || list.getLength() != 1) {
    		return null;
    	}
    	if (list.item(0) == null || list.item(0).getFirstChild() == null) {
    		return "";
    	} else {
    		return decodeString(list.item(0).getFirstChild().getNodeValue());
    	}
	}
	
	/**
	 * Parses and returns all Recommendations contained in the given element.
	 * @param elem The element to parse.
	 * @return a list of all contained recommendationobjects.
	 */
	public static List<RecommendationObject> parseRecommendations(final Element elem) {
		NodeList nodes = elem.getElementsByTagName("recommendation");
		if (nodes.getLength() == 0 && elem.getTagName().equals("recommendation")) {
			nodes = new NodeList() {
				
				@Override
				public Node item(int index) {
					if (index == 0) {
						return elem;
					}
					return null;
				}
				
				@Override
				public int getLength() {
					return 1;
				}
			};
		}
		List<RecommendationObject> result = new ArrayList<RecommendationObject>();
		for (int i=0; i<nodes.getLength(); i++) {
	    	Element element = (Element) nodes.item(i);
	    	if (!"mirror:application:activityrecommendationapp:recommendation".equals(element.getNamespaceURI())) {
	    		continue;
	    	}
	    	String title = getInnerText(element, "title");
	    	String issue = getInnerText(element, "issue");
	    	String solution = getInnerText(element, "solution");
	    	
	    	TaskType type = TaskType.getTaskType(element.getAttribute("tasktype"));
	    	if (title == null || issue == null || solution == null) {
	    		continue;
	    	}

	    	RecommendationObjectBuilder builder = new RecommendationObjectBuilder(title, issue, solution, type);
	    	
	    	String id = element.getAttribute("id");
	    	String customId = null;
	    	if (element.hasAttribute("customId")) {
	    		customId = element.getAttribute("customId");
	    	}
	    	if (element.hasAttribute("delete")) {
	    		String deleteAttribute = element.getAttribute("delete");
	    		if (deleteAttribute.equals("deleted")){
	    			builder.setDeleted();
	    		} else if (deleteAttribute.equals("todelete")) {
	    			builder.setToDelete();
	    		}
	    	}
	    	
	    	builder.setCreationDate(element.getAttribute("timestamp"))
	    		   .setId(id)
	    		   .setCustomId(customId)
	    		   .setState(Boolean.parseBoolean(element.getAttribute("active")));
	    	
	    	NodeList creatorNodes = element.getElementsByTagName("person");
	    	if (creatorNodes != null && creatorNodes.getLength() > 0 && creatorNodes.item(0).hasChildNodes()) {
	    		builder.setPublisher(creatorNodes.item(0).getFirstChild().getNodeValue());
	    	} else if (element.hasAttribute("publisher")) {
	    		builder.setPublisher(element.getAttribute("publisher"));	    		
	    	}
	    	
	    	if (element.hasAttribute("ref")) {
	    		String ref = element.getAttribute("ref");
	    		builder.setRef(ref);
	    	}
	    	
	    	NodeList entries = element.getElementsByTagName("entry");
	    	if (entries != null) {
		    	for (int j = 0; j<entries.getLength(); j++) {
					Element entryNode = (Element) entries.item(j);
					if (entryNode == null) continue;
					String timestamp = entryNode.getAttribute("timestamp");
					int entryId = -1;
					if (entryNode.hasAttribute("id")) {
						try {
							entryId = Integer.parseInt(entryNode.getAttribute("id"));
						} catch (Exception e) {
							entryId = -1;
						}
					}
			    	if (entryNode != null && entryNode.getFirstChild() != null) {
			    		builder.addEntry(new Entry(entryId, decodeString(entryNode.getFirstChild().getNodeValue()), timestamp));				    		
			    	}
					
		    	}
	    	}
	    	
	    	NodeList targetSpaces = element.getElementsByTagName("spaceId");
	    	if (targetSpaces != null) {
		    	for (int j = 0; j<targetSpaces.getLength(); j++) {
			    	if (targetSpaces.item(j) != null && targetSpaces.item(j).getFirstChild() != null) {
			    		builder.addTargetSpace(targetSpaces.item(j).getFirstChild().getNodeValue());
			    	}
		    	}
	    	}
	    	
	    	
	    	NodeList objects = element.getElementsByTagName("object");
	    	if (objects != null) {
	    		for (int j = 0; j<objects.getLength(); j++) {
	    			Element objectNode = (Element) objects.item(j);
	    			if (objectNode == null) continue;
	    			if (!objectNode.hasChildNodes()) {
	    				if (objectNode.hasAttribute("id")) {
			    			String objectId = objectNode.getAttribute("id");
			    			builder.addRelatedEvidenceId(objectId);
	    				}
	    			} else {
	    				NodeList childNodes = objectNode.getChildNodes();
		    			StringBuilder stringBuilder = new StringBuilder();
	    				if (childNodes.getLength() > 1) {
		    				for (int k=0; k<childNodes.getLength(); k++){
		    					try {
			    					CDATASection cdataObject = (CDATASection) childNodes.item(k);
			    					stringBuilder.append(decodeString(cdataObject.getData()));
		    					} catch (ClassCastException e) {
		    						stringBuilder.append(decodeString(childNodes.item(k).getNodeValue()));
		    					}
		    				}
	    				} else {
	    					stringBuilder.append(decodeString(childNodes.item(0).getNodeValue()));
	    				}
	    				try {
	    					Evidence ev = parseDataObjectToEvidence(stringBuilder.toString());
	    					if (ev != null) {
	    						if (ev instanceof FileEvidence) {
	    							((FileEvidence) ev).setFileNamePrefix(customId);
	    						}
	    						builder.addRelatedEvidence(ev);
	    					}
	    				} catch (Exception e) {
	    					continue;
	    				}
	    			}
	    		}
	    	}
	    	
	    	NodeList participants = element.getElementsByTagName("userId");
	    	if (participants != null) {
		    	for (int j = 0; j<participants.getLength(); j++) {
			    	if (participants.item(j) != null && participants.item(j).getFirstChild() != null) {
			    		builder.addParticipant(participants.item(j).getFirstChild().getNodeValue());
			    	}
		    	}
	    	}
	    	
	    	NodeList properties = element.getElementsByTagName("property");
	    	if (properties != null) {
	    		for (int j=0; j<properties.getLength(); j++) {
	    			Element property = (Element) properties.item(j);
	    			if (property.hasAttribute("type")) {
	    				if (property.getAttribute("type").equals("other")) {
	    					if (property.getAttribute("id").equals("rating")) {
		    		    		if (property.hasChildNodes()) {
		    		    			builder.setRatingDescription(decodeString(property.getFirstChild().getNodeValue()));
		    		    		}
	    					}
	    				} else if (property.getAttribute("type").equals("benefit")) {
	    		    		String benefitId = property.getAttribute("id");
	    		    		Benefit benefitRating = Benefit.getBenefit(benefitId);	    		    		
	    		    		if (property.hasAttribute("display")) {
		    		    		String display = property.getAttribute("display");
		    		    		benefitRating.setDisplay(display);
	    		    		}
	    		    		if (property.hasChildNodes()) {
	    		    			benefitRating.setDescription(decodeString(property.getFirstChild().getNodeValue()));
	    		    		}
	    		    		builder.setBenefitRating(benefitRating);	    					
	    				} else if (property.getAttribute("type").equals("effort")) {
	    		    		String effortId = property.getAttribute("id");  
	    		    		Effort effortRating = Effort.getEffort(effortId); 		    		    		
	    		    		if (property.hasAttribute("display")) {
		    		    		String display = property.getAttribute("display");
		    		    		effortRating.setDisplay(display);
	    		    		}
	    		    		if (property.hasChildNodes()) {
	    		    			effortRating.setDescription(decodeString(property.getFirstChild().getNodeValue()));
	    		    		}
	    		    		builder.setEffortRating(effortRating);	    					
	    				}
	    			}
	    		}
	    	}
	    	result.add(builder.build());
	    }
		return result;
	}

	/**
	 * Parses and returns all experiences contained in the given element.
	 * @param elem The element to parse.
	 * @return a list of all contained experienceobjects.
	 */
	public static List<Experience> parseExperiences(final Element elem) {
		NodeList nodes = elem.getElementsByTagName("experience");
		
		if (nodes.getLength() == 0 && elem.getTagName().equals("experience")) {
			nodes = new NodeList() {
				
				@Override
				public Node item(int index) {
					if (index == 0) {
						return elem;
					}
					return null;
				}
				
				@Override
				public int getLength() {
					return 1;
				}
			};
		}
		
		List<Experience> result = new ArrayList<Experience>();
		for (int i=0; i<nodes.getLength(); i++) {
	    	Element element = (Element) nodes.item(i);
	    	String ref = element.getAttribute("ref");
	    	String publisher = element.getAttribute("publisher");
	    	String timestamp = element.getAttribute("timestamp");
	    	String id = element.getAttribute("id");
	    	String customId = element.getAttribute("customId");
	    	if (customId == null || customId.equals("")) {
	    		customId = id;
	    	}
	    	
	    	if (ref == null) {
	    		continue;
	    	}
	    	
	    	Experience experience = new Experience(publisher, ref, timestamp, id);
	    	experience.setCustomId(customId);
	    	if (element.hasAttribute("toDelete")) {
	    		experience.setToDelete();
	    	}
	    	if (element.hasAttribute("sharingLevel")) {
	    		String level = element.getAttribute("sharingLevel");
	    		try {
	    			int sharingLevel = Integer.parseInt(level);
		    		experience.setSharingLevel(sharingLevel);
	    		} catch (NumberFormatException e) {
	    			//ignore as if no sharinglevel was set;
	    		}
	    	}
	    	
	    	NodeList ratingNode = element.getElementsByTagName("rating");
	    	if (ratingNode.getLength() > 0) {
	    		if (ratingNode.getLength() != 1 || !ratingNode.item(0).hasChildNodes()) {
	    			continue;
	    		}
		    	int rating = Integer.parseInt(ratingNode.item(0).getFirstChild().getNodeValue());
		    	experience.setRating(rating);
	    	}
	    	
	    	NodeList commentNode = element.getElementsByTagName("comment");
	    	if (commentNode.getLength() > 0) {
	    		if (commentNode.getLength() != 1 || !commentNode.item(0).hasChildNodes()) {
	    			continue;
	    		}
	    		String comment = commentNode.item(0).getFirstChild().getNodeValue();
	    		experience.setComment(decodeString(comment));
	    	}
	    	NodeList propertyNode = element.getElementsByTagName("property");
	    	if (propertyNode.getLength() > 0) {
	    		for (int j=0; j<propertyNode.getLength(); j++) {
	    			Element property = (Element) propertyNode.item(j);
	    			String type = property.getAttribute("type");
	    			if ("benefit".equals(type)) {
	    				try {
	    					Benefit benefit = Benefit.getBenefit(property.getAttribute("id"));
		    				benefit.setValue(Integer.parseInt(property.getAttribute("value")));
		    				experience.setBenefit(benefit);
	    				} catch (Exception e) {
	    					
	    				}
	    			} else if ("effort".equals(type)) {
	    				try {
		    				Effort effort = Effort.getEffort(property.getAttribute("id"));
		    				effort.setValue(Integer.parseInt(property.getAttribute("value")));
		    				experience.setEffort(effort);
	    				} catch (Exception e) {
	    					
	    				}
	    			}
	    		}
	    	}

	    	NodeList objects = element.getElementsByTagName("object");
	    	if (objects != null) {
	    		List<Evidence> evs = new ArrayList<Evidence>();
	    		for (int j = 0; j<objects.getLength(); j++) {
	    			Element objectNode = (Element) objects.item(j);
	    			if (!objectNode.hasChildNodes()) continue; 			

    				NodeList childNodes = objectNode.getChildNodes();
	    			StringBuilder stringBuilder = new StringBuilder();
    				if (childNodes.getLength() > 1) {
	    				for (int k=0; k<childNodes.getLength(); k++){
	    					try {
		    					CDATASection cdataObject = (CDATASection) childNodes.item(k);
		    					stringBuilder.append(decodeString(cdataObject.getData()));
	    					} catch (ClassCastException e) {
	    						stringBuilder.append(decodeString(childNodes.item(k).getNodeValue()));
	    					}
	    				}
    				} else {
    					stringBuilder.append(decodeString(childNodes.item(0).getNodeValue()));
    				}
	    			
					Evidence ev = parseDataObjectToEvidence(stringBuilder.toString());
					if (ev != null) {
						if (ev instanceof FileEvidence) {
							((FileEvidence) ev).setFileNamePrefix(customId);
						}
						evs.add(ev);
					}
	    		}
	    		experience.setEvidences(evs);
	    	}
	    	
	    	
	    	result.add(experience);
	    }
		return result;
	}

	/**
	 * Parses and returns all recommendationstati contained in the given element.
	 * @param elem The element to parse.
	 * @return a list of all contained recommendationstati.
	 */
	public static List<RecommendationStatus> parseRecommendationStati(final Element elem) {
		NodeList nodes = elem.getElementsByTagName("recommendationstatus");
		
		if (nodes.getLength() == 0 && elem.getTagName().equals("recommendationstatus")) {
			nodes = new NodeList() {
				
				@Override
				public Node item(int index) {
					if (index == 0) {
						return elem;
					}
					return null;
				}
				
				@Override
				public int getLength() {
					return 1;
				}
			};
		}
		
		List<RecommendationStatus> result = new ArrayList<RecommendationStatus>();
		for (int i=0; i<nodes.getLength(); i++) {
	    	Element element = (Element) nodes.item(i);
	    	
	    	String ref = element.getAttribute("ref");
	    	String publisher = element.getAttribute("publisher");
	    	String timestamp = element.getAttribute("timestamp");
	    	
	    	String user = null;
	    	NodeList userNode = element.getElementsByTagName("user");
	    	if (userNode.getLength() > 0) {
	    		if (userNode.getLength() != 1 || !userNode.item(0).hasChildNodes()) {
	    			continue;
	    		}
		    	user = userNode.item(0).getFirstChild().getNodeValue();
	    	}
	    	
	    	Status recStatus = null;
	    	NodeList statusNode = element.getElementsByTagName("status");
	    	if (statusNode.getLength() > 0) {
	    		if (statusNode.getLength() != 1 || !statusNode.item(0).hasChildNodes()) {
	    			continue;
	    		}
	    		String stat = statusNode.item(0).getFirstChild().getNodeValue();
	    		recStatus = Status.parseStatus(stat);
	    	}	

	    	if (recStatus == null || user == null || ref == null) continue;
	    	RecommendationStatus status = new RecommendationStatus(recStatus, user, ref);
	    	status.setPublisher(publisher);
	    	status.setTimestamp(timestamp);
	    	result.add(status);
	    }
		return result;
	}
	
	/**
	 * Parses a given dataobject in xml-form to an evidenceobject.
	 * @param xml The dataobject as an xml-string.
	 * @return the parsed evidenceobject.
	 */
	public static Evidence parseDataObjectToEvidence(String xml) {
		Evidence result = null;
		if (xml.contains("<") && xml.contains(">") && xml.contains("xmlns=")) {
			Element elem = parseXMLStringToElement(xml);
			if (elem != null) {
				try {
					String ns = elem.getNamespaceURI();
					if (ns == null || (ns.contains("activityrecommendationapp") && !ns.contains("experience") && !ns.contains("textevidence"))) {
						return null;
					}
					String id = elem.getAttribute("id");
					String name = elem.getTagName();
					String timestamp = elem.getAttribute("timestamp");
					NodeList attachments = elem.getElementsByTagName("attachment");
					if (ns.contains("textevidence") && attachments != null && attachments.getLength() == 1) {
						Element attachment = (Element)attachments.item(0);
						String fileName;
						String location;
						Element contentElem = (Element) elem.getElementsByTagName("content").item(0);
						String content = ((CDATASection)contentElem.getFirstChild()).getData();
						if (attachment.hasAttribute("filename")) {
							fileName = attachment.getAttribute("filename");
							location = attachment.getAttribute("location");
						} else {
							Element link = (Element) attachment.getFirstChild();
							String url = link.getAttribute("url");
							location = URL.decodeQueryString(url.split("/")[url.split("/").length - 2]);
							fileName = link.getAttribute("id");
						}
						result = new FileEvidence(id, name, timestamp, content, fileName);
						((FileEvidence)result).setLocation(location);
					} else {
						result = new Evidence(id, name, timestamp, xml);
					}
					String publisher = null;
					result.setNS(ns);
					if (elem.hasAttribute("publisher")) {
						publisher = elem.getAttribute("publisher");
						result.setPublisher(publisher);
					}
		
					return result;			
				} catch (Exception e) {
					result = new Evidence(ARApp.uuid(), "text", HasTimestamp.TIMESTAMPFORMAT.format(new Date()), xml);
				}			
			} else {
				result = new Evidence(ARApp.uuid(), "text", HasTimestamp.TIMESTAMPFORMAT.format(new Date()), xml);
			}
		} else {
			result = new Evidence(ARApp.uuid(), "text", HasTimestamp.TIMESTAMPFORMAT.format(new Date()), xml);			
		}
		return result;		
	}

}
