package be.wide.test;

import toxi.geom.*;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class CameraController 
{
	private Vec3D cameraEye;
	private Vec3D cameraLook;
	private Vec3D cameraUp;
	private Vec3D u, v, n;
	private float rotationSpeed = 2;
	private float panSpeed = 10;
	private float zoomSpeed = 10;
	
	// Default position of the camera
	public final static Vec3D defaultEye = new Vec3D(410, 280, 360);
	public final static Vec3D defaultLook = new Vec3D(400, 280, 50);
	public final static Vec3D defaultUp = new Vec3D(0, 1.0f, 0);
	
	/**
	 * Standard constructor.
	 * Creates a new CameraController object.
	 * @param eye position of the camera
	 * @param look position the camera is looking at
	 * @param up orientation of camera
	 */
	public CameraController(Vec3D eye, Vec3D look, Vec3D up)
	{
		setCameraEye(eye);
		setCameraLook(look);
		setCameraUp(up);
		
		n = new Vec3D(cameraLook);
		n.subSelf(cameraEye);
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
		cameraEye.set(defaultEye);
		cameraLook.set(defaultLook);
		cameraUp.set(defaultUp);
		
		n = new Vec3D(cameraLook);
		n.subSelf(cameraEye);
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

	private void verticalRotation(float rotSpeed)
	{
		Quaternion startQ = new Quaternion(0, cameraEye.x, cameraEye.y, cameraEye.z);
		float rot = (float) Math.cos(Math.toRadians(rotSpeed)/2f);
		Vec3D vec = new Vec3D((float) (Math.sin(Math.toRadians(rotSpeed)/2f) * u.x),
								(float) (Math.sin(Math.toRadians(rotSpeed)/2f) * u.y),
								(float) (Math.sin(Math.toRadians(rotSpeed)/2f) * u.z));
		Quaternion rotationQ = new Quaternion(rot, vec);
		Quaternion newPointQ = (rotationQ.multiply(startQ)).multiply(rotationQ.getConjugate());
		
		Vec3D newPQ = new Vec3D(newPointQ.x, newPointQ.y, newPointQ.z);
		cameraEye.set(newPQ);
		
		n = new Vec3D(cameraEye);
		n.subSelf(cameraLook);
		n.normalize();
		v = n.cross(u);
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
	
	private void horizontalRotation(float rotSpeed)
	{
		Quaternion startQ = new Quaternion(0, cameraEye.x, cameraEye.y, cameraEye.z);
		float rot = (float) Math.cos(Math.toRadians(rotSpeed)/2f);
		Vec3D vec = new Vec3D((float) (Math.sin(Math.toRadians(rotSpeed)/2f) * v.x),
								(float) (Math.sin(Math.toRadians(rotSpeed)/2f) * v.y),
								(float) (Math.sin(Math.toRadians(rotSpeed)/2f) * v.z));
		Quaternion rotationQ = new Quaternion(rot, vec);
		Quaternion newPointQ = (rotationQ.multiply(startQ)).multiply(rotationQ.getConjugate());
		
		Vec3D newPQ = new Vec3D(newPointQ.x, newPointQ.y, newPointQ.z);
		cameraEye.set(newPQ);
		n = new Vec3D(cameraLook);
		n.subSelf(cameraEye);
		u = cameraUp.cross(n);
		n.normalize();
		u.normalize();
	}
	
	/**
	 * Zooms the camera in.
	 */
	public void zoomIn(float zoomSpeed)
	{
		zoom(zoomSpeed);
	}
	
	/**
	 * Zooms the camera out.
	 */
	public void zoomOut(float zoomSpeed)
	{
		zoom(-zoomSpeed);
	}
	
	private void zoom(float val)
	{
		cameraEye.set(cameraEye.x + n.x * val, cameraEye.y + n.y * val, cameraEye.z + n.z * val);
	}
	
	/**
	 * Pans the camera left.
	 */
	public void panLeft()
	{
		panHorizontal(panSpeed);
	}
	
	/**
	 * Pans the camera right.
	 */
	public void panRight()
	{
		panHorizontal(-panSpeed);
	}
	
	private void panHorizontal(float val)
	{
		cameraLook.set(cameraLook.x + u.x * val, cameraLook.y + u.y * val, cameraLook.z + u.z * val);
		cameraEye.set(cameraEye.x + u.x * val, cameraEye.y + u.y * val, cameraEye.z + u.z * val);
	}
	
	/**
	 * Pans the camera upward.
	 */
	public void panUp()
	{
		panVertical(-panSpeed);
	}
	
	/**
	 * Pans the camera downward.
	 */
	public void panDown()
	{
		panVertical(panSpeed);
	}
	
	private void panVertical(float val)
	{
		cameraLook.set(cameraLook.x + v.x * val, cameraLook.y + v.y * val, cameraLook.z + v.z * val);
		cameraEye.set(cameraEye.x + v.x * val, cameraEye.y + v.y * val, cameraEye.z + v.z * val);
	}
	
	/**
	 * Gets the camera position (Eye).
	 * @return camera position
	 */
	public Vec3D getCameraEye() {
		return cameraEye;
	}
	
	/**
	 * Sets the camera position (Eye).
	 * @param cameraEye camera position
	 */
	public void setCameraEye(Vec3D cameraEye) {
		this.cameraEye = cameraEye;
	}
	
	/**
	 * Get the position the camera is looking at (Look).
	 * @return position the camera is looking at
	 */
	public Vec3D getCameraLook() {
		return cameraLook;
	}
	
	/**
	 * Sets the position the camera is looking at (Look).
	 * @param cameraLook position the camera is looking at.
	 */
	public void setCameraLook(Vec3D cameraLook) {
		this.cameraLook = cameraLook;
	}
	
	/**
	 * Gets the orientation of the camera (Up).
	 * @return camera orientation
	 */
	public Vec3D getCameraUp() {
		return cameraUp;
	}
	
	/**
	 * Sets the orientation of the camere (Up).
	 * @param cameraUp camera orientation
	 */
	public void setCameraUp(Vec3D cameraUp) {
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
		if (rotationSpeed <= 0) throw new NullPointerException("rotationSpeed cannot be lower of equal to zero");
		this.rotationSpeed = rotationSpeed;
	}
	
	/**
	 * Gets the speed with which the camera pans.
	 * @return pan speed
	 */
	public float getPanSpeed() {
		return panSpeed;
	}
	
	/**
	 * Sets the speed with which the camera pans.
	 * @param panSpeed pan speed
	 */
	public void setPanSpeed(float panSpeed) {
		if (panSpeed <= 0) throw new NullPointerException("panSpeed cannot be lower of equal to zero");
		this.panSpeed = panSpeed;
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
		if (zoomSpeed <= 0) throw new NullPointerException("zoomSpeed cannot be lower of equal to zero");
		this.zoomSpeed = zoomSpeed;
	}
}
