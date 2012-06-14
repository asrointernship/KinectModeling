package be.wide.test;

import java.awt.*;
import java.util.*;
import processing.core.*;
import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.processing.*;

public class MeshController 
{
	private WETriangleMesh mesh;
	private ArrayList<Vec3D> selectList;
	private PGraphics canvas;
	private ArrayList<Face> selectedFacesList;
	
	// Constant variables
	private final Color selectionColor = new Color(255, 0, 0);
	private final Color meshColor = new Color(0, 255, 0);
	private final int selectionSphereSize = 10;

	public MeshController(ToxiclibsSupport gfx)
	{
		mesh = new WETriangleMesh();
		mesh.addMesh(new AABB(new Vec3D(), 200).toMesh());
		mesh.translate(new Vec3D(250, 200, 250));
		mesh.faceOutwards();
		
		canvas = gfx.getGraphics();
		selectList = new ArrayList<Vec3D>();
		selectList.add(mesh.faces.get(5).a);
		selectList.add(mesh.faces.get(8).c);
		
		selectedFacesList = new ArrayList<Face>();
		selectedFacesList.add(mesh.faces.get(8));
		selectedFacesList.add(mesh.faces.get(9));
	}

	public void drawMesh()
	{
		for (Face f : mesh.faces) 
		{
			f.flipVertexOrder();
			canvas.noStroke();
			canvas.fill(meshColor.getRGB());
			canvas.vertex(f.a.x, f.a.y, f.a.z);
			canvas.vertex(f.b.x, f.b.y, f.b.z);
			canvas.vertex(f.c.x, f.c.y, f.c.z);
		}
	}

	public void highLightVertexSelection()
	{
		for(Face f : mesh.faces)
		{
			if (selectList.contains(f.a))
			{
				canvas.pushMatrix();
				canvas.fill(selectionColor.getRGB());
				canvas.translate(f.a.x, f.a.y, f.a.z);
				canvas.sphere(selectionSphereSize);
				canvas.popMatrix();
			}
			
			if (selectList.contains(f.b))
			{
				canvas.pushMatrix();
				canvas.fill(selectionColor.getRGB());
				canvas.translate(f.b.x, f.b.y, f.b.z);
				canvas.sphere(selectionSphereSize);
				canvas.popMatrix();
			}
			
			if (selectList.contains(f.c))
			{
				canvas.pushMatrix();
				canvas.fill(selectionColor.getRGB());
				canvas.translate(f.c.x, f.c.y, f.c.z);
				canvas.sphere(selectionSphereSize);
				canvas.popMatrix();
			}
		}
	}
	
	public void highLightFaceSelection()
	{
		
	}

	public void setMesh(WETriangleMesh mesh) {
		this.mesh = mesh;
	}

	public WETriangleMesh getMesh() {
		return mesh;
	}
}
