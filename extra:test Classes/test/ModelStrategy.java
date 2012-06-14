package be.wide.test;

import be.wide.controller.*;

/**
 * Abstract class for the modeling strategy.
 * @author Maarten Taeymans
 *
 */
public abstract class ModelStrategy {

	private HemeshController meshControl;
	
	/**
	 * Standard constructor.
	 * Takes a HemeshController to get acces to the mesh.
	 * @param mesh HemeshController
	 */
	public ModelStrategy(HemeshController mesh)
	{
		this.setMeshControl(mesh);
	}
	
	/**
	 * Abstract method to be overwritten by different strategys.
	 * @param info MeshUpdateInformation
	 */
	public abstract void updateMesh(MeshUpdateInformation info);

	private void setMeshControl(HemeshController meshControl) {
		this.meshControl = meshControl;
	}

	/**
	 * Gets the HemeshController.
	 * @return HemeshController
	 */
	public HemeshController getMeshControl() {
		return meshControl;
	}
}
