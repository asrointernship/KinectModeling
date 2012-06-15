package be.wide.detector;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import java.awt.Color;
import java.awt.image.*;
import java.util.*;
import org.OpenNI.*;
import be.wide.controller.KinectController;
import com.googlecode.javacpp.*;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_objdetect;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class DetectionHandler implements IObservable<HandDetectionEventArgs>
{
	private KinectController kinect;	
	private int depthThreshold = 50;
	private int xOffsetL, yOffsetL;
	private int xOffsetR, yOffsetR;
	private BufferedImage rightHandImage, leftHandImage;
	private CanvasFrame sourceFrame, contourFrame;
	private CvFont font;
	private float prevLeftDepth, prevRightDepth;
	private float depthOffset = 50;
	private int minFingersForFist = 1;
	private ArrayList<CvConvexityDefect> defectList;

	private List<IObserver<HandDetectionEventArgs>> observers;

	/**
	 * Standard constructor. 
	 * Creates the DetectionHandler object and initializes dependencies.
	 */
	public DetectionHandler()
	{
		observers = new ArrayList<IObserver<HandDetectionEventArgs>>();
		kinect = KinectController.getInstance();
		Loader.load(opencv_objdetect.class);
		font = new CvFont();
		cvInitFont(font, CV_FONT_HERSHEY_COMPLEX, 1.0f, 1.0f, 0, 2, 8);
		defectList = new ArrayList<CvConvexityDefect>();
		sourceFrame = new CanvasFrame("Source");
		sourceFrame.setLocation(100, 100);
		sourceFrame.setVisible(false);
		contourFrame = new CanvasFrame("Contour");
		contourFrame.setLocation(500, 100);
		contourFrame.setVisible(false);
		System.out.println("--OpenCV initialized");
	}

	/**
	 * Updates the hand detection information.
	 */
	public void update()
	{
		if (kinect.isTracking())
		{
			Point3D head = kinect.getSkeletonJointPosition(SkeletonJoint.HEAD);
			Point3D hip = kinect.getSkeletonJointPosition(SkeletonJoint.LEFT_HIP);

			// left hand image
			Point3D handL = kinect.getLeftHandPositionReal();
			if (handL != null)
			{
				int leftDepth = (int) handL.getZ();
				xOffsetL = yOffsetL = 150;

				if (prevLeftDepth <= leftDepth + depthOffset && prevLeftDepth >= leftDepth - depthOffset)
				{
					IplImage leftImage = createHandImage(handL, leftDepth, xOffsetL, yOffsetL);
					if (leftImage != null)
					{
						setLeftHandImage(leftImage.getBufferedImage());
						
						if (handL.getY() >= hip.getY() - 180 && handL.getY() <= head.getY() + 100)
						{
							HandDetectionEventArgs hdEventArgs = new HandDetectionEventArgs(this, detect(leftImage), "left");
							for (IObserver<HandDetectionEventArgs> obs : observers)
							{
								obs.update(this, hdEventArgs);
							}
						}
						else
						{
							HandDetectionEventArgs hdEventArgs = new HandDetectionEventArgs(this, false, "left");
							for (IObserver<HandDetectionEventArgs> obs : observers)
							{
								obs.update(this, hdEventArgs);
							}
						}
					}
				}
				prevLeftDepth = leftDepth;
			}

			Point3D handR = kinect.getRightHandPositionReal();

			if (handR != null)
			{
				int rightDepth = (int)handR.getZ();
				xOffsetR = yOffsetR = 150;

				if (prevRightDepth <= rightDepth + depthOffset && prevRightDepth >= rightDepth - depthOffset)
				{
					IplImage rightImage = createHandImage(handR, rightDepth, xOffsetR, yOffsetR);
					if (rightImage != null)
					{
						setRightHandImage(rightImage.getBufferedImage());
						
						if (handR.getY() >= hip.getY() - 180 && handR.getY() <= head.getY() + 100)
						{
							HandDetectionEventArgs hdEventArgs = new HandDetectionEventArgs(this, detect(rightImage), "right");
							for (IObserver<HandDetectionEventArgs> obs : observers)
							{
								obs.update(this, hdEventArgs);
							}
						}
						else
						{
							HandDetectionEventArgs hdEventArgs = new HandDetectionEventArgs(this, false, "right");
							for (IObserver<HandDetectionEventArgs> obs : observers)
							{
								obs.update(this, hdEventArgs);
							}
						}
					}
				}
				prevRightDepth = rightDepth;
			}
		}
	}

	/**
	 * Creates a image of the hand at the specified position with specified depth and offset.
	 * @param hand real world position of the hand 
	 * @param depth depth at the current hand
	 * @param xOffset offset in x direction
	 * @param yOffset offset in y direction
	 * @return B/W image of the hand
	 */
	public IplImage createHandImage(Point3D hand, int depth, int xOffset, int yOffset)
	{
		Color black = new Color(0, 0, 0);
		Color white = new Color(255, 255, 255);

		hand = kinect.convertRealWorldToProjective(hand); 

		int startX = (int) hand.getX() - xOffset;
		int startY = (int) hand.getY() - yOffset;
		int maxX = (int) hand.getX() + xOffset;
		int maxY = (int) hand.getY() + yOffset;

		int imWidth = Math.abs(xOffset*2);
		int imHeight = Math.abs(yOffset*2);

		IplImage im = null;

		if (imWidth > 0 && imHeight > 0)
		{
			BufferedImage temp = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_RGB);

			for (int x = startX; x < maxX; ++x)
			{
				for (int y = startY; y < maxY; ++y)
				{
					int currDepth = kinect.getDepthMap().readPixel(x, y);

					if (currDepth >= (depth - depthThreshold) && currDepth <= (depth + depthThreshold)
							&& x >= startX && x <= maxX
							&& y >= startY && y <= maxY)
					{
						temp.setRGB(x-startX, y-startY, white.getRGB());
					}
					else temp.setRGB(x-startX, y-startY, black.getRGB());
				}
			}
			im = IplImage.createFrom(temp);
			im.depth(IPL_DEPTH_8U);
		}
		return im;
	}

	/**
	 * Detect the number of fingers and updates the hand element.
	 * @param image hand image to be checked
	 */
	public boolean detect(IplImage image)
	{
		IplImage grayImage = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, 1);
		cvZero(grayImage);
		cvCvtColor(image, grayImage, CV_BGR2GRAY);

		CvMemStorage storage = CvMemStorage.create();

		// Adapt original image with smoothing and erode
		cvThreshold( grayImage, grayImage, 1, 255, CV_THRESH_BINARY );
		cvSmooth(grayImage, grayImage, CV_GAUSSIAN, 3, 5, 0, 0);
		cvErode(grayImage, grayImage, null, 1);
		cvSmooth(grayImage, grayImage, CV_GAUSSIAN, 5, 7, 0, 0);

		// Find contours
		CvSeq contour = new CvSeq(null);
		cvFindContours(grayImage, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
		IplImage dst = cvCreateImage( cvGetSize(grayImage), 8, 3 );
		cvZero(dst);
		CvMemStorage hullstorage = CvMemStorage.create();
		CvMemStorage polystorage = CvMemStorage.create();

		int defectCounter = 0;
		double prevSize = 0;
		CvSeq biggestContour = new CvSeq(null);

		// Find biggest contour (98% it's the hand)
		while(contour != null && !contour.isNull())
		{
			double contourSize = Math.abs(cvContourArea(contour, CV_WHOLE_SEQ, 0));
			if (contourSize > prevSize)
			{
				prevSize = contourSize;
				biggestContour = contour;
			}
			contour = contour.h_next();
		}

		if (biggestContour != null && !biggestContour.isNull())
		{
			CvScalar colorOut = CV_RGB(125, 125, 125);
			CvScalar colorIn = CV_RGB(255, 0, 0);

			biggestContour = cvApproxPoly(biggestContour, Loader.sizeof(CvContour.class), polystorage, CV_POLY_APPROX_DP, 1.2, 1);

			// Show contours
			cvDrawContours(dst, biggestContour, colorOut, colorIn, 1, 1, 8);

			// Find convex hull
			CvSeq hull = cvConvexHull2(biggestContour, hullstorage, CV_CLOCKWISE, 0);

			// Find convex hull defects (peaks and valleys)
			CvSeq defects = cvConvexityDefects(biggestContour, hull, hullstorage);
			defectList.clear();

			for (int i = 0; i < defects.total(); ++i)
			{
				Pointer pntr = cvGetSeqElem(defects, i);
				CvConvexityDefect cdf = new CvConvexityDefect(pntr);

				// Count the defects (if depth of defect greater than 5 and defect is in top 2/3 of image)
				if (cdf.depth() >= 3 && cdf.start().y() <= (image.height() - image.height()/3 - 10)
						&& cdf.start().y() >= image.height()/3
						&& cdf.start().x() >= image.width()/4
						&& cdf.start().x() <= (image.width() - image.width()/4))
				{
					cvCircle(dst, cdf.start(), 3, CV_RGB(0, 255, 0), -1, 8, 0);
					defectCounter++;
					defectList.add(cdf);
				}
			}
			String text = Integer.toString(defectCounter);
			cvPutText(dst, text , new CvPoint(10, image.height() - 5), font, CV_RGB(255, 255, 255));
		}

		if (defectCounter <= minFingersForFist)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Maps a value with a certain range to a new range.
	 * @param value values to be mapped
	 * @param iStart begin value of original range
	 * @param iStop end value of original range
	 * @param oStart begin value of new range
	 * @param oStop end value of new range
	 * @return mapped value as float
	 */
	public static float mapTo(float value, float iStart, float iStop, float oStart, float oStop)
	{
		return oStart + (oStop - oStart) * ((value - iStart) / (iStop - iStart));
	}

	/**
	 * Maps a value with a certain range to a new range.
	 * @param value values to be mapped
	 * @param iStart begin value of original range
	 * @param iStop end value of original range
	 * @param oStart begin value of new range
	 * @param oStop end value of new range
	 * @return mapped value as double
	 */
	public static double mapTo(double value, double iStart, double iStop, double oStart, double oStop)
	{
		return oStart + (oStop - oStart) * ((value - iStart) / (iStop - iStart));
	}

	/**
	 * Sets the image of the right hand.
	 * @param rightHandImage bufferedImage
	 */
	public void setRightHandImage(BufferedImage rightHandImage) {
		this.rightHandImage = rightHandImage;
	}

	/**
	 * Gets the image of the right hand.
	 * @return bufferedImage of right hand
	 */
	public BufferedImage getRightHandImage() {
		return rightHandImage;
	}

	/**
	 * Sets the image of the left hand.
	 * @param leftHandImage bufferedImage
	 */
	public void setLeftHandImage(BufferedImage leftHandImage) {
		this.leftHandImage = leftHandImage;
	}

	/**
	 * Gets the image of the left hand.
	 * @return bufferedImage of the left hand.
	 */
	public BufferedImage getLeftHandImage() {
		return leftHandImage;
	}

	/**
	 * Add an observer to the list of observers.
	 * Register an object to listen to updates of this class.
	 */
	public void addObserver(IObserver<HandDetectionEventArgs> arg0) throws StatusException 
	{
		if (arg0 != null)
		{
			observers.add(arg0);
		}
	}

	/**
	 * Remove an observer from the list of observers.
	 * Unregister an object to listen to updates of this class.
	 */
	public void deleteObserver(IObserver<HandDetectionEventArgs> arg0) 
	{
		if (observers.contains(arg0))
		{
			observers.remove(arg0);
		}
	}
}
