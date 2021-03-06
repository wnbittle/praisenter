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
package org.praisenter.slide.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.praisenter.common.utilities.FontManager;
import org.praisenter.common.xml.FontTypeAdapter;
import org.praisenter.slide.AbstractPositionedComponent;
import org.praisenter.slide.PositionedComponent;
import org.praisenter.slide.RenderableComponent;
import org.praisenter.slide.SlideComponent;
import org.praisenter.slide.graphics.CapType;
import org.praisenter.slide.graphics.ColorFill;
import org.praisenter.slide.graphics.DashPattern;
import org.praisenter.slide.graphics.Fill;
import org.praisenter.slide.graphics.FillTypeAdapter;
import org.praisenter.slide.graphics.JoinType;
import org.praisenter.slide.graphics.LineStyle;
import org.praisenter.slide.graphics.LinearGradientDirection;
import org.praisenter.slide.graphics.LinearGradientFill;
import org.praisenter.slide.graphics.Point;
import org.praisenter.slide.graphics.RadialGradientFill;
import org.praisenter.slide.graphics.Stop;
import org.praisenter.slide.resources.Messages;

/**
 * Represents a component that displays text.
 * @author William Bittle
 * @version 2.0.2
 * @since 2.0.0
 */
@XmlRootElement(name = "TextComponent")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({
	ColorFill.class,
	LinearGradientFill.class,
	RadialGradientFill.class
})
public class TextComponent extends AbstractPositionedComponent implements PositionedComponent, RenderableComponent, SlideComponent, Serializable {
	/** The version id */
	private static final long serialVersionUID = 6365686545826144182L;

	/** The text */
	@XmlElement(name = "Text", required = false, nillable = true)
	protected String text;
	
	/** The text color */
	@XmlElement(name = "TextFill")
	@XmlJavaTypeAdapter(value = FillTypeAdapter.class)
	protected Fill textFill;
	
	/** The text font */
	@XmlElement(name = "TextFont", required = false, nillable = true)
	@XmlJavaTypeAdapter(value = FontTypeAdapter.class)
	protected Font textFont;
	
	/** The horizontal text alignment */
	@XmlElement(name = "HorizontalTextAlignment", required = false, nillable = true)
	protected HorizontalTextAlignment horizontalTextAlignment;
	
	/** The vertical text alignment */
	@XmlElement(name = "VerticalTextAlignment", required = false, nillable = true)
	protected VerticalTextAlignment verticalTextAlignment; 
	
	/** The font scale type */
	@XmlElement(name = "FontScaleType", required = false, nillable = true)
	protected FontScaleType textFontScaleType;
	
	/** True if this text should wrap */
	@XmlElement(name = "TextWrapped", required = false, nillable = true)
	protected boolean textWrapped;
	
	/** The inner component padding */
	@XmlElement(name = "TextPadding", required = false, nillable = true)
	protected int textPadding;
	
	/** True if the text should be visible */
	@XmlElement(name = "TextVisible", required = false, nillable = true)
	protected boolean textVisible;
	
	/** True if the text outline should be visible */
	@XmlElement(name = "TextOutlineVisible", required = false, nillable = true)
	protected boolean textOutlineVisible;
	
	/** The text outline style */
	@XmlElement(name = "TextOutlineStyle", required = false, nillable = true)
	protected LineStyle textOutlineStyle;
	
	/** The text outline fill */
	@XmlElement(name = "TextOutlineFill", required = false, nillable = true)
	@XmlJavaTypeAdapter(value = FillTypeAdapter.class)
	protected Fill textOutlineFill;
	
	/** True if the text shadow should be visible */
	@XmlElement(name = "TextShadowVisible", required = false, nillable = true)
	protected boolean textShadowVisible;
	
	/** The text shadow fill */
	@XmlElement(name = "TextShadowFill", required = false, nillable = true)
	@XmlJavaTypeAdapter(value = FillTypeAdapter.class)
	protected Fill textShadowFill;
	
	/** The text shadow offset */
	@XmlElement(name = "TextShadowOffset", required = false, nillable = true)
	protected Point textShadowOffset;
	
