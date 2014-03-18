package de.imc.mirror.arapp.client.view.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Benefit;
import de.imc.mirror.arapp.client.Effort;
import de.imc.mirror.arapp.client.Entry;
import de.imc.mirror.arapp.client.HasTimestamp;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.RecommendationObjectBuilder;
import de.imc.mirror.arapp.client.view.ManageTab;

public class UpdateRecommendationPopup {
	protected enum ElementIds {
		TITLEEDITBUTTON("updateRecommendationPopupRecommendationTitleEditButton"),
		TITLECANCELBUTTON("updateRecommendationPopupRecommendationTitleCancelButton"),
		TITLESUBMITBUTTON("updateRecommendationPopupRecommendationTitleSubmitButton"),
		TITLELABEL("updateRecommendationPopupRecommendationTitleLabel"),
		TITLEEDITFIELD("updateRecommendationPopupRecommendationTitleInput"),

		ISSUEEDITBUTTON("updateRecommendationPopupRecommendationIssueEditButton"),
		ISSUECANCELBUTTON("updateRecommendationPopupRecommendationIssueCancelButton"),
		ISSUESUBMITBUTTON("updateRecommendationPopupRecommendationIssueSubmitButton"),
		ISSUELABEL("updateRecommendationPopupRecommendationIssueLabel"),
		ISSUEEDITFIELD("updateRecommendationPopupRecommendationIssueInput"),

		SOLUTIONEDITBUTTON("updateRecommendationPopupRecommendationSolutionEditButton"),
		SOLUTIONCANCELBUTTON("updateRecommendationPopupRecommendationSolutionCancelButton"),
		SOLUTIONSUBMITBUTTON("updateRecommendationPopupRecommendationSolutionSubmitButton"),
		SOLUTIONLABEL("updateRecommendationPopupRecommendationSolutionLabel"),
		SOLUTIONEDITFIELD("updateRecommendationPopupRecommendationSolutionInput"),

		RATINGEDITBUTTON("updateRecommendationPopupRecommendationRatingTextEditButton"),
		RATINGCANCELBUTTON("updateRecommendationPopupRecommendationRatingTextCancelButton"),
		RATINGSUBMITBUTTON("updateRecommendationPopupRecommendationRatingTextSubmitButton"),
		RATINGLABEL("updateRecommendationPopupRecommendationRatingTextLabel"),
		RATINGEDITFIELD("updateRecommendationPopupRecommendationRatingTextInput"),

		EFFORTPANEL("updateRecommendationPopupRecommendationEffort"),
		EFFORTEDITBUTTON("updateRecommendationPopupRecommendationEffortEditButton"),
		EFFORTCANCELBUTTON("updateRecommendationPopupRecommendationEffortCancelButton"),
		EFFORTSUBMITBUTTON("updateRecommendationPopupRecommendationEffortSubmitButton"),
		EFFORTDISPLAYEDITFIELD("updateRecommendationPopupRecommendationEffortDisplayInput"),
		EFFORTDESCRIPTIONEDITFIELD("updateRecommendationPopupRecommendationEffortTextInput"),

		BENEFITPANEL("updateRecommendationPopupRecommendationBenefit"),
		BENEFITEDITBUTTON("updateRecommendationPopupRecommendationBenefitEditButton"),
		BENEFITCANCELBUTTON("updateRecommendationPopupRecommendationBenefitCancelButton"),
		BENEFITSUBMITBUTTON("updateRecommendationPopupRecommendationBenefitSubmitButton"),
		BENEFITDISPLAYEDITFIELD("updateRecommendationPopupRecommendationBenefitDisplayInput"),
		BENEFITDESCRIPTIONEDITFIELD("updateRecommendationPopupRecommendationBenefitTextInput"),
		
		SUBMITBUTTON("updateRecommendationPopupCreateButton"),
		CANCELBUTTON("updateRecommendationPopupCancelButton"),
		ERRORMESSAGE("updateRecommendationPopupErrorMessage");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};

	private Button cancelButton;
	private Button submitButton;	
	
