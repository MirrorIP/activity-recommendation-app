package de.imc.mirror.arapp.client.view.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
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

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.view.View;

public class UsageExperiencePopup extends View{

	protected enum ElementIds {
		CLOSEBUTTON("usageExperienceDetailsButton"),
		PUBLISHER("usageExperienceDetailsPublisher"),
		PUBLISHINGDATE("usageExperienceDetailsPublishingDate"),
		STARRATING("usageExperienceDetailsStarRating"),
		EFFORT("usageExperienceDetailsEffort"),
		BENEFIT("usageExperienceDetailsBenefit"),
		COMMENT("usageExperienceDetailsComment"),
		EVIDENCES("usageExperiencedetailsAttachedEvidences"),
		DELETEBUTTON("usageExperienceDetailsDeleteButton");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};

	private Button closeButton;
	
	private Element publisher;
	private Element date;
	private List<Element> starRatingSpans;
	private Element benefit;
	private Element effort;
	private Element comment;
	private Element evidencesLabel;
	private Element evidencesTable;
	private Element deleteButton;
	
	private DialogBox dia;
	
	private Experience shownExperience;
	
	
	public UsageExperiencePopup(final ARApp instance) {
		super(instance);
		dia = new DialogBox();
		dia.setGlassEnabled(true);
		dia.setGlassStyleName("transparent");
		
		starRatingSpans = new ArrayList<Element>();
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
							shownExperience = null;
						}
					});
					break;
				case EVIDENCES:
					evidencesLabel = elem.getElementsByTagName("div").getItem(0);
					evidencesTable = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case PUBLISHER:
					publisher = elem.getElementsByTagName("div").getItem(0);
					break;
				case PUBLISHINGDATE:
					date = elem.getElementsByTagName("div").getItem(0);
					break;
				case STARRATING:
					NodeList<Element> elems = elem.getElementsByTagName("span");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						starRatingSpans.add(child);
					}
					break;
				case EFFORT:
					effort = elem.getElementsByTagName("div").getItem(0);
					break;
				case BENEFIT:
					benefit = elem.getElementsByTagName("div").getItem(0);
					break;
				case COMMENT:
					comment = elem.getElementsByTagName("div").getItem(0);
					break;
				case DELETEBUTTON:
					deleteButton = elem;
					Button.wrap(deleteButton).addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (!shownExperience.getPublisher().startsWith(instance.getBareJID())) {
								return;
							}
							String recId = shownExperience.getRecommendationId();
							Map<String, RecommendationObject> recs = instance.getRecommendationsForUser();
							List<String> spaces = new ArrayList<String>(instance.getSpacesMap().values());
							if (recs != null && recs.size()>0) {
								RecommendationObject rec = recs.get(recId);
								if (rec != null && rec.getTargetSpaces() != null && rec.getTargetSpaces().size() > 0) {
									spaces = rec.getTargetSpaces();
								}				
							}
							List<String> spaceIds = new ArrayList<String>();
							Map<String, JavaScriptObject> realSpaces = instance.getCompleteSpacesMap();
							for (String id:spaces) {
								JavaScriptObject space = realSpaces.get(id);
								if (space != null && isModeratorOfSpace(space, instance.getBareJID())) {
									spaceIds.add(id);
								}
							}
							spaceIds.add(instance.getBareJID().split("@")[0]);
							if (!spaceIds.isEmpty()) {
								instance.requestAllDataToDelete(spaceIds, shownExperience.getCustomId());	
							}
							spaces.removeAll(spaceIds);
							if (!spaces.isEmpty()) {
								shownExperience.setToDelete();
								for (String id:spaces) {
									publishExperience(id, shownExperience);
								}
							}
							instance.removeExperience(shownExperience);
							dia.hide();
							shownExperience = null;
						}					

						private native void publishExperience(String spaceId, Experience experience) /*-{
							var dataObject = experience.@de.imc.mirror.arapp.client.Experience::toDataObject()();
							$wnd.dataHandler.publishDataObject(dataObject, spaceId, function() {
							}, function() {
							});
						}-*/;
						
						private native boolean isModeratorOfSpace(JavaScriptObject space, String userId) /*-{
							return space.isModerator(userId);
						}-*/;
					});
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}

		Document.get().getElementById("usageExperienceDetailsPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("usageExperienceDetailsPopup")));
	}
	
	/**
	 * Shows the popup for a given experience.
	 * @param exp the experience to show the details of.
	 */
	public void showPopup(Experience exp) {
		shownExperience = exp;
		publisher.setInnerHTML(instance.getDisplayNameForJid(exp.getPublisher()));
		date.setInnerHTML(exp.getFormattedTimestamp(HasTimestamp.LONGDATE));
		if (exp.getEffort() != null) {
			effort.setInnerText(exp.getEffortValue() + " " + exp.getEffort().getDisplay());
		} else {
			effort.setInnerText("-");
		}
		if (exp.getBenefit() != null) {
			benefit.setInnerText(exp.getBenefitValue() + " " + exp.getBenefit().getDisplay());
		} else {
			benefit.setInnerText("-");
		}
		if (exp.getComment() != null && !exp.getComment().equals("")) {
			comment.setInnerText(exp.getComment());
		} else {
			comment.setInnerText("-");
		}
		
		if (!shownExperience.getPublisher().startsWith(instance.getBareJID())) {
			deleteButton.getStyle().setDisplay(Display.NONE);
		} else {
			deleteButton.getStyle().clearDisplay();
		}
		
		evidencesTable.removeAllChildren();
		if (exp.getEvidences() != null) {
			for (final Evidence ev:exp.getEvidences()) {
				HTML trHTML = HTML.wrap(Document.get().createTRElement());
				trHTML.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						instance.getEvidenceVisualisationPopup().visualizeEvidence(ev);
					}
				});
				
	
				Element iconTD = Document.get().createTDElement();
				Element img = Document.get().createImageElement();
				img.setAttribute("src", "img/data-unknown.png");
				iconTD.appendChild(img);
				
				Element nameTD = Document.get().createTDElement();
				nameTD.setInnerHTML(ev.getType());
				
	
				Element dateTD = Document.get().createTDElement();
				dateTD.setInnerHTML(ev.getFormattedTimestamp(HasTimestamp.MEDIUMDATE));
				
				trHTML.getElement().appendChild(iconTD);
				trHTML.getElement().appendChild(nameTD);
				trHTML.getElement().appendChild(dateTD);
				
				evidencesTable.appendChild(trHTML.getElement());
			}
		}
		evidencesLabel.setInnerHTML(instance.infoMessage.manageAttachedEvidences(exp != null?exp.getEvidences().size():0));
		
		
		for (int i=0; i<5; i++) {
			Element elem = starRatingSpans.get(i);
			if (i<exp.getRating()) {
				elem.addClassName("starFilled");
			} else {
				elem.removeClassName("starFilled");
			}
		}
		dia.center();
	}
	
}
