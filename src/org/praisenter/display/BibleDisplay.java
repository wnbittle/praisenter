package org.praisenter.display;

import java.awt.Dimension;
import java.awt.Graphics2D;

import org.praisenter.data.bible.Verse;

/**
 * Represents a display for showing bible verses.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class BibleDisplay extends Display {
	/** Additional reference to the title component */
	protected TextComponent scriptureTitleComponent;
	
	/** Additional reference to the text component */
	protected TextComponent scriptureTextComponent;
	
	/**
	 * Minimal constructor.
	 * @param displaySize the target display size
	 */
	public BibleDisplay(Dimension displaySize) {
		super(displaySize);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.display.Display#render(java.awt.Graphics2D)
	 */
	@Override
	public void render(Graphics2D graphics) {
		// render the backgrounds
		super.render(graphics);
		// render the text components
		this.scriptureTitleComponent.render(graphics);
		this.scriptureTextComponent.render(graphics);
	}
	
	/**
	 * Convenience method for setting the texts for the given bible verse.
	 * @param verse the verse
	 */
	public void setVerse(Verse verse) {
		this.scriptureTitleComponent.setText(verse.getBook().getName() + " " + verse.getChapter() + ":" + verse.getVerse());
		this.scriptureTextComponent.setText(verse.getText());
	}
	
	/**
	 * Convenience method for clearing the text of the display.
	 */
	public void clearVerse() {
		this.scriptureTitleComponent.setText("");
		this.scriptureTextComponent.setText("");
	}
	
	/**
	 * Returns the {@link TextComponent} for the scripture title.
	 * @return {@link TextComponent}
	 */
	public TextComponent getScriptureTitleComponent() {
		return this.scriptureTitleComponent;
	}

	/**
	 * Sets the {@link TextComponent} for the scripture title.
	 * @param titleComponent the scripture title component
	 */
	public void setScriptureTitleComponent(TextComponent titleComponent) {
		this.scriptureTitleComponent = titleComponent;
	}
	
	/**
	 * Returns the {@link TextComponent} for the scripture text.
	 * @return {@link TextComponent}
	 */
	public TextComponent getScriptureTextComponent() {
		return this.scriptureTextComponent;
	}
	
	/**
	 * Sets the {@link TextComponent} for the scripture text.
	 * @param textComponent the scripture text component
	 */
	public void setScriptureTextComponent(TextComponent textComponent) {
		this.scriptureTextComponent = textComponent;
	}
}
