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


/**
 * Represents a resize {@link Command}.
 * @author William Bittle
 * @version 2.0.0
 * @since 2.0.0
 */
public class ResizeWidthAndHeightCommand extends ResizeCommand {
	/* (non-Javadoc)
	 * @see org.praisenter.slide.ui.editor.command.BoundsCommand#update(java.awt.Point)
	 */
	public synchronized void update(Point end) {
		int xdiff = end.x - this.current.x;
		int ydiff = end.y - this.current.y;
		
		if (this.prongLocation == ResizeProngLocation.BOTTOM_RIGHT) {
			this.beginArguments.resize(xdiff, ydiff);
		} else if (this.prongLocation == ResizeProngLocation.BOTTOM_LEFT) {
			// reposition the x coorindate and modify the size
			Dimension ds = this.beginArguments.resize(-xdiff, ydiff);
			// only translate by the actual amount resized
			this.beginArguments.translate(-ds.width, 0);
		} else if (this.prongLocation == ResizeProngLocation.TOP_LEFT) {
			// reposition both coorindates and modify the size
			Dimension ds = this.beginArguments.resize(-xdiff, -ydiff);
			// only translate by the actual amount resized
			this.beginArguments.translate(-ds.width, -ds.height);
		} else if (this.prongLocation == ResizeProngLocation.TOP_RIGHT) {
			// reposition the y coorindate and modify the size
			Dimension ds = this.beginArguments.resize(xdiff, -ydiff);
			// only translate by the actual amount resized
			this.beginArguments.translate(0, -ds.height);
		}
		
		super.update(end);
	}
}
