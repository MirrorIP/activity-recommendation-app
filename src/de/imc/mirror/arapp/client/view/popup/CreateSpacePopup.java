package de.imc.mirror.arapp.client.view.popup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.view.View;

public class CreateSpacePopup extends View {
	
	
	protected enum ElementIds {
		CANCELBUTTON("createSpacePopupCancelButton"),
		OPENBUTTON("createSpacePopupCreateButton"),
		PARTICIPANTS("createSpacePopupMembersList"),
		DISCUSSIONGROUPS("createSpacePopupMembers"),		
		TITLEINPUT("createSpacePopupTitle"),
		SEARCHBOX("createSpacePopupMembersSearchInput"),
		SEARCHBUTTON("createSpacePopupMembersSearchButton"),
		ADDMEMBERBUTTON("createSpacePopupAddMemberButton"),
		MEMBERINPUT("createSpacePopupMemberInput"),
		ERRORMESSAGE("createSpacePopupErrorMessage");
		
		private String id;
				
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};

	private TextBox titleInput;
	
	private TextBox searchBox;
	private Button searchButton;
	
	private Button cancelButton;
	private Button openButton;	
	
	private Element membersList;
	private Element discussionGroupsTable;
	private Element errorMessage;
	
	private String textToSearchFor;
	
	private DialogBox dia;
	private Map<String, HTML> tableElements;
	private List<String> selectedUsers;
	private List<String> chosenModerators;
	
	private List<String> users;

	private Map<String, SimpleCheckBox> checkBoxes;

