package de.imc.mirror.arapp.client.view.popup;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.xml.client.XMLParser;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.FileEvidence;
import de.imc.mirror.arapp.client.Parser;
import de.imc.mirror.arapp.client.Interfaces.HasEvidences;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.service.ARAppService;
import de.imc.mirror.arapp.client.service.ARAppServiceAsync;
import de.imc.mirror.arapp.client.view.View;

public class CreateEvidenceDialog extends View{
	protected enum ElementIds {
		DESCRIPTIONTEXTAREA("createNewEvidencePopupDescriptionInput"),
		ADDBUTTON("createNewEvidencePopupAttachmentAddButton"),
		REMOVEBUTTON("createNewEvidencePopupAttachmentRemoveButton"),
		ATTACHMENT("createNewEvidencePopupAttachment"),
		CANCELBUTTON("createNewEvidencePopupCancelButton"),
		FORMPANEL("createNewEvidencePopupUploadFileForm"),
		FORMPANELFILEINPUT("createNewEvidencePopupUploadFileInput"),
		FORMPANELFILENAMEINPUT("createNewEvidencePopupUploadFileNameInput"),
		FORMPANELSPACEINPUT("createNewEvidencePopupUploadSpaceInput"),
		CREATEBUTTON("createNewEvidencePopupCreateButton"),
		ERRORMESSAGE("createNewEvidencePopupErrorMessage");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Button addFileButton;
	private Button removeFileButton;
	
	private TextArea descriptionTextArea;
	
	private Button cancelButton;
	private Button createButton;
	
	private DialogBox dia;
	
	private FileUpload upload;
	private HasEvidences callingView;
	private Element attachedFileLabel;
	private Element fileEditableContent;
	private Element errorMessage;
	private FormPanel formPanel;
	private TextBox fileNameInput;
	private TextBox spaceInput;
	
	private DialogBox loaderDialog;
	
	private Element attachment;
	
