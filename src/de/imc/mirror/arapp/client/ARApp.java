package de.imc.mirror.arapp.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.localization.ErrorMessage;
import de.imc.mirror.arapp.client.localization.InfoMessage;
import de.imc.mirror.arapp.client.service.ARAppService;
import de.imc.mirror.arapp.client.service.ARAppServiceAsync;
import de.imc.mirror.arapp.client.view.CaptureTab;
import de.imc.mirror.arapp.client.view.DiscussionTab;
import de.imc.mirror.arapp.client.view.DiscussionView;
import de.imc.mirror.arapp.client.view.LoginPage;
import de.imc.mirror.arapp.client.view.ManageTab;
import de.imc.mirror.arapp.client.view.popup.AttachEvidenceDialog;
import de.imc.mirror.arapp.client.view.popup.CaptureEvidencesPopup;
import de.imc.mirror.arapp.client.view.popup.CreateEvidenceDialog;
import de.imc.mirror.arapp.client.view.popup.CreateSpacePopup;
import de.imc.mirror.arapp.client.view.popup.DeleteEntryPopup;
import de.imc.mirror.arapp.client.view.popup.InvitedPersonsPopup;
import de.imc.mirror.arapp.client.view.popup.ManageDiscussionPopup;
import de.imc.mirror.arapp.client.view.popup.OpenDiscussionPopup;
import de.imc.mirror.arapp.client.view.popup.PublishRecommendationWizard;
import de.imc.mirror.arapp.client.view.popup.UpdateRecommendationPopup;
import de.imc.mirror.arapp.client.view.popup.UpdateUserStatusPopup;
import de.imc.mirror.arapp.client.view.popup.UsageExperiencePopup;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ARApp implements EntryPoint {
	
//	private Map<String, View> views;
	
	private CaptureTab experienceTab;
	private ManageTab manageTab;
	private DiscussionTab discussionTab;
	private DiscussionView discussionView;
	
	private String discussionId;
	
	private OpenDiscussionPopup openDiscussionPopup;
	private CaptureEvidencesPopup captureEvidencesPopup;
	private PublishRecommendationWizard wizard;
	private DeleteEntryPopup deleteEntryPopup;
	private ManageDiscussionPopup manageDiscussionPopup;
	private UsageExperiencePopup usageExperiencePopup;
	private CreateEvidenceDialog createEvidencePopup;
	private AttachEvidenceDialog attachEvidenceDialog;
	private UpdateUserStatusPopup updateUserStatusPopup;
	private EvidenceVisualisation visualisation;
	private InvitedPersonsPopup invitedPersonsPopup;
	
	private Map<String, String> discussionsMap;
	private Map<String, String> spacesMap;
	private Map<String, JavaScriptObject> completeSpacesMap;
	private Map<String, RecommendationObject> recomms;
	private Map<String, Map<String, Experience>> experiences;
	private Map<String, String> refMap;
	private Map<String, Map<String, RecommendationStatus>> statusMap;
	private Map<String, String> displayNames;
	
	private HTML userInfo;
	private HTML logoutButton;
	
	private boolean previouslyLoggedIn = false;
	private UpdateRecommendationPopup updateRecommendationPopup;
	
	private Timer timer;
	private boolean timerRunning = false;
	
	private boolean isLoggedIn = false;
	private boolean isFileServiceAvailable = false;
	
	private List<Element> showChangeElements;
	
	private CreateSpacePopup createSpacePopup;

	public ErrorMessage errorMessage;
	public InfoMessage infoMessage;
	

	
	public native void log(String t) /*-{
		$wnd.console.log(t);
	}-*/;	
	
	/**
	 * EntryPoint of the App. 
	 */
	public void onModuleLoad() {
		errorMessage = GWT.create(ErrorMessage.class);
		infoMessage = GWT.create(InfoMessage.class);
		
		
		spacesMap = new HashMap<String, String>();
		experiences = new HashMap<String, Map<String, Experience>>();
		recomms = new HashMap<String, RecommendationObject>();
		refMap = new HashMap<String, String>();
		discussionsMap = new HashMap<String, String>();
		completeSpacesMap = new HashMap<String, JavaScriptObject>();
		statusMap = new HashMap<String, Map<String, RecommendationStatus>>();
		displayNames = new HashMap<String, String>();
		showChangeElements = new ArrayList<Element>();
		
		timer = new Timer() {
			
			Map<Element, Boolean> up = new HashMap<Element, Boolean>();
			int amount = 0;
			
			@Override
			public void run() {
				synchronized(showChangeElements) {
					if (showChangeElements.size() != amount) {
						amount = showChangeElements.size();
					}
					for (Element elem:showChangeElements) {
						String opacity = elem.getStyle().getOpacity();
						if (opacity == null || opacity.equals("")) {
							opacity = "1.0";
						}
						double opac = Double.parseDouble(opacity);
						if (opac <= 0.5) {
							up.put(elem, true);
						} else if (opac >= 1.0) {
							up.put(elem, false);
						}
						boolean currentUp = up.get(elem);
						if (currentUp) {
							opac = (100*opac + 1)/100.0;	
						} else {
							opac = (100*opac - 1)/100.0;
						}
						elem.getStyle().setOpacity(opac);						
					}
				}
			}
		};
		timer.scheduleRepeating(50);
		

		final ARAppServiceAsync service = GWT.create(ARAppService.class);
		
		final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(Boolean arg0) {
				isFileServiceAvailable = arg0;
			}
		};
		service.isFileServiceAvailable(callback);
		
		new LoginPage(this);
	}
	
	/**
	 * If the user logged in successfully this method initialises all necessary elements of the page and all necessary classes.
	 */
	public void loggedIn() {	
		isLoggedIn = true;
		
		final HTML captureTabButton = HTML.wrap(Document.get().getElementById("tabCapture"));
		final HTML manageTabButton = HTML.wrap(Document.get().getElementById("tabManage"));
		final HTML discussionTabButton = HTML.wrap(Document.get().getElementById("tabDiscussion"));

		final Element contentCapture = Document.get().getElementById("contentCapture");
		final Element contentManage = Document.get().getElementById("contentManage");
		final Element contentDiscussion = Document.get().getElementById("contentDiscussion");
		final Element contentDiscussionSession = Document.get().getElementById("contentDiscussionSession");

		if (Cookies.getCookie("ARAppLastTab") != null) {
			String lastTab = Cookies.getCookie("ARAppLastTab");
			captureTabButton.getElement().removeClassName("activeItem");
			manageTabButton.getElement().removeClassName("activeItem");
			synchronized(showChangeElements) {
				if (!showChangeElements.contains(discussionTabButton.getElement())) {
					discussionTabButton.getElement().removeClassName("activeItem");
				}
			}
			
			contentCapture.removeClassName("activeItem");
			contentManage.removeClassName("activeItem");
			contentDiscussion.removeClassName("activeItem");
			contentDiscussionSession.removeClassName("activeItem");

			if ("DiscussionTab".equals(lastTab)) {
				contentDiscussion.addClassName("activeItem");
				discussionTabButton.getElement().addClassName("activeItem");
				synchronized(showChangeElements) {
					removeShowChangeElement(discussionTabButton.getElement());
				}
			} else if ("ManageTab".equals(lastTab)) {
				contentManage.addClassName("activeItem");
				manageTabButton.getElement().addClassName("activeItem");
			} else {
				contentCapture.addClassName("activeItem");
				captureTabButton.getElement().addClassName("activeItem");				
			}
		}
		
		if (captureTabButton != null) {
			captureTabButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					manageTabButton.getElement().removeClassName("activeItem");
					synchronized(showChangeElements) {
						if (!showChangeElements.contains(discussionTabButton.getElement())) {
							discussionTabButton.getElement().removeClassName("activeItem");
						}
					}
					captureTabButton.getElement().addClassName("activeItem");
					
					contentCapture.addClassName("activeItem");
					contentManage.removeClassName("activeItem");
					contentDiscussionSession.removeClassName("activeItem");
					contentDiscussion.removeClassName("activeItem");
					
					Cookies.setCookie("ARAppLastTab", "CaptureTab");
				}
			});
		}
		if (manageTabButton != null) { 
			manageTabButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					captureTabButton.getElement().removeClassName("activeItem");
					synchronized(showChangeElements) {
						if (!showChangeElements.contains(discussionTabButton.getElement())) {
							discussionTabButton.getElement().removeClassName("activeItem");
						}
					}
					manageTabButton.getElement().addClassName("activeItem");
					
					contentManage.addClassName("activeItem");
					contentCapture.removeClassName("activeItem");
					contentDiscussion.removeClassName("activeItem");
					contentDiscussionSession.removeClassName("activeItem");
					
					Cookies.setCookie("ARAppLastTab", "ManageTab");
				}
			});
		}
		if (discussionTabButton != null) {
			discussionTabButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					captureTabButton.getElement().removeClassName("activeItem");
					manageTabButton.getElement().removeClassName("activeItem");
					discussionTabButton.getElement().addClassName("activeItem");
					removeShowChangeElement(discussionTabButton.getElement());
	
					if (discussionId == null) {
						contentDiscussion.addClassName("activeItem");
					} else {
						contentDiscussionSession.addClassName("activeItem");
					}
					contentCapture.removeClassName("activeItem");
					contentManage.removeClassName("activeItem");
					
					Cookies.setCookie("ARAppLastTab", "DiscussionTab");
				}
			});
		}

		
		if (previouslyLoggedIn) {
			Document.get().getElementById("login").removeClassName("activeItem");
			Document.get().getElementById("main").addClassName("activeItem");
			if (userInfo != null) {
				userInfo.getElement().setInnerHTML(infoMessage.signedInAs(getDisplayNameForJid(getBareJID())));
			}
			
			experienceTab.update();
			manageTab.update();
			discussionTab.getDiscussions();
			return;
		}

		userInfo = HTML.wrap(Document.get().getElementById("userInfo"));
		if (userInfo != null) {
			userInfo.getElement().setInnerHTML(infoMessage.signedInAs(getDisplayNameForJid(getBareJID())));
			userInfo.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					//Needed so only a click on the sign out button logs the user out
					event.stopPropagation();
				}
			});
		}

		logoutButton = HTML.wrap(Document.get().getElementById("rightHeader"));
		if (logoutButton != null) {
			logoutButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					logout();
				}
			});
		}

		experienceTab = new CaptureTab(this);
		manageTab = new ManageTab(this);
		discussionTab = new DiscussionTab(this);
		discussionView = new DiscussionView(this);
		
		openDiscussionPopup = new OpenDiscussionPopup(this);
		captureEvidencesPopup = new CaptureEvidencesPopup(this);
		usageExperiencePopup = new UsageExperiencePopup(this);
		
		wizard = new PublishRecommendationWizard(discussionView);
		deleteEntryPopup = new DeleteEntryPopup(discussionView);
		manageDiscussionPopup = new ManageDiscussionPopup(this, discussionView);
		updateUserStatusPopup = new UpdateUserStatusPopup(manageTab);
		createEvidencePopup = new CreateEvidenceDialog(this);
		attachEvidenceDialog = new AttachEvidenceDialog(this);
		updateRecommendationPopup = new UpdateRecommendationPopup(manageTab, this);
		visualisation = new EvidenceVisualisation(this);
		invitedPersonsPopup = new InvitedPersonsPopup(this);
		createSpacePopup = new CreateSpacePopup(this);
		
		previouslyLoggedIn = true;
		
		Document.get().getElementById("login").removeClassName("activeItem");
		Document.get().getElementById("main").addClassName("activeItem");
	}
	
	/**
	 * Opens the discussionview and sets the discussionId.
	 * @param id The id of the discussion.
	 */
	public void joinDiscussion(String id) {
		if (discussionView != null) {
			this.discussionId = id;
			discussionView.setDiscussionSpaceId(id);
			discussionView.initializeMuc(id);
			
			Document.get().getElementById("contentDiscussion").removeClassName("activeItem");
			Document.get().getElementById("contentDiscussionSession").addClassName("activeItem");
		}
	}
	
	/**
	 * Opens the discussionview and sets the discussionId and discussiongroup.
	 * @param id The id of the discussion.
	 * @param discussionGroup The string representing all discussiongroups.
	 */
	public void startDiscussion(String id, String discussionGroup) {
		if (discussionView != null) {
			this.discussionId = id;
			discussionView.setDiscussionGroup(discussionGroup);
			discussionView.setDiscussionSpaceId(id);
			discussionView.initializeMuc(id);
	
			Document.get().getElementById("tabManage").removeClassName("activeItem");
			Document.get().getElementById("contentManage").removeClassName("activeItem");
			Document.get().getElementById("contentDiscussion").removeClassName("activeItem");
	
			Document.get().getElementById("tabDiscussion").addClassName("activeItem");
			Document.get().getElementById("contentDiscussionSession").addClassName("activeItem");
		}
	}
	
	/**
	 * Hides the discussionview.
	 */
	public void leaveDiscussion() {
		this.discussionId = null;
		Document.get().getElementById("contentDiscussion").addClassName("activeItem");
		Document.get().getElementById("contentDiscussionSession").removeClassName("activeItem");		
	}
	
	/**
	 * Returns an instance of the popup for opening a new discussion
	 * @return an instance of the popup.
	 */
	public OpenDiscussionPopup getOpenDiscussionPopup() {
		return openDiscussionPopup;
	}
	
	public CreateSpacePopup getCreateSpacePopup() {
		return createSpacePopup;
	}

	/**
	 * Returns an instance of the popup for adding evidences to an experience.
	 * @return an instance of the popup.
	 */
	public CaptureEvidencesPopup getCaptureEvidencesPopup() {
		return captureEvidencesPopup;
	}
	
	/**
	 * Returns an instance of the popup for adding evidences to an experience.
	 * @return an instance of the popup.
	 */
	public InvitedPersonsPopup getInvitedPersonsPopup() {
		return invitedPersonsPopup;
	}

	/**
	 * Returns an instance of the publishwizard
	 * @return an instance of the publishwizard.
	 */
	public PublishRecommendationWizard getPublishWizard() {
		return wizard;
	}

	/**
	 * Returns an instance of the popup for updating an existing recommendation
	 * @return an instance of the popup.
	 */
	public UpdateRecommendationPopup getUpdateRecommendationPopup() {
		return updateRecommendationPopup;
	}
	
	public void showPublishWizard(String spaceId) {
		if (discussionView != null && discussionId != null && discussionId.equals(spaceId)) {
			discussionView.showPublishWizard();
		}
	}
	
	public void addVote(String spaceId, String xml) {
		if (discussionView != null && discussionId != null && discussionId.equals(spaceId)) {
			discussionView.addVote(xml);
		}
	}

	/**
	 * Returns an instance of the popup for updating the userstatus.
	 * @return an instance of the popup.
	 */
	public UpdateUserStatusPopup getUpdateUserStatusPopup() {
		return updateUserStatusPopup;
	}

	/**
	 * Returns an instance of the popup for deleting a minuteentry.
	 * @return an instance of the popup.
	 */
	public DeleteEntryPopup getDeleteEntryPopup() {
		return deleteEntryPopup;
	}

	/**
	 * Returns an instance of the popup for changing the moderator of a discussion.
	 * @return an instance of the popup.
	 */
	public ManageDiscussionPopup getManageDiscussionPopup() {
		return manageDiscussionPopup;
	}

	/**
	 * Returns an instance of the popup for showing the details of a usageexperience.
	 * @return an instance of the popup.
	 */
	public UsageExperiencePopup getUsageExperiencePopup() {
		return usageExperiencePopup;
	}

	/**
	 * Returns an instance of the popup for creating a new evidence.
	 * @return an instance of the popup.
	 */
	public CreateEvidenceDialog getCreateEvidencePopup() {
		return createEvidencePopup;
	}

	/**
	 * Returns an instance of the popup for attaching an evidence.
	 * @return an instance of the popup.
	 */
	public AttachEvidenceDialog getAttachEvidenceDialog() {
		return attachEvidenceDialog;
	}

	/**
	 * Returns an instance of the popup for showing the details of an evidence.
	 * @return an instance of the popup.
	 */
	public EvidenceVisualisation getEvidenceVisualisationPopup() {
		return visualisation;
	}
	
	/**
	 * Method to get the name to display for a jid.
	 * @param jid the jid to get the display name for.
	 * @return the displayname for the provided jid. <code>null</code> if no displayname was saved beforehand.
	 */
	public String getDisplayNameForJid(String jid) {
		if (jid.contains("/")) {
			jid = jid.split("/")[0];
		}
		if (displayNames.get(jid) == null || displayNames.get(jid).equals("")) {
			return jid;
		} else {
			return displayNames.get(jid);
		}
	}
	
	/**
	 * Sets the displayname for a given jid.
	 * @param jid The jid to save the displayname for.
	 * @param displayName The corresponding displayname.
	 */
	public void setDisplayNameForJid(String jid, String displayName) {
		displayNames.put(jid, displayName);
	}
	
	public List<String> getAllUserJids() {
		List<String> userJids = new ArrayList<String>();
		userJids.addAll(displayNames.keySet());
		return userJids;
	}
	
	/**
	 * Returns all cached Recommendations which are available for the user.
	 * @return a Map consisting of the ID and an RecommendationObject.
	 */
	public Map<String, RecommendationObject> getRecommendationsForUser() {
		return recomms;
	}
	
	/**
	 * Returns all cached Experiences which are available for the user.
	 * @return a Map consisting of the ID of the Recommendation and a List of Experiences.
	 */
	public Map<String, Map<String, Experience>> getExperienceVisibleForUser() {
		return experiences;
	}
	
	/**
	 * Returns all cached Experiences for a specific Recommendation.
	 * @param id The id of the Recommendation.
	 * @return a List of Experiences
	 */
	public Map<String, Experience> getExperiencesForRecommendation(String id) {
		return experiences.get(id);
	}
	
	/**
	 * Convenience method to get the BareJID of the current User.
	 * @return the BareJID of the current User.
	 */
	public native String getBareJID() /*-{
		return $wnd.connection.getCurrentUser().getBareJID();
	}-*/;

	/**
	 * Mehtod to get the Base64 encoded logininformation. This should only be used for the fileservice.
	 * @return the Base64 encoded logininformation in the form "username:password".
	 */
	public native String getLoginInfos() /*-{
		var user = $wnd.connection.getCurrentUser().getBareJID();
		var pass = $wnd.pass;
		var loginInfos = user + ":" + pass;
		return $wnd.Base64.encode(loginInfos);
	}-*/;
	
	/**
	 * Caches a new sessionobject, which contains all information about a running discussion.
	 * @param nodeId the id of the node the discussion takes place.
	 * @param sessionXML the sessionobject represented as a xml-string.
	 */
	public void addSessionObject(String nodeId, String sessionXML) {
		boolean startTimer = false;
		if (!Document.get().getElementById("tabDiscussion").getClassName().contains("activeItem")) { 
			startTimer = true;
		}
		discussionsMap.put(nodeId, sessionXML);
		if (discussionId != null && discussionView != null && isLoggedIn) {
			discussionView.update();
		}
		if (discussionTab != null && isLoggedIn) {
			discussionTab.update(nodeId, sessionXML);
		} else if (startTimer && isLoggedIn) {
			startTimer = false;
		}
		if (startTimer) {
			addShowChangeElement(Document.get().getElementById("tabDiscussion"));
			Document.get().getElementById("tabDiscussion").addClassName("activeItem");
		}
	}
	
	public void addShowChangeElement(Element elem) {
		if (!showChangeElements.contains(elem)) {
			synchronized(showChangeElements) {
				showChangeElements.add(elem);
				elem.getStyle().setOpacity(0.5);
			}
		}
	}
	
	public boolean removeShowChangeElement(Element elem) {
		if (showChangeElements.contains(elem)) {
			synchronized (showChangeElements) {
				elem.getStyle().clearOpacity();
				return showChangeElements.remove(elem);
			}
		}
		return false;
	}
	
	public void removeNode(String nodeId) {
		if (discussionsMap.containsKey(nodeId)) {
			removeSession(nodeId);
		}
		String id = nodeId.replaceAll("[a-zA-Z0-9]+#((team|private|orga)#[0-9]+)", "$1");
		if (nodeId.matches("[a-zA-Z0-9]+#(team|private|orga)#[0-9]+") && 
				spacesMap.containsKey(id)) {
			removeSpace(id);			
		}
	}
	
	/**
	 * Deletes a cached session.
	 * @param nodeId the id of the node the discussion took place on.
	 */
	public void removeSession(String nodeId) {
		discussionsMap.remove(nodeId);
		if (discussionTab != null) {
			discussionTab.update(nodeId, null);
		}
		if (discussionId != null && discussionId.equals(nodeId) && discussionView != null) {
			if (!discussionView.isOneTargetSpaceAvailable()) {
				discussionView.publishRecommendationOnPrivateSpace();
			}
			wizard.hidePopup();
			Window.alert("The discussion was closed.");
			leaveDiscussion();
		}
	}

	/**
	 * Returns all cached discussions which are available for the user.
	 * @return a Map consisting of the nodeId the discussion takes place on and a sessionObject which represents the discussion.
	 */
	public Map<String, String> getDiscussions() {
		return discussionsMap;
	}

	/**
	 * Returns all cached Recommendationstati which are available for the user.
	 * @return a Map consisting of the jid of a user and a Map consisting of the custom id of a recommendation an the corresponding recommendationstatus.
	 */
	public Map<String, Map<String,RecommendationStatus>> getRecommendationStati() {
		return statusMap;
	}
	
	/**
	 * Caches an Experience.
	 * @param ref The custom id of the recommendation this experience is for.
	 * @param experience The Experience to cache.
	 */
	public void addExperience(String ref, Experience experience) {
		Map<String, Experience> experienceList;
    	if (experiences.get(ref) == null) {
    		experienceList = new HashMap<String, Experience>();
    		experienceList.put(experience.getCustomId(), experience);
    	} else {
    		experienceList = experiences.get(ref);
    		if (experienceList.get(experience.getCustomId()) != null) {
    			Experience savedExp = experienceList.get(experience.getCustomId());
    			if (savedExp.getTimestamp().compareTo(experience.getTimestamp()) < 0) {
    				experienceList.put(experience.getCustomId(), experience);    				
    			}
    		} else {
    			experienceList.put(experience.getCustomId(), experience);
    		}
    	}
		experiences.put(ref, experienceList);

		if (experienceTab != null && isLoggedIn) {
			experienceTab.update();
		}
		if (manageTab != null && isLoggedIn) {
			manageTab.update();
		}
	}

	/**
	 * Caches an Recommendationstatus.
	 * @param status The recommendationstatus to cache.
	 */
	public void addRecommendationStatus(RecommendationStatus status) {
		Map<String, RecommendationStatus> stati;
		if (statusMap.get(status.getUser()) != null) {
			stati = statusMap.get(status.getUser());
			if (stati.get(status.getRef()) != null) {
				RecommendationStatus savedStatus = stati.get(status.getRef());
				if (savedStatus.getTimestamp().compareTo(status.getTimestamp()) <= 0) {
					stati.put(status.getRef(), status);
				}
			} else {
				stati.put(status.getRef(), status);
			}
		} else {
			stati = new HashMap<String, RecommendationStatus>();
			stati.put(status.getRef(), status);
		}
		statusMap.put(status.getUser(), stati);
		if (experienceTab != null && isLoggedIn) {
			experienceTab.updateRecommendationStatus(status);
		}
		if (manageTab != null && isLoggedIn) {
			manageTab.update();
		}
	}
	
	/**
	 * Caches a Recommendation.
	 * @param id The custom id of the recommendation.
	 * @param obj The recommendation.
	 */
	public void addRecommendation(String id, RecommendationObject obj) {
		if (obj.deleted()) {
			recomms.remove(id);
		} else {
			if (recomms.get(id) != null) {
				String timestamp = recomms.get(id).getTimestamp();
				String timestamp2 = obj.getTimestamp();
				Date date1 = HasTimestamp.TIMESTAMPFORMAT.parseStrict(timestamp);
				Date date2 = HasTimestamp.TIMESTAMPFORMAT.parseStrict(timestamp2);
				if (date1.before(date2)) {
					recomms.put(id, obj);
				}
			} else {
				recomms.put(id, obj);
			}
		}
		if (obj.hasPreviousRevisions()) {
			refMap.put(id, obj.getPreviousRevisions());
		}
		if (experienceTab != null && isLoggedIn) {
			experienceTab.update();
		}
		if (manageTab != null && isLoggedIn) {
			manageTab.update();
		}
		if (persistenceServiceAvailable()) {
			if (obj.toDelete()) {
				if (obj.getPublisher().equals(getBareJID())) {
					manageTab.setDeleteStatus(obj);
				}
			} else if (obj.deleted()) { 
				List<String> targetSpaces = obj.getTargetSpaces();
				if (targetSpaces != null && !targetSpaces.isEmpty()) {
					List<String> spaces = new ArrayList<String>();
					for (String space:targetSpaces) {
						if (isModeratorOfSpace(space)) {
							spaces.add(space);
						}
					}
					if (!spaces.isEmpty()) {
						requestAllDataToDelete(spaces, obj.getCustomId());
					}
				}
			}
		}
	}
	
	public boolean isFileServiceAvailable() {
		return isFileServiceAvailable;
	}

	public native boolean persistenceServiceAvailable() /*-{
		return $wnd.connection.getNetworkInformation().getPersistenceServiceJID() != null;
	}-*/;
	
	private native void requestAllDataToDelete(List<String> spaces, String customId) /*-{
		var that = this;
		var size = spaces.@java.util.List::size()();
		var spaceIds = [];
		for (var i=0; i<size; i++) {
			spaceIds[i] = spaces.@java.util.List::get(I)(i);
		}
		
		var modelFilter1 = new $wnd.SpacesSDK.filter.DataModelFilter('mirror:application:activityrecommendationapp:recommendation');
		var modelFilter2 = new $wnd.SpacesSDK.filter.DataModelFilter('mirror:application:activityrecommendationapp:experience');
		var modelFilter3 = new $wnd.SpacesSDK.filter.DataModelFilter('mirror:application:activityrecommendationapp:recommendationstatus');
		var orFilter = new $wnd.SpacesSDK.filter.OrFilter();
		
		orFilter.addFilter(modelFilter1);
		orFilter.addFilter(modelFilter2);
		orFilter.addFilter(modelFilter3);
		
		$wnd.dataHandler.setDataObjectFilter(orFilter);
			
		$wnd.dataHandler.queryDataObjectsBySpaces(spaceIds, [], function(result) {
			$wnd.dataHandler.setDataObjectFilter(null);
			var idsToDelete = [];
			var elems = @java.util.ArrayList::new()();
			for (var i=0; i<result.length; i++) {
				var elem = result[i].getElement();
								
				if (elem.tagName === "recommendation") {
					if (elem.getAttribute("customId") === customId) {
						idsToDelete.push(elem.getAttribute("id"));
					}
				} else {
					if (elem.getAttribute("ref") === customId) {
						idsToDelete.push(elem.getAttribute("id"));
						if (elem.tagName === "experience") {
							elems.@java.util.List::add(Ljava/lang/Object;)(result[i].toString());
						}
					}
				}
			}
			if (elems.@java.util.List::size()() > 0) {
				that.@de.imc.mirror.arapp.client.ARApp::deleteFilesOnFileService(Ljava/util/List;)(elems);
			}
			$wnd.dataHandler.deleteDataObjects(idsToDelete, function(success){}, function(error){});
		}, function(error) {
		
		});
	}-*/;
	
	private void deleteFilesOnFileService(List<String> list) {
		Map<String, String> fileEvs = new HashMap<String, String>();
		for (String elem:list) {
			List<Experience> exp = Parser.parseExperiences(Parser.parseXMLStringToElement(elem));
			if (exp != null && exp.size() > 0) {
				if (exp.get(0).getEvidences().size() > 0) {
					List<Evidence> evs = exp.get(0).getEvidences();
					for (Evidence ev:evs) {
						if (ev instanceof FileEvidence) {
							fileEvs.put(((FileEvidence)ev).getFileNameWithPrefix(), ((FileEvidence)ev).getLocation());
						}
					}
				}
			}
		}

		final ARAppServiceAsync service = GWT.create(ARAppService.class);
		
		final AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(Void arg0) {
			}
		};
		service.deleteFilesOnFileService(fileEvs, getLoginInfos(), callback);
	}
	
	private native boolean isModeratorOfSpace(String spaceId) /*-{
		var space = this.@de.imc.mirror.arapp.client.ARApp::completeSpacesMap.@java.util.Map::get(Ljava/lang/Object;)(spaceId);
		if (space && space != null) {
			var members = space.getMembers();
			for (var i in members) {
				if (members[i].getJID() === $wnd.connection.getCurrentUser().getBareJID() && members[i].getRole() === $wnd.SpacesSDK.Role.MODERATOR) {
					return true;
				}
			}
		}
		return false;
	}-*/;
	
	/**
	 * @return all cached spaces in a map consisting of the id and the name of the space.
	 */
	public Map<String, String> getSpacesMap() {
		return spacesMap;
	}
	
	public Map<String, JavaScriptObject> getCompleteSpacesMap() {
		return completeSpacesMap;
	}
	
	/**
	 * Caches the id an the name of a space.
	 * @param id The id of the space.
	 * @param name The name of the space.
	 */
	public void addSpace(String id, String name, JavaScriptObject space) {
		spacesMap.put(id, name);
		completeSpacesMap.put(id, space);
		if (openDiscussionPopup != null) {
			openDiscussionPopup.updateSpacesList();
		}
		if (discussionView != null) {
			discussionView.updateSpacesList();
		}
	}
	
	public void removeSpace(String id) {
		spacesMap.remove(id);
		completeSpacesMap.remove(id);
		if (openDiscussionPopup != null) {
			openDiscussionPopup.updateSpacesList();
		}
		if (discussionView != null) {
			discussionView.updateSpacesList();
		}
	}
	
	/**
	 * Returns a map consisting of two recommendation ids. The values in this map are the newer recommendations whereas the respective key is the older revision.
	 * @return a Map.
	 */
	public Map<String, String> getRefMap() {
		return refMap;
	}
	
	/**
	 * @return if the user is logged in.
	 */
	public native boolean isLoggedIn() /*-{
		return $wnd.connection != null;
	}-*/;
	
	/**
	 * Logs the user out and shows the loginscreen.
	 * @param instance An instance of this class.
	 */
	public native void logout() /*-{
		if ($wnd.connection && $wnd.connection != null) {
			if ($wnd.loginListener && $wnd.loginListener != null) {
				$wnd.connection.removeConnectionStatusListener($wnd.loginListener);
			}
			if ($wnd.statusListener && $wnd.statusListener != null) {
				$wnd.connection.removeConnectionStatusListener($wnd.statusListener);
			}
			$wnd.connection.disconnect();
		}
		$wnd.document.cookie = "ARAppLogin=; expires=Thu, 01-Jan-1970 00:00:01 GMT;";
		$wnd.dataHandler = null;
		$wnd.spaceHandler = null;
		$wnd.connection = null;
		this.@de.imc.mirror.arapp.client.ARApp::resetData()();
	}-*/;
	
	/**
	 * Clears all cached data.
	 */
	public void resetData() {		
		spacesMap = new HashMap<String, String>();
		experiences = new HashMap<String, Map<String, Experience>>();
		recomms = new HashMap<String, RecommendationObject>();
		refMap = new HashMap<String, String>();
		discussionsMap = new HashMap<String, String>();
		completeSpacesMap = new HashMap<String, JavaScriptObject>();
		statusMap = new HashMap<String, Map<String, RecommendationStatus>>();
		displayNames = new HashMap<String, String>();
		discussionId = null;
		
		manageTab.reset();
		experienceTab.reset();
		discussionTab.reset();
		
		isLoggedIn = false;
		
		showLoginPage();
	}
	
	/**
	 * Shows the login mask
	 */
	public void showLoginPage() {
		Document.get().getElementById("login").addClassName("activeItem");
		Document.get().getElementById("main").removeClassName("activeItem");
	}
	
	/**
	 * Method to get a random id in the format of an uuid.
	 * @return a random id.
	 */
	public static String uuid() {
		final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
		char[] uuid = new char[36];
		int r;
		// rfc4122 requires these characters
		uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
		uuid[14] = '4';
		// Fill in random data.  At i==19 set the high bits of clock sequence as
		// per rfc4122, sec. 4.1.5
		for (int i = 0; i < 36; i++) {
			if (uuid[i] == 0) {
				r = (int) (Math.random()*16);
				uuid[i] = CHARS[(i == 19) ? (r & 0x3) | 0x8 : r & 0xf];
			}
		}
		return new String(uuid);
	}
}
