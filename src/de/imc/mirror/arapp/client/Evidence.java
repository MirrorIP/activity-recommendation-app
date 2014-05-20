package de.imc.mirror.arapp.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;

import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;

public class Evidence implements HasTimestamp {
	
	protected String id;
	private String locationId;
	protected String type;
	protected String timestamp;
	private String publisher;
	private String xml;
	private String ns;

	public enum Namespace {

		ACTIVITYRECOMMENDATIONAPPEXPERIENCE ("mirror:application:activityrecommendationapp:experience", "ARA Experience"),
		ACTIVITYRECOMMENDATIONAPPTEXTEVIDENCE ("mirror:application:activityrecommendationapp:textevidence", "Text"),
		CROMARRECOMMENDATION ("mirror:application:cromar:recommendation", "Cromar Recommendation"),
		MOODMAPMOOD ("mirror:application:moodmap:mood", "Mood"),
		CLINICNOTE ("mirror:application:thinkbettercare:note", "Clinic Note"),
		CLINICSCORE ("mirror:application:thinkbettercare:score", "Clinic Score"),
		ISSUEMANAGEMENTAPPISSUE ("mirror:application:ima:issue", "Issuemanagement Issue"),
		DOCTRAINNOTE ("mirror:application:doctrain", "Doctrain Note"),
		KNOWSELF("mirror:application:knowself:visualization", "Knowself Visualization"),
		OTHER("","");
		
		private String ns;
		private String type;
		
		private Namespace(String ns, String type) {
			this.ns = ns;
			this.type = type;
		}
		
		public static Namespace parse(String nasp) {
			for (Namespace namespace:Namespace.values()) {
				if (namespace.ns.equals(nasp)) {
					return namespace;
				}
			}
			return OTHER;
		}
		
		public String getType() {
			return type;
		}
		
	};
	
	/**
	 * Constructor for a new Evidenceobject.
	 * @param id The id of the evidence.
	 * @param type The "type" of the evidence. At the moment it is the name of the rootelement.
	 * @param timestamp The timestamp of the evidence.
	 * @param xml The evidence as a xmlstring.
	 */
	public Evidence(String id, String type, String timestamp, String xml) {
		this.id = id;
		this.type = type;
		this.timestamp = timestamp;
		this.xml = xml;
	}
	
	protected Evidence() {
	}

	@Override
	public String toString() {
		return xml;
	}

	/**
	 * @return the Id of the evidence.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the Id of the space the evidence lies on. <code>null</code> if it is not set.
	 */
	public String getLocationId() {
		return locationId;
	}

	/**
	 * @return the type of the evidence.
	 */
	public String getType() {
		if (ns != null && Namespace.parse(ns) != Namespace.OTHER) {
			return Namespace.parse(ns).getType();
		}
		return type;
	}

	/**
	 * @return the publisher of this evidence.
	 */
	public String getPublisher() {
		return publisher;
	}
	
	/**
	 * @return the namespace of this evidence
	 */
	public String getNS() {
		return ns;
	}
	
	/**
	 * Sets the location of this evidence.
	 * @param id the id of the space the evidence lies on.
	 */
	public void setLocation(String id){
		this.locationId = id;
	}
	
	/**
	 * Sets the publisher of this evidence.
	 * @param publisher the publisher of this evidence.
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	/**
	 * Sets the namespace of this evidence.
	 * @param ns the namespace of this evidence.
	 */
	public void setNS(String ns) {
		this.ns = ns;
	}

	@Override
	public String getTimestamp() {
		return timestamp;
	}
	
	@Override
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getFormattedTimestamp(DateTimeFormat dateFormat) {
		return dateFormat.format(TIMESTAMPFORMAT.parseStrict(timestamp));
	}
	
