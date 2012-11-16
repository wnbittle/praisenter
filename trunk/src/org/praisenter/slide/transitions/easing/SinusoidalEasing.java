package org.praisenter.slide.transitions.easing;

import org.praisenter.resources.Messages;

/**
 * Sinusoidal easing from http://gizma.com/easing/.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class SinusoidalEasing extends AbstractEasing {
	/** The id for the easing */
	public static final int ID = 60;

	/**
	 * Default constructor.
	 */
	public SinusoidalEasing() {
		super(Messages.getString("easing.sinusoidal"));
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Easing#easeIn(long, long)
	 */
	@Override
	public double easeIn(long time, long duration) {
		double t = (double)time / (double)duration;
		return -Math.cos(t * Math.PI * 0.5) + 1.0;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Easing#easeOut(long, long)
	 */
	@Override
	public double easeOut(long time, long duration) {
		double t = (double)time / (double)duration;
		return Math.sin(t * Math.PI * 0.5);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Easing#easeInOut(long, long)
	 */
	@Override
	public double easeInOut(long time, long duration) {
		double t = (double)time / ((double)duration * 0.5);
		return -(Math.cos(t * Math.PI) - 1) * 0.5;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.easing.Easing#getEasingId()
	 */
	@Override
	public int getEasingId() {
		return ID;
	}
}