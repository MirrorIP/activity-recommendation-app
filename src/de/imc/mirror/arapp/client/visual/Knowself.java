package de.imc.mirror.arapp.client.visual;

import com.google.gwt.xml.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Element;

import de.imc.mirror.arapp.client.EvidenceVisualisation;

public class Knowself extends EvidenceVisualisation {
	
	public static void buildVisualization(EvidenceVisualisation instance, Element elem) {
		AbsolutePanel absolutePanel = new AbsolutePanel();
		
		
		NodeList titleElems = elem.getElementsByTagName("title");
		if (titleElems != null && titleElems.getLength() != 0) {
			Label titleLabel = new Label("Title:");
			titleLabel.getElement().getStyle().setMarginTop(8, Unit.PX);
			absolutePanel.add(titleLabel);
			
			Element titleElem = (Element) titleElems.item(0);
			Label titleText;
			if (titleElem.hasChildNodes() && !titleElem.getFirstChild().getNodeValue().equals("")) {
				titleText = new Label(titleElem.getFirstChild().getNodeValue());
			} else {
				titleText = new Label("n/A");
			}
			absolutePanel.add(titleText);
		}
		NodeList descriptionElems = elem.getElementsByTagName("description");
		if (descriptionElems != null && descriptionElems.getLength() != 0) {
			Label descriptionLabel = new Label("Description:");
			descriptionLabel.getElement().getStyle().setMarginTop(8, Unit.PX);
			absolutePanel.add(descriptionLabel);
			
			Element descriptionElem = (Element) descriptionElems.item(0);
			Label descriptionText;
			if (descriptionElem.hasChildNodes() && !descriptionElem.getFirstChild().getNodeValue().equals("")) {
				descriptionText = new Label(descriptionElem.getFirstChild().getNodeValue());
			} else {
				descriptionText = new Label("n/A");
			}
			absolutePanel.add(descriptionText);
		}
		
		NodeList bodyElems = elem.getElementsByTagName("body");
		if (bodyElems != null && bodyElems.getLength() != 0 && bodyElems.item(0).hasChildNodes() 
				&& !bodyElems.item(0).getFirstChild().getNodeValue().equals("")) {
			Label visualizationLabel = new Label("Visualization:");
			visualizationLabel.getElement().getStyle().setMarginTop(8, Unit.PX);
			absolutePanel.add(visualizationLabel);
			
			Element imageElem = (Element) bodyElems.item(0);
			StringBuilder builder = new StringBuilder();
			builder.append("data:").append(imageElem.getAttribute("mimeType"))
					.append(";base64,");
			NodeList imageChilds = imageElem.getChildNodes();
			for (int i=0; i<imageChilds.getLength(); i++) {
				builder.append(imageChilds.item(i).getNodeValue());
			}
			Image img = new Image(builder.toString());
			img.getElement().getStyle().setMarginTop(10, Unit.PX);
			img.setWidth("300px");
			img.setHeight("auto");
			
			absolutePanel.add(img);
		}
		
		instance.addContent(absolutePanel.getElement());
	}

}
