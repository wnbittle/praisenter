package org.praisenter.media.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.SwingConstants;

import org.praisenter.media.MediaThumbnail;

/**
 * Custom list cell renderer for {@link MediaThumbnail}s.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class MediaThumbnailListCellRenderer extends DefaultListCellRenderer {	
	/** The version id */
	private static final long serialVersionUID = -8260540909617276091L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof MediaThumbnail) {
			MediaThumbnail t = (MediaThumbnail)value;
			this.setIcon(new ImageIcon(t.getImage()));
			this.setHorizontalTextPosition(SwingConstants.CENTER);
			this.setVerticalTextPosition(SwingConstants.BOTTOM);
			this.setText(t.getFile().getName());
			this.setToolTipText(t.getFile().getName());
			this.setHorizontalAlignment(CENTER);
		}
		return this;
	}
}