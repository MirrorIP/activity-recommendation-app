package de.imc.mirror.arapp.client.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.RecommendationPanel;
import de.imc.mirror.arapp.client.Interfaces.HasDetailPanel;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.RecommendationObject.TaskType;

public abstract class RecommendationsOverviewView extends View implements HasDetailPanel{
	
	protected enum SharedElements {
		TASKTYPE("captureSummaryType", "manageSummaryType"),
		ISSUE("captureIssue", "manageIssue"),
		NOITEMSELECTEDPANEL("captureNoItemSelectedInfoPanel", "manageNoItemSelectedInfoPanel"),
		NOITEMSPANEL("captureNoItemsAvailableInfoPanel", "manageNoItemsAvailableInfoPanel"),
		PUBLISHERLABEL("captureSummaryPublisher", "manageSummaryPublisher"),
		PUBLISHERLISTBOX("capturePublisherSelect", "managePublisherSelect"),
		PUBLISHINGDATELABEL("captureSummaryDate", "manageSummaryDate"),
		RECOMMENDATIONSPANEL("captureRecommendationPanel", "manageRecommendationPanel"),
		SEARCHBOX("captureSearchInput", "manageSearchInput"),
		SEARCHBUTTON("captureSearchInputButton", "manageSearchInputButton"),
		SOLUTION("captureSolution", "manageSolution"),
		TITLELABEL("captureRecommendationTitle", "manageRecommendationTitle"),
		TYPESELECT("captureTypeSelect", "manageTypeSelect"),
		VIEWEXPERIENCEBENEFIT("captureMyExperiencesBenefit", "manageViewExperiencesBenefit"),
		VIEWEXPERIENCEEFFORT("captureMyExperiencesEffort", "manageViewExperiencesEffort"),
		VIEWEXPERIENCERATING("captureMyExperiencesRating", "manageViewExperiencesRating"),
		VIEWEXPERIENCESRATIOPANEL("captureMyExperienceRatioPanel", "manageViewExperiencesRatioPanel"),
		VIEWEXPERIENCESRATIO("captureMyExperienceRatio", "manageViewExperiencesRatio"),
		VIEWEXPERIENCETABLE("captureMyExperiencesExperiences", "manageViewExperiencesExperiences");
		
		private String captureId;
		private String manageId;
		
		private static boolean manage = false;
		
		private SharedElements(String captureId, String manageId) {
			this.captureId = captureId;
			this.manageId = manageId;
		}
		
		public static void setManage(boolean manage) {
			SharedElements.manage = manage;
		}
		
		public String getId() {
			if (manage) {
				return manageId;
			} else {
				return captureId;
			}
		}
	}	
	protected Map<String, RecommendationPanel> recomms;
	protected Map<String, RecommendationObject> recommObjects;
	protected List<String> recommIds;
	
	protected Map<String, List<String>> userJidToRecommIds;

	protected String recommId;

	protected ListBox publisherListBox;
	protected ListBox typeListBox;
	
	protected boolean filterPublisher = false;
	protected boolean filterType = false;
	protected String textToSearchFor = "";
	
	protected TextBox searchBox;
	protected Button searchButton;

	protected Element noItemsPanel;
	protected Element noItemSelectedPanel;
	protected Element recommendationsPanel;

	protected Element titleLabel;
	protected Element issue;
	protected Element solution;
	protected Element publisherLabel;
	protected Element publishingDateLabel;

	protected Element viewExperience;
	protected HTML viewExperiencesTab; 

	protected Element averageStarRatingStarsList;
	protected Element averageRatingLabel;
	protected Element averageStarRatingLabel;
	protected Element averageExperiencesSummary;

	protected Element experienceBenefitRating;
	protected Element experienceBenefitRatingLabel;
	protected Element experienceBenefitRatingRating;	
	protected Element experienceEffortRating;		
	protected Element experienceEffortRatingLabel;	
	protected Element experienceEffortRatingRating;
	protected Element averageExperiencesExperiencesListBody;
	private Element activityTask;
	private Element learningTask;
	private Element behaviorTask;
	private Element ratioLabel;
	private Element ratioPanel;
	
