package be.wide.main;

import java.util.*;

import be.wide.dom.AnimatedCircle;
import processing.core.PApplet;

public class AnimatedCircleTest extends PApplet
{

	private List<AnimatedCircle> ac;
	
	public void setup()
	{
		size(640, 480, OPENGL);
		ac = new ArrayList<AnimatedCircle>();
		frameRate(30);
		
		for (int i = 0; i < 30; ++i)
		{
			AnimatedCircle temp = new AnimatedCircle(width, height);
			ac.add(temp);
		}
	}
	
	public void draw()
	{
		background(0);
		
		for (AnimatedCircle anC : ac)
		{
			anC.draw(g);
		}
	}
}
