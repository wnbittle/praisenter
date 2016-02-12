package org.praisenter.slide.text;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum VerticalTextAlignment {
	/** Text should be aligned at the top */
	TOP,
	/** Text should be aligned at the bottom */
	BOTTOM,
	/** Text should be aligned in the center */
	CENTER
}