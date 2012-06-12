package be.wide.controller;

import java.awt.image.*;
import java.nio.*;
import java.util.*;
import org.OpenNI.*;
import com.primesense.NITE.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
@SuppressWarnings("unused")
public class KinectController implements Runnable {

	/**
	 * Unique instance of KinectController class
	 */
	private volatile static KinectController uniqueInstance;

	private static final int MAX_DEPTH_SIZE = 10000;  
	private float histogram[];
	private int maxDepth = 0;
	private byte[] imgbytes;
	private BufferedImage cameraImage = null;
	private BufferedImage depthImage = null;
	private int imWidth = 640;
	private int imHeight = 480;

	private volatile boolean isRunning;
	private long totalTime = 0;
	private Thread myThread;

	private Point3D leftHandPosition;
	private Point3D rightHandPosition;

	// OpenNI
	private Context context;
	private OutArg<ScriptNode> scriptNode;
	private DepthMetaData depthMD;
	private ImageGenerator imageGen;
	private DepthGenerator depthGen;
	private UserGenerator userGen;
	private SkeletonCapability skelCap;
	private PoseDetectionCapability poseDetectionCap;
	private String calibPoseName = null;
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkels;
	private final String XML_FILE = "../KinectConfig.xml";
	private final String SKEL_FILE = "../SkeletonData.xml";
	private boolean isTracking = false;
	private boolean toggleGesture = true;

	// NITE
	private GestureController gestureCont;

	private KinectController()
	{
		configOpenNI();
	}

	/**
	 * Returns the instance of this class. If no instance existed, one is created.
	 * @return KinectController instance
	 */
	public static KinectController getInstance()
	{
		if (uniqueInstance == null)
		{
			synchronized(KinectController.class)
			{
				if (uniqueInstance == null)
				{
					uniqueInstance = new KinectController();
				}
			}
		}
		return uniqueInstance;
	}

