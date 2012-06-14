package be.wide.test;

import be.wide.controller.*;
import processing.core.*;
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;

@SuppressWarnings("serial")
public class MeshExplosion extends PApplet
{
	private HE_Mesh mesh;
	private Particle[] particles;
	private WB_Render render;
	private boolean isExploding = false;
	private int move = 50;
	private SphereCamera cam;
	private HE_Mesh[] voroMesh;
	private boolean explosionDone = false;
	private int numberOfParticles = 300;

	public void setup()
	{
		randomSeed( 10 );
		size(640, 480, OPENGL);

		HEC_Geodesic geo = new HEC_Geodesic();
		geo.setRadius(100).setLevel(2); 
		HEC_Box box = new HEC_Box(200, 200, 200, 10, 10, 10);
		mesh = new HE_Mesh(geo);

		render = new WB_Render(this);
		cam = new SphereCamera(SphereCamera.defaultEye, SphereCamera.defaultLook, SphereCamera.defaultUp);

		HEMC_VoronoiCells vc = new HEMC_VoronoiCells();
		vc.setContainer( mesh );
		float[][] points;

		int numpoints=50;
		points=new float[numpoints][3];
		for (int i=0;i<numpoints;i++) {
			points[i][0]=random(-80, 80);
			points[i][1]=random(-80, 80);
			points[i][2]=random(-80, 80);
		}
		vc.setPoints(points).setOffset(1);
		voroMesh = vc.create();

		particles = new Particle[numberOfParticles];
		for( int i =0; i < particles.length; i++) {
			PVector pos = new PVector( random(-40,40), random( -40,40), random( -40,40));
			pos.limit(50);
			PVector v = new PVector();
			v.set(pos);
			v.normalize();
			v.mult(random(10));
			particles[i] = new Particle( pos, v );
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

		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

		if (isExploding)
		{			
			for ( int i =0; i < voroMesh.length; i++) 
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
				
				if (move >= 2)
				{
					move -= 0.01f;
				}
			}

			for( int i =0; i < particles.length; i++) {
				particles[i].update();
				particles[i].draw(this);
				if (particles[i].ttl == 0)
				{
					isExploding = false;
					explosionDone = true;
				}
			}
		}
		else
		{
			if (!explosionDone)
			{
				fill(0, 255, 0);
				render.drawEdges(mesh);
				stroke(0);
				render.drawFaces(mesh);
			}
		}

		camera(cam.getCameraEye().x, cam.getCameraEye().y, cam.getCameraEye().z, 
				cam.getCameraLook().x, cam.getCameraLook().y, cam.getCameraLook().z, 
				cam.getCameraUp().x, cam.getCameraUp().y, cam.getCameraUp().z);

	}

	private void reset()
	{
		if (!isExploding && explosionDone)
		{
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
			voroMesh = vc.create();

			particles = new Particle[numberOfParticles];
			for( int i =0; i < particles.length; i++) {
				PVector pos = new PVector(random(-40, 40), random(-40, 40), random(-40, 40));
				pos.limit(50);
				PVector v = new PVector();
				v.set(pos);
				v.normalize();
				v.mult(random(10));
				particles[i] = new Particle(pos, v);
			}
			explosionDone = false;
		}
	}

	public void mousePressed()
	{
		if (!isExploding)
		{
			startExplosion();
		}
	}
	
	public void keyPressed()
	{
		if (key == 'r')
		{
			reset();
		}
	}

	private void startExplosion()
	{
		if (!isExploding && !explosionDone)
		{
			isExploding = true;
		}
	}
}
