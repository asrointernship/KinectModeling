package be.wide.test;

import be.wide.controller.*;
import processing.core.*;
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;

@SuppressWarnings("serial")
public class NavigationTest extends PApplet
{
	private SphereCamera cam;
	private HE_Mesh mesh;
	private WB_Render render;
	private HE_Mesh outerSphere;
	
	public void setup()
	{
		size(800, 600, P3D);
		cam = new SphereCamera(SphereCamera.defaultEye, SphereCamera.defaultLook, SphereCamera.defaultUp);
		
		HEC_Box boxCreator = new HEC_Box(100, 100, 100, 1, 1, 1);

		mesh = new HE_Mesh(boxCreator);	
		render = new WB_Render(this);
	}
	
	public void draw()
	{
		background(125);
		smooth();
		translate(width/2, height/2, 0);
		rotateX(radians(180));
		
		//drawAxes(1000, true);
		
		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);
		
		strokeWeight(1);
		stroke(255);
		fill(0);
		render.drawFaces(mesh);
		
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
		stroke(0, 0, 0);
		strokeWeight(0.3f);
		render.drawEdges(outerSphere);
		popMatrix();
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
		noStroke();
	}
	
	public void keyPressed()
	{
		if (key == 'w')
		{
			cam.up(1);
		}
		if (key == 's')
		{
			cam.down(1);
		}
		if (key == 'a')
		{
			cam.left(1);
		}
		if (key == 'd')
		{
			cam.right(1);
		}
		if (key == 'f')
		{
			cam.zoomIn(5);
		}
		if (key == 'g')
		{
			cam.zoomOut(5);
		}
	}
}
