package de.imc.mirror.arapp.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Effort {
	
	private static final Effort TIMESPENTMIN = new Effort("timeSpentMin", "Time Spent (min.)", "The minutes you spent to implement the recommended solution (per application).");
	private static final Effort TIMESPENTHRS = new Effort("timeSpentHrs", "Time Spent (hrs.)", "The hours you spent to implement the recommended solution (per application).");
	private static final Effort ADDITIONALCOSTS = new Effort("additionalCosts", "Additional Costs (EUR)", "The amount of money you invested to implement the recommended solution.");
	private static final Effort DISTANCEDRIVEN = new Effort("distanceDriven", "Distance Driven (km)", "The distance driven to apply the recommended solution");
	private static final Effort CUSTOM = new Effort("custom", "Custom", "");
	
	private static final Effort[] efforts = {TIMESPENTMIN, TIMESPENTHRS, ADDITIONALCOSTS, DISTANCEDRIVEN, CUSTOM};

	private String id;
	private String display;
	private String description;
	private int value;
	
	private Effort(String id, String display, String description) {
		this.id = id;
		this.display = display;
		this.description = description;
		this.value = -1;
	}

	/**
	 * Gets the id of the effort.
	 * @return the id of the effort.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the String to display, e.g. the unit.
	 * @return the string to display.
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * Sets the string to display. E.g. the unit.
	 * @param display the string to display.
	 */
	public void setDisplay(String display) {
		this.display = display;
	}

	/**
	 * Gets the description for the effort.
	 * @return the set description for the effort. 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description for the effort.
	 * @param description the description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the value for this effort.
	 * @param value the value to set.
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Gets the set value.
	 * @return the set value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @return a list of all available efforts.
	 */
	public static List<Effort> values() {
		List<Effort> effortList = new ArrayList<Effort>();
		for (Effort effort: efforts) {
			effortList.add(effort);
		}
		return Collections.unmodifiableList(effortList);		
	}

	/**
	 * Method to get the effort with the id matching the given one.
	 * @param id the id of the effort to get
	 * @return the effort with the correct id. <code>null</code> if no effort was found.
	 */
	public static Effort getEffort(String id) {
		for (Effort effort: efforts) {
			if (effort.getId().equalsIgnoreCase(id)) {
				return new Effort(effort.getId(), effort.getDisplay(), effort.getDescription());
			}
		}
		return null;
	}
}
