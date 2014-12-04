package de.imc.mirror.arapp.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.text.client.IntegerParser;
import com.google.gwt.text.client.IntegerRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Benefit;
import de.imc.mirror.arapp.client.Effort;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.FileEvidence;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.RecommendationPanel;
import de.imc.mirror.arapp.client.RecommendationStatus;
import de.imc.mirror.arapp.client.Interfaces.ExperiencesTab;
import de.imc.mirror.arapp.client.Interfaces.HasEvidences;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.RecommendationStatus.Status;
import de.imc.mirror.arapp.client.service.ARAppService;
import de.imc.mirror.arapp.client.service.ARAppServiceAsync;
import de.imc.mirror.arapp.client.view.experiencesTab.CaptureTabGroupExperiences;
import de.imc.mirror.arapp.client.view.experiencesTab.CaptureTabMyExperiences;

public class CaptureTab extends RecommendationsOverviewView implements HasEvidences{
	
	private enum ElementIds{
		ACTIVEELEMENT("captureActiveSection"),
		ACTIVESECTION("captureActiveSectionHeader"),
		ATTACHEDEVIDENCESLABEL("captureCaptureExperienceEvidencesNumber"),
		ATTACHEVIDENCESBUTTON("captureCaptureExperienceEvidencesButton"),
		BENEFITDIV("captureCaptureExperienceBenefit"),
		BENEFITINPUT("captureCaptureExperienceBenefitInput"),
		CAPTUREEXPERIENCE("captureCaptureExperience"),
		CAPTUREXPERIENCETAB("tabCaptureCaptureExperience"),
		COMMENTAREA("captureCaptureExperienceCommentInput"),
		ERRORMESSAGE("captureCaptureExperienceErrorMessage"),
		EFFORTDIV("captureCaptureExperienceEffort"),
		EFFORTINPUT("captureCaptureExperienceEffortInput"),
		GROUPINFOLABEL("captureSummaryGroupInfo"),
		IGNOREDBUTTONELEMENT("captureIgnoreButton"),
		IGNOREDELEMENT("captureIgnoredSection"),
		IGNOREDSECTION("captureIgnoredSectionHeader"),
		
		LASTEXPERIENCE("captureSummaryLastExperience"),
		MARKASSOLVEDBUTTONELEMENT("captureMarkAsSolvedButton"),
		MYPROGRESSLABEL("captureSummaryProgress"),		
		
		RATINGDIV("captureCaptureExperienceRating"),		
		RECOMMENDATIONSPANEL("captureRecommendationPanel"),
		
		SOLVEDELEMENT("captureSolvedSection"),
		SOLVEDSECTION("captureSolvedSectionHeader"),
		SUBMITBUTTON("captureCaptureExperienceSubmitButton"),

		VIEWMYEXPERIENCE("captureMyExperiences"),
		VIEWMYEXPERIENCESTAB("tabCaptureMyExperiences"),
		VIEWGROUPEXPERIENCE("captureGroupExperiences"),
		VIEWGROUPEXPERIENCESTAB("tabCaptureGroupExperiences"),
		
		SHARINGLEVEL("captureCaptureExperienceSharingLevel");
		
		private String id;
		
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private int chosenSharingLevel = 3;
	private int rating;
	private String comment;
	private Effort effort;
	private Benefit benefit;
	
	private Element ratingDiv;

	private Element effortDiv;
	private Element effortLabel;
	private Element effortUnitLabel;
	private ValueBox<Integer> effortInput;

	private Element benefitDiv;
	private Element benefitLabel;
	private Element benefitUnitLabel;
	private ValueBox<Integer> benefitInput;
	
	private TextArea commentArea;

	private Element myProgressLabel;
	private Element lastExperience;
	private Element groupInfoLabel;
	private Element evidencesLabel;

	private Element activityList;
	private Element solvedList;
	private Element ignoredList;
	
	private Element starRatingDescription;
	private Element starRatingStarsList;
	
	private Element errorMessage;
	
	private Element sharingLevel;
	
	private HTML captureExperienceTab;

	private Element captureExperience;
	
	private Button submitButton;
	private Button attachEvidenceButton;
	private List<Evidence> attachedEvidences;
	private String customId;
	
	private Button markAsSolvedButton;
	private Button ignoreButton;
	
