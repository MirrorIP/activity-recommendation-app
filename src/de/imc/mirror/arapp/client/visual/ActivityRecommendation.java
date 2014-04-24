package de.imc.mirror.arapp.client.visual;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Clear;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.NodeList;

import de.imc.mirror.arapp.client.Benefit;
import de.imc.mirror.arapp.client.Effort;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.EvidenceVisualisation;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.FileEvidence;
import de.imc.mirror.arapp.client.Parser;
import de.imc.mirror.arapp.client.view.popup.ShowImagePopup;

public class ActivityRecommendation {
	
	private Experience exp;
	
	public static void buildExperience(EvidenceVisualisation instance, com.google.gwt.xml.client.Element elem)  {
		
		
		Experience exp = Parser.parseExperiences(elem).get(0);
		
		Element ratingLabel = Document.get().createLabelElement();
		ratingLabel.setInnerHTML("Rating");
		
		instance.addContent(ratingLabel);
		
		Element starRatingDiv = Document.get().createDivElement();
		starRatingDiv.addClassName("starRating");
		
		for (int i=0; i<5; i++) {
			Element spanElement = Document.get().createSpanElement();
			if (i < exp.getRating()) {
				spanElement.addClassName("starFilled");
			}
			starRatingDiv.appendChild(spanElement);
		}	
		instance.addContent(starRatingDiv);
		
		final Effort effortRating = exp.getEffort();
		final Benefit benefitRating = exp.getBenefit();
		
		if (effortRating != null || benefitRating != null) {
			AbsolutePanel abs = new AbsolutePanel();
						
			if (effortRating != null) {
				Label heading = new Label("Effort:");
				heading.setStyleName("label font-bold");
				heading.getElement().getStyle().setMarginTop(10, Unit.PX);
				
				Label description = new Label(effortRating.getDescription());
				description.setStyleName("label");
				description.getElement().getStyle().setMarginBottom(5, Unit.PX);
				
				Label effortLabel = new Label(effortRating.getDisplay());
				effortLabel.setStyleName("label");
				effortLabel.setWidth("190px");
				effortLabel.getElement().getStyle().setFloat(Float.LEFT);
				
				Label effortValueLabel = new Label(effortRating.getValue() + "");
				effortValueLabel.setStyleName("label");
				
				abs.add(heading);
				abs.add(description);
				abs.add(effortLabel);
				abs.add(effortValueLabel);
			}
			
			if (benefitRating != null) {			
				Label heading = new Label("Benefit:");
				heading.setStyleName("label font-bold");
				heading.getElement().getStyle().setMarginTop(10, Unit.PX);
				
				Label description = new Label(benefitRating.getDescription());
				description.setStyleName("label");
				description.getElement().getStyle().setMarginBottom(5, Unit.PX);				
				
				Label benefitLabel = new Label(benefitRating.getDisplay());
				benefitLabel.setStyleName("label");		
				benefitLabel.setWidth("190px");
				benefitLabel.getElement().getStyle().setFloat(Float.LEFT);
				
				Label benefitValueLabel = new Label(benefitRating.getValue() + "");
				benefitValueLabel.setStyleName("label");

				abs.add(heading);
				abs.add(description);
				abs.add(benefitLabel);
				abs.add(benefitValueLabel);				
			}
			instance.addContent(abs.getElement());
		}
		
		if (exp.getComment() != null && !exp.getComment().replaceAll(" ", "").equals("")) {
			Label commentLabel = new Label("Comment:");
			commentLabel.setStyleName("label font-bold");
			commentLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
			instance.addContent(commentLabel.getElement());
			
			Label commentTextLabel = new Label(exp.getComment());
			commentTextLabel.setStyleName("label");
			instance.addContent(commentTextLabel.getElement());
		}
		
	}
	

	
	public static void buildTextEvidence(EvidenceVisualisation instance, Evidence ev, com.google.gwt.xml.client.Element elem)  {
		
		AbsolutePanel absolutePanel = new AbsolutePanel();
		
		Element contentLabel = Document.get().createDivElement();
		
		NodeList content = elem.getElementsByTagName("content");
		String text = content.item(0).getFirstChild().getNodeValue();
		
		NodeList urls = elem.getElementsByTagName("url");
		
		if (urls != null && urls.getLength() > 0) {
			for (int i=0; i<urls.getLength(); ++i) {
				String url = urls.item(i).getFirstChild().getNodeValue();
				text = text.replace(url, "<a target=\"_blank\" href='" + url + "'>" + url + "</a>");
			}
		}
		contentLabel.setInnerText(text);
		
		absolutePanel.getElement().appendChild(contentLabel);
		
		if (ev instanceof FileEvidence) {
			final FileEvidence fe = (FileEvidence)ev;
			String filename = fe.getFileName();
			if (filename.length() > 15) {
				filename = filename.substring(0, 12);
				filename += "...";
			}
			Element attachmentLabel = Document.get().createDivElement();
			attachmentLabel.getStyle().setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
			attachmentLabel.getStyle().setMarginTop(10, Unit.PX);
			attachmentLabel.getStyle().setMarginRight(10, Unit.PX);
			attachmentLabel.setTitle(fe.getFileName());
			attachmentLabel.setInnerHTML("Attachment: " + filename);

			String[] array = fe.getFileName().split("\\.");
			final String fileType = array[array.length - 1].toLowerCase();
			Element buttonRow = Document.get().createDivElement();
			buttonRow.setClassName("buttonRow");
			
			Element downloadButton = Document.get().createPushButtonElement();
			downloadButton.setClassName("defaultButton");
			downloadButton.getStyle().setMarginTop(10, Unit.PX);
			downloadButton.setInnerHTML("Download");
			Button.wrap(downloadButton).addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					StringBuilder builder = new StringBuilder();
					builder.append(GWT.getModuleBaseURL())
							.append("server?")
							.append("nameOnServer=")
							.append(URL.encodeQueryString(fe.getFileNameWithPrefix()))
							.append("&filename=")
							.append(URL.encodeQueryString(fe.getFileName()))
							.append("&location=")
							.append(URL.encodeQueryString(fe.getLocation()))
							.append("&auth=")
							.append(URL.encodeQueryString(getLoginInfos()))
							.append("&download=true");
					Window.open(builder.toString(), null, null);
				}
			});
			
			
			absolutePanel.getElement().appendChild(attachmentLabel);
			buttonRow.appendChild(downloadButton);
			Element clearElement = Document.get().createDivElement();
			clearElement.getStyle().setClear(Clear.BOTH);
			absolutePanel.getElement().appendChild(clearElement);
			absolutePanel.getElement().appendChild(buttonRow);

			if (fileType.equals("png") || fileType.equals("gif") || fileType.equals("jpg")) {

				final Image img = new Image("./img/loader.gif");
				img.getElement().getStyle().setMarginTop(10, Unit.PX);
				img.setVisible(false);
				img.setWidth("auto");
				img.setHeight("auto");
				
				final Element showButton = Document.get().createPushButtonElement();
				showButton.getStyle().setMarginTop(10, Unit.PX);
				showButton.getStyle().setMarginLeft(10, Unit.PX);
				showButton.setClassName("defaultButton");
				showButton.setInnerHTML("Show");

				Button.wrap(showButton).addClickHandler(new ClickHandler() {
					
					private ShowImagePopup popup;
					@Override
					public void onClick(ClickEvent event) {
						if (popup == null) {
							popup = new ShowImagePopup(fe);
						}
						popup.showPopup();
					}
				});
				buttonRow.appendChild(showButton);
				absolutePanel.add(img);
			}
		}		

		//Needed or else the "ok" button would be on the wrong position
		absolutePanel.add(new AbsolutePanel());
		
		instance.addContent(absolutePanel.getElement());
	}

	private static native String getLoginInfos() /*-{
		var user = $wnd.connection.getCurrentUser().getBareJID();
		var pass = $wnd.pass;
		var loginInfos = user + ":" + pass;
		return $wnd.Base64.encode(loginInfos);
	}-*/;
}
