package de.imc.mirror.arapp.client.view.popup;


import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextArea;

import de.imc.mirror.arapp.client.Benefit;
import de.imc.mirror.arapp.client.Effort;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.view.DiscussionView;

public class PublishRecommendationWizard {

	protected enum ElementIds {
		
		
		EFFORTLABEL("recommendationSummaryEffort"),
		BENEFITLABEL("recommendationSummaryBenefit"),
		TARGETGROUPSLABEL("recommendationSummaryTargetGroups"),
		RELEASECHECKBOX("recommendationSummaryReleaseRecommendationCheckbox"),
		AGREEMENTPANEL("recommendationSummaryAgreement"),
		COMMENTTEXTAREA("recommendationSummaryComment"),
		PUBLISHBUTTON("recommendationSummaryPublishButton"),
		CLOSEBUTTON("recommendationSummaryCloseButton"),
		ISSUELABEL("recommendationSummaryIssue"),
		SOLUTIONLABEL("recommendationSummaryRecommendedSolution"),
		TITLELABEL("recommendationSummaryTitle"),
		RATINGLABEL("recommendationSummaryRatingText"),
		
		REJECTBUTTON("recommendationSummaryRejectButton"),
		ABSTAINBUTTON("recommendationSummaryAbstainButton"),
		ACCEPTBUTTON("recommendationSummaryAcceptButton"),
		
		ERRORMESSAGE("recommendationSummaryErrorMessage");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};

	private final String ABSTAIN = "abstain";
	private final String REJECT = "reject";
	private final String ACCEPT = "accept";

	private TextArea commentTextArea;
	
	private Button closeButton;
	private Button publishButton;	
	
	private Element targetGroupsLabel;
	
	private Element issueLabel;
	private Element solutionLabel;

	private Element benefitTypeLabel;
	private Element benefitUnitLabel;
	private Element benefitDescriptionLabel;

	private Element effortTypeLabel;
	private Element effortUnitLabel;
	private Element effortDescriptionLabel;
	
	private Element titleLabel;
	
	private Element ratingLabel;
	
	private Element errorMessage;
	
	private SimpleCheckBox releaseBox;

	private Button abstainButton;
	private Button acceptButton;
	private Button rejectButton;
	
	private DialogBox dia;
	
	private DiscussionView instance;
	
	private int accepted;
	private int rejected;
	private int abstained;

	private Element agreementPanel;
	
	private List<String> problems;
	