	private TextBox memberInput; 
	
	
	public CreateSpacePopup(final ARApp instance) {
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
				case SEARCHBOX:
					searchBox = TextBox.wrap(elem);
					searchBox.addKeyDownHandler(new KeyDownHandler() {
						
						@Override
						public void onKeyDown(KeyDownEvent event) {
							if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
								textToSearchFor = searchBox.getText();
								filter();
							}
						}
					});
					break;
				case SEARCHBUTTON:
					searchButton = Button.wrap(elem);
					searchButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (searchBox != null) {
								textToSearchFor = searchBox.getText();
								filter();
							}
						}
					});
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
				case DISCUSSIONGROUPS:
					discussionGroupsTable = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case OPENBUTTON:
					openButton = Button.wrap(elem);
					openButton.addClickHandler(new ClickHandler(){

						@Override
						public void onClick(ClickEvent event) {
							if (titleInput.getText().replaceAll(" ", "").isEmpty()) {
								errorMessage.setInnerText(instance.errorMessage.emptySpaceName());
								errorMessage.addClassName("activeItem");
//								Window.alert("The name of the space may not be empty.");
								return;
							}
							List<String> members = new ArrayList<String>();
							members.addAll(selectedUsers);
							members.removeAll(chosenModerators);
							createSpace(instance, titleInput.getText(), members, chosenModerators);
							dia.hide();
						}
						
					});
					break;
				case PARTICIPANTS:
					membersList = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case TITLEINPUT:
					titleInput = TextBox.wrap(elem);
					break;
				case ADDMEMBERBUTTON:
					Button.wrap(elem).addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							String user = memberInput.getText();
							if (user.matches("[a-zA-Z0-9.\\-_]{3,50}")) {
								user += "@" + getDomain();
								if (!selectedUsers.contains(user)) {
									selectedUsers.add(user);
								}
								if (users.contains(user)) {
									checkBoxes.get(user).setValue(true);
								}
								updateMembersList();
							} else {
								errorMessage.setInnerText(instance.errorMessage.wrongUserIdForm());
								errorMessage.addClassName("activeItem");
							}						
						}
					});
					break;
				case MEMBERINPUT:
					memberInput = TextBox.wrap(elem);
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

		Document.get().getElementById("createSpacePopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("createSpacePopup")));
		
		tableElements = new HashMap<String, HTML>();
		selectedUsers = new ArrayList<String>();
		chosenModerators = new ArrayList<String>();
		buildMembersTable();
	}
	
	/**
	 * Method to create the spaceslist to choose the persons who are invited to take part in the discussion.
	 */
	private void buildMembersTable() {
		discussionGroupsTable.removeAllChildren();

		users = instance.getAllUserJids();
		users.remove(instance.getBareJID());
		
		Collections.sort(users);
		tableElements.clear();
		checkBoxes = new HashMap<String, SimpleCheckBox>();
		for (final String user: users) {
			HTML trHTML = HTML.wrap(Document.get().createTRElement());
			Element checkTD = Document.get().createTDElement();
			SimpleCheckBox checkBox = SimpleCheckBox.wrap(Document.get().createCheckInputElement());
			checkBox.addClickHandler(new ClickHandler() {
			      @Override
			      public void onClick(ClickEvent event) {
			    	  String name = user;
			    	  if (((SimpleCheckBox)event.getSource()).getValue()) {
			    		  if (!selectedUsers.contains(name)) {
			    			  selectedUsers.add(name);
			    		  }
			    	  } else {
			    		  selectedUsers.remove(user);
			    	  }
			    	  updateMembersList();
			      }
			    });    
			checkBoxes.put(user, checkBox);
			checkTD.appendChild(checkBox.getElement());
			
			Element nameTD = Document.get().createTDElement();
			nameTD.setInnerHTML(instance.getDisplayNameForJid(user));

			trHTML.getElement().appendChild(checkTD);
			trHTML.getElement().appendChild(nameTD);
			
			discussionGroupsTable.appendChild(trHTML.getElement());
			tableElements.put(user, trHTML);
		}
	}
	
	private native void createSpace(ARApp instance, String title, List<String> users, List<String> moderators) /*-{
		var spaceMembers = [];
		for (var i=0; i<users.@java.util.List::size()(); i++) {
			spaceMembers.push(new $wnd.SpacesSDK.SpaceMember(users.@java.util.List::get(I)(i), $wnd.SpacesSDK.Role.MEMBER));
		}
		for (var i=0; i<moderators.@java.util.List::size()(); i++) {
			spaceMembers.push(new $wnd.SpacesSDK.SpaceMember(moderators.@java.util.List::get(I)(i), $wnd.SpacesSDK.Role.MODERATOR));
		}		
		var config = new $wnd.SpacesSDK.SpaceConfiguration($wnd.SpacesSDK.Type.TEAM, title, spaceMembers, $wnd.SpacesSDK.PersistenceType.ON, null);
		$wnd.spaceHandler.createSpace(config, function(space) {
					instance.@de.imc.mirror.arapp.client.ARApp::addSpace(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(space.getId(), title, space);
				}, function(){
					$wnd.console.log("moep");
				});
	}-*/;
	
	/**
	 * If a space was selected or unselected, this method is called to update the momentarily invited people.
	 */
	private void updateMembersList() {
		membersList.removeAllChildren();
		Collections.sort(selectedUsers);
		for (final String user: selectedUsers) {
			final HTML trHTML = HTML.wrap(Document.get().createTRElement());
			Element nameTD = Document.get().createTDElement();
			
			String name = instance.getDisplayNameForJid(user);
			if (name != null) {
				nameTD.setInnerText(name);
			} else {
				if (user.contains("@")) {
					nameTD.setInnerText(user.split("@")[0]);
				} else {
					nameTD.setInnerText(user);
				}
			}
			
			Element idTD = Document.get().createTDElement();
			if (user.contains("@")) {
				idTD.setInnerText(user.split("@")[0]);
			} else {
				idTD.setInnerText(user);
			}
			
			Element checkBoxTD = Document.get().createTDElement();
			SimpleCheckBox moderatorCheckBox = SimpleCheckBox.wrap(Document.get().createCheckInputElement());
			
			moderatorCheckBox.addClickHandler(new ClickHandler() {
			      @Override
			      public void onClick(ClickEvent event) {
			    	  if (((SimpleCheckBox)event.getSource()).getValue()) {
			    		  if (!chosenModerators.contains(user)) {
			    			  chosenModerators.add(user);
			    		  }
			    	  } else {
			    		  chosenModerators.remove(user);
			    	  }
			      }
			    });
			moderatorCheckBox.setValue(chosenModerators.contains(user));
			checkBoxTD.appendChild(moderatorCheckBox.getElement());
			
			Element removeTD = Document.get().createTDElement();
			Element removeButton = Document.get().createPushButtonElement();
			Button.wrap(removeButton).addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					membersList.removeChild(trHTML.getElement());
					chosenModerators.remove(user);
					selectedUsers.remove(user);
					if (checkBoxes.get(user) != null) {
						checkBoxes.get(user).setValue(false);
					}
				}
			});

			if (user.equals(instance.getBareJID())) {
				moderatorCheckBox.setValue(true);
				moderatorCheckBox.setEnabled(false);
			} else {
				removeTD.appendChild(removeButton);
			}

			trHTML.getElement().appendChild(nameTD);
			trHTML.getElement().appendChild(idTD);
			trHTML.getElement().appendChild(checkBoxTD);
			trHTML.getElement().appendChild(removeTD);
			
			membersList.appendChild(trHTML.getElement());
		}
	}
	
	private void filter() {
		for (String user:users) {
			if (textToSearchFor.length() == 0 || user.toLowerCase().contains(textToSearchFor.toLowerCase()) || instance.getDisplayNameForJid(user).toLowerCase().contains(textToSearchFor.toLowerCase())) {
				tableElements.get(user).setVisible(true);
			} else {
				tableElements.get(user).setVisible(false);
			}
		}
	}
	
	/**
	 * Shows the popup.
	 */
	public void showPopup() {
		errorMessage.removeClassName("activeItem");
		selectedUsers.clear();
		chosenModerators.clear();
		
		selectedUsers.add(instance.getBareJID());
		chosenModerators.add(instance.getBareJID());
		
		buildMembersTable();
		updateMembersList();
		dia.center();
	}
	
	public void update() {
		buildMembersTable();
	}

	private native String getDomain() /*-{
		return $wnd.connection.getConfiguration().getDomain();
	}-*/;
	
	
}
