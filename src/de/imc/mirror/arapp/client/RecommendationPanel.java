package de.imc.mirror.arapp.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import de.imc.mirror.arapp.client.Interfaces.HasDetailPanel;

public class RecommendationPanel extends ListPanel {
	
	private String customId;
	
	private HasDetailPanel instance;

	private static Map<HasDetailPanel, ListPanel> activeElements = new HashMap<HasDetailPanel, ListPanel>();
	
	private ClickHandler handler = new ClickHandler() {

		@Override
		public void onClick(ClickEvent event) {
			if (activeElements.containsKey(instance)) {
				activeElements.get(instance).setInactive();
			}
			setActive();
			instance.showDetails(customId);
			activeElements.put(instance, RecommendationPanel.this);
		}

	};
	
	/**
	 * Creates a new RecommendationPanel. This can be added to a list
	 * @param recomm
	 */
	public RecommendationPanel(RecommendationObject recomm, HasDetailPanel instance) {
		super();
		addClickHandler(handler);
		this.customId = recomm.getCustomId();
		this.instance = instance;
		build(recomm);
	}
	
	private void build(RecommendationObject recomm) {		
		String taskType = "";
		if (recomm.getTaskType() != null) {
			switch(recomm.getTaskType()) {
				case ACTIVITY:
					taskType = "activityItem";
					break;
				case BEHAVIOR:
					taskType = "behaviorItem";
					break;
				case LEARNING:
					taskType = "learningItem";
					break;
				
			}
			li.setClassName(taskType);
		}
		
		Element iconSpan = Document.get().createSpanElement();
		iconSpan.setClassName("icon");
		iconSpan.setInnerHTML("&nbsp;");

		li.appendChild(iconSpan);
		
		super.build(recomm.getTitle());
	}
	
	/**
	 * Returns the custom id of the Recommendation the Panel is for.
	 * @return the custom id.
	 */
	public String getCustomId() {
		return customId;
	}
	
}
