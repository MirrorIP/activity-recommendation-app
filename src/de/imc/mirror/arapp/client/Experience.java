package de.imc.mirror.arapp.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;

public class Experience implements HasTimestamp{
	
	private int rating;
	private int sharingLevel;
	private boolean toDelete;
	private String comment;
	private Benefit benefit;
	private Effort effort;
	
	private String recommendationId;
	private String publisher;
	private String id;
	private String timestamp;
	private String customId;
	private List<Evidence> evidences;
	
	public Experience(String publisher, String recommendationId, String timestamp, String id){
		this.publisher = publisher;
		this.recommendationId = recommendationId;
		this.timestamp = timestamp;
		this.id = id;
		this.comment = null;
		this.customId = null;
		this.rating = -1;
		this.effort = null;
		this.benefit = null;
		this.evidences = new ArrayList<Evidence>();
	}
	
	/**
	 * Sets the custom id of this experience.
	 * @param id the custom id to be set.
	 */
	public void setCustomId(String id) {
		this.customId = id;
	}

	/**
	 * Sets the comment of this experience.
	 * @param comment the comment to be set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Sets the rating of this experience.
	 * @param rating the rating to be set
	 */
	public void setRating(int rating) {
		this.rating = rating;
	}

	/**
	 * Sets the benefit of this experience.
	 * @param benefit the benefit to be set
	 */
	public void setBenefit(Benefit benefit) {
		this.benefit = benefit;
	}

	/**
	 * Sets the effort of this experience.
	 * @param effort the effort to be set
	 */
	public void setEffort(Effort effort) {
		this.effort = effort;
	}

	/**
	 * Sets the evidences which are attached to this experience.
	 * @param evidences a list of evidences to be set
	 */
	public void setEvidences(List<Evidence> evidences) {
		this.evidences.clear();
		if (evidences != null) {
			this.evidences.addAll(evidences);
		}
	}
	
	/**
	 * @return the custom id of this experience
	 */
	public String getCustomId() {
		return customId;
	}

	/**
	 * @return the rating of this experience
	 */
	public int getRating() {
		return rating;
	}

	/**
	 * @return the comment of this experience
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return the benefitvalue of this experience
	 */
	public double getBenefitValue() {
		if (benefit == null) {
			return -1;
		}
		return benefit.getValue();
	}

	/**
	 * @return the benefit of this experience
	 */
	public Benefit getBenefit() {
		return benefit;
	}

	/**
	 * @return the effort of this experience
	 */
	public Effort getEffort() {
		return effort;
	}

	/**
	 * @return the effortvalue of this experience
	 */
	public double getEffortValue() {
		if (effort == null) {
			return -1;
		}
		return effort.getValue();
	}

	/**
	 * @return the id of the recommendation this experience is for.
	 */
	public String getRecommendationId() {
		return recommendationId;
	}
	
	public void anonymizeExperience() {
		this.publisher = "Anonymous";
	}

	/**
	 * @return the publisher of this experience
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * @return the id of this experience
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return a list of evidences of this experience
	 */
	public List<Evidence> getEvidences() {
		return evidences;
	}
	
	public void setSharingLevel(int level) {
		this.sharingLevel = level;
	}
	
	public int getSharingLevel() { 
		return sharingLevel;
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
	
	public void setToDelete() {
		this.toDelete = true;
	}
	
	public boolean getToDelete() {
		return toDelete;
	}
	
	public String toString() {
		String namespace = "mirror:application:activityrecommendationapp:experience";
		Document doc = XMLParser.createDocument();
		Element root = Parser.createElement("experience", namespace);
		root.setAttribute("ref", recommendationId);
		root.setAttribute("customId", customId);
		root.setAttribute("publisher", publisher);
		root.setAttribute("timestamp", timestamp);
		root.setAttribute("modelVersion", "1.0");
		root.setAttribute("cdmVersion", "2.0");
		if (toDelete) {
			root.setAttribute("toDelete", "true");
		}
		if (sharingLevel > 0 && sharingLevel <= 5) {
			root.setAttribute("sharingLevel", sharingLevel + "");
		}
		
		Element creationInfo = Parser.createElement("creationInfo", namespace);
		Element personElement = Parser.createElement("person", namespace);
		personElement.appendChild(doc.createTextNode(publisher));
		
		Element dateElement = Parser.createElement("date", namespace);
		dateElement.appendChild(doc.createTextNode(HasTimestamp.TIMESTAMPFORMAT.format(new Date())));

		creationInfo.appendChild(dateElement);
		creationInfo.appendChild(personElement);
		
		root.appendChild(creationInfo);

		if (this.rating != -1) {
			Element rating = Parser.createElement("rating", namespace);
			rating.appendChild(doc.createTextNode(this.rating + ""));
			root.appendChild(rating);
		}
		
		if (this.comment != null) {
			Element comment = Parser.createElement("comment", namespace);
			comment.appendChild(doc.createTextNode(this.comment));
			root.appendChild(comment);			
		}
		
		if (this.benefit != null || this.effort != null) {
			Element properties = Parser.createElement("properties", namespace);
			if (this.benefit != null) {
				Element property = Parser.createElement("property", namespace);
				property.setAttribute("id", benefit.getId());
				property.setAttribute("type", "benefit");
				property.setAttribute("value", benefit.getValue() + "");
				properties.appendChild(property);
			}
			if (this.effort != null) {
				Element property = Parser.createElement("property", namespace);
				property.setAttribute("id", effort.getId());
				property.setAttribute("type", "effort");
				property.setAttribute("value", effort.getValue() + "");
				properties.appendChild(property);				
			}
			root.appendChild(properties);
		}

		if (evidences.size() > 0) {
			Element relatedObjects = Parser.createElement("relatedObjects", namespace);
			for (Evidence ev:evidences) {
				Element objectElem = Parser.createElement("object", namespace);
				String cdata = ev.toString();
				objectElem.appendChild(doc.createTextNode(cdata));
				objectElem.setAttribute("id", ev.getId());
				relatedObjects.appendChild(objectElem);
			}
			root.appendChild(relatedObjects);	
		}
		
		return root.toString();
	}
	

	/**
	 * Parses the recommendationobject to an dataobject.
	 * @return the recommendationobject as a Dataobject.
	 */
	public native JavaScriptObject toDataObject() /*-{
		var expString = this.@de.imc.mirror.arapp.client.Experience::toString()();
		var xmlDoc;
		if (window.DOMParser){
			var parser = new DOMParser();
		  	xmlDoc = parser.parseFromString(expString, 'text/xml');
		} else { // Internet Explorer
			xmlDoc = new ActiveXObject('MSXML.DOMDocument');
			xmlDoc.async = false;
			xmlDoc.loadXML(expString);
		}
		return new $wnd.SpacesSDK.DataObject(xmlDoc.childNodes[0]);
	}-*/;

}
