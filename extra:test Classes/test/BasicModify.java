package be.wide.test;

// OpenNI/NITE imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.*;

import com.primesense.NITE.*;

// User imports
import be.wide.controller.*;
import be.wide.detector.*;
import be.wide.dom.*;

// Processing imports
import processing.core.*;

// Hemesh imports
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;
import wblut.hemesh.modifiers.*;
import wblut.hemesh.tools.*;

@SuppressWarnings("serial")
public class BasicModify extends PApplet
{
	// Control and Detect
	private KinectController kinect;
	private OpenCVHandler opencv;
	// ------------------

	// Camera
	private SphereCamera cam;
	// ------

	// Hemesh
	private HE_Mesh mesh;
	private WB_Render render;
	private HET_Selector selector;
	private HE_Face selected;
	// ------

	// Image
	private PImage handImage;
	private String imagePath = "../resources/openHand.png";
	// -----

	// Gestures
	private boolean isNavigation = true;
	private boolean isRotatingLeft = false;
	private boolean isRotatingRight = false;
	private boolean isRotatingUp = false;
	private boolean isRotatingDown = false;
	private float rotVal = 5;
	private float rotSpeed = 0.5f; 
	private GestureController gestCont;
	private boolean steady = false;
	private HashMap<Integer, List<HE_Face>> selectedList;
	private List<HE_Vertex> vertMoved;
	// ---------

	// Uncategorized
	private Point3D prevPos;
	// -------------

	public void setup()
	{
		size(800, 600, OPENGL);

		// Setup kinect
		kinect = KinectController.getInstance();
		kinect.setToggleGesture(true);

		// Get the kinect gesture controller
		// only gestures with right hand recognized
		gestCont = kinect.getGestureCont();

		// Register observers
		try {
			gestCont.getSwipeDetector().getSwipeLeftEvent().addObserver(new SwipeLeftObserver());
			gestCont.getSwipeDetector().getSwipeRightEvent().addObserver(new SwipeRightObserver());
			gestCont.getSwipeDetector().getSwipeUpEvent().addObserver(new SwipeUpObserver());
			gestCont.getSwipeDetector().getSwipeDownEvent().addObserver(new SwipeDownObserver());
			gestCont.getSteadyDetector().getSteadyEvent().addObserver(new SteadyObserver());
			gestCont.getSteadyDetector().getNotSteadyEvent().addObserver(new NotSteadyObserver());
			gestCont.getPushDetector().getPushEvent().addObserver(new PushObserver());
			gestCont.getSwipeDetector().setUseSteady(true);

		} catch (StatusException e) {
			e.printStackTrace();
		}

		// Detection
		opencv = new OpenCVHandler(false);

		// Camera
		cam = new SphereCamera(SphereCamera.defaultEye, SphereCamera.defaultLook, SphereCamera.defaultUp);

		// Mesh
		render = new WB_Render(this);
		selector = new HET_Selector(this);

		HEC_Geodesic geo = new HEC_Geodesic();
		geo.setRadius(200).setLevel(2); 
		mesh = new HE_Mesh(geo);
		//		HEM_Smooth smooth = new HEM_Smooth();
		//		mesh.modify(smooth);

		// Image
		handImage = new PImage();
		handImage = loadImage(imagePath, "png");
		handImage.resize(handImage.width/3, handImage.height/3);

		prevPos = null;
		selectedList = new HashMap<Integer, List<HE_Face>>();
		vertMoved = new ArrayList<HE_Vertex>();
	}

