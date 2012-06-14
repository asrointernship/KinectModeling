package be.wide.test;

import be.wide.controller.*;
import be.wide.dom.*;
import processing.core.*;
import wblut.geom.core.*;

@SuppressWarnings("serial")
public class HemeshTest extends PApplet
{
	private WB_Point3d transVector;
	private float xRotate;
	private float yRotate;
	private HemeshController mesh;
	private CameraController cam;

	public void setup()
	{
		size(800, 600, P3D);
		transVector = new WB_Point3d(width/5, height - height/4, 0);
		xRotate = radians(180);
		yRotate = radians(15);
		mesh = new HemeshController(this);
		cam = new CameraController(CameraController.defaultEye, CameraController.defaultLook, CameraController.defaultUp);
	}

	public void draw()
	{
		// Clear background, enable lights, smooth lines
		background(150, 150, 150);
		//lights();
		smooth();

		hint(ENABLE_DEPTH_TEST);

		// Turn and move scene so it overlaps with kinect scene
		translate((float) transVector.x, (float) transVector.y, (float) transVector.z);
		rotateX(xRotate);
		rotateY(yRotate);

		// Camera position
		pushMatrix();
		rotateX(radians(-180));
		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);
		popMatrix();

		// Draw mesh
		drawAxes(1000, true);

		mesh.drawMesh(mouseX, mouseY, HemeshController.SELECT_VERTEX);
		
		if (mousePressed)
		{
			MeshUpdateInformation info = new MeshUpdateInformation();
			info.setDistance(50);
			info.setFace(mesh.getMesh().getFaceByKey(mesh.getSelectedId()));
			ModelStrategy strat = new ExtrudeModelStrategy(mesh);
			strat.updateMesh(info);
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
			stroke(120, 120, 120);
			line(0, 0, 0, -length, 0, 0);
			line(0, 0, 0, 0, -length, 0);
			line(0, 0, 0, 0, 0, -length);
		}
		noStroke();
	}


	// CAMERA TEST CONTROLLS
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
			cam.left(10);
		}
		if (key == 'd')
		{
			cam.right(10);
		}
		if (key == 'f')
		{
			cam.zoomIn(5);
		}
		if (key == 'g')
		{
			cam.zoomOut(5);
		}
		if (key == 'q')
		{
			cam.panLeft();
		}
		if (key == 'e')
		{
			cam.panRight();
		}
		if (key == 'z')
		{
			cam.panUp();
		}
		if (key == 'x')
		{
			cam.panDown();
		}
		if (key == 'r')
		{
			cam.reset();
		}
	}
}
