package de.imc.mirror.arapp.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public class RecommendationObject implements HasTimestamp {
	
	public enum TaskType{
		ACTIVITY("Activity Task", "activity"),
		BEHAVIOR("Behavior Task", "behavior"),
		LEARNING("Learning Task", "learning");
		
		private String id;
		private String display;
		
		private TaskType(String display, String id) {
			this.id = id;
			this.display = display;
		}
		
		public static TaskType getTaskType(String id) {
			for (TaskType type:TaskType.values()) {
				if (type.id.equals(id)) {
					return type;
				}
			}
			return null;
		}
		
		public String getId() {
			return id;
		}
		
		public String getDisplay() {
			return display;
		}
	} 
	
	private String id;
	private String customId;
	private String issue;
	private String recommendedSolution;
	private String timestamp;
	private String title;
	private String publisher;
	private String previousRevision;
//	private String updatesObject;
	private List<Entry> entries;
	private List<String> participants;
	private List<Evidence> relatedEvidence;
	private List<String> relatedEvidenceIds;
	private List<String> targetSpaces;
	private boolean state;
	
	private String ratingDescription;
	private Benefit benefitRating;
	private Effort effortRating;
	
	private boolean toDelete;
	private boolean deleted;
	private TaskType type;
	
	protected RecommendationObject(String id, String customId, String timestamp, String publisher, String title, String issue, String recommendedSolution, TaskType type, List<Entry> entries, boolean state) {
		this.title = title;
		this.id = id;
		this.customId = customId;
		this.issue = issue;
		this.recommendedSolution = recommendedSolution;
		this.timestamp = timestamp;
		this.state = state;
		if (publisher != null) {
			this.publisher = publisher;
		} else {
			this.publisher = "";
		}		
		this.entries = entries;
		if (this.entries == null) {
			this.entries = new ArrayList<Entry>();
		}
		this.participants = new ArrayList<String>();
//		this.updatesObject = null;
		relatedEvidence = new ArrayList<Evidence>();
		relatedEvidenceIds = new ArrayList<String>();
		previousRevision = null;
		this.type = type;
	}
	
	/**
	 * Sets the benefit for this recommendation.
	 * @param rating the benefit to be set.
	 */
	public void setBenefitRating(Benefit rating) {
		this.benefitRating = rating;
	}

	/**
	 * Sets the effort for this recommendation.
	 * @param rating the effort to be set.
	 */
	public void setEffortRating(Effort rating) {
		this.effortRating = rating;
	}
	
	/**
	 * @return the benefit of this recommendation.
	 */
	public Benefit getBenefitRating() {
		return benefitRating;
	}
	
	/**
	 * @return the effort of this recommendation.
	 */
	public Effort getEffortRating() {
		return effortRating;
	}
	
	/**
	 * Sets the custom id of the recommendation this recommendation references.
	 * @param ref the custom id of the previous recommendation.
	 */
	public void setPreviousRevision(String ref) {
		previousRevision = ref;
	}
	
	/**
	 * Sets the description for the rating for this recommendation.
	 * @param description The description for the rating.
	 */
	public void setRatingDescription(String description) {
		this.ratingDescription = description;
	}
	
	/**
	 * @return the description for the rating for this recommendation.
	 */
	public String getRatingDescription() {
		return ratingDescription;
	}
	
	/**
	 * @return if this recommendation is a newer version of another.
	 */
	public boolean hasPreviousRevisions() {
		return previousRevision != null;
	}

	/**
	 * @return the custom id of the previous recommendation.
	 */
	public String getPreviousRevisions() {
		return previousRevision;
	}
	
	/**
	 * @return the publisher of this recommendation.
	 */
	public String getPublisher() {
		return publisher;
	}
	
	/**
	 * @return the id of this recommendation.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the custom id of this recommendation.
	 */
	public String getCustomId() {
		return customId;
	}
	
	/**
	 * Gets all minuteentries which were created during the discussion.
	 * @return a list of entries.
	 */
	public List<Entry> getEntries() {
		return entries;
	}

	/**
	 * @return the issue of this recommendation.
	 */
	public String getIssue() {
		return issue;
	}

	/**
	 * @return the recommended solution of this recommendation.
	 */
	public String getRecommendedSolution() {
		return recommendedSolution;
	}

	public String getFormattedTimestamp(DateTimeFormat dateFormat) {
		return dateFormat.format(TIMESTAMPFORMAT.parseStrict(timestamp));
	}

	/**
	 * @return if this recommendation is currently active or not.
	 */
	public boolean getState() {
		return state;
	}
	
	/**
	 * @return the title of this recommendation.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Adds a participant to the list of users, who discussed this recommendation.
	 * @param userId the id of the user.
	 */
	public void addParticipant(String userId) {
		if (this.participants == null) {
			this.participants = new ArrayList<String>();
		}
		this.participants.add(userId);
	}

	/**
	 * @return a list of userids, which participated at the discussion of this recommendation.
	 */
	public List<String> getParticipants() {
		return participants;
	}

	/**
	 * Sets the participants of the discussion of this recommendation.
	 * @param participants a list of userids.
	 */
	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}
	
	/**
	 * Sets a list of space ids on which this recommendation will be published.
	 * @param targetSpaces the list of space ids.
	 */
	public void setTargetSpaces(List<String> targetSpaces) {
		this.targetSpaces = targetSpaces;
	}

	/**
	 * @return a list of space ids this recommendation will be or was published on.
	 */
	public List<String> getTargetSpaces() {
		return targetSpaces;
	}
	
	/**
	 * Attaches evidences to this recommendation.
	 * @param objs the evidences to be attached.
	 */
	public void setRelatedEvidence(List<Evidence> objs) {
		this.relatedEvidence = objs;
	}
	
	/**
	 * If a evidence should not be attached completely, this will give the opportunity to only set its id.
	 * @param ids a list of ids.
	 */
	public void setRelatedEvidenceIds(List<String> ids) {
		this.relatedEvidenceIds = ids;
	}
	
	/**
	 * @return a list of all evidence ids attached to the recommendationobject.
	 */
	public List<String> getRelatedEvidenceIds() {
		return relatedEvidenceIds;
	}
	
	/**
	 * @return a list of all attached evidences.
	 */
	public List<Evidence> getRelatedEvidence() {
		return relatedEvidence;
	}
	
	public void setToDelete() {
		toDelete = true;
	}
	
	public void setDeleted() {
		deleted = true;
	}
	
	public boolean toDelete() {
		return toDelete;
	}
	
	public boolean deleted() {
		return toDelete || deleted;
	}
	
	/**
	 * Sets the state of the recommendation to active or inactive.
	 * @param state if the recommendation is active.
	 */
	public void setState(boolean state) {
		this.state = state;
	}
	
	/**
	 * @return the current tasktype of the recommendation.
	 */
	public TaskType getTaskType() {
		return type;
	}
	
	/**
	 * Sets the title of the recommendation.
	 * @param title the new title of the recommendation.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;		
	}
	
	@Override
	public String getTimestamp() {
		return timestamp;
	}
	
	/**
	 * This method returns a recommendationobject with all information of the one calling from, except the information of the discussion (i.e. participants and minutes) and without any evidences.
	 * Furthermore the calling recommendationobjects custom id is set as the previous version. This new recommendationobject should be used as base for a newer version of an existing recommendation.
	 * @return the new recommendationobject.
	 */
	public RecommendationObject prepareAsPreviousRevision() {
		RecommendationObject result = new RecommendationObject(id, null, timestamp, publisher, title, issue, recommendedSolution, type, new ArrayList<Entry>(), true);
		result.setParticipants(new ArrayList<String>());
		result.setPreviousRevision(customId);
		result.setBenefitRating(benefitRating);
		result.setEffortRating(effortRating);
		result.setRatingDescription(ratingDescription);
		result.setTargetSpaces(targetSpaces);
		return result;
	}
	
	
	/**
	 * Parses the recommendationobject to an dataobject.
	 * @return the recommendationobject as a Dataobject.
	 */
	public native JavaScriptObject toDataObject() /*-{
		var recommString = this.@de.imc.mirror.arapp.client.RecommendationObject::toString()();
		var xmlDoc;
		if (window.DOMParser){
			var parser = new DOMParser();
		  	xmlDoc = parser.parseFromString(recommString, 'text/xml');
		} else { // Internet Explorer
			xmlDoc = new ActiveXObject('MSXML.DOMDocument');
			xmlDoc.async = false;
			xmlDoc.loadXML(recommString);
		}
		return new $wnd.SpacesSDK.DataObject(xmlDoc.childNodes[0]);
	}-*/;
	
	/**
	 * Parses the recommendationobject to a xml-string.
	 * @return The recommendationobject as a xml-string.
	 */
	public String toString() {
		return createRecommendationXMLElement().toString();
	}
	
	public Element createRecommendationXMLElement() {
		String namespace = "mirror:application:activityrecommendationapp:recommendation";
		Document doc = XMLParser.createDocument();
		Element root = Parser.createElement("recommendation", namespace);
		root.setAttribute("active", state + "");
		if (customId != null) {
			root.setAttribute("customId", customId);
		}
		root.setAttribute("publisher", publisher);
		root.setAttribute("modelVersion", "1.0");
		root.setAttribute("cdmVersion", "2.0");
		if (previousRevision != null) {
			root.setAttribute("ref", previousRevision);
		}
//		if (updatesObject != null) {
//			root.setAttribute("updates", updatesObject);
//		}
		if (type != null) {
			root.setAttribute("tasktype", type.getId());
		}
		
		if (deleted) {
			root.setAttribute("delete", "deleted");
		} else if (toDelete) {
			root.setAttribute("delete", "todelete");
		}
		
		Element creationInfo = Parser.createElement("creationInfo", namespace);
		Element personElement = Parser.createElement("person", namespace);
		personElement.appendChild(doc.createTextNode(publisher));
		
		Element dateElement = Parser.createElement("date", namespace);
		dateElement.appendChild(doc.createTextNode(HasTimestamp.TIMESTAMPFORMAT.format(new Date())));

		creationInfo.appendChild(dateElement);
		creationInfo.appendChild(personElement);
		
		Element title = Parser.createElement("title", namespace);
		title.appendChild(doc.createTextNode(this.title));
		Element issue = Parser.createElement("issue", namespace);
		issue.appendChild(doc.createTextNode(this.issue));
		Element solution = Parser.createElement("solution", namespace);
		solution.appendChild(doc.createTextNode(this.recommendedSolution));
		
		Element discussion = Parser.createElement("discussion", namespace);

		if (this.participants != null && this.participants.size() != 0) {
			Element participants = Parser.createElement("participants", namespace);
			for (String participant:this.participants) {
				Element entryElem = Parser.createElement("userId", namespace);
				entryElem.appendChild(doc.createTextNode(participant));
				participants.appendChild(entryElem);
			}
			discussion.appendChild(participants);
		}
		
		if (this.targetSpaces != null && this.targetSpaces.size() != 0) {
			Element targetSpacesElem = Parser.createElement("targetSpaces", namespace);
			for (String spaceId:this.targetSpaces) {
				Element entryElem = Parser.createElement("spaceId", namespace);
				entryElem.appendChild(doc.createTextNode(spaceId));
				targetSpacesElem.appendChild(entryElem);
			}
			discussion.appendChild(targetSpacesElem);
		}

		if (entries != null && entries.size() != 0) {
			Element minutes = Parser.createElement("minutes", namespace);
			for (Entry entry:entries) {
				Element entryElem = Parser.createElement("entry", namespace);
				entryElem.setAttribute("timestamp", entry.getTimestamp());
				entryElem.setAttribute("id", entry.getId() + "");
				entryElem.appendChild(doc.createTextNode(entry.getMessage()));
				minutes.appendChild(entryElem);
			}
			discussion.appendChild(minutes);
		}
		
		if (this.relatedEvidence.size() != 0 || this.relatedEvidenceIds.size() != 0) {
			Element relatedObjects = Parser.createElement("relatedObjects", namespace);
			for (String id:this.relatedEvidenceIds) {
				Element objectElem = Parser.createElement("object", namespace);
				objectElem.setAttribute("id", id);
				relatedObjects.appendChild(objectElem);
			}
			for (Evidence relatedObject:this.relatedEvidence) {
				Element objectElem = Parser.createElement("object", namespace);
				String cdata = relatedObject.toString();
				objectElem.appendChild(doc.createTextNode(cdata));
				objectElem.setAttribute("id", relatedObject.getId());
				relatedObjects.appendChild(objectElem);
			}
			discussion.appendChild(relatedObjects);			
		}
		Element experienceStructure = Parser.createElement("experienceStructure", namespace);
		if (this.ratingDescription != null) {
			Element rating = Parser.createElement("property", namespace);
			rating.setAttribute("id", "rating");
			rating.setAttribute("type", "other");
			rating.setAttribute("display", "Rating");
						
			rating.appendChild(doc.createTextNode(ratingDescription));
			experienceStructure.appendChild(rating);
		}
		if (this.benefitRating != null) {
			Element benefit = Parser.createElement("property", namespace);
			benefit.setAttribute("id", benefitRating.getId());
			benefit.setAttribute("type", "benefit");
			benefit.setAttribute("display", benefitRating.getDisplay());
			benefit.appendChild(doc.createTextNode(benefitRating.getDescription()));
			experienceStructure.appendChild(benefit);
		}
		if (this.effortRating != null) {
			Element effort = Parser.createElement("property", namespace);
			effort.setAttribute("id", effortRating.getId());
			effort.setAttribute("type", "effort");
			effort.setAttribute("display", effortRating.getDisplay());
			effort.appendChild(doc.createTextNode(effortRating.getDescription()));
			experienceStructure.appendChild(effort);
		}
		root.appendChild(creationInfo);
		root.appendChild(title);
		root.appendChild(issue);
		root.appendChild(solution);
		root.appendChild(discussion);
		root.appendChild(experienceStructure);
		return root;
	}
}
