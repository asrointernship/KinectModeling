package be.wide.detector;

import org.OpenNI.*;
import be.wide.controller.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class BodyPoseRule 
{
	private KinectController kinect;
	private SkeletonJoint fromJoint;
	private SkeletonJoint toJoint;
	private Point3D fromJointVector;
	private Point3D toJointVector;
	private boolean succes;
	private int jointRelation;
	private int minDist;
	
	public static final int ABOVE = 1;
	public static final int BELOW = 2;
	public static final int LEFT_OF = 3;
	public static final int RIGHT_OF = 4;
	public static final int IN_FRONT_OF = 5;
	public static final int BEHIND = 6;
	public static final int DISTANCE = 7;

	/**
	 * Standard constructor.
	 * Creates a new BodyPoseRule object.
	 * @param from skeleton joint
	 * @param relation relation between joints
	 * @param to skeleton joint
	 */
	public BodyPoseRule(SkeletonJoint from, int relation, SkeletonJoint to)
	{
		setFromJoint(from);
		setToJoint(to);
		setJointRelation(relation);
		kinect = KinectController.getInstance();

		fromJointVector = new Point3D();
		toJointVector = new Point3D();
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a new BodyPoseRule object.
	 * @param from skeleton joint
	 * @param relation relation between joints
	 * @param dist distance between joints
	 * @param to skeleton joint
	 */
	public BodyPoseRule(SkeletonJoint from, int relation, int dist, SkeletonJoint to)
	{
		setFromJoint(from);
		setToJoint(to);
		setJointRelation(relation);
		setMinDist(dist);
		kinect = KinectController.getInstance();

		fromJointVector = new Point3D();
		toJointVector = new Point3D();
	}

	/**
	 * Checks if a the BodyPoseRule is achieved.
	 * @return true if achieved, false otherwise
	 */
	public boolean check()
	{
		boolean result = false;

		try {
			fromJointVector = kinect.getSkeletonJointPosition(fromJoint);
			toJointVector = kinect.getSkeletonJointPosition(toJoint);

			switch(jointRelation)
			{
			case ABOVE:
				result = (fromJointVector.getY() > toJointVector.getY());
				break;
			case BELOW:
				result = (fromJointVector.getY() < toJointVector.getY());
				break;
			case LEFT_OF:
				result = (fromJointVector.getX() < toJointVector.getX());
				break;
			case RIGHT_OF:
				result = (fromJointVector.getX() > toJointVector.getX());
				break;
			case IN_FRONT_OF:
				result = (fromJointVector.getZ() < toJointVector.getZ());
				break;
			case BEHIND:
				result = (fromJointVector.getZ() > toJointVector.getZ());
				break;
			case DISTANCE:
				float dist = (float) Math.sqrt(Math.pow((fromJointVector.getX() - toJointVector.getY()), 2)
											+ Math.pow(fromJointVector.getY() - toJointVector.getY(), 2)
											+ Math.pow(fromJointVector.getZ() - toJointVector.getZ(), 2));
				result = (dist > minDist);
				break;
			}

		} catch (NullPointerException e) {
			//e.printStackTrace();
		}
		catch (IndexOutOfBoundsException ex) {
			//ex.printStackTrace();
		}
		
		setSucces(result);
		return result;
	}
	
	/**
	 * Gets the from skeleton joint.
	 * @return skeleton joint
	 */
	public SkeletonJoint getFromJoint() {
		return fromJoint;
	}

	/**
	 * Sets the from skeleton joint.
	 * @param fromJoint skeleton joint
	 */
	private void setFromJoint(SkeletonJoint fromJoint) {
		this.fromJoint = fromJoint;
	}

	/**
	 * Gets the to skeleton joint.
	 * @return skeleton joint
	 */
	public SkeletonJoint getToJoint() {
		return toJoint;
	}

	/**
	 * Sets the to skeleton joint.
	 * @param toJoint skeleton joint
	 */
	private void setToJoint(SkeletonJoint toJoint) {
		this.toJoint = toJoint;
	}

	/**
	 * Gets the joints relation.
	 * @return relation between joints
	 */
	public int getJointRelation() {
		return jointRelation;
	}

	/**
	 * Sets the joint relation.
	 * @param jointRelation relation between joints
	 */
	private void setJointRelation(int jointRelation) {
		this.jointRelation = jointRelation;
	}

	/**
	 * Sets the succes boolean.
	 * @param succes true when poserule detected, false otherwise
	 */
	private void setSucces(boolean succes) {
		this.succes = succes;
	}

	/**
	 * Gets the succes boolean.
	 * @return true when poserule detected, false otherwise
	 */
	public boolean isSucces() {
		return succes;
	}

	/**
	 * Gets the minimum distance between joints.
	 * @return distance
	 */
	public int getMinDist() {
		return minDist;
	}

	/**
	 * Sets the minimum distance between joints.
	 * @param minDist distance
	 */
	public void setMinDist(int minDist) {
		this.minDist = minDist;
	}
}

