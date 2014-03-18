package de.imc.mirror.arapp.client.view.popup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.HasEvidences;
import de.imc.mirror.arapp.client.HasSpacesList;
import de.imc.mirror.arapp.client.HasTimestamp;
import de.imc.mirror.arapp.client.view.View;

public class AttachEvidenceDialog extends View implements HasSpacesList{
	
	protected enum ElementIds {
		SEARCHSPACEINPUTFIELD("browseSpacesPopupSpacesListSearchInput"),
		SEARCHSPACEBUTTON("browseSpacesPopupSpacesListSearchButton"),
		SEARCHDATAOBJECTINPUTFIELD("browseSpacesPopupDataObjectListSearchInput"),
		SEARCHDATAOBJECTBUTTON("browseSpacesPopupDataObjectListSearchButton"),
		SPACESLIST("browseSpacesPopupSpacesList"),
		DATAOBJECTSLIST("browseSpacesPopupDataObjectList"),
		PUBLISHERLISTBOX("browseSpacesPopupPublisherSelect"),
		PUBLISHINGDATELISTBOX("browseSpacesPopupPublishingDateSelect"),
		CLOSEBUTTON("browseSpacesPopupCloseButton"),
		ATTACHBUTTON("browseSpacesPopupAttachButton");
		
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
	

	private TextBox spaceSearchBox;
	private Button spaceSearchButton;
	private TextBox dataObjectSearchBox;
	private Button dataObjectSearchButton;	

	private Button attachButton;
	private Element spacesList;
	private Element dataObjectsList;
	
	private ListBox publishersListBox;
	private ListBox dateListBox;
	
	private long time = -1;
	private String spacesTextToSearchFor;
	private String dataObjectsTextToSearchFor;
	
	private Map<String, String> spaces;
	
	private Element evidencesTable;
	private Element checkedItemsLabel;
	

	private List<SimpleCheckBox> checkBoxList;
	private Map<String, Evidence> evidences;
	
	private List<Evidence> evidencesList;
	private List<Evidence> attachedEvidences;
	
	private List<String> evidenceSpaceIds;
	
	private DialogBox dia;
//	private Map<String, JavaScriptObject> dataObjects;
	
	private Map<String, HTML> listElements;
	private Map<String, SimpleCheckBox> checkBoxes;
	
	private HasEvidences callingView;
	
