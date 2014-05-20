package de.imc.mirror.arapp.client.visual;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;

import de.imc.mirror.arapp.client.EvidenceVisualisation;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
//
public class Cromar extends EvidenceVisualisation {

	public static void buildCromarEvidence(EvidenceVisualisation instance, Element elem) {

//		this.setText("CroMAR - Recommendation");

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
			
			String createdTimestamp = cdtDate.item(0).getFirstChild().getNodeValue();
			
			String createdPublishedTime = HasTimestamp.LONGDATE.format(getDate(createdTimestamp));
			
			Label publishedTimeLabel = new Label(createdPublishedTime);
			publishedTimeLabel.setStyleName("label");
			absolutePanel.add(publishedTimeLabel);
		}	
		
		
		NodeList note = elem.getElementsByTagName("noteBody");
		if (note != null && note.getLength() != 0) {
			
			if (note.item(0).hasChildNodes() && !note.item(0).getFirstChild().getNodeValue().equals("")) {
				Label noteLabel = new Label("Content:");
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
}
