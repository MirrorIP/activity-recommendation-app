package de.imc.mirror.arapp.client;


import com.google.gwt.i18n.client.DateTimeFormat;

import de.imc.mirror.arapp.client.Interfaces.HasTimestamp;

public class Entry implements HasTimestamp{
	
	private String timestamp;
	private String message;
	private int id;
	
	/**
	 * Creates a new Entry for the minutes of a recommendation.
	 * @param id the id of the entry.
	 * @param message the message of the entry.
	 * @param timestamp the timestamp of the entry.
	 */
	public Entry(int id, String message, String timestamp) {
		this.message = message;
		this.timestamp = timestamp;
		this.id = id;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return the id of the entry.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the message of the entry
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String getFormattedTimestamp(DateTimeFormat format) {
		return format.format(HasTimestamp.TIMESTAMPFORMAT.parseStrict(timestamp));
	}

	@Override
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;		
	}

}
