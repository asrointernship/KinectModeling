package be.wide.test;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.OpenNI.*;

import be.wide.controller.*;
import be.wide.detector.*;
import be.wide.dom.Hand;
import com.primesense.NITE.*;
import processing.core.*;
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;
import wblut.hemesh.tools.HET_Selector;

@SuppressWarnings("serial")
public class GestureTest2 extends PApplet
{
	private KinectController kinect;
	private OpenCVHandler opencv;
	private GestureController gest;
	private String gestString;
	private SphereCamera cam;
	private HE_Mesh mesh;
	private WB_Render render;
	private HE_Mesh outerSphere;
	private boolean isRotating = false;
	private boolean isRotatingLeft = false;
	private boolean isRotatingRight = false;
	private boolean isRotatingUp = false;
	private boolean isRotatingDown = false;
	private boolean isZooming = false;
	private float rotVal = 12;
	private float rotSpeed = 1; 
	private boolean steady = false;
	private Point3D prevPos = null;
	private PImage image;
	private String modeString;
	private HET_Selector selector;
	private PImage handImage;
	private String imagePath = "../resources/openHand.png";


	public void setup()
	{
		size(800, 600, OPENGL);

		background(125);
		PFont font = createFont("Verdana", 18);
		textFont(font);
		textMode(SCREEN);
		text("Loading..", width/2 - 20, height/2);
		redraw();

		kinect = KinectController.getInstance();
		opencv = new OpenCVHandler(false);

		gest = kinect.getGestureCont();
		kinect.setToggleGesture(false);
		setObservers();

		cam = new SphereCamera(SphereCamera.defaultEye, SphereCamera.defaultLook, SphereCamera.defaultUp);

		HEC_Box boxCreator = new HEC_Box(100, 100, 100, 1, 1, 1);

		mesh = new HE_Mesh(boxCreator);	
		render = new WB_Render(this);
		image = new PImage();
		modeString = "";
		selector = new HET_Selector(this);
		handImage = new PImage();
		handImage = loadImage(imagePath, "png");
		handImage.resize(handImage.width/3, handImage.height/3);
	}

	private void setObservers()
	{
		try {
			// SWIPES
			gest.getSwipeDetector().getSwipeLeftEvent().addObserver(new SwipeLeftObserver());
			gest.getSwipeDetector().getSwipeRightEvent().addObserver(new SwipeRightObserver());
			gest.getSwipeDetector().getSwipeUpEvent().addObserver(new SwipeUpObserver());
			gest.getSwipeDetector().getSwipeDownEvent().addObserver(new SwipeDownObserver());
			gest.getSwipeDetector().setUseSteady(true);

			// WAVE
			gest.getWaveDetector().getWaveEvent().addObserver(new WaveObserver());

			// CIRCLE
			gest.getCircleDetector().getCircleEvent().addObserver(new CircleObserver());
			gest.getCircleDetector().getNoCircleEvent().addObserver(new NoCircleObserver());

			// PUSH
			gest.getPushDetector().getPushEvent().addObserver(new PushObserver());

			// STEADY
			gest.getSteadyDetector().getSteadyEvent().addObserver(new SteadyObserver());
			gest.getSteadyDetector().getNotSteadyEvent().addObserver(new NotSteadyObserver());


		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	public void draw()
	{
		background(125);
		smooth();

		opencv.update();

		hint(ENABLE_DEPTH_TEST);
		translate(width/2, height/2, 0);
		rotateX(radians(180));

		int xPos = 0;
		int yPos = 0;
		//drawAxes(1000, true);

		if (kinect.isTracking())
		{
			if (isRotating)
			{
				modeString = "Rotate";
				if (isRotatingLeft)
				{
					cam.right(rotVal);
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingLeft = false;
						rotVal = 10;
						rotSpeed = 1;
					}
				}
				else if (isRotatingRight)
				{
					cam.left(rotVal);
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingRight = false;
						rotVal = 12;
						rotSpeed = 1;
					}
				}
				else if (isRotatingUp)
				{
					cam.up(rotVal);
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingUp = false;
						rotVal = 12;
						rotSpeed = 1;
					}
				}
				else if (isRotatingDown)
				{
					cam.down(rotVal);
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingDown = false;
						rotVal = 12;
						rotSpeed = 1;
					}
				}
			}
			else if (isZooming)
			{
				modeString = "Zoom";
				Hand left = opencv.getLeftHandElement();
				if (left != null)
				{
					if (left.isFist())
					{
						Hand right = opencv.getRightHandElement();
						if (right != null)
						{
							float dist = prevPos.getZ() - right.getPosition().getZ();
							cam.zoomIn(dist);
							prevPos = right.getPosition();
						}
					}
					else
					{
						isZooming = false;
					}
				}
			}
			else
			{
				modeString = "";
			}

