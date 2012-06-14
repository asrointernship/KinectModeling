package be.wide.test;

import wblut.geom.core.*;
import wblut.hemesh.core.*;

public class MeshUpdateInformation 
{
	private HE_Face myFace;
	private HE_Edge myEdge;
	private HE_Vertex[] myVertices;
	private WB_Point3d rotateOrigin;
	private WB_Vector3d rotateAxis;
	private WB_Vector3d direction;
	private float distance;
	private float angle;
	
	
	public MeshUpdateInformation() {}
	
	// EXTRUDE CONSTRUCTOR
	public MeshUpdateInformation(HE_Face f, float dist, WB_Vector3d dir)
	{
		setFace(f);
		setDistance(dist);
		setDirection(dir);
	}
	
	// ROTATE FACE CONSTRUCTOR
	public MeshUpdateInformation(HE_Face f, WB_Point3d orig, WB_Vector3d axis, float angle)
	{
		setFace(f);
		setRotateOrigin(orig);
		setRotateAxis(axis);
		setAngle(angle);
	}
	
	// ROTATE EDGE CONSTRUCTOR
	public MeshUpdateInformation(HE_Edge e, WB_Point3d orig, WB_Vector3d axis, float angle)
	{
		setEdge(e);
		setRotateOrigin(orig);
		setRotateAxis(axis);
		setAngle(angle);
	}

	public HE_Face getFace() {
		return myFace;
	}

	public void setFace(HE_Face myFace) {
		this.myFace = myFace;
	}

	public HE_Edge getEdge() {
		return myEdge;
	}

	public void setEdge(HE_Edge myEdge) {
		this.myEdge = myEdge;
	}

	public HE_Vertex[] getVertices() {
		return myVertices;
	}

	public void setVertices(HE_Vertex[] myVertices) {
		this.myVertices = myVertices;
	}

	public WB_Point3d getRotateOrigin() {
		return rotateOrigin;
	}

	public void setRotateOrigin(WB_Point3d rotateOrigin) {
		this.rotateOrigin = rotateOrigin;
	}

	public WB_Vector3d getRotateAxis() {
		return rotateAxis;
	}

	public void setRotateAxis(WB_Vector3d rotateAxis) {
		this.rotateAxis = rotateAxis;
	}

	public WB_Vector3d getDirection() {
		return direction;
	}

	public void setDirection(WB_Vector3d dir) {
		this.direction = dir;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
}
