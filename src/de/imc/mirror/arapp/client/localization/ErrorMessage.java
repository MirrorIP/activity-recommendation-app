package de.imc.mirror.arapp.client.localization;

import java.util.List;

import com.google.gwt.i18n.client.Messages;

public interface ErrorMessage extends Messages{
	String loginNoCredentialsProvided();
	String loginAuthenticationFailed();
	String connectionClosed();
	
	String emptyTitle();
	String emptyIssue();
	String emptySolution();
	String emptyRatingDescription();
	
	String unsavedChanges(List<String> errors);
	String emptyBenefit(@Select boolean measurementUnit, @Select boolean description);
	String emptyEffort(@Select boolean measurementUnit, @Select boolean description);
	String noTargetSpacesChosen();
	String noTargetSpacesInfoAvailable();
	String fileAlreadyExists();
	String fileSubmitError();
	String addDescription();
	
	String emptySpaceName();
	String wrongUserIdForm();
	
	String noDiscussionGroupChosen();
	String titleAlreadyUsed();
}
