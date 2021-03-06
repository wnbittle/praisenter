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
package org.praisenter.slide.media;

import java.io.Serializable;

import org.praisenter.media.MediaPlayerListener;
import org.praisenter.media.PlayableMedia;
import org.praisenter.slide.SlideComponent;

/**
 * Interface representing a media component that must run while being displayed (video, audio, etc).
 * @param <E> the {@link PlayableMedia} type
 * @author William Bittle
 * @version 2.0.0
 * @since 2.0.0
 */
public interface PlayableMediaComponent<E extends PlayableMedia> extends MediaComponent<E>, MediaPlayerListener, SlideComponent, Serializable {
	/**
	 * Returns true if looping of the media is enabled.
	 * @return boolean
	 */
	public abstract boolean isLoopEnabled();
	
	/**
	 * Sets looping of the media to true or false.
	 * @param loopEnabled true if looping should be enabled
	 */
	public abstract void setLoopEnabled(boolean loopEnabled);
	
	/**
	 * Returns true if the audio is muted.
	 * @return boolean
	 */
	public abstract boolean isAudioMuted();

	/**
	 * Sets the audio to muted or not.
	 * @param audioMuted true if the audio should be muted
	 */
	public abstract void setAudioMuted(boolean audioMuted);
}
