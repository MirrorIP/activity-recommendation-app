package de.imc.mirror.arapp.client.view.popup;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.Interfaces.HasEvidences;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.view.View;

public class CaptureEvidencesPopup extends View implements HasEvidences{
	protected enum ElementIds {
		UPLOADFILEBUTTON("captureExperiencesEvidencesPopupUploadButton"),
		BROWSEBUTTON("captureExperiencesEvidencesPopupBrowseButton"),
		EVIDENCESLIST("captureExperiencesEvidencesList");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Button uploadFileButton;
	private Button browseButton;
	private Button closeButton;	
	
	private Element evidencesTable;
	
	private List<Evidence> attachedEvidences;
	
	private DialogBox dia;
	
	private HasEvidences callingView;
	
	
	public CaptureEvidencesPopup(final ARApp instance) {
		super(instance);
		dia = new DialogBox();
		dia.setGlassEnabled(true);
		dia.setGlassStyleName("transparent");
		
		attachedEvidences = new ArrayList<Evidence>();
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
				case BROWSEBUTTON:
					browseButton = Button.wrap(elem);
					browseButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getAttachEvidenceDialog().showPopup(CaptureEvidencesPopup.this, attachedEvidences);
						}
					});
					break;
				case EVIDENCESLIST:
					evidencesTable = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case UPLOADFILEBUTTON:
					uploadFileButton = Button.wrap(elem);
					uploadFileButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getCreateEvidencePopup().showPopup(CaptureEvidencesPopup.this);
						}
					});
					break;
				}
			}
		}
		Document.get().getElementById("captureExperiencesEvidencesPopup").addClassName("activeItem");
		Element elem = Document.get().getElementById("captureExperiencesEvidencesPopup");
		
		NodeList<Element> elems = elem.getElementsByTagName("button");
		
		for (int i=0; i<elems.getLength(); i++) {
			Element buttonElem = elems.getItem(i);
			if (buttonElem.getInnerText().contains("Close")) {
				closeButton = Button.wrap(buttonElem);
				closeButton.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						callingView.attachEvidences(attachedEvidences);
						dia.hide();
					}
				});
				break;
			}
		}
		
		evidencesTable.removeAllChildren();
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}
		
		dia.setWidget(HTML.wrap(elem));
	}
	
	/**
	 * Builds the list of the already attached evidences.
	 */
	private void buildEvidencesTable() {
		evidencesTable.removeAllChildren();
		for (final Evidence ev:attachedEvidences) {
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


			Element deleteTD = Document.get().createTDElement();
			HTML.wrap(deleteTD).addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					attachedEvidences.remove(ev);
					buildEvidencesTable();
					event.stopPropagation();
				}
			});
			
			trHTML.getElement().appendChild(iconTD);
			trHTML.getElement().appendChild(nameTD);
			trHTML.getElement().appendChild(dateTD);
			trHTML.getElement().appendChild(deleteTD);
			
			evidencesTable.appendChild(trHTML.getElement());
		}
	}

	/**
	 * Shows the popup.
	 * @param callingView instance of the view opening this popup.
	 * @param attachedEvidences a list of already attached evidences.
	 */
	public void showPopup(HasEvidences callingView, List<Evidence> attachedEvidences) {
		this.callingView = callingView;
		this.attachedEvidences.clear();
		if (attachedEvidences != null) {
			this.attachedEvidences.addAll(attachedEvidences);
		}
		buildEvidencesTable();
		dia.center();
	}
	
	public void attachEvidences(List<Evidence> attachEvidences) {
		this.attachedEvidences.clear();
		if (attachEvidences != null) {
			this.attachedEvidences.addAll(attachEvidences);
		}
		buildEvidencesTable();
	}

	public void attachCreatedEvidence(Evidence ev) {
		this.attachedEvidences.add(ev);
		buildEvidencesTable();
	}
	
	public String getFileEvidenceLocation() {
		return callingView.getFileEvidenceLocation();
	}
	
	public String getCustomId() {
		return callingView.getCustomId();
	}
}
