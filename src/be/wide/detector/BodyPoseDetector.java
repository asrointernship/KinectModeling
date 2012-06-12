package be.wide.detector;

import java.util.*;
import org.OpenNI.*;
import be.wide.controller.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class BodyPoseDetector 
{
	// Kinect variables
	private KinectController kinect;

	// BodyPose variables
	private ArrayList<BodyPoseRule> rules;
	private String name;

	/**
	 * Standard constructor.
	 * @param name name of the pose
	 */
	public BodyPoseDetector(String name)
	{
		initialize(name);
	}

	/**
	 * Initializes the BodyPoseDetector object.
	 * @param name nme of the pose
	 */
	private void initialize(String name)
	{
		kinect = KinectController.getInstance();
		setName(name);
		rules = new ArrayList<BodyPoseRule>();
	}

	/**
	 * Adds a BodyPoseRule to the list of rules.
	 * @param from skeleton joint
	 * @param relation relation between joints
	 * @param to skeleton joint
	 */
	public void addRule(SkeletonJoint from, int relation, SkeletonJoint to)
	{
		BodyPoseRule rule = new BodyPoseRule(from, relation, to);
		rules.add(rule);
	}
	
	/**
	 * Adds a BodyPoseRule to the list of rules.
	 * @param from skeleton joint
	 * @param relation relation between joints
	 * @param dist distance between joints
	 * @param to skeleton joint
	 */
	public void addRule(SkeletonJoint from, int relation, int dist, SkeletonJoint to)
	{
		BodyPoseRule rule = new BodyPoseRule(from, relation, dist, to);
		rules.add(rule);
	}

	/**
	 * Checks all the rules to see if pose is detected.
	 * @return true if pose detected, false otherwise
	 */
	public boolean check()
	{
		if (kinect.isTracking())
		{
			boolean result = true;
			for (int i = 0; i < rules.size(); ++i)
			{
				BodyPoseRule rule = rules.get(i);
				result = result && rule.check();
			}
			return result;
		}
		return false;
	}

	/**
	 * Sets the name of the pose.
	 * @param name name of the pose
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the pose
	 * @return name of the pose
	 */
	public String getName() {
		return name;
	}
}
