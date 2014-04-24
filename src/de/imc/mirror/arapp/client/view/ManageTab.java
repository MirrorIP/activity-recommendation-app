package de.imc.mirror.arapp.client.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Entry;
import de.imc.mirror.arapp.client.Evidence;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.HasTimestamp;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.RecommendationPanel;
import de.imc.mirror.arapp.client.RecommendationStatus;
import de.imc.mirror.arapp.client.RecommendationStatus.Status;
import de.imc.mirror.arapp.client.view.popup.UpdateUserStatusPopup;

public class ManageTab extends RecommendationsOverviewView {

	protected enum ElementIds {
		CHANGESTATUSLISTBOX("manageChangeStatusSelect"),
		CREATENEWBUTTON("manageCreateNewButton"),
		DISCUSSBUTTON("manageDiscussButton"),
		
		STATUSSELECT("manageStatusSelect"),
		TARGETPERSONSELECT("manageTargetPersonSelect"),
		
		UPDATEBUTTON("manageUpdateButton"),
		VERSIONLISTBOX("manageSummaryVersionSelect"),
		VIEWEXPERIENCESTAB("tabManageViewExperiences"),
		
		PROGRESSMANAGETAB("tabManageProgress"),
		PROGRESSMANAGE("manageProgress"),
		PROGRESSMANAGESTATISTICSLABEL("manageProgressGeneralStatistics"),
		PROGRESSMANAGELIST("manageProgressUserStatusInformation"),
		EVIDENCESMANAGETAB("tabManageEvidences"),
		EVIDENCESMANAGE("manageEvidences"),
		EVIDENCES("manageEvidencesAttachedEvidences"),

		VIEWEXPERIENCE("manageViewExperiences"),
		MINUTESMANAGETAB("tabManageMinutes"),
		MINUTESMANAGE("manageMinutes"),
		
		MINUTESLIST("manageMinutesList"),
		
		REMOVEBUTTON("manageRemoveRecommendationButton");
		
		private String id;
		
		private ElementIds(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	};
	
	private Map<String, List<Experience>> experiences;
	
	private RecommendationObject recomm;
	
	private Map<String, List<Evidence>> evidencesMap;
	
	private List<String> evidencesNotAvailable = new ArrayList<String>();

	private Map<String, List<String>> targetPersons;

	private HTML progressTab;
	private HTML evidencesTab;
	private HTML minutesTab;

	private Element progressPanel;
	private Element evidencesPanel;
	private Element minutesPanel;
	
	private ListBox statusListBox;
	private ListBox targetPersonListBox;
	private ListBox versionListBox;
	private ListBox changeStatusListBox;
	
	private Button createNewButton;
	private Button updateButton;
	private Button discussButton;
	private Button removeButton;
	
	private boolean filterStatus;
	private boolean filterTargetPerson;
	
	private Element recommendationsList;
	private Element minutesList;
	private Element evidencesList;

	private Element progressList;

	private Element progressStatisticsLabel;

	private Element evidencesLabel;

