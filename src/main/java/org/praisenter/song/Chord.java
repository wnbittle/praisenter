package org.praisenter.song;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "chord")
@XmlAccessorType(XmlAccessType.NONE)
public final class Chord implements VerseFragment {
	/** The chord */
	@XmlAttribute(name = "name", required = false)
	String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
