package org.praisenter.settings.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.praisenter.display.Display;
import org.praisenter.display.GraphicsComponent;
import org.praisenter.images.Images;
import org.praisenter.settings.RootSettings;
import org.praisenter.settings.SettingsException;
import org.praisenter.utilities.ImageUtilities;
import org.praisenter.utilities.WindowUtilities;

/**
 * Panel used to manage a display.
 * @author William Bittle
 * @param <E> the {@link RootSettings} type
 * @param <T> the {@link Display} type
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class DisplaySettingsPanel<E extends RootSettings<E>, T extends Display> extends JPanel implements SettingsPanel, MouseMotionListener, MouseListener, PropertyChangeListener {
	/** The version id */
	private static final long serialVersionUID = -3587523680958177637L;
	
	/** The resize gap (the additional gap area to allow resizing) */
	private static final int RESIZE_GAP = 30;
	
	/** The component line width */
	private static final int LINE_WIDTH = 2;
	
	/** Half the line width */
	private static final int HALF_LINE_WIDTH = LINE_WIDTH / 2;
	
	/** The shadow width */
	private static final int SHADOW_WIDTH = 8;
	
	/** The total border width */
	private static final int TOTAL_BORDER_WIDTH	= 4;
	
	/** The dash length */
	private static final float DASH_LENGTH = 5.0f;
	
	/** The border color */
	private static final Color BORDER_COLOR = Color.YELLOW;
	
	/** Property used to notify of a display component change */
	protected static final String DISPLAY_COMPONENT_PROPERTY = "DisplayComponent";
	
	// input
	
	/** The settings to setup */
	protected E settings;
	
	/** The size of the display */
	protected Dimension displaySize;

	/** The display (created from the settings) */
	protected T display;

	// components
	
	/** The panel used to preview the changes */
	protected JPanel pnlDisplayPreview;
	
	// mouse commands
	
	/** Mouse command for moving components */
	private MoveCommand moveCommand = new MoveCommand();
	
	/** Mouse command for resizing components */
	private ResizeCommand resizeCommand = new ResizeCommand();
	
	/** Mouse command for resizing the width of components */
	private ResizeWidthCommand resizeWidthCommand = new ResizeWidthCommand();
	
	/** Mouse command for resizing the height of components */
	private ResizeHeightCommand resizeHeightCommand = new ResizeHeightCommand();
	
	/** Mouse command mutually exclusive group */
	private MutexCommandGroup<BoundsCommand> mouseCommandGroup = new MutexCommandGroup<BoundsCommand>(new BoundsCommand[] {
			this.moveCommand,
			this.resizeCommand,
			this.resizeWidthCommand,
			this.resizeHeightCommand
	});
	
	// computed
	
	/** The scale factor from Display coordinates to this components coordinates */
	private double scale = 1.0;
	
	// temporary
	
	/** The component the mouse is over */
	private GraphicsComponent mouseOverComponent;

	/** The map of cached images */
	private Map<String, BufferedImage> cachedImages; 
	
	/**
	 * Minimal constructor.
	 * @param settings the display settings to setup
	 * @param displaySize the display target size
	 */
	@SuppressWarnings("serial")
	public DisplaySettingsPanel(E settings, Dimension displaySize) {
		// set the settings and size
		this.settings = settings;
		this.displaySize = displaySize;
		
		this.cachedImages = new HashMap<String, BufferedImage>();
		
		// create a display for preview and setup
		this.display = this.getDisplay(settings, displaySize);
		
		Dimension previewSize = new Dimension(400, 200);
		this.pnlDisplayPreview = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintPreivew((Graphics2D)g);
			}
		};
		this.pnlDisplayPreview.setMinimumSize(previewSize);
		this.pnlDisplayPreview.addMouseListener(this);
		this.pnlDisplayPreview.addMouseMotionListener(this);
	}
	
	/**
	 * Returns a new display for the given settings and display size.
	 * @param settings the settings
	 * @param displaySize the display size
	 * @return T
	 */
	protected abstract T getDisplay(E settings, Dimension displaySize);
	
	/**
	 * Sets all settings using the current state of the components.
	 * @throws SettingsException if an exception occurs while assigning a setting
	 */
	protected abstract void setSettingsFromComponents() throws SettingsException;

	/**
	 * Returns the first component that is at the given point.
	 * @param displayPoint the Display space point
	 * @return {@link GraphicsComponent}
	 */
	protected abstract GraphicsComponent getGraphicsComponent(Point displayPoint);

	/**
	 * Returns true if the given display point is inside any component.
	 * @param displayPoint the Display space point
	 * @return boolean
	 */
	protected abstract boolean isInsideAny(Point displayPoint);

	/* (non-Javadoc)
	 * @see org.praisenter.panel.setup.SetupPanel#saveSettings()
	 */
	@Override
	public void saveSettings() throws SettingsException {
		// set the settings using the components
		this.setSettingsFromComponents();
	}
	
	/**
	 * Returns the equivalent point in Display space from the given
	 * panel space point.
	 * @param panelPoint the panel point
	 * @return Point
	 */
	protected Point getDisplayPoint(Point panelPoint) {
		Point point = new Point();
		// translate by the shadow width and border width
		point.setLocation((panelPoint.x - SHADOW_WIDTH - 2) / this.scale, (panelPoint.y - SHADOW_WIDTH - 2) / this.scale);
		return point;
	}

	/**
	 * Returns true if the given display point is inside the given component.
	 * @param displayPoint the Display space point
	 * @param component the component
	 * @return boolean
	 */
	protected boolean isInside(Point displayPoint, GraphicsComponent component) {
		// get the bounds
		Rectangle bounds = component.getBounds();
		// expand the bounds by the line width
		int bx = bounds.x - HALF_LINE_WIDTH + 2;
		int by = bounds.y - HALF_LINE_WIDTH + 2;
		int bw = bounds.width + HALF_LINE_WIDTH + 2;
		int bh = bounds.height + HALF_LINE_WIDTH + 2;
		
		int px = displayPoint.x;
		int py = displayPoint.y;
		// see if the point is within the bounds
		if (px >= bx && px <= bx + bw) {
			if (py >= by && py <= by + bh) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Paints the preview to the given graphics object.
	 * @param graphics the graphics object to paint to
	 */
	protected void paintPreivew(Graphics2D graphics) {
		// paint the display
		if (this.display != null) {
			// get the display size
			Dimension ds = this.displaySize;
			int dw = ds.width;
			int dh = ds.height;
			
			// get this panel's size
			int cw = this.pnlDisplayPreview.getWidth() - SHADOW_WIDTH * 2 - TOTAL_BORDER_WIDTH;
			int ch = this.pnlDisplayPreview.getHeight() - SHADOW_WIDTH * 2 - TOTAL_BORDER_WIDTH;
			
			// get the size ratios
			double pw = (double)cw / (double)dw;
			double ph = (double)ch / (double)dh;
			
			// choose the most dramatic scale
			this.scale = ph;
			if (pw < ph) {
				this.scale = pw;
			}
			double bw = dw * this.scale;
			double bh = dh * this.scale;
			
			// paint the shadow background
			this.paintShadow(graphics, "SHADOW", bw + TOTAL_BORDER_WIDTH, bh + TOTAL_BORDER_WIDTH, SHADOW_WIDTH);
			
			graphics.translate(SHADOW_WIDTH + 2, SHADOW_WIDTH + 2);
			
			// paint the borders
			graphics.setColor(Color.GRAY);
			graphics.fill(new Rectangle2D.Double(-2, -2, bw + TOTAL_BORDER_WIDTH, bh + TOTAL_BORDER_WIDTH));
			graphics.setColor(Color.WHITE);
			graphics.fill(new Rectangle2D.Double(-1, -1, bw + 2, bh + 2));
			
			// tile the transparent background
			this.paintTransparentBackground(graphics, "BACKGROUND", bw, bh);
			
			// apply a scaling transform
			AffineTransform ot = graphics.getTransform();
			graphics.transform(AffineTransform.getScaleInstance(this.scale, this.scale));
			
			// use the fastest rendering possible
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			
			// render the display
			this.display.render(graphics);
			
			graphics.setTransform(ot);
			
			// render the bounds of floating components
			Stroke oStroke = graphics.getStroke();
			// set the line width
			graphics.setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, HALF_LINE_WIDTH, new float[] { DASH_LENGTH }, 0));
			// only draw a border for the hovered component
			if (this.mouseOverComponent != null) {
				Rectangle b = this.mouseOverComponent.getBounds();
				// FIXME need to change the color of the border depending on the color(s) of the background (image, video, color)
				// wrap the text component in a green border
				// make sure this green border is inside the bounds of the rectangle
				// so that the resizing makes more sense
				graphics.setColor(BORDER_COLOR);
				graphics.drawRect(
						(int)Math.ceil(b.x * this.scale) + HALF_LINE_WIDTH, 
						(int)Math.ceil(b.y * this.scale) + HALF_LINE_WIDTH, 
						(int)Math.ceil(b.width * this.scale) - LINE_WIDTH, 
						(int)Math.ceil(b.height * this.scale) - LINE_WIDTH);
			}
			graphics.setStroke(oStroke);
		}
	}
	
	/**
	 * Paints a drop shadow for the given display.
	 * @param g2d the graphics to paint to
	 * @param name the display name
	 * @param w the width of the display
	 * @param h the height of the display
	 * @param sw the shadow width
	 */
	private void paintShadow(Graphics2D g2d, String name, double w, double h, int sw) {
		BufferedImage image = this.cachedImages.get(name);
		
		// see if we need to re-render the image
		if (image == null || image.getWidth() != w || image.getHeight() != h) {
			// create a new image of the right size
			image = ImageUtilities.getDropShadowImage(g2d.getDeviceConfiguration(), w, h, sw);
			this.cachedImages.put(name, image);
		}
		
		// render the image
		g2d.drawImage(image, 0, 0, null);
	}
	
	/**
	 * Paints a drop shadow for the given display.
	 * @param g2d the graphics to paint to
	 * @param name the display name
	 * @param w the width of the display
	 * @param h the height of the display
	 */
	private void paintTransparentBackground(Graphics2D g2d, String name, double w, double h) {
		BufferedImage image = this.cachedImages.get(name);
		
		// see if we need to re-render the image
		if (image == null || image.getWidth() != w || image.getHeight() != h) {
			// create a new image of the right size
			image = ImageUtilities.getTiledImage(Images.TRANSPARENT_BACKGROUND, g2d.getDeviceConfiguration(), (int)Math.ceil(w), (int)Math.ceil(h));
			this.cachedImages.put(name, image);
		}
		
		// render the image
		g2d.drawImage(image, 0, 0, null);
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String p = event.getPropertyName();
		// check the property name
		if (DISPLAY_COMPONENT_PROPERTY.equals(p)) {
			// repaint the preview
			this.repaint();
		} else if (GeneralSettingsPanel.DISPLAY_PROPERTY.equals(p)) {
			// the display was updated so we need to update the preview panel
			GraphicsDevice device = (GraphicsDevice)event.getNewValue();
			Dimension size = WindowUtilities.getDimension(device.getDisplayMode());
			// set the display size
			this.displaySize = size;
			// set the size of the display (so that it resizes its components)
			this.display.setDisplaySize(size);
			// repaint the preview panel
			this.pnlDisplayPreview.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		if (!this.mouseCommandGroup.isCommandActive()) {
			this.setCursor(Cursor.getDefaultCursor());
			this.mouseOverComponent = null;
			this.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// check for mouse button 1
		if (e.getButton() == MouseEvent.BUTTON1) {
			// make sure no other command is active
			if (!this.mouseCommandGroup.isCommandActive()) {
				// convert the point to display space
				Point point = this.getDisplayPoint(e.getPoint());
				GraphicsComponent component = null;
				// see if we are still over the same component
				if (this.mouseOverComponent != null && this.isInside(point, this.mouseOverComponent)) {
					// then set the component as the one we are still over
					component = this.mouseOverComponent;
				} else {
					// otherwise see if we are over a different one
					component = this.getGraphicsComponent(point);
				}
				// make sure we found one
				if (component != null) {
					// get the bounds
					Rectangle bounds = component.getBounds();
					int rx = bounds.x + bounds.width - RESIZE_GAP;
					int ry = bounds.y + bounds.height - RESIZE_GAP;
					if (point.x >= rx && point.y >= ry) {
						// resize both dimensions
						this.resizeCommand.begin(point, component);
					} else if (point.x >= rx) {
						// resize width
						this.resizeWidthCommand.begin(point, component);
					} else if (point.y >= ry) {
						// resize height
						this.resizeHeightCommand.begin(point, component);
					} else {
						// move
						this.moveCommand.begin(point, component);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// check for mouse button 1
		if (e.getButton() == MouseEvent.BUTTON1) {
			// end all the mouse button 1 commands
			this.moveCommand.end();
			this.resizeCommand.end();
			this.resizeWidthCommand.end();
			this.resizeHeightCommand.end();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// see if any mouse command is active
		if (this.mouseCommandGroup.isCommandActive()) {
			// convert the point to display space
			Point end = this.getDisplayPoint(e.getPoint());
			// check if we are still over a component
			if (this.moveCommand.isActive()) {
				// move the component
				this.moveCommand.update(end);
			} else if (this.resizeCommand.isActive()) {
				this.resizeCommand.update(end);
			} else if (this.resizeWidthCommand.isActive()) {
				this.resizeWidthCommand.update(end);
			} else if (this.resizeHeightCommand.isActive()) {
				this.resizeHeightCommand.update(end);
			}
			// if we modify a component then we need to redraw it
			this.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// make sure no other command is active in this group
		if (!this.mouseCommandGroup.isCommandActive()) {
			// convert the point to display space
			Point point = this.getDisplayPoint(e.getPoint());
			GraphicsComponent component = null;
			// see if we are still over the same component
			if (this.mouseOverComponent != null && this.isInside(point, this.mouseOverComponent)) {
				// then set the component as the one we are still over
				component = this.mouseOverComponent;
			} else {
				// otherwise see if we are over a different one
				component = this.getGraphicsComponent(point);
			}
			// make sure we found one
			if (component != null) {
				// set the hover component
				if (component != this.mouseOverComponent) {
					this.mouseOverComponent = component;
					this.repaint();
				}
				// get the bounds
				Rectangle bounds = component.getBounds();
				int rx = bounds.x + bounds.width - RESIZE_GAP;
				int ry = bounds.y + bounds.height - RESIZE_GAP;
				if (point.x >= rx && point.y >= ry) {
					// resize both dimensions
					this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
				} else if (point.x >= rx) {
					// resize width
					this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else if (point.y >= ry) {
					// resize height
					this.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				} else {
					// then just show the move command
					this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
			} else {
				this.repaint();
				this.mouseOverComponent = null;
				this.setCursor(Cursor.getDefaultCursor());
			}
		}
	}
}