	/**
	 * Configures OpenNI.
	 * Creates different nodes for depth, image, users and gestures.
	 */
	private void configOpenNI()
	{
		System.out.println("Kinect/OpenNI starting..");
		userSkels = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();

		try {
			scriptNode = new OutArg<ScriptNode>();
			//System.out.println(XML_FILE);
			context = Context.createFromXmlFile(XML_FILE, scriptNode);

			depthGen = DepthGenerator.create(context);
			imageGen = ImageGenerator.create(context);
			userGen = UserGenerator.create(context);
			
			gestureCont = new GestureController(context);

			// if possible, set the viewpoint of the DepthGenerator to match the ImageGenerator
			boolean hasAltView = depthGen.isCapabilitySupported("AlternativeViewPoint");
			System.out.println("Alternative ViewPoint supported: " + hasAltView); 

			poseDetectionCap = userGen.getPoseDetectionCapability();

			skelCap = userGen.getSkeletonCapability();
			calibPoseName = skelCap.getSkeletonCalibrationPose();  // the 'psi' pose
			skelCap.setSkeletonProfile(SkeletonProfile.ALL);

			// set up four observers
			userGen.getNewUserEvent().addObserver(new NewUserObserver());
			userGen.getLostUserEvent().addObserver(new LostUserObserver());

			poseDetectionCap.getPoseDetectedEvent().addObserver(
					new PoseDetectedObserver());  

			skelCap.getCalibrationCompleteEvent().addObserver(
					new CalibrationCompleteObserver());

			depthMD = depthGen.getMetaData();

			setLeftHandPosition(new Point3D());
			setRightHandPosition(new Point3D());

			histogram = new float[MAX_DEPTH_SIZE];

			imgbytes = new byte[imWidth * imHeight];
			depthImage = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_BYTE_GRAY);

			context.startGeneratingAll(); 
			System.out.println("Started context generating..."); 

			myThread = new Thread(this, "KinectThread");
			myThread.start();
			isRunning = true;	
		} 
		catch (Exception e) {
			System.out.println(e);
			System.out.println("Did you connect a kinect?");
			System.exit(1);
		}
		System.out.println("--Kinect/OpenNI initialized");
	} 

	/**
	 * Program loop.
	 * Updates depthimage and skeleton-data.
	 */
	public void run() 
	{
		while (isRunning) {
			long startTime = System.currentTimeMillis();

			try {
				//context.waitAnyUpdateAll();
				context.waitAndUpdateAll();  // wait for all nodes to have new data, then updates them
				
				if (toggleGesture) 
				{
					gestureCont.updateSessionManager(context);
				}

			} catch (StatusException e) {  
				System.out.println(e); 
			}


			//updateImage();
			//updateDepthImage();
			updateSkeleton();

			setTotalTime((System.currentTimeMillis() - startTime));

			//System.out.println("kinect: " + totalTime + "ms");

			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}

		// close down
		try {
			context.stopGeneratingAll();
		}
		catch (StatusException e) {}

		context.release();
	}

	/**
	 * Recreates depthimage with the current depth values.
	 * Colors depend on histogram, which is updated at start of this method.
	 */
	private void updateDepthImage()
	{
		ShortBuffer depthBuf = depthMD.getData().createShortBuffer();
		// update histogram
		calcHistogram(depthBuf);
		depthBuf.rewind();

		while (depthBuf.remaining() > 0) {
			int pos = depthBuf.position();
			short depth = depthBuf.get();
			imgbytes[pos] = (byte) histogram[depth];
			//imgbytes[pos] = (byte) OpenCVHandler.mapTo(depth, 0, 4000, 255, 0); // more efficient?
		}

		DataBufferByte dataBuffer = new DataBufferByte(imgbytes, imWidth * imHeight);
		Raster raster = Raster.createPackedRaster(dataBuffer, imWidth, imHeight, 8, null);
		depthImage.setData(raster);
	} 

	/**
	 * Updates the histogram for creation of depthimage.
	 * @param depthBuf buffer with color for every depth value
	 */
	private void calcHistogram(ShortBuffer depthBuf)
	{
		for (int i = 0; i <= maxDepth; i++)
			histogram[i] = 0;

		maxDepth = depthBuf.get();
		int minDepth = maxDepth;
		depthBuf.rewind();

		int numPoints = 0;
		while (depthBuf.remaining() > 0) {
			short depthVal = depthBuf.get();
			if (depthVal > maxDepth)
				maxDepth = depthVal;
			if (depthVal < minDepth)
				minDepth = depthVal;
			if ((depthVal != 0)  && (depthVal < MAX_DEPTH_SIZE))
			{ 
				histogram[depthVal]++;
				numPoints++;
			}
		}

		for (int i = 1; i <= maxDepth; i++)
			histogram[i] += histogram[i-1];

		/* convert cummulative depth into 8-bit range (0-255), 
	       which will later become colors using an indexed colour model
		 */
		if (numPoints > 0) {
			for (int i = 1; i <= maxDepth; i++)
				histogram[i] = (int) (256 * (1.0f - (histogram[i] / (float) numPoints)));
		}
	}

	/**
	 * Updates the rgb image.
	 */
	private void updateImage()
	{
		try {
			ByteBuffer imageBB = imageGen.getImageMap().createByteBuffer();
			setCameraImage(bufToImage(imageBB));
		}
		catch (GeneralException e) {
			System.out.println(e);
		}
	}

	/**
	 * Converts a byteBuffer to a bufferedImage
	 * @param pixelsRGB byteBuffer
	 * @return bufferedImage
	 */
	private BufferedImage bufToImage(ByteBuffer pixelsRGB)
	{
		int[] pixelInts = new int[imWidth * imHeight];

		int rowStart = 0;
		// rowStart will index the first byte (red) in each row;
		// starts with first row, and moves down

		int bbIdx;               // index into ByteBuffer
		int i = 0;               // index into pixels int[]
		int rowLen = imWidth * 3;    // number of bytes in each row
		for (int row = 0; row < imHeight; row++) {
			bbIdx = rowStart;
			// System.out.println("bbIdx: " + bbIdx);
			for (int col = 0; col < imWidth; col++) {
				int pixR = pixelsRGB.get( bbIdx++ );
				int pixG = pixelsRGB.get( bbIdx++ );
				int pixB = pixelsRGB.get( bbIdx++ );
				pixelInts[i++] = 
					0xFF000000 | ((pixR & 0xFF) << 16) | 
					((pixG & 0xFF) << 8) | (pixB & 0xFF);
			}
			rowStart += rowLen;   // move to next row
		}
		// create a BufferedImage from the pixel data
		BufferedImage im = 
			new BufferedImage( imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
		im.setRGB( 0, 0, imWidth, imHeight, pixelInts, 0, imWidth );
		return im;
	}

	/**
	 * Updates skelton information for all users.
	 */
	private void updateSkeleton()
	{
		int[] userIDs;
		try {
			userIDs = userGen.getUsers();
			for (int i = 0; i < userIDs.length; ++i) {
				int userID = userIDs[i];
				if (skelCap.isSkeletonCalibrating(userID))
					continue;    // test to avoid occassional crashes with isSkeletonTracking()
				if (skelCap.isSkeletonTracking(userID))
					updateJoints(userID);
			}
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the joint information for every joint.
	 * @param userID Id of current user
	 */
	private void updateJoints(int userID)
	{
		HashMap<SkeletonJoint, SkeletonJointPosition> skel = userSkels.get(userID);

		updateJoint(skel, userID, SkeletonJoint.HEAD);
		updateJoint(skel, userID, SkeletonJoint.NECK);

		updateJoint(skel, userID, SkeletonJoint.LEFT_SHOULDER);
		updateJoint(skel, userID, SkeletonJoint.LEFT_ELBOW);
		updateJoint(skel, userID, SkeletonJoint.LEFT_HAND);

		updateJoint(skel, userID, SkeletonJoint.RIGHT_SHOULDER);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_ELBOW);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_HAND);

		updateJoint(skel, userID, SkeletonJoint.TORSO);

		updateJoint(skel, userID, SkeletonJoint.LEFT_HIP);
		updateJoint(skel, userID, SkeletonJoint.LEFT_KNEE);
		updateJoint(skel, userID, SkeletonJoint.LEFT_FOOT);

		updateJoint(skel, userID, SkeletonJoint.RIGHT_HIP);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_KNEE);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_FOOT);
	}

	/**
	 * Updates the joint positions in the HashMap.
	 * @param skel HashMap for joints and jointPositions
	 * @param userID Id of current user
	 * @param joint the joint to be updated
	 */
	private void updateJoint(HashMap<SkeletonJoint, SkeletonJointPosition> skel,
			int userID, SkeletonJoint joint)
	{
		try {
			// report unavailable joints (should not happen)
			if (!skelCap.isJointAvailable(joint) || !skelCap.isJointActive(joint)) {
				System.out.println(joint + " not available for updates");
				return;
			}

			SkeletonJointPosition pos = skelCap.getSkeletonJointPosition(userID, joint);
			if (pos == null) {
				System.out.println("No update for " + joint);
				return;
			}
			SkeletonJointPosition jPos = null;
			if (pos.getPosition().getZ() != 0)   // has a depth position
			{
				jPos = new SkeletonJointPosition( 
						pos.getPosition(),
						pos.getConfidence());

				if (joint == SkeletonJoint.RIGHT_HAND)
				{
					if (jPos.getConfidence() >= 0.5)
					{
						rightHandPosition = jPos.getPosition();
					}
					else rightHandPosition = null;
				}

				if (joint == SkeletonJoint.LEFT_HAND)
				{
					if (jPos.getConfidence() >= 0.5)
					{
						leftHandPosition = jPos.getPosition();
					}
					else leftHandPosition = null;
				}
			}
			else  // no info found for that user's joint
			{
				jPos = new SkeletonJointPosition(new Point3D(), 0);
			}
			skel.put(joint, jPos);
			skelCap.saveSkeletonCalibrationDataToFile(userID, SKEL_FILE);
		}
		catch (StatusException e) 
		{  System.out.println(e); }
	}


	/**
	 * Get the position of a skeleton-joint.
	 * @param skelJoint the skeleton joint requested
	 * @return position of the skelton joint
	 */
	public Point3D getSkeletonJointPosition(SkeletonJoint skelJoint)
	{
		if (skelCap.isJointAvailable(skelJoint) && skelCap.isJointActive(skelJoint)) 
		{
			try {
				SkeletonJointPosition pos = skelCap.getSkeletonJointPosition(1, skelJoint);
				return pos.getPosition();
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Converts a point from real world coordinates to projective coordinates
	 * @param point real world point
	 * @return projective point
	 */
	public Point3D convertRealWorldToProjective(Point3D point)
	{
		Point3D temp = null;
		if (point != null)
		{
			try {
				temp = depthGen.convertRealWorldToProjective(point);
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
		return temp;
	}

	/**
	 * Converts a point from projective coordinates to real world coordinates
	 * @param point projective point
	 * @return real world point
	 */
	public Point3D convertProjectiveToRealWorld(Point3D point)
	{
		Point3D temp = null;
		if (point != null)
		{
			try {
				temp = depthGen.convertProjectiveToRealWorld(point);
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
		return temp;
	}

	private void setCameraImage(BufferedImage cameraImage) {
		this.cameraImage = cameraImage;
	}

	/**
	 * Gets the RGB image from sensor.
	 * @return rgb image
	 */
	public BufferedImage getCameraImage() {
		return cameraImage;
	}

	/**
	 * Gets the depth image from sensor.
	 * @return depth image
	 */
	public BufferedImage getDepthImage() {
		return depthImage;
	}

	private void setLeftHandPosition(Point3D leftHandPosition) {
		this.leftHandPosition = leftHandPosition;
	}

	/**
	 * Gets the position of the left hand in real world coordinates.
	 * @return real world position of left hand
	 */
	public Point3D getLeftHandPositionReal() {
		return leftHandPosition;
	}

	/**
	 * Gets the position of the left hand in projective coordinates.
	 * @return projective position of left hand
	 */
	public Point3D getLeftHandPositionProj(){
		return (convertRealWorldToProjective(leftHandPosition));
	}

	private void setRightHandPosition(Point3D rightHandPosition) {
		this.rightHandPosition = rightHandPosition;
	}

	/**
	 * Gets the position of the right hand in real world coordinates.
	 * @return real world position of right hand
	 */
	public Point3D getRightHandPositionReal() {
		return rightHandPosition;
	} 

	/**
	 * Gets the position of the right hand in projective coordinates.
	 * @return projective position of right hand
	 */
	public Point3D getRightHandPositionProj(){
		return (convertRealWorldToProjective(rightHandPosition));
	}

	/**
	 * Gets the depthmap from the sensor.
	 * @return depthmap
	 */
	public DepthMap getDepthMap()
	{
		return depthMD.getData();
	}

	/**
	 * Gets the depth at a certain point in real world coordinates
	 * @param p real world point
	 * @return depth at the specified point
	 */
	public int getDepthAt(Point3D p)
	{
		return depthMD.getData().readPixel((int)p.getX(), (int)p.getY());
	}

	private void setTracking(boolean isTracking) {
		this.isTracking = isTracking;
	}

	/**
	 * Returns if the sensor is tracking a user.
	 * @return 	true: sensor is tracking a user;
	 * 			false: sensor is not tracking any users
	 */
	public boolean isTracking() {
		return isTracking;
	}

	private void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	/**
	 * Gets the total duration of the last update cycle.
	 * @return duration in ms
	 */
	public long getTotalTime() {
		return totalTime;
	}

	/**
	 * Gets the HashMap with user skeleton information.
	 * @return HashMap with skelton information
	 */
	public HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> getUserSkels() {
		return userSkels;
	}

	private void setUserSkels(
			HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkels) {
		this.userSkels = userSkels;
	}

	/**
	 * Gets the GestureController object used for gesture tracking.
	 * @return GestureController object
	 */
	public GestureController getGestureCont() {
		return gestureCont;
	}

	private void setGestureCont(GestureController gestureCont) {
		this.gestureCont = gestureCont;
	}

	/**
	 * See if the gesture is toggled on or off
	 * @return true when on; false when off
	 */
	public boolean isToggleGesture() {
		return toggleGesture;
	}

	/**
	 * Toggle gestures on/off
	 * @param toggleGesture boolean
	 */
	public void setToggleGesture(boolean toggleGesture) {
		this.toggleGesture = toggleGesture;
	}
	
	/**
	 * Stop updating the Kinect information (skeleton, depth, ..)
	 */
	public void endTracking()
	{
		this.isTracking = false;
	}

	/* -------------
	 * - Observers -
	 * -------------
	 * For user detection/calibration.
	 */
	/**
	 * Called when new user is detected.
	 * Starts the pose detection.
	 */
	class NewUserObserver implements IObserver<UserEventArgs>
	{
		public void update(IObservable<UserEventArgs> observable, UserEventArgs args)
		{
			if (!isTracking)
			{
				System.out.println("Detected new user " + args.getId());

				if (args.getId() == 1)
				{
				/*if (!LoadCalibration())
				{*/
					try {
						// try to detect a pose for the new user
						poseDetectionCap.startPoseDetection(calibPoseName, args.getId());
					}
					catch (StatusException e)
					{ e.printStackTrace(); }
				//}
				}
			}
		}
	}

	/**
	 * Load the calibration from file instead of a pose.
	 * @return true if successful, false otherwise
	 */
	public boolean LoadCalibration()
	{
		int userId = 1;
		try {
			if (skelCap.isSkeletonCalibrated(userId)) return false;
			if (skelCap.isSkeletonCalibrating(userId)) return false;
			skelCap.loadSkeletonCalibrationDatadFromFile(userId, SKEL_FILE);
		}
		catch (StatusException e)
		{
			e.printStackTrace();
		}

		try {
			poseDetectionCap.stopPoseDetection(userId);
			skelCap.startTracking(userId);
			return true;

		} catch (StatusException e) {
			e.printStackTrace();
		}
		return false;
	} 

	/**
	 * Called when user is lost.
	 * Stops the tracking.
	 */
	class LostUserObserver implements IObserver<UserEventArgs>
	{
		public void update(IObservable<UserEventArgs> observable, UserEventArgs args)
		{ 
			System.out.println("Lost track of user " + args.getId());
			userSkels.remove(args.getId());    // remove user from userSkels
			if (args.getId() == 1) setTracking(false);
		}
	} 

	/**
	 * Called when pose is detected.
	 * Starts skeleton calibration.
	 */
	class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs>
	{
		public void update(IObservable<PoseDetectionEventArgs> observable,
				PoseDetectionEventArgs args)
		{
			int userID = args.getUser();
			System.out.println(args.getPose() + " pose detected for user " + userID);
			try {
				// finished pose detection; switch to skeleton calibration
				poseDetectionCap.stopPoseDetection(userID);
				skelCap.requestSkeletonCalibration(userID, true);
			}
			catch (StatusException e)
			{  e.printStackTrace(); }
		}
	}

	/**
	 * Called when calibration is completed.
	 * Starts user tracking.
	 */
	class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs>
	{
		public void update(IObservable<CalibrationProgressEventArgs> observable,
				CalibrationProgressEventArgs args)
		{
			int userID = args.getUser();
			System.out.println("Calibration status: " + args.getStatus() + " for user " + userID);
			try {
				if (args.getStatus() == CalibrationProgressStatus.OK) {
					// calibration succeeeded; move to skeleton tracking
					System.out.println("Starting tracking user " + userID);
					skelCap.startTracking(userID);
					setTracking(true);
					userSkels.put(new Integer(userID),
							new HashMap<SkeletonJoint, SkeletonJointPosition>());  
					// create new skeleton map for the user in userSkels
					
					//int temp = 0;
					//skelCap.saveSkeletonCalibrationData(userID, temp);
				}
				else    // calibration failed; return to pose detection
					poseDetectionCap.startPoseDetection(calibPoseName, userID);
			}
			catch (StatusException e)
			{  e.printStackTrace(); }
		}
	}
}