	private Element titlePanel;
	private Button titleEditButton;
	private Button titleSubmitButton;
	private Button titleCancelButton;
	private Element titleLabel;
	private TextArea titleEditField;

	private Element solutionPanel;
	private Button solutionEditButton;
	private Button solutionSubmitButton;
	private Button solutionCancelButton;
	private Element solutionLabel;
	private TextArea solutionEditField;

	private Element issuePanel;
	private Button issueEditButton;
	private Button issueSubmitButton;
	private Button issueCancelButton;
	private Element issueLabel;
	private TextArea issueEditField;

	private Element ratingPanel;
	private Button ratingEditButton;
	private Button ratingSubmitButton;
	private Button ratingCancelButton;
	private Element ratingLabel;
	private TextArea ratingEditField;

	private Element effortEditPanel;
	private Element effortPanel;
	private Button effortEditButton;
	private Button effortSubmitButton;
	private Button effortCancelButton;
	private TextArea effortUnitEditField;
	private TextArea effortDescriptionEditField;

	private Element effortUnitLabel;
	private Element effortDescriptionLabel;
	private Element effortTypeLabel1;
	private Element effortTypeLabel2;

	private Element benefitEditPanel;
	private Element benefitPanel;
	private Button benefitEditButton;
	private Button benefitSubmitButton;
	private Button benefitCancelButton;
	private TextArea benefitUnitEditField;
	private TextArea benefitDescriptionEditField;
	
	private Element benefitUnitLabel;
	private Element benefitDescriptionLabel;
	private Element benefitTypeLabel1;
	private Element benefitTypeLabel2;
	
	private Element errorMessage;
	
	private ManageTab view;
	private ARApp instance;
	
	private DialogBox dia;
	
	private RecommendationObject rec;
	
