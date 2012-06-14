package be.wide.detector;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import org.OpenNI.*;

import toxi.geom.Vec2D;
import be.wide.controller.*;
import be.wide.dom.*;
import com.googlecode.javacpp.*;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_imgproc.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class OpenCVHandler
{
	private KinectController kinect;	
	private int depthThreshold = 50;
	private int xOffsetL, yOffsetL;
	private int xOffsetR, yOffsetR;
	private BufferedImage rightHandImage, leftHandImage;
	private CanvasFrame canvas, contourCanvas, original;
	private CvFont font;
	private Hand leftHand, rightHand;
	private long totalTime = 0;
	private boolean showOutput = false;
	private float prevLeftDepth, prevRightDepth;
	private float depthOffset = 50;
	private int minFingersForFist = 1;
	private ArrayList<CvConvexityDefect> defectList;

	/**
	 * Standard constructor.
	 * Creates all the needed elements for the detection.
	 * @param show true: only right hand is calculated, and output is shown;
	 * 				false: both hand data is calculated, no output
	 */
	public OpenCVHandler(boolean show)
	{
		kinect = KinectController.getInstance();
		System.out.println("OpenCV started..");
		setShowOutput(show);
		System.out.println("OpenCV show ouput: " + show);
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		if (showOutput)
		{
			canvas = new CanvasFrame("Source");
			canvas.setLocation(100, 100);
			contourCanvas = new CanvasFrame("Contour");
			contourCanvas.setLocation(500, 100);
			original = new CanvasFrame("Original");
			original.setLocation(900, 100);
		}

		font = new CvFont();
		cvInitFont(font, CV_FONT_HERSHEY_COMPLEX, 1.0f, 1.0f, 0, 2, 8);
		defectList = new ArrayList<CvConvexityDefect>();
		System.out.println("--OpenCV initialized");
	}

	/**
	 * Updates the hand information for leftHand and rightHand elements.
	 */
	public void update()
	{	
		long startTime = System.currentTimeMillis();
		if (kinect.isTracking())
		{
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
						leftHandImage = leftImage.getBufferedImage();
						leftHand = new Hand("left");
						leftHand.setPosition(handL);

						if (!showOutput) { // only when output not requested! otherwise output is switching rapidly between left - right.
							detect(leftImage, leftHand);
						}
					}
				}
				prevLeftDepth = leftDepth;
			}

			// right hand image
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
						rightHandImage = rightImage.getBufferedImage();
						rightHand = new Hand("right");
						rightHand.setPosition(handR);

						detect(rightImage, rightHand);

						if (showOutput) {
							// showImage original
							original.showImage(rightImage);
						}
					}
				}
				prevRightDepth = rightDepth;
			}
		}
		setTotalTime((System.currentTimeMillis() - startTime));
		//System.out.println("OpenCV: " + totalTime + "ms");
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
	 * @param hand hand to be updated
	 */
	public void detect(IplImage image, Hand hand)
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

		if (showOutput) {
			canvas.showImage(grayImage);
		}

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

		if (defectCounter >= 2 && defectCounter <= 4)
		{
			CvConvexityDefect prevDef = null;
			for (int i = 0; i < defectList.size(); ++i)
			{
				CvConvexityDefect def = defectList.get(i);
				if (prevDef != null)
				{
					CvPoint p1 = def.start();
					CvPoint p2 = def.depth_point();
					CvPoint p3 = prevDef.start();

					Vec2D vec1 = new Vec2D((p1.x() - p2.x()), (p1.y() - p2.y()));
					Vec2D vec2 = new Vec2D((p2.x() - p3.x()), (p2.y() - p3.y()));

					vec1.normalize();
					vec2.normalize();


					double angle = Math.acos(vec1.dot(vec2));

					if (angle >= (Math.PI * 85 / 180) && angle <= (Math.PI * 95 /180))
					{
						//System.out.println("loodrecht: " + (angle*180/Math.PI) + " - " + p1.toString() + " - " + p3.toString());
					}

					cvLine(dst, p1, p2, CV_RGB( 0, 255, 0 ), 1, 8, 2);
					cvLine(dst, p2, p3, CV_RGB( 0, 255, 0 ), 1, 8, 2);

				}
				prevDef = def;
			}
		}

		if (showOutput) {
			contourCanvas.showImage(dst);
		}
		Point3D torsoPos = kinect.getSkeletonJointPosition(SkeletonJoint.TORSO);
		Point3D headPos = kinect.getSkeletonJointPosition(SkeletonJoint.HEAD);
		
		hand.setNumberOfFingers(defectCounter);
		if (defectCounter <= minFingersForFist)
		{
			hand.setFist(true);
		}
		else hand.setFist(false);
		
		if (hand.getPosition().getY() <= torsoPos.getY())
		{
			System.out.println("torso");
			hand.setFist(false);
		}
		if (hand.getPosition().getY() >= headPos.getY())
		{
			System.out.println("head");
			hand.setFist(false);
		}
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
	 * Gets the left hand object.
	 * @return left hand object.
	 */
	public Hand getLeftHandElement()
	{
		return leftHand;
	}

	/**
	 * Gets the right hand object.
	 * @return right hand object
	 */
	public Hand getRightHandElement()
	{
		return rightHand;
	}

	/**
	 * Gets the image of the left hand.
	 * @return image of left hand
	 */
	public BufferedImage getLeftHandImage()
	{
		return leftHandImage;
	}

	/**
	 * Gets the image of the right hand.
	 * @return image of right hand
	 */
	public BufferedImage getRightHandImage()
	{
		return rightHandImage;
	}

	private void setShowOutput(boolean showOutput) {
		this.showOutput = showOutput;
	}

	/**
	 * Returns if the output is being shown.
	 * @return true: output is shown;
	 * 			false: no output is shown
	 */
	public boolean isShowOutput() {
		return showOutput;
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
}
