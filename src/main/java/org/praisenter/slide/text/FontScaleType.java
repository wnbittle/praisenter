package org.praisenter.slide.text;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum FontScaleType {
	/** No scaling */
	NONE,
	
	/** The specified font is the maximum size */
	REDUCE_SIZE_ONLY,
	
	/** The font will grow/shrink to fix the available space */
	BEST_FIT
}