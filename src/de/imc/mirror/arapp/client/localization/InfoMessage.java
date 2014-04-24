package de.imc.mirror.arapp.client.localization;

import java.util.List;

import com.google.gwt.i18n.client.Messages;

import de.imc.mirror.arapp.client.RecommendationStatus;

public interface InfoMessage extends Messages {
	String captureAttachedEvidences(@PluralCount int amount);
	String manageAttachedEvidences(@PluralCount int amount);
	String noFileAttached();
	String captureRecommendationSolvedInfo(@PluralCount int usersSolved, int usersTotal);
	String captureNoExperiencesCaptured();
	String discussionAttachedEvidences(@PluralCount int amount);
	String listBoxDoNotMeasureEntry();
	String listBoxAllEntry();
	String recommendationStatus(@Select RecommendationStatus.Status status);
	String progressStatisticsInfo(@PluralCount int solvedAmount, @PluralCount int ignoredAmount, int personsAmount);
	String currentRevision(@PluralCount int version);
	String averageRating(double rating);
	String noExperiencesYet();
	String experiencesAmount(int experiencesAmount, @PluralCount int commentAmount);
	String averageProperty(@PluralCount @Optional int amount, int average, int total);
	String notAvailable();
	String itemsSelected(@PluralCount int amount);
	String available();
	String offline();
	
	String userList(@PluralCount List<String> users);
	String recommendationUpdated();
	String signedInAs(String user);
	
	String getSolvedButtonCaption(@Select RecommendationStatus.Status status);
	String getIgnoreButtonCaption(@Select RecommendationStatus.Status status);
	
	String recommendationProgress(@Select RecommendationStatus.Status status);
}
