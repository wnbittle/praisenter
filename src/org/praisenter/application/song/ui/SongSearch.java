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
package org.praisenter.application.song.ui;

/**
 * Represents a text search in a song list.
 * @author William Bittle
 * @version 2.0.1
 * @since 1.0.0
 */
public class SongSearch {
	/** The text to search */
	private String text;
	
	/** The search callback */
	private SongSearchCallback callback;
	
	/** True if a distinct list of songs should be returned */
	private boolean distinct;
	
	/**
	 * Minimal constructor.
	 * @param text the text to search for
	 * @param callback the code to run after the search has completed
	 */
	public SongSearch(String text, SongSearchCallback callback) {
		this(text, false, callback);
	}
	
	/**
	 * Optional constructor.
	 * @param text the text to search for
	 * @param distinct true if a distinct song search should be used
	 * @param callback the code to run after the search has completed
	 */
	public SongSearch(String text, boolean distinct, SongSearchCallback callback) {
		this.text = text;
		this.distinct = distinct;
		this.callback = callback;
	}
	
	/**
	 * Returns the text to search for.
	 * @return String
	 */
	public String getText() {
		return this.text;
	}
	
	/**
	 * Returns true if a distinct song search should be used.
	 * @return boolean
	 */
	public boolean isDistinct() {
		return this.distinct;
	}
	
	/**
	 * Returns the code to run after the search is completed.
	 * @return {@link SongSearchCallback}
	 */
	public SongSearchCallback getCallback() {
		return this.callback;
	}
}
