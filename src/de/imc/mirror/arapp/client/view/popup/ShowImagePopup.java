package de.imc.mirror.arapp.client.view.popup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Image;

import de.imc.mirror.arapp.client.FileEvidence;

public class ShowImagePopup {

	private DialogBox dia;
	private Image loader;
	private Image img;
	private AbsolutePanel absolute;
	private FileEvidence fe;
	private Element errorElem;
	
	public ShowImagePopup(final FileEvidence fe) {
		this.fe = fe;
		dia = new DialogBox();
		dia.setGlassEnabled(true);
		dia.setGlassStyleName("transparent");
		
		absolute = new AbsolutePanel();
		absolute.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		
		loader = new Image("./img/loader.gif");
		loader.setVisible(true);
		loader.setWidth("42px");
		loader.setHeight("42px");		

		img = new Image();
		img.getElement().getStyle().setPosition(Position.ABSOLUTE);
		img.getElement().getStyle().setVisibility(Visibility.HIDDEN);
		img.setWidth("auto");
		img.setHeight("auto");
		img.getElement().getStyle().setProperty("maxWidth", "900px");
		img.getElement().getStyle().setProperty("maxHeight", "620px");
		
		addOnLoadHandler(img.getElement());
		absolute.add(loader);
		absolute.add(img);
		dia.setAutoHideEnabled(true);
		dia.add(absolute);
	}
	
	private void showImage() {
//		loader.setVisible(false);
		final int w = img.getWidth();
		final int h = img.getHeight();
		img.getElement().getStyle().setLeft(-w/2, Unit.PX);
		img.getElement().getStyle().setTop(-h/2, Unit.PX);
		img.getElement().getStyle().clearVisibility();
	}
	
	private void requestImage() {
		StringBuilder builder = new StringBuilder();
		builder.append(GWT.getModuleBaseURL())
				.append("server?")
				.append("nameOnServer=")
				.append(URL.encodeQueryString(fe.getFileNameWithPrefix()))
				.append("&filename=")
				.append(URL.encodeQueryString(fe.getFileName()))
				.append("&location=")
				.append(URL.encodeQueryString(fe.getLocation()))
				.append("&auth=")
				.append(URL.encodeQueryString(getLoginInfos()))
				.append("&download=false");
		RequestBuilder reqBuilder = new RequestBuilder(RequestBuilder.GET, builder.toString());
		try {
			loader.setVisible(true);
			reqBuilder.sendRequest(null, new RequestCallback() {
				
				@Override
				public void onResponseReceived(Request request, Response response) {
					String result = response.getText();
					result = result.replaceAll("\\$", "+");
					result = result.replaceAll("_", "/");
					if (result.replaceAll(" ", "").isEmpty() || response.getStatusCode() != 200) {
						showError();
					} else {
						String[] array = fe.getFileName().split("\\.");
						String fileType = array[array.length - 1].toLowerCase();
						img.setUrl("data:image/" + fileType + ";base64," + result);
					}
					loader.setVisible(false);
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					showError();
					
				}
			});
		} catch (RequestException e) {
			showError();
		}	
	}
	
	private void showError() {
		if (errorElem == null) {
			errorElem = Document.get().createDivElement();
			errorElem.setInnerText("The image couldn't be loaded. Please try again.");
			errorElem.getStyle().setBackgroundColor("#FFFFFF");
			errorElem.getStyle().setColor("#FF0000");
			errorElem.getStyle().setPadding(10, Unit.PX);
		}
		
		absolute.getElement().appendChild(errorElem);
		dia.center();
	}
		
	public void showPopup() {
		dia.center();
		if (errorElem != null) {
			absolute.getElement().removeChild(errorElem);
		}
		if (img.getUrl().isEmpty()) {
			requestImage();
		}
	}
	
	private native void addOnLoadHandler(Element imageElement) /*-{
		var that = this;
		imageElement.onload = function() {
			that.@de.imc.mirror.arapp.client.view.popup.ShowImagePopup::showImage()();
		} 
	}-*/;
 
	private static native String getLoginInfos() /*-{
		var user = $wnd.connection.getCurrentUser().getBareJID();
		var pass = $wnd.pass;
		var loginInfos = user + ":" + pass;
		return $wnd.Base64.encode(loginInfos);
	}-*/;
}