	/**
	 * Default constructor.
	 * <p>
	 * This constructor should only be used by JAXB for
	 * marshalling and unmarshalling the objects.
	 */
	protected TextComponent() {
		this(Messages.getString("slide.component.unnamed"), 0, 0, 0, 0, "");
	}
	
	/**
	 * Minimal constructor.
	 * @param name the name of the component
	 * @param width the width in pixels
	 * @param height the height in pixels
	 */
	public TextComponent(String name, int width, int height) {
		this(name, 0, 0, width, height, null);
	}
	
	/**
	 * Optional constructor.
	 * @param name the name of the component
	 * @param x the x coordinate in pixels
	 * @param y the y coordinate in pixels
	 * @param width the width in pixels
	 * @param height the height in pixels
	 */
	public TextComponent(String name, int x, int y, int width, int height) {
		this(name, x, y, width, height, null);
	}

	/**
	 * Optional constructor.
	 * @param name the name of the component
	 * @param x the x coordinate in pixels
	 * @param y the y coordinate in pixels
	 * @param width the width in pixels
	 * @param height the height in pixels
	 * @param text the text
	 */
	public TextComponent(String name, int x, int y, int width, int height, String text) {
		super(name, x, y, width, height);
		this.text = text;
		this.textFill = new ColorFill(Color.WHITE);
		this.textFont = null;
		this.horizontalTextAlignment = HorizontalTextAlignment.CENTER;
		this.verticalTextAlignment = VerticalTextAlignment.TOP;
		this.textFontScaleType = FontScaleType.REDUCE_SIZE_ONLY;
		this.textWrapped = true;
		this.textPadding = 5;
		this.textVisible = true;
		this.textOutlineFill = new ColorFill(Color.BLACK);
		this.textOutlineStyle = new LineStyle(1.5f, CapType.ROUND, JoinType.ROUND, DashPattern.SOLID);
		this.textOutlineVisible = false;
		this.textShadowVisible = false;
		this.textShadowFill = new LinearGradientFill(
				LinearGradientDirection.TOP, 
				new Stop(0.0f, new Color(0, 0, 0, 255)),
				new Stop(0.5f, new Color(0, 0, 0, 153)),
				new Stop(1.0f, new Color(0, 0, 0, 50)));
		this.textShadowOffset = new Point(3, 3);
	}
	
