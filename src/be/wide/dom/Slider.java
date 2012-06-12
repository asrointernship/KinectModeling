package be.wide.dom;

import be.wide.detector.*;
import processing.core.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class Slider 
{
	private float xPos;
	private float yPos;
	private float myHeight;
	private float myWidth;
	private float currentSelection;
	private float currentValue;
	private boolean isVisible = true;
	private float alpha = 70;

	/**
	 * Standard constructor.
	 * Creates a slider object with default values.
	 */
	public Slider()
	{
		this(0, 0, 100, 20);
	}

	/**
	 * Overloaded constructor.
	 * Creates a slider object with specified values.
	 * @param x x-position
	 * @param y y-position
	 * @param h height
	 * @param w width
	 */
	public Slider(int x, int y, int h, int w)
	{
		setxPos(x);
		setyPos(y);
		setMyHeight(h);
		setMyWidth(w);
		setCurrentSelection(0, 10);
	}

	/**
	 * Test if a specified position overlaps with the slider.
	 * @param x x-position
	 * @param y y-position
	 * @return true if overlaps, false otherwise
	 */
	public boolean hitTest(int x, int y)
	{
		if (x <= xPos + myWidth && x >= xPos && y <= yPos + myHeight && y >= yPos)
		{
			return true;
		}
		return false;
	}

	/**
	 * Draw the slider to the specified canvas.
	 * @param gl PGraphics canvas
	 */
	public void draw(PGraphics gl)
	{
		if (isVisible)
		{
			gl.strokeWeight(3);
			gl.stroke(0, 0, 0, alpha);
			gl.fill(0, 0, 255, alpha);
			gl.rect(xPos, yPos, myWidth, myHeight);
			gl.fill(0, 255, 0, alpha);
			float newh = myHeight - (currentValue - yPos);
			gl.rect(xPos, currentValue, myWidth, newh);
			gl.noStroke();
			gl.noFill();
		}
		return;
	}

	/**
	 * Gets the x-position of the slider.
	 * @return x-position
	 */
	public float getxPos() {
		return xPos;
	}
	
	/**
	 * Sets the x-position of the slider.
	 * @param xPos x-position
	 */
	public void setxPos(float xPos) {
		this.xPos = xPos;
	}
	
	/**
	 * Gets the y-position of the slider.
	 * @return y-position
	 */
	public float getyPos() {
		return yPos;
	}
	
	/**
	 * Sets the y-position of the slider.
	 * @param yPos y-position
	 */
	public void setyPos(float yPos) {
		this.yPos = yPos;
	}
	
	/**
	 * Gets the height of the slider.
	 * @return height
	 */
	public float getMyHeight() {
		return myHeight;
	}
	
	/**
	 * Sets the height of the slider
	 * @param myHeight height
	 */
	public void setMyHeight(float myHeight) {
		this.myHeight = myHeight;
	}
	
	/**
	 * Gets the width of the slider
	 * @return width
	 */
	public float getMyWidth() {
		return myWidth;
	}
	
	/**
	 * Sets the width of the slider
	 * @param myWidth width
	 */
	public void setMyWidth(float myWidth) {
		this.myWidth = myWidth;
	}
	
	/**
	 * Gets the current selection.
	 * @return current selection
	 */
	public float getCurrentSelection()
	{
		return this.currentSelection;
	}

	/**
	 * Sets the current selection and calculates the current value.
	 * @param currentSelection current value of selection
	 * @param screenHeight height of the screen
	 */
	public void setCurrentSelection(float currentSelection, int screenHeight) 
	{
		if (currentSelection >= 0 && currentSelection <= screenHeight)
		{
			this.currentSelection = currentSelection;
			this.currentValue = DetectionHandler.mapTo(currentSelection, 100, screenHeight, yPos, yPos + myHeight + 300);
			if (this.currentValue >= yPos + myHeight)
			{
				this.currentValue = yPos + myHeight;
			}
			else if (this.currentValue <= yPos)
			{
				this.currentValue = yPos;
			}
		}
	}

	/**
	 * Gets the current value (between 0 - 1)
	 * @return current value
	 */
	public float getCurrentValue() {
		return currentValue;
	}

	/**
	 * Sets the current value.
	 * @param currentValue current value
	 */
	public void setCurrentValue(float currentValue) {
		this.currentValue = currentValue;
	}

	/**
	 * Sees if the slider is visible.
	 * @return true if visible, false otherwise
	 */
	public boolean isVisible() {
		return isVisible;
	}

	/**
	 * Sets the visibility of the slider.
	 * @param isVisible true if visible, false otherwise
	 */
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
	/**
	 * Gets the calculated value of the slider.
	 * @return value
	 */
	public float getSliderValue()
	{
		float dist = yPos + myHeight - currentValue;
		float temp = dist/myHeight;
		return temp;
	}

	/**
	 * Sets the transparency of the slider.
	 * @param alpha transparency value
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	/**
	 * Gets the transparency of the slider
	 * @return transparency value
	 */
	public float getAlpha() {
		return alpha;
	}
}
