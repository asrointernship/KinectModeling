package be.wide.test;
import processing.core.*;
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;

@SuppressWarnings("serial")
public class ExplosionHemesh extends PApplet{

	private HE_Mesh sp;
	private HE_Mesh[] mesh;
	private WB_Render render;
	private int mode = 0;
	private Particle[] particles;

	public void setup() 
	{
		randomSeed( 10 );
		size(800, 400, OPENGL);
		render = new WB_Render(this);
		HEC_Creator creator = new HEC_Sphere().setRadius(100).setUFacets(60).setVFacets(60);
		sp = new HE_Mesh( creator );  

		HEMC_VoronoiCells vc = new HEMC_VoronoiCells();
		vc.setContainer( sp );
		float[][] points;

		int numpoints=50;
		points=new float[numpoints][3];
		for (int i=0;i<numpoints;i++) {
			points[i][0]=random(-80, 80);
			points[i][1]=random(-80, 80);
			points[i][2]=random(-80, 80);
		}
		vc.setPoints(points).setOffset(1);
		mesh = vc.create();

		particles = new Particle[1000];
		for( int i =0; i < particles.length; i++) {
			PVector pos = new PVector( random(-40,40), random( -40,40), random( -40,40));
			pos.limit(50);
			PVector v = new PVector();
			v.set(pos);
			v.normalize();
			v.mult(random(10));
			particles[i] = new Particle( pos, v );
		}
		smooth();
	}

	public void draw() 
	{
		background(0);
		translate( width/2, height/2);
		camera( 0, 0, map( frameCount, 0, 500, 1000, 300), 0, map( frameCount, 0, 500, -200, 0), 0, 0, 1, 0);

		directionalLight(255, 255, 255, 1, 1, 0);
		if ( frameCount < 300 ) { 
			mode = 0;
		} else {
			mode = 1;
		}
		if ( mode == 0 ) {
			fill(100);
			for ( int i =0; i < mesh.length; i++) {
				noStroke();
				if (mesh[i] != null)
				{
					render.drawFaces(mesh[i]);
				}
			}
			ambientLight(255, 255, 255);
			pushMatrix();
			scale(0.98f);
			fill( map( frameCount, 0, 255, 0, 255), map( frameCount, 128, 512, 0, 255), map( frameCount, 256, 512, 0, 255));
			render.drawFaces(sp);
			popMatrix();
		} else {

			float tr = map( frameCount -500, 0, 20, 255, 0 );
			fill(255, tr );

			render.drawFaces(sp);

			fill(100);
			for ( int i =0; i < mesh.length; i++) {

				pushMatrix();

				WB_Point3d c = mesh[i].getCenter();
				PVector d = new PVector( (float)c.x,(float)c.y,(float)c.z);
				d.normalize(); 
				d.mult( frameCount - 550 );
				translate( d.x, d.y, d.z );
				noStroke();
				render.drawFaces(mesh[i]);
				popMatrix();
			}
			for( int i =0; i < particles.length; i++) {
				particles[i].update();
				particles[i].draw(this);
			}  
		}
	}
}
