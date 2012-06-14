package be.wide.test;

import processing.core.*;
import wblut.core.processing.*;
import wblut.geom.core.*;
import wblut.hemesh.core.*;
import wblut.hemesh.creators.*;
import wblut.hemesh.tools.*;

public class HemeshController 
{
	private PApplet parent;
	private HE_Mesh mesh;
	private WB_Render render;
	private HET_Selector selector;
	private int selectedId;
	
	public static final int SELECT_FACE = 0;
	public static final int SELECT_EDGE = 1;
	public static final int SELECT_VERTEX = 2;

	public HemeshController(PApplet par)
	{
		this.setParent(par);
		HEC_Box boxCreator = new HEC_Box(100, 100, 100, 2, 2, 2);

		mesh = new HE_Mesh(boxCreator);	
		mesh.moveTo(100, 50, 100);
		render = new WB_Render(parent);
		selector = new HET_Selector(parent);
	}

	public void drawMesh(int xPos, int yPos, int select)
	{
		parent.noStroke();
		parent.noFill();
		if (select == SELECT_FACE)
		{
			parent.fill(0, 255, 0);
			render.drawFaces(selector, mesh);

			parent.fill(255, 0, 0);
			if (selector.get(xPos, yPos) !=null) 
			{ 
				render.drawFace(selector.get(xPos, yPos), mesh); 
				selectedId = selector.get(xPos, yPos);
			}
			parent.stroke(0, 0, 0);
			parent.strokeWeight(1);
			render.drawEdges(mesh);
		}
		else if (select == SELECT_EDGE)
		{
			parent.fill(0, 255, 0);
			render.drawFaces(mesh);

			parent.stroke(0, 0, 0);
			parent.strokeWeight(1);
			render.drawEdges(selector, mesh);

			parent.stroke(255, 0, 0);
			parent.strokeWeight(5);
			if (selector.get(xPos, yPos) != null)
			{
				render.drawEdge(selector.get(xPos, yPos), mesh);
				selectedId = selector.get(xPos, yPos);
			}
		}
		else if (select == SELECT_VERTEX)
		{
			parent.fill(0, 255, 0);
			render.drawFaces(mesh);
			parent.strokeWeight(1);
			parent.stroke(0, 0, 0);
			render.drawEdges(mesh);
			
			parent.noFill();
			parent.noStroke();
			render.drawVertices(selector, 5, mesh);
			
			parent.stroke(255,0,0);
			parent.fill(255,0,0);
			if (selector.get(xPos, yPos) != null)
			{
				render.drawVertex(selector.get(xPos, yPos), 5, mesh);
				selectedId = selector.get(xPos, yPos);
			}
			else
			{
				// Snap to midPoints
				int counter = 0;
				parent.stroke(0, 0, 255);
				parent.fill(0, 0, 255);
				HE_Edge[] edges = mesh.getEdgesAsArray();
				
				for(int i = 0; i < edges.length; ++i)
				{
					WB_Point3d punt = new WB_Point3d(xPos - 130, -yPos + 510, edges[i].getEdgeCenter().z);
					WB_Vector3d vec = new WB_Vector3d(punt);
					vec.sub(edges[i].getEdgeCenter());
					double length = Math.sqrt((vec.x * vec.x) + (vec.y * vec.y) + (vec.z * vec.z));
					if (length <= 20 && counter == 0)
					{
						render.drawVertex(new HE_Vertex(edges[i].getEdgeCenter()), 5);
						counter++;
					}
				}
			}
		}
		parent.noStroke();
		parent.noFill();
	}

	private void setParent(PApplet parent) {
		this.parent = parent;
	}

	public HE_Mesh getMesh() {
		return mesh;
	}
	
	public void setMesh(HE_Mesh m)
	{
		this.mesh = m;
	}

	public void setSelectedId(int selectedId) {
		this.selectedId = selectedId;
	}

	public int getSelectedId() {
		return selectedId;
	}
}
