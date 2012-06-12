package be.wide.controller;

import org.OpenNI.*;
import com.primesense.NITE.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class GestureController 
{
	private Context context;
	private SessionManager sm;
	private SwipeDetector sd;
	private WaveDetector wd;
	private CircleDetector cd;
	private PushDetector pd;
	private SteadyDetector std;
	private FlowRouter fr;
	private float totalTime = 0;

	/**
	 * Standard constructor. 
	 * Creates a new GestureController object.
	 * @param context OpenNI context for NITE initalization
	 */
	public GestureController(Context context)
	{
		this.context = context;
		config();
	}

	/**
	 * Configures the different gesture detectors.
	 * Only Swipe (left and right) active at the moment.
	 */
	public void config()
	{
		System.out.println("Gestures/NITE starting..");
		try {
			sm = new SessionManager(context, "Wave");
			sm.getSessionStartEvent().addObserver(new SessionStartObserver());
			sm.getSessionEndEvent().addObserver(new SessionEndObserver());
			
			fr = new FlowRouter();
			
			// SWIPE DETECTOR
			sd = new SwipeDetector();
			//sd.getGeneralSwipeEvent().addObserver(new GeneralSwipeObserver()); //General Swipe Event
			//sd.getSwipeLeftEvent().addObserver(new SwipeLeftObserver());
			//sd.getSwipeRightEvent().addObserver(new SwipeRightObserver());
			//sd.getSwipeUpEvent().addObserver(new SwipeUpObserver());
			//sd.getSwipeDownEvent().addObserver(new SwipeDownObserver());
			sm.addListener(sd);

			// WAVE DETECTOR
			wd = new WaveDetector();
			wd.SetMaxDeviation(100);
			//wd.getWaveEvent().addObserver(new WaveObserver());
			//sm.addListener(wd);

			// CIRCLE DETECTOR
			cd = new CircleDetector();
			//cd.getCircleEvent().addObserver(new CircleObserver());
			//sm.addListener(cd);

			// PUSH DETECTOR
			pd = new PushDetector();
			//pd.getPushEvent().addObserver(new PushObserver());
			sm.addListener(pd);
			
			// STEADY DETECTOR
			std = new SteadyDetector();
			//std.getSteadyEvent().addObserver(new SteadyObserver());
			sm.addListener(std);
			
			System.out.println("--Gestures/NITE initialized");
			
		} catch (GeneralException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the NITE swipe detector.
	 * @return SwipeDetector
	 */
	public SwipeDetector getSwipeDetector() {
		return sd;
	}

	/**
	 * Gets the NITE wave detector.
	 * @return WaveDetector
	 */
	public WaveDetector getWaveDetector() {
		return wd;
	}

	/**
	 * Gets the NITE circle detector.
	 * @return CircleDetector
	 */
	public CircleDetector getCircleDetector() {
		return cd;
	}

	/**
	 * Gets the NITE push detector.
	 * @return PushDetector
	 */
	public PushDetector getPushDetector() {
		return pd;
	}

	/**
	 * Gets the NITE steady detector.
	 * @return SteadyDetector
	 */
	public SteadyDetector getSteadyDetector() {
		return std;
	}


	/**
	 * Gets the NITE flowrouter.
	 * @return FlowRouter
	 */
	public FlowRouter getFlowRouter() {
		return fr;
	}

	/**
	 * Start the sessionmanager.
	 * Force session if no session in progress.
	 */
	public void startSessionManager()
	{
		try {
			if (!sm.IsInSession() && KinectController.getInstance().isTracking())
			{
				sm.ForceSession(KinectController.getInstance().getRightHandPositionReal());
			}
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update the session with the OpenNI context.
	 * @param context OpenNI context
	 */
	public void updateSessionManager(Context context)
	{
		try {
			float startTime = System.currentTimeMillis();

			startSessionManager();
			sm.update(context);
			
			if (KinectController.getInstance().getRightHandPositionReal() != null)
			{
				sm.TrackPoint(KinectController.getInstance().getRightHandPositionReal());
			}
			
			setTotalTime(System.currentTimeMillis() - startTime);
			//System.out.println("gesture: " + totalTime + "ms");
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	public void setTotalTime(float totalTime) {
		this.totalTime = totalTime;
	}

	public float getTotalTime() {
		return totalTime;
	}

/*	*//**
	 * Called when a left swipe is detected.
	 * Prints the gesture to console.
	 *//*
	public class SwipeLeftObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0,
				VelocityAngleEventArgs arg1) {
			System.out.println("gesture: left swipe");
		}
	}

	*//**
	 * Called when right swipe is detected.
	 * Prints the gesture to console.
	 *//*
	public class SwipeRightObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0,
				VelocityAngleEventArgs arg1) {
			System.out.println("gesture: right swipe");
		}
	}
	
	*//**
	 * Called when swipe up is detected.
	 * Prints the gesture to console.
	 *//*
	class SwipeUpObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0,
				VelocityAngleEventArgs arg1) {
			System.out.println("gesture: swipe up");
		}
	}
	
	*//**
	 * Called when swipe down is detected.
	 * Prints the gesture to console.
	 *//*
	class SwipeDownObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0,
				VelocityAngleEventArgs arg1) {
			System.out.println("gesture: swipe down");
		}
	}

	*//**
	 * Called when wave gesture is detected.
	 * Prints the gesture to console.
	 *//*
	public class WaveObserver implements IObserver<NullEventArgs>
	{
		public void update(IObservable<NullEventArgs> arg0, NullEventArgs arg1) {
			System.out.println("gesture: wave");
		}
	}

	*//**
	 * Called when circle gesture is detected.
	 * Prints the gesture to console.
	 *//*
	public class CircleObserver implements IObserver<CircleEventArgs>
	{
		public void update(IObservable<CircleEventArgs> arg0,
				CircleEventArgs arg1) {
			System.out.println("gesture: circle");
		}
	}

	*//**
	 * Called when push/click gesture is detected.
	 * Prints the gesture to console.
	 *//*
	public class PushObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0,
				VelocityAngleEventArgs arg1) {
			System.out.println("gesture: push");
		}
	}
	
	*//**
	 * Called when steady gesture is detected.
	 * Prints the gesture to console.
	 *//*
	public class SteadyObserver implements IObserver<IdValueEventArgs>
	{
		public void update(IObservable<IdValueEventArgs> arg0,
				IdValueEventArgs arg1) {
			System.out.println("gesture: steady");
		}
	}
	
	*//**
	 * Called when any swipe is detected.
	 * Prints the gesture to console.
	 *//*
	public class GeneralSwipeObserver implements IObserver<DirectionVelocityAngleEventArgs>
	{
		public void update(IObservable<DirectionVelocityAngleEventArgs> arg0,
				DirectionVelocityAngleEventArgs arg1) {
			System.out.println("gesture: General Swipe");
		}
	}*/
	
	/**
	 * Called when the session is started.
	 * Prints status to console.
	 */
	class SessionStartObserver implements IObserver<PointEventArgs>
	{
		public void update(IObservable<PointEventArgs> arg0, PointEventArgs arg1) {
			System.out.println("-session started");
		}
		
	}
	
	/**
	 * Called when the session is ended.
	 * Prints status to console.
	 */
	class SessionEndObserver implements IObserver<NullEventArgs>
	{
		public void update(IObservable<NullEventArgs> arg0, NullEventArgs arg1) {
			System.out.println("-session ended");
		}
	}
}