	protected RecommendationsOverviewView(ARApp instance) {
		super(instance);
		recomms = new HashMap<String, RecommendationPanel>();
		recommObjects = new HashMap<String, RecommendationObject>();
		recommIds = new ArrayList<String>();
		userJidToRecommIds = new HashMap<String, List<String>>();
		build();	
		getRecommendations();	
		initializeFilters();
	}
	
	/**
	 * Shows all the recommendationdetails for a specific id.
	 * @param id the custom id of the recommendation to show.
	 */
	public void showDetails(String id){
		RecommendationObject recomm = recommObjects.get(id);
		if (recomm == null) {
			return;
		}
		recommId = id;
		
		if (!recommendationsPanel.getClassName().contains("activeItem")) {
			recommendationsPanel.addClassName("activeItem");
			noItemSelectedPanel.removeClassName("activeItem");
			noItemsPanel.removeClassName("activeItem");
		}
		
		learningTask.removeClassName("activeType");
		behaviorTask.removeClassName("activeType");
		activityTask.removeClassName("activeType");

		if (recomm.getTaskType() != null) {
			switch (recomm.getTaskType()) {
			case ACTIVITY:
				activityTask.addClassName("activeType");
				break;
			case BEHAVIOR:
				behaviorTask.addClassName("activeType");
				break;
			case LEARNING:
				learningTask.addClassName("activeType");
				break;		
			}
		}
		
		titleLabel.setInnerHTML(recomm.getTitle());
		issue.setInnerHTML(recomm.getIssue());
		solution.setInnerHTML(recomm.getRecommendedSolution());

		publisherLabel.setInnerHTML(instance.getDisplayNameForJid(recomm.getPublisher()));
		publishingDateLabel.setInnerHTML(recomm.getFormattedTimestamp(HasTimestamp.LONGDATE));

		if (averageExperiencesExperiencesListBody != null) {
			averageExperiencesExperiencesListBody.removeAllChildren();
		}
		if (averageStarRatingStarsList != null) {
			for (int i=0; i<averageStarRatingStarsList.getChildCount(); i++) {
				Element child = (Element)averageStarRatingStarsList.getChild(i);
				if (!"span".equalsIgnoreCase(child.getTagName())) continue;
				child.removeClassName("starFilled");				
			} 
		}
		averageExperiencesSummary.setInnerHTML("");
		averageStarRatingLabel.setInnerHTML("");
	}
	
	/**
	 * Adds a recommendationpanel to the list of recommendations.
	 * @param recomm the recommendationpanel to add.
	 */
	protected void addRecommendation(final RecommendationPanel recomm) {
		recomms.put(recomm.getCustomId(), recomm);		
		recommIds.add(recomm.getCustomId());
		recomm.showView();
	}
	