	private String title;
	private String issue;
	private String solution;
	private String rating;
	private String benefitUnit;
	private String benefitDescription;
	private String effortUnit;
	private String effortDescription;
	
	
	/**
	 * Creates the dialog to update a recommedantion
	 * @param view Instance of the ManageTab.
	 */
	public UpdateRecommendationPopup(ManageTab view, ARApp instance){
		this.view = view;
		this.instance = instance;
		dia = new DialogBox();
		dia.setGlassEnabled(true);
		dia.setGlassStyleName("transparent");
		build();
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
				case CANCELBUTTON:
					cancelButton = Button.wrap(elem);
					cancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							dia.hide();
						}
					});
					break;
				case SUBMITBUTTON:
					submitButton = Button.wrap(elem);
					submitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (showErrors()) {
								return;
							}
							dia.hide();
							
							RecommendationObjectBuilder builder = new RecommendationObjectBuilder(title, issue, solution, rec.getTaskType());
							if (rec.getBenefitRating() != null) {
								Benefit benefit = Benefit.getBenefit(rec.getBenefitRating().getId());
								benefit.setDescription(benefitDescription);
								benefit.setDisplay(benefitUnit);
								builder.setBenefitRating(benefit);
							}
							if (rec.getEffortRating() != null) {
								Effort effort = Effort.getEffort(rec.getEffortRating().getId());
								effort.setDescription(effortDescription);
								effort.setDisplay(effortUnit);
								builder.setEffortRating(effort);
							}
							builder.setRatingDescription(rating)
									.setCustomId(rec.getCustomId())
									.setCreationDate(rec.getTimestamp())
									.setId(rec.getId())
									.setPublisher(rec.getPublisher())
									.setRef(rec.getPreviousRevisions())
									.setState(rec.getState());
							for(Entry entry:rec.getEntries()) {
								builder.addEntry(entry);
							}
							Entry entry = new Entry(0, instance.infoMessage.recommendationUpdated(), HasTimestamp.TIMESTAMPFORMAT.format(new Date()));
							builder.addEntry(entry);
							RecommendationObject recomm = builder.build();
							recomm.setParticipants(rec.getParticipants());
							recomm.setRelatedEvidence(rec.getRelatedEvidence());
							recomm.setRelatedEvidenceIds(rec.getRelatedEvidenceIds());
							recomm.setTargetSpaces(rec.getTargetSpaces());
							view.updateRecommendation(recomm);
						}
					});
					break;
				case ISSUECANCELBUTTON:
					issueCancelButton = Button.wrap(elem);
					issueCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = issueEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
						}
					});
					break;
				case ISSUEEDITBUTTON:
					issueEditButton = Button.wrap(elem);
					issuePanel = elem.getParentElement().getParentElement().getParentElement();
					issueEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = issueEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.addClassName("editState");
							issueEditField.setText(issueLabel.getInnerText());
							issueEditField.setFocus(true);
						}
					});
					break;
				case ISSUEEDITFIELD:
					issueEditField = TextArea.wrap(elem);
					break;
				case ISSUELABEL:
					issueLabel = elem;
					break;
				case ISSUESUBMITBUTTON:
					issueSubmitButton = Button.wrap(elem);
					issueSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = issueEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
							issueLabel.setInnerText(issueEditField.getText());
							issue = issueEditField.getText();
						}
					});
					break;
				case SOLUTIONCANCELBUTTON:
					solutionCancelButton = Button.wrap(elem);
					solutionCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = solutionEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
						}
					});
					break;
				case SOLUTIONEDITBUTTON:
					solutionEditButton = Button.wrap(elem);
					solutionPanel = elem.getParentElement().getParentElement().getParentElement();
					solutionEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = solutionEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.addClassName("editState");
							solutionEditField.setText(solutionLabel.getInnerText());
							solutionEditField.setFocus(true);
						}
					});
					break;
				case SOLUTIONEDITFIELD:
					solutionEditField = TextArea.wrap(elem);
					break;
				case SOLUTIONLABEL:
					solutionLabel = elem;
					break;
				case SOLUTIONSUBMITBUTTON:
					solutionSubmitButton = Button.wrap(elem);
					solutionSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = solutionEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
							solutionLabel.setInnerText(solutionEditField.getText());
							solution = solutionEditField.getText();
						}
					});
					break;
				case TITLECANCELBUTTON:
					titleCancelButton = Button.wrap(elem);
					titleCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = titleEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
						}
					});
					break;
				case TITLEEDITBUTTON:
					titleEditButton = Button.wrap(elem);
					titlePanel = elem.getParentElement().getParentElement().getParentElement();
					titleEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = titleEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.addClassName("editState");
							titleEditField.setText(titleLabel.getInnerText());
							titleEditField.setFocus(true);
						}
					});
					break;
				case TITLEEDITFIELD:
					titleEditField = TextArea.wrap(elem);
					break;
				case TITLELABEL:
					titleLabel = elem;
					break;
				case TITLESUBMITBUTTON:
					titleSubmitButton = Button.wrap(elem);
					titleSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = titleEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
							titleLabel.setInnerText(titleEditField.getText());
							title = titleEditField.getText();