			Hand right = opencv.getRightHandElement();
			if (right != null)
			{
				xPos = (int) kinect.convertRealWorldToProjective(right.getPosition()).getX();
				yPos = (int) kinect.convertRealWorldToProjective(right.getPosition()).getY();

				//System.out.println(xPos + ", "+ yPos);

				prevPos = right.getPosition();
			}
			Hand left = opencv.getLeftHandElement();
			if (left != null && right != null)
			{

				if (left.isFist())
				{
					if (right.isFist())
					{
						//isRotating = true;
					}
				}
				else
				{
					if (isRotating)
					{
						isRotating = false;
					}
				}
			}
//			BufferedImage bimg = kinect.getDepthImage();
//			image = new PImage(bimg.getWidth(),bimg.getHeight(),PConstants.ARGB);
//			bimg.getRGB(0, 0, image.width, image.height, image.pixels, 0, image.width);
//			image.updatePixels();
			image = createSkelImage();
			image.resize(160, 120);
		}

		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		fill(0, 255, 0);
		render.drawFaces(mesh);
		strokeWeight(1);
		stroke(0, 0, 0);
		render.drawEdges(mesh);

		noFill();
		noStroke();
		render.drawVertices(selector, 5, mesh);

		stroke(255,0,0);
		fill(255,0,0);

		if (kinect.isTracking())
		{
			if (selector.get(xPos, yPos) != null)
			{
				render.drawVertex(selector.get(xPos, yPos), 20, mesh);
			}
		}


		HEC_Sphere sphereCreator = new HEC_Sphere();
		WB_Vector3d vec = new WB_Vector3d(new WB_Point3d(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z));
		vec.sub(new WB_Point3d(0, 0, 0));
		double rad = Math.sqrt((vec.x *vec.x) + (vec.y*vec.y)+ (vec.z*vec.z)) + 10;
		sphereCreator.setRadius(rad);
		sphereCreator.setUFacets(15);
		sphereCreator.setVFacets(10);
		outerSphere = new HE_Mesh(sphereCreator);

		pushMatrix();
		rotateX(radians(90));
		if (steady)
		{
			stroke(0, 255, 0);
		}
		else
		{
			stroke(0);
		}
		strokeWeight(0.5f);
		render.drawEdges(outerSphere);
		popMatrix();

		hint(DISABLE_DEPTH_TEST);
		camera();
		image(image, 0, 0);
		text(modeString, image.width + 10, 10);
		image(handImage, xPos, yPos);
	}
	
	public PImage createSkelImage()
	{
		PGraphics temp = createGraphics(640, 480, P2D);
		temp.beginDraw();
		temp.background(0);
		temp.stroke(255);
		temp.strokeWeight(10);
		
		HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> allSkel = kinect.getUserSkels();
		HashMap<SkeletonJoint, SkeletonJointPosition> userSkel = (HashMap<SkeletonJoint, SkeletonJointPosition>) allSkel.get(1);
		
		Point3D p1 = userSkel.get(SkeletonJoint.HEAD).getPosition();
		Point3D p2 = userSkel.get(SkeletonJoint.NECK).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		p2 = kinect.convertRealWorldToProjective(p2);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.LEFT_SHOULDER).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.RIGHT_SHOULDER).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p2 = userSkel.get(SkeletonJoint.LEFT_SHOULDER).getPosition();
		p2 = kinect.convertRealWorldToProjective(p2);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.LEFT_ELBOW).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p2 = userSkel.get(SkeletonJoint.LEFT_HAND).getPosition();
		p2 = kinect.convertRealWorldToProjective(p2);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.RIGHT_SHOULDER).getPosition();
		p2 = userSkel.get(SkeletonJoint.RIGHT_ELBOW).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		p2 = kinect.convertRealWorldToProjective(p2);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.RIGHT_HAND).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.RIGHT_SHOULDER).getPosition();
		p2 = userSkel.get(SkeletonJoint.TORSO).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		p2 = kinect.convertRealWorldToProjective(p2);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.LEFT_SHOULDER).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.LEFT_HIP).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		p1 = userSkel.get(SkeletonJoint.RIGHT_HIP).getPosition();
		p1 = kinect.convertRealWorldToProjective(p1);
		temp.endDraw();
		PImage ret = new PImage();
		ret = temp.get(0, 0, temp.width, temp.height);
		ret.updatePixels();
		return ret;
	}

	public void setGestString(String gestString) {
		this.gestString = gestString;
	}

	public String getGestString() {
		return gestString;
	}

	/**
	 * Called when a left swipe is detected.
	 */
	public class SwipeLeftObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

		}
	}

	/**
	 * Called when right swipe is detected.
	 */
	public class SwipeRightObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

		}
	}

	/**
	 * Called when swipe up is detected.
	 */
	class SwipeUpObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

		}
	}

	/**
	 * Called when swipe down is detected.
	 */
	class SwipeDownObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

		}
	}

	/**
	 * Called when wave gesture is detected.
	 */
	public class WaveObserver implements IObserver<NullEventArgs>
	{
		public void update(IObservable<NullEventArgs> arg0, NullEventArgs arg1) {

			System.out.println("WAVE");
		}
	}

	/**
	 * Called when circle gesture is detected.
	 */
	public class CircleObserver implements IObserver<CircleEventArgs>
	{
		public void update(IObservable<CircleEventArgs> arg0, CircleEventArgs arg1) {

			System.out.println("CIRCLE");		
		}
	}

	/**
	 * Called when no circle gesture is detected.
	 * Prints the gesture to console.
	 */
	public class NoCircleObserver implements IObserver<NoCircleEventArgs>
	{
		public void update(IObservable<NoCircleEventArgs> arg0,
				NoCircleEventArgs arg1) {
			System.out.println("no circle");
		}
	}

	/**
	 * Called when push/click gesture is detected.
	 */
	public class PushObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {


		}
	}

	/**
	 * Called when steady gesture is detected.
	 */
	public class SteadyObserver implements IObserver<IdValueEventArgs>
	{
		public void update(IObservable<IdValueEventArgs> arg0,
				IdValueEventArgs arg1) {
			steady = true;
		}
	}

	/**
	 * Called when not steady gesture is detected.
	 */
	public class NotSteadyObserver implements IObserver<IdValueEventArgs>
	{
		public void update(IObservable<IdValueEventArgs> arg0,
				IdValueEventArgs arg1) {
			steady = false;
		}
	}

	public class BodyPoseObserver implements IObserver<BodyPoseEventArgs>
	{
		public void update(IObservable<BodyPoseEventArgs> arg0,
				BodyPoseEventArgs arg1) {
			System.out.println("body pose: " + arg1.getPose().getName());
		}
	}
}
