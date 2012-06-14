package be.wide.test;

import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import be.wide.controller.KinectController;
import be.wide.detector.OpenCVHandler;
import be.wide.dom.Hand;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;

@SuppressWarnings("serial")
public class DetectionTest extends PApplet
{
	KinectController kinect;
	OpenCVHandler cv;

	public void setup()
	{
		size(640, 480);
		cv = new OpenCVHandler(true);
		kinect = KinectController.getInstance();
		PFont font = createFont("Verdana", 24);
		textFont(font);
	}

	public void draw()
	{
		background(0);
		cv.update();
		BufferedImage im = kinect.getDepthImage();
		PImage pIm = bufferedToPImage(im);
		image(pIm, 0, 0);
		
		fill(255, 0, 255);

		if (KinectController.getInstance().isTracking())
		{
			Hand left = cv.getLeftHandElement();
			if (left != null)
			{
				if (left.isFist())
				{
					text("left fist detected!", 10, 20);
				}
			}

			Hand right = cv.getRightHandElement();
			if (right != null)
			{
				if (right.isFist())
				{
					text("right fist detected!", 10, 50);
				}
			}
		}
	}
	
	private PImage bufferedToPImage(BufferedImage im)
	{
		PImage pIm = new PImage(im.getWidth(), im.getHeight(), PConstants.RGB);
		im.getRGB(0, 0, pIm.width, pIm.height, pIm.pixels, 0, pIm.width);
		pIm.updatePixels();
		return pIm;
	}

	public PImage getScreenShot() 
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		DisplayMode mode = gs[0].getDisplayMode();
		Rectangle bounds = new Rectangle(0, 0, mode.getWidth(), mode.getHeight());
		BufferedImage desktop = new BufferedImage(mode.getWidth(), mode.getHeight(), BufferedImage.TYPE_INT_RGB);

		try {
			desktop = new Robot(gs[0]).createScreenCapture(bounds);
		}
		catch(AWTException e) {
			System.err.println("Screen capture failed.");
		}

		return (new PImage(desktop));
	}
}
