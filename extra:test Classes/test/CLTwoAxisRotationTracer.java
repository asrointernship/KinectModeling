package be.wide.test;

import org.OpenNI.Point3D;

import be.wide.controller.KinectController;
import be.wide.detector.OpenCVHandler;
import be.wide.dom.Hand;
import processing.core.*;

@SuppressWarnings("serial")
public class CLTwoAxisRotationTracer extends PApplet
{
	TwoAxisRotationTracer RotationTracer;

	public void setup(){

		size(500,500,P3D);
		ellipseMode(RADIUS);
		noFill();

		RotationTracer = new TwoAxisRotationTracer(width/2);

	}

	public void draw(){

		background(0);

		translate(width/2,height/2);

		rotateX(radians(RotationTracer.rx));
		rotateY(radians(RotationTracer.ry));

		RotationTracer.update();

		stroke(0,255,0);
		box(30);
		noStroke();
	}


	// Inner class
	public class TwoAxisRotationTracer {

		float sx,sy;
		float px,py;
		float rx,ry;
		float vx,vy;
		float prevX, prevY;

		int radius;
		boolean indicate;
		private OpenCVHandler cv;

		public TwoAxisRotationTracer(int _radius)
		{
			cv = new OpenCVHandler(false);
			radius = _radius;
			if (KinectController.getInstance().isTracking())
			{
				Hand right = cv.getRightHandElement();

				if (right != null)
				{
					Point3D rProj = KinectController.getInstance().convertRealWorldToProjective(right.getPosition());
					prevY = OpenCVHandler.mapTo(rProj.getY(), 0, 480, 0, height);
					prevX = OpenCVHandler.mapTo(rProj.getX(), 0, 640, 0, width);
				}
			}
		}

		public void update(){
			cv.update();
			if(indicate) render();
			rotation();
		}

		public void render(){
			drawXAxis();
			drawYAxis();
		}

		public void rotation(){
			px = rx;
			py = ry;
			rx += vy;
			ry += vx;
			if(ry > 360) ry = 0;
			else if( ry < 0) ry = 360;
			if(rx > 360) rx = 0;
			else if( rx < 0) rx = 360;

			float currX = 0;
			float currY = 0;
			if (KinectController.getInstance().isTracking())
			{
				Hand right = cv.getRightHandElement();
				if (right != null && right.isFist())
				{
					Point3D temp = KinectController.getInstance().convertRealWorldToProjective(right.getPosition());
					currY = OpenCVHandler.mapTo(temp.getY(), 0, 480, 0, height);
					currX = OpenCVHandler.mapTo(temp.getX(), 0, 640, 0, width);
					vy -= (currY-prevY) * 0.02;
					vx -= (currX-prevX) * 0.02;
					sy = ry;
					sx = rx;
					indicate = true;
				}
			}
			prevX = currX;
			prevY = currY;
			vx *= 0.95;
			vy *= 0.95;
			if(px == rx && py == ry) indicate = false;
		}

		public void drawXAxis(){
			pushMatrix();
			rotateY(-HALF_PI);

			stroke(255);
			strokeWeight(4);
			point(radius * (sin(radians(rx))), radius * (cos(radians(rx))));
			strokeWeight(1);

			pushMatrix();
			rotateY(PI);
			rotateZ(HALF_PI);
			stroke(0,0,255);
			arc(0, 0, radius, radius, radians(sx), radians(rx));
			arc(0, 0, radius, radius, radians(rx), radians(sx));
			popMatrix();
			popMatrix();
		}

		public void drawYAxis()
		{
			pushMatrix();
			rotateX(HALF_PI);

			stroke(255);
			strokeWeight(4);
			point(radius * (sin(radians(ry))), radius * (cos(radians(ry))));
			strokeWeight(1);

			pushMatrix();
			rotateY(PI);
			rotateZ(HALF_PI);
			stroke(255,0,0);
			arc(0, 0, radius, radius, radians(sy), radians(ry));
			arc(0, 0, radius, radius, radians(ry), radians(sy));
			popMatrix();
			popMatrix();
		}

	}
}


