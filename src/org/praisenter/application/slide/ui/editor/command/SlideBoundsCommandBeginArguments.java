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
package org.praisenter.application.slide.ui.editor.command;

import java.awt.Dimension;
import java.awt.Point;

import org.praisenter.slide.AbstractPositionedSlide;

/**
 * Class used as input for the {@link Command#begin(Object)} method for {@link BoundsCommand}s for {@link AbstractPositionedSlide}s.
 * @author William Bittle
 * @version 2.0.2
 * @since 2.0.0
 */
public class SlideBoundsCommandBeginArguments extends AbstractBoundsCommandBeginArguments implements BoundsCommandBeginArguments {
	/** The slide */
	protected AbstractPositionedSlide slide;
	
	/**
	 * Full constructor.
	 * @param start the start point in slide space
	 * @param slide the slide
	 */
	public SlideBoundsCommandBeginArguments(Point start, AbstractPositionedSlide slide) {
		super(start);
		this.slide = slide;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.ui.editor.command.BoundsCommandBeginArguments#translate(int, int)
	 */
	@Override
	public void translate(int dx, int dy) {
		this.slide.translate(dx, dy);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.ui.editor.command.BoundsCommandBeginArguments#resize(int, int)
	 */
	@Override
	public Dimension resize(int dw, int dh) {
		return this.slide.resize(dw, dh);
	}
	
	/**
	 * Returns the slide.
	 * @return {@link AbstractPositionedSlide}
	 */
	public AbstractPositionedSlide getSlide() {
		return this.slide;
	}
}
