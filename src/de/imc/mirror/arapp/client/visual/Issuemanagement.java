package de.imc.mirror.arapp.client.visual;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;

import de.imc.mirror.arapp.client.EvidenceVisualisation;

public class Issuemanagement extends EvidenceVisualisation {

	public static void buildIssue(EvidenceVisualisation instance, Element elem) {

		AbsolutePanel absolutePanel = new AbsolutePanel();
		
		NodeList type = elem.getElementsByTagName("type");
		if (type != null && type.getLength() != 0) {
			Label typeLabel = new Label("Type:");
			typeLabel.setStyleName("label font-bold");
			typeLabel.getElement().getStyle().setMarginTop(8, Unit.PX);
			absolutePanel.add(typeLabel);
			
			Label typeTextLabel = new Label(type.item(0).getFirstChild().getNodeValue());
			typeTextLabel.setStyleName("label");
			absolutePanel.add(typeTextLabel);			
		}
		
		NodeList description = elem.getElementsByTagName("description");
		if (description != null && description.getLength() != 0) {
			Label descriptionLabel = new Label("Description:");
			descriptionLabel.setStyleName("label font-bold");
			descriptionLabel.getElement().getStyle().setMarginTop(8, Unit.PX);
			absolutePanel.add(descriptionLabel);
			
			Label descriptioTextLabel = new Label(description.item(0).getFirstChild().getNodeValue());
			descriptioTextLabel.setStyleName("label");
			absolutePanel.add(descriptioTextLabel);			
		}
			
		instance.addContent(absolutePanel.getElement());
	}
}
