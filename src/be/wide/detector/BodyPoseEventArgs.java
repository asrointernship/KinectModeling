package be.wide.detector;

import org.OpenNI.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class BodyPoseEventArgs extends EventArgs
{
	private BodyPoseDetector pose;
	private long time;
	
	/**
	 * Standard constructor.
	 * @param pose detected pose
	 * @param time time of detection
	 */
	public BodyPoseEventArgs(BodyPoseDetector pose, long time)
	{
		setPose(pose);
		setTime(time);
	}

	/**
	 * Gets the detected pose.
	 * @return detected pose
	 */
	public BodyPoseDetector getPose() {
		return pose;
	}

	/**
	 * Sets the detected pose.
	 * @param pose detected pose
	 */
	public void setPose(BodyPoseDetector pose) {
		this.pose = pose;
	}

	/**
	 * Gets the time of detection.
	 * @return time of detection
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the time of detection.
	 * @param time time of detection
	 */
	public void setTime(long time) {
		this.time = time;
	}
}
