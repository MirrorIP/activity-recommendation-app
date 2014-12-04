package de.imc.mirror.arapp.client.view.experiencesTab;

import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.Interfaces.ExperiencesTab;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;

public class ManageTabExperiences extends ExperiencesTab {
	
	protected enum Elements {
		VIEWEXPERIENCEBENEFIT("manageViewExperiencesBenefit"),
		VIEWEXPERIENCEEFFORT("manageViewExperiencesEffort"),
		VIEWEXPERIENCERATING("manageViewExperiencesRating"),
		VIEWEXPERIENCESRATIOPANEL("manageViewExperiencesRatioPanel"),
		VIEWEXPERIENCESRATIO("manageViewExperiencesRatio"),
		VIEWEXPERIENCETABLE("manageViewExperiencesExperiences");
		
		private String id;		
		
		private Elements(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	}
	
	public ManageTabExperiences(ARApp instance) {
		super(instance);
		initialize();
	}
	
	private void initialize() {
		StringBuilder errorBuilder = new StringBuilder("The HTML of this site was malformed. Please contact the administrator and send him the following infos:");
		boolean errors = false;
		for (Elements id:Elements.values()) {
			Element elem = Document.get().getElementById(id.getId());
			if (elem == null) {
				errorBuilder.append("\n").append(id.getId());
				errors = true;
			} else {
				switch (id) {
				case VIEWEXPERIENCEBENEFIT:
					setBenefitRatingElements(elem);
					break;
				case VIEWEXPERIENCEEFFORT:
					setEffortRatingElements(elem);
					break;
				case VIEWEXPERIENCERATING:
					setRatingElements(elem);
					break;
				case VIEWEXPERIENCESRATIO:
					setExperienceRatioLabel(elem);
					break;
				case VIEWEXPERIENCESRATIOPANEL:
					setExperienceRatioPanel(elem);
					break;
				case VIEWEXPERIENCETABLE:
					setExperienceTable(elem);
					break;
				}
			}
		}
		if (errors) {
			Window.alert(errorBuilder.toString());
		}
	}
	
	public void showExperienceDetails(List<Experience> experiences, RecommendationObject recomm) {
		averageRatingLabel.setInnerText(recomm.getRatingDescription());
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
		
		showBenefit(experiences, recomm);
		showEffort(experiences, recomm);
		showRatio(experiences, recomm);
		
		
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
				
				Element publisherTdElement = Document.get().createTDElement();
				String publisherString = exp.getPublisher();
				publisherString = instance.getDisplayNameForJid(publisherString);
				publisherTdElement.setInnerHTML(publisherString);
				root.appendChild(publisherTdElement);
				
				root.appendChild(commentTdElement);
				
				averageExperiencesExperiencesListBody.appendChild(root);
			}
			averageExperiencesSummary.setInnerText(instance.infoMessage.experiencesAmount(experiences.size(), commentAmount));
		} else {
			averageExperiencesSummary.setInnerText(instance.infoMessage.noExperiencesYet());
		}		
	}

}
