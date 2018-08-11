package nl.tue.vc.projection;

import nl.tue.vc.model.BoxParameters;

public class VolumeModel {

	public Vector3D[] modelVertices;
	
	public double xLength;
	
	public double yLength;
	
	public double zLength;
	
	public VolumeModel() {
		int centerX = 4;
		int centerY = 4;
		int centerZ = 4;
		int halfSize = 4;
		xLength = 8.0;
		yLength = 8.0;
		zLength = 8.0;
		modelVertices = new Vector3D[] {	
			new Vector3D(centerX - halfSize, centerY + halfSize, centerZ + halfSize, 1),
			new Vector3D(centerX + halfSize, centerY + halfSize, centerZ + halfSize, 1),
			new Vector3D(centerX - halfSize, centerY - halfSize, centerZ + halfSize, 1),
			new Vector3D(centerX + halfSize, centerY - halfSize, centerZ + halfSize, 1),
			new Vector3D(centerX - halfSize, centerY + halfSize, centerZ - halfSize, 1),
			new Vector3D(centerX + halfSize, centerY + halfSize, centerZ - halfSize, 1),
			new Vector3D(centerX - halfSize, centerY - halfSize, centerZ - halfSize, 1),
			new Vector3D(centerX + halfSize, centerY - halfSize, centerZ - halfSize, 1)
		};
	}
	
	public VolumeModel(BoxParameters boxParameters) {
		double centerX = boxParameters.getCenterX();
		double centerY = boxParameters.getCenterY();
		double centerZ = boxParameters.getCenterZ();
		double halfSizeX = boxParameters.getSizeX()/2;
		double halfSizeY = boxParameters.getSizeY()/2;
		double halfSizeZ = boxParameters.getSizeZ()/2;
		
		
		modelVertices = new Vector3D[] {
			new Vector3D(centerX - halfSizeX, centerY + halfSizeY, centerZ + halfSizeZ, 1),
			new Vector3D(centerX + halfSizeX, centerY + halfSizeY, centerZ + halfSizeZ, 1),
			new Vector3D(centerX - halfSizeX, centerY - halfSizeY, centerZ + halfSizeZ, 1),
			new Vector3D(centerX + halfSizeX, centerY - halfSizeY, centerZ + halfSizeZ, 1),
			new Vector3D(centerX - halfSizeX, centerY + halfSizeY, centerZ - halfSizeZ, 1),
			new Vector3D(centerX + halfSizeX, centerY + halfSizeY, centerZ - halfSizeZ, 1),
			new Vector3D(centerX - halfSizeX, centerY - halfSizeY, centerZ - halfSizeZ, 1),
			new Vector3D(centerX + halfSizeX, centerY - halfSizeY, centerZ - halfSizeZ, 1)
		};
	}
	
}