	protected void initializeSharedVariables() {
		StringBuilder errorBuilder = new StringBuilder("The HTML of this site was malformed. Please contact the administrator and send him the following infos:");
		boolean errors = false;
		for (SharedElements id:SharedElements.values()) {
			Element elem = Document.get().getElementById(id.getId());
			if (elem == null) {
				errorBuilder.append("\n").append(id.getId());
				errors = true;
			} else {
			switch (id) {
				case VIEWEXPERIENCERATING:
					NodeList<Element> elements = elem.getElementsByTagName("div");
					for (int j=0; j<elements.getLength(); j++) {
						Element child = elements.getItem(j);
						if ("descriptionText".equals(child.getClassName())) {
							averageRatingLabel = child;
						} else if ("starRating".equals(child.getClassName())) {
							averageStarRatingStarsList = child;
							averageStarRatingLabel = child.getElementsByTagName("label").getItem(0);
						}
					}
					break;
				case VIEWEXPERIENCETABLE:
					averageExperiencesSummary = elem.getElementsByTagName("div").getItem(0);	
			//		averageExperiencesExperiencesListHead = child.getElementsByTagName("thead").getItem(0);
					averageExperiencesExperiencesListBody = elem.getElementsByTagName("tbody").getItem(0);
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
				case TITLELABEL:
					titleLabel = elem;
					break;
				case ISSUE:
					issue = getBlockContentChild(elem);
					break;
				case SOLUTION:
					solution = getBlockContentChild(elem);
					break;
				case PUBLISHERLABEL:
					publisherLabel = getBlockContentChild(elem);
					break;
				case PUBLISHINGDATELABEL:
					publishingDateLabel = getBlockContentChild(elem);
					break;
				case PUBLISHERLISTBOX:
					publisherListBox = ListBox.wrap(elem);					
					publisherListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							if (publisherListBox.getSelectedIndex() > 0) {
								filterPublisher = true;
							} else {
								filterPublisher = false;
							}
							filter();
						}
					});	
					break;
				case TYPESELECT:
					typeListBox = ListBox.wrap(elem);
					typeListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							if (typeListBox.getSelectedIndex() > 0) {
								filterType = true;
							} else {
								filterType = false;
							}
							filter();
						}
					});	
					break;
				case NOITEMSELECTEDPANEL:
					noItemSelectedPanel = elem;
					break;
				case NOITEMSPANEL:
					noItemsPanel = elem;
					break;
				case RECOMMENDATIONSPANEL:
					recommendationsPanel = elem;
					break;
				case TASKTYPE:
					NodeList<Element> elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().contains("activityType")) {
							activityTask = child;						
						} else if (child.getClassName().contains("learningType")) {
							learningTask = child;						
						} else if (child.getClassName().contains("behaviorType")) {
							behaviorTask = child;						
						}
					}
					break;
				case VIEWEXPERIENCEBENEFIT:
					experienceBenefitRating = elem;
					elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().contains("descriptionText")) {
							experienceBenefitRatingLabel = child;
						} else if ("block".equals(child.getClassName())) {
							experienceBenefitRatingRating = child;
						}
					}
					break;
				case VIEWEXPERIENCEEFFORT:
					experienceEffortRating = elem;
					elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().contains("descriptionText")) {
							experienceEffortRatingLabel = child;
						} else if ("block".equals(child.getClassName())) {
							experienceEffortRatingRating = child;
						}
					}
					break;
				case VIEWEXPERIENCESRATIOPANEL:
					ratioPanel = elem;
					break;
				case VIEWEXPERIENCESRATIO:
					ratioLabel = elem;
					break;
				}
			}
		}

		if (errors) {
			Window.alert(errorBuilder.toString());
		}
		
		recommendationsPanel.removeClassName("activeItem");
		noItemSelectedPanel.addClassName("activeItem");
		noItemsPanel.removeClassName("activeItem");
	}
	
	/**
	 * This method will build the recommendationslist.
	 */
	private void getRecommendations() {
		recommObjects = instance.getRecommendationsForUser();
		if (recommObjects == null || recommObjects.size() == 0) {
			return;
		}
		Map<String, String> refMap = instance.getRefMap();
		List<RecommendationObject> recommObjects = new ArrayList<RecommendationObject>();
		recommObjects.addAll(this.recommObjects.values());
		
		Collections.sort(recommObjects, HasTimestamp.COMPAREAGAINSTTIMESTAMP);		
		List<String> availableCustomIds = new ArrayList<String>();
		for (RecommendationObject recomm:recommObjects) {
			if (recomm.getCustomId() == null) continue;
			if (!recomms.containsKey(recomm.getCustomId())) {
				if (refMap.containsValue(recomm.getCustomId()) || recomm.deleted()) {
					continue;
				}
				addUsers(recomm);
				addRecommendation(new RecommendationPanel(recomm, this));
			} else {
				if (refMap.containsValue(recomm.getCustomId()) || recomm.deleted()) {
					recomms.get(recomm.getCustomId()).removePanel();
					recomms.remove(recomm.getCustomId());
					recommIds.remove(recomm.getCustomId());
				} else {
					recomms.get(recomm.getCustomId()).updateTitle(recomm.getTitle());
				}
			}
			availableCustomIds.add(recomm.getCustomId());
		}
		List<String> currentCustomIds = new ArrayList<String>();
		currentCustomIds.addAll(recomms.keySet());
		if (currentCustomIds != null && currentCustomIds.removeAll(availableCustomIds) && currentCustomIds.size() > 0) {
			for (String customId:currentCustomIds) {
				recomms.get(customId).removePanel();
				recomms.remove(customId);
				recommIds.remove(customId);
			}
		}
		sort();
	}
	
	/**
	 * Method to return all members of a space.
	 * @param space the space to get the members for. It is a "Space" object from the Javascript Spaces-SDK.
	 * @return a list with all jids of all the members of the space.
	 */
	protected native List<String> getSpaceMembers(JavaScriptObject space) /*-{
		var list = @java.util.ArrayList::new()();
		if (!space || space == null) return list;
		var members = space.getMembers();
		for (var i=0; i<members.length; i++) {
			list.@java.util.List::add(Ljava/lang/Object;)(members[i].getJID());
		}
		return list;
	}-*/;
	
	/**
	 * Caches all user ids and the custom id of the given recommendation in a map, so it is possible to say to what recommendations a user has access.
	 * @param recomm the recommendation to caches the users for.
	 */
	private void addUsers(RecommendationObject recomm) {
		Map<String, JavaScriptObject> spacesMap = instance.getCompleteSpacesMap();
		List<String> spaces = recomm.getTargetSpaces();
		if (spaces != null) {
			for (String spaceId: spaces) {
				if (!spacesMap.containsKey(spaceId)) continue;
				
				List<String> spaceMembers = getSpaceMembers(spacesMap.get(spaceId));
				for (String jid:spaceMembers) {
					if (userJidToRecommIds.containsKey(jid)) {
						if (userJidToRecommIds.get(jid) != null) {
							if (userJidToRecommIds.get(jid).contains(recomm.getCustomId())) continue;
							userJidToRecommIds.get(jid).add(recomm.getCustomId());
						} else {
							List<String> value = new ArrayList<String>();
							value.add(recomm.getCustomId());
							userJidToRecommIds.put(jid, value);
						}
					} else {
						List<String> value = new ArrayList<String>();
						value.add(recomm.getCustomId());
						userJidToRecommIds.put(jid, value);
					}
				}
			}
		}
	}
	
	/**
	 * Called when newer information about recommendations or experiences are available.
	 */
	public void update() {
		getRecommendations();
		if (recommId != null && recommObjects.get(recommId) != null && recomms.get(recommId) != null) {
			showDetails(recommId);
		}
		filter();
		updateFilters();
	}
	
	public void reset() {
		recommIds.clear();
		recomms.clear();
		recommObjects.clear();
	}
	
	protected void updateFilters() {
		Map<String, String> refMap = instance.getRefMap();
		List<String> publishers = new ArrayList<String>();

		List<String> publishersInListBox = new ArrayList<String>();

		for (int i=1; i<publisherListBox.getItemCount(); i++) {
			publishersInListBox.add(publisherListBox.getItemText(i));
		}

		for (String id:recomms.keySet()) {
			RecommendationObject recomm = recommObjects.get(id);
			if (recomm == null) continue;
			if (refMap.containsValue(recomm.getCustomId())) {
				continue;
			}
			String publisher = instance.getDisplayNameForJid(recomm.getPublisher());
			if (publisher != null && !publishersInListBox.contains(publisher) && !publishers.contains(publisher)) {
				publishers.add(publisher);
			}
		}

		int publisherIndex = publisherListBox.getSelectedIndex();
		for (int j=0; j<publishers.size(); j++) {
			String newPublisher = publishers.get(j);
			if (publisherListBox.getItemCount() == 1) { 
				publisherListBox.insertItem(newPublisher, 1);
			} else {
				for (int i=1; i<publisherListBox.getItemCount()-1; i++) {
					String publisher = publisherListBox.getItemText(i);
					String nextPublisher = publisherListBox.getItemText(i+1);
					if (publisher.compareToIgnoreCase(newPublisher) < 0 && nextPublisher.compareToIgnoreCase(newPublisher) > 0) {
						publisherListBox.insertItem(newPublisher, i+1);
						if (publisherIndex > i) {
							publisherIndex++;
						}
						break;
					}
				}
			}
		}
		publisherListBox.setSelectedIndex(publisherIndex);
	}
	
	protected void initializeFilters() {
		Map<String, String> refMap = instance.getRefMap();
		Collection<RecommendationObject> recommObjects = instance.getRecommendationsForUser().values();
		
		List<String> publishers = new ArrayList<String>();

		for (RecommendationObject recomm:recommObjects) {
			if (refMap.containsValue(recomm.getCustomId())) {
				continue;
			}

			String publisher = instance.getDisplayNameForJid(recomm.getPublisher());
			if (publisher == null) {
				publisher = recomm.getPublisher();
			}
			if (!publishers.contains(publisher)) {
				publishers.add(publisher);
			}
		}

		Collections.sort(publishers, COMPARESTRINGIGNORECASE);
		publisherListBox.clear();
		publisherListBox.addItem(instance.infoMessage.listBoxAllEntry());
		
		for (String publisher:publishers) {
			publisherListBox.addItem(publisher);
		}
		

		if (typeListBox.getSelectedIndex() > 0) {
			filterType = true;
		} else {
			filterType = false;
		}
		
		if (publisherListBox.getSelectedIndex() > 0) {
			filterPublisher = true;
		} else {
			filterPublisher = false;
		}
	}
	
	protected void sort() {
		for (RecommendationPanel panel:recomms.values()) {
			panel.hideView();
		}		
		if (recommIds.size() == 0) {
			noItemsPanel.addClassName("activeItem");
			noItemSelectedPanel.removeClassName("activeItem");
			recommendationsPanel.removeClassName("activeItem");
		} else if (recommId == null || !recommIds.contains(recommId)){
			noItemsPanel.removeClassName("activeItem");
			noItemSelectedPanel.addClassName("activeItem");
			recommendationsPanel.removeClassName("activeItem");
		} else {
			noItemsPanel.removeClassName("activeItem");
			recommendationsPanel.addClassName("activeItem");
			noItemSelectedPanel.removeClassName("activeItem");
		}
		for (String id:recommIds) {
			recomms.get(id).showView();
		}
	}
	
	protected void filter() {		
		recommIds.clear();
		for (String id:recomms.keySet()) {
			RecommendationObject recomm = recommObjects.get(id);
			if (recomm == null) continue;
			recomms.get(id).showView();
			if (filterPublisher || filterType ||  !textToSearchFor.isEmpty()) {
				if (filterType) {
					int index = typeListBox.getSelectedIndex();
					String typeString = typeListBox.getItemText(index);
					typeString = typeString.split(" ")[0];
					TaskType type = TaskType.valueOf(typeString.toUpperCase());
					if (!type.equals(recomm.getTaskType())) {
						continue;
					}
				}
				if (filterPublisher) {
					int index = publisherListBox.getSelectedIndex();
					if (!publisherListBox.getItemText(index).contains(instance.getDisplayNameForJid(recomm.getPublisher()))) {
						continue;
					}
				}
				if (textToSearchFor != null && !textToSearchFor.isEmpty()) {
					if (recomm.getTitle() != null && !recomm.getTitle().toLowerCase().contains(textToSearchFor.toLowerCase())) {
						continue;
					} else if (recomm.getIssue() != null &&	!recomm.getIssue().toLowerCase().contains(textToSearchFor.toLowerCase())) {
						continue;
					} else if (recomm.getRecommendedSolution() != null && !recomm.getRecommendedSolution().toLowerCase().contains(textToSearchFor.toLowerCase())) {
						continue;
					}
				}
				recommIds.add(recomm.getCustomId());
			} else {
				recommIds.add(recomm.getCustomId());
			}
		}
		
		sort();
	}

	@Override
	protected abstract void build();
	
	protected double getAverageRating(List<Experience> experiences) {
		if (experiences == null || experiences.size() == 0) {
			return 0;
		} else {
			int amount = 0;
			double absolute = 0;
			for (Experience experience:experiences) {
				if (experience.getRating() != -1) {
					amount++;
					absolute += experience.getRating();
				}
			}
			if (amount == 0) {
				return 0;
			}
			absolute = absolute/amount;
			absolute = Math.rint(10*absolute) / 10;
			return absolute;		
		}
	}
	
	protected void showExperiences(List<Experience> experiences, boolean publisher) {
		averageRatingLabel.setInnerText(recommObjects.get(recommId).getRatingDescription());
		double average = getAverageRating(experiences);
		if (Double.isNaN(average)) {
			average = 0;
		}
		int absolute = (int)Math.rint(average);
		int amount = 0;
		for (int i=0; i<averageStarRatingStarsList.getChildCount(); i++) {
			Element child = (Element)averageStarRatingStarsList.getChild(i);
			if (!"span".equalsIgnoreCase(child.getTagName())) continue;
			if (amount<absolute) {
				child.setClassName("starFilled");
			}
			amount++;
			if (amount >= absolute) {
				break;
			}
		} 
		
		showBenefit(experiences);
		showEffort(experiences);
		showRatio(experiences);
		
		
		Collections.sort(experiences, HasTimestamp.COMPAREAGAINSTTIMESTAMP);
		
		averageStarRatingLabel.setInnerText(instance.infoMessage.averageRating(average));
		
		averageExperiencesExperiencesListBody.removeAllChildren();
		int commentAmount = 0;
		
		if (experiences.size() > 0) {
			for (Experience exp:experiences) {
				final Experience ex = exp;
				
				Element root = Document.get().createTRElement();
				
				HTML.wrap(root).addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						instance.getUsageExperiencePopup().showPopup(ex);
					}
				});
				
				Element starTdElement = Document.get().createTDElement();
				Element divElement = Document.get().createDivElement();
				divElement.setClassName("starRating");
				for (int i=0; i<5; i++) {
					Element spanElement = Document.get().createSpanElement();
					spanElement.setInnerHTML("&nbsp;");
					if (i<exp.getRating()) {
						spanElement.setClassName("starFilled");
					}
					divElement.appendChild(spanElement);
				}
				starTdElement.appendChild(divElement);
				
				Element dateTdElement = Document.get().createTDElement();
				dateTdElement.setInnerHTML(exp.getFormattedTimestamp(HasTimestamp.SHORTDATE));
				
				
				Element commentTdElement = Document.get().createTDElement();
				commentTdElement.setInnerHTML(exp.getComment());
				
				if (exp.getComment() != null && !"".equals(exp.getComment())) {
					commentAmount++;
				}
	
				root.appendChild(starTdElement);
				root.appendChild(dateTdElement);
				
				if (publisher) {
					Element publisherTdElement = Document.get().createTDElement();
					String publisherString = exp.getPublisher();
					publisherString = instance.getDisplayNameForJid(publisherString);
					publisherTdElement.setInnerHTML(publisherString);
					root.appendChild(publisherTdElement);
				}
				
				root.appendChild(commentTdElement);
				
				averageExperiencesExperiencesListBody.appendChild(root);
			}
			averageExperiencesSummary.setInnerText(instance.infoMessage.experiencesAmount(experiences.size(), commentAmount));
		} else {
			averageExperiencesSummary.setInnerText(instance.infoMessage.noExperiencesYet());
		}
		
	}
	
	protected void showEffort(List<Experience> experiences) {
		int totalEffort = 0;
		int efforts = 0;

		RecommendationObject rec = recommObjects.get(recommId);
		if (rec.getEffortRating() == null) {
			experienceEffortRating.getStyle().setDisplay(Display.NONE);
			return;
		} else {
			experienceEffortRating.getStyle().clearDisplay();		
		}
		if (experiences != null && experiences.size() > 0) {
			for (Experience ex:experiences) {
				if (ex.getEffort() != null && ex.getEffort().getValue() >= 0) {
					totalEffort += ex.getEffort().getValue();
					efforts++;
				}
			}
		}
		
		experienceEffortRatingLabel.setInnerHTML(rec.getEffortRating().getDescription());

		Element displayLabel = Document.get().createLabelElement();
		displayLabel.setInnerHTML(rec.getEffortRating().getDisplay());
		displayLabel.addClassName("unitText");
		
		Element spaceSpan = Document.get().createSpanElement();
		spaceSpan.setInnerHTML("&nbsp;&nbsp;");
		
		Element ratingSpan = Document.get().createSpanElement();
		
		ratingSpan.setInnerText(instance.infoMessage.averageProperty(efforts, totalEffort/efforts, totalEffort));
		
		experienceEffortRatingRating.removeAllChildren();
		
		experienceEffortRatingRating.appendChild(ratingSpan);
		experienceEffortRatingRating.appendChild(spaceSpan);
		experienceEffortRatingRating.appendChild(displayLabel);
		
	}

	protected void showBenefit(List<Experience> experiences) {
		int totalBenefit = 0;
		int benefits = 0;	
		
		RecommendationObject rec = recommObjects.get(recommId);
		if (rec.getBenefitRating() == null) {
			experienceBenefitRating.getStyle().setDisplay(Display.NONE);
			return;
		} else {
			experienceBenefitRating.getStyle().clearDisplay();
		}
		if (experiences != null && experiences.size() > 0) {
			for (Experience ex:experiences) {
				if (ex.getBenefit() != null && ex.getBenefit().getValue() >= 0) {
					totalBenefit += ex.getBenefit().getValue();
					benefits++;
				}				
			}
		}
		
		experienceBenefitRatingLabel.setInnerHTML(rec.getBenefitRating().getDescription());		

		Element displayLabel = Document.get().createLabelElement();
		displayLabel.setInnerHTML(rec.getBenefitRating().getDisplay());
		displayLabel.addClassName("unitText");

		Element spaceSpan = Document.get().createSpanElement();
		spaceSpan.setInnerHTML("&nbsp;&nbsp;");
		
		Element ratingSpan = Document.get().createSpanElement();
		
		ratingSpan.setInnerText(instance.infoMessage.averageProperty(benefits, totalBenefit/benefits, totalBenefit));
		
		experienceBenefitRatingRating.removeAllChildren();
		
		experienceBenefitRatingRating.appendChild(ratingSpan);
		experienceBenefitRatingRating.appendChild(spaceSpan);
		experienceBenefitRatingRating.appendChild(displayLabel);
	}

	
	protected void showRatio(List<Experience> experiences) {
		int totalEffort = 0;
		int totalBenefit = 0;

		RecommendationObject rec = recommObjects.get(recommId);
		if (rec.getEffortRating() == null || rec.getBenefitRating() == null) {
			ratioPanel.getStyle().setDisplay(Display.NONE);
			return;
		} else {
			ratioPanel.getStyle().clearDisplay();		
		}
		if (experiences != null && experiences.size() > 0) {
			for (Experience ex:experiences) {
				if (ex.getEffort() != null && ex.getEffort().getValue() >= 0) {
					totalEffort += ex.getEffort().getValue();
				}
				if (ex.getBenefit() != null && ex.getBenefit().getValue() >= 0) {
					totalBenefit += ex.getBenefit().getValue();
				}
			}
		}
		
		StringBuilder builder = new StringBuilder();
		if (totalEffort > 0) {
			double ratio = (double)((totalBenefit * 10) / totalEffort)/10;
			builder.append(ratio);
		} else {
			builder.append(instance.infoMessage.notAvailable());
		}
		ratioLabel.setInnerHTML(builder.toString());		
	}

	protected Element getBlockContentChild(Element elem) {
		int childCount = elem.getChildCount();
		for (int j = 0; j<childCount; j++) {
			Element child = (Element) elem.getChild(j);
			if (child.getClassName() == null || "".equals(child.getClassName())) continue;
			if ("blockContent".equalsIgnoreCase(child.getClassName())) {
				return child;
			}
		}
		return null;
	}


	protected native void publishStatus(String statusXml, List<String> targetSpaces) /*-{
		var xmlDoc;
		if (window.DOMParser){
			var parser = new DOMParser();
		  	xmlDoc = parser.parseFromString(statusXml, 'text/xml');
		} else { // Internet Explorer
			xmlDoc = new ActiveXObject('MSXML.DOMDocument');
			xmlDoc.async = false;
			xmlDoc.loadXML(statusXml);
		}
		var dataObject = new $wnd.SpacesSDK.DataObject(xmlDoc.childNodes[0]);
		var length = targetSpaces.@java.util.List::size()();
		for (var i=0; i<length; i++) {
			var id = targetSpaces.@java.util.List::get(I)(i);
			$wnd.dataHandler.publishDataObject(dataObject, id);
		}
	}-*/;
}
