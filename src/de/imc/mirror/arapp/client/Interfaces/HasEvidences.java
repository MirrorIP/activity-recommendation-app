package de.imc.mirror.arapp.client.Interfaces;

import java.util.List;

import de.imc.mirror.arapp.client.Evidence;

public interface HasEvidences {
	
	/**
	 * Attaches a list of evidences.
	 * @param evidences the list of evidences to be attached.
	 */
	public void attachEvidences(List<Evidence> evidences);
	
	/**
	 * Attaches a newly created evidence.
	 * @param ev the created evidence.
	 */
	public void attachCreatedEvidence(Evidence ev);
	
	/**
	 * Method to get the location where a file should be saved, e.g. the space where an experience will be published.
	 * @return the location where a file should be saved.
	 */
	public String getFileEvidenceLocation();
	
	/**
	 * @return the custom id.
	 */
	public String getCustomId();
}
