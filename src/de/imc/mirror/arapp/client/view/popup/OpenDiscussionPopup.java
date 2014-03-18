package de.imc.mirror.arapp.client.view.popup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
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
import de.imc.mirror.arapp.client.HasSpacesList;
import de.imc.mirror.arapp.client.HasTimestamp;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.SessionObject;
import de.imc.mirror.arapp.client.view.View;

public class OpenDiscussionPopup extends View implements HasSpacesList{
	
	
	protected enum ElementIds {
		CANCELBUTTON("createDiscussionPopupCancelButton"),
		OPENBUTTON("createDiscussionPopupOpenButton"),
		PARTICIPANTS("createDiscussionPopupParticipants"),
		DISCUSSIONGROUPS("createDiscussionPopupDiscussionGroups"),		
		TITLEINPUT("createDiscussionPopupTitle"),
		SEARCHBOX("createDiscussionPopupDiscussionGroupsSearchInput"),
		SEARCHBUTTON("createDiscussionPopupDiscussionGroupsSearchButton"),
		CREATESPACEBUTTON("createDiscussionPopupCreateSpaceButton"),
		ERRORMESSAGE("createDiscussionPopupErrorMessage");
		
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
	
	private Element participantsLabel;
	private Element discussionGroupsTable;
	private Element errorMessage;
	
	private String textToSearchFor;
	
	private DialogBox dia;
	private Map<String, SimpleCheckBox> groupCheckBoxes;
	private List<String> selectedSpaces;
	private Map<String, String> spaces;
	private Map<String, HTML> tableElements;

