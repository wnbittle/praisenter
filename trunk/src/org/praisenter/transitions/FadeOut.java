package org.praisenter.transitions;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.praisenter.resources.Messages;

/**
 * Represents a fade-out {@link Transition}.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class FadeOut extends Transition {
	/**
	 * Default constructor.
	 */
	public FadeOut() {
		super(Messages.getString("transition.fadeOut"), Type.OUT);
	}

	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Transition#getTransitionId()
	 */
	@Override
	public int getTransitionId() {
		return 21;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Transition#render(java.awt.Graphics2D, java.awt.image.BufferedImage, java.awt.image.BufferedImage, double)
	 */
	@Override
	public void render(Graphics2D g2d, BufferedImage image0, BufferedImage image1, double pc) {
		// apply alpha composite
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)Math.max(1.0 - pc, 0.0));
		Composite composite = g2d.getComposite();
		g2d.setComposite(ac);
		g2d.drawImage(image0, 0, 0, null);
		g2d.setComposite(composite);
	}
}