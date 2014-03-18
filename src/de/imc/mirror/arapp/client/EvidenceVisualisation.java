package de.imc.mirror.arapp.client;

import java.util.Date;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.NodeList;

import de.imc.mirror.arapp.client.Evidence.Namespace;
import de.imc.mirror.arapp.client.visual.ActivityRecommendation;
import de.imc.mirror.arapp.client.visual.Clinic;
import de.imc.mirror.arapp.client.visual.Cromar;
import de.imc.mirror.arapp.client.visual.Doctrain;
import de.imc.mirror.arapp.client.visual.Issuemanagement;
import de.imc.mirror.arapp.client.visual.Knowself;
import de.imc.mirror.arapp.client.visual.MoodMap;

public class EvidenceVisualisation extends DialogBox {
	

	protected enum ElementIds {		
		PUBLISHERLABEL("evidenceDetailsPublisher"),
		DATELABEL("evidenceDetailsPublishingDate"),
		CONTENT("evidenceDetailsContent"),
		CLOSEBUTTON("evidenceDetailsButtonClose");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Button closeButton;

	private Label publisherLabel;
	
	private Label dateLabel;
	private Element content;

	private ARApp instance;
	
	public EvidenceVisualisation(ARApp instance) {
		this.instance = instance;
		this.setGlassEnabled(true);
		this.setGlassStyleName("transparent");
		build();
	}
	
	protected EvidenceVisualisation() {
		
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
							EvidenceVisualisation.this.hide();
						}
					});
					break;
				case CONTENT:
					content = elem.getElementsByTagName("div").getItem(0);
					break;
				case DATELABEL:
					dateLabel = Label.wrap(elem.getElementsByTagName("div").getItem(0));
					break;
				case PUBLISHERLABEL:
					publisherLabel = Label.wrap(elem.getElementsByTagName("div").getItem(0));
					break;
				}
			}
		}

		if (errors) {
			Window.alert(errorBuilder.toString());
		}
		Document.get().getElementById("evidenceDetailsPopup").addClassName("activeItem");
		this.setWidget(HTML.wrap(Document.get().getElementById("evidenceDetailsPopup")));
	}
	
	public void center() {
		super.center();
	}
	
	/**
	 * Opens the popup and shows the details of the given evidence.
	 * @param ev the evidecne to show the popup for.
	 */
	public void visualizeEvidence(Evidence ev) {				
		while (content.hasChildNodes()) {
			content.removeChild(content.getFirstChild());
		}
		com.google.gwt.xml.client.Element elem = Parser.parseXMLStringToElement(ev.toString());
		if (elem == null) {
			publisherLabel.setText(instance.infoMessage.notAvailable());
			dateLabel.setText(instance.infoMessage.notAvailable());
			content.setInnerText(ev.toString());
			return;
		}
		String ns = elem.getNamespaceURI();

		if (elem.hasAttribute("publisher")) {
			String name = instance.getDisplayNameForJid(elem.getAttribute("publisher"));
			if (name == null) {
				publisherLabel.setText(elem.getAttribute("publisher"));				
			} else {
				publisherLabel.setText(name);
			}
		} else {
			publisherLabel.setText(instance.infoMessage.notAvailable());
		}
		if (elem.hasAttribute("timestamp")) {
			String timestamp = elem.getAttribute("timestamp");
			String publishedTime = HasTimestamp.MEDIUMDATE.format(getDate(timestamp));
			dateLabel.setText(publishedTime);
		}

		Namespace namespace = Namespace.parse(ns);
		switch (namespace) {
		case ACTIVITYRECOMMENDATIONAPPEXPERIENCE:
			ActivityRecommendation.buildExperience(this, elem);
			break;
		case ACTIVITYRECOMMENDATIONAPPTEXTEVIDENCE:
			ActivityRecommendation.buildTextEvidence(this, ev, elem);
			break;
		case CROMARRECOMMENDATION:
			Cromar.buildCromarEvidence(this, elem);
			break;
		case CLINICNOTE:
			Clinic.buildClinicNote(this, elem);
			break;
		case CLINICSCORE:
			Clinic.buildClinicScore(this, elem);
			break;
		case DOCTRAINNOTE:
			Doctrain.buildDoctrainEvidence(this, elem);
			break;
		case ISSUEMANAGEMENTAPPISSUE:
			Issuemanagement.buildIssue(this, elem);
			break;
		case MOODMAPMOOD:
			MoodMap.buildMood(this, elem);
			break;
		case KNOWSELF:
			Knowself.buildVisualization(this, elem);
			break;
		case OTHER:	
			AbsolutePanel absolutePanel = new AbsolutePanel();	
			if (elem.hasAttribute("id")) {
				Label idLabel = new Label("ID:");
				idLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
				absolutePanel.add(idLabel);
				String value = elem.getAttribute("id");
				Label valueLabel = new Label(value);
				absolutePanel.add(valueLabel);
			}
			
			NodeList summary = elem.getElementsByTagName("summary");
			if (summary != null && summary.getLength() != 0) {
				if (summary.item(0).hasChildNodes() && !"".equals(summary.item(0).getFirstChild().getNodeValue())) {
					Label summaryLabel = new Label("Summary:");
					summaryLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
					absolutePanel.add(summaryLabel);

					String value = summary.item(0).getFirstChild().getNodeValue();
					
					Label valueLabel = new Label(value);
					absolutePanel.add(valueLabel);	
				}
			}	
			content.appendChild(absolutePanel.getElement());
		default:
			break;
		}
		center();
	}
	
	/**
	 * Parses a timestamp to a date.
	 * @param timestamp a timestamp in the datetime format.
	 * @return the parsed date.
	 */
	protected static Date getDate(String timestamp) {
		StringBuilder pattern = new StringBuilder("yyyy-MM-dd'T'HH:mm:ss");
		if (timestamp.indexOf(".") != -1) {
			pattern.append(".SSS");
		}

		if (timestamp.indexOf("+") != -1 || timestamp.lastIndexOf("-") == timestamp.length()-6) {
			pattern.append("ZZZ");
		}
		DateTimeFormat format = DateTimeFormat.getFormat(pattern.toString());
		return format.parse(timestamp);
	}
	
	public void addContent(Element element) {
		if (element != null) {
			content.appendChild(element);
		}
	}

}
