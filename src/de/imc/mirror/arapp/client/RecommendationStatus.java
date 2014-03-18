package de.imc.mirror.arapp.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public class RecommendationStatus implements HasTimestamp{
	
	public enum Status{
		IGNORED("ignored"),
		SOLVED("solved"),
		OPEN("open");
		
		private String status;
		
		private Status(String status) {
			this.status = status;
		}
		
		public String getStatus() {
			return status;
		}
		
		public static Status parseStatus(String status) {
			for (Status stat:Status.values()) {
				if (stat.getStatus().equals(status)) {
					return stat;
				}
			}
			return null;
		}
	}
	
	private Status status;
	private String timestamp;
	private String publisher;
	private String user;
	private String ref;
	
	/**
	 * Creates a new RecommendationStatus for a person and a recommendation.
	 * @param status the new status.
	 * @param user the user this status is for.
	 * @param ref the custom id of the recommendation this status is for.
	 */
	public RecommendationStatus(Status status, String user, String ref) {
		this.status = status;
		this.user = user;
		this.ref = ref;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Sets the publisher of the RecommendationStatus.
	 * @param publisher the publisher who published this status.
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	/**
	 * @return the jid of the person who published this status.
	 */
	public String getPublisher() {
		return publisher;
	}
	
	/**
	 * @return the status(OPEN, SOLVED, IGNORED) this object stands for.
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * @return the jid of the user this status is for.
	 */
	public String getUser() {
		return user;
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public String getFormattedTimestamp(DateTimeFormat dateFormat) {
		return dateFormat.format(TIMESTAMPFORMAT.parseStrict(timestamp));
	}
	
	/**
	 * @return the custom id of the recommendation this status is for.
	 */
	public String getRef() {
		return ref;
	}


	public String toString() {
		if (ref == null || user == null || status == null) return null;
		String namespace = "mirror:application:activityrecommendationapp:recommendationstatus";
		Document doc = XMLParser.createDocument();
		Element root = Parser.createElement("recommendationstatus", namespace);
		root.setAttribute("publisher", publisher);
		root.setAttribute("modelVersion", "1.0");
		root.setAttribute("cdmVersion", "2.0");
		root.setAttribute("ref", ref);
		
		Element creationInfo = Parser.createElement("creationInfo", namespace);
		Element personElement = Parser.createElement("person", namespace);
		personElement.appendChild(doc.createTextNode(publisher));
		
		Element dateElement = Parser.createElement("date", namespace);
		dateElement.appendChild(doc.createTextNode(HasTimestamp.TIMESTAMPFORMAT.format(new Date())));

		creationInfo.appendChild(dateElement);
		creationInfo.appendChild(personElement);
		
		Element user = Parser.createElement("user", namespace);
		user.appendChild(doc.createTextNode(this.user));
		Element userStatus = Parser.createElement("status", namespace);
		userStatus.appendChild(doc.createTextNode(this.status.getStatus()));
		
		root.appendChild(creationInfo);
		root.appendChild(user);
		root.appendChild(userStatus);
		return root.toString();
	}
}