//							}
						}
					});
					break;
				case RATINGCANCELBUTTON:
					ratingCancelButton = Button.wrap(elem);
					ratingCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = ratingEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
						}
					});
					break;
				case RATINGEDITBUTTON:
					ratingEditButton = Button.wrap(elem);
					ratingPanel = elem.getParentElement().getParentElement().getParentElement();
					ratingEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = ratingEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.addClassName("editState");
							ratingEditField.setText(ratingLabel.getInnerText());
							ratingEditField.setFocus(true);
						}
					});
					break;
				case RATINGEDITFIELD:
					ratingEditField = TextArea.wrap(elem);
					break;
				case RATINGLABEL:
					ratingLabel = elem;
					break;
				case RATINGSUBMITBUTTON:
					ratingSubmitButton = Button.wrap(elem);
					ratingSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = ratingEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
							ratingLabel.setInnerText(ratingEditField.getText());
							rating = ratingEditField.getText();
						}
					});
					break;
				case BENEFITPANEL:
					benefitPanel = elem;

					benefitTypeLabel1 = elem.getElementsByTagName("span").getItem(0);
					benefitUnitLabel = elem.getElementsByTagName("span").getItem(1);
					benefitTypeLabel2 = elem.getElementsByTagName("span").getItem(2);	
					
					NodeList<Element> elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element el = elems.getItem(j);
						if (el.getClassName().contains("blockContent")) {
							benefitDescriptionLabel = el;
							break;
						}
					}
					break;
				case BENEFITCANCELBUTTON:
					benefitCancelButton = Button.wrap(elem);
					benefitCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = benefitEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
						}
					});
					break;
				case BENEFITDESCRIPTIONEDITFIELD:
					benefitDescriptionEditField = TextArea.wrap(elem);
					break;
				case BENEFITDISPLAYEDITFIELD:
					benefitUnitEditField = TextArea.wrap(elem);
					break;
				case BENEFITEDITBUTTON:
					benefitEditButton = Button.wrap(elem);
					benefitEditPanel = elem.getParentElement().getParentElement().getParentElement();
					benefitEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = benefitEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.addClassName("editState");
							benefitUnitEditField.setText(benefitUnit);
							benefitDescriptionEditField.setText(benefitDescription);
							benefitDescriptionEditField.setFocus(true);
						}
					});
					break;
				case BENEFITSUBMITBUTTON:
					benefitSubmitButton = Button.wrap(elem);
					benefitSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = benefitEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
							
							benefitUnit = benefitUnitEditField.getText();
							benefitUnitLabel.setInnerText(benefitUnit);
							benefitDescription = benefitDescriptionEditField.getText();
							benefitDescriptionLabel.setInnerText(benefitDescription);
						}
					});
					break;
				case EFFORTPANEL:
					effortPanel = elem;
					
					effortTypeLabel1 = elem.getElementsByTagName("span").getItem(0);
					effortUnitLabel = elem.getElementsByTagName("span").getItem(1);
					effortTypeLabel2 = elem.getElementsByTagName("span").getItem(2);	
					
					elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element el = elems.getItem(j);
						if (el.getClassName().contains("blockContent")) {
							effortDescriptionLabel = el;
							break;
						}
					}		
					break;
				case EFFORTCANCELBUTTON:
					effortCancelButton = Button.wrap(elem);
					effortCancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = effortEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
						}
					});
					break;
				case EFFORTDESCRIPTIONEDITFIELD:
					effortDescriptionEditField = TextArea.wrap(elem);
					break;
				case EFFORTDISPLAYEDITFIELD:
					effortUnitEditField = TextArea.wrap(elem);
					break;
				case EFFORTEDITBUTTON:
					effortEditButton = Button.wrap(elem);
					effortEditPanel = elem.getParentElement().getParentElement().getParentElement();
					effortEditButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = effortEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.addClassName("editState");
							
							effortUnitEditField.setText(effortUnit);
							effortDescriptionEditField.setText(effortDescription);
							effortDescriptionEditField.setFocus(true);
						}
					});		
					break;
				case EFFORTSUBMITBUTTON:
					effortSubmitButton = Button.wrap(elem);
					effortSubmitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Element parent = effortEditButton.getElement().getParentElement().getParentElement().getParentElement();
							parent.removeClassName("editState");
							
							effortUnit = effortUnitEditField.getText();
							effortUnitLabel.setInnerText(effortUnit);
							effortDescription = effortDescriptionEditField.getText();
							effortDescriptionLabel.setInnerText(effortDescription);
						}
					});
					break;
				case ERRORMESSAGE:
					errorMessage = elem;
					break;
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}

		Document.get().getElementById("updateRecommendationPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("updateRecommendationPopup")));
	}
	
	/**
	 * Shows the popup. Only if setRecommendationObject(RecommendationObject recomm) was called beforehand!
	 */
	public void showPopup() {
		if (rec == null) {
			return;
		}
		errorMessage.removeClassName("activeItem");
		dia.center();
		titleLabel.setInnerText(rec.getTitle());
		title = rec.getTitle();
		
		issueLabel.setInnerText(rec.getIssue());
		issue = rec.getIssue();
		
		solutionLabel.setInnerText(rec.getRecommendedSolution());
		solution = rec.getRecommendedSolution();
		
		ratingLabel.setInnerText(rec.getRatingDescription());
		rating = rec.getRatingDescription();
		
		if (rec.getBenefitRating() != null) {
			benefitPanel.getStyle().setDisplay(Display.BLOCK);
			
			Benefit benefit = rec.getBenefitRating();
			benefitTypeLabel1.setInnerText(Benefit.getBenefit(benefit.getId()).getDisplay());
			benefitTypeLabel2.setInnerText(Benefit.getBenefit(benefit.getId()).getDisplay());
			
			benefitUnitLabel.setInnerText(benefit.getDisplay());
			benefitUnit = benefit.getDisplay();
			benefitUnitEditField.setText(benefit.getDisplay());
			
			benefitDescriptionLabel.setInnerText(benefit.getDescription());
			benefitDescription = benefit.getDescription();
			benefitDescriptionEditField.setText(benefit.getDescription());
		} else {
			benefitPanel.getStyle().setDisplay(Display.NONE);
		}
		
		if (rec.getEffortRating() != null) {
			effortPanel.getStyle().setDisplay(Display.BLOCK);
			
			Effort effort = rec.getEffortRating();
			effortTypeLabel1.setInnerText(Effort.getEffort(effort.getId()).getDisplay());
			effortTypeLabel2.setInnerText(Effort.getEffort(effort.getId()).getDisplay());
			
			effortUnitLabel.setInnerText(effort.getDisplay());
			effortUnit = effort.getDisplay();
			effortUnitEditField.setText(effort.getDisplay());
			
			effortDescriptionLabel.setInnerText(effort.getDescription());
			effortDescription = effort.getDescription();
			effortDescriptionEditField.setText(effort.getDescription());
		} else {
			effortPanel.getStyle().setDisplay(Display.NONE);
		}
	}
	
	private boolean showErrors() {
		List<String> unsavedErrors = new ArrayList<String>();
		if (titlePanel.getClassName().contains("editState")) {
			unsavedErrors.add("title");
		}
		if (issuePanel.getClassName().contains("editState")) {
			unsavedErrors.add("issue");
		}
		if (solutionPanel.getClassName().contains("editState")) {
			unsavedErrors.add("solution");
		}
		if (ratingPanel.getClassName().contains("editState")) {
			unsavedErrors.add("rating");
		}
		if (benefitEditPanel.getClassName().contains("editState")) {
			unsavedErrors.add("benefit");
		}
		if (effortEditPanel.getClassName().contains("editState")) {
			unsavedErrors.add("effort");
		}
		List<String> errors = new ArrayList<String>();
		if (unsavedErrors.size() > 0) {
			errors.add(instance.errorMessage.unsavedChanges(unsavedErrors));
		}
		
		if (title.isEmpty()) {
			errors.add(instance.errorMessage.emptyTitle());
		}
		if (issue.isEmpty()) {
			errors.add(instance.errorMessage.emptyIssue());
		}
		if (solution.isEmpty()) {
			errors.add(instance.errorMessage.emptySolution());
		}
		if (rating.isEmpty()) {
			errors.add(instance.errorMessage.emptyRatingDescription());
		}
		

		if (rec.getBenefitRating() != null) {
			if (benefitUnit.isEmpty() || benefitDescription.isEmpty()) {
				errors.add(instance.errorMessage.emptyBenefit(benefitUnit.isEmpty(), benefitDescription.isEmpty()));
			}
		}
		if (rec.getEffortRating() != null) {
			if (effortUnit.isEmpty() || effortDescription.isEmpty()) {
				errors.add(instance.errorMessage.emptyEffort(effortUnit.isEmpty(), effortDescription.isEmpty()));
			}
		}
		if (errors.size() > 0) {
			StringBuilder builder = new StringBuilder();
			builder.append("One or more problems were found.");
			for (String error:errors) {
				builder.append("<br>").append(error);
			}
			errorMessage.setInnerHTML(builder.toString());
			errorMessage.addClassName("activeItem");
			return true;
		}
		return false;
	}

	/**
	 * Sets the recommendation which should be updated.
	 * This method has to be called before showPopup()
	 * @param recomm the recommendation to show.
	 */
	public void setRecommendationObject(RecommendationObject recomm) {
		this.rec = recomm;
	}
}