	private int checkedItems;
	
	
	public AttachEvidenceDialog(final ARApp instance) {
		super(instance);
		dia = new DialogBox();
		dia.setGlassEnabled(true);
		dia.setGlassStyleName("transparent");
		
		attachedEvidences = new ArrayList<Evidence>();
		evidenceSpaceIds = new ArrayList<String>();
//		dataObjects = new HashMap<String, JavaScriptObject>();
		listElements = new HashMap<String, HTML>();
		checkBoxes = new HashMap<String, SimpleCheckBox>();
		
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
				case ATTACHBUTTON:
					attachButton = Button.wrap(elem);
					attachButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							callingView.attachEvidences(attachedEvidences);
							dia.hide();
						}
					});
					break;
				case SPACESLIST:
					spacesList = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case DATAOBJECTSLIST:
					dataObjectsList = elem.getElementsByTagName("tbody").getItem(0);
					NodeList<Element> elems = elem.getElementsByTagName("div");
					for (int j=0; j<elems.getLength(); j++) {
						Element child = elems.getItem(j);
						if (child.getClassName().contains("blockContent")) {
							checkedItemsLabel = child;
						}
					}
					break;
				case SEARCHSPACEINPUTFIELD:
					spaceSearchBox = TextBox.wrap(elem);
					spaceSearchBox.addKeyDownHandler(new KeyDownHandler() {
						
						@Override
						public void onKeyDown(KeyDownEvent event) {
							if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
								spacesTextToSearchFor = spaceSearchBox.getText();
							}
							filter();
						}
					});
					break;
				case SEARCHSPACEBUTTON:
					spaceSearchButton = Button.wrap(elem);
					spaceSearchButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (spaceSearchBox != null) {
								spacesTextToSearchFor = spaceSearchBox.getText();
							}
							filter();
						}
					});
					break;
				case SEARCHDATAOBJECTINPUTFIELD:
					dataObjectSearchBox = TextBox.wrap(elem);
					dataObjectSearchBox.addKeyDownHandler(new KeyDownHandler() {
						
						@Override
						public void onKeyDown(KeyDownEvent event) {
							if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
								dataObjectsTextToSearchFor = dataObjectSearchBox.getText();
							}
							buildEvidenceList();
							
						}
					});
					break;
				case SEARCHDATAOBJECTBUTTON:
					dataObjectSearchButton = Button.wrap(elem);
					dataObjectSearchButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (dataObjectSearchBox != null) {
								dataObjectsTextToSearchFor = dataObjectSearchBox.getText();
							}
							buildEvidenceList();
						}
					});
					break;
				case PUBLISHERLISTBOX:
					publishersListBox = ListBox.wrap(elem);
					publishersListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							if (instance.persistenceServiceAvailable()) {
								int index = publishersListBox.getSelectedIndex();
								String publisher = null;
								if (index > 0) {
									publisher = publishersListBox.getValue(index);
								}
								queryDataObjects(evidenceSpaceIds, publisher, time);
							} else {
								buildEvidenceList();
							}
						}
					});
					break;
				case PUBLISHINGDATELISTBOX:
					dateListBox = ListBox.wrap(elem);
					dateListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							if (dateListBox.getSelectedIndex() == dateListBox.getItemCount()-1) {
								time = -1;
							} else {
								time = 1000*60*60*24;
								if (dateListBox.getSelectedIndex() == 1) {
									time = time * 7;
								} else if (dateListBox.getSelectedIndex() == 2) {
									time = time * 30;
								}
							}
							if (instance.persistenceServiceAvailable()) {
								int index = publishersListBox.getSelectedIndex();
								String publisher = null;
								if (index > 0) {
									publisher = publishersListBox.getValue(index);
								}
								queryDataObjects(evidenceSpaceIds, publisher, time);
							} else {
								buildEvidenceList();
							}
						}
					});
				}
			}
		}
		Document.get().getElementById("browseSpacesPopup").addClassName("activeItem");
		Element elem = Document.get().getElementById("browseSpacesPopup");
		
		
		spaces = instance.getSpacesMap();
		List<String> spaceIds = new ArrayList<String>();
		spaceIds.addAll(spaces.keySet());
		if (!instance.persistenceServiceAvailable()) {
			getAllAvailableEvidence(spaceIds);
		}
		buildSpacesList();
		if (errors) {
			Window.alert(errorBuilder.toString());
		}
		
		dia.setWidget(HTML.wrap(elem));
	}
	
	/**
	 * Called to show the popup.
	 * @param callingView instance of the view which called this method.
	 * @param attachedEvidences a list of already attached evidences.
	 */
	public void showPopup(HasEvidences callingView, List<Evidence> attachedEvidences) {
		this.callingView = callingView;
		this.attachedEvidences.clear();
		if (attachedEvidences != null) {
			this.attachedEvidences.addAll(attachedEvidences);
		}
		if (dateListBox.getSelectedIndex() == dateListBox.getItemCount()-1) {
			time = -1;
		} else {
			time = 1000*60*60*24;
			if (dateListBox.getSelectedIndex() == 1) {
				time = time * 7;
			} else if (dateListBox.getSelectedIndex() == 2) {
				time = time * 30;
			}
		}
		spaces = instance.getSpacesMap();
		if (!instance.persistenceServiceAvailable()) {
			List<String> spaceIds = new ArrayList<String>();
			spaceIds.addAll(spaces.keySet());
			getAllAvailableEvidence(spaceIds);
			buildEvidenceList();
		} else {
			List<String> publishers = new ArrayList<String>();
			for (String user: instance.getAllUserJids()) {
				publishers.add(user);
			}
			Collections.sort(publishers);
			publishersListBox.clear();
			publishersListBox.addItem(instance.infoMessage.listBoxAllEntry());
			for (String publisher:publishers) {
				if (instance.getDisplayNameForJid(publisher) != null) {
					publishersListBox.addItem(instance.getDisplayNameForJid(publisher), publisher);
				} else {
					publishersListBox.addItem(publisher, publisher);
				}
			}
			
		}
		updateSpacesList();
		dia.center();
	}
	

	/**
	 * Method to retrieve all DataObjects before creating the AttachEvidenceDialog.
	 * @param view Instance of this class
	 * @param dataObjects initialised Map<String, JavaScriptObject>-object.
	 */
	private native void getAllAvailableEvidence(List<String> spaceIds) /*-{
		var that = this;
		var length = spaceIds.@java.util.ArrayList::size()();
		var amount = spaceIds.@java.util.ArrayList::size()();
		var evidence = @java.util.ArrayList::new()();
		
		var getObjects = function(spaceId) {
			$wnd.dataHandler.queryDataObjectsBySpace(spaceId, [], function(objects) {
				if (objects && objects != null) {
					for (var j=0; j<objects.length; j++) {
						var object = @de.imc.mirror.arapp.client.Parser::parseDataObjectToEvidence(Ljava/lang/String;)(objects[j].toString());
						if (!object || object == null) continue;
						if (object.@de.imc.mirror.arapp.client.Evidence::getId()() == null) continue;
						object.@de.imc.mirror.arapp.client.Evidence::setLocation(Ljava/lang/String;)(spaceId);
						evidence.@java.util.ArrayList::add(Ljava/lang/Object;)(object);
					}
				}
				amount--;
				if (amount == 0)  {
					that.@de.imc.mirror.arapp.client.view.popup.AttachEvidenceDialog::showEvidencePanel(Ljava/util/List;)(evidence);
				}
			});
		}
		$wnd.currentObject = new Object();
		for (var i=0; i<length; i++) {
			var space = spaceIds.@java.util.ArrayList::get(I)(i);
			getObjects(space);
		}
	}-*/;
	
	/**
	 * Builds the evidencelist to show it in the popup.
	 */
	private void buildEvidenceList() {
		dataObjectsList.removeAllChildren();
		
		for (final Evidence ev:evidencesList) {
			if (!instance.persistenceServiceAvailable()) {
				if (!evidenceSpaceIds.contains(ev.getLocationId())) continue;
				if (publishersListBox.getSelectedIndex() > 0) {
					String publisher = publishersListBox.getItemText(publishersListBox.getSelectedIndex());
					if (!publisher.equalsIgnoreCase(instance.getDisplayNameForJid(ev.getPublisher()))) continue;
				}
				if (time >= 0) {
					Date date = new Date();
					long dateTime = date.getTime() - time;
					
					if (HasTimestamp.TIMESTAMPFORMAT.parse(ev.getTimestamp()).getTime() < dateTime) continue;
				}
			}
			if (dataObjectsTextToSearchFor != null && !dataObjectsTextToSearchFor.isEmpty()) {
				if (!ev.getType().toLowerCase().contains(dataObjectsTextToSearchFor.toLowerCase()) &&
						!ev.getFormattedTimestamp(HasTimestamp.LONGDATE).toLowerCase().contains(dataObjectsTextToSearchFor.toLowerCase()) && 
						!ev.getFormattedTimestamp(HasTimestamp.MEDIUMDATE).toLowerCase().contains(dataObjectsTextToSearchFor.toLowerCase())) {
					continue;
				}
			}
			HTML trHTML = HTML.wrap(Document.get().createTRElement());
			trHTML.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					instance.getEvidenceVisualisationPopup().visualizeEvidence(ev);
				}
			});
			
			Element checkTD = Document.get().createTDElement();
			SimpleCheckBox checkBox = SimpleCheckBox.wrap(Document.get().createCheckInputElement());
			checkedItems = 0;
			if (attachedEvidences.contains(ev)) {
				checkBox.setValue(true);
				checkedItems++;
			}
	    	checkedItemsLabel.setInnerText(instance.infoMessage.itemsSelected(checkedItems));
			checkBox.addClickHandler(new ClickHandler() {
			      @Override
			      public void onClick(ClickEvent event) {
			    	  if (((SimpleCheckBox)event.getSource()).getValue()) {
			    		  checkedItems++;
			    		  attachedEvidences.add(ev);
			    	  } else {
			    		  checkedItems--;
			    		  attachedEvidences.remove(ev);
			    	  }
			    	  checkedItemsLabel.setInnerText(instance.infoMessage.itemsSelected(checkedItems));
			    	  event.stopPropagation();
			      }
			});    
			checkTD.appendChild(checkBox.getElement());
			
			Element iconTD = Document.get().createTDElement();
			Element img = Document.get().createImageElement();
			img.setAttribute("src", "img/data-unknown.png");
			iconTD.appendChild(img);
			
			Element nameTD = Document.get().createTDElement();
			nameTD.setInnerHTML(ev.getType());
			

			Element dateTD = Document.get().createTDElement();
			dateTD.setInnerHTML(ev.getFormattedTimestamp(HasTimestamp.MEDIUMDATE));

			trHTML.getElement().appendChild(checkTD);
			trHTML.getElement().appendChild(iconTD);
			trHTML.getElement().appendChild(nameTD);
			trHTML.getElement().appendChild(dateTD);
			
			dataObjectsList.appendChild(trHTML.getElement());
		}
		
	}
	
	/**
	 * Creates and shows the AttachEvidenceDialog.
	 * @param evidenceList
	 */
	private void showEvidencePanel(List<Evidence> evidenceList) {
		if (this.evidencesList != null) {
			this.evidencesList.clear();
		}
		this.evidencesList = evidenceList;
		
		Collections.sort(evidencesList, HasTimestamp.COMPAREAGAINSTTIMESTAMP);
		
		if (!instance.persistenceServiceAvailable()) {
			List<String> publishers = new ArrayList<String>();
			
			for (Evidence ev:evidenceList) {
				if (ev.getPublisher() != null) {
					String publisher = ev.getPublisher().split("/")[0];
					if (!publishers.contains(publisher)) {
						publishers.add(publisher);
					}
				}
			}
			Collections.sort(publishers);
			publishersListBox.clear();
			publishersListBox.addItem(instance.infoMessage.listBoxAllEntry());
			for (String publisher:publishers) {
				if (instance.getDisplayNameForJid(publisher) != null) {
					publishersListBox.addItem(instance.getDisplayNameForJid(publisher), publisher);
				} else {
					publishersListBox.addItem(publisher, publisher);
				}
			}
		}
		
		buildEvidenceList();
	}

	@Override
	public void buildSpacesList() {
		spacesList.removeAllChildren();
		
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
		oldIds.addAll(listElements.keySet());
		oldIds.removeAll(spaceIds);
		if (oldIds.size() > 0) {
			for (String id:oldIds) {
				spacesList.removeChild(listElements.get(id).getElement());
				listElements.remove(id);
				evidenceSpaceIds.remove(id);
			}
		}
		
		String lastId = null;
		for (String id:spaceIds) {
			if (!listElements.containsKey(id)) {
				addSpaceToList(id);
				if (lastId == null) {
					spacesList.insertFirst(listElements.get(id).getElement());
				} else {
					spacesList.insertAfter(listElements.get(id).getElement(), listElements.get(lastId).getElement());
				}
			}
			lastId = id;
		}
//		buildEvidenceList();		
	}
	
	private void addSpaceToList(final String id) {
		HTML trHTML = HTML.wrap(Document.get().createTRElement());
		Element checkTD = Document.get().createTDElement();
		SimpleCheckBox checkBox = SimpleCheckBox.wrap(Document.get().createCheckInputElement());
		checkBox.addClickHandler(new ClickHandler() {
		      @Override
		      public void onClick(ClickEvent event) {
		    	  if (((SimpleCheckBox)event.getSource()).getValue()) {
		    		  evidenceSpaceIds.add(id);
		    	  } else {
		    		  evidenceSpaceIds.remove(id);
		    	  }
		    	  if (instance.persistenceServiceAvailable()) {
						int index = publishersListBox.getSelectedIndex();
						String publisher = null;
						if (index > 0) {
							publisher = publishersListBox.getValue(index);
						}
						queryDataObjects(evidenceSpaceIds, publisher, time);
		    	  } else {
		    		  buildEvidenceList();
		    	  }
		      }
		    });   
		checkTD.appendChild(checkBox.getElement());
		
		Element nameTD = Document.get().createTDElement();
		nameTD.setInnerHTML(spaces.get(id));

		trHTML.getElement().appendChild(checkTD);
		trHTML.getElement().appendChild(nameTD);
		
		spacesList.appendChild(trHTML.getElement());
		listElements.put(id, trHTML);
	}
	
	private void filter() {
		List<String> spaceIds = new ArrayList<String>();
		spaceIds.addAll(spaces.keySet());
		for (String id:spaceIds) {
			if (spacesTextToSearchFor != null && !spaces.get(id).toLowerCase().contains(spacesTextToSearchFor.toLowerCase())) {
				listElements.get(id).setVisible(false);
			} else {
				listElements.get(id).setVisible(true);
			}
		}
	}
	
	private native void queryDataObjects(String space, String publisher, double time) /*-{
		var list = @java.util.ArrayList::new()();
		list.@java.util.List::add(Ljava/lang/Object;)(space);
		this.@de.imc.mirror.arapp.client.view.popup.AttachEvidenceDialog::queryDataObjects(Ljava/util/List;Ljava/lang/String;D)(list, publisher, time);
	}-*/;
	
	private native void queryDataObjects(List<String> spaces, String publisher, double time) /*-{
		var that = this;
		var filter = [];
		if (publisher) {
			filter.push(new $wnd.SpacesSDK.filter.PublisherFilter(publisher));
		}
		if (time > 0) {
			var currentTime = new $wnd.Date().getTime();
			filter.push(new $wnd.SpacesSDK.filter.PeriodFilter(new $wnd.Date(currentTime - time), new $wnd.Date()));
		}
		var spaceIds = [];
		for (var i=0; i<spaces.@java.util.List::size()(); i++) {
			spaceIds.push(spaces.@java.util.List::get(I)(i));
		}
		$wnd.dataHandler.queryDataObjectsBySpaces(spaceIds, filter, function(result) {		
				var evidence = @java.util.ArrayList::new()();	
				if (result && result != null) {
					for (var j=0; j<result.length; j++) {
						var object = @de.imc.mirror.arapp.client.Parser::parseDataObjectToEvidence(Ljava/lang/String;)(result[j].toString());
						if (!object || object == null) continue;
						if (object.@de.imc.mirror.arapp.client.Evidence::getId()() == null) continue;
						
						evidence.@java.util.ArrayList::add(Ljava/lang/Object;)(object);
					}
				}
				that.@de.imc.mirror.arapp.client.view.popup.AttachEvidenceDialog::showEvidencePanel(Ljava/util/List;)(evidence);
			}, function(error) {
				
			});
	}-*/;
	
	
}
