package de.imc.mirror.arapp.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.DiscussionPanel;
import de.imc.mirror.arapp.client.Entry;
import de.imc.mirror.arapp.client.HasDetailPanel;
import de.imc.mirror.arapp.client.HasTimestamp;
import de.imc.mirror.arapp.client.Parser;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.SessionObject;
import de.imc.mirror.arapp.client.service.ARAppService;
import de.imc.mirror.arapp.client.service.ARAppServiceAsync;

public class DiscussionTab extends View implements HasDetailPanel{

	protected enum ElementIds {
		CREATEDLABEL("discussionSummaryCreated"),
		CREATENEWBUTTON("discussionCreateNewButton"),
		CURRENTPARTICIPANTS("discussionCurrentParticipants"),
		DISCUSSIONGROUPS("discussionDiscussionGroups"),		
		ISSUE("discussionIssue"),
		JOINBUTTON("discussionJoinButton"),
		MINUTESLIST("discussionMinutes"),
		MODERATORLABEL("discussionSummaryModerator"),
		MODERATORLISTBOX("discussionModeratorSelect"),
		SEARCHBOX("discussionSearchInput"),
		SEARCHBUTTON("discussionSearchInputButton"),		
		TITLELABEL("discussionRecommendationTitle"),
		NOITEMSELECTEDPANEL("discussionNoItemSelectedInfoPanel"),
		NOITEMSPANEL("discussionNoItemsAvailableInfoPanel"),
		DISCUSSIONSPANEL("discussionDetailPanel");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Map<String, Long> startingTimes;
	private Map<String, String> discussions;
	private Map<String, DiscussionPanel> discussionMap;
	
	private List<String> discussionIds;
	private String discussionId;

	private Button createNewButton;
	private Button joinButton;
	private Button searchButton;
	
	private TextBox searchBox;
	
	private Element discussionsList;
	private Element minutesList;
	
	private Element issue;
	private Element discussionGroups;
	private Element currentParticipants;
	private Element moderator;
	private Element createdLabel;
	private Element title;
	
	private ListBox moderatorListBox;
	
	private String textToSearchFor = "";
	
	private Element noItemSelectedPanel;
	private Element noItemsPanel;
	private Element discussionsPanel;

	public DiscussionTab(ARApp instance) {
		super(instance);
		discussionIds = new ArrayList<String>();

		discussions = new HashMap<String, String>();
		discussionMap = new HashMap<String, DiscussionPanel>();
		build();
		getDiscussions();
	}
	
