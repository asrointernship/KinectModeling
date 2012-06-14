package be.wide.test;

import wblut.hemesh.core.*;
import wblut.hemesh.modifiers.*;
import be.wide.controller.*;


public class ExtrudeModelStrategy extends ModelStrategy
{
	/**
	 * Standard constructor.
	 * Takes a HemeshController to get acces to the mesh.
	 * @param mesh HemeshController
	 */
	public ExtrudeModelStrategy(HemeshController mesh) {
		super(mesh);
	}

	/**
	 * Update the mesh with the information found in MeshUpdateInformation.
	 * This strategy extrudes a given face with a certain distance.
	 * @param info MeshUpdateInformation
	 */
	public void updateMesh(MeshUpdateInformation info) 
	{
		HE_Mesh mesh = this.getMeshControl().getMesh();
		
		if (mesh.getFacesAsList().contains(info.getFace()))
		{
			int index = mesh.getFacesAsList().indexOf(info.getFace());
			
			HE_Selection selection = new HE_Selection(mesh);
			selection.add(mesh.getFacesAsList().get(index));
			
			HEM_Extrude extrude = new HEM_Extrude();
			extrude.setDistance(info.getDistance());
			extrude.setFuse(true);
			mesh.modifySelected(extrude, selection);
			this.getMeshControl().setMesh(mesh);
		}
	}
}
