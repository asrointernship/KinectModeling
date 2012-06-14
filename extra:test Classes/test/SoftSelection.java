package be.wide.test;

import java.util.*;
import be.wide.controller.*;
import processing.core.*;
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;
import wblut.hemesh.modifiers.*;
import wblut.hemesh.tools.*;

public class SoftSelection extends PApplet
{

	// Camera
	private SphereCamera cam;
	// ------

	// Hemesh
	private HE_Mesh mesh;
	private WB_Render render;
	private HET_Selector selector;
	private HE_Face selected;
	// ------

	private HashMap<Integer, List<HE_Face>> selectedList;
	private List<HE_Vertex> vertMoved;
	private boolean isEdit = true;
	private int softSelLvl = 4;

	public void setup()
	{
		size(1200, 800, OPENGL);
		cam = new SphereCamera(SphereCamera.defaultEye, SphereCamera.defaultLook, SphereCamera.defaultUp);

		// Mesh
		render = new WB_Render(this);
		selector = new HET_Selector(this);
		selectedList = new HashMap<Integer, List<HE_Face>>();
		HEC_Geodesic geo = new HEC_Geodesic();
		geo.setRadius(200).setLevel(3); 

		HEC_Box box = new HEC_Box(200, 200, 200, 10, 10, 10);
		mesh = new HE_Mesh(geo);
//		HEM_Smooth smooth = new HEM_Smooth();
//		smooth.setIterations(5);
//		mesh.modify(smooth);
		vertMoved = new ArrayList<HE_Vertex>();
		cam.up(30);
	}

	public void draw()
	{
		background(125);
		smooth();
		lights();

		hint(ENABLE_DEPTH_TEST);
		translate(width/2, height/2, 0);
		rotateX(radians(180));

		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		if (isEdit)
		{
			stroke(0);
			strokeWeight(2);
			render.drawEdges(mesh);
			fill(0, 255, 0);
			render.drawFaces(selector, mesh);
			if (selector.get(mouseX, mouseY) != null)
			{
				softSelection(mesh.getFaceByKey(selector.get(mouseX, mouseY)));	
			}
		}
		else
		{
			fill(0, 0, 255);
			stroke(0);
			strokeWeight(1);
			render.drawEdges(mesh);
			render.drawFacesSmooth(mesh);
		}

	}


	public void mousePressed()
	{
		Integer[] keyList = new Integer[4];
		keyList = selectedList.keySet().toArray(keyList);
		int movement = 16;

		WB_Normal3d normal = selectedList.get(1).get(0).getFaceNormal();

		vertMoved.clear();
		for (int i = 1; i <= keyList.length; ++i)
		{
			moveFace(movement/i, selectedList.get(i), normal);
		}
		//mesh.collapseDegenerateEdges();
		//mesh.fuseCoplanarFaces();
		//mesh.triangulate();
	}
	
	public void keyPressed()
	{
		if (key == 'r')
		{
			isEdit = false;
		}
		if (key == 'f')
		{
			isEdit = true;
		}
	}

	private void moveFace(int move, List<HE_Face> faces, WB_Normal3d norm)
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
}