	private RecommendationObject recommendationObject;
	
	
	public OpenDiscussionPopup(final ARApp instance) {
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
							titleInput.setText("");
							recommendationObject = null;
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
							List<String> errorList = new ArrayList<String>();
							if (titleInput.getText().isEmpty() || titleInput.getText().replaceAll(" ", "").isEmpty()) {
								errorList.add(instance.errorMessage.emptyTitle());
							}
							if (selectedSpaces.isEmpty()) {
								errorList.add(instance.errorMessage.noDiscussionGroupChosen());
							}
							if (instance.getDiscussions().containsKey(titleInput.getText())) {
								errorList.add(instance.errorMessage.titleAlreadyUsed());
							}

							StringBuilder builder = new StringBuilder();	
							if (errorList.size() > 0) {							
								builder.insert(0, "The following problems were found:");
								for (String error:errorList) {
									builder.append("<br>").append(error);
								}
								errorMessage.setInnerHTML(builder.toString());
								errorMessage.addClassName("activeItem");
								return;
							}
							
							List<String> members = new ArrayList<String>();							
							Map<String, JavaScriptObject> spacesMap = instance.getCompleteSpacesMap();
							
//							StringBuilder builder = new StringBuilder();
							
							Comparator<String> compare = new Comparator<String>() {

								@Override
								public int compare(String o1, String o2) {
									String name1 = spaces.get(o1);
									String name2 = spaces.get(o2);
									return name1.compareTo(name2);
								}
							};
							Collections.sort(selectedSpaces, compare);
							
							for(String id:selectedSpaces) {
								List<String> usersOfSpace = getSpaceMembers(spacesMap.get(id));
								builder.append(spaces.get(id)).append(", ");
								for (String jid:usersOfSpace) {
									if (!members.contains(jid) && !jid.equals(instance.getBareJID())) {
										members.add(jid);
									}
								}
							}
							builder.setLength(builder.length() - 2);
							String discussionGroupsString = builder.toString(); 
							
							

							if (recommendationObject != null) {
								builder.setLength(0);
								Map<String, String> spaces = instance.getSpacesMap();
								if (recommendationObject.getTargetSpaces() != null) {
									for (String spaceId:recommendationObject.getTargetSpaces()) {
										if (spaces.containsKey(spaceId)) {
											builder.append(spaces.get(spaceId)).append(",");
										}
									}
									if (builder.length() > 0) {
										builder.setLength(builder.length()-1);
									}
								}
								String targetGroups = builder.toString();
								
	
								Map<String, String> infos = new HashMap<String, String>();
								infos.put("targetGroups", targetGroups);
								infos.put("discussionGroup", discussionGroupsString);
								infos.put("moderator", instance.getBareJID());
	
								long diff = new Date().getTime();
								String timestamp = HasTimestamp.TIMESTAMPFORMAT.format(new Date(diff));
								RecommendationObject startingPoint = recommendationObject.prepareAsPreviousRevision();
								startingPoint.setTitle(titleInput.getText());
								String sessionText = SessionObject.createCompleteSessionObject(startingPoint, infos, timestamp).toString();
								instance.addSessionObject(titleInput.getText(), sessionText);
							}
							
							createDiscussion(instance, titleInput.getText(), discussionGroupsString, members);
							titleInput.setText("");
							selectedSpaces.clear();
							recommendationObject = null;
							dia.hide();
						}
						
					});
					break;
				case PARTICIPANTS:
					participantsLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case TITLEINPUT:
					titleInput = TextBox.wrap(elem);
					break;
				case CREATESPACEBUTTON:
					Button.wrap(elem).addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							instance.getCreateSpacePopup().showPopup();
						}
						
					});
				case ERRORMESSAGE:
					errorMessage = elem;
					break;
				}
			}
		}
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}

		Document.get().getElementById("createDiscussionPopup").addClassName("activeItem");
		dia.setWidget(HTML.wrap(Document.get().getElementById("createDiscussionPopup")));
		
		groupCheckBoxes = new HashMap<String, SimpleCheckBox>();
		tableElements = new HashMap<String, HTML>();
		selectedSpaces = new ArrayList<String>();
		buildSpacesList();
	}
	
	/**
	 * Method to create the spaceslist to choose the persons who are invited to take part in the discussion.
	 */
	public void buildSpacesList() {
		discussionGroupsTable.removeAllChildren();
		groupCheckBoxes.clear();
		selectedSpaces.clear();
		
		spaces = instance.getSpacesMap();
		
		List<String> spaceIds = new ArrayList<String>();
		spaceIds.addAll(spaces.keySet());
		
		Comparator<String> compare = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String name1 = spaces.get(o1);
				String name2 = spaces.get(o2);
				return name1.compareToIgnoreCase(name2);
			}
		};
		Collections.sort(spaceIds, compare);
		
		tableElements.clear();
		
		for (final String id:spaceIds) {
			addSpaceToList(id);
		}
	}

	@Override
	public void updateSpacesList() {
		spaces = instance.getSpacesMap();
		List<String> spaceIds = new ArrayList<String>();
		spaceIds.addAll(spaces.keySet());
		
		Comparator<String> compare = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				String name1 = spaces.get(o1);
				String name2 = spaces.get(o2);
				return name1.compareToIgnoreCase(name2);
			}
		};
		Collections.sort(spaceIds, compare);
		
		List<String> oldIds = new ArrayList<String>();
		oldIds.addAll(groupCheckBoxes.keySet());
		oldIds.removeAll(spaceIds);
		if (oldIds.size() > 0) {
			for (String id:oldIds) {
				discussionGroupsTable.removeChild(tableElements.get(id).getElement());
				tableElements.remove(id);
				groupCheckBoxes.remove(id);
				selectedSpaces.remove(id);
			}
		}
		
		String lastId = null;
		if (!tableElements.keySet().containsAll(spaceIds)) {
			for (String id:selectedSpaces) {
				groupCheckBoxes.get(id).setValue(false);
			}
			selectedSpaces.clear();
			for (String id:spaceIds) {
				if (!tableElements.containsKey(id)) {
					selectedSpaces.add(id);
					addSpaceToList(id);
					if (lastId == null) {
						discussionGroupsTable.insertFirst(tableElements.get(id).getElement());
					} else {
						discussionGroupsTable.insertAfter(tableElements.get(id).getElement(), tableElements.get(lastId).getElement());
					}
					tableElements.get(id).getElement().scrollIntoView();
				}
				lastId = id;
			}
		}
		updateParticipantsLabel();
	}
	
	private void addSpaceToList(final String id) {
		HTML trHTML = HTML.wrap(Document.get().createTRElement());
		Element checkTD = Document.get().createTDElement();
		SimpleCheckBox checkBox = SimpleCheckBox.wrap(Document.get().createCheckInputElement());
		checkBox.addClickHandler(new ClickHandler() {
		      @Override
		      public void onClick(ClickEvent event) {
		    	  if (((SimpleCheckBox)event.getSource()).getValue()) {
		    		  selectedSpaces.add(id);
		    	  } else {
		    		  selectedSpaces.remove(id);
		    	  }
		    	  updateParticipantsLabel();
		      }
		    });    
		if (selectedSpaces.contains(id)) {
			checkBox.setValue(true);
		}
		checkTD.appendChild(checkBox.getElement());
		
		Element nameTD = Document.get().createTDElement();
		nameTD.setInnerHTML(spaces.get(id));

		trHTML.getElement().appendChild(checkTD);
		trHTML.getElement().appendChild(nameTD);
		
		discussionGroupsTable.appendChild(trHTML.getElement());
		tableElements.put(id, trHTML);
		groupCheckBoxes.put(id, checkBox);
	}
	
	/**
	 * Creates a pubsub node on which the discussion will take place on.
	 * @param instance instance of the ARApp class.
	 * @param title the title of the new discussion-
	 * @param groupsString the spacenames of the spaces that were selected. The names are seperated by a ",". 
	 * @param members a list of all jid of the user who are allowed to participate in the discussion.
	 */
	private native void createDiscussion(ARApp instance, String title, String groupsString, List<String> members) /*-{
		var that = this;
		var user = $wnd.connection.getCurrentUser().getBareJID();	
		var options = [];
		var conn = $wnd.connection.getXMPPConnection();
		options['pubsub#access_model'] = 'whitelist';
		options['pubsub#publish_model'] = 'publishers';
		options['pubsub#persist_items'] = '1';
		options['pubsub#send_item_subscribe'] = '0';
		options['pubsub#max_items'] = '10';
		conn.pubsub.createNode(title, options, function(){
			conn.pubsub.subscribe(title, null, function(result){return false;}, null, null, true);			
			for (var i=0; i<members.@java.util.List::size()(); i++) {
				conn.pubsub.setAffiliation(title, members.@java.util.List::get(I)(i), "publisher", function(){});
			}
			instance.@de.imc.mirror.arapp.client.ARApp::startDiscussion(Ljava/lang/String;Ljava/lang/String;)(title, groupsString);
			for (var i=0; i<members.@java.util.List::size()(); i++) {
				that.@de.imc.mirror.arapp.client.view.popup.OpenDiscussionPopup::sendMessage(Ljava/lang/String;Ljava/lang/String;)(title, members.@java.util.List::get(I)(i));
			}
		});
	}-*/;
	

	private native void sendMessage(String discussionNode, String bareJid) /*-{			
        var that = $wnd.connection.getXMPPConnection();
        var iqid = that.getUniqueId("subscribenode");

        var jid = $wnd.connection.getCurrentUser().getFullJID();

        var iq = $wnd.$msg({from:jid, to:bareJid, id:iqid})
          .c('discussionnode', { 'node': discussionNode });

        //add the event handler to receive items
        that.addHandler(function(result){return false;}, null, 'message', null, null, null);
        that.sendIQ(iq.tree(), null, null);
	}-*/;

	
	/**
	 * If a space was selected or unselected, this method is called to update the momentarily invited people.
	 */
	private void updateParticipantsLabel() {
		Map<String, JavaScriptObject> spaces = instance.getCompleteSpacesMap();
		List<String> users = new ArrayList<String>();
		for(String id:selectedSpaces) {
			List<String> usersOfSpace = getSpaceMembers(spaces.get(id));
			for (String jid:usersOfSpace) {
				String userName = instance.getDisplayNameForJid(jid);
				if (!users.contains(userName)) {
					users.add(userName);
				}
			}
		}
		Collections.sort(users, COMPARESTRINGIGNORECASE);
		participantsLabel.setInnerText(instance.infoMessage.userList(users));
	}

	/**
	 * Method to return all members of a space.
	 * @param space the space to get the members for. It is a "Space" object from the Javascript Spaces-SDK.
	 * @return a list with all jids of all the members of the space.
	 */
	private native List<String> getSpaceMembers(JavaScriptObject space) /*-{
		var list = @java.util.ArrayList::new()();
		var members = space.getMembers();
		for (var i=0; i<members.length; i++) {
			list.@java.util.List::add(Ljava/lang/Object;)( members[i].getJID());
		}
		return list;
	}-*/;
	
	private void filter() {
		for (String id:groupCheckBoxes.keySet()) {
			if (textToSearchFor.length() == 0 || spaces.get(id).toLowerCase().contains(textToSearchFor.toLowerCase())) {
				tableElements.get(id).setVisible(true);
			} else {
				tableElements.get(id).setVisible(false);
			}
		}
	}
	
	/**
	 * The given recommendationobject is saved in a variable. If this variable is not <code>null</code> the new discussion will be seen as a new version of a previous recommendation.
	 * After the popup is closed any set recommendationObject will be discarded, to avoid unwanted newer versions of recommendations.
	 * @param obj the recommendation to use for opening a new discussion.
	 */
	public void setPreviousRecommendation(RecommendationObject obj) {
		this.recommendationObject = obj;
		titleInput.setText(obj.getTitle());
	}
	
	/**
	 * Shows the popup.
	 */
	public void showPopup() {
		errorMessage.removeClassName("activeItem");
		buildSpacesList();
		participantsLabel.setInnerHTML("-");
		dia.center();
	}

}
