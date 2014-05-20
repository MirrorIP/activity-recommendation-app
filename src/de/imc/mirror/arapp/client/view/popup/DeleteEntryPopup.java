package de.imc.mirror.arapp.client.view.popup;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;

import de.imc.mirror.arapp.client.Entry;
import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;
import de.imc.mirror.arapp.client.view.DiscussionView;

public class DeleteEntryPopup {

	protected enum ElementIds {
		CONTENT("deleteMinutesEntryPopupDisplay"),
		CANCELBUTTON("deleteMinutesEntryCancelButton"),
		DELETEBUTTON("deleteMinutesEntryDeleteButton");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Button cancelButton;
	private Button deleteButton;	
	
	private Element timeLabel;
	private Element textLabel;
	
	private DiscussionView view;	
	
	private int id;
	
	private DialogBox dia;
	
	/**
	 * Creates the dialog to confirm the deletion of an minuteentry.
	 * @param view Instance of the discussionview.
	 */
	public DeleteEntryPopup(final DiscussionView view){
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
				case DELETEBUTTON:
					deleteButton = Button.wrap(elem);
					deleteButton.addClickHandler(new ClickHandler(){

						@Override
						public void onClick(ClickEvent event) {
							view.deleteEntry(DeleteEntryPopup.this.id);
							dia.hide();
						}
						
					});
					break;
				case CONTENT:
					timeLabel = elem.getElementsByTagName("label").getItem(0);
					textLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}

		Document.get().getElementById("deleteMinutesEntryPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("deleteMinutesEntryPopup")));
	}
	
	/**
	 * Opens the popup.
	 * @param entry the entry in question.
	 */
	public void showPopup(Entry entry) {
		this.id = entry.getId();
		timeLabel.setInnerHTML(entry.getFormattedTimestamp(HasTimestamp.LONGDATE));
		textLabel.setInnerHTML(entry.getMessage());
		dia.center();
	}
}
