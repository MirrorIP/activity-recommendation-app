package de.imc.mirror.arapp.client;

import com.google.gwt.core.client.JavaScriptObject;

import de.imc.mirror.arapp.client.Interfaces.HasChat;

public class Muc {
	
	private static final String MUCSERVICE = "spacemucs." + getDomain();
	
	private String mucId;
	private String nick;
	private HasChat callingView;
	
	public Muc(String mucId, String nick, HasChat callingView) {
		this.mucId = mucId;
		this.nick = nick;
		this.callingView = callingView;
		createOrJoinChat();
	}
	
	public void leave() {
		leaveMuc();
		this.mucId = null;
		this.nick = null;
		this.callingView = null;
	}
	
	private native void leaveMuc() /*-{
		var that = this;
		var conn = $wnd.connection.getXMPPConnection();
		var id = that.@de.imc.mirror.arapp.client.Muc::mucId;
		var service = @de.imc.mirror.arapp.client.Muc::MUCSERVICE;
		var nick = that.@de.imc.mirror.arapp.client.Muc::nick;
		conn.muc.leave(id + "@" + service, nick);
	}-*/;
	
	private native void createOrJoinChat() /*-{
		var that = this;
		var conn = $wnd.connection.getXMPPConnection();
		conn.muc.listRooms(@de.imc.mirror.arapp.client.Muc::MUCSERVICE, function(result){
			var items = result.getElementsByTagName("item");
			if (items.length == 0) {
				that.@de.imc.mirror.arapp.client.Muc::createMuc()();			
			} else {
				var mucId = that.@de.imc.mirror.arapp.client.Muc::mucId;
				for (var i=0; i<items.length; i++) {
					if (items[i].attributes.getNamedItem("name").nodeValue == mucId) {
						that.@de.imc.mirror.arapp.client.Muc::joinMuc()();
						return;
					}
				}
				that.@de.imc.mirror.arapp.client.Muc::createMuc()();
			}
		});
	}-*/;

	private native void createMuc() /*-{
		var that = this;
		var id = that.@de.imc.mirror.arapp.client.Muc::mucId;
		var service = @de.imc.mirror.arapp.client.Muc::MUCSERVICE;
		var nick = that.@de.imc.mirror.arapp.client.Muc::nick;
		var conn = $wnd.connection.getXMPPConnection();
		conn.muc.join(id + "@" + service, nick, function(msg) {			
			return that.@de.imc.mirror.arapp.client.Muc::messageHandler(Lcom/google/gwt/core/client/JavaScriptObject;Lde/imc/mirror/arapp/client/Muc;)(msg, that);
		},
		 	function(){
				conn.muc.saveConfiguration(id + "@" + service, {});
				return false;
			});
	}-*/;
	
	private native void joinMuc() /*-{
		var that = this;
		var id = that.@de.imc.mirror.arapp.client.Muc::mucId;
		var service = @de.imc.mirror.arapp.client.Muc::MUCSERVICE;
		var nick = that.@de.imc.mirror.arapp.client.Muc::nick;
		var conn = $wnd.connection.getXMPPConnection();
		conn.muc.join(id + "@" + service, nick, function(msg) {			
			return that.@de.imc.mirror.arapp.client.Muc::messageHandler(Lcom/google/gwt/core/client/JavaScriptObject;Lde/imc/mirror/arapp/client/Muc;)(msg, that);
		});
	}-*/;
	
	private native void messageHandler(JavaScriptObject msg, Muc instance) /*-{
		var sender = msg.attributes.getNamedItem("from").nodeValue;
		var service = @de.imc.mirror.arapp.client.Muc::MUCSERVICE;
		if (sender.lastIndexOf(service) == sender.length - service.length) {
			return true;
		}
		sender = sender.split("/")[sender.split("/").length - 1];
		var callingView = instance.@de.imc.mirror.arapp.client.Muc::callingView;
		var id = instance.@de.imc.mirror.arapp.client.Muc::mucId;
		if (callingView == null) {
			return false;
		}
		if (id == msg.attributes.getNamedItem("from").nodeValue.split("@")[0]) {
			callingView.@de.imc.mirror.arapp.client.Interfaces.HasChat::appendNewMessage(Ljava/lang/String;Ljava/lang/String;)(msg.textContent, sender);
		}
		return true;
	}-*/;
	
	public native void sendChatMessage(String message) /*-{
		var id = this.@de.imc.mirror.arapp.client.Muc::mucId;
		var service = @de.imc.mirror.arapp.client.Muc::MUCSERVICE;
		var nick = this.@de.imc.mirror.arapp.client.Muc::nick;
		$wnd.connection.getXMPPConnection().muc.message(id + "@" + service, null, message);
	}-*/;
	
	private static native String getDomain() /*-{
		return $wnd.domain;
	}-*/;

}
