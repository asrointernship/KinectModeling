package be.wide.main;

import java.util.*;
import org.OpenNI.*;
import com.primesense.NITE.*;
import be.wide.controller.*;
import be.wide.detector.*;
import be.wide.dom.*;
import processing.core.*;
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;
import wblut.hemesh.subdividors.*;
import wblut.hemesh.tools.*;

@SuppressWarnings("serial")
public class MainProgram extends PApplet
{

	private KinectController kinect;
	private OpenCVHandler opencv;
	private GestureController gest;
	private SphereCamera cam;
	private BodyPoseHandler bpHandler;
	private BodyPoseDetector pd;

	// Hemesh
	private HE_Mesh mesh;
	float[][] points;
	// ------

	private WB_Render render;
	private boolean isZooming = false;
	private boolean isNavigation = false;
	private boolean isModeling = false;
	private boolean isChoosing = true;
	private Point3D prevPos = null;
	private PImage image;
	private PImage poseImage;
	private String modeString;
	private HET_Selector selector;
	private PImage handImage;
	private String imagePath = "../resources/openHand.png";
	private HE_Face selected;
	private HashMap<Integer, List<HE_Face>> selectedList;
	private List<HE_Vertex> vertMoved;
	private int bgColor = 125;
	private Slider rotateSlider;
	private Slider modelSlider;
	private Button menuButton;
	private Button modelButton;
	private Button zoomButton;
	private boolean explosionStarting = false;

	// Explosion shizzle
	private HE_Mesh[] voroMesh;
	private boolean explosionDone = false;
	private int numberOfParticles = 500;
	private boolean isExploding = false;
	private int move = 20;
	private Particle[] particles;
	private int exploCounter = 0;

	private int xPos, yPos;
	private float rotateDistanceX = 5;
	private float rotateDistanceY = 5;
	private int selectedModel = 2;
	private HE_Mesh[] startMeshes;
	private Button selectionButton;

	// Non-applet starting point
	//	static public void main(String args[])
	//	{
	//		PApplet.main(new String[] {"--display=1", "--present", "be.wide.main.MainProgram"});
	//	}

	public void setup()
	{
		//		Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
		//		size(scr.width, scr.height, OPENGL);
		size(1000, 800, OPENGL);
		background(bgColor);
		PFont font = createFont("Verdana", 30);
		textFont(font);
		textMode(SCREEN);
		text("Loading..", width/2 - 20, height/2);
		redraw();

		kinect = KinectController.getInstance();
		opencv = new OpenCVHandler(false);
		bpHandler = BodyPoseHandler.getInstance();
		gest = kinect.getGestureCont();
		//kinect.setToggleGesture(false);

		cam = new SphereCamera(SphereCamera.defaultEye, SphereCamera.defaultLook, SphereCamera.defaultUp);

		HEC_Box boxCreator = new HEC_Box(300, 300, 300, 5, 5, 5);

		mesh = new HE_Mesh(boxCreator);	
		render = new WB_Render(this);

		image = new PImage();
		modeString = "";
		selector = new HET_Selector(this);
		handImage = new PImage();
		handImage = loadImage(imagePath, "png");
		handImage.resize(handImage.width/3, handImage.height/3);

		poseImage = new PImage();
		poseImage = loadImage("../resources/pose.png", "png");


		selectedList = new HashMap<Integer, List<HE_Face>>();
		vertMoved = new ArrayList<HE_Vertex>();

		rotateSlider = new Slider(50, 200, 200, 50);
		rotateSlider.setVisible(false);
		modelSlider = new Slider(50, 200, 200, 50);
		modelSlider.setVisible(false);
		menuButton = new Button("NAVIGATION", 900, 200, 120, 1000);
		menuButton.setVisible(false);
		modelButton = new Button("MODELING", 900, 350, 120, 1000);
		modelButton.setVisible(false);
		zoomButton = new Button("ZOOM", 900, 500, 120, 1000);
		zoomButton.setVisible(false);
		selectionButton = new Button("SELECT", 900, 350, 120, 1000);
		selectionButton.setVisible(false);
		cam.zoomOut(400);
		cam.left(50);

		startMeshes = new HE_Mesh[5];
		loadMeshes();
		setObservers();

		particles = new Particle[numberOfParticles];
		for (int i = 0; i < particles.length; i++) 
		{
			PVector pos = new PVector(random(-40, 40), random(-40, 40), random(-40, 40));
			pos.limit(50);
			PVector v = new PVector();
			v.set(pos);
			v.normalize();
			v.mult(random(10));
			particles[i] = new Particle(pos, v);
		}
	}

