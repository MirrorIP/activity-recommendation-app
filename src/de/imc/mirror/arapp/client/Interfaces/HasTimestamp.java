package de.imc.mirror.arapp.client.Interfaces;

import java.util.Comparator;

import com.google.gwt.i18n.client.DateTimeFormat;

public interface HasTimestamp {

	public static final DateTimeFormat TIMESTAMPFORMAT = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
	
	public static final DateTimeFormat SHORTDATE = DateTimeFormat.getFormat("dd.MM.yyyy");
	public static final DateTimeFormat MEDIUMDATE = DateTimeFormat.getFormat("dd.MM.yyyy - HH:mm");
	public static final DateTimeFormat LONGDATE = DateTimeFormat.getFormat("dd. MMMM yyyy - HH:mm");
	
	
	public static final Comparator<HasTimestamp> COMPAREAGAINSTTIMESTAMP = new Comparator<HasTimestamp>() {

		@Override
		public int compare(HasTimestamp left, HasTimestamp right) {
			String timestamp1 = left.getTimestamp();
			String timestamp2 = right.getTimestamp();
			return timestamp2.compareTo(timestamp1);
		}
		
	};
	

	/**
	 * @param format the format to parse the timestamp to.
	 * @return the timestamp in the given format.
	 */
	public String getFormattedTimestamp(DateTimeFormat format);

	/**
	 * Returns the timestamp in the ISO8601 format.
	 * @return the timestamp.
	 */
	public String getTimestamp();

	/**
	 * Sets the timestamp of this entry.
	 * @param timestamp the timestmap to set
	 */
	public void setTimestamp(String timestamp);

}
