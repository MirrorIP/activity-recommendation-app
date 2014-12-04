package de.imc.mirror.arapp.client.Interfaces;

import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Display;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.RecommendationObject;

public abstract class ExperiencesTab {
	
	protected ARApp instance;

	protected Element experienceBenefitRating;
	protected Element experienceBenefitRatingLabel;
	protected Element experienceBenefitRatingRating;	
	protected Element experienceEffortRating;		
	protected Element experienceEffortRatingLabel;	
	protected Element experienceEffortRatingRating;
	protected Element averageExperiencesExperiencesListBody;

	protected Element averageStarRatingStarsList;
	protected Element averageRatingLabel;
	protected Element averageStarRatingLabel;
	protected Element averageExperiencesSummary;

	protected Element ratioLabel;
	protected Element ratioPanel;
	
	public ExperiencesTab(ARApp instance) {
		this.instance = instance;
	}
	
	public void reset() {
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
	
	public abstract void showExperienceDetails(List<Experience> experiences, RecommendationObject recomm);

	protected void setBenefitRatingElements(Element benefitElem) {
		this.experienceBenefitRating = benefitElem;
		NodeList<Element> elems = benefitElem.getElementsByTagName("div");
		for (int j=0; j<elems.getLength(); j++) {
			Element child = elems.getItem(j);
			if (child.getClassName().contains("descriptionText")) {
				experienceBenefitRatingLabel = child;
			} else if ("block".equals(child.getClassName())) {
				experienceBenefitRatingRating = child;
			}
		}
	}

	protected void setEffortRatingElements(Element effortElem) {
		this.experienceEffortRating = effortElem;
		NodeList<Element> elems = effortElem.getElementsByTagName("div");
		for (int j=0; j<elems.getLength(); j++) {
			Element child = elems.getItem(j);
			if (child.getClassName().contains("descriptionText")) {
				experienceEffortRatingLabel = child;
			} else if ("block".equals(child.getClassName())) {
				experienceEffortRatingRating = child;
			}
		}
	}
	
	protected void setRatingElements(Element ratingElem) {
		NodeList<Element> elements = ratingElem.getElementsByTagName("div");
		for (int j=0; j<elements.getLength(); j++) {
			Element child = elements.getItem(j);
			if ("descriptionText".equals(child.getClassName())) {
				averageRatingLabel = child;
			} else if ("starRating".equals(child.getClassName())) {
				averageStarRatingStarsList = child;
				averageStarRatingLabel = child.getElementsByTagName("label").getItem(0);
			}
		}
	}
	
	protected void setExperienceTable(Element tableElem) {
		averageExperiencesSummary = tableElem.getElementsByTagName("div").getItem(0);	
		averageExperiencesExperiencesListBody = tableElem.getElementsByTagName("tbody").getItem(0);
	}
	
	protected void setExperienceRatioPanel(Element ratioElem) {
		this.ratioPanel = ratioElem;
	}
	
	protected void setExperienceRatioLabel(Element ratioElem) {
		this.ratioLabel = ratioElem;
	}
	

	
	protected void showEffort(List<Experience> experiences, RecommendationObject rec) {
		int totalEffort = 0;
		int efforts = 0;

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

	protected void showBenefit(List<Experience> experiences, RecommendationObject rec) {
		int totalBenefit = 0;
		int benefits = 0;	
		
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

	
	protected void showRatio(List<Experience> experiences, RecommendationObject rec) {
		int totalEffort = 0;
		int totalBenefit = 0;

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
} 
