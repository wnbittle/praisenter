package org.praisenter.transitions.easing;

/**
 * Circular easing from http://gizma.com/easing/.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class CircularEasing implements Easing {
	/** The id for the easing */
	public static final int ID = 80;
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Easing#easeIn(long, long)
	 */
	@Override
	public double easeIn(long time, long duration) {
		double t = (double)time / (double)duration;
		return -(Math.sqrt(1.0 - t * t) - 1);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Easing#easeOut(long, long)
	 */
	@Override
	public double easeOut(long time, long duration) {
		double t = (double)time / (double)duration;
		t -= 1.0;
		return Math.sqrt(1.0 - t * t);
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.Easing#easeInOut(long, long)
	 */
	@Override
	public double easeInOut(long time, long duration) {
		double t = (double)time / ((double)duration * 0.5);
		if (t < 1.0) {
			return -(Math.sqrt(1.0 - t * t) - 1) * 0.5;
		}
		t -= 2.0;
		return (Math.sqrt(1.0 - t * t) + 1) * 0.5;
	}
	
	/* (non-Javadoc)
	 * @see org.praisenter.transitions.easing.Easing#getEasingId()
	 */
	@Override
	public int getEasingId() {
		return ID;
	}
}