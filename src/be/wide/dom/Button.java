package be.wide.dom;

import java.util.*;
import org.OpenNI.*;
import be.wide.detector.*;
import processing.core.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class Button implements IObservable<ButtonEventArgs> {

	private String name;
	private int xPos;
	private int yPos;
	private int diameter;
	private int duration;
	private boolean isVisible = true;
	private ArrayList<IObserver<ButtonEventArgs>> observers;
	private boolean clicked;
	private Point3D myPos;
	private boolean active;
	private long currTime = -1;
	private int counter;
	private int maxCount = 30;
	private boolean isSelected = false;
	private float alpha = 128;
	private PImage icon;


	/**
	 * Standard constructor.
	 * Creates a button with a specified name and default positioning.
	 * @param name String
	 */
	public Button(String name)
	{
		this(name, 100, 100, 50, 3000, null);
	}

	/**
	 * Overloaded constructor.
	 * Creates a button with specified name and positioning.
	 * @param name name of button
	 * @param x x-position
	 * @param y y-position
	 * @param r radius
	 * @param time click duration
	 */
	public Button(String name, int x, int y, int r, int time, PImage icon)
	{
		setXPos(x);
		setYPos(y);
		setDiameter(r);
		setDuration(time);
		setName(name);
		setIcon(icon);
		observers = new ArrayList<IObserver<ButtonEventArgs>>();
		myPos = new Point3D(x, y, 0);
		maxCount = time/1000 * 30;
	}

	/**
	 * Draws the button on the specified canvas.
	 * @param gl PGraphics canvas
	 */
	public void draw(PGraphics gl)
	{
		if (isVisible)
		{
			if (!isSelected)
			{
				gl.strokeWeight(3);
				gl.stroke(0, 0, 0, alpha);
				gl.fill(0, 0, 255, alpha);
				gl.ellipse(xPos, yPos, diameter, diameter);
				float innerDiameter = DetectionHandler.mapTo(counter, 0, maxCount, 1, diameter);
				gl.fill(0, 255, 0, alpha);
				gl.noStroke();
				gl.ellipse(xPos, yPos, innerDiameter, innerDiameter);

				if (icon == null)
				{
					if (active) gl.fill(0);
					else gl.fill(255);
					gl.textSize(14);
					int len = name.length();
					int offset = (int) DetectionHandler.mapTo(len, 2, 15, 40, 5);
					gl.text(name, xPos - diameter/2 + offset, yPos + 8);
				}
				else
				{
					gl.noFill();
					gl.tint(255, alpha);
					gl.image(icon, xPos - icon.width/2, yPos - icon.height/2);
					gl.noTint();
				}
			}
			else
			{
				gl.strokeWeight(3);
				gl.stroke(0, 0, 0, alpha);
				gl.fill(0, 255, 0, alpha);
				gl.ellipse(xPos, yPos, diameter, diameter);

				if (icon == null)
				{
					int len = name.length();
					int offset = (int) DetectionHandler.mapTo(len, 2, 15, 40, 5);
					gl.fill(0);
					gl.textSize(14);
					gl.text(name, xPos - diameter/2 + offset, yPos + 8);
				}
				else
				{
					gl.noFill();
					gl.tint(255, alpha);
					gl.image(icon, xPos - icon.width/2, yPos - icon.height/2);
					gl.noTint();
				}
			}
		}
		return;
	}

	/**
	 * Updates the button depending on the handposition.
	 * @param handPos handposition
	 */
	public void update(Point3D handPos)
	{
		if (isVisible)
		{
			float dist = (float) Math.sqrt(((handPos.getX() - myPos.getX())*(handPos.getX() - myPos.getX())) 
					+ ((handPos.getY() - myPos.getY())*(handPos.getY() - myPos.getY())));

			if (dist <= diameter/2)
			{
				active = true;
			}
			else
			{
				active = false;
				currTime = -1;
				counter = 0;
			}

			if (active)
			{
				if (currTime == -1)
				{
					counter = 0;
				}
				else
				{
					counter++;
				}
				currTime = System.currentTimeMillis();
				if (counter >= maxCount)
				{
					counter = 0;
					currTime = -1;
					clicked = true;
				}
			}
			if (clicked)
			{
				ButtonEventArgs args = new ButtonEventArgs(this, System.currentTimeMillis());
				for (IObserver<ButtonEventArgs> obs : observers)
				{
					obs.update(this, args);
				}
				clicked = false;
				isSelected = true;
			}
		}
	}

	/**
	 * Gets the x-position of the button.
	 * @return x-position
	 */
	public int getXPos() {
		return xPos;
	}

	/**
	 * Sets the x-position of the button.
	 * @param xpos x-position
	 */
	public void setXPos(int xpos) {
		this.xPos = xpos;
	}

	/**
	 * Gets the y-position of the button
	 * @return y-position
	 */
	public int getYPos() {
		return yPos;
	}

	/**
	 * Sets the y-position of the button
	 * @param ypos y-position
	 */
	public void setYPos(int ypos) {
		this.yPos = ypos;
	}

	/**
	 * Gets the radius of the button-circle.
	 * @return radius
	 */
	public int getDiameter() {
		return diameter;
	}

	/**
	 * Sets the radius of the button-circle.
	 * @param diameter radius
	 */
	public void setDiameter(int diameter) {
		this.diameter = diameter;
	}

	/**
	 * Gets the duration of a click.
	 * @return duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Sets the duration of a click.
	 * @param length duration
	 */
	public void setDuration(int length) {
		this.duration = length;
	}

	/**
	 * Sets the name of the button
	 * @param name name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the button.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sees if the button is visible.
	 * @return true button visible, false otherwise
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Sets the visibility of the button.
	 * @param isVisible true button visible, false otherwise
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * Sees if the button is selected.
	 * @return true if selected, false otherwise
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Sets if the button is selected.
	 * @param isSelected true if selected, false otherwise
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	/**
	 * Gets the transparency of the button.
	 * @return transparency value
	 */
	public float getAlpha() {
		return alpha;
	}

	/**
	 * Sets the transparency of the button.
	 * @param alpha transparency value
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	/**
	 * Gets the icon of the button as PImage
	 * @return icon image
	 */
	public PImage getIcon() {
		return icon;
	}

	/**
	 * Sets the icon of the button as PImage
	 * @param icon image
	 */
	public void setIcon(PImage icon) {
		this.icon = icon;
	}

	/**
	 * Adds an observer to the observer list.
	 * Register an observer to receive updates of this class.
	 */
	public void addObserver(IObserver<ButtonEventArgs> arg0)
	throws StatusException {
		if (arg0 != null)
		{
			observers.add(arg0);
		}
	}

	/**
	 * Removes an observer of the observer list.
	 * Unregister an observer to receive updates of this class.
	 */
	public void deleteObserver(IObserver<ButtonEventArgs> arg0) {

		if (arg0 != null && observers.contains(arg0))
		{
			observers.remove(arg0);
		}
	}
}
