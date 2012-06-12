package be.wide.detector;

import java.util.*;
import org.OpenNI.*;

/**
 * 
 * @author Maarten.Taeymans
 *
 */
public class BodyPoseHandler implements Runnable, IObservable<BodyPoseEventArgs>
{
	// Unique instance for Singleton
	private volatile static BodyPoseHandler uniqueInstance;
	
	// Pose variables
	private ArrayList<BodyPoseDetector> poses;
	private volatile boolean isRunning;
	private Thread myThread;
	private ArrayList<IObserver<BodyPoseEventArgs>> observers;
	
	/**
	 * Standard private constructor.
	 */
	private BodyPoseHandler()
	{
		initialize();
	}
	
	/**
	 * Returns an instance of the BodyPoseHandler.
	 * If no instance exists, creates a new one.
	 * @return BodyPoseHandler instance
	 */
	public static BodyPoseHandler getInstance()
	{
		if (uniqueInstance == null)
		{
			synchronized(BodyPoseHandler.class)
			{
				if (uniqueInstance == null)
				{
					uniqueInstance = new BodyPoseHandler();
				}
			}
		}
		return uniqueInstance;
	}
	
	/**
	 * Initializes the BodyPoseHandler object.
	 */
	private void initialize()
	{
		poses = new ArrayList<BodyPoseDetector>();
		observers = new ArrayList<IObserver<BodyPoseEventArgs>>();
		myThread = new Thread(this, "BodyPoseHandlerThread");
		myThread.start();
		isRunning = true;
		System.out.println("--BodyPoseHandler initialized");
	}
	
	/**
	 * Main loop method of the runnable interface.
	 */
	public void run()
	{
		while(isRunning)
		{
			for (BodyPoseDetector bpd : poses)
			{					
				if (checkPose(bpd))
				{
					BodyPoseEventArgs tempArgs = new BodyPoseEventArgs(bpd, System.currentTimeMillis());
					for (IObserver<BodyPoseEventArgs> obs : observers)
					{
						obs.update(this, tempArgs);
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds a pose to the pose list.
	 * @param pose pose to be added
	 */
	public void addPose(BodyPoseDetector pose)
	{
		if (pose == null) throw new IllegalArgumentException("Pose cannot be null.");
		poses.add(pose);
	}
	
	/**
	 * Check if a pose is detected.
	 * @param pose pose to be checked
	 * @return true if pose detected, false otherwise
	 */
	private boolean checkPose(BodyPoseDetector pose)
	{
		if (pose.check())
		{
			return true;
		}
		return false;
	}

	/**
	 * Adds an observer to the list of observers.
	 * Registers an observer to receive updates from this class.
	 */
	@Override
	public void addObserver(IObserver<BodyPoseEventArgs> obs) throws StatusException {
		observers.add(obs);
	}

	/**
	 * Removes an observer from the list of observers.
	 * Unregister an observer to receive updates from this class
	 */
	@Override
	public void deleteObserver(IObserver<BodyPoseEventArgs> obs) {
		observers.remove(obs);
	}
}
