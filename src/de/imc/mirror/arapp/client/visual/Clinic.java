package de.imc.mirror.arapp.client.visual;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;

import de.imc.mirror.arapp.client.EvidenceVisualisation;
import de.imc.mirror.arapp.client.HasTimestamp;

public class Clinic extends EvidenceVisualisation {
	
	public static void buildClinicNote(EvidenceVisualisation instance, Element elem) {
		AbsolutePanel absolutePanel = new AbsolutePanel();
		
		NodeList cdtPerson = elem.getElementsByTagName("person");
		if (cdtPerson != null && cdtPerson.getLength() != 0) {
			
			Label creatorLabel = new Label("Creator:");
			creatorLabel.setStyleName("label font-bold");
			absolutePanel.add(creatorLabel);
			
			String creator = cdtPerson.item(0).getFirstChild().getNodeValue();
			
			Label creatorNameLabel = new Label(creator);
			creatorNameLabel.setStyleName("label");
			absolutePanel.add(creatorNameLabel);
		}	
		
		NodeList cdtDate = elem.getElementsByTagName("date");
		if (cdtDate != null && cdtDate.getLength() != 0) {
			
			Label publishedLabel = new Label("Created:");
			publishedLabel.setStyleName("label font-bold");
			publishedLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
			absolutePanel.add(publishedLabel);
			
			String timestamp = cdtDate.item(0).getFirstChild().getNodeValue();
			
			String publishedTime = HasTimestamp.LONGDATE.format(getDate(timestamp));
			
			Label publishedTimeLabel = new Label(publishedTime);
			publishedTimeLabel.setStyleName("label");
			absolutePanel.add(publishedTimeLabel);
		}	
		
		
		NodeList note = elem.getElementsByTagName("content");
		if (note != null && note.getLength() != 0) {
			
			if (note.item(0).hasChildNodes() && !note.item(0).getFirstChild().getNodeValue().equals("")) {
				Label noteLabel = new Label("Note:");
				noteLabel.setStyleName("label font-bold");
				noteLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
				absolutePanel.add(noteLabel);

				String value = note.item(0).getFirstChild().getNodeValue();
				
				Label valueLabel = new Label(value);
				valueLabel.setStyleName("label");
				absolutePanel.add(valueLabel);			
			}
		}	
		instance.addContent(absolutePanel.getElement());
	}
	
	public static void buildClinicScore(EvidenceVisualisation instance, Element elem) {
//		this.setText("ClinIC - Score");

		AbsolutePanel absolutePanel = new AbsolutePanel();
		
		NodeList cdtPerson = elem.getElementsByTagName("person");
		if (cdtPerson != null && cdtPerson.getLength() != 0) {
			
			Label creatorLabel = new Label("Player:");
			creatorLabel.setStyleName("label font-bold");
			absolutePanel.add(creatorLabel);
			
			String creator = cdtPerson.item(0).getFirstChild().getNodeValue();
			
			Label creatorNameLabel = new Label(creator);
			creatorNameLabel.setStyleName("label");
			absolutePanel.add(creatorNameLabel);
		}	
		
		NodeList cdtDate = elem.getElementsByTagName("date");
		if (cdtDate != null && cdtDate.getLength() != 0) {
			
			Label publishedLabel = new Label("Created:");
			publishedLabel.setStyleName("label font-bold");
			publishedLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
			absolutePanel.add(publishedLabel);
			
			String timestamp = cdtDate.item(0).getFirstChild().getNodeValue();
			
			String publishedTime = HasTimestamp.LONGDATE.format(getDate(timestamp));
			
			Label publishedTimeLabel = new Label(publishedTime);
			publishedTimeLabel.setStyleName("label");
			absolutePanel.add(publishedTimeLabel);
		}	
		
		NodeList overallScore = elem.getElementsByTagName("overallScore");
		if (overallScore != null && overallScore.getLength() != 0) {			
			if (overallScore.item(0).hasChildNodes() && !overallScore.item(0).getFirstChild().getNodeValue().equals("")) {
				Label noteLabel = new Label("Overall Score:");
				noteLabel.setStyleName("label font-bold");
				noteLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
				absolutePanel.add(noteLabel);

				String value = overallScore.item(0).getFirstChild().getNodeValue();
				
				Label valueLabel = new Label(value);
				valueLabel.setStyleName("label");
				absolutePanel.add(valueLabel);			
			}
		}
		
		NodeList scores = elem.getElementsByTagName("score");
		if (scores != null && scores.getLength() != 0) {			
			Label noteLabel = new Label("Specific Scores:");
			noteLabel.setStyleName("label font-bold");
			noteLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
			absolutePanel.add(noteLabel);
			
			for (int i=0; i<scores.getLength(); ++i) {
				Element score = (Element) scores.item(i);
				StringBuilder builder = new StringBuilder(score.getAttribute("id"));
				builder.append(":  ").append(score.getFirstChild().getNodeValue());
				
				Label valueLabel = new Label(builder.toString());
				valueLabel.setStyleName("label");
				absolutePanel.add(valueLabel);					
			}			
		}	
		instance.addContent(absolutePanel.getElement());
	}
}
