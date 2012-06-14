package be.wide.dom;

import org.OpenNI.Point3D;

/**
 *
 * @author Maarten Taeymans
 *
 */
public class Hand 
{
	private String name;
	private int numberOfFingers;
	private boolean isFist;
	private Point3D position;
	private long myPrevTimer;
	private long minDuration = 2000;

	/**
	 * Standard constructor.
	 * Creates a hand object with the specified name.
	 * @param name name of hand object
	 */
	public Hand(String name)
	{
		setName(name);
	}

	/**
	 * Gets the name of the hand object.
	 * @return name of object
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the hand object.
	 * @param name name of object
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the number of fingers detected for the hand.
	 * @return number of fingers
	 */
	public int getNumberOfFingers() {
		return numberOfFingers;
	}
	
	/**
	 * Sets the number of fingers detected for the hand.
	 * @param numberOfFingers number of fingers
	 */
	public void setNumberOfFingers(int numberOfFingers) {
		this.numberOfFingers = numberOfFingers;
	}
	
	/**
	 * Returns if a fist is made with this hand.
	 * @return true: fist;
	 * 			false: no fist
	 */
	public boolean isFist() {
		return isFist;
	}

	/**
	 * Sets if a fist is made with the hand.
	 * Can only updated after a certain duration, set by minDuration.
	 * @param isFist
	 */
	public void setFist(boolean isFist) {

		long currTime = System.currentTimeMillis();

		if (currTime > myPrevTimer + minDuration)
		{
			this.isFist = isFist;
			myPrevTimer = currTime;
		}

	}

	/**
	 * Sets the position of the hand object.
	 * @param position position of the hand
	 */
	public void setPosition(Point3D position) {
		this.position = position;
	}

	/**
	 * Gets the position of the hand.
	 * @return position of the hand
	 */
	public Point3D getPosition() {
		return position;
	}

	/**
	 * Gets the minimum duration to wait before isFist-boolean can be changed.
	 * @return minimum duration in ms
	 */
	public long getMinDuration() {
		return minDuration;
	}

	/**
	 * Sets the minimum duration to wait before isFist-boolean can be changed.
	 * @param minDuration minimum duration in ms
	 */
	public void setMinDuration(long minDuration) {
		this.minDuration = minDuration;
	}
}
