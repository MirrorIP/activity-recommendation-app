package de.imc.mirror.arapp.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Benefit;
import de.imc.mirror.arapp.client.Effort;
import de.imc.mirror.arapp.client.Entry;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.FileEvidence;
import de.imc.mirror.arapp.client.HasSpacesList;
import de.imc.mirror.arapp.client.HasTimestamp;
import de.imc.mirror.arapp.client.Parser;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.RecommendationObjectBuilder;
import de.imc.mirror.arapp.client.SessionObject;
import de.imc.mirror.arapp.client.RecommendationObject.TaskType;
import de.imc.mirror.arapp.client.HasEvidences;
import de.imc.mirror.arapp.client.localization.DiscussionEntryMessage;
import de.imc.mirror.arapp.client.service.ARAppService;
import de.imc.mirror.arapp.client.service.ARAppServiceAsync;
import de.imc.mirror.arapp.client.view.popup.InvitedPersonsPopup;

public class DiscussionView extends View implements HasEvidences, HasSpacesList{
	
	protected enum ElementIds {
		PARTICIPANTSLIST("discussionSessionParticipantsList"),
		SELECTNEWMANAGERBUTTON("discussionSessionManageButton"),
		TARGETGROUPS("discussionSessionTargetGroups"),
		
		LEAVEBUTTON("discussionSessionTargetGroupsLeaveButton"),
		
		TARGETGROUPSCHANGEBUTTON("discussionSessionTargetGroupsEditButton"),
		TARGETGROUPSSEARCHBOX("discussionSessionTargetGroupsSearchInput"),
		TARGETGROUPSSEARCHBUTTON("discussionSessionTargetGroupsSearchButton"),
		TARGETGROUPSCANCELBUTTON("discussionSessionTargetGroupsCancelButton"),
		TARGETGROUPSSUBMITBUTTON("discussionSessionTargetGroupsSubmitButton"),

		DISCUSSIONTITLECANCELBUTTON("recommendationTitleCancelButton"),
		DISCUSSIONTITLEEDITBUTTON("discussionSessionTitleEditButton"),
		DISCUSSIONTITLEINPUTAREA("discussionSessionTitleInput"),
		DISCUSSIONTITLE("discussionSessionTitleLabel"),
		DISCUSSIONTITLESUBMITBUTTON("recommendationTitleSubmitButton"),
		
		DISCUSSIONISSUE("discussionSessionIssue"),
		DISCUSSIONISSUEEDITBUTTON("discussionSessionIssueEditButton"),
		DISCUSSIONISSUEINPUTFIELD("discussionSessionIssueInput"),
		DISCUSSIONISSUECANCELBUTTON("discussionSessionIssueCancelButton"),
		DISCUSSIONISSUESUBMITBUTTON("discussionSessionIssueSubmitButton"),

		DISCUSSIONSOLUTION("discussionSessionSolution"),
		DISCUSSIONSOLUTIONEDITBUTTON("discussionSessionSolutionEditButton"),
		DISCUSSIONSOLUTIONINPUTFIELD("discussionSessionSolutionInput"),
		DISCUSSIONSOLUTIONCANCELBUTTON("discussionSessionSolutionCancelButton"),
		DISCUSSIONSOLUTIONSUBMITBUTTON("discussionSessionSolutionSubmitButton"),
		
		MINUTESTABBUTTON("tabDiscussionSessionMinutes"),
		EVIDENCESTABBUTTON("tabDiscussionSessionEvidences"),
		EVALUATIONTABSBUTTON("tabDiscussionSessionEvaluation"),
		
		MINUTESTAB("discussionSessionMinutes"),
		MINUTESLIST("discussionSessionMinutesList"),
		
		EVIDENCESTAB("discussionSessionEvidences"),
		EVIDENCESUPLOADBUTTON("discussionSessionEvidencesUploadButton"),
		EVIDENCESBROWSEBUTTON("discussionSessionEvidencesBrowseButton"),		
		EVIDENCESLIST("discussionSessionEvidencesAttachedEvidences"),
		
		EVALUATIONTAB("discussionSessionEvaluation"),
		EVALUATIONRATING("discussionSessionEvaluationRatingText"),
		EVALUATIONRATINGINPUT("discussionSessionEvaluationRatingTextInput"),
		EVALUATIONRATINGEDITBUTTON("discussionSessionEvaluationRatingTextEditButton"),
		EVALUATIONRATINGCANCELBUTTON("discussionSessionEvaluationRatingTextCancelButton"),
		EVALUATIONRATINGSUBMITBUTTON("discussionSessionEvaluationRatingTextSubmitButton"),
		
		EVALUATIONEFFORT("discussionSessionEvaluationEffort"),
		EVALUATIONEFFORTLISTBOX("discussionSessionEvaluationEffortTypeSelect"),
		EVALUATIONEFFORTUNITTEXT("discussionSessionEvaluationEffortDisplayInput"),
		EVALUATIONEFFORTTEXTINPUT("discussionSessionEvaluationEffortTextInput"),
		EVALUATIONEFFORTEDITBUTTON("discussionSessionEvaluationEffortEditButton"),
		EVALUATIONEFFORTCANCELBUTTON("discussionSessionEvaluationEffortCancelButton"),
		EVALUATIONEFFORTSUBMITBUTTON("discussionSessionEvaluationEffortSubmitButton"),
		
		EVALUATIONBENEFIT("discussionSessionEvaluationBenefit"),
		EVALUATIONBENEFITLISTBOX("discussionSessionEvaluationBenefitTypeSelect"),
		EVALUATIONBENEFITUNITTEXT("discussionSessionEvaluationBenefitDisplayInput"),
		EVALUATIONBENEFITTEXTINPUT("discussionSessionEvaluationBenefitTextInput"),
		EVALUATIONBENEFITEDITBUTTON("discussionSessionEvaluationBenefitEditButton"),
		EVALUATIONBENEFITCANCELBUTTON("discussionSessionEvaluationBenefitCancelButton"),
		EVALUATIONBENEFITSUBMITBUTTON("discussionSessionEvaluationBenefitSubmitButton"),
		
		TASKTYPELISTBOX("discussionSessionRecommendationTypeSelect"),		
		DISCUSSIONGROUPLABEL("discussionSessionDiscussionGroup"),		
		PUBLISHBUTTON("publishRecommendationButton"),
		
		SHOWINVITEDPERSONSPOPUP("discussionSessionShowInvitedPersonsPopupButton"),
		CREATESPACEBUTTON("discussionSessionTargetGroupsCreateSpaceButton");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Element issuePanel;
	private Label issueLabel;
	private Button issueEditButton;
	private Button issueSubmitButton;
	private Button issueCancelButton;
	private TextArea issueTextArea;

	private Element solutionPanel;
	private Label solutionLabel;
	private Button solutionEditButton;
	private Button solutionSubmitButton;
	private Button solutionCancelButton;
	private TextArea solutionTextArea;
	
	private Element titlePanel;
	private Label title;
	private Button titleCancelButton;
	private Button titleEditButton;
	private Button titleSubmitButton;
	private TextArea titleTextArea;

	private TextArea benefitTextArea;
	private ListBox benefitListBox;
	private TextBox benefitUnitTextBox;
	private Element benefitTypeLabel;
	private Element benefitUnitLabel;
	private Element benefitDescriptionLabel;

	private TextArea effortTextArea;
	private ListBox effortListBox;
	private TextBox effortUnitTextBox;	
	private Element effortTypeLabel;
	private Element effortUnitLabel;
	private Element effortDescriptionLabel;
	
	private TextArea ratingTextArea;
	
	private Element minutesTab;
	private Button minutesTabButton;
	private Element minutesList;
	
	private Element evidencesTab;
	private Button evidencesTabButton;
	private Element evidencesList;
	private Button evidenceUploadButton;
	private Button evidenceBrowseButton;
	
	private Element evaluationTab;
	private Button evaluationTabButton;

	private Element participantsList;
	private Button selectNewManagerButton;
	
	
	private Element effortPanel;
	private Element ratingPanel;
	private Element benefitPanel;
	
	private Element ratingLabel;

	private ListBox taskTypeList;
	

	private Element targetGroupsPanel;
	private TextBox targetGroupsSearchBox;
	private Button targetGroupsSearchButton;
	private Button targetGroupsCancelButton;
	private Button targetGroupsChangeButton;
	private Button targetGroupsSubmitButton;
	private Element targetGroupsCheckBoxList;
	private Element targetGroupsList;
	
	private Map<String, SimpleCheckBox> checkBoxes;
	private String targetGroups;
	
	private Button leaveButton;
	
	private Map<String, Element> targetSpacesListElements;
	
	private boolean isModerator = false;
	
	private Button publishButton;
	
