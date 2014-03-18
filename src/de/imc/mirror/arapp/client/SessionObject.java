package de.imc.mirror.arapp.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.xml.client.CDATASection;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class SessionObject {
	
	/**
	 * parses an xml-string to a dataobject.
	 * @param xml The xml-string
	 * @return the dataobject.
	 */
	private static native JavaScriptObject stringToDataObject(String xml) /*-{
		var xmlDoc;
		if (window.DOMParser){
			var parser = new DOMParser();
		  	xmlDoc = parser.parseFromString(xml, 'text/xml');
		} else { // Internet Explorer
			xmlDoc = new ActiveXObject('MSXML.DOMDocument');
			xmlDoc.async = false;
			xmlDoc.loadXML(xml);
		}
		return new $wnd.SpacesSDK.DataObject(xmlDoc.childNodes[0]);
	}-*/;
	
	/**
	 * Returns the name of the latest sessionobject.
	 * @param xml A xml-string containing at least one sessionobject.
	 * @return The name.
	 */
	public static String getLatestNameFromSessionObject(String xml) {
		Element latestSessionElement = getLatestCompleteSessionObject(xml);
		if (latestSessionElement != null) {
			NodeList titleList = latestSessionElement.getElementsByTagName("title");
			Element titleElement = (Element) titleList.item(0);
			return titleElement.getFirstChild().getNodeValue();
		}
		return "";
	}
	
	/**
	 * Method to get the type of the given sessionxml.
	 * @param xml the sessionobject as an xmlstring.
	 * @return the type of the sessionobject, or an empty string if none was found.
	 */
	public static String getType(String xml) {
		Element elem = Parser.parseXMLStringToElement(xml);
		if (elem != null) {
			String type = elem.getAttribute("type");
			return type;	
		}
		return "";
	}
	
	/**
	 * Method to determine if a given person is the moderator of the given session.
	 * @param xml the sessionobject as a string.
	 * @param bareJid the userjid to check.
	 * @return if the given user is the moderator.
	 */
	public static boolean isModerator(String xml, String bareJid) {
		Element latestSessionElement = getLatestCompleteSessionObject(xml);
		if (latestSessionElement != null) {
			NodeList moderatorNodes = latestSessionElement.getElementsByTagName("moderator");
			if (moderatorNodes.getLength() != 0) {
				if (moderatorNodes.item(0).hasChildNodes() && moderatorNodes.item(0).getFirstChild().getNodeValue().equals(bareJid)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if the given sessionobject is older than one hour.
	 * @param xml the sessionobject to check.
	 * @return if the given sessionobject is older than one hour.
	 */
	public static boolean isObsolete(String xml) {
		Element latestSessionElement = getLatestCompleteSessionObject(xml);
		if (latestSessionElement != null) {
			Date timestamp = HasTimestamp.TIMESTAMPFORMAT.parse(latestSessionElement.getAttribute("timestamp"));
			Date currentDate = new Date();
			if (currentDate.getTime() - timestamp.getTime() > 3600000) {
				NodeList participantsList = latestSessionElement.getElementsByTagName("participants");
				if (participantsList.getLength() == 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Parses the xml and returns the latest sessionobject that has the type attribute set to "complete".
	 * @param xml the xml to parse.
	 * @return the latest sessionobject with the type attribute set to "complete" or null if no such sessionobject is contained.
	 */
	public static Element getLatestCompleteSessionObject(String xml) {
		final Element elem = Parser.parseXMLStringToElement(xml);
		if (elem != null) {
			Element latestSessionElement = null;
			Date latestSessionDate = null;
			NodeList nodes = elem.getElementsByTagName("session");
			if (nodes.getLength() == 0 ) {
				if (elem.getTagName().equals("session")) {
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
				} else {
					return null;
				}
			}
			for (int i=0; i<nodes.getLength(); i++) {
				Element sessionElement = (Element) nodes.item(i);
				if (sessionElement.hasAttribute("type") && sessionElement.getAttribute("type").equals("complete")) {
					String timestamp = sessionElement.getAttribute("timestamp");
					Date date = HasTimestamp.TIMESTAMPFORMAT.parseStrict(timestamp);
					if (latestSessionDate != null) {
						if (date.before(latestSessionDate)) {
							continue;
						}
					}
					latestSessionDate = date;
					latestSessionElement = sessionElement;
				}
			}
			return latestSessionElement;
		}
		return null;
	}
	
	/**
	 * Checks if the given xml-string contains a sessionobject with the typeattribute set to <code>complete</code>
	 * @param xml The xml-string containing a sessionobject.
	 * @return if the session object is of type <code>complete</code>
	 */
	public static boolean isOfCompleteType(String xml) {
		Element elem = Parser.parseXMLStringToElement(xml);
		if (elem != null) {
			String type = elem.getAttribute("type");
			return type.equals("complete");
		}
		return false;
	}
	
	/**
	 * Parses the information contained in the given sessionobject.
	 * @param xml The sessionobject as a xml-string.
	 * @return a map consisting of all infos.
	 */
	public static Map<String, String> getAllInformation(String xml) {
		Element elem = Parser.parseXMLStringToElement(xml);
		Map<String, String> infoMap = new HashMap<String, String>();
		if (elem != null) {
			if (elem.hasAttribute("timestamp")) {
				infoMap.put("timestamp", elem.getAttribute("timestamp"));
			}
			if (elem.hasAttribute("publisher")) {
				infoMap.put("publisher", elem.getAttribute("publisher"));
			}
			NodeList nodes = elem.getChildNodes();
			for (int i=0; i<nodes.getLength(); i++) {
				Element childElem = (Element) nodes.item(i);
				if (childElem.getTagName().equals("recommendation")) continue;
				if (childElem.getTagName().equals("entry")) {
					String type = childElem.getAttribute("type");
					infoMap.put("entry", ((CDATASection)childElem.getFirstChild()).getData());
					infoMap.put("id", childElem.getAttribute("id"));
					infoMap.put("type", type);
				} else if (childElem.getTagName().equals("user")) {
					infoMap.put("user", childElem.getFirstChild().getNodeValue());
					infoMap.put("type", childElem.getAttribute("type"));
				}
				if (childElem.getFirstChild() != null) {
					infoMap.put(childElem.getTagName(), childElem.getFirstChild().getNodeValue());
				} else {
					infoMap.put(childElem.getTagName(), "");
				}
			}
		}
		return infoMap;
	}
	
	/**
	 * Creates a new sessionobject which will lead to showing the publishing wizard for all persons in the same discussion.
	 * @return the newly created sessionobject.
	 */
	public static JavaScriptObject createNewShowPublishWizardSessionObject() {
		StringBuilder builder = new StringBuilder();
		builder.append("<session xmlns='mirror:application:activityrecommendationapp:session' type='publish'/>");
		return stringToDataObject(builder.toString());
	}

	/**
	 * Creates a new sessionobject which contains the given vote.
	 * @param publisher the publisher of the vote.
	 * @param decission the vote the publisher chose.
	 * @return the newly created sessionobject.
	 */
	public static JavaScriptObject createVoteSessionObject(String publisher, String decission) {
		StringBuilder builder = new StringBuilder();
		builder.append("<session xmlns='mirror:application:activityrecommendationapp:session' type='vote' publisher='");
		builder.append(publisher)
			   .append("'><vote>")
			   .append(decission)
			   .append("</vote></session>");
		return stringToDataObject(builder.toString());
	}
	
	/**
	 * Conveniencemethod to create a <code>complete</code> session object.
	 * @param recomm The recommendation to be sent with this sessionobject.
	 * @param infos A map containing all infos to be sent with this sessionobject.
	 * @return The sessionobject as a dataobject.
	 */
	public static JavaScriptObject createCompleteSessionObject(RecommendationObject recomm, Map<String, String> infos, String timestamp) {
		String namespace = "mirror:application:activityrecommendationapp:session";
		Document doc = XMLParser.createDocument();
		Element root = Parser.createElement("session", namespace);
		root.setAttribute("type", "complete");
		if (timestamp != null) {
			root.setAttribute("timestamp", timestamp);
		}
		root.appendChild(recomm.createRecommendationXMLElement());
		
		for (String tagName:infos.keySet()) {
			Element tag = Parser.createElement(tagName, namespace);
			tag.appendChild(doc.createTextNode(infos.get(tagName)));
			root.appendChild(tag);
		}
		return stringToDataObject(root.toString());
	};
}
