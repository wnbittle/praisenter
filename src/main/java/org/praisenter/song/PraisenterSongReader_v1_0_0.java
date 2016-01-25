/*
 * Copyright (c) 2015-2016 William Bittle  http://www.praisenter.org/
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
package org.praisenter.song;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.praisenter.song.openlyrics.OpenLyricsComment;
import org.praisenter.song.openlyrics.OpenLyricsSong;
import org.praisenter.song.openlyrics.OpenLyricsTitle;
import org.praisenter.song.openlyrics.OpenLyricsVerse;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX XML reader for the Praisenter's song format v1.0.0.
 * @author William Bittle
 * @version 3.0.0
 */
public class PraisenterSongReader_v1_0_0 extends DefaultHandler implements SongFormatReader {
	/** The class-level logger */
	private static final Logger LOGGER = LogManager.getLogger();

	/* (non-Javadoc)
	 * @see org.praisenter.data.song.SongFormatReader#read(java.nio.file.Path)
	 */
	@Override
	public List<OpenLyricsSong> read(Path path) throws IOException, SongFormatReaderException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			parser.parse(new InputSource(new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile())))), this);
			
			return this.songs;
		} catch (SAXException | ParserConfigurationException e) {
			throw new SongFormatReaderException(e.getMessage(), e);
		}
	}
	
	// SAX parser implementation

	/** The songs */
	private List<OpenLyricsSong> songs;
	
	/** The song currently being processed */
	private OpenLyricsSong song;
	
	/** The verse currently being processed */
	private OpenLyricsVerse part;
	
	/** Buffer for tag contents */
	private StringBuilder dataBuilder;
	
	/**
	 * Default constructor.
	 */
	public PraisenterSongReader_v1_0_0() {
		this.songs = new ArrayList<OpenLyricsSong>();
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// inspect the tag name
		if (qName.equalsIgnoreCase("Song")) {
			// when we see the <Songs> tag we create a new song
			this.song = new OpenLyricsSong();
			this.song.metadata = new SongMetadata();
		} else if (qName.equalsIgnoreCase("SongPart")) {
			this.part = new OpenLyricsVerse();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// this method can be called a number of times for the contents of a tag
		// this is done to improve performance so we need to append the text before
		// using it
		String s = new String(ch, start, length);
		if (this.dataBuilder == null) {
			this.dataBuilder = new StringBuilder();
		}
		this.dataBuilder.append(s);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("Song".equalsIgnoreCase(qName)) {
			// we are done with the song so add it to the list
			this.songs.add(this.song);
			this.song = null;
		} else if ("SongPart".equalsIgnoreCase(qName)) {
			this.song.getVerses().add(this.part);
			this.part = null;
		} else if ("Title".equalsIgnoreCase(qName)) {
			// make sure the tag was not self terminating
			if (this.dataBuilder != null) {
				// set the song title
				OpenLyricsTitle title = new OpenLyricsTitle();
				title.setOriginal(true);
				title.setText(StringEscapeUtils.unescapeXml(this.dataBuilder.toString().trim()));
				this.song.getProperties().getTitles().add(title);
			}
		} else if ("Notes".equalsIgnoreCase(qName)) {
			// make sure the tag was not self terminating
			if (this.dataBuilder != null) {
				// set the song title
				OpenLyricsComment comment = new OpenLyricsComment();
				comment.setText(StringEscapeUtils.unescapeXml(this.dataBuilder.toString().trim()));
				this.song.getProperties().getComments().add(comment);
			}
		} else if ("DateAdded".equalsIgnoreCase(qName)) {
			// ignore
		} else if ("Type".equalsIgnoreCase(qName)) {
			// make sure the tag was not self terminating
			if (this.dataBuilder != null) {
				// get the type
				String type = this.dataBuilder.toString().trim();
				this.part.setType(getType(type));
			}
		} else if ("Index".equalsIgnoreCase(qName)) {
			// make sure the tag was not self terminating
			if (this.dataBuilder != null) {
				String data = this.dataBuilder.toString().trim();
				try {
					this.part.setNumber(Integer.parseInt(data));
				} catch (NumberFormatException e) {
					LOGGER.warn("Failed to read verse number: {}", data);
				}
			}
		} else if ("Text".equalsIgnoreCase(qName)) {
			// make sure the tag was not self terminating
			if (this.dataBuilder != null) {
				String data = this.dataBuilder.toString().trim();
				this.part.setText(StringEscapeUtils.unescapeXml(data).replaceAll("(\\r|\\r\\n|\\n\\r|\\n)", "<br/>"));
			}
		} else if ("FontSize".equalsIgnoreCase(qName)) {
			// make sure the tag was not self terminating
			if (this.dataBuilder != null) {
				String data = this.dataBuilder.toString().trim();
				try {
					this.song.metadata.verses.add(new VerseMetadata(this.part.name, Integer.parseInt(data)));
				} catch (NumberFormatException e) {
					LOGGER.warn("Failed to read verse font size: {}", data);
				}
			}
		}
		
		this.dataBuilder = null;
	}

	/**
	 * Returns the type string for the given type.
	 * @param type the type
	 * @return String
	 */
	private static final String getType(String type) {
		if ("VERSE".equals(type)) {
			return "v";
		} else if ("PRECHORUS".equals(type)) {
			return "p";
		} else if ("CHORUS".equals(type)) {
			return "c";
		} else if ("BRIDGE".equals(type)) {
			return "b";
		} else if ("TAG".equals(type)) {
			return "t";
		} else if ("VAMP".equals(type)) {
			return "e";
		} else if ("END".equals(type)) {
			return "e";
		} else {
			return "o";
		}
	}
}
