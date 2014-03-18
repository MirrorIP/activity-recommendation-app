package de.imc.mirror.arapp.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;

public class ListPanel {

	protected Element li;
	
	private Element titleSpan;
	
	private ClickHandler handler;
	

	/**
	 * Creates a new RecommendationPanel. This can be added to a list
	 * @param recomm
	 */
	protected ListPanel() {
		li = Document.get().createLIElement();
	}
	
	protected void addClickHandler(ClickHandler handler) {
		if (handler != null) {
			this.handler = handler;
			setClickHandler(li);
		}
	}
	
	protected void build(String title) {
		titleSpan = Document.get().createSpanElement();
		titleSpan.setClassName("title");
				
		titleSpan.setInnerHTML(title);
		
		Element arrowSpan = Document.get().createSpanElement();
		arrowSpan.setClassName("arrow");
		arrowSpan.setInnerHTML("&nbsp;");

		li.appendChild(titleSpan);
		li.appendChild(arrowSpan);		
	}

	/**
	 * Adds the class "activeItem" to the RecommendationPanel.
	 */
	public void setActive() {
		li.addClassName("activeItem");
	}

	/**
	 * Removes the class "activeItem" from the RecommendationPanel.
	 */
	public void setInactive() {
		li.removeClassName("activeItem");
	}

	/**
	 * Returns the actual HTML Element of the panel.
	 * @return the HTML Element.
	 */
	public Element getPanel() {
		return li;
	}
	
	public void removePanel() {
		li.removeFromParent();
	}
	
	/**
	 * Adds "display:none" to the style of the element.
	 */
	public void hideView() {
		li.setAttribute("style", "display:none");
	}

	/**
	 * Removes the style attribute from the element.
	 */
	public void showView() {
		li.removeAttribute("style");		
	}
	
	public void updateTitle(String title) {
		titleSpan.setInnerText(title);
	}
	
	private native void setClickHandler(Element li) /*-{
		var that = this;
		li.onclick = function(event) {
			that.@de.imc.mirror.arapp.client.ListPanel::handler.@com.google.gwt.event.dom.client.ClickHandler::onClick(Lcom/google/gwt/event/dom/client/ClickEvent;)(event);
		}
	}-*/;
}