	public ManageTab(ARApp instance) {
		super(instance);
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
				case VIEWEXPERIENCESTAB:
					viewExperiencesTab = HTML.wrap(elem);
					viewExperiencesTab.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (progressTab == null || evidencesTab == null || minutesTab == null || viewExperience == null ||
									progressPanel == null || evidencesPanel == null || minutesPanel == null) return;
							viewExperiencesTab.getElement().setClassName("activeItem");
							viewExperience.addClassName("activeItem");

							progressTab.getElement().removeClassName("activeItem");
							progressPanel.removeClassName("activeItem");
							evidencesTab.getElement().removeClassName("activeItem");
							evidencesPanel.removeClassName("activeItem");
							minutesTab.getElement().removeClassName("activeItem");
							minutesPanel.removeClassName("activeItem");
						}
					});
					break;
				case PROGRESSMANAGETAB:
					progressTab = HTML.wrap(elem);
					progressTab.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (viewExperiencesTab == null || evidencesTab == null || minutesTab == null || viewExperience == null ||
									progressPanel == null || evidencesPanel == null || minutesPanel == null) return;
							progressTab.getElement().setClassName("activeItem");
							progressPanel.addClassName("activeItem");

							viewExperiencesTab.getElement().removeClassName("activeItem");
							viewExperience.removeClassName("activeItem");
							evidencesTab.getElement().removeClassName("activeItem");
							evidencesPanel.removeClassName("activeItem");
							minutesTab.getElement().removeClassName("activeItem");
							minutesPanel.removeClassName("activeItem");
						}
					});
					break;
				case EVIDENCESMANAGETAB:
					evidencesTab = HTML.wrap(elem);
					evidencesTab.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (progressTab == null || viewExperiencesTab == null || minutesTab == null || viewExperience == null ||
									progressPanel == null || evidencesPanel == null || minutesPanel == null) return;
							evidencesTab.getElement().setClassName("activeItem");
							evidencesPanel.addClassName("activeItem");

							progressTab.getElement().removeClassName("activeItem");
							progressPanel.removeClassName("activeItem");
							viewExperiencesTab.getElement().removeClassName("activeItem");
							viewExperience.removeClassName("activeItem");
							minutesTab.getElement().removeClassName("activeItem");
							minutesPanel.removeClassName("activeItem");
						}
					});
					break;
				case MINUTESMANAGETAB:
					minutesTab = HTML.wrap(elem);
					minutesTab.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (progressTab == null || evidencesTab == null || viewExperiencesTab == null || viewExperience == null ||
									progressPanel == null || evidencesPanel == null || minutesPanel == null) return;
							minutesTab.getElement().setClassName("activeItem");
							minutesPanel.addClassName("activeItem");

							progressTab.getElement().removeClassName("activeItem");
							progressPanel.removeClassName("activeItem");
							evidencesTab.getElement().removeClassName("activeItem");
							evidencesPanel.removeClassName("activeItem");
							viewExperiencesTab.getElement().removeClassName("activeItem");
							viewExperience.removeClassName("activeItem");
						}
					});
					break;
				case VIEWEXPERIENCE:
					viewExperience = elem;
					break;
				case EVIDENCESMANAGE:
					evidencesPanel = elem;
					break;
				case EVIDENCES:
					evidencesLabel = elem.getElementsByTagName("div").getItem(0);
					evidencesList = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case MINUTESMANAGE:
					minutesPanel = elem;
					break;
				case PROGRESSMANAGE:
					progressPanel = elem;
					break;
				case STATUSSELECT:
					statusListBox = ListBox.wrap(elem);
					statusListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							if (statusListBox.getSelectedIndex() > 0) {
								filterStatus = true;
							} else {
								filterStatus = false;
							}
							filter();
						}
					});	
					break;
				case TARGETPERSONSELECT:
					targetPersonListBox = ListBox.wrap(elem);
					targetPersonListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							if (targetPersonListBox.getSelectedIndex() > 0) {
								filterTargetPerson = true;
							} else {
								filterTargetPerson = false;
							}
							filter();
						}
					});	
					break;
				case CHANGESTATUSLISTBOX:
					changeStatusListBox = ListBox.wrap(elem);
					changeStatusListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							changeStatus();
						}
					});	
					break;
				case CREATENEWBUTTON:
					createNewButton = Button.wrap(elem);
					createNewButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getOpenDiscussionPopup().showPopup();
						}
					});
					break;
				case UPDATEBUTTON:
					updateButton = Button.wrap(elem);
					updateButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getUpdateRecommendationPopup().setRecommendationObject(recomm);
							instance.getUpdateRecommendationPopup().showPopup();
						}
					});
					break;
				case VERSIONLISTBOX:
					versionListBox = ListBox.wrap(elem);
					versionListBox.addChangeHandler(new ChangeHandler() {
						
						@Override
						public void onChange(ChangeEvent event) {
							int index = versionListBox.getSelectedIndex();
							showDetails(versionListBox.getValue(index));
						}
					});	
					break;
				case DISCUSSBUTTON:
					discussButton = Button.wrap(elem);
					discussButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							instance.getOpenDiscussionPopup().setPreviousRecommendation(recomm);
							instance.getOpenDiscussionPopup().showPopup();
							
						}
					});
					break;
				case MINUTESLIST:
					minutesList = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case PROGRESSMANAGELIST:
					progressList = elem.getElementsByTagName("tbody").getItem(0);
					break;
				case PROGRESSMANAGESTATISTICSLABEL:
					progressStatisticsLabel = elem.getElementsByTagName("div").getItem(0);
					break;
				case REMOVEBUTTON:
					removeButton = Button.wrap(elem);
					removeButton.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							if (!recomm.getState() && instance.getBareJID().equals(recomm.getPublisher())) {
								//TODO
								if (Window.confirm("Do you really want to delete this Recommendation?")) {
									setDeleteStatus(recomm);
								}
							}
						}
					});
				}
			}
		}	
		
		SharedElements.setManage(true);
		super.initializeSharedVariables();
		
		if (errors) {
			Window.alert(errorBuilder.toString());
		}	
		Element contentElem = Document.get().getElementById("contentManage");
		NodeList<Element> elems = contentElem.getElementsByTagName("ul");
		if (elems != null) {
			for (int i=0; i<elems.getLength(); i++) {
				Element elem = elems.getItem(i);
				if ("recommendationList".equalsIgnoreCase(elem.getClassName())) {
					elem.removeAllChildren();
					recommendationsList = elem;
					break;
				}
			}
		}
		
		filterStatus = (statusListBox.getSelectedIndex() > 0);
	}
	
	public void setDeleteStatus(RecommendationObject object) {
		object.setDeleted();
		publishUpdatedVersion(object);
		
		Map<String, String> refMap = instance.getRefMap();
		String currentId = object.getCustomId();
			
		while (refMap.containsKey(currentId)) {
			currentId = refMap.get(currentId);
			if (recommObjects.containsKey(currentId)) {
				RecommendationObject recomm = recommObjects.get(currentId);
				if (recomm != null && !recomm.deleted()) {
					if (instance.getBareJID().equals(recomm.getPublisher())) {
						recomm.setDeleted();
						publishUpdatedVersion(recomm);
					} else {
						Map<String, String> spaces = instance.getSpacesMap();
						for (String space:recomm.getTargetSpaces()) {
							if (spaces.containsKey(space)) {
								recomm.setToDelete();
								publish(space, recomm.toDataObject());
							}
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * Changes the status of the recommendation and publishes it.
	 */
	public void changeStatus() {	
		recomm.setState(!recomm.getState());
		publishUpdatedVersion(recomm);
	}
	
	/**
	 * Publishes an updated version of the recommendation.
	 * @param object the updated version of the recommendation.
	 */
	public void updateRecommendation(RecommendationObject object) {
		recomm = object;
		publishUpdatedVersion(recomm);
	}
	
	/**
	 * Used to publish the changes. Called from changeStatus or updateRecommendation.
	 */
	private void publishUpdatedVersion(RecommendationObject recomm) {
		List<String> spaces = recomm.getTargetSpaces();	
		if (spaces == null || spaces.isEmpty()) return;
		for (String spaceId:spaces) {
			publish(spaceId, recomm.toDataObject());
		}
	}
 	
	@Override
	public void showDetails(final String id){
		RecommendationObject newRec = recommObjects.get(id);
		if (newRec == null) return;
		recomm = newRec;
		super.showDetails(id);
		

		if (!instance.getBareJID().equals(recomm.getPublisher())) {
			changeStatusListBox.setEnabled(false);
			updateButton.setVisible(false);
			removeButton.setVisible(false);
		} else {
			changeStatusListBox.setEnabled(true);
			updateButton.setVisible(true);
			if (instance.persistenceServiceAvailable()) {
				if (!recomm.getState()) {
					removeButton.setVisible(true);
				} else {
					removeButton.setVisible(false);
				}
			} else {
				removeButton.setVisible(false);
			}
		}
		
		Map<String, Experience> exps = instance.getExperiencesForRecommendation(id);
		if (exps != null) {
			List<Experience> expList = new ArrayList<Experience>();
			expList.addAll(exps.values());
			showExperiences(expList, true);
		} else {
			showExperiences(new ArrayList<Experience>(), true);
		}
		
		minutesList.removeAllChildren();
		evidencesList.removeAllChildren();
		
		List<Entry> minutes = recomm.getEntries();
		for (Entry entry:minutes) {
			Element tr = Document.get().createTRElement();
			
			Element dateTD = Document.get().createTDElement();
			dateTD.setInnerHTML(entry.getFormattedTimestamp(HasTimestamp.MEDIUMDATE));
			
			Element textTD = Document.get().createTDElement();
			textTD.setInnerHTML(entry.getMessage());
			
			tr.appendChild(dateTD);
			tr.appendChild(textTD);
			minutesList.appendChild(tr);
		}
		
		List<Evidence> evidences = recomm.getRelatedEvidence();
		if (evidences != null) {
			for (final Evidence ev:evidences) {
				Element tr = Document.get().createTRElement();

				HTML trHTML = HTML.wrap(tr);
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
				
				Element textTD = Document.get().createTDElement();
				textTD.setInnerHTML(ev.getType());
				
				Element dateTD = Document.get().createTDElement();
				dateTD.setInnerHTML(ev.getFormattedTimestamp(HasTimestamp.LONGDATE));
				
				tr.appendChild(iconTD);
				tr.appendChild(textTD);
				tr.appendChild(dateTD);
				evidencesList.appendChild(tr);
			}
		}
		evidencesLabel.setInnerText(instance.infoMessage.manageAttachedEvidences(evidences!=null? evidences.size():0));
		
		progressList.removeAllChildren();
		
		Map<String, Map<String, RecommendationStatus>> stati = instance.getRecommendationStati();
		List<String> spaceIds = recomm.getTargetSpaces();
		Map<String, JavaScriptObject> spaces = instance.getCompleteSpacesMap();
		if (stati != null && spaceIds != null && spaces != null) {
			Set<String> targetPersons = new HashSet<String>();
			for (String spaceId: spaceIds) {
				if (!spaces.containsKey(spaceId)) continue;
				List<String> users = getSpaceMembers(spaces.get(spaceId));
				targetPersons.addAll(users);
			}
			
			List<String> targetPersonsList = new ArrayList<String>();
			targetPersonsList.addAll(targetPersons);
			Collections.sort(targetPersonsList);
			int solved = 0;
			int ignored = 0;
			
			for (String person:targetPersonsList) {
				String status;
				String timestamp;
				RecommendationStatus recStatus;
				if (!stati.containsKey(person) || stati.get(person) == null) {
					recStatus = null;
				} else {
					recStatus = stati.get(person).get(id);
				}
				if (recStatus == null) {
					timestamp = recomm.getTimestamp();
				} else {
					timestamp = recStatus.getTimestamp();
					if (recStatus.getStatus() == Status.SOLVED) {
						solved++;
					} else if (recStatus.getStatus() == Status.IGNORED) {
						ignored++;
					}
				}
				status = instance.infoMessage.recommendationStatus(recStatus==null?Status.OPEN:recStatus.getStatus());
				
				Element trElement = Document.get().createTRElement();
				
				if (instance.getBareJID().contains(recomm.getPublisher()) && !instance.getRefMap().containsValue(recomm.getCustomId())) {
					final String user = person;
					HTML.wrap(trElement).addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							UpdateUserStatusPopup popup = instance.getUpdateUserStatusPopup();
							popup.setRef(id);
							popup.setUser(user);
							popup.showPopup();
						}
					});
				}
				
				Element userElement = Document.get().createTDElement();
				userElement.setInnerHTML(instance.getDisplayNameForJid(person));
				
				Element statusElement = Document.get().createTDElement();
				statusElement.setInnerHTML(status);
				
				Element updateElement = Document.get().createTDElement();
				updateElement.setInnerHTML(HasTimestamp.LONGDATE.format(HasTimestamp.TIMESTAMPFORMAT.parse(timestamp)));
	
				trElement.appendChild(userElement);
				trElement.appendChild(statusElement);
				trElement.appendChild(updateElement);
				
				progressList.appendChild(trElement);
			}
			progressStatisticsLabel.setInnerHTML(instance.infoMessage.progressStatisticsInfo(solved, ignored, targetPersons.size()));
		}
		
		Map<String, String> refMap = instance.getRefMap();
		if (!refMap.containsValue(id)) {
			discussButton.setEnabled(true);
			String currentId = recommId;
			versionListBox.clear();
			int version = 1;
			
			while (refMap.containsKey(currentId)) {
				version++;
				currentId = refMap.get(currentId);
				if (recommObjects.containsKey(currentId)) {
					RecommendationObject recomm = recommObjects.get(currentId);
					if (recomm != null) {
						versionListBox.addItem(recomm.getFormattedTimestamp(HasTimestamp.LONGDATE), recomm.getCustomId());
					}
				}
			}
			versionListBox.insertItem(instance.infoMessage.currentRevision(version), recommId, 0);
			versionListBox.setSelectedIndex(0);
		} else {
			discussButton.setEnabled(false);
		}
		
		changeStatusListBox.setSelectedIndex(recomm.getState()? 0:1);
	}
	
	/**
	 * Publishes the given dataobject on the given space.
	 * @param spaceId the id of the space to publish the dataobject on.
	 * @param dataObject the dataobject to publish.
	 */
	private native void publish(String spaceId, JavaScriptObject dataObject) /*-{
			$wnd.dataHandler.publishDataObject(dataObject, spaceId);
	}-*/;

	@Override
	protected void addRecommendation(final RecommendationPanel recomm) {
		RecommendationObject object = recommObjects.get(recomm.getCustomId());
		if (object == null || object.getParticipants() == null || !object.getParticipants().contains(instance.getBareJID())) {
			return;
		}
		super.addRecommendation(recomm);
		recommendationsList.appendChild(recomm.getPanel());
	}
	
	private void filterStatusAndTargetPerson() {
		for (String id:recomms.keySet()) {
			if (!recommIds.contains(id)) continue;
			boolean remove = false;
			RecommendationObject recomm = recommObjects.get(id);
			recomms.get(id).showView();
			if (filterTargetPerson || filterStatus) {
				if (filterTargetPerson) {
					int index = targetPersonListBox.getSelectedIndex();
					String itemText = targetPersonListBox.getItemText(index);
					if (targetPersons.get(itemText) != null && 
							!targetPersons.get(itemText).contains(recomm.getCustomId())) {
						remove = true;
					} else if (targetPersonListBox.getValue(index) != null && !targetPersonListBox.getValue(index).equals("")) {
						String spaceId = targetPersonListBox.getValue(index);
						if (recomm.getTargetSpaces() == null || !recomm.getTargetSpaces().contains(spaceId)) {
							remove = true;
						}
					}
				}
				if (!remove && filterStatus) {
					switch (statusListBox.getSelectedIndex()) {
					case 1:
						if (!recomm.getState()) {
							remove = true;
						}
						break;
					case 2:
						if (recomm.getState()) {
							remove = true;
						}
						break;
					default:
						remove = false;
						break;							
					}
				}
				if (remove) {
					recommIds.remove(id);
				}
			}
		}
	}	
	
	
	@Override
	protected void sort() {
		filterStatusAndTargetPerson();
		super.sort();
	}	
	
	@Override
	protected void updateFilters() {
		super.updateFilters();
		showTargetPersons(userJidToRecommIds, true);
	}
	
	@Override
	protected void initializeFilters() {
		super.initializeFilters();		
		targetPersons = new HashMap<String, List<String>>();
		showTargetPersons(userJidToRecommIds, false);
		
		if (statusListBox.getSelectedIndex() > 0) {
			filterStatus = true;
		} else {
			filterStatus = false;
		}
		
		if (targetPersonListBox.getSelectedIndex() > 0) {
			filterTargetPerson = true;
		} else {
			filterTargetPerson = false;
		}
	}
	
	/**
	 * Publishes a new RecommendationStatus on all available targetspaces the user, this status is for, is on.
	 * @param status the status to publish.
	 */
	public void publishStatus(RecommendationStatus status) {
		if (status == null) return;
		status.setPublisher(instance.getBareJID());
		RecommendationObject object = recommObjects.get(status.getRef());
		if (object == null) return;
		List<String> recommTargets = object.getTargetSpaces();
		if (recommTargets == null || recommTargets.size() == 0) {
			Window.alert(instance.errorMessage.noTargetSpacesInfoAvailable());
		} else {
			List<String> targets = new ArrayList<String>();
			Map<String, JavaScriptObject> spacesMap = instance.getCompleteSpacesMap();
			for (String id:recommTargets) {
				if (spacesMap.get(id) == null) continue;
				List<String> spaceMembers = getSpaceMembers(spacesMap.get(id));
				if (spaceMembers.contains(status.getUser())) {
					targets.add(id);
				}
			}
			publishStatus(status.toString(), targets);
		}
	}

	private void showTargetPersons(Map<String, List<String>> targetPersons, boolean update) {
		int currentIndex = targetPersonListBox.getSelectedIndex();
		String currentItemText = "";
		if (currentIndex >= 0) {
			currentItemText = targetPersonListBox.getItemText(currentIndex);
		}

		final Map<String, String> targetSpaces = new HashMap<String,String>();
		List<String> spaceIds = new ArrayList<String>();
		for (RecommendationObject recomm:recommObjects.values()) {
			if (recomm.getTargetSpaces() != null && recomms.containsKey(recomm.getCustomId())) {
				for (String spaceId:recomm.getTargetSpaces()) {
					if (targetSpaces.containsKey(spaceId)) {
						continue;
					} else {
						if (instance.getSpacesMap().containsKey(spaceId)) {
							targetSpaces.put(spaceId, instance.getSpacesMap().get(spaceId));
							spaceIds.add(spaceId);
						}
					}
				}
			}
		}
		

		Comparator<String> compare2 = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int ergebnis = targetSpaces.get(o1).compareToIgnoreCase(targetSpaces.get(o2));
				return ergebnis;
			}
		};
		
		Collections.sort(spaceIds, compare2);
		

		targetPersonListBox.clear();
		targetPersonListBox.addItem("All");		
		for (String spaceId:spaceIds) {
			targetPersonListBox.addItem(targetSpaces.get(spaceId), spaceId);
			if (currentIndex > 0 && targetSpaces.get(spaceId).equals(currentItemText)) {
				targetPersonListBox.setSelectedIndex(targetPersonListBox.getItemCount()-1);
			}
		}

		Element listBoxElement = targetPersonListBox.getElement();
		NodeList<Element> options = listBoxElement.getElementsByTagName("option");
		for (int i=0; i<options.getLength(); i++) {
			Element child = options.getItem(i);
			if (child.getAttribute("value") != null && !child.getAttribute("value").equals("") && !child.getAttribute("value").equalsIgnoreCase("All")) {
				child.setClassName("space");
			}
		}		

		if (targetPersons == null || targetPersons.size() == 0) {
			return;
		}
		List<String> persons = new ArrayList<String>();
		for (String jid:targetPersons.keySet()) {
			persons.add(jid);
		}		
		Comparator<String> compare = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int ergebnis = instance.getDisplayNameForJid(o1).compareToIgnoreCase(instance.getDisplayNameForJid(o2));
				return ergebnis;
			}
		};
		Collections.sort(persons, compare);
		for (String jid:persons) {
			String newJid = instance.getDisplayNameForJid(jid);
			this.targetPersons.put(newJid, targetPersons.get(jid));
			targetPersonListBox.addItem(newJid, "");
			if (currentIndex > 0 && newJid.equals(currentItemText)) {
				targetPersonListBox.setSelectedIndex(targetPersonListBox.getItemCount()-1);
			}
		}
		
		options = listBoxElement.getElementsByTagName("option");
		for (int i=0; i<options.getLength(); i++) {
			Element child = options.getItem(i);
			if ((child.getClassName() == null || child.getClassName().equals("")) && (child.getAttribute("value") == null || child.getAttribute("value").equals(""))) {
				child.setClassName("user");
			}
		}
	}
	
	public void update() {
		super.update();
	}
	
	public void reset() {
		super.reset();
		recommendationsList.removeAllChildren();
	}

}
