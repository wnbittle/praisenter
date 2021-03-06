/*
 * Copyright (c) 2011-2013 William Bittle  http://www.praisenter.org/
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted 
 * provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions 
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
 *     and the following disclaimer in the documentation and/or other materials provided with the 
 *     distribution.
 *   * Neither the name of Praisenter nor the names of its contributors may be used to endorse or 
 *     promote products derived from this software without specific prior written permission.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.praisenter.slide;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.praisenter.slide.resources.Messages;
import org.praisenter.slide.text.TextComponent;

/**
 * Specific slide for showing bible verses.
 * <p>
 * This slide has all the functionality of a normal slide but adds two
 * {@link TextComponent}s for the bible location and text.  These components
 * cannot be removed, but can be edited.
 * @author William Bittle
 * @version 2.0.0
 * @since 2.0.0
 */
@XmlRootElement(name = "BibleSlide")
@XmlAccessorType(XmlAccessType.NONE)
public class BibleSlide extends BasicSlide implements Slide, Serializable {
	/** The version id */
	private static final long serialVersionUID = -4960923069594566353L;

	/** The scripture location component (like: Genesis 1:1) */
	@XmlElement(name = "ScriptureLocationComponent")
	protected TextComponent scriptureLocationComponent;
	
	/** The scripture text component (like: In the beginning...) */
	@XmlElement(name = "ScriptureTextComponent")
	protected TextComponent scriptureTextComponent;
	
	/**
	 * Default constructor.
	 * <p>
	 * This constructor should only be used by JAXB for
	 * marshalling and unmarshalling the objects.
	 */
	protected BibleSlide() {
		this(Messages.getString("slide.unnamed"), 400, 400);
	}
	
	/**
	 * Full constructor.
	 * @param name the name of the template
	 * @param width the width of the slide
	 * @param height the height of the slide
	 */
	public BibleSlide(String name, int width, int height) {
		super(name, width, height);
		
		// get the minimum dimension (typically the height)
		int maxd = height;
		if (maxd > width) {
			// the width is smaller so use it
			maxd = width;
		}
		// set the default screen to text component padding
		final int margin = (int)Math.floor((double)maxd * 0.04);
		
		// compute the default width, height and position
		final int h = height - margin * 2;
		final int w = width - margin * 2;
		
		final int tth = (int)Math.ceil((double)h * 0.20);
		final int th = h - tth - margin;
		
		this.scriptureLocationComponent = new TextComponent(Messages.getString("slide.bible.location.name"), margin, margin, w, tth);
		this.scriptureTextComponent = new TextComponent(Messages.getString("slide.bible.text.name"), margin, tth + margin * 2, w, th);
		
		this.scriptureLocationComponent.setOrder(1);
		this.scriptureTextComponent.setOrder(2);
	}
	
	/**
	 * Copy constructor.
	 * <p>
	 * This will perform a deep copy where necessary.
	 * @param slide the slide to copy
	 */
	public BibleSlide(BibleSlide slide) {
		super(slide);
		this.scriptureLocationComponent = slide.scriptureLocationComponent.copy();
		this.scriptureTextComponent = slide.scriptureTextComponent.copy();
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#copy()
	 */
	@Override
	public BibleSlide copy() {
		return new BibleSlide(this);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#createTemplate()
	 */
	public BibleSlideTemplate createTemplate() {
		return new BibleSlideTemplate(this);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.BasicSlide#getComponents(java.lang.Class, boolean)
	 */
	@Override
	public <E extends SlideComponent> List<E> getComponents(Class<E> clazz, boolean includeBackground) {
		List<E> components = super.getComponents(clazz, includeBackground);
		if (clazz.isAssignableFrom(TextComponent.class)) {
			components.add(clazz.cast(this.scriptureLocationComponent));
			components.add(clazz.cast(this.scriptureTextComponent));
		}
		this.sortComponentsByOrder(components);
		return components;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.Slide#getStaticComponents(java.lang.Class)
	 */
	@Override
	public <E extends SlideComponent> List<E> getStaticComponents(Class<E> clazz) {
		if (clazz.isAssignableFrom(TextComponent.class)) {
			List<E> components = new ArrayList<E>();
			components.add(clazz.cast(this.scriptureLocationComponent));
			components.add(clazz.cast(this.scriptureTextComponent));
			this.sortComponentsByOrder(components);
			return components;
		}
		return super.getStaticComponents(clazz);
	}
	
	/**
	 * Returns the scripture location component.
	 * @return {@link TextComponent}
	 */
	public TextComponent getScriptureLocationComponent() {
		return this.scriptureLocationComponent;
	}

	/**
	 * Returns the scripture text component.
	 * @return {@link TextComponent}
	 */
	public TextComponent getScriptureTextComponent() {
		return this.scriptureTextComponent;
	}
}
