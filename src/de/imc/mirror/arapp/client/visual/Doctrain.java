package de.imc.mirror.arapp.client.visual;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Element;

import de.imc.mirror.arapp.client.EvidenceVisualisation;

public class Doctrain extends EvidenceVisualisation {
	
	public static void buildDoctrainEvidence(EvidenceVisualisation instance, Element elem) {
		AbsolutePanel absolutePanel = new AbsolutePanel();
		
		Label noteLabel = new Label("Note:");
		noteLabel.setStyleName("label font-bold");
		noteLabel.getElement().getStyle().setMarginTop(8, Unit.PX);
		absolutePanel.add(noteLabel);
		
		Label noteTextLabel = new Label(elem.getAttribute("remark"));
		noteTextLabel.setStyleName("label");
		absolutePanel.add(noteTextLabel);
		
		instance.addContent(absolutePanel.getElement());
	}
}
