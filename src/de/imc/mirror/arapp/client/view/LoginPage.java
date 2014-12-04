package de.imc.mirror.arapp.client.view;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.xml.client.Element;

import de.imc.mirror.arapp.client.ARApp;
import de.imc.mirror.arapp.client.Experience;
import de.imc.mirror.arapp.client.Parser;
import de.imc.mirror.arapp.client.RecommendationObject;
import de.imc.mirror.arapp.client.RecommendationStatus;
import de.imc.mirror.arapp.client.SessionObject;
import de.imc.mirror.arapp.client.service.ARAppService;
import de.imc.mirror.arapp.client.service.ARAppServiceAsync;

public class LoginPage extends View{

	private Button loginButton;
	private TextBox nameField;
	private TextBox passField;
	
	private com.google.gwt.dom.client.Element errorMessage;
	
	private String username;
	private String password;
	
	private DialogBox dialog;
	
	private int amount;
	
	private boolean rememberMe;
	
	public LoginPage(final ARApp instance, String username, String password){
		super(instance);
		if (username != null && password != null) {
			rememberMe = false;
			Cookies.removeCookie("ARAppLogin");
			build();
			nameField.setText(username);
			passField.setText(password);
			loginToServer();
		} else {
			build();
		}
	}
	
	public LoginPage(final ARApp instance) {
		this(instance, null, null);
	}
	