	private ExperiencesTab myExperiences;
	private ExperiencesTab groupExperiences;

	protected Element viewMyExperience;
	protected HTML viewMyExperiencesTab; 

	protected Element viewGroupExperience;
	protected HTML viewGroupExperiencesTab; 
	
	public CaptureTab(ARApp instance) {
		super(instance);
		attachedEvidences = new ArrayList<Evidence>();
		
		myExperiences = new CaptureTabMyExperiences(instance);
		groupExperiences = new CaptureTabGroupExperiences(instance);
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
				
				case ACTIVESECTION:
					HTML activeSection = HTML.wrap(elem);

					activeSection.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (solvedList == null || ignoredList == null || activityList == null) return;
							solvedList.getParentElement().removeClassName("activeItem");
							ignoredList.getParentElement().removeClassName("activeItem");
							activityList.getParentElement().addClassName("activeItem");
						}
					});
					break;
				case SOLVEDSECTION:
					HTML solvedSection = HTML.wrap(elem);
					solvedSection.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (solvedList == null || ignoredList == null || activityList == null) return;
							activityList.getParentElement().removeClassName("activeItem");
							ignoredList.getParentElement().removeClassName("activeItem");
							solvedList.getParentElement().addClassName("activeItem");
						}
					});
					break;
				case IGNOREDSECTION:
					HTML ignoredSection = HTML.wrap(elem);
					ignoredSection.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (solvedList == null || ignoredList == null || activityList == null) return;
							activityList.getParentElement().removeClassName("activeItem");
							solvedList.getParentElement().removeClassName("activeItem");
							ignoredList.getParentElement().addClassName("activeItem");
						}
					});	
					break;
				case ACTIVEELEMENT:
					Element activeElement = elem;
					activeElement.removeAllChildren();
					activityList = Document.get().createULElement();
					activityList.setClassName("recommendationList");
					activeElement.appendChild(activityList);
					break;
					
				case SOLVEDELEMENT:
					Element solvedElement = elem;
					solvedElement.removeAllChildren();
					solvedList = Document.get().createULElement();
					solvedList.setClassName("recommendationList");
					solvedElement.appendChild(solvedList);
					break;
					
				case IGNOREDELEMENT:
					Element ignoredElement = elem;
					ignoredElement.removeAllChildren();
					ignoredList = Document.get().createULElement();
					ignoredList.setClassName("recommendationList");
					ignoredElement.appendChild(ignoredList);
					break;
					
				case CAPTUREXPERIENCETAB:
					captureExperienceTab = HTML.wrap(elem);
					captureExperienceTab.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (viewMyExperiencesTab == null || viewMyExperience == null || viewGroupExperiencesTab == null || viewGroupExperience == null || captureExperience == null) return;
							captureExperienceTab.getElement().setClassName("activeItem");
							viewMyExperiencesTab.getElement().removeClassName("activeItem");
							viewMyExperience.removeClassName("activeItem");
							viewGroupExperiencesTab.getElement().removeClassName("activeItem");
							viewGroupExperience.removeClassName("activeItem");
							captureExperience.addClassName("activeItem");
						}
					});
					break;
				case VIEWMYEXPERIENCESTAB:
					viewMyExperiencesTab = HTML.wrap(elem);
					viewMyExperiencesTab.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (captureExperienceTab == null || viewMyExperience == null || viewGroupExperiencesTab == null || viewGroupExperience == null || captureExperience == null) return;
							captureExperienceTab.getElement().removeClassName("activeItem");
							viewMyExperiencesTab.getElement().setClassName("activeItem");
							captureExperience.removeClassName("activeItem");
							viewMyExperience.addClassName("activeItem");
							viewGroupExperiencesTab.getElement().removeClassName("activeItem");
							viewGroupExperience.removeClassName("activeItem");
						}
					});
					break;
				case VIEWGROUPEXPERIENCESTAB:
					viewGroupExperiencesTab = HTML.wrap(elem);
					viewGroupExperiencesTab.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							showGroupExperiencesTab();
						}
					});
					break;
				case CAPTUREEXPERIENCE:
					captureExperience = elem;
					break;
				case VIEWMYEXPERIENCE:
					viewMyExperience = elem;
					break;
				case VIEWGROUPEXPERIENCE:
					viewGroupExperience = elem;
					break;
				case MYPROGRESSLABEL:
					myProgressLabel = getBlockContentChild(elem);
					break;
				case LASTEXPERIENCE:
					lastExperience = getBlockContentChild(elem);
					break;
				case GROUPINFOLABEL:
					groupInfoLabel = getBlockContentChild(elem);
					break;
				case RATINGDIV:
					ratingDiv = elem;

					int ratingDivChildCount = ratingDiv.getChildCount();
					for (int k=0; k<ratingDivChildCount; k++) {
						Element child = (Element) ratingDiv.getChild(k);
						if (child.getClassName() == null || "".equals(child.getClassName()) || "titleLabel".equals(child.getClassName())) continue;
						if ("descriptionText".equals(child.getClassName())) {
							starRatingDescription = child;
						} else if ("starRating".equals(child.getClassName())){
							starRatingStarsList = child;
							int childCount = child.getChildCount();
							for (int j=0; j<childCount; j++) {
								child.removeChild(child.getChild(0));
							}
							for (int j=1; j<=5; j++) {
								final int starRating = j;
								final Element spanElement = Document.get().createSpanElement();
								spanElement.setInnerHTML("&nbsp");
								HTML.wrap(spanElement).addClickHandler(new ClickHandler() {
									
									@Override
									public void onClick(ClickEvent event) {
										Element prevSibling = spanElement.getPreviousSiblingElement();
										Element nextSibling = spanElement.getNextSiblingElement();
										
										spanElement.setClassName("starFilled");
										
										while(prevSibling != null) {
											prevSibling.setClassName("starFilled");
											prevSibling = prevSibling.getPreviousSiblingElement();
										}
										while(nextSibling != null) {
											nextSibling.removeClassName("starFilled");
											nextSibling = nextSibling.getNextSiblingElement();
										}
										rating = starRating;
									}
								});
								child.appendChild(spanElement);
							}
						}
					}	
					
					break;
				case EFFORTDIV:
					effortDiv = elem;
					int childCount = elem.getChildCount();
					for (int j=0; j<childCount; j++) {
						Element child = (Element)elem.getChild(j);
						if (child.getClassName() == null || "".equals(child.getClassName()) || "titleLabel".equals(child.getClassName())) continue;
						if ("descriptionText".equals(child.getClassName())) {
							effortLabel = child;
						} else if ("unitText".equals(child.getClassName())) {
							effortUnitLabel = child;
						}
					}
					break;
				case BENEFITDIV:
					benefitDiv = elem;
					childCount = elem.getChildCount();
					for (int j=0; j<childCount; j++) {
						Element child = (Element)elem.getChild(j);
						if (child.getClassName() == null || "".equals(child.getClassName()) || "titleLabel".equals(child.getClassName())) continue;
						if ("descriptionText".equals(child.getClassName())) {
							benefitLabel = child;
						} else if ("unitText".equals(child.getClassName())) {
							benefitUnitLabel = child;
						}
					}
					break;
				case COMMENTAREA:
					commentArea = TextArea.wrap(elem);
					break;	
				case EFFORTINPUT:
					effortInput = IntegerBox.wrap(elem, IntegerRenderer.instance(), IntegerParser.instance());
					effortInput.addKeyPressHandler(new KeyPressHandler() {
						
						@Override
						public void onKeyPress(KeyPressEvent event) {
							if ((event.getNativeEvent().getCharCode() < 48 || event.getNativeEvent().getCharCode() > 57) && 
									event.getNativeEvent().getKeyCode() != KeyCodes.KEY_BACKSPACE && event.getNativeEvent().getKeyCode() != KeyCodes.KEY_DELETE &&
									!KeyCodes.isArrowKey(event.getNativeEvent().getKeyCode()) && event.getNativeEvent().getKeyCode() != KeyCodes.KEY_TAB && 
									event.getNativeEvent().getKeyCode() != KeyCodes.KEY_END && event.getNativeEvent().getKeyCode() != KeyCodes.KEY_HOME) {
								event.getNativeEvent().preventDefault();
							}
						}
					});
					break;
				case BENEFITINPUT:
					benefitInput = IntegerBox.wrap(elem, IntegerRenderer.instance(), IntegerParser.instance());					
					benefitInput.addKeyPressHandler(new KeyPressHandler() {
						
						@Override
						public void onKeyPress(KeyPressEvent event) {
							if ((event.getNativeEvent().getCharCode() < 48 || event.getNativeEvent().getCharCode() > 57) && 
									event.getNativeEvent().getKeyCode() != KeyCodes.KEY_BACKSPACE && event.getNativeEvent().getKeyCode() != KeyCodes.KEY_DELETE &&
									!KeyCodes.isArrowKey(event.getNativeEvent().getKeyCode()) && event.getNativeEvent().getKeyCode() != KeyCodes.KEY_TAB && 
									event.getNativeEvent().getKeyCode() != KeyCodes.KEY_END && event.getNativeEvent().getKeyCode() != KeyCodes.KEY_HOME) {
								event.getNativeEvent().preventDefault();
							}
						}
					});
					break;
				case MARKASSOLVEDBUTTONELEMENT:
					Element markAsSolvedButtonElement = elem;					
					markAsSolvedButton = Button.wrap(markAsSolvedButtonElement);
					markAsSolvedButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (recommId != null) {
								markAsSolved(recommId);
							}
						}
					});
					break;
				case IGNOREDBUTTONELEMENT:
					Element ignoredButtonElement = elem;					
					ignoreButton = Button.wrap(ignoredButtonElement);
					ignoreButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (recommId != null) {
								ignore(recommId);
							}
						}
					});
					break;
				case ATTACHEDEVIDENCESLABEL:
					evidencesLabel = elem;
					evidencesLabel.setInnerText(instance.infoMessage.captureAttachedEvidences(0));
					break;
				case ATTACHEVIDENCESBUTTON:
					attachEvidenceButton = Button.wrap(elem);
					attachEvidenceButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getCaptureEvidencesPopup().showPopup(CaptureTab.this, attachedEvidences);
						}
					});
					break;
				case SUBMITBUTTON:
					submitButton = Button.wrap(Document.get().getElementById("captureCaptureExperienceSubmitButton"));
					submitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (rating == -1) {
								errorMessage.setInnerText(instance.errorMessage.notRated());
								errorMessage.addClassName("activeItem");
								return;
							} 
							if (commentArea.getText().length() > 0) {
								comment = commentArea.getText();
							} else {
								comment = null;
							}
							if (benefit != null && benefitInput.getValue() != null) {
								benefit.setValue(benefitInput.getValue());
							}

							if (effort != null && effortInput.getValue() != null) {
								effort.setValue(effortInput.getValue());
							}
							prepareExperience(recommId);
						}
					});
					break;
				case SHARINGLEVEL:
					sharingLevel = elem.getElementsByTagName("input").getItem(0);
					TextArea.wrap(sharingLevel).addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							Element elem = sharingLevel.getPreviousSiblingElement();
							String value = InputElement.as(sharingLevel).getValue();
							elem.getElementsByTagName("div").getItem(Integer.parseInt(value)-1).addClassName("activeItem");
							elem.getElementsByTagName("div").getItem(chosenSharingLevel-1).removeClassName("activeItem");
							sharingLevel.setAttribute("value", value);
							chosenSharingLevel = Integer.parseInt(value);
						}
					});
					break;
				case ERRORMESSAGE:
					errorMessage = elem;
					break;
				default:
					break;
				}
			}
		}	
		
		SharedElements.setManage(false);
		super.initializeSharedVariables();
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}
	}
	
	public void showGroupExperiencesTab() {
		if (captureExperienceTab == null || viewGroupExperience == null || viewMyExperiencesTab == null || viewMyExperience == null || captureExperience == null) return;
		captureExperienceTab.getElement().removeClassName("activeItem");
		viewGroupExperiencesTab.getElement().setClassName("activeItem");
		captureExperience.removeClassName("activeItem");
		viewGroupExperience.addClassName("activeItem");
		viewMyExperiencesTab.getElement().removeClassName("activeItem");
		viewMyExperience.removeClassName("activeItem");
	}
	
	public void attachEvidences(List<Evidence> attachEvidences) {
		this.attachedEvidences.clear();
		if (attachEvidences != null) {
			this.attachedEvidences.addAll(attachEvidences);
		}
		evidencesLabel.setInnerText(instance.infoMessage.captureAttachedEvidences(attachedEvidences.size()));
	}
	
	public void attachCreatedEvidence(Evidence ev) {
		this.attachedEvidences.add(ev);
	}
	
	public String getFileEvidenceLocation() {
		String userId = instance.getBareJID();
		userId = userId.split("@")[0];
		return recommId + "_" + userId;
	}
	
	public String getCustomId() {
		if (customId == null) {
			customId = ARApp.uuid();
		}
		return customId;
	}

	@Override
	public void showDetails(final String id){
		RecommendationObject recomm = recommObjects.get(id);
		if (recomm == null) return;
		
		super.showDetails(id);
		resetInput();
		myExperiences.reset();
		groupExperiences.reset();
		this.recommId = id;
		
		if (recomm.getTargetSpaces() == null || recomm.getTargetSpaces().size() == 0) {
			markAsSolvedButton.setVisible(false);
			ignoreButton.setVisible(false);
			submitButton.setVisible(false);
			errorMessage.setInnerHTML(instance.errorMessage.noTargetSpacesAvailableHTML());
			errorMessage.addClassName("activeItem");
		} else {
			markAsSolvedButton.setVisible(true);
			ignoreButton.setVisible(true);
			submitButton.setVisible(true);
			errorMessage.removeClassName("activeItem");
		}

		lastExperience.setInnerHTML(instance.infoMessage.captureNoExperiencesCaptured());

		Map<String, Map<String, RecommendationStatus>> stati = instance.getRecommendationStati();
		Map<String, RecommendationStatus> myStati = stati.get(instance.getBareJID());

		RecommendationStatus status = null;
		
		if (myStati != null && myStati.get(id) != null) {
			status = myStati.get(id);
			
			activityList.getParentElement().removeClassName("activeItem");
			ignoredList.getParentElement().removeClassName("activeItem");
			solvedList.getParentElement().removeClassName("activeItem");
			
			if (status.getStatus() == Status.IGNORED) {
				ignoredList.getParentElement().addClassName("activeItem");
			} else if (status.getStatus() == Status.OPEN) {
				activityList.getParentElement().addClassName("activeItem");
			} else {
				solvedList.getParentElement().addClassName("activeItem");
			}
		}
		markAsSolvedButton.setText(instance.infoMessage.getSolvedButtonCaption(status == null? null: status.getStatus()));
		ignoreButton.setText(instance.infoMessage.getIgnoreButtonCaption(status == null? null: status.getStatus()));
		
		myProgressLabel.setInnerHTML(instance.infoMessage.recommendationProgress(status == null? null:status.getStatus()));

		Map<String, JavaScriptObject> spacesMap = instance.getCompleteSpacesMap();	
		Map<String, List<String>> targetPersonsMap = new HashMap<String, List<String>>();
		List<String> spaces = recomm.getTargetSpaces();
		if (spaces != null) {
			for (String spaceId: spaces) {
				if (!spacesMap.containsKey(spaceId)) continue;
				
				List<String> spaceMembers = getSpaceMembers(spacesMap.get(spaceId));
				for (String jid:spaceMembers) {
					if (targetPersonsMap.containsKey(recomm.getCustomId())) {
						if (targetPersonsMap.get(recomm.getCustomId()) != null) {
							if (targetPersonsMap.get(recomm.getCustomId()).contains(jid)) continue;
							targetPersonsMap.get(recomm.getCustomId()).add(jid);
						} else {
							List<String> value = new ArrayList<String>();
							value.add(jid);
							targetPersonsMap.put(recomm.getCustomId(), value);
						}
					} else {
						List<String> value = new ArrayList<String>();
						value.add(jid);
						targetPersonsMap.put(recomm.getCustomId(), value);
					}
				}
			}
		}
		int amount = 0;
		int userSolved = 0;
		if (targetPersonsMap.containsKey(id)) {
			amount = targetPersonsMap.get(id).size();
			for (String jid: targetPersonsMap.get(id)) {
				if (stati.get(jid) != null) {
					Map<String, RecommendationStatus> userStati = stati.get(jid);
					if (userStati.get(id) != null && userStati.get(id).getStatus() == Status.SOLVED) {
						userSolved++;
					}
				}
			}
		}
		groupInfoLabel.setInnerText(instance.infoMessage.captureRecommendationSolvedInfo(userSolved, amount));		
		
		starRatingDescription.setInnerHTML(recomm.getRatingDescription());
		
		effort = recomm.getEffortRating();
		if (effort != null) {
			effortDiv.removeAttribute("style");
			effortLabel.setInnerHTML(effort.getDescription());
			effortUnitLabel.setInnerHTML(effort.getDisplay());
		} else {
			effortDiv.setAttribute("style", "display:none;");
		}

		benefit = recomm.getBenefitRating();
		if (benefit != null) {
			benefitDiv.removeAttribute("style");
			benefitLabel.setInnerHTML(benefit.getDescription());
			benefitUnitLabel.setInnerHTML(benefit.getDisplay());
		} else {
			benefitDiv.setAttribute("style", "display:none;");
		}
		boolean isModeratorOfRecommendation = recomm.getParticipants().contains(instance.getBareJID());
		Map<String, Experience> exps = instance.getExperiencesForRecommendation(id);
		if (exps != null && exps.size() > 0) {
			List<Experience> userExps = new ArrayList<Experience>();
			List<Experience> experiences = new ArrayList<Experience>();
			
			for (Experience exp:exps.values()) {
				if (exp.getPublisher().contains(instance.getBareJID())) {
					userExps.add(exp);
					experiences.add(exp);
				} else if (exp.getSharingLevel() > 0 && exp.getSharingLevel() <= 5) {
					int acceptableLevel = isModeratorOfRecommendation? 4:3;
					if (exp.getSharingLevel() <= acceptableLevel) {
						experiences.add(exp);
					}
				} else {
					experiences.add(exp);
				}
			}
						
			myExperiences.showExperienceDetails(userExps, recomm);
			groupExperiences.showExperienceDetails(experiences, recomm);
			if (experiences.size() > 0) {
				lastExperience.setInnerHTML(experiences.get(0).getFormattedTimestamp(HasTimestamp.LONGDATE));
			}
		} else {
			myExperiences.showExperienceDetails(new ArrayList<Experience>(), recomm);
			groupExperiences.showExperienceDetails(new ArrayList<Experience>(), recomm);
		}
	}
	
	/**
	 * Creates a new RecommendationStatus. Either one depicting the status "Ignored" or "Open" depending on the current status of the recommendation.
	 * @param recommId the custom id of the recommendation this status is for.
	 */
	private void ignore(String recommId) {
		if (instance.getRecommendationStati() != null && instance.getRecommendationStati().get(instance.getBareJID()) != null) {
			RecommendationStatus recStatus = instance.getRecommendationStati().get(instance.getBareJID()).get(recommId);
			if (recStatus != null && recStatus.getStatus() == Status.IGNORED) {
				sendNewRecommendationStatus(new RecommendationStatus(Status.OPEN, instance.getBareJID(), recommId));
			} else {
				sendNewRecommendationStatus(new RecommendationStatus(Status.IGNORED, instance.getBareJID(), recommId));
			}
		} else {
			sendNewRecommendationStatus(new RecommendationStatus(Status.IGNORED, instance.getBareJID(), recommId));
		}
	}

	/**
	 * Creates a new RecommendationStatus. Either one depicting the status "Solved" or "Open" depending on the current status of the recommendation.
	 * @param recommId the custom id of the recommendation this status is for.
	 */
	private void markAsSolved(String recommId) {
		if (instance.getRecommendationStati() != null && instance.getRecommendationStati().get(instance.getBareJID()) != null) {
			RecommendationStatus recStatus = instance.getRecommendationStati().get(instance.getBareJID()).get(recommId);
			if (recStatus != null && recStatus.getStatus() == Status.SOLVED) {
				sendNewRecommendationStatus(new RecommendationStatus(Status.OPEN, instance.getBareJID(), recommId));
			} else {
				sendNewRecommendationStatus(new RecommendationStatus(Status.SOLVED, instance.getBareJID(), recommId));
			}
		} else {
			sendNewRecommendationStatus(new RecommendationStatus(Status.SOLVED, instance.getBareJID(), recommId));
		}
	}
	

	/**
	 * Publishes a recommendationstatus on all available spaces the recommendation lies on. 
	 * @param status the status to publish.
	 */
	private void sendNewRecommendationStatus(RecommendationStatus status) {
		RecommendationObject object = recommObjects.get(status.getRef());
		if (object != null && object.getTargetSpaces() != null && object.getTargetSpaces().size() > 0) {
			List<String> spaces = recommObjects.get(status.getRef()).getTargetSpaces();
			List<String> targetSpaces = new ArrayList<String>();
			for (String space:spaces) {
				if (instance.getSpacesMap().containsKey(space)) {
					targetSpaces.add(space);
				}
			}
			status.setPublisher(instance.getBareJID());
			publishStatus(status.toString(), targetSpaces);
		}
	}
	
	@Override
	protected void addRecommendation(final RecommendationPanel recomm){
		RecommendationObject rec = recommObjects.get(recomm.getCustomId());
		if (rec == null || !rec.getState()) return;
		super.addRecommendation(recomm);
		Map<String, RecommendationStatus> stati = instance.getRecommendationStati().get(instance.getBareJID());
		if (stati == null || stati.get(rec.getCustomId()) == null) {
			activityList.appendChild(recomm.getPanel());
		} else {
			RecommendationStatus status = stati.get(rec.getCustomId());
			switch (status.getStatus()) {
			case OPEN:
				activityList.appendChild(recomm.getPanel());
				break;
			case IGNORED:
				ignoredList.appendChild(recomm.getPanel());
				break;
			case SOLVED:
				solvedList.appendChild(recomm.getPanel());
				break;			
			}
		}
	}
	
	/**
	 * Resets the state of the input fields for a new experience.
	 */
	private void resetInput() {
		int childCount = starRatingStarsList.getChildCount();
		for (int j=0; j<childCount; j++) {
			Element child = (Element)starRatingStarsList.getChild(j);
			if (child != null) {
				child.removeClassName("starFilled");
			}
		}
		if (attachedEvidences != null && attachedEvidences.size() > 0) {

			boolean fileEvidenceAttached = false;
			for (Evidence ev:attachedEvidences) {
				if (ev instanceof FileEvidence) {
					fileEvidenceAttached = true;
					break;
				}
			}
			if (fileEvidenceAttached) {
				final ARAppServiceAsync service = GWT.create(ARAppService.class);
		
				service.deleteFiles(getFileEvidenceLocation(), new AsyncCallback<Void>() {
		
					@Override
					public void onFailure(Throwable arg0) {
					}
		
					@Override
					public void onSuccess(Void arg0) {
					}
				});
			}
			attachedEvidences.clear();
			
			evidencesLabel.setInnerText(instance.infoMessage.captureAttachedEvidences(0));
		}
		errorMessage.removeClassName("activeItem");
		customId = null;
		commentArea.setText("");
		
		rating = -1;
		comment = null;
		
		benefitInput.setValue(null);
		effortInput.setValue(null);
		if (benefit != null) {
			benefit.setValue(-1);
		}
		if (effort != null) {
			effort.setValue(-1);
		}
	}
	
	/**
	 * This method creates the experience from all entered information and then publishes it on the same space as the recomm is on.
	 * @param recommId The id of the recommendation.
	 */
	private void prepareExperience(final String recommId) {
		String userJid = instance.getBareJID();
		final Experience experience = new Experience(userJid, recommId, HasTimestamp.TIMESTAMPFORMAT.format(new Date()), null);
		if (customId != null) {
			experience.setCustomId(customId);
		} else {
			experience.setCustomId(ARApp.uuid());
		}
		experience.setSharingLevel(chosenSharingLevel);
		if (rating != -1) {
			experience.setRating(rating);
		}
		if (comment != null) {
			experience.setComment(comment);
		}
		if (benefit != null && benefit.getValue() != -1) {
			experience.setBenefit(benefit);
		}
		if (effort != null && effort.getValue() != -1) {
			experience.setEffort(effort);
		}
		experience.setEvidences(attachedEvidences);
		
		RecommendationObject rec = recommObjects.get(recommId);
		if (rec == null) return;
		List<String> spaceIds = new ArrayList<String>();
		spaceIds.addAll(instance.getSpacesMap().keySet());

		List<String> recommendationTargetSpaces = rec.getTargetSpaces();
		List<String> targetIds = new ArrayList<String>();
		List<String> targetSpaces = new ArrayList<String>();

		if (chosenSharingLevel == 5) {
			targetSpaces.add(instance.getBareJID().split("@")[0]);
		} else {
			if (recommendationTargetSpaces != null) {
				targetIds.addAll(recommendationTargetSpaces);
				targetSpaces.addAll(recommendationTargetSpaces);
			}		
	
			targetIds.removeAll(spaceIds);
			targetSpaces.removeAll(targetIds);
			
			if (targetSpaces.size() == 0) {
				return;
			}
		}
		boolean fileEvidenceAttached = false;
		for (Evidence ev:attachedEvidences) {
			if (ev instanceof FileEvidence) {
				fileEvidenceAttached = true;
				break;
			}
		}
		if (fileEvidenceAttached) {
			final ARAppServiceAsync service = GWT.create(ARAppService.class);
	
			service.saveFilesOnSpaces(getFileEvidenceLocation(), instance.getLoginInfos(), targetSpaces, new AsyncCallback<Void>(){
				
				@Override
				public void onFailure(Throwable arg0) {
				}
	
				@Override
				public void onSuccess(Void arg0) {
				}
			});
		}
		for (String id:targetSpaces) {
			if (fileEvidenceAttached) {
				for (Evidence ev:attachedEvidences) {
					if (ev instanceof FileEvidence) {
						((FileEvidence)ev).setLocation(id);
					}
				}
			}
			if (chosenSharingLevel == 5) {
				publishExperienceOnPrivateSpace(experience);
				break;
			} else {
				publishExperience(id, experience);
			}
		}
		resetInput();
	}
	

	/**
	 * This method publishes the recommendation on the own private space. If it is not available it will be created first.
	 * @param instance an instance of the class DiscussionView.
	 */
	public native void publishExperienceOnPrivateSpace(Experience experience) /*-{
		var that = this;
		$wnd.spaceHandler.getDefaultSpace(function(result){
			if (!result || result == null) {
				$wnd.spaceHandler.createDefaultSpace(function(res) {
					var config = res.generateSpaceConfiguration();
					config.setPersistenceType($wnd.SpacesSDK.PersistenceType.ON);
					$wnd.spaceHandler.configureSpace(res.getId(), config, function(){}, function(){});
					$wnd.dataHandler.registerSpace(res.getId());
					var dataObject = experience.@de.imc.mirror.arapp.client.Experience::toDataObject()();					
					$wnd.dataHandler.publishDataObject(dataObject, res.getId(), function() {}, function() {});
				}, function(error){});
			} else {
					var dataObject = experience.@de.imc.mirror.arapp.client.Experience::toDataObject()();
					$wnd.dataHandler.publishDataObject(dataObject, result.getId(), function() {}, function() {});
			}
			}, function(error){});
	}-*/;
	
	/**
	 * This method publishes an experience.
	 * @param spaceId The id of the space to publish the experience on.
	 * @param experienceString The experience as a String.
	 */
	private native void publishExperience(String spaceId, Experience experience) /*-{
		var dataObject = experience.@de.imc.mirror.arapp.client.Experience::toDataObject()();
		$wnd.dataHandler.publishDataObject(dataObject, spaceId, function() {
//			alert("Experience published successfully");
//			instance.@de.imc.mirror.arapp.client.view.CaptureTab::reset()();
		}, function() {
//			alert("There was an error. Please try again");
		});
	}-*/;
	
	public void updateRecommendationStatus(RecommendationStatus status) {
		if (status.getUser().equals(instance.getBareJID())) {
			RecommendationPanel recomm = recomms.get(status.getRef());
			RecommendationObject rec = recommObjects.get(status.getRef());
			if (rec == null || !rec.getState()) return;
			switch (status.getStatus()) {
			case OPEN:
				activityList.appendChild(recomm.getPanel());
				break;
			case IGNORED:
				ignoredList.appendChild(recomm.getPanel());
				break;
			case SOLVED:
				solvedList.appendChild(recomm.getPanel());
				break;			
			}	
		}
		super.update();
	}
	
	public void reset() {
		super.reset();
		activityList.removeAllChildren();
		ignoredList.removeAllChildren();
		solvedList.removeAllChildren();
	}
}