	/**
	 * Copy constructor.
	 * <p>
	 * This constructor performs a deep copy where necessary.
	 * @param component the component to copy
	 */
	public TextComponent(TextComponent component) {
		super(component);
		this.text = component.text;
		this.textFill = component.textFill;
		this.textFont = component.textFont;
		this.horizontalTextAlignment = component.horizontalTextAlignment;
		this.verticalTextAlignment = component.verticalTextAlignment;
		this.textFontScaleType = component.textFontScaleType;
		this.textWrapped = component.textWrapped;
		this.textPadding = component.textPadding;
		this.textVisible = component.textVisible;
		this.textOutlineFill = component.textOutlineFill;
		this.textOutlineStyle = component.textOutlineStyle;
		this.textOutlineVisible = component.textOutlineVisible;
		this.textShadowVisible = component.textShadowVisible;
		this.textShadowFill = component.textShadowFill;
		this.textShadowOffset = component.textShadowOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.GenericSlideComponent#copy()
	 */
	@Override
	public TextComponent copy() {
		return new TextComponent(this);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.GenericComponent#isTransitionRequired(org.praisenter.slide.RenderableComponent)
	 */
	@Override
	public boolean isTransitionRequired(RenderableComponent component) {
		// this component shouldn't be used for backgrounds but can be because of the
		// extension from generic component.
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.GenericSlideComponent#render(java.awt.Graphics2D)
	 */
	@Override
	public void render(Graphics2D g) {
		// render the background
		if (this.backgroundVisible) {
			this.renderBackground(g, this.x, this.y);
		}
		// render the border
		if (this.borderVisible) {
			this.renderBorder(g);
		}
		// only render the text if its visible
		if (this.textVisible) {
			// render the text
			this.renderText(g, false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.slide.GenericSlideComponent#renderPreview(java.awt.Graphics2D)
	 */
	@Override
	public void renderPreview(Graphics2D g) {
		// render the background
		if (this.backgroundVisible) {
			this.renderBackground(g, this.x, this.y);
		}
		// render the border
		if (this.borderVisible) {
			this.renderBorder(g);
		}
		// only render the text if its visible
		if (this.textVisible) {
			// render the text
			this.renderText(g, true);
		}
	}
	
	/**
	 * Return the text used for rendering.
	 * <p>
	 * Override this method to provide custom formatting or other
	 * text processing before rendering.  This should return quickly
	 * so that the rendering speed is not affected.
	 * @return String
	 */
	protected String getTextToRender() {
		return this.text;
	}
	
	/**
	 * Renders the text to the given graphics object.
	 * @param g the graphics object to render to
	 * @param preview true if we are rending a preview
	 */
	protected void renderText(Graphics2D g, boolean preview) {
		// get the text
		String text = this.getTextToRender();
		if (text != null && text.length() > 0) {
			// compute the real width
			int rw = this.getTextWidth();
			int rh = this.getTextHeight();

			// set the font
			Font font = this.textFont;
			if (font == null) {
				// if the given font is null then use the default font
				font = FontManager.getDefaultFont();
			}
			
			// make sure the available width and height is greater
			// than zero before trying to fit the text
			if (rw > 0 && rh > 0) {
				// save the old font and color
				Font oFont = g.getFont();
				Paint oPaint = g.getPaint();
				Shape oClip = g.getClip();
				
				// make sure the line break characters are correct
				text = text.replaceAll("(\\r\\n)|(\\r)", String.valueOf(TextRenderer.LINE_SEPARATOR));
				// get the text metrics
				TextMetrics metrics = null;
				// check the font scaling method
				if (this.textFontScaleType == FontScaleType.REDUCE_SIZE_ONLY) {
					// check the wrap flag
					if (this.textWrapped) {
						// get a scaled font size to fit the width and height but is maxed at the current font size
						metrics = TextRenderer.getFittingParagraphMetrics(font, g.getFontRenderContext(), text, rw, rh);
					} else {
						// get a scaled font size to fit the entire line on one line
						metrics = TextRenderer.getFittingLineMetrics(font, g.getFontRenderContext(), text, rw, rh);
					}
				} else if (this.textFontScaleType == FontScaleType.BEST_FIT) {
					// check the wrap flag
					if (this.textWrapped) {
						// get a scaled font size to fit the width and height but is maxed at the current font size
						metrics = TextRenderer.getFittingParagraphMetrics(font, Float.MAX_VALUE, g.getFontRenderContext(), text, rw, rh);
					} else {
						// get a scaled font size to fit the entire line on one line
						metrics = TextRenderer.getFittingLineMetrics(font, Float.MAX_VALUE, g.getFontRenderContext(), text, rw, rh);
					}
				} else {
					// get the bounds without modifying the font size
					TextBounds bounds = null;
					if (this.textWrapped) {
						bounds = TextRenderer.getParagraphBounds(text, font, g.getFontRenderContext(), rw, rh);
					} else {
						bounds = TextRenderer.getLineBounds(font, g.getFontRenderContext(), text, rw, rh);
					}
					metrics = new TextMetrics(font.getSize2D(), bounds);
				}
				
				// see if we need to derive the font
				// the text renderer uses the font on the graphics object
				if (font.getSize2D() != metrics.fontSize) {
					g.setFont(font.deriveFont(metrics.fontSize));
				} else {
					g.setFont(font);
				}
				
				// apply the text padding
				float x = this.x + this.textPadding;
				float y = this.y + this.textPadding;
				
				// setup the text properties
				TextRenderProperties properties = new TextRenderProperties(metrics);
				properties.setX(x);
				properties.setY(y);
				properties.setVerticalAlignment(this.verticalTextAlignment);
				properties.setHorizontalAlignment(this.horizontalTextAlignment);
				properties.setTextFill(this.textFill);
				properties.setOutlineEnabled(this.textOutlineVisible);
				properties.setOutlineFill(this.textOutlineFill);
				properties.setOutlineStyle(this.textOutlineStyle);
				properties.setShadowEnabled(this.textShadowVisible);
				properties.setShadowFill(this.textShadowFill);
				properties.setShadowOffset(this.textShadowOffset);
				
				// save the old rendering hints
				RenderingHints oHints = g.getRenderingHints();
				
				// enable anti-aliasing to make the preview look decent
				if (this.textOutlineVisible && this.textOutlineFill != null && this.textOutlineStyle != null && preview) {
					// turn on anti-aliasing so that the text outlines don't look terrible
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
				
				// setup the clip region
				g.clipRect(this.x, this.y, this.width, this.height);
				if (this.textWrapped) {
					// render the text as a paragraph
					TextRenderer.renderParagraph(g, text, properties);
				} else {
					// render the text as a line
					TextRenderer.renderLine(g, text, properties);
				}
				
				g.setRenderingHints(oHints);
				g.setClip(oClip);
				g.setPaint(oPaint);
				g.setFont(oFont);
			}
		}
	}
	
	/**
	 * This returns the available width to render the text.
	 * @return int
	 */
	protected int getTextWidth() {
		return this.width - this.textPadding * 2 - 2;
	}
	
	/**
	 * This returns the available height to render the text.
	 * @return int
	 */
	protected int getTextHeight() {
		return this.height - this.textPadding * 2 - 2;
	}
	
	/**
	 * Returns a customized fill for the horizontal location of the text.
	 * @param fill the fill
	 * @param x the x location
	 * @param y the y location
	 * @param width the text width
	 * @param height the text height
	 * @return {@link Paint}
	 */
	protected Paint getPaint(Fill fill, float x, float y, float width, float height) {
		// make sure the fill is using the text metrics rather than the
		// text components bounds
		// we also need to take the horizontal alignment into consideration
		if (this.horizontalTextAlignment == HorizontalTextAlignment.RIGHT) {
			x = this.x + (this.width - this.textPadding - width);
		} else if (this.horizontalTextAlignment == HorizontalTextAlignment.CENTER) {
			x = this.x + (this.width / 2.0f) - width / 2.0f;
		}
		return fill.getPaint(
				(int)Math.floor(x), (int)Math.floor(y), 
				(int)Math.ceil(width), (int)Math.ceil(height));
	}
	
	/**
	 * Returns the text of this text component.
	 * @return String
	 */
	public String getText() {
		return this.text;
	}
	
	/**
	 * Sets the text of this component.
	 * @param text the text
	 */
	public void setText(String text) {
		this.text = text;
		if (this.text != null) {
			this.text = this.text.trim();
		}
	}
	
	/**
	 * Returns the text fill.
	 * @return {@link Fill}
	 */
	public Fill getTextFill() {
		return this.textFill;
	}
	
	/**
	 * Sets the text fill.
	 * @param fill the text fill
	 */
	public void setTextFill(Fill fill) {
		this.textFill = fill;
	}
	
	/**
	 * Returns the text font.
	 * @return Font
	 */
	public Font getTextFont() {
		return this.textFont;
	}
	
	/**
	 * Sets the text font.
	 * @param font the font
	 */
	public void setTextFont(Font font) {
		this.textFont = font;
	}
	
	/**
	 * Returns the horizontal text alignment.
	 * @return {@link HorizontalTextAlignment}
	 */
	public HorizontalTextAlignment getHorizontalTextAlignment() {
		return this.horizontalTextAlignment;
	}
	
	/**
	 * Sets the horizontal text alignment.
	 * @param alignment the alignment
	 */
	public void setHorizontalTextAlignment(HorizontalTextAlignment alignment) {
		this.horizontalTextAlignment = alignment;
	}

	/**
	 * Returns the vertical text alignment.
	 * @return {@link VerticalTextAlignment}
	 */
	public VerticalTextAlignment getVerticalTextAlignment() {
		return this.verticalTextAlignment;
	}
	
	/**
	 * Sets the vertical text alignment.
	 * @param alignment the alignment
	 */
	public void setVerticalTextAlignment(VerticalTextAlignment alignment) {
		this.verticalTextAlignment = alignment;
	}
	
	/**
	 * Returns the text padding.
	 * @return int
	 */
	public int getTextPadding() {
		return this.textPadding;
	}
	
	/**
	 * Sets the text padding.
	 * @param padding the padding
	 */
	public void setTextPadding(int padding) {
		this.textPadding = padding;
	}
	
	/**
	 * Returns the font scale type.
	 * @return {@link FontScaleType}
	 */
	public FontScaleType getTextFontScaleType() {
		return this.textFontScaleType;
	}
	
	/**
	 * Sets the font scale type.
	 * @param fontScaleType the font scale type
	 */
	public void setTextFontScaleType(FontScaleType fontScaleType) {
		this.textFontScaleType = fontScaleType;
	}
	
	/**
	 * Returns true if this text wraps.
	 * @return boolean
	 */
	public boolean isTextWrapped() {
		return this.textWrapped;
	}
	
	/**
	 * Sets the text to wrap inside this component.
	 * @param flag true to wrap the text
	 */
	public void setTextWrapped(boolean flag) {
		this.textWrapped = flag;
	}

	/**
	 * Returns true if the text is visible.
	 * @return boolean
	 */
	public boolean isTextVisible() {
		return this.textVisible;
	}

	/**
	 * Toggles the visibility of the text.
	 * @param visible true if the text should be visible
	 */
	public void setTextVisible(boolean visible) {
		this.textVisible = visible;
	}

	/**
	 * Returns true if the text outline is visible.
	 * @return boolean
	 */
	public boolean isTextOutlineVisible() {
		return this.textOutlineVisible;
	}

	/**
	 * Toggles the visibility of the text outline.
	 * @param flag true if the text outline should be visible
	 */
	public void setTextOutlineVisible(boolean flag) {
		this.textOutlineVisible = flag;
	}

	/**
	 * Returns the text outline style.
	 * @return {@link LineStyle}
	 */
	public LineStyle getTextOutlineStyle() {
		return this.textOutlineStyle;
	}

	/**
	 * Set the text outline style.
	 * @param lineStyle the text outline style
	 */
	public void setTextOutlineStyle(LineStyle lineStyle) {
		this.textOutlineStyle = lineStyle;
	}

	/**
	 * Returns the text outline fill.
	 * @return {@link Fill}
	 */
	public Fill getTextOutlineFill() {
		return this.textOutlineFill;
	}

	/**
	 * Sets the text outline fill.
	 * @param fill the text outline fill
	 */
	public void setTextOutlineFill(Fill fill) {
		this.textOutlineFill = fill;
	}

	/**
	 * Returns true if the text shadow is visible.
	 * @return boolean
	 * @since 2.0.2
	 */
	public boolean isTextShadowVisible() {
		return this.textShadowVisible;
	}

	/**
	 * Toggles the visibility of the text shadow.
	 * @param flag true if the text shadow should be visible
	 * @since 2.0.2
	 */
	public void setTextShadowVisible(boolean flag) {
		this.textShadowVisible = flag;
	}

	/**
	 * Returns the text shadow fill.
	 * @return {@link Fill}
	 * @since 2.0.2
	 */
	public Fill getTextShadowFill() {
		return this.textShadowFill;
	}

	/**
	 * Sets the text shadow fill.
	 * @param fill the text shadow fill
	 * @since 2.0.2
	 */
	public void setTextShadowFill(Fill fill) {
		this.textShadowFill = fill;
	}

	/**
	 * Returns the text shadow offset.
	 * @return {@link Point}
	 * @since 2.0.2
	 */
	public Point getTextShadowOffset() {
		return this.textShadowOffset;
	}

	/**
	 * Sets the text shadow offset.
	 * @param offset the text shadow offset
	 * @since 2.0.2
	 */
	public void setTextShadowOffset(Point offset) {
		this.textShadowOffset = offset;
	}
}
