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

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.praisenter.application.resources.Messages;
import org.praisenter.data.song.Song;

/**
 * Table model for the results of a song search.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class SongTableModel extends AbstractTableModel {
	/** The version id */
	private static final long serialVersionUID = -5649109704788085612L;

	/** The column names */
	protected final String[] columnNames = new String[] {
		Messages.getString("panel.song.title")
	};
	
	/** The data (list of songs) */
	protected List<Song> songs;
	
	/** Default constructor */
	public SongTableModel() {}
	
	/**
	 * Full constructor.
	 * @param songs the list of songs
	 */
	public SongTableModel(List<Song> songs) {
		this.songs = songs;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		if (this.songs != null) {
			return this.songs.size();
		}
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (this.songs != null && this.songs.size() > rowIndex) {
			Song song = this.songs.get(rowIndex);
			switch (columnIndex) {
				case 0:
					return song.getTitle();
				default:
					return "";
			}
		}
		return null;
	}
	
	/**
	 * Returns the row data for the given row index.
	 * @param rowIndex the row index
	 * @return {@link Song}
	 */
	public Song getRow(int rowIndex) {
		if (this.songs != null && this.songs.size() > rowIndex) {
			return this.songs.get(rowIndex);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return this.columnNames[column];
	}
}