	protected void build() {		
		nameField = TextBox.wrap(Document.get().getElementById("loginMaskUsernameInput"));
		nameField.setFocus(true);
		
		passField = PasswordTextBox.wrap(Document.get().getElementById("loginMaskPasswordInput"));
		
		errorMessage = Document.get().getElementById("loginMaskErrorMessage");
		
		final SimpleCheckBox rememberMe = SimpleCheckBox.wrap(Document.get().getElementById("loginMaskRemeberMeCheckbox"));
		
		loginButton = Button.wrap(Document.get().getElementById("loginButton"));
		loginButton.getElement().removeAttribute("disabled");
		loginButton.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				LoginPage.this.rememberMe = rememberMe.getValue();
				loginToServer();
			}
		});
		
		passField.addKeyUpHandler(new KeyUpHandler() {
			
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					LoginPage.this.rememberMe = rememberMe.getValue();
					loginToServer();
				}
			}
		});

		if (Cookies.getCookie("ARAppLogin") != null) {
			String encoded = Cookies.getCookie("ARAppLogin");
			Map<String, String> loginInfos = getLoginInfosDecoded(encoded);
			nameField.setText(loginInfos.get("username"));
			passField.setText(loginInfos.get("password"));
			loginToServer();
		}
	}
	
	/**
	 * Logs the user in if username and password are correct.
	 */
	private void loginToServer() {
		username = nameField.getText();
		password = passField.getText();
		if (username.replaceAll(" ", "").equals("") || password.replaceAll(" ", "").equals("")) {
			errorMessage.addClassName("activeItem");
			errorMessage.setInnerText(instance.errorMessage.loginNoCredentialsProvided());
			return;
		}
		passField.setText("");
		String resource = "recommApp";
		login(username, password, resource);
		
		loginButton.setEnabled(false);
		
		dialog = new DialogBox();
		dialog.setGlassEnabled(true);
		dialog.setGlassStyleName("transparent loader");
		dialog.show();
	}
	
	/**
	 * Called when the login was successful.
	 */
	private void loginSuccessful(){
		if (rememberMe) {
			setCookie();
		}
		errorMessage.removeClassName("activeItem");
		requestAndRegisterAllSpaces();
		requestAllOpenDiscussions(instance.getBareJID());
		changeConnectionListener(instance);
		addListener();
	}
	
	/**
	 * Called when there was an error while logging in.
	 */
	private void failedToLogin(){
		Cookies.removeCookie("ARAppLogin");
		dialog.hide();
		loginButton.getElement().removeAttribute("disabled");
		errorMessage.addClassName("activeItem");
		errorMessage.setInnerText(instance.errorMessage.loginAuthenticationFailed());
		instance.logout();
	}	
	
	/**
	 * If the "Remember me" box was checked, this method sets a cookie with the login credentials.
	 */
	private void setCookie() {
		String encoded = getBase64Encoded(this.username, password);
		Cookies.setCookie("ARAppLogin", encoded);
	}
	
	/**
	 * If a cookie with the encoded login credentials was set this method will decode the string and return the login credentials in a map with keys "username" and "password".
	 * @param encoded the encoded login credentials.
	 * @return a map containing the decoded login credentials.
	 */
	private native Map<String, String> getLoginInfosDecoded(String encoded) /*-{
		var map = @java.util.HashMap::new()();
		var decoded = $wnd.Base64.decode(encoded);
		var object = $wnd.JSON.parse(decoded);
		map.@java.util.HashMap::put(Ljava/lang/Object;Ljava/lang/Object;)("username", object.username);
		map.@java.util.HashMap::put(Ljava/lang/Object;Ljava/lang/Object;)("password", object.password);
		return map;
	}-*/;
	
	/**
	 * Encodes the username and password with a Base64 encryption which will be set as a cookie if the "Remember me" checkbox is checked.
	 * @param username the entered username.
	 * @param password the entered password.
	 * @return the encoded username and password as a concatenated string.
	 */
	private native String getBase64Encoded(String username, String password) /*-{
		var loginInfo = new Object();
		loginInfo.username = username;
		loginInfo.password = password;
		var cookieText = $wnd.JSON.stringify(loginInfo);
		return $wnd.Base64.encode(cookieText);
	}-*/;
	
	/**
	 * Parses all received Recommendations and caches them. If this happened for all spaces the startview is shown.
	 * @param docXML The recommendations as xml.
	 */	
	private void parseDataObjects(String namespace, String xml) {
		Element elem = Parser.parseXMLStringToElement(xml);
		if (elem != null) {
			if (namespace.equals("mirror:application:activityrecommendationapp:recommendation")) {
				List<RecommendationObject> recomms = Parser.parseRecommendations(elem);
				instance.addRecommendation(recomms.get(0).getCustomId(), recomms.get(0));
			} else if (namespace.equals("mirror:application:activityrecommendationapp:experience")) {
				List<Experience> experiences = Parser.parseExperiences(elem);
				instance.addExperience(experiences.get(0).getRecommendationId(), experiences.get(0));
			} else if (namespace.equals("mirror:application:activityrecommendationapp:recommendationstatus")) {
				List<RecommendationStatus> stati = Parser.parseRecommendationStati(elem);
				instance.addRecommendationStatus(stati.get(0));
			}
		}
	}
	
	/**
	 * Shows the logoutbutton and shows the next view depending of if the user is logged in to the front or backend.
	 */
	private void showStartView() {
		instance.loggedIn();
		dialog.hide();
		loginButton.setEnabled(true);
	}

	/**
	 * Increments a variable by one. This variable is used to check if all spaces were requested for their information.
	 */
	private void countUp() {
		amount++;
	}


	/**
	 * Decrements a variable by one. This variable is used to check if all spaces were requested for their information.
	 */
	private void countDown() {
		amount--;
		if (amount == 0) {
			showStartView();
		}
	}
	
	/**
	 * Convenience method to call <code>addSpace</code> of ARApp.class
	 * @param id The id of the space.
	 * @param name The name of the space.
	 */
	private void addSpace(String id, String name, JavaScriptObject space) {
		instance.addSpace(id, name, space);
	}
	
	/**
	 * Parses an newly received recommendation object and caches it.
	 * @param spaceId The id of the space this recommendation was received on.
	 * @param xml The received data as xml which contains the new recommendation.
	 */
	private void addNewRecommendationObject(String spaceId, String xml) {
		Element elem = Parser.parseXMLStringToElement(xml);
		if (elem != null) {
			List<RecommendationObject> recomms = Parser.parseRecommendations(elem);
			instance.addRecommendation(recomms.get(0).getCustomId(), recomms.get(0));
		}
	}
	
	/**
	 * Parses an newly received experience object and caches it.
	 * @param xml The received data as xml which contains the new experience.
	 */
	private void addNewExperienceObject(String xml) {
		Element elem = Parser.parseXMLStringToElement(xml);
		if (elem != null) {
			List<Experience> experience = Parser.parseExperiences(elem);
			if (experience != null && experience.size() > 0) {
				Experience exp = experience.get(0);
				instance.addExperience(exp.getRecommendationId(), exp);
			}
		}
	}
	
	/**
	 * Parses an newly received recommendationstatus object and caches it.
	 * @param xml The received data as xml which contains the new recommendationstatus.
	 */
	private void addNewRecommendationStatusObject(String xml) {
		Element elem = Parser.parseXMLStringToElement(xml);
		if (elem != null) {
			List<RecommendationStatus> stati = Parser.parseRecommendationStati(elem);
			if (stati != null && stati.size() > 0) {
				RecommendationStatus status = stati.get(0);
				instance.addRecommendationStatus(status);
			}
		}
	}
	
	
	private void addDiscussions(Map<String, String> infos) {
		for (String node:infos.keySet()) {
			Element elem = SessionObject.getLatestCompleteSessionObject(infos.get(node));
			if (elem != null) {
				instance.addSessionObject(node, elem.toString());
				subscribeToNode(node);
			}
		}
	}
	
	private void addVote(String spaceId, String xml) {
		instance.addVote(spaceId, xml);
	}
	
	private void addNewSessionObject(String spaceId, String xml) {
		instance.addSessionObject(spaceId, xml);
	}
	
	private void showPublishWizard(String spaceId) {
		instance.showPublishWizard(spaceId);
	}
	
	private void removeNode(String nodeId) {
		instance.removeNode(nodeId);
	}

	/**
	 * Saves a given displayname for a given jid. If these two are both the barejid only the userId will be saved as the displayname. 
	 * @param jid the jid of the person.
	 * @param displayName the displayname to save.
	 */
	private void saveDisplayNameForJid(String jid, String displayName) {
		if (jid.equals(displayName)) {
			instance.setDisplayNameForJid(jid, displayName.split("@")[0]);
		} else {
			instance.setDisplayNameForJid(jid, displayName);
		}
	}
	
	/**
	 * After login the connection listener is changed so that a lost connection will be displayed.
	 * @param instance Instance of ARApp.class
	 */
	private native void changeConnectionListener(ARApp instance) /*-{
		$wnd.connection.removeConnectionStatusListener($wnd.loginListener);
		$wnd.connection.addConnectionStatusListener(($wnd.statusListener = new $wnd.SpacesSDK.ConnectionStatusListener("statusListener", function(status){
			if (status == $wnd.SpacesSDK.ConnectionStatus.OFFLINE || status == $wnd.SpacesSDK.ConnectionStatus.ERROR){
				alert(instance.@de.imc.mirror.arapp.client.ARApp::errorMessage.@de.imc.mirror.arapp.client.localization.ErrorMessage::connectionClosed()());
				instance.@de.imc.mirror.arapp.client.ARApp::logout()();
			}
		})));
	}-*/;
	
	/**
	 * Requests all available Spaces, registers them in the datahandler and requests all Recommendations on them.
	 */
	private native void requestAndRegisterAllSpaces() /*-{
		var that = this;
		if (!$wnd.spaceHandler) {
				$wnd.spaceHandler = new $wnd.SpacesSDK.SpaceHandler($wnd.connection);
		}
		if (!$wnd.dataHandler) {
				$wnd.dataHandler = new $wnd.SpacesSDK.DataHandler($wnd.connection, $wnd.spaceHandler);
		}
		
		var listener = new $wnd.SpacesSDK.DataObjectListener("recommListener", function(dataObject, spaceId) {
				if (@de.imc.mirror.arapp.client.Parser::isRecommendationObject(Ljava/lang/String;)(dataObject.toString())) {
					that.@de.imc.mirror.arapp.client.view.LoginPage::addNewRecommendationObject(Ljava/lang/String;Ljava/lang/String;)(spaceId, dataObject.toString());
				} else if (@de.imc.mirror.arapp.client.Parser::isExperienceObject(Ljava/lang/String;)(dataObject.toString())) {
					that.@de.imc.mirror.arapp.client.view.LoginPage::addNewExperienceObject(Ljava/lang/String;)(dataObject.toString());
				} else if (@de.imc.mirror.arapp.client.Parser::isRecommendationStatusObject(Ljava/lang/String;)(dataObject.toString())) {
					that.@de.imc.mirror.arapp.client.view.LoginPage::addNewRecommendationStatusObject(Ljava/lang/String;)(dataObject.toString());
				}
		});
		
		$wnd.dataHandler.addDataObjectListener(listener);
		$wnd.spaceHandler.getAllSpaces(function(result){				
				var persistenceAvailable = $wnd.connection.getNetworkInformation().getPersistenceServiceJID();
				var spaceIds = [];
				var jids = @java.util.HashSet::new()();
				
				if (result.length > 0) {
					that.@de.imc.mirror.arapp.client.view.LoginPage::countUp()();
					
					for (var i = 0; i<result.length; i++) {
						spaceIds.push(result[i].getId());
						$wnd.dataHandler.registerSpace(result[i].getId());
						
						var users = result[i].getMembers();
						for (var j in users) {
							jids.@java.util.Set::add(Ljava/lang/Object;)(users[j].getJID());
						}					
						
						that.@de.imc.mirror.arapp.client.view.LoginPage::addSpace(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(result[i].getId(), result[i].getName(), result[i]);
					}
					var modelFilter1 = new $wnd.SpacesSDK.filter.DataModelFilter('mirror:application:activityrecommendationapp:recommendation');
					var modelFilter2 = new $wnd.SpacesSDK.filter.DataModelFilter('mirror:application:activityrecommendationapp:experience');
					var modelFilter3 = new $wnd.SpacesSDK.filter.DataModelFilter('mirror:application:activityrecommendationapp:recommendationstatus');
					var orFilter = new $wnd.SpacesSDK.filter.OrFilter();
					
					orFilter.addFilter(modelFilter1);
					orFilter.addFilter(modelFilter2);
					orFilter.addFilter(modelFilter3);
					
					$wnd.dataHandler.setDataObjectFilter(orFilter);
						
					$wnd.dataHandler.queryDataObjectsBySpaces(spaceIds, [], function(result) {
						for (var i=0; i<result.length; i++) {
							that.@de.imc.mirror.arapp.client.view.LoginPage::parseDataObjects(Ljava/lang/String;Ljava/lang/String;)(result[i].getNamespaceURI(), result[i].toString());
						}
						$wnd.dataHandler.setDataObjectFilter(null);
						that.@de.imc.mirror.arapp.client.view.LoginPage::countDown()();	
					}, function(error) {
					
					});
				} else {
					jids.@java.util.Set::add(Ljava/lang/Object;)($wnd.connection.getCurrentUser().getBareJID());
				}
				that.@de.imc.mirror.arapp.client.view.LoginPage::getFullNames(Ljava/util/Set;)(jids);
				
			},
			function(err){
				$wnd.alert("moep");
			}
		);
	}-*/;

	/**
	 * This method will request and save all fullnames for all members of the given space.
	 * @param space the space to get the fullnames for its members.
	 */
	private native void getFullNames(Set<String> jids) /*-{
		var that = this;
		var xmppConnection = $wnd.connection.getXMPPConnection();
		var iter = jids.@java.util.Set::iterator()();
		while (iter.@java.util.Iterator::hasNext()()) {
			that.@de.imc.mirror.arapp.client.view.LoginPage::countUp()();
			var jid = iter.@java.util.Iterator::next()();
			that.@de.imc.mirror.arapp.client.view.LoginPage::saveDisplayNameForJid(Ljava/lang/String;Ljava/lang/String;)(jid, jid);
			xmppConnection.vcard.get(function(result) {
				var nameElement = result.getElementsByTagName("FN");
				if (nameElement && nameElement != null && nameElement[0] && nameElement[0] != null) {
					var requestedJid = result.getAttribute("from");
					var name = requestedJid;
					if (nameElement[0].firstChild && nameElement[0].firstChild.nodeValue) name = nameElement[0].firstChild.nodeValue;
					else if (nameElement[0].textContent) name = nameElement[0].textContent;
					else if (nameElement[0].innerText) name = nameElement[0].innerText;
					that.@de.imc.mirror.arapp.client.view.LoginPage::saveDisplayNameForJid(Ljava/lang/String;Ljava/lang/String;)(requestedJid, name);
				}
				that.@de.imc.mirror.arapp.client.view.LoginPage::countDown()();
			}, jid, function(error) {
				that.@de.imc.mirror.arapp.client.view.LoginPage::countDown()();
			});
		}
	}-*/;
	
	private void removeTime(String discussion) {
		final ARAppServiceAsync service = GWT.create(ARAppService.class);
		
		
		service.endDiscussion(discussion, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(Void arg0) {
			}
		});
	}
	
	/**
	 * Sends requests to all pubsub nodes a user has access to, to get the possibly saved discussions.
	 * @param bareJid the barejid of the user.
	 */
	public native void requestAllOpenDiscussions(String bareJid) /*-{	
		var that = this;
		var amount = 0;
		var conn = $wnd.connection.getXMPPConnection();
		var user = $wnd.connection.getCurrentUser().getBareJID();
		conn.pubsub.discoverNodes(function(result) {
			var items = result.getElementsByTagName("item");
			var spaces = new Object();
			if (items && items != null) {
				for (var i=0; i<items.length; i++) {
					if (items[i].getAttribute("node") === "spacepubs") continue;
					amount++;
					spaces[i] = items[i].getAttribute("node");
				}
			}
			conn.pubsub.getSubscriptions(function(res) {
				var subs = res.getElementsByTagName("subscription");
				if (subs && subs != null) {
					for (var i=0; i<subs.length; i++) {
						var node = subs[i].getAttribute("node");
						for (var j in spaces) {
							 if (spaces[j] === node) {
								conn.pubsub.unsubscribe(node, user, subs[i].getAttribute("subid"));						 	
							 } 
						}
					}
				}
				lookIfOpenDiscussion(spaces);
			});
		}, function() {
		});
		

		var lookIfOpenDiscussion = function(spaces) {
			var map = @java.util.HashMap::new()();	
			
			if (amount > 0) {
				for (var i in spaces) {
					conn.pubsub.items(spaces[i], 
						function(result) {
							var items = result.getElementsByTagName("items");
							var node = items[0].getAttribute("node");
							var xml = $wnd.Strophe.serialize(result);
							if (@de.imc.mirror.arapp.client.Parser::hasSessionObject(Ljava/lang/String;)(xml)) {
								if (@de.imc.mirror.arapp.client.SessionObject::isModerator(Ljava/lang/String;Ljava/lang/String;)(xml, bareJid) && 
										@de.imc.mirror.arapp.client.SessionObject::isObsolete(Ljava/lang/String;)(xml)) {
												conn.pubsub.deleteNode(node, function() {
													that.@de.imc.mirror.arapp.client.view.LoginPage::removeTime(Ljava/lang/String;)(node);
												});	
								}
								else {
									map.@java.util.HashMap::put(Ljava/lang/Object;Ljava/lang/Object;)(node, xml);
								}	
							} else {
								conn.pubsub.deleteNode(node, function() {
									that.@de.imc.mirror.arapp.client.view.LoginPage::removeTime(Ljava/lang/String;)(node);
								});	
							}
							amount--;
							if (amount == 0) {
								that.@de.imc.mirror.arapp.client.view.LoginPage::addDiscussions(Ljava/util/Map;)(map);
							}
						}, 
						function(error){
							amount--;
							if (amount == 0) {
								that.@de.imc.mirror.arapp.client.view.LoginPage::addDiscussions(Ljava/util/Map;)(map);
							}
						});
				}
			} else {
				that.@de.imc.mirror.arapp.client.view.LoginPage::addDiscussions(Ljava/util/Map;)(map);
			}				
		}
	}-*/;
	
	/**
	 * Subscribes the user to a pubsub node.
	 * @param discussionNode the node to subscribe the user to.
	 */
	private native void subscribeToNode(String discussionNode) /*-{			
		$wnd.connection.getXMPPConnection().pubsub.subscribe(discussionNode, null, function(result){return false;}, null, null, true);
	}-*/;

	/**
	 * Adds a handler, which parses the incoming message and  calls the corresponding function.
	 */
	private native void addListener() /*-{
		var that = this;		
		var subscriptionCallback = function(message){
			var object = message.getElementsByTagName("session");
			var xml = null;
			if (object && object != null && object.length > 0) {
				
				var nodeId = message.getElementsByTagName("items")[0];
				if (nodeId && nodeId != null) {
					var node = nodeId.attributes.getNamedItem('node');
					if (node && node.value) nodeId = node.value;
					else if (node && node.nodeValue) nodeId = node.nodeValue;
				}
				if (!nodeId || nodeId == null) {
							return true;
						}
				
				object = object[0];
				xml = $wnd.Strophe.serialize(object);
				if (xml != null) {
					var type = @de.imc.mirror.arapp.client.SessionObject::getType(Ljava/lang/String;)(xml);
					if (type === "complete") {
						that.@de.imc.mirror.arapp.client.view.LoginPage::addNewSessionObject(Ljava/lang/String;Ljava/lang/String;)(nodeId, xml);
					} else if (type === "publish") {
						that.@de.imc.mirror.arapp.client.view.LoginPage::showPublishWizard(Ljava/lang/String;)(nodeId);
					} else if (type === "vote") {
						that.@de.imc.mirror.arapp.client.view.LoginPage::addVote(Ljava/lang/String;Ljava/lang/String;)(nodeId, xml);
					}
				}
			} else {
				object = message.getElementsByTagName("delete");
				if (object && object != null && object.length > 0) {
					for (var i=0; i<object.length; i++) {
						var node = object[i].attributes.getNamedItem("node");
						if (node && node.value) node = node.value;
						else if (node && node.nodeValue) node = node.nodeValue;
						if (!node || node == null) {
							return true;
						}
						that.@de.imc.mirror.arapp.client.view.LoginPage::removeNode(Ljava/lang/String;)(node);
					}
				} else {
					object = message.getElementsByTagName("discussionnode");
					if (object && object != null && object.length > 0) {
						for (var i=0; i<object.length; i++) {
							var node = object[i].attributes.getNamedItem("node");
							if (node && node.value) node = node.value;
							else if (node && node.nodeValue) node = node.nodeValue;
							if (!node || node == null) {
								return true;
							}
							that.@de.imc.mirror.arapp.client.view.LoginPage::subscribeToNode(Ljava/lang/String;)(node);
							$wnd.connection.getXMPPConnection().pubsub.items(node,
						function(result) {
							var xml = $wnd.Strophe.serialize(result);
							if (@de.imc.mirror.arapp.client.Parser::hasSessionObject(Ljava/lang/String;)(xml)) {
								var items = result.getElementsByTagName("session");
								xml = $wnd.Strophe.serialize(items[0]);
								that.@de.imc.mirror.arapp.client.view.LoginPage::addNewSessionObject(Ljava/lang/String;Ljava/lang/String;)(node, xml);
							}
						});
						}
					}
				}
			}
			return true;
		}		
		
        $wnd.connection.getXMPPConnection().addHandler(subscriptionCallback, null, 'message', null, null, null);
	}-*/;
	
	/**
	 * Tries to log the user in with the given data.
	 * @param name The given username.
	 * @param pass The given password.
	 */
	private native void login(String name, String pass, String resource) /*-{
		var that = this;
		var config = new $wnd.SpacesSDK.ConnectionConfigurationBuilder($wnd.domain, resource);
		config.setTimeout(5000);

		config.setSecureConnection(true);
		config.setHost($wnd.domain);
		$wnd.name = name;
		$wnd.pass = pass;
		
		$wnd.connection = new $wnd.SpacesSDK.ConnectionHandler(name, pass, config.build(), $wnd.httpbind);
		
		$wnd.connection.addConnectionStatusListener(($wnd.loginListener = new $wnd.SpacesSDK.ConnectionStatusListener("loginListener", function(status){
			if (status == $wnd.SpacesSDK.ConnectionStatus.ONLINE){
				that.@de.imc.mirror.arapp.client.view.LoginPage::loginSuccessful()();
			} else if (status == $wnd.SpacesSDK.ConnectionStatus.OFFLINE){
				that.@de.imc.mirror.arapp.client.view.LoginPage::failedToLogin()();
			} else if (status == $wnd.SpacesSDK.ConnectionStatus.ERROR){
				that.@de.imc.mirror.arapp.client.view.LoginPage::failedToLogin()();
			}
		})));
		$wnd.connection.connect();
	}-*/;
}
