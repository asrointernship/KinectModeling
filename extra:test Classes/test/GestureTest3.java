package be.wide.test;

import java.util.HashMap;

import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.StatusException;

import be.wide.controller.GestureController;
import be.wide.controller.KinectController;
import be.wide.controller.SphereCamera;
import be.wide.detector.BodyPoseEventArgs;
import be.wide.detector.OpenCVHandler;
import be.wide.dom.Hand;
import be.wide.test.GestureTest2.CircleObserver;
import be.wide.test.GestureTest2.NoCircleObserver;
import be.wide.test.GestureTest2.NotSteadyObserver;
import be.wide.test.GestureTest2.PushObserver;
import be.wide.test.GestureTest2.SteadyObserver;
import be.wide.test.GestureTest2.SwipeDownObserver;
import be.wide.test.GestureTest2.SwipeLeftObserver;
import be.wide.test.GestureTest2.SwipeRightObserver;
import be.wide.test.GestureTest2.SwipeUpObserver;
import be.wide.test.GestureTest2.WaveObserver;

import com.primesense.NITE.CircleEventArgs;
import com.primesense.NITE.IdValueEventArgs;
import com.primesense.NITE.NoCircleEventArgs;
import com.primesense.NITE.NullEventArgs;
import com.primesense.NITE.VelocityAngleEventArgs;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import wblut.core.processing.WB_Render;
import wblut.geom.core.WB_Plane;
import wblut.geom.core.WB_Point3d;
import wblut.geom.core.WB_Vector3d;
import wblut.hemesh.core.HE_Mesh;
import wblut.hemesh.creators.HEC_Box;
import wblut.hemesh.creators.HEC_Geodesic;
import wblut.hemesh.creators.HEC_Sphere;
import wblut.hemesh.creators.HEMC_VoronoiCells;
import wblut.hemesh.modifiers.HEM_Slice;
import wblut.hemesh.modifiers.HEM_Smooth;
import wblut.hemesh.tools.HET_Selector;

public class GestureTest3 extends PApplet
{

	private KinectController kinect;
	private OpenCVHandler opencv;
	private GestureController gest;
	private String gestString;
	private SphereCamera cam;

	// VORONOI
	private HE_Mesh mesh;
	float[][] points;
	private int numpoints;
	private HE_Mesh[] cells;
	private int numcells;
	private WB_Plane P1,P2;
	// -------

	private WB_Render render;
	private HE_Mesh outerSphere;
	private boolean isZooming = true;
	private boolean isNavigation = false;
	private float rotVal = 12;
	private float rotSpeed = 1; 
	private boolean steady = false;
	private Point3D prevPos = null;
	private float prevDist = 0;
	private PImage image;
	private String modeString;
	private HET_Selector selector;
	private PImage handImage;
	private String imagePath = "../resources/openHand.png";


	public void setup()
	{
		size(640, 480, OPENGL);

		background(125);
		PFont font = createFont("Verdana", 18);
		textFont(font);
		textMode(SCREEN);
		text("Loading..", width/2 - 20, height/2);
		redraw();

		kinect = KinectController.getInstance();
		opencv = new OpenCVHandler(false);

		gest = kinect.getGestureCont();
		//kinect.setToggleGesture(false);
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

		HEC_Geodesic geo = new HEC_Geodesic();
		geo.setRadius(300).setLevel(2); 
		mesh=new HE_Mesh(geo);

		//slice off most of both hemispheres
		P1=new WB_Plane(new WB_Point3d(0,0,-10), new WB_Vector3d(0,0,1));
		P2=new WB_Plane(new WB_Point3d(0,0,10), new WB_Vector3d(0,0,-1));
		HEM_Slice s=new HEM_Slice().setPlane(P1);
		mesh.modify(s);
		s=new HEM_Slice().setPlane(P2);
		mesh.modify(s);
		
		HEM_Smooth sm = new HEM_Smooth();
		mesh.modify(sm);

		//generate points
		numpoints = 50;
		points = new float[numpoints][3];
		for(int i=0;i<numpoints;i++) {
			points[i][0]=random(-250,250);
			points[i][1]=random(-250,250);
			points[i][2]=random(-20,20);
		}

		//generate voronoi cells
		HEMC_VoronoiCells vcmc = new HEMC_VoronoiCells();
		vcmc.setPoints(points).setContainer(mesh).setOffset(5);
		cells=vcmc.create();
		numcells=cells.length;
		cam.zoomOut(200);
		cam.left(50);
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
		lights();
	
		hint(ENABLE_DEPTH_TEST);
		translate(width/2, height/2, 0);
		rotateX(radians(180));

		int xPos = 0;
		int yPos = 0;
		//drawAxes(1000, true);
		
		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);


		if (kinect.isTracking())
		{
			opencv.update();

			Hand right = opencv.getRightHandElement();
			if (right != null)
			{
				xPos = (int) kinect.convertRealWorldToProjective(right.getPosition()).getX();
				yPos = (int) kinect.convertRealWorldToProjective(right.getPosition()).getY();

				//System.out.println(xPos + ", "+ yPos);
			}

			if (isNavigation)
			{
				Hand left = opencv.getLeftHandElement();
				if (left != null && right != null && prevPos != null)
				{
					if (right.isFist() && left.isFist())
					{
						Point3D rProj = kinect.convertRealWorldToProjective(right.getPosition());
						Point3D pProj = kinect.convertRealWorldToProjective(prevPos);
						float xDist = (rProj.getX() - pProj.getX())/3;
						float yDist = (rProj.getY() - pProj.getY())/3;

						if (xDist < 0)
						{
							cam.left(-xDist);
						}
						else
						{
							cam.right(xDist);
						}

						if (yDist < 0)
						{
							cam.up(yDist);
						}
						else
						{
							cam.down(-yDist);
						}
					}
				}
			}
			else if (isZooming)
			{
				Hand left = opencv.getLeftHandElement();
				if (left != null && right != null && prevPos != null)
				{
					if (left.isFist() && right.isFist())
					{
						float zDist = (right.getPosition().getZ() - prevPos.getZ()) - prevDist;
						
						if (zDist < 0)
						{
							cam.zoomIn(zDist);
						}
						else
						{
							cam.zoomOut(-zDist);
						}
						prevDist = zDist;
					}
				}
			}
			
			
			if (right != null)
			{
				prevPos = right.getPosition();
			}
			image = createSkelImage();
		}

		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		/*fill(0, 255, 0);
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
		}*/

		drawEdges();
		drawFaces();

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

	private void drawEdges(){
		smooth();
		stroke(0);
		strokeWeight(2);
		for(int i=0;i<numcells;i++) {
			render.drawEdges(cells[i]);
		} 
	}

	private	void drawFaces(){
		noSmooth();
		noStroke();
		for(int i=0;i<numcells;i++) {
			fill(100+i,i,i);
			render.drawFaces(cells[i]);
		}  
	}

	public PImage createSkelImage()
	{
		PGraphics temp = createGraphics(640, 480, P2D);
		temp.beginDraw();
		temp.background(0);
		temp.stroke(255);
		temp.strokeWeight(10);
		temp.smooth();
		PImage ret = new PImage();

		HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> allSkel = kinect.getUserSkels();

		if (allSkel.containsKey(1))
		{
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
			ret = temp.get();
			ret.resize(160, 120);
		}
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

			System.out.println("PUSH");
			if (kinect.isTracking())
			{
				if (isNavigation)
				{
					isNavigation = false;
					isZooming = true;
				}
				else if (isZooming)
				{
					isNavigation = true;
					isZooming = false;
				}
				else
				{
					isZooming = true;
				}
			}
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


