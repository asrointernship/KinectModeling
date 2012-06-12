package be.wide.detector;

import org.OpenNI.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class HandDetectionEventArgs extends EventArgs
{

	private IObservable<HandDetectionEventArgs> parent;
	private boolean isFist = false;
	private String hand;
	
	/**
	 * Standard constructor.
	 * @param par parent observable
	 * @param isFist true if fist is made, false otherwise
	 * @param side hand (left/right)
	 */
	public HandDetectionEventArgs(IObservable<HandDetectionEventArgs> par, boolean isFist, String side)
	{
		setParent(par);
		setFist(isFist);
		setHandSide(side);
	}

	/**
	 * Gets the observable parent.
	 * @return observable parent
	 */
	public IObservable<HandDetectionEventArgs> getParent() {
		return parent;
	}

	/**
	 * Sets the observable parent.
	 * @param parent observable parent
	 */
	public void setParent(IObservable<HandDetectionEventArgs> parent) {
		this.parent = parent;
	}

	/**
	 * Sees if fist is made.
	 * @return true if fist is made, false otherwise
	 */
	public boolean isFist() {
		return isFist;
	}

	/**
	 * Sets if a fist is made.
	 * @param isFist true if fist is made, false otherwise
	 */
	public void setFist(boolean isFist) {
		this.isFist = isFist;
	}

	/**
	 * Gets which hand was checked.
	 * @return hand as string
	 */
	public String getHand() {
		return hand;
	}

	/**
	 * Sets which hand was checked.
	 * @param side hand as string
	 */
	public void setHandSide(String side) {
		this.hand = side;
	}
}
