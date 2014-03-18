package de.imc.mirror.arapp.server;

import java.util.List;

public class EvidenceFile {

	
	List<Byte> content;
	String filename;
	
	public EvidenceFile(List<Byte> content, String filename) {
		this.content = content;
		this.filename = filename;
	}
	
	public List<Byte> getContent() {
		return content;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public int hashCode() {
		return content.hashCode() * 8 + filename.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof EvidenceFile) {
			EvidenceFile ef = (EvidenceFile) o;
			if (ef.getFilename().equals(filename)) {
				List<Byte> oContent = ef.getContent();
				for (int i=0; i<oContent.size(); i++) {
					if (oContent.get(i) != content.get(i)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
}
