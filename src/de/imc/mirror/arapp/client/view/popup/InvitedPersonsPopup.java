package de.imc.mirror.arapp.client.view.popup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;

import de.imc.mirror.arapp.client.ARApp;

public class InvitedPersonsPopup {


	protected enum ElementIds {
		USERSLIST("invitedPersonsPopupPersons"),
		CLOSEBUTTON("invitedPersonsPopupCloseButton");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Button closeButton;	
	
	private Element userListBox;
	
	private DialogBox dia;
	
	private ARApp instance;
	
	private List<String> invitedPersons;
	
	/**
	 * Creates the dialog to change the moderator of a discussion.
	 * @param view Instance of the discussionview.
	 */
	public InvitedPersonsPopup(ARApp instance){
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
							dia.hide();
						}
					});
					break;
				case USERSLIST:
					userListBox = elem.getElementsByTagName("tbody").getItem(0); 
					break;
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}

		Document.get().getElementById("invitedPersonsPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("invitedPersonsPopup")));
	}
	
	public void setInvitedPersons(List<String> invitedPersons) {
		this.invitedPersons = invitedPersons;
	}
	
	
	public void showPopup(String moderator, List<String> participatingPersons) {
		showPopup(moderator, invitedPersons, participatingPersons);
	}
	
	public void buildParticipantsList(String moderator, List<String> invitedPersons, List<String> participatingPersons) {
		if (invitedPersons == null) return;
		userListBox.removeAllChildren();
		Comparator<String> compare = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String display1 = instance.getDisplayNameForJid(o1);
				String display2 = instance.getDisplayNameForJid(o2);
				if (display1 == null && display2 == null) {
					return o1.compareToIgnoreCase(o2);
				} else if (display1 == null) {
					return o1.compareToIgnoreCase(display2);
				} else if (display2 == null) {
					return display1.compareToIgnoreCase(o2);
				} else {
					return display1.compareToIgnoreCase(display2);
				}
			}
		};
		Collections.sort(invitedPersons, compare);
		for (String user:invitedPersons) {
			Element tr = Document.get().createTRElement();
			Element nameElement = Document.get().createTDElement();
			Element statusElement = Document.get().createTDElement();
			
			if (instance.getDisplayNameForJid(user) != null) {
				nameElement.setInnerText(instance.getDisplayNameForJid(user));
			} else {
				nameElement.setInnerText(user.split("@")[0]);
			}
			if (participatingPersons.contains(user)) {
				statusElement.setInnerText(instance.infoMessage.available());
			} else {
				statusElement.setInnerText(instance.infoMessage.offline());				
			}
			if (user.equals(moderator)) {
				nameElement.setClassName("moderator");
			}
			tr.appendChild(nameElement);
			tr.appendChild(statusElement);
			userListBox.appendChild(tr);
		}
	}
	
	/**
	 * Shows the popup.
	 * @param moderator the current moderatorjid.
	 * @param users list of other participants.
	 */
	public void showPopup(String moderator, List<String> invitedPersons, List<String> participatingPersons) {
		buildParticipantsList(moderator, invitedPersons, participatingPersons);
		dia.center();
	}
	
	public void update(String moderator, List<String> participatingPersons) {
		buildParticipantsList(moderator, invitedPersons, participatingPersons);
	}
	
	
}