	private String moderatorJid;
	private String discussionSpaceId;
	
	private List<String> targetSpaces;
	private List<String> participants;
	private Map<Integer, Entry> minuteEntries;
	private List<Evidence> attachedEvidences;
	
	private Map<String, JavaScriptObject> dataObjects = new HashMap<String, JavaScriptObject>();
	
	private int currentMinuteId = 0;
	private String ref;
	private String discussionGroup;
	
	private boolean containsFileEvidence = false;
	private String customId;
	
	private long latestReceivedTimestamp;
	private long latestLocalTimestamp;
	
	private boolean release = true;

	private Label discussionGroupLabel;
	
	private TaskType taskType;
	
	private Benefit benefit;
	private Effort effort;
	private Element evidencesLabel;
	
	private DiscussionEntryMessage discussionEntryMessage;

	public DiscussionView(final ARApp instance) {
		super(instance);	
		discussionEntryMessage = GWT.create(DiscussionEntryMessage.class);
		initialiseTime();
		build();
	};

	
	@Override
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
				case DISCUSSIONISSUE:
					NodeList<Element> elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().equals("editableContent")) {
							issuePanel = child;
						} else if (child.getClassName().equals("blockContent")) {
							issueLabel = Label.wrap(child);
						}
					}
					break;
				case DISCUSSIONISSUECANCELBUTTON:
					issueCancelButton = Button.wrap(elem);
					issueCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							issuePanel.removeClassName("editState");
						}
					});
					break;
				case DISCUSSIONISSUEEDITBUTTON:
					issueEditButton = Button.wrap(elem);
					issueEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							issuePanel.addClassName("editState");
							if (issueLabel.getElement().getInnerHTML().equals(discussionEntryMessage.emptyIssueHtml())) {
								issueTextArea.setText("");
							} else {
								issueTextArea.setText(issueLabel.getText());
							}
							issueTextArea.setFocus(true);
						}
					});
					break;
				case DISCUSSIONISSUEINPUTFIELD:
					issueTextArea = TextArea.wrap(elem);
					break;
				case DISCUSSIONISSUESUBMITBUTTON:
					issueSubmitButton = Button.wrap(elem);
					issueSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							issuePanel.removeClassName("editState");
							if (issueTextArea.getText().replaceAll(" ", "").equals("") && discussionEntryMessage.emptyIssueHtml().contains(issueLabel.getText())) {
								return;
							} else if (!issueTextArea.getText().equals(issueLabel.getText())) {
								String newIssue = issueTextArea.getText().replaceAll(" ", "");
								if (newIssue.equals("")) {
									newIssue = discussionEntryMessage.emptyIssueHtml();
									issueLabel.getElement().setInnerHTML(discussionEntryMessage.emptyIssueHtml());
								} else {
									newIssue = issueTextArea.getText();
									issueLabel.setText(issueTextArea.getText());
								}
								sendNewEntry(discussionEntryMessage.issueEntry(newIssue));
							}
							
						}
					});
					break;
				case DISCUSSIONSOLUTION:
					elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().equals("editableContent")) {
							solutionPanel = child;
						} else if (child.getClassName().equals("blockContent")) {
							solutionLabel = Label.wrap(child);
						}
					}
					break;
				case DISCUSSIONSOLUTIONCANCELBUTTON:
					solutionCancelButton = Button.wrap(elem);
					solutionCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							solutionPanel.removeClassName("editState");
						}
					});
					break;
				case DISCUSSIONSOLUTIONEDITBUTTON:
					solutionEditButton = Button.wrap(elem);
					solutionEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							solutionPanel.addClassName("editState");
							if (solutionLabel.getElement().getInnerHTML().equals(discussionEntryMessage.emptySolutionHtml())) {
								solutionTextArea.setText("");
							} else {
								solutionTextArea.setText(solutionLabel.getText());
							}
							solutionTextArea.setFocus(true);
						}
					});
					break;
				case DISCUSSIONSOLUTIONINPUTFIELD:
					solutionTextArea = TextArea.wrap(elem);
					break;
				case DISCUSSIONSOLUTIONSUBMITBUTTON:
					solutionSubmitButton = Button.wrap(elem);
					solutionSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							solutionPanel.removeClassName("editState");
							if (solutionTextArea.getText().replaceAll(" ", "").equals("") && discussionEntryMessage.emptySolutionHtml().contains(solutionLabel.getText())) {
								return;
							} else if (!solutionTextArea.getText().equals(solutionLabel.getText())) {
								String newSolution = solutionTextArea.getText().replaceAll(" ", "");
								if (newSolution.equals("")) {
									newSolution = discussionEntryMessage.emptySolutionHtml();
									solutionLabel.getElement().setInnerHTML(discussionEntryMessage.emptySolutionHtml());
								} else {
									newSolution = solutionTextArea.getText();
									solutionLabel.setText(solutionTextArea.getText());
								}
								sendNewEntry(discussionEntryMessage.solutionEntry(newSolution));
							}
						}
					});
					break;
				case DISCUSSIONTITLE:
					title = Label.wrap(elem);
					titlePanel = title.getElement().getParentElement().getParentElement();
					break;
				case EVALUATIONBENEFIT:
					benefitPanel = elem.getElementsByTagName("div").getItem(0);
					

					benefitTypeLabel = benefitPanel.getElementsByTagName("span").getItem(0);
					benefitUnitLabel = benefitPanel.getElementsByTagName("span").getItem(1);
					NodeList<Element> divs = benefitPanel.getElementsByTagName("div");
					for (int j=0; j<divs.getLength(); j++) {
						Element child = divs.getItem(j);
						if (child.getClassName().equalsIgnoreCase("blockContent")) {
							benefitDescriptionLabel = child;
							break;
						}
					}
					break;
				case EVALUATIONBENEFITLISTBOX:
					benefitListBox = ListBox.wrap(elem);
					benefitListBox.clear();
					benefitListBox.addItem("Do not measure.");
					for (Benefit benefit:Benefit.values()) {
						benefitListBox.addItem(benefit.getDisplay(), benefit.getId());
					}
					benefitListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							int index = benefitListBox.getSelectedIndex();
							if (index > 0) {
								Benefit benefit = Benefit.getBenefit(benefitListBox.getValue(index));
								benefitTextArea.setText(benefit.getDescription());
								benefitUnitTextBox.setText(benefit.getDisplay());
								
								benefitTextArea.getElement().removeAttribute("disabled");
								benefitUnitTextBox.getElement().removeAttribute("disabled");
							} else {
								benefitTextArea.setText("");
								benefitUnitTextBox.setText("");		
								
								benefitTextArea.getElement().setAttribute("disabled", "disabled");
								benefitUnitTextBox.getElement().setAttribute("disabled", "disabled");				
							}
						}
					});
					break;
				case EVALUATIONBENEFITTEXTINPUT:
					benefitTextArea = TextArea.wrap(elem);
					break;
				case EVALUATIONBENEFITUNITTEXT:
					benefitUnitTextBox = TextBox.wrap(elem);
					break;
				case EVALUATIONEFFORT:
					effortPanel = elem.getElementsByTagName("div").getItem(0);

					effortTypeLabel = effortPanel.getElementsByTagName("span").getItem(0);
					effortUnitLabel = effortPanel.getElementsByTagName("span").getItem(1);
					divs = effortPanel.getElementsByTagName("div");
					for (int j=0; j<divs.getLength(); j++) {
						Element child = divs.getItem(j);
						if (child.getClassName().equalsIgnoreCase("blockContent")) {
							effortDescriptionLabel = child;
							break;
						}
					}
					break;
				case EVALUATIONEFFORTLISTBOX:
					effortListBox = ListBox.wrap(elem);
					effortListBox.clear();
					effortListBox.addItem(instance.infoMessage.listBoxDoNotMeasureEntry());
					for (Effort effort:Effort.values()) {
						effortListBox.addItem(effort.getDisplay(), effort.getId());
					}
					effortListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							int index = effortListBox.getSelectedIndex();
							if (index > 0) {
								Effort effort = Effort.getEffort(effortListBox.getValue(index));
								effortTextArea.setText(effort.getDescription());
								effortUnitTextBox.setText(effort.getDisplay());
								
								effortTextArea.getElement().removeAttribute("disabled");
								effortUnitTextBox.getElement().removeAttribute("disabled");
							} else {
								effortTextArea.setText("");
								effortUnitTextBox.setText("");
								
								effortTextArea.getElement().setAttribute("disabled", "disabled");
								effortUnitTextBox.getElement().setAttribute("disabled", "disabled");
							}
						}
					});
					break;
				case EVALUATIONEFFORTTEXTINPUT:
					effortTextArea = TextArea.wrap(elem);
					break;
				case EVALUATIONEFFORTUNITTEXT:
					effortUnitTextBox = TextBox.wrap(elem);
					break;
				case EVALUATIONRATING:
					ratingPanel = elem.getElementsByTagName("div").getItem(0);
					divs = ratingPanel.getElementsByTagName("div");
					for (int j=0; j<divs.getLength(); j++) {
						Element child = divs.getItem(j);
						if (child.getClassName().equalsIgnoreCase("blockContent")) {
							ratingLabel = child;
							break;
						}
					}	
					break;
				case EVALUATIONRATINGINPUT:
					ratingTextArea = TextArea.wrap(elem);
					break;
				case EVALUATIONTAB:
					evaluationTab = elem;
					break;
				case EVALUATIONTABSBUTTON:
					evaluationTabButton = Button.wrap(elem);
					evaluationTabButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							evaluationTabButton.getElement().addClassName("activeItem");
							evidencesTabButton.getElement().removeClassName("activeItem");
							minutesTabButton.getElement().removeClassName("activeItem");

							evaluationTab.addClassName("activeItem");
							evidencesTab.removeClassName("activeItem");
							minutesTab.removeClassName("activeItem");
						}
					});
					break;
				case EVIDENCESBROWSEBUTTON:
					evidenceBrowseButton = Button.wrap(elem);
					evidenceBrowseButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getAttachEvidenceDialog().showPopup(DiscussionView.this, attachedEvidences);
						}
					});
					break;
				case EVIDENCESLIST:
					evidencesList = elem.getElementsByTagName("tbody").getItem(0);
					elems = elem.getElementsByTagName("label");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().contains("blockContent")) {
							evidencesLabel = child;
						}
					}
					break;
				case EVIDENCESTAB:
					evidencesTab = elem;
					break;
				case EVIDENCESTABBUTTON:
					evidencesTabButton = Button.wrap(elem);
					evidencesTabButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							evidencesTabButton.getElement().addClassName("activeItem");
							minutesTabButton.getElement().removeClassName("activeItem");
							evaluationTabButton.getElement().removeClassName("activeItem");

							evidencesTab.addClassName("activeItem");
							minutesTab.removeClassName("activeItem");
							evaluationTab.removeClassName("activeItem");
						}
					});
					break;
				case EVIDENCESUPLOADBUTTON:
					evidenceUploadButton = Button.wrap(elem);
					evidenceUploadButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getCreateEvidencePopup().showPopup(DiscussionView.this);
						}
					});
					break;
				case MINUTESLIST:
					minutesList = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case MINUTESTAB:
					minutesTab = elem;
					break;
				case MINUTESTABBUTTON:
					minutesTabButton = Button.wrap(elem);
					minutesTabButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							minutesTabButton.getElement().addClassName("activeItem");
							evidencesTabButton.getElement().removeClassName("activeItem");
							evaluationTabButton.getElement().removeClassName("activeItem");

							minutesTab.addClassName("activeItem");
							evidencesTab.removeClassName("activeItem");
							evaluationTab.removeClassName("activeItem");
						}
					});
					break;
				case PARTICIPANTSLIST:
					participantsList = elem.getElementsByTagName("ul").getItem(0);
					participantsList.removeAllChildren();
					break;
				case SELECTNEWMANAGERBUTTON:
					selectNewManagerButton = Button.wrap(elem);
					selectNewManagerButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (isModerator) {
								List<String> users = new ArrayList<String>();
								users.addAll(participants);
								users.remove(moderatorJid);
								instance.getManageDiscussionPopup().showPopup(moderatorJid, users);
							}
						}
					});
					break;
				case TARGETGROUPS:
					targetGroupsPanel = elem.getElementsByTagName("div").getItem(0);
					
					targetGroupsCheckBoxList = elem.getElementsByTagName("tbody").getItem(0);
					targetGroupsCheckBoxList.removeAllChildren();

					
					targetGroupsList = elem.getElementsByTagName("ul").getItem(0);
					targetGroupsList.removeAllChildren();
					break;
				case TARGETGROUPSCANCELBUTTON:
					targetGroupsCancelButton = Button.wrap(elem);
					targetGroupsCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							targetGroupsPanel.removeClassName("editState");
						}
					});
					break;
				case TARGETGROUPSCHANGEBUTTON:
					targetGroupsChangeButton = Button.wrap(elem);
					targetGroupsChangeButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (isModerator) {
								buildSpacesList();
								for (SimpleCheckBox checkBox:checkBoxes.values()) {
									if (targetSpaces.contains(checkBox.getFormValue())) {
										checkBox.setValue(true);
									} else {
										checkBox.setValue(false);
									}
								}
								targetGroupsPanel.addClassName("editState");
							}
						}
					});
					break;
				case TARGETGROUPSSEARCHBOX:
					targetGroupsSearchBox = TextBox.wrap(elem);
					targetGroupsSearchBox.addKeyDownHandler(new KeyDownHandler() {
						
						@Override
						public void onKeyDown(KeyDownEvent event) {
							if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
								String searchString = targetGroupsSearchBox.getText();
								for (SimpleCheckBox checkBox:checkBoxes.values()) {
									Element trElement = checkBox.getElement().getParentElement().getParentElement();
									Map<String, String> spaces = instance.getSpacesMap();
									String space = spaces.get(checkBox.getFormValue());
									if (searchString.length() == 0 || space.toLowerCase().contains(searchString.toLowerCase())) {
										trElement.getStyle().clearDisplay();
									} else {
										trElement.getStyle().setDisplay(Display.NONE);
									}
								}
							}
						}
					});
					break;
				case TARGETGROUPSSEARCHBUTTON:
					targetGroupsSearchButton = Button.wrap(elem);
					targetGroupsSearchButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							String searchString = targetGroupsSearchBox.getText();
							for (SimpleCheckBox checkBox:checkBoxes.values()) {
								Element trElement = checkBox.getElement().getParentElement().getParentElement();
								Map<String, String> spaces = instance.getSpacesMap();
								String space = spaces.get(checkBox.getFormValue());
								if (searchString.length() == 0 || space.toLowerCase().contains(searchString.toLowerCase())) {
									trElement.getStyle().clearDisplay();
								} else {
									trElement.getStyle().setDisplay(Display.NONE);
								}
							}
						}
					});
					break;
				case TARGETGROUPSSUBMITBUTTON:
					targetGroupsSubmitButton = Button.wrap(elem);
					targetGroupsSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							targetGroupsPanel.removeClassName("editState");
							
							updateTargetGroupsList();
						}
					});
					break;
				case LEAVEBUTTON:
					leaveButton = Button.wrap(elem);
					leaveButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (isModerator && participants.size() > 1) {
								if (!Window.confirm("Do you really want to leave without assigning a new Moderator for this discussion?")){
									return;
								}
							}
							if (discussionSpaceId != null) {
								participants.remove(instance.getBareJID());
								sendNewEntry(discussionEntryMessage.leaveMessage(instance.getDisplayNameForJid(instance.getBareJID())));
								discussionSpaceId = null;
							}
							instance.leaveDiscussion();
						}
					});
					break;
				case DISCUSSIONTITLECANCELBUTTON:
					titleCancelButton = Button.wrap(elem);
					titleCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							titlePanel.removeClassName("editState");
						}
					});
					break;
				case DISCUSSIONTITLEEDITBUTTON:
					titleEditButton = Button.wrap(elem);
					titleEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							titlePanel.addClassName("editState");
							titleTextArea.setText(title.getText());	
							titleTextArea.setFocus(true);
						}
					});
					break;
				case DISCUSSIONTITLEINPUTAREA:
					titleTextArea = TextArea.wrap(elem);
					break;
				case DISCUSSIONTITLESUBMITBUTTON:
					titleSubmitButton = Button.wrap(elem);
					titleSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							titlePanel.removeClassName("editState");
							if (!titleTextArea.getText().equals(title.getText()) && !titleTextArea.getText().replaceAll(" ", "").equals("")) {
								title.setText(titleTextArea.getText());
								sendNewEntry(discussionEntryMessage.titleEntry(titleTextArea.getText()));
							}
						}
					});
					break;
				case EVALUATIONBENEFITCANCELBUTTON:
					Button benefitCancelButton = Button.wrap(elem);
					benefitCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							benefitPanel.removeClassName("editState");
						}
					});
					break;
				case EVALUATIONBENEFITEDITBUTTON:
					Button benefitEditButton = Button.wrap(elem);
					benefitEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							benefitPanel.addClassName("editState");
							benefitTextArea.setFocus(true);
						}
					});
					break;
				case EVALUATIONBENEFITSUBMITBUTTON:
					Button benefitSubmitButton = Button.wrap(elem);
					benefitSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {

							int index = benefitListBox.getSelectedIndex();
							benefitPanel.removeClassName("editState");
							if (benefit != null && index != 0 
									&& benefit.getId().equals(benefitListBox.getValue(index))
									&& benefit.getDescription().equals(benefitTextArea.getText()) 
									&& benefit.getDisplay().equals(benefitUnitTextBox.getText())) {
								return;
							} else if (benefit == null && index == 0) {
								return;
							}
								
							if (index > 0) {
								benefit = Benefit.getBenefit(benefitListBox.getValue(index));
								benefit.setDescription(benefitTextArea.getText());
								benefit.setDisplay(benefitUnitTextBox.getText());
							} else {
								benefit = null;
							}

							benefitTypeLabel.setInnerHTML(benefitListBox.getItemText(index));
							benefitUnitLabel.setInnerHTML(benefitUnitTextBox.getText());
							benefitDescriptionLabel.setInnerHTML(benefitTextArea.getText());
							sendNewEntry(discussionEntryMessage.benefitChange(benefitListBox.getItemText(index), benefitTextArea.getText()));
							
						}
					});
					break;
				case EVALUATIONEFFORTCANCELBUTTON:
					Button effortCancelButton = Button.wrap(elem);
					effortCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							effortPanel.removeClassName("editState");
						}
					});
					break;
				case EVALUATIONEFFORTEDITBUTTON:
					Button effortEditButton = Button.wrap(elem);
					effortEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							effortPanel.addClassName("editState");
							effortTextArea.setFocus(true);
						}
					});
					break;
				case EVALUATIONEFFORTSUBMITBUTTON:
					Button ratingEffortButton = Button.wrap(elem);
					ratingEffortButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							int index = effortListBox.getSelectedIndex();
							effortPanel.removeClassName("editState");
							
							if (effort != null && index != 0 
									&& effort.getId().equals(effortListBox.getValue(index))
									&& effort.getDescription().equals(effortTextArea.getText()) 
									&& effort.getDisplay().equals(effortUnitTextBox.getText())) {
								return;
							} else if (effort == null && index == 0) {
								return;
							}
							
							if (index > 0) {
								effort = Effort.getEffort(effortListBox.getValue(index));
								effort.setDescription(effortTextArea.getText());
								effort.setDisplay(effortUnitTextBox.getText());
							} else {
								effort = null;
							}
							
							effortTypeLabel.setInnerHTML(effortListBox.getItemText(index));
							effortUnitLabel.setInnerHTML(effortUnitTextBox.getText());
							effortDescriptionLabel.setInnerHTML(effortTextArea.getText());
							sendNewEntry(discussionEntryMessage.effortChange(effortListBox.getItemText(index), effortTextArea.getText()));
						}
					});
					break;
				case EVALUATIONRATINGCANCELBUTTON:
					Button ratingCancelButton = Button.wrap(elem);
					ratingCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							ratingPanel.removeClassName("editState");
						}
					});
					break;
				case EVALUATIONRATINGEDITBUTTON:
					Button ratingEditButton = Button.wrap(elem);
					ratingEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							ratingPanel.addClassName("editState");
							ratingTextArea.setFocus(true);
						}
					});
					break;
				case EVALUATIONRATINGSUBMITBUTTON:
					Button ratingSubmitButton = Button.wrap(elem);
					ratingSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {

							if (!ratingLabel.getInnerHTML().equals(ratingTextArea.getText())) {
								ratingLabel.setInnerHTML(ratingTextArea.getText());
								sendNewEntry(discussionEntryMessage.ratingChange(ratingTextArea.getText()));
							}					
							ratingPanel.removeClassName("editState");			
						}
					});
					break;
				case PUBLISHBUTTON:
					publishButton = Button.wrap(elem);
					publishButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (isModerator) {
								List<String> problems = getStateProblems();
								if (problems.size() > 0) {
									instance.getPublishWizard().setProblems(problems);
									showPublishWizard();
								} else {
									showPublishWizard();
									sendPublishWizardNotice();
								}
							}
						}
					});
					break;
				case TASKTYPELISTBOX:
					taskTypeList = ListBox.wrap(elem);
					taskTypeList.clear();
					taskTypeList.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							taskType = TaskType.getTaskType(taskTypeList.getValue(taskTypeList.getSelectedIndex()));
							sendCompleteInformation();
						}
					});
					for (TaskType type:TaskType.values()) {
						taskTypeList.addItem(type.getDisplay(), type.getId());
					}
					break;
				case DISCUSSIONGROUPLABEL:
					discussionGroupLabel = Label.wrap(elem.getElementsByTagName("div").getItem(0));
					break;
				case SHOWINVITEDPERSONSPOPUP:
					Button.wrap(elem).addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getInvitedPersonsPopup().showPopup(moderatorJid, participants);
						}
					});
					break;
				case CREATESPACEBUTTON:
					Button.wrap(elem).addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							instance.getCreateSpacePopup().showPopup();
						}
						
					});
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}
		
		selectNewManagerButton.setVisible(false);
		targetGroupsChangeButton.setVisible(false);
		publishButton.setVisible(false);
		
		targetSpaces = new ArrayList<String>();
		attachedEvidences = new ArrayList<Evidence>();
		minuteEntries = new HashMap<Integer, Entry>();
		targetSpacesListElements = new HashMap<String, Element>();
		participants = new ArrayList<String>();
	}
	
	public void attachEvidences(List<Evidence> attachedEvidences) {
		this.attachedEvidences.clear();
		if (attachedEvidences != null) {
			this.attachedEvidences.addAll(attachedEvidences);
		}
		containsFileEvidence = false;
		for (Evidence ev:this.attachedEvidences) {
			if (ev instanceof FileEvidence) {
				containsFileEvidence = true;
				break;
			}
		}
		evidencesLabel.setInnerText(instance.infoMessage.discussionAttachedEvidences(attachedEvidences.size()));
		sendCompleteInformation();
	}
	

	public void attachCreatedEvidence(Evidence ev) {
		this.attachedEvidences.add(ev);
		if (!containsFileEvidence && ev instanceof FileEvidence) {
			containsFileEvidence = true;
		}
		evidencesLabel.setInnerText(instance.infoMessage.discussionAttachedEvidences(attachedEvidences.size()));
		sendCompleteInformation();
	}
	
	public String getFileEvidenceLocation() {
		return discussionSpaceId;
	}
	
	/**
	 * Creates a new sessionobject and publishes it which will lead to showing the publishing wizard for all discussionparticipants. 
	 */
	private void sendPublishWizardNotice() {
		JavaScriptObject obj = SessionObject.createNewShowPublishWizardSessionObject();
		publishObject(obj, discussionSpaceId, null);
	}
	
	/**
	 * Shows the publishing wizard.
	 */
	public void showPublishWizard() {
		instance.getPublishWizard().showPopup(createRecommendationObject(), targetGroups, isModerator);
	}
	
	public List<String> getStateProblems() {
		List<String> errors = new ArrayList<String>();
		if (titlePanel.getClassName().contains("editState")) {
			errors.add("title");
		}
		if (issuePanel.getClassName().contains("editState")) {
			errors.add("issue");
		}
		if (solutionPanel.getClassName().contains("editState")) {
			errors.add("solution");
		}
		if (ratingPanel.getClassName().contains("editState")) {
			errors.add("rating");
		}
		if (benefitPanel.getClassName().contains("editState")) {
			errors.add("benefit");
		}
		if (effortPanel.getClassName().contains("editState")) {
			errors.add("effort");
		}
		if (targetGroupsPanel.getClassName().contains("editState")) {
			errors.add("target groups");
		}
		
		List<String> result = new ArrayList<String>();
		if (!errors.isEmpty()) {
			result.add(instance.errorMessage.unsavedChanges(errors));
		}
		
		RecommendationObject recomm = createRecommendationObject();
		if (recomm.getTitle().isEmpty()) {
			result.add(instance.errorMessage.emptyTitle());
		}
		if (recomm.getIssue().isEmpty()) {
			result.add(instance.errorMessage.emptyIssue());
		}
		if (recomm.getRecommendedSolution().isEmpty()) {
			result.add(instance.errorMessage.emptySolution());
		}
		if (recomm.getRatingDescription().isEmpty()) {
			result.add(instance.errorMessage.emptyRatingDescription());
		}
		if (recomm.getBenefitRating() != null) {
			Benefit benefit = recomm.getBenefitRating();
			if (benefit.getDisplay().isEmpty() || benefit.getDescription().isEmpty()) {
				result.add(instance.errorMessage.emptyBenefit(benefit.getDisplay().isEmpty(), benefit.getDescription().isEmpty()));
			}
		}
		if (recomm.getEffortRating() != null) {
			Effort effort = recomm.getEffortRating();
			if (effort.getDisplay().isEmpty() || effort.getDescription().isEmpty()) {
				result.add(instance.errorMessage.emptyEffort(effort.getDisplay().isEmpty(), effort.getDescription().isEmpty()));
			}
		}
		if (recomm.getTargetSpaces() == null || recomm.getTargetSpaces().isEmpty()) {
			result.add(instance.errorMessage.noTargetSpacesChosen());
		}
		
		while(result.contains("")) {
			result.remove("");
		}
		
		return result;
	}
	
	/**
	 * If a vote was received it will be added to the already received votes. Also an entry in the minutes will be published.
	 * @param xml the sessionobject which contains the vote as a string.
	 */
	public void addVote(String xml) {
		if (isModerator) {
			Map<String, String> infos = SessionObject.getAllInformation(xml);
			String vote = infos.get("vote");
			sendNewEntry(discussionEntryMessage.voteEntry(instance.getDisplayNameForJid(infos.get("publisher")), vote));
			instance.getPublishWizard().addVote(vote);
		}
	}
	
	/**
	 * Adds a evidence to the evidencePanel.
	 * @param ev The evidence to add.
	 */
	private void updateEvidencePanel(final Evidence ev) {	
		final Element tr = Document.get().createTRElement();
		HTML.wrap(tr).addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				instance.getEvidenceVisualisationPopup().visualizeEvidence(ev);
			}
		});
		
		Element iconTD = Document.get().createTDElement();
		Element img = Document.get().createImageElement();
		img.setAttribute("src", "img/data-unknown.png");
		iconTD.appendChild(img);
		
		Element textTD = Document.get().createTDElement();
		textTD.setInnerHTML(ev.getType());
		
		Element dateTD = Document.get().createTDElement();
		dateTD.setInnerHTML(ev.getFormattedTimestamp(HasTimestamp.MEDIUMDATE));
		
		Element publisherTD = Document.get().createTDElement();
		publisherTD.setInnerHTML(ev.getPublisher());
		
		Element buttonTD = Document.get().createTDElement();
		HTML.wrap(buttonTD).addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				evidencesList.removeChild(tr);
				attachedEvidences.remove(ev);
				evidencesLabel.setInnerText(instance.infoMessage.discussionAttachedEvidences(attachedEvidences.size()));
				sendCompleteInformation();
				if (ev instanceof FileEvidence) {
					FileEvidence fe = (FileEvidence) ev;

					final ARAppServiceAsync service = GWT.create(ARAppService.class);
					
					service.deleteFile(discussionSpaceId, fe.getFileName(), new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable arg0) {
						}

						@Override
						public void onSuccess(Void arg0) {
						}
					});
				}
				event.getNativeEvent().stopPropagation();
			}
		});
		
		tr.appendChild(iconTD);
		tr.appendChild(textTD);
		tr.appendChild(publisherTD);
		tr.appendChild(dateTD);
		tr.appendChild(buttonTD);
		evidencesList.appendChild(tr);
	}
	
	/**
	 * If a discussion was closed, this function sends a notice to the server to delete the information when the discussion was started.
	 * @param discussion the id of the discussion.
	 */
	private void removeTime(String discussion) {
		final ARAppServiceAsync service = GWT.create(ARAppService.class);
		
		
		service.endDiscussion(discussion, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(Void arg0) {
			}
		});
	}
	
	/**
	 * Requests the current timestamp of the server.
	 */
	private void initialiseTime() {
		final ARAppServiceAsync service = GWT.create(ARAppService.class);
		
		final AsyncCallback<Long> timeCallback = new AsyncCallback<Long>() {

			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(Long arg0) {
				latestLocalTimestamp = new Date().getTime();
				latestReceivedTimestamp = arg0;
			}
		};
		service.getTime(timeCallback);
	}
	
	/**
	 * After receiving a new session object this method will update the received information.
	 * @param infos The received information.
	 */
	private void updateInfos(Map<String, String> infos) {
		if (infos.containsKey("targetGroups")) {
			targetGroups = infos.get("targetGroups");
			targetGroupsList.removeAllChildren();
			if (targetGroups.equals("")) {
				Element liElement = Document.get().createLIElement();
				//TODO
				liElement.setInnerHTML("None.");
				targetGroupsList.appendChild(liElement);
			} else {
				String[] spaces = targetGroups.split(",");
				
				for (String space:spaces) {
					Element liElement = Document.get().createLIElement();
					liElement.setInnerHTML(space);
					targetGroupsList.appendChild(liElement);
				}
			}
			
		}
		if (infos.containsKey("discussionGroup")) {
			discussionGroup = infos.get("discussionGroup");
			discussionGroupLabel.setText(discussionGroup);
		}
		if (infos.containsKey("moderator") && !infos.get("moderator").equals(moderatorJid)) {
			String oldMod = moderatorJid;
			moderatorJid = infos.get("moderator");
			if (moderatorJid.equals(instance.getBareJID()) && oldMod != null) {
				isModerator = true;
				setModeratorRights();
				revokeModeratorRightsOfPreviousModerator(oldMod, discussionSpaceId);
				
				long diff = new Date().getTime() - latestLocalTimestamp;
				String time = HasTimestamp.MEDIUMDATE.format(new Date(latestReceivedTimestamp + diff));
				
				sendNewEntry(discussionEntryMessage.moderatorChange(instance.getDisplayNameForJid(oldMod), instance.getDisplayNameForJid(instance.getBareJID()), time));
			} else if (moderatorJid.equals(instance.getBareJID())) {
				isModerator = true;
			} else {
				isModerator = false;

				selectNewManagerButton.setVisible(false);
				targetGroupsChangeButton.setVisible(false);
				publishButton.setVisible(false);
			}
			updateParticipantsLabel();
		}
	}
	
	/**
	 * If a sessionobject with type "complete" was received we update all infos with the included recommendation.
	 * @param recomm The received recommendation.
	 */
	private void updateInfosWithRecommendation(RecommendationObject recomm) {
		if (recomm.getIssue().equals("")) {
			issueLabel.getElement().setInnerHTML(discussionEntryMessage.emptyIssueHtml());
		} else {
			issueLabel.setText(recomm.getIssue());
		}
		if (recomm.getRecommendedSolution().equals("")) {
			solutionLabel.getElement().setInnerHTML(discussionEntryMessage.emptySolutionHtml());
		} else {
			solutionLabel.setText(recomm.getRecommendedSolution());
		}
		title.setText(recomm.getTitle());
		List<Entry> minutesEntries = recomm.getEntries();
		currentMinuteId = 0;
		minuteEntries.clear();
		for (Entry entry:minutesEntries) {
			minuteEntries.put(entry.getId(), entry);
			if (entry.getId() >= currentMinuteId) {
				currentMinuteId = entry.getId() + 1;
			}
		}
	
		minutesList.removeAllChildren();
		
		for (int i = 0; i<currentMinuteId; i++) {
			if (minuteEntries.get(i) == null) continue;
			Entry entry = minuteEntries.get(i);
			createNewMinutes(i, entry.getTimestamp(), entry.getMessage());
		}
		effort = recomm.getEffortRating();
		if (effort != null) {
			for (int i=1; i<effortListBox.getItemCount(); i++) {
				String id = effortListBox.getValue(i);
				if (effort.getId().equals(id)) {
					effortListBox.setSelectedIndex(i);
					effortUnitTextBox.setText(effort.getDisplay());
					effortTextArea.setText(effort.getDescription());
					
					effortTextArea.getElement().removeAttribute("disabled");
					effortUnitTextBox.getElement().removeAttribute("disabled");
					break;
				}
			}

			effortTypeLabel.setInnerHTML(Effort.getEffort(effort.getId()).getDisplay());
			effortUnitLabel.setInnerHTML(effort.getDisplay());
			effortDescriptionLabel.setInnerHTML(effort.getDescription());
		} else {
			effortListBox.setSelectedIndex(0);
			effortUnitTextBox.setText("");
			effortTextArea.setText("");
			
			effortTypeLabel.setInnerHTML(effortListBox.getItemText(0));
			effortUnitLabel.setInnerHTML("");
			effortDescriptionLabel.setInnerHTML("");

			effortTextArea.getElement().setAttribute("disabled", "disabled");
			effortUnitTextBox.getElement().setAttribute("disabled", "disabled");
		}
		benefit = recomm.getBenefitRating();
		if (benefit != null) {
			for (int i=1; i<benefitListBox.getItemCount(); i++) {
				String id = benefitListBox.getValue(i);
				if (benefit.getId().equals(id)) {
					benefitListBox.setSelectedIndex(i);
					benefitUnitTextBox.setText(benefit.getDisplay());
					benefitTextArea.setText(benefit.getDescription());
					
					benefitTextArea.getElement().removeAttribute("disabled");
					benefitUnitTextBox.getElement().removeAttribute("disabled");
					break;
				}
			}
			
			benefitTypeLabel.setInnerHTML(Benefit.getBenefit(benefit.getId()).getDisplay());
			benefitUnitLabel.setInnerHTML(benefit.getDisplay());
			benefitDescriptionLabel.setInnerHTML(benefit.getDescription());
			
		} else {
			benefitListBox.setSelectedIndex(0);
			benefitUnitTextBox.setText("");
			benefitTextArea.setText("");
			
			benefitTypeLabel.setInnerHTML(benefitListBox.getItemText(0));
			benefitUnitLabel.setInnerHTML("");
			benefitDescriptionLabel.setInnerHTML("");

			benefitTextArea.getElement().setAttribute("disabled", "disabled");
			benefitUnitTextBox.getElement().setAttribute("disabled", "disabled");
		}
		String ratingDescription = recomm.getRatingDescription();
		if (ratingDescription != null) {
			ratingTextArea.setText(ratingDescription);
			ratingLabel.setInnerHTML(ratingDescription);
		} else {
			ratingLabel.setInnerText(discussionEntryMessage.standardRatingText());
			ratingTextArea.setText(discussionEntryMessage.standardRatingText());
		}
		
		participants = recomm.getParticipants();
		instance.getInvitedPersonsPopup().update(moderatorJid, participants);
		updateParticipantsLabel();

		evidencesList.removeAllChildren();
		
		if (recomm.getRelatedEvidence() != null) {
			attachedEvidences.clear();
			for (Evidence ev:recomm.getRelatedEvidence()) {
				if (attachedEvidences.add(ev)) {
					updateEvidencePanel(ev);
				}
			}
			evidencesLabel.setInnerText(instance.infoMessage.discussionAttachedEvidences(attachedEvidences.size()));
		}
		if (recomm.getCustomId() != null) {
			customId = recomm.getCustomId();
		} else {
			customId = ARApp.uuid();
		}
		if (ref == null && recomm.getPreviousRevisions() != null) {
			ref = recomm.getPreviousRevisions();
		}
		this.release = recomm.getState();
		if (recomm.getTargetSpaces() != null) {
			targetSpaces = recomm.getTargetSpaces();
		} else {
			targetSpaces.clear();
		}
		taskType = recomm.getTaskType();
		if (taskType == null) {
			return;
		}
		switch (taskType) {
		case ACTIVITY:
			taskTypeList.setSelectedIndex(0);
			break;
		case BEHAVIOR:
			taskTypeList.setSelectedIndex(1);
			break;
		case LEARNING:
			taskTypeList.setSelectedIndex(2);
			break;
		default:
			break;
		
		}
	}
	
	/**
	 * Shows the buttons which only the moderator is allowed to see.
	 */
	private void setModeratorRights() {
		selectNewManagerButton.setVisible(true);
		targetGroupsChangeButton.setVisible(true);
		publishButton.setVisible(true);
	}
	
	/**
	 * Sends a new minute entry.
	 * @param entryText the text of the new entry.
	 */
	private void sendNewEntry(String entryText) {
		long diff = new Date().getTime() - latestLocalTimestamp;
		String timestamp = HasTimestamp.TIMESTAMPFORMAT.format(new Date(latestReceivedTimestamp + diff));
		Entry entry = new Entry(currentMinuteId, entryText, timestamp);
		minuteEntries.put(entry.getId(), entry);
		sendCompleteInformation();
	}
	
	/**
	 * Deletes a specific entry in the minutes.
	 * @param id the id of the entry to delete.
	 */
	public void deleteEntry(int id) {
		minuteEntries.remove(id);
		sendCompleteInformation();
	}
	
	/**
	 * Creates a new entry in the minutespanel.
	 * @param id the id of the new entry.
	 * @param timestamp The timestamp of the new entry.
	 * @param text The text of the new entry.
	 */
	private void createNewMinutes(final int id, String timestamp, String text) {
		String timeString = DateTimeFormat.getFormat("HH:mm").format(HasTimestamp.TIMESTAMPFORMAT.parseStrict(timestamp));
		
		final Element trElement = Document.get().createTRElement();
		
		Element timeElement = Document.get().createTDElement();
		timeElement.setInnerHTML(timeString);
		
		Element tdElement = Document.get().createTDElement();
		
		Element button = Document.get().createElement("button");
		button.setInnerHTML("&nbsp;");
		HTML.wrap(button).addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				instance.getDeleteEntryPopup().showPopup(minuteEntries.get(id));
			}
		});
		
		Element textElement = Document.get().createDivElement();
		textElement.setInnerHTML(text);
		
		tdElement.appendChild(button);
		tdElement.appendChild(textElement);
		
		trElement.appendChild(timeElement);
		trElement.appendChild(tdElement);
		
		minutesList.insertFirst(trElement);
	}
	
	/**
	 * Sends a new vote if the user accepts or rejects the recommendation in the current form, or if he abstains from a decission.
	 * @param decission the decission the user made.
	 */
	public void sendVote(String decission) {
		JavaScriptObject vote = SessionObject.createVoteSessionObject(instance.getBareJID(), decission);
		publishObject(vote, discussionSpaceId, null);
	}
	
	/**
	 * Hides all buttons and therefor functions which only the moderator may use.
	 * @param newMod the jid of the new moderator.
	 */
	private void moderatorChangeSuccessful(String newMod) {
		selectNewManagerButton.setVisible(false);
		targetGroupsChangeButton.setVisible(false);
		publishButton.setVisible(false);
		
		isModerator = false;
		moderatorJid = newMod;
		sendCompleteInformation();
	}
	
	/**
	 * Sets another person as the moderator of the discussion.
	 * @param newModerator the jid of the new moderator.
	 */
	public void setNewModerator(String newModerator) {
		setNewModerator(newModerator, discussionSpaceId);
	}
	
	/**
	 * Setsy another person as the moderator of the discussion.
	 * @param instance an instance of the class DiscussionView.
	 * @param newMod the jid of the new moderator.
	 * @param nodeId the id of the pubsub node the discussion takes place on.
	 */
	private native void setNewModerator(String newMod, String nodeId) /*-{
		var that = this;
		var conn = $wnd.connection.getXMPPConnection();
		conn.pubsub.setAffiliation(nodeId, newMod, "owner", function(){
				that.@de.imc.mirror.arapp.client.view.DiscussionView::moderatorChangeSuccessful(Ljava/lang/String;)(newMod);
				});
	}-*/;
	
	/**
	 * After a successful moderatorchange this method revokes all rights about the pubsub node of the previous moderator.
	 * @param oldMod the jid of the old moderator.
	 * @param nodeId the id of the pubsub node the discussion takes place on.
	 */
	private native void revokeModeratorRightsOfPreviousModerator(String oldMod, String nodeId) /*-{
		var conn = $wnd.connection.getXMPPConnection();
		conn.pubsub.setAffiliation(nodeId, oldMod, "publisher", function(){});
	}-*/;
	
	/**
	 * Sends a Session object with all current information.
	 */
	private void sendCompleteInformation() {
		Map<String, String> infos = new HashMap<String, String>();
				
		infos.put("targetGroups", targetGroups);
		infos.put("discussionGroup", discussionGroup);
		infos.put("moderator", moderatorJid);
		
		long diff = new Date().getTime() - latestLocalTimestamp;
		String timestamp = HasTimestamp.TIMESTAMPFORMAT.format(new Date(latestReceivedTimestamp + diff));
		JavaScriptObject object = SessionObject.createCompleteSessionObject(createRecommendationObject(), infos, timestamp);
		publishObject(object, discussionSpaceId, null);
	}
	
	/**
	 * Creates a recommendation object with all available information which can be published.
	 * <br><br>
	 * If a List of evidence ids is given, but is not <code>null</code>, the created recommendation object will have only those ids in the relatedObjects elements. This should only be done if the created recommendation object will be published on the targetspaces.
	 * <br><br>
	 * If no list of evidence ids is given, i.e. it is <code>null</code>, each relatedObject element will contain the complete evidence which was added to the discussion. This should only be done for session Objects.
	 * @param evidenceIds A list of evidence ids. Can be null or empty.
	 * @return a recommendation Object with all available information.
	 */
	private RecommendationObject createRecommendationObject() {
		
		String issue;
		if (!discussionEntryMessage.emptyIssueHtml().equals(issueLabel.getElement().getInnerHTML())) {
			issue = issueLabel.getText();
		} else {
			issue = "";
		}
		String solution;
		if (!discussionEntryMessage.emptySolutionHtml().equals(solutionLabel.getElement().getInnerHTML())) {
			solution = solutionLabel.getText();
		} else {
			solution = "";
		}
		if (taskType == null) {
			taskType = TaskType.getTaskType(taskTypeList.getValue(taskTypeList.getSelectedIndex()));
		}
		RecommendationObjectBuilder builder = new RecommendationObjectBuilder(title.getText(), issue, solution, taskType);
		
		if (customId == null) {
			customId = ARApp.uuid();
		}
		builder.setCustomId(customId);		
		
		for (Entry entry:minuteEntries.values()) {
			builder.addEntry(entry);
		}
		
		for (Evidence ev:attachedEvidences) {
			builder.addRelatedEvidence(ev);
		}
		
		for (String userId:participants) {
			builder.addParticipant(userId);
		}
		for (String spaceId:targetSpaces) {
			builder.addTargetSpace(spaceId);
		}
		builder.setPublisher(moderatorJid);
		builder.setState(release);
		if (ratingLabel.getInnerText().replaceAll(" ", "").isEmpty()) {
			builder.setRatingDescription("");
		} else {
			builder.setRatingDescription(ratingLabel.getInnerText());		
		}
		builder.setCreationDate(HasTimestamp.TIMESTAMPFORMAT.format(new Date()));
		if (this.ref != null) {
			builder.setRef(ref);
		}
		if (effort != null) {
			builder.setEffortRating(effort);
		}
		if (benefit != null) {
			builder.setBenefitRating(benefit);
		}
		return builder.build();
	}
	
	/**
	 * Publishes all added evidence on the targetspaces and then, with the new ids of the evidence, creates a new Recommendationobject and publishes these on the spaces.
	 * If no evidence was added the recommendation object will just be published on all targetspaces.
	 */
	public void publishRecommendation(boolean release) {
		this.release = release;
		
		if (containsFileEvidence) {
			ARAppServiceAsync service = (ARAppServiceAsync) GWT.create(ARAppService.class);
			service.saveFilesOnSpaces(discussionSpaceId, instance.getLoginInfos(), targetSpaces, new AsyncCallback<Void>(){
				@Override
				public void onFailure(Throwable arg0) {
					log("There was an error");
				}

				@Override
				public void onSuccess(Void arg0) {
					log("Everything Ok");
				}
			});
			
			List<FileEvidence> fileEvs = new ArrayList<FileEvidence>();
			for (Evidence evidence:attachedEvidences) {
				if (evidence instanceof FileEvidence) {
					fileEvs.add((FileEvidence)evidence);
				}
			}
			for (String space:targetSpaces) {
				for (FileEvidence fe:fileEvs) {
					fe.setLocation(space);
				}
				List<String> spaceId = new ArrayList<String>();
				spaceId.add(space);
				publishObject(createRecommendationObject().toDataObject(), spaceId);
			}
		} else {
			publishObject(createRecommendationObject().toDataObject(), targetSpaces);
		}
		deleteSpace(discussionSpaceId);
		discussionSpaceId = null;
		ref = null;
	}
	
	public String getCustomId() {
		return customId;
	}

	/**
	 * Checks if the user has access to at least one of the specified targetspaces.
	 * @return if the user can access at least one targetspace.
	 */
	public boolean isOneTargetSpaceAvailable() {
		Map<String, String> spaces = instance.getSpacesMap();
		for (String space:targetSpaces) {
			if (spaces.containsKey(space)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method publishes the recommendation on the own private space. If it is not available it will be created first.
	 * @param instance an instance of the class DiscussionView.
	 */
	public native void publishRecommendationOnPrivateSpace() /*-{
		var that = this;
		$wnd.spaceHandler.getDefaultSpace(function(result){
			if (!result || result == null) {
				$wnd.spaceHandler.createDefaultSpace(function(res) {
					var config = res.generateSpaceConfiguration();
					config.setPersistenceType($wnd.SpacesSDK.PersistenceType.ON);
					$wnd.spaceHandler.configureSpace(res.getId(), config, function(){}, function(){});
					$wnd.dataHandler.registerSpace(res.getId());
					var dataObject = instance.@de.imc.mirror.arapp.client.view.DiscussionView::createRecommendationObject()().@de.imc.mirror.arapp.client.RecommendationObject::toDataObject()();
					var spaces = @java.util.ArrayList::new()();
					spaces.@java.util.List::add(Ljava/lang/Object;)(res.getId());
					that.@de.imc.mirror.arapp.client.view.DiscussionView::publishObject(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/util/List;)(dataObject, spaces);								
				}, function(error){});
			} else {
					var dataObject = instance.@de.imc.mirror.arapp.client.view.DiscussionView::createRecommendationObject()().@de.imc.mirror.arapp.client.RecommendationObject::toDataObject()();
					var spaces = @java.util.ArrayList::new()();
					spaces.@java.util.List::add(Ljava/lang/Object;)(result.getId());
					that.@de.imc.mirror.arapp.client.view.DiscussionView::publishObject(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/util/List;)(dataObject, spaces);
			}
			}, function(error){});
	}-*/;

	/**
	 * Convenience method to publish a dataobject on the given space.
	 * @param dataObj The dataobject to publish.
	 * @param spaceId The space id to publish the dataobject on.
	 */
	private native void publishObject(final JavaScriptObject dataObj, String spaceId, ARApp instance) /*-{
		var elementToStopheTree = function(element, parent) {
			var i;
			var tree = null;
			var attributesObject = {};
			for (i = 0; i < element.attributes.length; i++) {
				attributesObject[element.attributes.item(i).name] = element.attributes.item(i).value; 
			}
			if (element.namespaceURI) {
				attributesObject['xmlns'] = element.namespaceURI;
			}
			tree = $wnd.$build(element.nodeName, attributesObject);
			for (i = 0; i < element.childNodes.length; i++) {
				var childNode = element.childNodes[i];
				if (childNode.nodeType == 3) {
					tree.t(childNode.nodeValue);
				} else if (childNode.nodeType == 4) {
					tree.cnode(childNode);
					tree.up();
				} else {
					elementToStopheTree(childNode, tree);
				}
			}
			if (parent) {
				parent.cnode(tree.tree());
				parent.up();
			}
			return tree;
		};
		
		var element = dataObj.getElement();
		var item = elementToStopheTree(element);
		
		var items = new Array();
		items[0] = new Object();
		items[0].attrs = {};
		items[0].data = item.tree();
		$wnd.connection.getXMPPConnection().pubsub.publish(spaceId, items, function(resultIq) {
			switch (resultIq.getAttribute('type')) {
			case 'result':
				if (instance && instance != null) {
					instance.@de.imc.mirror.arapp.client.ARApp::logout()();
				}
				break;
			default:
				//TODO
				break;
			}
		});
	}-*/;
	
	/**
	 * Convenience method to publish a dataobject on multiple spaces.
	 * @param dataObj The dataobject to publish.
	 * @param targetSpaces The space ids to publish the dataobject on.
	 */
	private native void publishObject(final JavaScriptObject dataObj, List<String> targetSpaces) /*-{
		var length = targetSpaces.@java.util.List::size()();
		for (var i=0; i<length; i++) {
			var id = targetSpaces.@java.util.List::get(I)(i);
			$wnd.dataHandler.publishDataObject(dataObj, id);
		}
	}-*/;
	
	/**
	 * Method to delete the discussionspace. This also removes the sessionListener and the unregisters the space in the datahandler.
	 * @param discussionSpaceId The id of the discussionspace.
	 */
	private native void deleteSpace(String discussionSpaceId) /*-{
		var that = this;
		var xmppConnection = $wnd.connection.getXMPPConnection();
		xmppConnection.deleteHandler($wnd.handlerRef);		
		xmppConnection.pubsub.getSubscriptions(function(result) {
			var items = result.getElementsByTagName("subscription");
			if (items && items != null) {
				for (var i=0; i<items.length; i++) {
					if (items[i].getAttribute("node") === discussionSpaceId) {
						xmppConnection.pubsub.unsubscribe(items[i].getAttribute("node"), items[i].getAttribute("jid"), items[i].getAttribute("subid"), function(){}, function(){});
					}
				}
			}
		});
		xmppConnection.pubsub.deleteNode(discussionSpaceId, function() {
			that.@de.imc.mirror.arapp.client.view.DiscussionView::removeTime(Ljava/lang/String;)(discussionSpaceId);
		});	
	}-*/;

	@Override
	public void buildSpacesList() {
		targetGroupsCheckBoxList.removeAllChildren();
		
		final Map<String, String> spaces = instance.getSpacesMap();
		List<String> spaceIds = new ArrayList<String>();
		spaceIds.addAll(spaces.keySet());
		
		Comparator<String> compare = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String name1 = spaces.get(o1);
				String name2 = spaces.get(o2);
				return name1.compareToIgnoreCase(name2);
			}
		};
		
		Collections.sort(spaceIds, compare);
		
		checkBoxes = new HashMap<String, SimpleCheckBox>();
		
		for (String id:spaceIds) {
			addSpaceToList(id, spaces.get(id));
		}		
	}
	
	public void updateTargetGroupsList() {
		Map<String, String> spaces = instance.getSpacesMap();
		
		targetGroupsList.removeAllChildren();
		targetSpaces.clear();
		StringBuilder builder = new StringBuilder();
		for (SimpleCheckBox box:checkBoxes.values()) {
			if (box.getValue()) {
				Element liElement = Document.get().createLIElement();
				liElement.setInnerHTML(spaces.get(box.getFormValue()));
				builder.append(spaces.get(box.getFormValue())).append(",");
				targetGroupsList.appendChild(liElement);
				targetSpaces.add(box.getFormValue());
			}
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length()-1);
		}
		targetGroups = builder.toString();
		sendCompleteInformation();
	}
	
	public void updateSpacesList() {
		if (checkBoxes == null) return;
		
		final Map<String, String> spaces = instance.getSpacesMap();
		List<String> spaceIds = new ArrayList<String>();
		spaceIds.addAll(spaces.keySet());
		
		Comparator<String> compare = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String name1 = spaces.get(o1);
				String name2 = spaces.get(o2);
				return name1.compareToIgnoreCase(name2);
			}
		};
		Collections.sort(spaceIds, compare);

		List<String> oldIds = new ArrayList<String>();
		oldIds.addAll(checkBoxes.keySet());
		oldIds.removeAll(spaceIds);
		if (oldIds.size() > 0) {
			for (String id:oldIds) {
				targetGroupsCheckBoxList.removeChild(targetSpacesListElements.get(id));
				targetSpacesListElements.remove(id);
				checkBoxes.remove(id);
			}
		}
		
		String lastId = null;
		if (!targetSpacesListElements.keySet().containsAll(spaceIds)) {
			for (String id:checkBoxes.keySet()) {
				checkBoxes.get(id).setValue(false);
			}
			for (String id:spaceIds) {
				if (!targetSpacesListElements.containsKey(id)) {
					addSpaceToList(id, spaces.get(id));
					checkBoxes.get(id).setValue(true);
					if (lastId == null) {
						targetGroupsCheckBoxList.insertFirst(targetSpacesListElements.get(id));
					} else {
						targetGroupsCheckBoxList.insertAfter(targetSpacesListElements.get(id), targetSpacesListElements.get(lastId));
					}
					targetSpacesListElements.get(id).scrollIntoView();
				}
				lastId = id;
			}
		}
		updateTargetGroupsList();
	}
	
	private void addSpaceToList(String id, String spaceName) {
		Element trElement = Document.get().createTRElement();
		
		Element tdCheckElement = Document.get().createTDElement();
		SimpleCheckBox checkBox = SimpleCheckBox.wrap(Document.get().createCheckInputElement());
		checkBox.setFormValue(id);
		
		Element tdTitleElement = Document.get().createTDElement();
		Element spanElement = Document.get().createSpanElement();
		spanElement.setInnerHTML(spaceName);
		
		tdCheckElement.appendChild(checkBox.getElement());
		tdTitleElement.appendChild(spanElement);

		trElement.appendChild(tdCheckElement);
		trElement.appendChild(tdTitleElement);
		
		targetGroupsCheckBoxList.appendChild(trElement);		
		checkBoxes.put(id, checkBox);
		targetSpacesListElements.put(id, trElement);
	}
	
	/**
	 * If new information are available this method can be called to update the shown information.
	 */
	public void update() {		
		Map<String, String> discussions = instance.getDiscussions();
		if (discussions.get(discussionSpaceId) == null) {
			moderatorJid = instance.getBareJID();
			isModerator = true;
			title.setText(discussionSpaceId);
			
			issueLabel.getElement().setInnerHTML(discussionEntryMessage.emptyIssueHtml());
			solutionLabel.getElement().setInnerHTML(discussionEntryMessage.emptySolutionHtml());
			ratingLabel.setInnerText(discussionEntryMessage.standardRatingText());
			ratingTextArea.setText(discussionEntryMessage.standardRatingText());
			benefit = null;
			effort = null;
			targetGroups = null;
			ref = null;
			targetGroups = "";
			customId = null;
			
			participants.clear();
			minuteEntries.clear();
			dataObjects.clear();
			attachedEvidences.clear();
			targetSpaces.clear();

			targetGroupsList.removeAllChildren();
			if (checkBoxes != null) {
				for (SimpleCheckBox checkBox:checkBoxes.values()) {
					checkBox.setValue(false);
				}
			}
			
			participants.add(moderatorJid);
			updateParticipantsLabel();
			sendNewEntry(discussionEntryMessage.openMessage(instance.getDisplayNameForJid(moderatorJid), HasTimestamp.MEDIUMDATE.format(new Date())));
		} else {
			com.google.gwt.xml.client.Element discElem = Parser.parseXMLStringToElement(discussions.get(discussionSpaceId));
			if (discElem != null) {
				List<RecommendationObject> objs = Parser.parseRecommendations(discElem);
				if (objs != null && objs.size() > 0) {
					final RecommendationObject obj = objs.get(0);
					final Map<String, String> infos = SessionObject.getAllInformation(discussions.get(discussionSpaceId));
		
					updateInfosWithRecommendation(obj);
					updateInfos(infos);
				}
			}
		}
	}

	/**
	 * Sets the discussionSpace id
	 * @param id The discussionspace id.
	 */
	public void setDiscussionSpaceId(String id) {
		this.discussionSpaceId = id;
		update();
		String bareJid = instance.getBareJID();
		if (!participants.contains(instance.getBareJID())) {
			participants.add(bareJid);
			updateParticipantsLabel();
			sendNewEntry(discussionEntryMessage.joinMessage(instance.getDisplayNameForJid(bareJid)));
		}
		if (isModerator) {
			setModeratorRights();
		}
		getInvitedPersons(instance.getInvitedPersonsPopup(), id);
	}
	

	public native void getInvitedPersons(InvitedPersonsPopup popup, String discussionId) /*-{
		var pubsubLocation = "pubsub." + $wnd.connection.getConfiguration().getDomain(); 
		$wnd.connection.getXMPPConnection().disco.info(pubsubLocation, discussionId, function(success) {
			var list = @java.util.ArrayList::new()();
			var elements = success.getElementsByTagName("field");
			for (var i=0; i<elements.length; i++) {
			    var element = elements.item(i);
			    if (element.attributes.getNamedItem("var") && element.attributes.getNamedItem("var").nodeValue &&
			            (element.attributes.getNamedItem("var").nodeValue === "pubsub#owner" || element.attributes.getNamedItem("var").nodeValue === "pubsub#publisher")) {
			            	var people = element.getElementsByTagName("value");
			            	for (var j=0; j<people.length; j++) {
			            		list.@java.util.List::add(Ljava/lang/Object;)(people.item(j).textContent);
			            	}
			    }
			}
			popup.@de.imc.mirror.arapp.client.view.popup.InvitedPersonsPopup::setInvitedPersons(Ljava/util/List;)(list);
		});
	}-*/;
	
	public String getDiscussionSpaceId() {
		return this.discussionSpaceId;
	}
	
	/**
	 * Sets the name of the discussion.
	 * @param name The new name of the discussion.
	 */
	public void setName(String name) {
		this.title.setText(name);
	}
	
	/**
	 * Sets the text of the discussiongrouplabel to the given one.
	 * @param group The Discussion groups represented as a string.
	 */
	public void setDiscussionGroup(String group) {
		this.discussionGroup = group;
	}
	
	/**
	 * If a new user entered this will update the participants label.
	 */
	private void updateParticipantsLabel() {
		participantsList.removeAllChildren();
		Collections.sort(participants);
		for (String user:participants) {
			Element liElement = Document.get().createLIElement();
			liElement.setInnerHTML(instance.getDisplayNameForJid(user));
			
			if (user.equals(moderatorJid)) {
				liElement.addClassName("moderator");
				participantsList.insertFirst(liElement);
			} else {
				participantsList.appendChild(liElement);
			}
		}
	}
	
	/**
	 * 
	 * @param release if the recommendation should be active when it is published.
	 */
	public void setRelease(boolean release) {
		this.release = release;
		sendCompleteInformation();
	}
	
	/**
	 * Sends a last minute entry before the recommendation is published.
	 * @param lastEntry optional last comment.
	 */
	public void sendLastEntry(String lastEntry) {
		long diff = new Date().getTime() - latestLocalTimestamp;
		String time = HasTimestamp.MEDIUMDATE.format(new Date(latestReceivedTimestamp + diff));
		
		sendNewEntry(discussionEntryMessage.publishMessage(instance.getDisplayNameForJid(instance.getBareJID()), time, (lastEntry==null?"":lastEntry)));
	}
}
