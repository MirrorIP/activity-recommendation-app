package de.imc.mirror.arapp.client;

import java.util.ArrayList;
import java.util.List;

import de.imc.mirror.arapp.client.RecommendationObject.TaskType;

public class RecommendationObjectBuilder {
	
	private String id;
	private String customId;
	private String issue;
	private String recommendedSolution;
	private String date;
	private String title;
	private String publisher;
	private String ref;
//	private String updates;
	private String ratingDescription;
	private TaskType taskType;
	private Benefit benefitRating;
	private Effort effortRating;
	private List<Entry> entries;
	private List<Evidence> relatedEvidence;
	private List<String> relatedEvidenceIds;
	private List<String> participants;
	private List<String> targetSpaces;
	private boolean state;
	private boolean toDelete;
	private boolean deleted;
	
	public RecommendationObjectBuilder(String title, String issue, String solution, TaskType type) {
		this.title = title;
		this.issue = issue;
		this.recommendedSolution = solution;
		this.taskType = type;
	}
	
	/**
	 * Sets the benefitrating.
	 * @param rating the benefitrating to be used.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setBenefitRating(Benefit rating) {
		this.benefitRating = rating;
		return this;
	}

	/**
	 * Sets the effortrating.
	 * @param rating the effortrating to be used.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setEffortRating(Effort rating) {
		this.effortRating = rating;
		return this;
	}

	/**
	 * Sets the id.
	 * @param id the id to be used.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Sets the state of the recommendation.
	 * @param state true if the recommendation is active, false if it is inactive
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setState(boolean state) {
		this.state = state;
		return this;
	}

	/**
	 * Sets the recommendation as a newer version of a already existing one.
	 * @param ref the custom id of the recommendation this one will update.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setRef(String ref) {
		this.ref = ref;
		return this;
	}

	/**
	 * Sets the customId.
	 * @param customId the customId to be used.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setCustomId(String customId) {
		this.customId = customId;
		return this;
	}
	
	/**
	 * Sets the description for how this recommendation should be rated.
	 * @param description the description to be used.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setRatingDescription(String description) {
		this.ratingDescription = description;
		return this;
	}
	
	/**
	 * Sets the publisher of this recommendation.
	 * @param publisher the jid of the one who publishes this recommendation.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setPublisher(String publisher) {
		this.publisher = publisher;
		return this;
	}

	/**
	 * Sets the creationdate of the recommendation.
	 * @param dateString the date when the recommendation was created. In ISO8601 format.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder setCreationDate(String dateString) {
		this.date = dateString;
		return this;
	}

	/**
	 * Adds a targetspace, where the recommendation will be/was published on.
	 * @param spaceId the id of a space.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder addTargetSpace(String spaceId) {
		if (targetSpaces == null) {
			targetSpaces = new ArrayList<String>();
		}
		targetSpaces.add(spaceId);
		return this;
	}

	/**
	 * Adds an entry to the minutes.
	 * @param entry the entry to add.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder addEntry(Entry entry) {
		if (entries == null) {
			entries = new ArrayList<Entry>();
		}
		entries.add(entry);
		return this;
	}

	/**
	 * Adds a participant.
	 * @param userId the participant to add.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder addParticipant(String userId) {
		if (this.participants == null) {
			this.participants = new ArrayList<String>();
		}
		this.participants.add(userId);
		return this;
	}

	
	public RecommendationObjectBuilder setDeleted() {
		this.deleted = true;
		return this;
	}
	
	public RecommendationObjectBuilder setToDelete() {
		this.toDelete = true;
		return this;
	}
	/**
	 * Sets this as a new version.
	 * @param updates the id of the recommendation this one updates or will update.
	 * @return the recommendationobjectbuilder.
	 */
//	public RecommendationObjectBuilder setUpdates(String updates) {
//		this.updates = updates;
//		return this;
//	}

	/**
	 * Adds a evidence to the recommendation.
	 * @param evidence the evidence to add.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder addRelatedEvidence(Evidence evidence) {
		if (relatedEvidence == null) {
			relatedEvidence = new ArrayList<Evidence>();
		}
		relatedEvidence.add(evidence);
		return this;
	}

	/**
	 * Adds a evidenceid to the recommendation.
	 * @param evidenceId the id of the evidence to add.
	 * @return the recommendationobjectbuilder.
	 */
	public RecommendationObjectBuilder addRelatedEvidenceId(String evidenceId) {
		if (relatedEvidenceIds == null) {
			relatedEvidenceIds = new ArrayList<String>();
		}
		relatedEvidenceIds.add(evidenceId);
		return this;
	}

	/**
	 * Builds the RecommendationObject.
	 * @return the newly created RecommendationObject.
	 */
	public RecommendationObject build() {
		RecommendationObject recomm = new RecommendationObject(id, customId, date, publisher, title, issue, recommendedSolution, taskType, entries, state);
		if (ref != null) {
			recomm.setPreviousRevision(ref);
		}
		if (effortRating != null) {
			recomm.setEffortRating(effortRating);
		}
		if (benefitRating != null) {
			recomm.setBenefitRating(benefitRating);
		}
		if (relatedEvidence != null) {
			recomm.setRelatedEvidence(relatedEvidence);
		}
		if (relatedEvidenceIds != null) {
			recomm.setRelatedEvidenceIds(relatedEvidenceIds);
		}
		if (participants != null) {
			recomm.setParticipants(participants);
		}
		if (targetSpaces != null) {
			recomm.setTargetSpaces(targetSpaces);
		}
		if (toDelete) {
			recomm.setToDelete();
		}
		if (deleted) {
			recomm.setDeleted();
		}
//		if (updates != null) {
//			recomm.setUpdatedObject(updates);
//		}
		if (ratingDescription != null) {
			recomm.setRatingDescription(ratingDescription);
		}
		return recomm;
	}
}
