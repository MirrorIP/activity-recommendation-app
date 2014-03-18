package de.imc.mirror.arapp.client.view.popup;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;

import de.imc.mirror.arapp.client.RecommendationStatus;
import de.imc.mirror.arapp.client.RecommendationStatus.Status;
import de.imc.mirror.arapp.client.view.ManageTab;

public class UpdateUserStatusPopup {
	
	protected enum ElementIds {
		UPDATESTATUSLISTBOX("updateUserStatusSelect"),
		SUBMITBUTTON("updateUserStatusButtonSubmit"),
		CANCELBUTTON("updateUserStatusButtonCancel");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};

	private Button cancelButton;
	private Button submitButton;	
	
	private ListBox userStatusListBox;
	
	private ManageTab view;
	
	private DialogBox dia;
	
	private String user;
	private String ref;
	
	/**
	 * Creates the dialog to update the status of a user.
	 * @param view instance of the ManageTab.
	 */
	public UpdateUserStatusPopup(final ManageTab view){
		this.view = view;
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
				case CANCELBUTTON:
					cancelButton = Button.wrap(elem);
					cancelButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							dia.hide();
						}
					});
					break;
				case SUBMITBUTTON:
					submitButton = Button.wrap(elem);
					submitButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							Status newStatus;
							int statusIndex = userStatusListBox.getSelectedIndex();
							if (statusIndex == 0) {
								newStatus = Status.OPEN;
							} else if (statusIndex == 1) {
								newStatus = Status.SOLVED;
							} else {
								newStatus = Status.IGNORED;
							}
							RecommendationStatus status = new RecommendationStatus(newStatus, user, ref);
							view.publishStatus(status);
							user = null;
							ref = null;
							dia.hide();
						}
					});
					break;
				case UPDATESTATUSLISTBOX:
					userStatusListBox = ListBox.wrap(elem.getElementsByTagName("select").getItem(0));
					break;
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}

		Document.get().getElementById("updateUserStatusPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("updateUserStatusPopup")));
	}
	
	/**
	 * Shows the popup.
	 * setUser(String user) and setRef(String ref) have to be called beforehand.
	 */
	public void showPopup() {
		if (user == null || ref == null) {
			return;
		}
		dia.center();
	}
	
	/**
	 * Sets the user the status should be updated.
	 * @param user the jid of the user.
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	/**
	 * Sets the custom id of the recommendation for which a new status should be created.
	 * @param ref the custom id of the recommendation.
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}
}
