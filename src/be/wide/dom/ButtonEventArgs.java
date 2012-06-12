package be.wide.dom;

import org.OpenNI.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class ButtonEventArgs extends EventArgs {

	private long time;
	private Button but;
	
	/**
	 * Standard constructor.
	 * @param button clicked button
	 * @param time time of click
	 */
	public ButtonEventArgs(Button button, long time)
	{
		setButton(button);
		setTime(time);
	}

	/**
	 * Gets the time of the click.
	 * @return time of click
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets the time of the click.
	 * @param time time of click
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Gets the clicked button.
	 * @return clicked button
	 */
	public Button getButton() {
		return but;
	}

	/**
	 * Sets the clicked button.
	 * @param but clicked button
	 */
	public void setButton(Button but) {
		this.but = but;
	}
}