	protected void build() {
		StringBuilder errorBuilder = new StringBuilder("The HTML of this site was malformed. Please contact the administrator and send him the following infos:");
		
		boolean errors = false;
		ElementIds[] ids = ElementIds.values();
		for (int i=0; i<ids.length; i++) {
			ElementIds id = ids[i];
			Element elem = Document.get().getElementById(id.getId());
			if (elem == null) {
				errorBuilder.append("\n").append(id.getId());
				errors = true;
			} else {
				switch (id) {			
				
				case CREATENEWBUTTON:
					createNewButton = Button.wrap(elem);
					createNewButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getOpenDiscussionPopup().showPopup();
						}
					});
					break;
				case MINUTESLIST:
					minutesList = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case CREATEDLABEL:
					createdLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case ISSUE:
					issue = elem.getElementsByTagName("div").getItem(0);
					break;
				case MODERATORLABEL:
					moderator = elem.getElementsByTagName("div").getItem(0);
					break;
				case TITLELABEL:
					title = elem;
					break;
				case CURRENTPARTICIPANTS:
					currentParticipants = elem.getElementsByTagName("div").getItem(0);
					break;
				case DISCUSSIONGROUPS:
					discussionGroups = elem.getElementsByTagName("div").getItem(0);
					break;
				case MODERATORLISTBOX:
					moderatorListBox = ListBox.wrap(elem);					
					moderatorListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							filter();
						}
					});	
					break;
				case SEARCHBOX:
					searchBox = TextBox.wrap(elem);
					searchBox.addKeyDownHandler(new KeyDownHandler() {
						
						@Override
						public void onKeyDown(KeyDownEvent event) {
							if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
								textToSearchFor = searchBox.getText();
								filter();
							}
						}
					});
					break;
				case SEARCHBUTTON:
					searchButton = Button.wrap(elem);
					searchButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (searchBox != null) {
								textToSearchFor = searchBox.getText();
								filter();
							}
						}
					});
					break;
				case JOINBUTTON:
					joinButton = Button.wrap(elem);
					joinButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.joinDiscussion(discussionId);
							
						}
					});
					break;
				case NOITEMSELECTEDPANEL:
					noItemSelectedPanel = elem;
					break;
				case NOITEMSPANEL:
					noItemsPanel = elem;
					break;
				case DISCUSSIONSPANEL:
					discussionsPanel = elem;
					break;
				}
			}
		}	
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}	
		
		Element contentElem = Document.get().getElementById("contentDiscussion");
		NodeList<Element> elems = contentElem.getElementsByTagName("ul");
		if (elems != null) {
			for (int i=0; i<elems.getLength(); i++) {
				Element elem = elems.getItem(i);
				if ("recommendationList".equalsIgnoreCase(elem.getClassName())) {
					elem.removeAllChildren();
					discussionsList = elem;
					break;
				}
			}
		}
	}
	
	public void showDetails(String id) {
		if (instance.removeShowChangeElement((Element)discussionMap.get(id).getPanel().getLastChild())) {
			discussionMap.get(id).unsetArrowActiveBackground();
		}
		discussionId = id;
		com.google.gwt.xml.client.Element discElem = Parser.parseXMLStringToElement(discussions.get(id));
		if (discElem != null) {
			List<RecommendationObject> objs = Parser.parseRecommendations(discElem);
			if (objs == null || objs.size() == 0) return;
			final RecommendationObject obj = objs.get(0);
			final Map<String, String> infos = SessionObject.getAllInformation(discussions.get(id));
			
			infos.put("startingTime", HasTimestamp.TIMESTAMPFORMAT.format(new Date(startingTimes.get(id))));
			
			showDiscussionDetails(obj, infos);
		}
	}
	
	/**
	 * Shows all relevant information about the chosen discussion.
	 * @param obj the current state of the discussion in the form of a recommendationobject.
	 * @param infos additional information which are sent in a sessionobject but aren't saved in a recommendation.
	 */
	private void showDiscussionDetails(RecommendationObject obj, Map<String, String> infos) {
		noItemsPanel.removeClassName("activeItem");
		discussionsPanel.addClassName("activeItem");
		noItemSelectedPanel.removeClassName("activeItem");
		
		title.setInnerHTML(obj.getTitle());
		if (obj.getIssue() != null && !"".equals(obj.getIssue())) {
			issue.setInnerHTML(obj.getIssue());
		} else {
			issue.setInnerHTML(instance.infoMessage.notAvailable());
		}
		String moderatorString = infos.get("moderator");
		if (moderatorString == null) {
			moderator.setInnerHTML(instance.infoMessage.notAvailable());			
		} else if (instance.getDisplayNameForJid(moderatorString) == null) {
			moderator.setInnerHTML(moderatorString);
		} else {
			moderator.setInnerHTML(instance.getDisplayNameForJid(moderatorString));
		}
		if (infos.get("discussionGroup") == null) {
			discussionGroups.setInnerHTML(instance.infoMessage.notAvailable());			
		} else {
			discussionGroups.setInnerHTML(infos.get("discussionGroup"));
		}
		List<String> participants = obj.getParticipants();
		if (participants != null && participants.size() > 0) {
			StringBuilder builder;
			if (instance.getDisplayNameForJid(participants.get(0)) == null) {
				builder = new StringBuilder(participants.get(0));
			} else {
				builder = new StringBuilder(instance.getDisplayNameForJid(participants.get(0)));
			}
			for (int i=1; i<participants.size(); i++) {
				String name = instance.getDisplayNameForJid(participants.get(i));
				if (name == null) {
					builder.append(", ").append(participants.get(i));
				} else {
					builder.append(", ").append(name);
				}
			}
			currentParticipants.setInnerHTML(builder.toString());
		} else {
			currentParticipants.setInnerHTML("-");
		}
		

		minutesList.removeAllChildren();
		
		List<Entry> minutes = obj.getEntries();
		if (minutes != null) {
			for (Entry entry:minutes) {
				Element tr = Document.get().createTRElement();
				
				Element dateTD = Document.get().createTDElement();
				dateTD.setInnerHTML(entry.getFormattedTimestamp(HasTimestamp.MEDIUMDATE));
				
				Element textTD = Document.get().createTDElement();
				textTD.setInnerHTML(entry.getMessage());
				
				tr.appendChild(dateTD);
				tr.appendChild(textTD);
				minutesList.insertFirst(tr);
			}
		}
		
		String timestamp = infos.get("startingTime");
		if (timestamp != null) {
			try {
				Date timestampDate = HasTimestamp.TIMESTAMPFORMAT.parseStrict(timestamp);
				
				createdLabel.setInnerText(HasTimestamp.LONGDATE.format(timestampDate));
			} catch (Exception e) {
				createdLabel.setInnerText(timestamp);
			}
		} else {
			createdLabel.setInnerText(instance.infoMessage.notAvailable());
		}
		
	}
	
	/**
	 * Creates and adds a listelement and adds it to the list of currently available discussions.
	 * @param sessionId the id of the discussion.
	 */
	private void addDiscussion(final String sessionId) {
		String session = discussions.get(sessionId);
		if (session == null) return;
		com.google.gwt.xml.client.Element sessionElem = Parser.parseXMLStringToElement(session);
		if (sessionElem == null) {
			return;
		}
		List<RecommendationObject> objs = Parser.parseRecommendations(sessionElem);
		if (objs == null || objs.size() == 0) return;
		final RecommendationObject obj = objs.get(0);
		final Map<String, String> infos = SessionObject.getAllInformation(session);
		
		if (startingTimes.get(sessionId) == null) {
			infos.put("startingTime", HasTimestamp.TIMESTAMPFORMAT.format(new Date()));
		} else {
			infos.put("startingTime", HasTimestamp.TIMESTAMPFORMAT.format(new Date(startingTimes.get(sessionId))));
		}

		if (discussionMap.get(sessionId) != null) {
			discussionMap.get(sessionId).updateTitle(obj.getTitle());
			
		} else {
			DiscussionPanel panel = new DiscussionPanel(obj.getTitle(), sessionId, this);

			discussionsList.appendChild(panel.getPanel());
			discussionMap.put(sessionId, panel);
		}
		
		if (!discussionIds.contains(sessionId)) {
			discussionIds.add(sessionId);
		}
	}
	
	/**
	 * Updates the items in the listbox for filtering the discussions list after specific moderators.
	 */
	private void updateModerators() {
		List<String> moderators = new ArrayList<String>();
		for (String xml:discussions.values()) {
			final Map<String, String> infos = SessionObject.getAllInformation(xml);
			String moderator = infos.get("moderator");
			if (moderator != null && instance.getDisplayNameForJid(moderator) != null && 
					!moderators.contains(instance.getDisplayNameForJid(moderator))) {
				moderators.add(instance.getDisplayNameForJid(moderator));
			}
		}
		Collections.sort(moderators, COMPARESTRINGIGNORECASE);
		moderatorListBox.clear();
		moderatorListBox.addItem("All");
		for (String mod:moderators) {
			moderatorListBox.addItem(mod);
		}
	}
	
	public void getDiscussions() {		
		final Map<String, String> discs = instance.getDiscussions();
		List<String> ids = new ArrayList<String>();
		ids.addAll(discs.keySet());
		

		final ARAppServiceAsync service = GWT.create(ARAppService.class);
		
		
		service.startingTimes(ids, new AsyncCallback<Map<String, Long>>() {

			@Override
			public void onFailure(Throwable arg0) {
				startingTimes = new HashMap<String, Long>();
				discussions = instance.getDiscussions();
				for (String id: discussions.keySet()) {
					com.google.gwt.xml.client.Element discElem = Parser.parseXMLStringToElement(discs.get(id));
					if (discElem == null) continue;
					List<RecommendationObject> objs = Parser.parseRecommendations(discElem);
					if (objs == null || objs.size() == 0) continue;
					final RecommendationObject obj = objs.get(0);
					long timestamp;
					if (obj.getEntries() != null && obj.getEntries().size() > 0) {
						try {
							timestamp = HasTimestamp.TIMESTAMPFORMAT.parseStrict(obj.getEntries().get(0).getTimestamp()).getTime();	
						} catch (Exception e) {
							timestamp = new Date().getTime();
						}
					} else {
						timestamp = new Date().getTime();
					}
					startingTimes.put(id, timestamp);
					addDiscussion(id);
				}
				updateModerators();
				sort();
			}

			@Override
			public void onSuccess(final Map<String, Long> result) {
				for (String id: discs.keySet()) {
					com.google.gwt.xml.client.Element discElem = Parser.parseXMLStringToElement(discs.get(id));
					if (discElem == null) continue;
					List<RecommendationObject> objs = Parser.parseRecommendations(discElem);
					if (objs == null || objs.size() == 0) continue;
					final RecommendationObject obj = objs.get(0);
										
					if (obj.getEntries() != null && obj.getEntries().size() > 0) {
						String timestamp = obj.getEntries().get(0).getTimestamp();
						
						Date timestampDate = HasTimestamp.TIMESTAMPFORMAT.parseStrict(timestamp);	
						if (!result.containsKey(id)) {
							result.put(id, timestampDate.getTime());
							continue;
						}
						Date serverDate = new Date(result.get(id));
						if (serverDate.after(timestampDate)) {
							result.put(id, timestampDate.getTime());
						}
					}
				}
				
				Comparator<String> comp = new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						long timestamp1 = result.get(o1);
						long timestamp2 = result.get(o2);
						return (int)(timestamp2-timestamp1);
					}
				};
				List<String> ids = new ArrayList<String>();
				ids.addAll(result.keySet());
				
				Collections.sort(ids, comp);
				startingTimes = new HashMap<String, Long>();
				discussions = new HashMap<String, String>();
				for (String id: ids) {
					startingTimes.put(id, result.get(id));
					discussions.put(id, discs.get(id));
					addDiscussion(id);
				}
				updateModerators();
				sort();
			}
		});
	}
	
	public void reset() {
		discussionsList.removeAllChildren();
		discussions.clear();
		discussionMap.clear();
		startingTimes.clear();
		discussionId = null;
	}
	
	/**
	 * If a new discussion is available it will be added to the already existing ones.
	 * If a discussion was closed it will be removed from the list of available discussions.
	 * If a already available discussion has new information, the state of this discussion will be updated.
	 * @param spaceId the id of the node the discussion took place on.
	 * @param sessionXML the newest published information on the given node. <code>null</code> if a discussion was closed and should be removed.
	 */
	public void update(String spaceId, String sessionXML) {
		if (sessionXML == null) {
			instance.removeShowChangeElement(((Element)discussionMap.get(spaceId).getPanel().getLastChild()));
			discussionsList.removeChild(discussionMap.get(spaceId).getPanel());
			discussions.remove(spaceId);
			discussionMap.remove(spaceId);
			startingTimes.remove(spaceId);
			if (spaceId.equals(discussionId)) {
				discussionId = null;
			}
		} else {
			discussions.put(spaceId, sessionXML);
			if (!startingTimes.containsKey(spaceId)) {
				com.google.gwt.xml.client.Element discElem = Parser.parseXMLStringToElement(sessionXML);
				if (discElem != null) {
					List<RecommendationObject> objs = Parser.parseRecommendations(discElem);
					if (objs == null || objs.size() == 0) return;
					final RecommendationObject obj = objs.get(0);
					long timestamp;
					if (obj.getEntries() != null && obj.getEntries().size() > 0) {
						timestamp = HasTimestamp.TIMESTAMPFORMAT.parseStrict(obj.getEntries().get(0).getTimestamp()).getTime();
					} else {
						timestamp = new Date().getTime();
					}
					startingTimes.put(spaceId, timestamp);
					addDiscussion(spaceId);
				}
			}
		}
		sort();
		updateModerators();
		if (discussionMap.containsKey(spaceId) && discussionMap.get(spaceId) != null && !spaceId.equals(discussionId)) {
			discussionMap.get(spaceId).setArrowActiveBackground();
			instance.addShowChangeElement(((Element)discussionMap.get(spaceId).getPanel().getLastChild()));
		}
	}
	
	private void sort() {
		Comparator<String> compare = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				long time1 = startingTimes.get(o1);
				long time2 = startingTimes.get(o2);
				return (int)(time2-time1);
			}
		};
		
		Map<String, String> discussionsMap = instance.getDiscussions();
		List<String> discussionsToRemove = new ArrayList<String>();
		for (String discussion:discussions.keySet()) {
			if (!discussionsMap.containsKey(discussion)) {
				discussionsToRemove.add(discussion);
			}
		}
		for (String discussion:discussionsToRemove) {
			discussions.remove(discussion);
			discussionMap.remove(discussion);
			discussionIds.remove(discussion);
		}
		
		List<String> ids = new ArrayList<String>();
		ids.addAll(discussions.keySet());
		Collections.sort(ids, compare);
		
		if (ids.size() == 0) {
			noItemsPanel.addClassName("activeItem");
			discussionsPanel.removeClassName("activeItem");
			noItemSelectedPanel.removeClassName("activeItem");
		} else if (discussionId == null || !ids.contains(discussionId)){
			noItemsPanel.removeClassName("activeItem");
			discussionsPanel.removeClassName("activeItem");
			noItemSelectedPanel.addClassName("activeItem");
		} else {
			noItemsPanel.removeClassName("activeItem");
			discussionsPanel.addClassName("activeItem");
			noItemSelectedPanel.removeClassName("activeItem");
		}
		for (String id:ids) {
			addDiscussion(id);
		}
		if (discussionId != null && discussions.containsKey(discussionId)) {
			showDetails(discussionId);
		}
	}
	
	private void filter() {		
		discussionIds.clear();
		for (String id:discussions.keySet()) {
			String xml = discussions.get(id);
			discussionMap.get(id).hideView();
			if (moderatorListBox.getSelectedIndex() > 0 ||  !"".equals(textToSearchFor)) {
				if (moderatorListBox.getSelectedIndex() > 0) {
					Map<String, String> infos = SessionObject.getAllInformation(xml);
					String moderatorToCheck = moderatorListBox.getItemText(moderatorListBox.getSelectedIndex());
					if (!moderatorToCheck.equals(instance.getDisplayNameForJid(infos.get("moderator")))) {
						continue;
					}
				}
				if (!"".equals(textToSearchFor)) {
					com.google.gwt.xml.client.Element recElem = Parser.parseXMLStringToElement(xml);
					if (recElem != null) {
						List<RecommendationObject> objs = Parser.parseRecommendations(recElem);
						if (objs != null && objs.size() > 0) {
							RecommendationObject recomm = objs.get(0);
							Map<String, String> infos = SessionObject.getAllInformation(xml);
							if (!recomm.getTitle().toLowerCase().contains(textToSearchFor.toLowerCase()) &&
									!recomm.getIssue().toLowerCase().contains(textToSearchFor.toLowerCase()) &&
									!infos.get("moderator").toLowerCase().contains(textToSearchFor.toLowerCase()) &&
									!infos.get("discussionGroup").toLowerCase().contains(textToSearchFor.toLowerCase())) {
								boolean part = false;
								for (String participant:recomm.getParticipants()) {
									if (participant.toLowerCase().contains(textToSearchFor.toLowerCase())) {
										part = true;
										break;
									}
										
								}
								if (!part) {
									continue;
								}
							}
						}
					}
				}
				discussionIds.add(id);
			} else {
				discussionIds.add(id);
			}
		}
		for (String id: discussionIds) {
			discussionMap.get(id).showView();
		}
		if (discussionIds.size() == 0) {
			noItemsPanel.addClassName("activeItem");
			discussionsPanel.removeClassName("activeItem");
			noItemSelectedPanel.removeClassName("activeItem");
		} else if (discussionId == null || !discussionIds.contains(discussionId)){
			noItemsPanel.removeClassName("activeItem");
			discussionsPanel.removeClassName("activeItem");
			noItemSelectedPanel.addClassName("activeItem");
		} else {
			noItemsPanel.removeClassName("activeItem");
			discussionsPanel.addClassName("activeItem");
			noItemSelectedPanel.removeClassName("activeItem");
		}
	}
}
