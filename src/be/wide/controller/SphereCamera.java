package be.wide.controller;

import be.wide.dom.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class SphereCamera 
{
	private Point3 cameraEye;
	private Point3 cameraLook;
	private Vector3 cameraUp;
	private Vector3 u, v, n;
	private float rotationSpeed = 10;
	private float zoomSpeed = 10;

	// Default position of the camera
	public final static Point3 defaultEye = new Point3(600, 0, 0);
	public final static Point3 defaultLook = new Point3(0, 0, 0);
	public final static Vector3 defaultUp = new Vector3(0, 1.0f, 0);

	/**
	 * Standard constructor.
	 * Creates a new CameraController object.
	 * @param eye position of the camera
	 * @param look position the camera is looking at
	 * @param up orientation of camera
	 */
	public SphereCamera(Point3 eye, Point3 look, Vector3 up)
	{
		setCameraEye(eye);
		setCameraLook(look);
		setCameraUp(up);

		n = new Vector3(cameraLook, cameraEye);
		u = cameraUp.cross(n);
		n.normalize();
		u.normalize();
		v = n.cross(u);
	}

	/**
	 * Resets the camera to default position.
	 */
	public void reset()
	{
		setCameraEye(defaultEye);
		setCameraLook(defaultLook);
		setCameraUp(defaultUp);

		n = new Vector3(cameraLook, cameraEye);
		u = cameraUp.cross(n);
		n.normalize();
		u.normalize();
		v = n.cross(u);
	}

	/**
	 * Rotate the camera upward.
	 */
	public void up(float rotSpeed)
	{
		verticalRotation(-rotSpeed);
	}

	/**
	 * Rotate the camera downward.
	 */
	public void down(float rotSpeed)
	{
		verticalRotation(rotSpeed);
	}

	/**
	 * Rotate the camera vertical.
	 * @param rotSpeed speed and direction of rotation
	 */
	public void verticalRotation(float rotSpeed)
	{
		Quaternion startQ = new Quaternion(cameraEye);
		Quaternion rotationQ = new Quaternion(rotSpeed, u);
		Quaternion newPointQ = (rotationQ.product(startQ)).product(rotationQ.conjugate());
		
		cameraEye.set(newPointQ.b, newPointQ.c, newPointQ.d);
		n = new Vector3(cameraLook, cameraEye);
		n.normalize();
		v = n.cross(u);
		cameraUp = v;
	}

	/**
	 * Rotates the camera to the left.
	 */
	public void left(float rotSpeed)
	{
		horizontalRotation(-rotSpeed);
	}

	/**
	 * Rotates the camera to the right.
	 */
	public void right(float rotSpeed)
	{
		horizontalRotation(rotSpeed);
	}

	/**
	 * Rotate the camera horizontal.
	 * @param rotSpeed speed and direction of rotation
	 */
	public void horizontalRotation(float rotSpeed)
	{
		Quaternion startQ = new Quaternion(cameraEye);
		Quaternion rotationQ = new Quaternion(rotSpeed, v);
		Quaternion newPointQ = (rotationQ.product(startQ)).product(rotationQ.conjugate());
		
		cameraEye.set(newPointQ.b, newPointQ.c, newPointQ.d);
		n = new Vector3(cameraLook, cameraEye);
		n.normalize();
		u = cameraUp.cross(n);
		u.normalize();
	}

	/**
	 * Zooms the camera in.
	 */
	public void zoomIn(float zoom)
	{
		zoom(zoom);
	}

	/**
	 * Zooms the camera out.
	 */
	public void zoomOut(float zoom)
	{
		zoom(-zoom);
	}

	private void zoom(float val)
	{
		cameraEye.set(cameraEye.x + n.x * val, cameraEye.y + n.y * val, cameraEye.z + n.z * val);
	}

	/**
	 * Gets the camera position (Eye).
	 * @return camera position
	 */
	public Point3 getCameraEye() {
		return cameraEye;
	}

	/**
	 * Sets the camera position (Eye).
	 * @param cameraEye camera position
	 */
	public void setCameraEye(Point3 cameraEye) {
		this.cameraEye = cameraEye;
	}

	/**
	 * Get the position the camera is looking at (Look).
	 * @return position the camera is looking at
	 */
	public Point3 getCameraLook() {
		return cameraLook;
	}

	/**
	 * Sets the position the camera is looking at (Look).
	 * @param cameraLook position the camera is looking at.
	 */
	public void setCameraLook(Point3 cameraLook) {
		this.cameraLook = cameraLook;
	}

	/**
	 * Gets the orientation of the camera (Up).
	 * @return camera orientation
	 */
	public Vector3 getCameraUp() {
		return cameraUp;
	}

	/**
	 * Sets the orientation of the camere (Up).
	 * @param cameraUp camera orientation
	 */
	public void setCameraUp(Vector3 cameraUp) {
		this.cameraUp = cameraUp;
	}

	/**
	 * Gets the speed with which the camera rotates.
	 * @return rotate speed
	 */
	public float getRotationSpeed() {
		return rotationSpeed;
	}

	/**
	 * Sets the speed with which the camera rotates.
	 * @param rotationSpeed rotate speed
	 */
	public void setRotationSpeed(float rotationSpeed) {
		if (rotationSpeed <= 0) throw new NullPointerException("rotationSpeed cannot be lower or equal to zero");
		this.rotationSpeed = rotationSpeed;
	}

	/**
	 * Gets the speed with which the camera zooms.
	 * @return zoom speed
	 */
	public float getZoomSpeed() {
		return zoomSpeed;
	}

	/**
	 * Sets the speed with which the camera zooms.
	 * @param zoomSpeed zoom speed
	 */
	public void setZoomSpeed(float zoomSpeed) {
		if (zoomSpeed <= 0) throw new NullPointerException("zoomSpeed cannot be lower or equal to zero");
		this.zoomSpeed = zoomSpeed;
	}
}
