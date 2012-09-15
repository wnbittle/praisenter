package org.praisenter.control;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Panel used to display an image.  The image will be stretched to fit the size of the panel
 * using uniform scaling.  This panel will use the fastest scaling opertation.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class ImagePanel extends JPanel {
	/** The version id */
	private static final long serialVersionUID = -313790744153697516L;

	/** The image */
	private BufferedImage image;
	
	/** The scaled image */
	private BufferedImage scaledImage;
	
	/**
	 * Default constructor.
	 */
	public ImagePanel() {}
	
	/**
	 * Optional constructor.
	 * @param image the initial image
	 */
	public ImagePanel(BufferedImage image) {
		this.image = image;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (this.image != null) {
			Rectangle cr = this.getPaintRectangle();
			// get the component width
			int cw = cr.width;
			int ch = cr.height;
			
			// see if the scaled image is null
			if (this.scaledImage != null) {
				// check the scaling
				int sw = this.scaledImage.getWidth();
				int sh = this.scaledImage.getHeight();
				if (sw != cw || sh != ch) {
					// we need to rescale the image
					this.scaledImage = null;
				}
			}
			
			// check if we need to scale the image
			if (this.scaledImage == null) {
				// get the width/height
		        int w = this.image.getWidth();
		        int h = this.image.getHeight();
		        
		        if (w <= cw && h <= ch) {
		        	// dont bother scaling
		        	this.scaledImage = this.image;
		        } else {
			        // get the scaling factors
			        double pw = (double)cw / (double)w;
			    	double ph = (double)ch / (double)h;
			    	// use uniform scaling
			    	double s = 1.0;
			    	if (pw < ph) {
			    		// the width scaling is more dramatic so use it
			    		s = pw;
			    	} else {
			    		s = ph;
			    	}
			        // attempt to resize it (just use the fastest scaling version)
			        AffineTransformOp scale = new AffineTransformOp(AffineTransform.getScaleInstance(s, s), AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			        this.scaledImage = scale.filter(this.image, null);
		        }
			}
			
			// get the position to place the image
			int iw = this.scaledImage.getWidth();
			int ih = this.scaledImage.getHeight();
			
			int x = cr.x + (cw - iw) / 2;
			int y = cr.y + (ch - ih) / 2;
			
			g.drawImage(this.scaledImage, x, y, null);
		}
	}
	
	/**
	 * Returns the rectangle in which we should paint considering
	 * the insets.
	 * @return Rectangle
	 */
	private Rectangle getPaintRectangle() {
		Insets insets = this.getInsets();
		return new Rectangle(
				insets.left,
				insets.top,
				this.getWidth() - insets.left - insets.right,
				this.getHeight() - insets.top - insets.bottom);
	}
	
	/**
	 * Returns the image this panel is rendering.
	 * @return BufferedImage
	 */
	public BufferedImage getImage() {
		return this.image;
	}
	
	/**
	 * Returns the scaled image this panel is rendering.
	 * @return BufferedImage
	 */
	public BufferedImage getScaledImage() {
		return this.scaledImage;
	}
	
	/**
	 * Sets the image this panel should render.
	 * @param image the image
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
		this.scaledImage = null;
		this.repaint();
	}
}