	private void loadMeshes()
	{
		HEC_Geodesic geo = new HEC_Geodesic();
		geo.setRadius(300).setLevel(2); 
		HE_Mesh geoMesh = new HE_Mesh(geo);
		startMeshes[0] = geoMesh;

		HEC_Box boxCreator = new HEC_Box(400, 400, 400, 10, 10, 10);
		HE_Mesh boxMesh = new HE_Mesh(boxCreator);
		startMeshes[1] = boxMesh;

		HEC_Cone cone = new HEC_Cone().setFacets(8).setRadius(400).setHeight(600).setSteps(10);
		HE_Mesh coneMesh = new HE_Mesh(cone);
		coneMesh.rotateAboutAxis(90, new WB_Point3d(0, 0, 0), new WB_Normal3d(1, 0, 0));
		startMeshes[2] = coneMesh;

		HEC_Cylinder cyl = new HEC_Cylinder().setFacets(8).setHeight(600).setRadius(300).setSteps(10);
		HE_Mesh cylMesh = new HE_Mesh(cyl);
		startMeshes[3] = cylMesh;

		HEC_Dodecahedron dodec = new HEC_Dodecahedron().setEdge(300);
		HE_Mesh dodecMesh = new HE_Mesh(dodec);
		HES_PlanarMidEdge subdiv = new HES_PlanarMidEdge();
		dodecMesh.subdivide(subdiv, 2);
		startMeshes[4] = dodecMesh;
	}

