package de.imc.mirror.arapp.client.localization;

import com.google.gwt.i18n.client.Messages;

public interface DiscussionEntryMessage extends Messages{
	String issueEntry(@Select String newIssue);
	String solutionEntry(@Select String newSolution);
	String titleEntry(String newTitle);
	String leaveMessage(String userJid);
	String benefitChange(String display, @Select String description);
	String effortChange(String display, @Select String description);
	String ratingChange(String ratingDescription);
	String voteEntry(String user, @Select String vote);
	String moderatorChange(String oldModerator, String newModerator, String timestamp);
	String openMessage(String moderator, String timestamp);
	String joinMessage(String userJid);
	String publishMessage(String publisher, String timestamp, @Select String lastComment);
}
