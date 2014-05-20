package de.imc.mirror.arapp.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import de.imc.mirror.arapp.client.Interfaces.HasDetailPanel;

public class DiscussionPanel extends ListPanel {
	
	private String discussionId;
	
	private HasDetailPanel instance;
	
	private static Map<HasDetailPanel, ListPanel> activeElements = new HashMap<HasDetailPanel, ListPanel>();

	private ClickHandler handler = new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			if (activeElements.containsKey(instance)) {
				activeElements.get(instance).setInactive();
			}
			setActive();
			instance.showDetails(discussionId);
			activeElements.put(instance, DiscussionPanel.this);
		}
	};
	
	/**
	 * Creates a new RecommendationPanel. This can be added to a list
	 * @param recomm
	 */
	public DiscussionPanel(String title, String discussionId, HasDetailPanel instance) {
		super();
		this.discussionId = discussionId;
		this.instance = instance;
		addClickHandler(handler);
		build(title);
	}
	
	public String getDiscussionId() {
		return discussionId;
	}
	
	public void setArrowActiveBackground() {
		Element elem = (Element) li.getLastChild();
		elem.getStyle().setBackgroundImage("url(\"./img/arrow_blue.png\")");
	}
	
	public void unsetArrowActiveBackground() {
		Element elem = (Element) li.getLastChild();
		elem.getStyle().clearBackgroundImage();
	}
}