	private void setObservers()
	{
		try {
			// SWIPES
			gest.getSwipeDetector().getSwipeLeftEvent().addObserver(new SwipeLeftObserver());
			gest.getSwipeDetector().getSwipeRightEvent().addObserver(new SwipeRightObserver());
			//gest.getSwipeDetector().getSwipeUpEvent().addObserver(new SwipeUpObserver());
			//gest.getSwipeDetector().getSwipeDownEvent().addObserver(new SwipeDownObserver());
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

			// BODY POSE
			pd = new BodyPoseDetector("x-pose");
			pd.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.RIGHT_OF, SkeletonJoint.LEFT_ELBOW);
			pd.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.LEFT_ELBOW);
			pd.addRule(SkeletonJoint.LEFT_HAND, BodyPoseRule.RIGHT_OF, SkeletonJoint.TORSO);
			pd.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.LEFT_OF, SkeletonJoint.RIGHT_ELBOW);
			pd.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.ABOVE, SkeletonJoint.RIGHT_ELBOW);
			pd.addRule(SkeletonJoint.RIGHT_HAND, BodyPoseRule.LEFT_OF, SkeletonJoint.TORSO);
			bpHandler.addPose(pd);

			bpHandler.addObserver(new BodyPoseObserver());

			// BUTTONS
			menuButton.addObserver(new ButtonObserver());
			modelButton.addObserver(new ButtonObserver());
			zoomButton.addObserver(new ButtonObserver());
			selectionButton.addObserver(new ButtonObserver());

		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	public void draw()
	{
		background(bgColor);
		smooth();
		lights();

		hint(ENABLE_DEPTH_TEST);
		translate(width/2, height/2, 0);
		rotateX(radians(180));

		xPos = 0;
		yPos = 0;

		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		if (kinect.isTracking())
		{

			if (!isExploding)
			{
				drawAxes(1000, true);
			}

			opencv.update();
			modeString = "";

			if (isChoosing)
			{
				selectionButton.setVisible(true);

				Hand right = opencv.getRightHandElement();
				if (right != null)
				{
					xPos = (int) OpenCVHandler.mapTo(kinect.convertRealWorldToProjective(right.getPosition()).getX(), 0, 640, 0, width);
					yPos = (int) OpenCVHandler.mapTo(kinect.convertRealWorldToProjective(right.getPosition()).getY(), 0, 480, 0, height);

					if (prevPos != null)
					{
						xPos = (int) lerp(xPos, prevPos.getX(), 0.3f);
						yPos = (int) lerp(yPos, prevPos.getY(), 0.3f);
					}

					selectionButton.update(new Point3D(xPos, yPos, 0));
				}

				fill(0, 255, 0);
				stroke(0);
				strokeWeight(2);
				render.drawEdges(startMeshes[selectedModel]);
				render.drawFaces(startMeshes[selectedModel]);
				modeString = "Swipe left/right to choose model";
			}
			else
			{

				Hand right = opencv.getRightHandElement();
				if (right != null)
				{
					xPos = (int) OpenCVHandler.mapTo(kinect.convertRealWorldToProjective(right.getPosition()).getX(), 0, 640, 0, width);
					yPos = (int) OpenCVHandler.mapTo(kinect.convertRealWorldToProjective(right.getPosition()).getY(), 0, 480, 0, height);

					if (prevPos != null)
					{
						xPos = (int) lerp(xPos, prevPos.getX(), 0.3f);
						yPos = (int) lerp(yPos, prevPos.getY(), 0.3f);
					}

					menuButton.update(new Point3D(xPos, yPos, 0));
					modelButton.update(new Point3D(xPos, yPos, 0));
					zoomButton.update(new Point3D(xPos, yPos, 0));
				}

				if (isNavigation)
				{
					drawNavigation();
				}
				else if (isZooming)
				{
					drawZooming();
				}
				else if (isModeling)
				{
					drawModeling();
				}
				else if (isExploding)
				{			
					drawExplosion();
				}
				else if (explosionStarting)
				{
					modeString = "Wait for it...";
				}

				if (right != null)
				{
					prevPos = right.getPosition();
				}

				if (!isExploding)
				{
					image = createSkelImage();
				}
			}
		}
		else
		{
			modeString = "Make the Psi-pose to start!";
		}

		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		// Normal draw if nothing is selected		
		if (!isModeling && !explosionDone && !isExploding && !isChoosing)
		{
			fill(0, 255, 0);
			stroke(0);
			strokeWeight(2);
			render.drawEdges(mesh);
			render.drawFacesSmooth(mesh);
			for (HE_Face face : mesh.getFacesAsList())
			{
				//System.out.println(face.key());
				if (face.key() <= 120)
				{
					fill(0);
				}
				else fill(0, 255, 0);
				render.drawFace(face);
			}
		}

		// 2D overlay
		hint(DISABLE_DEPTH_TEST);
		camera();
		noStroke();
		noFill();

		if (!isExploding && !explosionDone)
		{
			image(image, 0, 0);
			fill(255);
			PFont font = createFont("Verdana", 40);
			textFont(font);
			text(modeString, image.width + 30, 40);
			font = createFont("Verdana", 20);
			textFont(font);
			rotateSlider.draw(g);
			modelSlider.draw(g);
			menuButton.draw(g);
			modelButton.draw(g);
			zoomButton.draw(g);
			selectionButton.draw(g);

			if (!kinect.isTracking())
			{
				fill(0);
				stroke(0);
				image(poseImage, width/2 - poseImage.width/2, height/2 - poseImage.height/2);
			}
			else
			{
				fill(255);
				noStroke();
				image(handImage, xPos, yPos);
			}
		}

		if(explosionDone) System.exit(0);
	}

	private void drawNavigation()
	{
		Hand right = opencv.getRightHandElement();
		Hand left = opencv.getLeftHandElement();

		if (left != null && right != null && prevPos != null)
		{
			Point3D lproj = kinect.convertRealWorldToProjective(left.getPosition());
			rotateSlider.setCurrentSelection(lproj.getY(), height);
			if (right.isFist() && left.isFist())
			{
				Point3D rProj = kinect.convertRealWorldToProjective(right.getPosition());
				Point3D pProj = kinect.convertRealWorldToProjective(prevPos);

				float xDist = (OpenCVHandler.mapTo(rProj.getX(), 0, 640, 50, width - 50) 
						- OpenCVHandler.mapTo(pProj.getX(), 0, 640, 50, width - 50)) * rotateSlider.getSliderValue();
				float yDist = (OpenCVHandler.mapTo(rProj.getY(), 0, 480, 50, height - 50) 
						- OpenCVHandler.mapTo(pProj.getY(), 0, 480, 50, height - 50)) * rotateSlider.getSliderValue();

				xDist = xDist / 3;
				yDist = yDist / 3;

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

				rotateDistanceX = xDist/3;
				rotateDistanceY = yDist/3;
			}
			else
			{
				//				if (rotateDistanceX < 0)
				//				{
				//					cam.left(-rotateDistanceX);
				//				}
				//				else
				//				{
				//					cam.right(rotateDistanceX);
				//				}
				//				
				cam.left(-rotateDistanceX);

				//				if (rotateDistanceY < 0)
				//				{
				//					cam.up(rotateDistanceY);
				//				}
				//				else
				//				{
				//					cam.down(-rotateDistanceY);
				//				}
				cam.down(-rotateDistanceY);
			}
		}
	}

	private void drawZooming()
	{
		Hand right = opencv.getRightHandElement();
		Hand left = opencv.getLeftHandElement();

		if (left != null && right != null && prevPos != null)
		{
			Point3D lproj = kinect.convertRealWorldToProjective(left.getPosition());
			rotateSlider.setCurrentSelection(lproj.getY(), height);
			if (right.isFist() && left.isFist())
			{
				Point3D rProj = kinect.convertRealWorldToProjective(right.getPosition());
				Point3D pProj = kinect.convertRealWorldToProjective(prevPos);
				float zDist = (OpenCVHandler.mapTo(rProj.getZ(), 0, 480, 0, height) 
						- OpenCVHandler.mapTo(pProj.getZ(), 0, 480, 0, height))*rotateSlider.getSliderValue();

				if (zDist >= 0)
				{
					cam.zoomOut(zDist);
				}
				else
				{
					cam.zoomOut(zDist);
				}
			}
		}
	}

	private void drawModeling()
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
			Point3D lproj = kinect.convertRealWorldToProjective(left.getPosition());
			modelSlider.setCurrentSelection(lproj.getY(), height);
			int maxLvl = (int) OpenCVHandler.mapTo(modelSlider.getSliderValue(), 0, 1, 1, 8);

			if (selector.get(xPos, yPos) != null)
			{
				selected = mesh.getFaceByKey(selector.get(xPos, yPos));
			}

			Point3D rProj = kinect.convertRealWorldToProjective(right.getPosition());
			xPos = (int) OpenCVHandler.mapTo(rProj.getX(), 0, 640, 0, width);
			yPos = (int) OpenCVHandler.mapTo(rProj.getY(), 0, 480, 0, height);

			xPos = (int) lerp(xPos, prevPos.getX(), 0.3f);
			yPos = (int) lerp(yPos, prevPos.getY(), 0.3f);

			if(right.isFist() && left.isFist() && prevPos != null)
			{
				resetList();
				softSelection(selected, 1, maxLvl);

				Integer[] keyList = new Integer[maxLvl];
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
							if (f.key() <= 5) fill(0);
							render.drawFaceSmooth(f.key(), mesh);
						}
					}
				}
				double movement = (right.getPosition().getZ() - prevPos.getZ())/2;

				if (!selectedList.isEmpty() && selectedList.containsKey(1) && !selectedList.get(1).isEmpty())
				{
					WB_Normal3d normal = selectedList.get(1).get(0).getFaceNormal();

					vertMoved.clear();
					for (int i = 1; i <= keyList.length; ++i)
					{
						float mod = OpenCVHandler.mapTo(i, 1, 10, 1, 0.1f);
						moveFace(movement*mod, selectedList.get(i), normal);
					}
				}
			}
			else
			{
				resetList();
				softSelection(selected, 1, maxLvl);

				Integer[] keyList = new Integer[maxLvl];
				keyList = selectedList.keySet().toArray(keyList);
				if (!selectedList.isEmpty()){
					for (int i = 1; i <= keyList.length; ++i)
					{
						if (i == 1)
						{
							fill(0, 0, 255);
						}
						else
						{
							fill(132, 112, 255);
						}

						List<HE_Face> faces = selectedList.get(i);
						for (HE_Face f : faces)
						{
							if (f.key() <= 5) fill(0);
							render.drawFaceSmooth(f.key(), mesh);
						}
					}
				}
			}
			prevPos = right.getPosition();
		}
	}

	private void drawExplosion()
	{
		for ( int i = 0; i < voroMesh.length; i++) 
		{
			WB_Point3d c = voroMesh[i].getCenter();
			PVector d = new PVector( (float)c.x,(float)c.y,(float)c.z);
			d.normalize(); 
			d.mult((float) (move));
			voroMesh[i].move(d.x, d.y, d.z);
			fill(0, 255, 0);
			render.drawFaces(voroMesh[i]);
			stroke(0);
			render.drawEdges(voroMesh[i]);
			//move -= 0.01f;
		}

		for( int i =0; i < particles.length; i++) {
			particles[i].update();
			particles[i].draw(this);
		}
		exploCounter++;
		if (exploCounter >= 75)
		{
			explosionDone = true;
			isExploding = false;
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

	private void startExplosion()
	{
		if (!isExploding && !explosionDone)
		{
			explosionStarting = true;
			isNavigation = false;
			isModeling = false;
			isZooming = false;
			HEMC_VoronoiCells vc = new HEMC_VoronoiCells();
			vc.setContainer(mesh);
			float[][] points;

			int numpoints=50;
			points=new float[numpoints][3];
			for (int i=0;i<numpoints;i++) {
				points[i][0]=random(-80, 80);
				points[i][1]=random(-80, 80);
				points[i][2]=random(-80, 80);
			}
			vc.setPoints(points).setOffset(1);
			voroMesh = new HE_Mesh[50];
			modelButton.setVisible(false);
			menuButton.setVisible(false);
			rotateSlider.setVisible(false);
			modelSlider.setVisible(false);
			zoomButton.setVisible(false);

			try
			{
				voroMesh = vc.create();
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
				System.exit(0);
			}

			explosionStarting = false;
			isExploding = true;
		}
	}

	private void resetList()
	{
		selectedList.clear();
		List<HE_Face> fir = new ArrayList<HE_Face>();
		List<HE_Face> sec = new ArrayList<HE_Face>();
		List<HE_Face> tri = new ArrayList<HE_Face>();
		List<HE_Face> fou = new ArrayList<HE_Face>();
		List<HE_Face> fif = new ArrayList<HE_Face>();
		List<HE_Face> six = new ArrayList<HE_Face>();
		List<HE_Face> sev = new ArrayList<HE_Face>();
		List<HE_Face> eig = new ArrayList<HE_Face>();
		List<HE_Face> nin = new ArrayList<HE_Face>();
		List<HE_Face> ten = new ArrayList<HE_Face>();

		selectedList.put(1, fir);
		selectedList.put(2, sec);
		selectedList.put(3, tri);
		selectedList.put(4, fou);
		selectedList.put(5, fif);
		selectedList.put(6, six);
		selectedList.put(7, sev);
		selectedList.put(8, eig);
		selectedList.put(9, nin);
		selectedList.put(10, ten);
	}

	private void softSelection(HE_Face startFace, int lvl, int maxLvl)
	{
		// Recursive selection of neighboring faces
		if (startFace != null)
		{
			if (lvl <= maxLvl)
			{
				selectedList.get(lvl).add(startFace);		
				if (lvl >= 2)
				{
					if (selectedList.get(1).get(0) != null) // removes the middle/selected face from other lists for highlighting
					{
						selectedList.get(lvl).remove(selectedList.get(1).get(0));
					}
				}
				for (HE_Face face : startFace.getNeighborFaces())
				{
					softSelection(face, lvl + 1, maxLvl);
				}
			}
		}
	}

	public void drawAxes(int length, boolean multidir)
	{
		strokeWeight(1);
		stroke(0, 255, 0);
		line(0, 0, 0, length, 0, 0);
		stroke(255, 0, 0);
		line(0, 0, 0, 0, length, 0);
		stroke(0, 0, 255);
		line(0, 0, 0, 0, 0, length);

		if (multidir)
		{
			stroke(85);
			line(0, 0, 0, -length, 0, 0);
			line(0, 0, 0, 0, -length, 0);
			line(0, 0, 0, 0, 0, -length);
		}
		noStroke();
	}

	public PImage createSkelImage()
	{
		PGraphics temp = createGraphics(640, 480, P2D);
		temp.beginDraw();
		temp.background(0);
		temp.stroke(255);
		temp.fill(255);
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
			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
			p2 = userSkel.get(SkeletonJoint.LEFT_HIP).getPosition();
			p2 = kinect.convertRealWorldToProjective(p2);
			if (p1 != null && p2 != null) temp.line(p1.getX(), p1.getY(), p2.getX(), p2.getY());

			temp.noStroke();
			Hand left = opencv.getLeftHandElement();
			if (left != null)
			{
				if (left.isFist())
				{
					temp.fill(0, 255, 0);
				}
				else
				{
					temp.fill(255, 0, 0);
				}

				Point3D punt = kinect.convertRealWorldToProjective(left.getPosition());
				temp.ellipse(punt.getX(), punt.getY(), 50, 50);
			}
			Hand right = opencv.getRightHandElement();
			if (right != null)
			{
				if (right.isFist())
				{
					temp.fill(0, 255, 0);
				}
				else
				{
					temp.fill(255, 0, 0);
				}

				Point3D punt = kinect.convertRealWorldToProjective(right.getPosition());
				temp.ellipse(punt.getX(), punt.getY(), 50, 50);
			}

			temp.endDraw();
			ret = temp.get();
			ret.resize(160, 120);
		}
		return ret;
	}

	private void saveMesh()
	{
		HET_Export.saveToSTL(mesh, "../resources/screenCap.stl", 1);
	}

	//-------------------
	// OBSERVER CLASSES
	//-------------------

	/**
	 * Called when a left swipe is detected.
	 */
	public class SwipeLeftObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

			if (kinect.isTracking() && isChoosing)
			{
				selectedModel++;
				if (selectedModel > 4)
				{
					selectedModel = 0;
				}

			}
		}
	}

	/**
	 * Called when right swipe is detected.
	 */
	public class SwipeRightObserver implements IObserver<VelocityAngleEventArgs>
	{
		public void update(IObservable<VelocityAngleEventArgs> arg0, VelocityAngleEventArgs arg1) {

			if (kinect.isTracking() && isChoosing)
			{
				selectedModel--;
				if (selectedModel < 0)
				{
					selectedModel = 4;
				}
			}
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
			//	steady = true;
		}
	}

	/**
	 * Called when not steady gesture is detected.
	 */
	public class NotSteadyObserver implements IObserver<IdValueEventArgs>
	{
		public void update(IObservable<IdValueEventArgs> arg0,
				IdValueEventArgs arg1) {
			//	steady = false;
		}
	}

	public class BodyPoseObserver implements IObserver<BodyPoseEventArgs>
	{
		public void update(IObservable<BodyPoseEventArgs> arg0,
				BodyPoseEventArgs arg1) {
			System.out.println("body pose: " + arg1.getPose().getName());
			if (kinect.isTracking() && (isNavigation || isModeling || isZooming))
			{
				if (arg1.getPose() == pd)
				{
					saveMesh();
					startExplosion();
				}
			}
		}
	}

	public class ButtonObserver implements IObserver<ButtonEventArgs>
	{
		public void update(IObservable<ButtonEventArgs> arg0,
				ButtonEventArgs arg1) {

			if (kinect.isTracking())
			{
				if (arg1.getButton().getName().equals(modelButton.getName()))
				{
					System.out.println("modeling");
					isNavigation = false;
					isModeling = true;
					isZooming = false;
					rotateSlider.setVisible(false);
					modelSlider.setVisible(true);
					menuButton.setSelected(false);
					zoomButton.setSelected(false);
					bgColor = 200;
				}
				else if (arg1.getButton().getName().equals(menuButton.getName()))
				{
					System.out.println("navigation");
					isNavigation = true;
					isModeling = false;
					isZooming = false;
					rotateSlider.setVisible(true);
					modelSlider.setVisible(false);
					modelButton.setSelected(false);
					zoomButton.setSelected(false);
					bgColor = 125;
				}
				else if (arg1.getButton().getName().equals(zoomButton.getName()))
				{
					System.out.println("zooming");
					isZooming = true;
					isNavigation = false;
					isModeling = false;
					rotateSlider.setVisible(false);
					modelSlider.setVisible(false);
					modelButton.setSelected(false);
					menuButton.setSelected(false);
					bgColor = 175;
				}
				else if (arg1.getButton().getName().equals(selectionButton.getName()))
				{
					System.out.println("select");
					isChoosing = false;
					selectionButton.setVisible(false);
					zoomButton.setVisible(true);
					menuButton.setVisible(true);
					modelButton.setVisible(true);
					mesh = startMeshes[selectedModel];
				}
			}
		}
	}
}