	public void draw()
	{
		// UPDATE
		if (steady)
		{
			background(125);
		}
		else
		{
			background(90);
		}
		lights();
		smooth();

		// 3D DRAWING
		hint(ENABLE_DEPTH_TEST);
		translate(width/2, height/2, 0);
		rotateX(radians(180));

		int xPos = 0;
		int yPos = 0;

		// Set Camera
		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		if (kinect.isTracking())
		{
			opencv.update();

			if (isNavigation)
			{
				if (isRotatingLeft)
				{
					mesh.rotateAboutAxis(rotVal, new WB_Point3d(0, 0, 0), new WB_Normal3d(0, 1, 0));
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingLeft = false;
						rotVal = 5;
						rotSpeed = 0.5f;
					}
				}
				else if (isRotatingRight)
				{
					mesh.rotateAboutAxis(-rotVal, new WB_Point3d(0, 0, 0), new WB_Normal3d(0, 1, 0));
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingRight = false;
						rotVal = 5;
						rotSpeed = 0.5f;
					}
				}
				else if (isRotatingUp)
				{
					mesh.rotateAboutAxis(-rotVal, new WB_Point3d(0, 0, 0), new WB_Normal3d(0, 0, 1));
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingUp = false;
						rotVal = 5;
						rotSpeed = 0.5f;
					}
				}
				else if (isRotatingDown)
				{
					mesh.rotateAboutAxis(rotVal, new WB_Point3d(0, 0, 0), new WB_Normal3d(0, 0, 1));
					rotVal -= rotSpeed;
					rotSpeed -= 0.1f;

					if (rotSpeed <= 0 || rotVal <= 0)
					{
						isRotatingDown = false;
						rotVal = 5;
						rotSpeed = 0.5f;
					}
				}
			}
			else
			{
				// Draw on screen
				stroke(0);
				strokeWeight(2);
				render.drawEdges(mesh);
				fill(0, 255, 0);
				render.drawFaces(selector, mesh);

				Hand right = opencv.getRightHandElement();
				Hand left = opencv.getLeftHandElement();
				if (right != null && left != null)
				{	
					Point3D rProj = kinect.convertRealWorldToProjective(right.getPosition());
					xPos = (int) rProj.getX();
					yPos = (int) rProj.getY();

					// Check selected face and see if it needs to be moved (extruded?)
					if(right.isFist() && left.isFist() && prevPos != null)
					{
						if (selector.get((int)rProj.getX(), (int)rProj.getY()) != null)
						{
							if (selected == null)
							{
								selected = mesh.getFaceByKey(selector.get((int)rProj.getX(), (int)rProj.getY()));
							}


							softSelection(selected);

							Integer[] keyList = new Integer[4];
							keyList = selectedList.keySet().toArray(keyList);
							double movement = (right.getPosition().getZ() - prevPos.getZ())/5;

							WB_Normal3d normal = selectedList.get(1).get(0).getFaceNormal();

							vertMoved.clear();
							for (int i = 1; i <= keyList.length; ++i)
							{
								moveFace(movement/i, selectedList.get(i), normal);
							}
						}
					}
					else
					{
						selected = null;
						Integer[] keyList = new Integer[4];
						keyList = selectedList.keySet().toArray(keyList);
						if (!selectedList.isEmpty()){
							for (int i = 1; i <= keyList.length; ++i)
							{
								if (i == 1)
								{
									fill(255, 0, 0);
								}
								else
								{
									fill(255, 99, 71);
								}
								List<HE_Face> faces = selectedList.get(i);
								for (HE_Face f : faces)
								{
									render.drawFaceSmooth(f.key(), mesh);
								}
							}
						}
					}

					prevPos = right.getPosition();
				}
			}
		}

		//		// Set Camera
		//		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
		//				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
		//				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		// Draw on screen
		if (isNavigation)
		{
			stroke(0);
			strokeWeight(2);
			render.drawEdges(mesh);

			fill(0, 255, 0);
			render.drawFaces(mesh);
		}

		// 2D DRAWING (OVERLAY)
		hint(DISABLE_DEPTH_TEST);
		camera();
		if (!isNavigation)
		{
			fill(255);
			stroke(0);
			image(handImage, xPos, yPos);
		}
	}

	private void moveFace(double move, List<HE_Face> faces, WB_Normal3d norm)
	{
		norm.normalize();
		for (HE_Face face : faces)
		{
			for (HE_Vertex vert : face.getFaceVertices())
			{		
				if (!vertMoved.contains(vert))
				{
					vert.moveBy(norm.toPoint().mult(move));
					vertMoved.add(vert);
				}
			}
		}
	}

	private void softSelection(HE_Face f)
	{

		// TODO Recursie toevoegen!
		selectedList.clear();
		List<HE_Face> softSel = new ArrayList<HE_Face>();
		softSel =  f.getNeighborFaces();

		fill(255, 99, 71);
		List<HE_Face> fir = new ArrayList<HE_Face>();
		List<HE_Face> sec = new ArrayList<HE_Face>();
		List<HE_Face> tri = new ArrayList<HE_Face>();

		for (HE_Face face : softSel)
		{	
			for (HE_Face fac : face.getNeighborFaces())
			{
				for (HE_Face fa : fac.getNeighborFaces())
				{
					render.drawFaceSmooth(fa.key(), mesh);
					tri.add(fa);
				}
				selectedList.put(4, tri);
				render.drawFaceSmooth(fac.key(), mesh);
				sec.add(fac);
			}
			selectedList.put(3, sec);
			render.drawFaceSmooth(face.key(), mesh);
			fir.add(face);
		}
		selectedList.put(2, fir);
		fill(255, 0, 0);
		render.drawFaceSmooth(f.key(), mesh);
		List<HE_Face> temp = new ArrayList<HE_Face>();
		temp.add(f);
		selectedList.put(1, temp);
	}


	// OBSERVERS

	/**
	 * Called when push/click gesture is detected.
	 */
	public class PushObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

			System.out.println("push event");
			if (kinect.isTracking() && !isRotatingLeft && !isRotatingRight)
			{
				isNavigation = !isNavigation;
			}
		}
	}

	/**
	 * Called when a left swipe is detected.
	 */
	public class SwipeLeftObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

			System.out.println("left swipe --");
			if (kinect.isTracking() && isNavigation && !isRotatingRight && !isRotatingDown && !isRotatingUp)
			{
				isRotatingLeft = true;
			}
		}
	}

	/**
	 * Called when a right swipe is detected.
	 */
	public class SwipeRightObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

			System.out.println("right swipe --x");

			if (kinect.isTracking() && isNavigation && !isRotatingLeft && !isRotatingDown && !isRotatingUp)
			{
				isRotatingRight = true;
			}
		}
	}

	/**
	 * Called when a swipe up is detected.
	 */
	class SwipeUpObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0,
				VelocityAngleEventArgs arg1) {
			System.out.println("gesture: swipe up");

			if (kinect.isTracking() && isNavigation && !isRotatingLeft && !isRotatingDown && !isRotatingRight)
			{
				isRotatingUp = true;
			}
		}
	}

	/**
	 * Called when a swipe down is detected.
	 */
	class SwipeDownObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0,
				VelocityAngleEventArgs arg1) {
			System.out.println("gesture: swipe down");

			if (kinect.isTracking() && isNavigation && !isRotatingLeft && !isRotatingUp && !isRotatingRight)
			{
				isRotatingDown = true;
			}
		}
	}

	/**
	 * Called when steady gesture is detected. Hand is not moving.
	 */
	public class SteadyObserver implements IObserver<IdValueEventArgs>
	{
		public void update(IObservable<IdValueEventArgs> arg0,
				IdValueEventArgs arg1) {
			steady = true;
		}
	}

	/**
	 * Called when not steady gesture is detected. Hand is moving.
	 */
	public class NotSteadyObserver implements IObserver<IdValueEventArgs>
	{
		public void update(IObservable<IdValueEventArgs> arg0,
				IdValueEventArgs arg1) {
			steady = false;
		}
	}
}
