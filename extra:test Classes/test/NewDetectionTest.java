package be.wide.main;

import org.OpenNI.*;

import be.wide.controller.*;
import be.wide.detector.*;
import processing.core.*;

public class NewDetectionTest extends PApplet
{

	private KinectController kinect;
	private DetectionHandler dh;
	
	
	public void setup()
	{
		size(640, 480);
		kinect = KinectController.getInstance();
		dh = new DetectionHandler();
		try {
			dh.addObserver(new HandObserver());
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}
	
	public void draw()
	{
		dh.update();
	}
	
	class HandObserver implements IObserver<HandDetectionEventArgs>
	{

		@Override
		public void update(IObservable<HandDetectionEventArgs> arg0,
				HandDetectionEventArgs arg1) {
			
			if (arg1.getHand().equals("left"))
			{
				if (arg1.isFist())
				{
					System.out.println("left fist");
				}
			}
			else if (arg1.getHand().equals("right"))
			{
				if (arg1.isFist())
				{
					System.out.println("right fist -- ");
				}
			}
		}
		
	}
	
}
