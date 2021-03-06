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

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Custom cell renderer for Bible searching used to show checkboxes grouped.
 * <p>
 * This renderer delegates to the given renderer pass in the constructor.
 * @author William Bittle
 * @version 2.0.1
 * @since 2.0.1
 */
public class SongSearchCheckTableCellRenderer implements TableCellRenderer, Serializable {
	/** The version id */
	private static final long serialVersionUID = -4429625841931086389L;
	
	/** The delegate cell renderer */
	private TableCellRenderer delegate;
	
	/** An empty label field to be used for non-header rows */
	private JLabel lblEmpty;
	
	/**
	 * Minimal constructor.
	 * <p>
	 * The delegate TableCellRenderer will be used to generate the component to show
	 * when the row is a header row.  When a row is a sub row, a blank label component
	 * is shown.
	 * @param delegate the delegate cell renderer
	 */
	public SongSearchCheckTableCellRenderer(TableCellRenderer delegate) {
		this.delegate = delegate;
		this.lblEmpty = new JLabel("");
		this.lblEmpty.setOpaque(true);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component component = this.delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		row = table.convertRowIndexToModel(row);
		SongSearchTableModel model = (SongSearchTableModel)table.getModel();
		
		// make sure the value is of type string (sanity check)
		if (model.isGroupHeader(row)) {
			return component;
		} else {
			lblEmpty.setBackground(component.getBackground());
			return lblEmpty;
		}
	}
}
