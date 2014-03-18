package de.imc.mirror.arapp.client.view.popup;

import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.view.DiscussionView;

public class ManageDiscussionPopup {


	protected enum ElementIds {
		USERLISTBOX("manageDiscussionPopupModerator"),
		CLOSEBUTTON("manageDiscussionButtonClose");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Button closeButton;	
	
	private ListBox userListBox;
	
	private DiscussionView view;
	
	private DialogBox dia;
	private ARApp instance;
	
	/**
	 * Creates the dialog to change the moderator of a discussion.
	 * @param view Instance of the discussionview.
	 */
	public ManageDiscussionPopup(ARApp instance, final DiscussionView view){
		this.view = view;
		this.instance = instance;
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
				case CLOSEBUTTON:
					closeButton = Button.wrap(elem);
					closeButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (userListBox.getSelectedIndex() > 0) {
								view.setNewModerator(userListBox.getValue(userListBox.getSelectedIndex()));
							}
							dia.hide();
						}
					});
					break;
				case USERLISTBOX:
					userListBox = ListBox.wrap(elem.getElementsByTagName("select").getItem(0));
					break;
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}

		Document.get().getElementById("manageDiscussionPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("manageDiscussionPopup")));
	}
	
	/**
	 * Shows the popup.
	 * @param moderator the current moderatorjid.
	 * @param users list of other participants.
	 */
	public void showPopup(String moderator, List<String> users) {
		userListBox.clear();
		userListBox.addItem(instance.getDisplayNameForJid(moderator), moderator);
		for (String user:users) {
			userListBox.addItem(instance.getDisplayNameForJid(user), user);
		}
		dia.center();
	}
}