	public CreateEvidenceDialog(final ARApp instance) {
		super(instance);
		dia = new DialogBox();
		dia.setGlassEnabled(true);
		dia.setGlassStyleName("transparent");
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
				case ADDBUTTON:
					addFileButton = Button.wrap(elem);
					addFileButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							upload.getElement().<InputElement>cast().click();
						}
					});
					break;
				case REMOVEBUTTON:
					removeFileButton = Button.wrap(elem);
					removeFileButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							formPanel.reset();
							attachedFileLabel.setInnerText(instance.infoMessage.noFileAttached());
							fileEditableContent.removeClassName("editState");
						}
					});
					break;
				case DESCRIPTIONTEXTAREA:
					descriptionTextArea = TextArea.wrap(elem);
					break;
				case CANCELBUTTON:
					cancelButton = Button.wrap(elem);
					cancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							dia.hide();
						}
					});
					break;
				case CREATEBUTTON:
					createButton = Button.wrap(elem);
					createButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (!descriptionTextArea.getText().isEmpty()) {
								if (instance.isFileServiceAvailable() && !upload.getFilename().equals("")) {
									final ARAppServiceAsync service = GWT.create(ARAppService.class);

									String filename = upload.getFilename();
									if (filename.contains("/")) {
										filename = filename.split("/")[filename.split("/").length-1];
									} else if (filename.contains("\\")) {
										filename = filename.split("\\\\")[filename.split("\\\\").length-1];
									}
									service.fileAlreadyExists(callingView.getFileEvidenceLocation(), filename, new AsyncCallback<Boolean>() {
				
										@Override
										public void onFailure(Throwable arg0) {
										}
				
										@Override
										public void onSuccess(Boolean arg0) {
											if (arg0) {
												errorMessage.addClassName("activeItem");
												errorMessage.setInnerText(instance.errorMessage.fileAlreadyExists());
											} else {

												loaderDialog = new DialogBox();
												loaderDialog.setGlassEnabled(true);
												loaderDialog.setGlassStyleName("transparent loader");
												loaderDialog.center();
												formPanel.submit();								
											}
										}
									});
													
								} else {
									String id = ARApp.uuid();
									String timestamp = HasTimestamp.TIMESTAMPFORMAT.format(new Date());
									String xml = createEvidenceXML(descriptionTextArea.getText(), id, timestamp);
									Evidence ev = new Evidence(id, "textevidence", timestamp, xml);
									ev.setNS("mirror:application:activityrecommendationapp:textevidence");
									
									callingView.attachCreatedEvidence(ev);
									
									dia.hide();
								}
							} else {
								errorMessage.addClassName("activeItem");
								errorMessage.setInnerText(instance.errorMessage.addDescription());
							}
						}
					});
					break;
				case ATTACHMENT:
					attachment = elem;
					NodeList<Element> elems = elem.getElementsByTagName("div");
					boolean hasLabel = false;
					boolean hasEditableContent = false;
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().equalsIgnoreCase("blockContent")) {
							hasLabel = true;
							attachedFileLabel = child;
						} else if (child.getClassName().equalsIgnoreCase("editableContent")) {
							hasEditableContent = true;
							fileEditableContent = child;
						}
						if (hasEditableContent && hasLabel) {
							break;
						}
					}
					break;
				case FORMPANEL:
					formPanel = FormPanel.wrap(elem, true);
					formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
					formPanel.setMethod(FormPanel.METHOD_POST);
				    String url = GWT.getModuleBaseURL();
				    formPanel.setAction(url + "server");
					
					formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
						
						@Override
						public void onSubmitComplete(SubmitCompleteEvent event) {
							if (event.getResults().contains("200")) {
								String id = ARApp.uuid();
								String timestamp = HasTimestamp.TIMESTAMPFORMAT.format(new Date());
								

								String filename = upload.getFilename();
								if (filename.contains("/")) {
									filename = filename.split("/")[filename.split("/").length-1];
								} else if (filename.contains("\\")) {
									filename = filename.split("\\\\")[filename.split("\\\\").length-1];
								}
								
								String fileLocation = null;

								fileLocation = callingView.getFileEvidenceLocation();
				
								FileEvidence ev = new FileEvidence(id, "textevidence", timestamp, descriptionTextArea.getText(), filename);
								ev.setFileNamePrefix(callingView.getCustomId());
								ev.setPublisher(instance.getBareJID());
								ev.setLocation(fileLocation);
								ev.setNS("mirror:application:activityrecommendationapp:textevidence");
								callingView.attachCreatedEvidence(ev);

								loaderDialog.hide();
								dia.hide();
							} else {		
								loaderDialog.hide();
								errorMessage.addClassName("activeItem");
								errorMessage.setInnerText(instance.errorMessage.fileSubmitError());
							}
						}
					});
					break;
				case FORMPANELFILEINPUT:
					upload = FileUpload.wrap(elem);
					upload.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							String filename = upload.getFilename();
							if (filename.contains("/")) {
								filename = filename.split("/")[filename.split("/").length-1];
							} else if (filename.contains("\\")) {
								filename = filename.split("\\\\")[filename.split("\\\\").length-1];
							}
							
							attachedFileLabel.setInnerHTML(filename);
							fileEditableContent.addClassName("editState");
							fileNameInput.setValue(callingView.getCustomId() + "_" + filename);
							spaceInput.setValue(callingView.getFileEvidenceLocation());
						}
					});
					break;
				case FORMPANELFILENAMEINPUT:
					fileNameInput = TextBox.wrap(elem);
					break;
				case FORMPANELSPACEINPUT:
					spaceInput = TextBox.wrap(elem);
					break;
				case ERRORMESSAGE:
					errorMessage = elem;
					break;
				}
			}
		}
				
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}
		Document.get().getElementById("createNewEvidencePopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("createNewEvidencePopup")));
	}

	/**
	 * Shows the popup.
	 * @param callingView the view which opens this popup.
	 */
	public void showPopup(HasEvidences callingView) {
		if (instance.isFileServiceAvailable()) {
			attachment.getStyle().setVisibility(Visibility.VISIBLE);
		} else {
			attachment.getStyle().setVisibility(Visibility.HIDDEN);
		}
		
		errorMessage.removeClassName("activeItem");
		this.callingView = callingView;
		formPanel.reset();
		attachedFileLabel.setInnerHTML("No file attached.");
		fileEditableContent.removeClassName("editState");
		descriptionTextArea.setText("");
		dia.center();
	}
	
	/**
	 * Creates the xml for the newly created evidence.
	 * @param text the text of this evidence.
	 * @param id the id of this evidence.
	 * @param timestamp the timestamp when this evidence was created.
	 * @return the xml for the newly created evidence.
	 */
	private String createEvidenceXML(String text, String id, String timestamp) {
		String namespace = "mirror:application:activityrecommendationapp:textevidence";
		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		com.google.gwt.xml.client.Element root = Parser.createElement("textevidence", namespace);
		root.setAttribute("id", id);
		root.setAttribute("publisher", instance.getBareJID());
		root.setAttribute("timestamp", timestamp);
		root.setAttribute("modelVersion", "1.0");
		root.setAttribute("cdmVersion", "2.0");
		
		com.google.gwt.xml.client.Element creationInfo = Parser.createElement("creationInfo", namespace);
		com.google.gwt.xml.client.Element personElement = Parser.createElement("person", namespace);
		personElement.appendChild(doc.createTextNode(instance.getBareJID()));
		
		com.google.gwt.xml.client.Element dateElement = Parser.createElement("date", namespace);
		dateElement.appendChild(doc.createTextNode(HasTimestamp.TIMESTAMPFORMAT.format(new Date())));

		creationInfo.appendChild(dateElement);
		creationInfo.appendChild(personElement);
		
		com.google.gwt.xml.client.Element content = Parser.createElement("content", namespace);
		content.appendChild(doc.createTextNode(text));
		
		String[] split = text.split("( |\n)");
		com.google.gwt.xml.client.Element urls = null;
		for (int i=0; i<split.length; ++i) {
			if (split[i].toLowerCase().startsWith("http://") || split[i].toLowerCase().startsWith("https://")) {
				
				com.google.gwt.xml.client.Element url = Parser.createElement("url", namespace);
				url.appendChild(doc.createTextNode(split[i]));
				
				if (urls == null) {
					urls = Parser.createElement("urls", namespace);
				}
				urls.appendChild(url);
			}
		}
		
		root.appendChild(creationInfo);
		root.appendChild(content);
		if (urls != null) {
			root.appendChild(urls);
		}
		return root.toString();
	}
}