	public PublishRecommendationWizard(DiscussionView instance) {
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
				case CLOSEBUTTON:
					closeButton = Button.wrap(elem);
					closeButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							dia.hide();
						}
					});
					break;
				case TARGETGROUPSLABEL:
					targetGroupsLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case PUBLISHBUTTON:
					publishButton = Button.wrap(elem);
					publishButton.addClickHandler(new ClickHandler(){

						@Override
						public void onClick(ClickEvent event) {
							Timer timer = new Timer() {
								
								@Override
								public void run() {
									instance.publishRecommendation(releaseBox.getValue());
									dia.hide();
								}
							};
							if (commentTextArea.getText().equals("")) {
								instance.sendLastEntry("");
							} else {
								instance.sendLastEntry(commentTextArea.getText());
							}
							timer.schedule(500);
								
//							}
						}
						
					});
					break;
				case COMMENTTEXTAREA:
					commentTextArea = TextArea.wrap(elem.getElementsByTagName("textarea").getItem(0));
					break;
				case AGREEMENTPANEL:
					agreementPanel = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case BENEFITLABEL:
					benefitTypeLabel = elem.getElementsByTagName("span").getItem(0);
					benefitUnitLabel = elem.getElementsByTagName("span").getItem(1);
					
					NodeList<Element> elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						if (elems.getItem(j).getClassName().contains("blockContent")) {
							benefitDescriptionLabel = elems.getItem(j);
						}
					}
					
					break;
				case EFFORTLABEL:
					effortTypeLabel = elem.getElementsByTagName("span").getItem(0);
					effortUnitLabel = elem.getElementsByTagName("span").getItem(1);
					
					elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						if (elems.getItem(j).getClassName().contains("blockContent")) {
							effortDescriptionLabel = elems.getItem(j);
						}
					}					
					break;
				case ISSUELABEL:
					issueLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case RELEASECHECKBOX:
					releaseBox = SimpleCheckBox.wrap(elem);
					break;
				case SOLUTIONLABEL:
					solutionLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case TITLELABEL:
					titleLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case RATINGLABEL:
					ratingLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case ABSTAINBUTTON:
					abstainButton = Button.wrap(elem);
					abstainButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.sendVote(ABSTAIN);
							dia.hide();
						}
					});
					break;
				case ACCEPTBUTTON:
					acceptButton = Button.wrap(elem);
					acceptButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.sendVote(ACCEPT);
							dia.hide();
						}
					});
					break;
				case REJECTBUTTON:
					rejectButton = Button.wrap(elem);
					rejectButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.sendVote(REJECT);
							dia.hide();
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

		Document.get().getElementById("recommendationSummaryPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("recommendationSummaryPopup")));
	}
	
	/**
	 * Counts the given vote to display it for the manager.
	 * @param vote the received vote.
	 */
	public void addVote(String vote) {
		if (vote.equals(ABSTAIN)) {
			abstained++;
		} else if (vote.equals(REJECT)) {
			rejected++;
		} else if (vote.equals(ACCEPT)) {
			accepted++;
		} else {
			return;
		}
		
		showCurrentVotes();
	}
	
	/**
	 * Shows the current count of the different options to vote.
	 */
	private void showCurrentVotes() {	
		agreementPanel.removeAllChildren();

		Element trElement = Document.get().createTRElement();
		Element acceptedElement = Document.get().createTDElement();
		acceptedElement.setInnerHTML(accepted + "");
		
		Element abstainedElement = Document.get().createTDElement();
		abstainedElement.setInnerHTML(abstained + "");
		
		Element rejectedElement = Document.get().createTDElement();
		rejectedElement.setInnerHTML(rejected + "");

		trElement.appendChild(acceptedElement);
		trElement.appendChild(abstainedElement);
		trElement.appendChild(rejectedElement);
		
		agreementPanel.appendChild(trElement);
	}
	
	/**
	 * Shows the popup for a specific recommendation.
	 * @param recomm the recommendationobject to show the summary for.
	 * @param targetGroups the targetgroups as a string.
	 * @param moderator if it should be shown as the wizard for the moderator or not.
	 */
	public void showPopup(RecommendationObject recomm, String targetGroups, boolean moderator) {
		abstained = 0;
		rejected = 0;
		accepted = 0;
		showCurrentVotes();
		if (recomm == null) return;
		if (moderator) {
			dia.getWidget().getElement().removeClassName("participantView");
			dia.getWidget().getElement().addClassName("moderatorView");
			if (problems != null && problems.size() > 0) {
				StringBuilder builder = new StringBuilder();
				builder.append("One or more problems were found.");
				for (String problem:problems) {
					builder.append("<br>").append(problem);
				}
				errorMessage.setInnerHTML(builder.toString());
				errorMessage.addClassName("activeItem");
				
				publishButton.setVisible(false);
				problems.clear();
			} else {
				errorMessage.removeClassName("activeItem");
				publishButton.setVisible(true);
			}
			commentTextArea.setText("");
		} else {
			dia.getWidget().getElement().removeClassName("moderatorView");
			dia.getWidget().getElement().addClassName("participantView");			
		}
		issueLabel.setInnerText(recomm.getIssue());
		solutionLabel.setInnerText(recomm.getRecommendedSolution());
		titleLabel.setInnerText(recomm.getTitle());
		if (targetGroups == null || targetGroups.isEmpty()) {
			targetGroupsLabel.setInnerText("None.");
		} else {
			targetGroupsLabel.setInnerText(targetGroups);
		}
		
		Benefit benefit = recomm.getBenefitRating();
		if (benefit != null){
			benefitTypeLabel.setInnerText(Benefit.getBenefit(benefit.getId()).getDisplay());
			benefitUnitLabel.setInnerText(benefit.getDisplay());
			benefitDescriptionLabel.setInnerText(benefit.getDescription());
		} else {
			benefitTypeLabel.setInnerText("Do not measure");
			benefitUnitLabel.setInnerText("-");
			benefitDescriptionLabel.setInnerText("");
		}
		
		Effort effort = recomm.getEffortRating();
		if (effort != null){
			effortTypeLabel.setInnerText(Effort.getEffort(effort.getId()).getDisplay());
			effortUnitLabel.setInnerText(effort.getDisplay());
			effortDescriptionLabel.setInnerText(effort.getDescription());
		} else {
			effortTypeLabel.setInnerText("Do not measure");
			effortUnitLabel.setInnerText("-");
			effortDescriptionLabel.setInnerText("");
		}
		
		ratingLabel.setInnerText(recomm.getRatingDescription());
		
		dia.center();
	}
	
	public void setProblems(List<String> problems) {
		this.problems = problems;
	}
	
	public void hidePopup() {
		dia.hide();
	}
}