	@Override
	public int hashCode() {
		if (id == null) return 0;
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Evidence) {
			Evidence e = (Evidence) o;
			if (id == null || e.getId() == null) return false;
			if (e.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Method to check if to evidenceobjects are equal except the timestamp, publisher and the id.
	 * @param o The object to check against.
	 * @return if the object is a copy of the given one.
	 */
	public boolean isCopyOf(Object o) {
		if (o instanceof Evidence) {
			Evidence e = (Evidence) o;
			Element elem1 = Parser.parseXMLStringToElement(xml);
			Element elem2 = Parser.parseXMLStringToElement(e.toString());
			if (elem1 == null || elem2 == null) {
				return false;
			}
			return equalsForElements(elem1, elem2);
		}
		return false;
	}
	
	/**
	 * Recursive method to check if two Nodeobjects are the same except for the publisher, timestamp und id attributes. 
	 * @param node1 A node to check.
	 * @param node2 A node to check.
	 * @return if the two nodes denote the same data.
	 */
	private boolean equalsForElements(Node node1, Node node2) {
		Element elem1 = (Element) node1;
		Element elem2 = (Element) node2;
		NodeList childs1 = elem1.getChildNodes();
		if (childs1 != null) {
			for (int i=0; i<childs1.getLength(); i++) {
				Node child = childs1.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				if (!equalsForElements(child, elem2)) {
					return false;
				}
				NodeList elemChilds = null;
				if (child.getNodeName().contains("cdt:")) {
					String tagName = child.getNodeName().replace("cdt:", "");
					Node node = child.getParentNode();
					NodeList children = elem2.getElementsByTagName(node.getNodeName());
					if (children.getLength() == 0) return false;
					else if (children.getLength() == 1) {
						elemChilds = ((Element)children.item(0)).getElementsByTagName(tagName);
					} else {
						for (int k = 0; k<children.getLength(); k++) {
							Element item = (Element) children.item(k);
							NodeList children2 = item.getElementsByTagName(tagName);
							if (children2.getLength() == 0) continue;
							if (children2.item(0).getNamespaceURI().equals(child.getNamespaceURI())) {
								elemChilds = children2;
							}
						}
					}
				} else {
					elemChilds = elem2.getElementsByTagName(child.getNodeName());					
				}
				if (elemChilds == null) return false;
				String value1 = child.getNodeValue();
				boolean foundChild = false;
				for (int j=0; j<elemChilds.getLength(); j++) {
					if (value1 != null && elemChilds.item(j).getNodeValue() != null) {
						if (!checkAttributes(child, elemChilds.item(j))) {
							continue;
						}
						if (!value1.equals(elemChilds.item(j).getNodeValue())) {
							continue;
						}
					}
					foundChild = true;
					elemChilds.item(j).getParentNode().removeChild(elemChilds.item(j));
					break;
				}
				if (!foundChild) {
					return false;
				}
			}
			
		}
		return true;
	}
	
	private boolean checkAttributes(Node elem1, Node elem2) {
		if (elem1.getNodeType() == Node.TEXT_NODE) {
			if (elem2.getNodeType() == Node.TEXT_NODE) {
				if (elem1.getNodeValue().equals(elem2.getNodeValue())) {
					return true;
				}
			} else {
				return false;
			}
		} else if (elem2.getNodeType() == Node.TEXT_NODE) {
			return false;
		}
		if (!elem1.hasAttributes()) {
			if (elem2.hasAttributes()) {
				return false;
			}
		} else if (!elem2.hasAttributes()) {
			return false;
		} else {
			NamedNodeMap attr1 = elem1.getAttributes();
			NamedNodeMap attr2 = elem2.getAttributes();
			if (attr1.getLength() > 0 && attr1.getNamedItem("publisher")!=null) {
				if (attr2.getLength() > 0 && attr2.getNamedItem("publisher") != null) {
					if (attr1.getLength() != attr2.getLength()) {
						return false;
					}
				} else {
					if (attr1.getLength()-1 != attr2.getLength()) {
						return false;
					}
				}
			} else {
				if (attr2.getLength() > 0 && attr2.getNamedItem("publisher") != null) {
					if (attr1.getLength()+1 != attr2.getLength()) {
						return false;
					}
				} else {
					if (attr1.getLength() != attr2.getLength()) {
						return false;
					}
				}
			}
			for (int i=0; i<attr1.getLength(); i++) {
				String name = attr1.item(i).getNodeName();
				if (name.equals("publisher") || name.equals("timestamp") || name.equals("id")) continue;
				else {
					Node node = attr2.getNamedItem(name);
					if (node == null) {
						return false;
					}
					String value = node.getNodeValue();
					if (value == null || !value.equals(attr1.getNamedItem(name).getNodeValue())) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
