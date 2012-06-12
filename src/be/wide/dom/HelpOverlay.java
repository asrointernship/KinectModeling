package be.wide.dom;

import processing.core.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class HelpOverlay 
{
	private int xPos;
	private int yPos;
	private String[] text;
	private int width;
	private int height;
	private int alpha;
	private boolean visible;
	private boolean active;
	private PImage image;

	/**
	 * Standard constructor.
	 * Creates a helpOverlay object with specified values.
	 * @param x x-position
	 * @param y y-position
	 * @param w width of pop-up
	 * @param h height of pop-up
	 * @param text text to be displayed
	 * @param image icon
	 */
	public HelpOverlay(int x, int y, int w, int h, String[] text, PImage image)
	{
		setxPos(x);
		setyPos(y);
		setWidth(w);
		setHeight(h);
		setText(text);
		setAlpha(75);
		setVisible(false);
		setActive(false);
		setImage(image);
	}

	/**
	 * Draws the icon + pop-up on screen.
	 * @param gl screen canvas
	 */
	public void draw(PGraphics gl)
	{
		if (active)
		{
			if (visible)
			{
				gl.fill(0, 0, 0, alpha);
				gl.stroke(0, 0, 0, alpha);
				gl.textSize(24);
				gl.rect(xPos - width, yPos - height + image.height, width, height);
				gl.fill(255, 255, 255, alpha + 70);

				for(int i = 0; i < text.length; ++i)
				{
					gl.text(text[i], xPos - width + 10, yPos - height + image.height + 30 + i * 20);
				}
			}

			gl.noFill();
			gl.noStroke();
			gl.tint(255, 175);
			gl.image(image, xPos, yPos);
			gl.noTint();
		}
	}

	/**
	 * Checks if the specified x and y values are within the borders of the icon.
	 * @param x x value
	 * @param y y value
	 * @return true is between borders, false otherwise
	 */
	public boolean hitTest(int x, int y)
	{
		if (x >= xPos + 10 && x <= xPos + image.width - 10 && y >= yPos + 10 && y <= yPos + image.height - 10)
		{
			alpha = 128;
			return true;
		}
		alpha = 75;
		return false;
	}

	/**
	 * Gets the x position of the icon.
	 * @return x-position
	 */
	public int getxPos() {
		return xPos;
	}

	/**
	 * Sets the x position of the icon.
	 * @param xPos x-position
	 */
	public void setxPos(int xPos) {
		this.xPos = xPos;
	}

	/**
	 * Gets the y position of the icon.
	 * @return y-position
	 */
	public int getyPos() {
		return yPos;
	}

	/**
	 * Sets the y position of the icon.
	 * @param yPos y-position
	 */
	public void setyPos(int yPos) {
		this.yPos = yPos;
	}

	/**
	 * Gets the text to be displayed.
	 * @return text
	 */
	public String[] getText() {
		return text;
	}

	/**
	 * Sets the text to be displayed.
	 * @param text text
	 */
	public void setText(String[] text) {
		this.text = text;
	}

	/**
	 * Gets the width of the icon.
	 * @return width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Sets the width of the icon.
	 * @param width width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Gets the height of the icon.
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height of the icon.
	 * @param height height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Gets the transparency value.
	 * @return transparency value
	 */
	public int getAlpha() {
		return alpha;
	}

	/**
	 * Sets the transparency value
	 * @param alpha transparency value
	 */
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	/**
	 * Sees if the pop-up is visible.
	 * @return true if visible, false otherwise
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets if the pop-up is visible
	 * @param visible true if visible, false otherwise
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Sees if the icon is active (visible).
	 * @return true if active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the icon active
	 * @param active true if active, false otherwise
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets the image of the icon.
	 * @return image of icon
	 */
	public PImage getImage() {
		return image;
	}

	/**
	 * Sets the image of the icon.
	 * @param image image of icon
	 */
	public void setImage(PImage image) {
		this.image = image;
	}
}
