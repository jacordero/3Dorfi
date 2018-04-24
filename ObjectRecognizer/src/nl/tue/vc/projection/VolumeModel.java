package nl.tue.vc.projection;

import nl.tue.vc.voxelengine.BoxParameters;

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
//			new Vector3D(0, 0, 0, 1),
//			new Vector3D(xLength, 0, 0, 1),
//			new Vector3D(xLength, 0, zLength, 1),
//			new Vector3D(0, 0, zLength, 1),
//			new Vector3D(0, yLength, 0, 1),
//			new Vector3D(xLength, yLength, 0, 1),
//			new Vector3D(xLength, yLength, zLength, 1),
//			new Vector3D(0, yLength, zLength, 1)
			
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
		int centerX = boxParameters.getCenterX();
		int centerY = boxParameters.getCenterY();
		int centerZ = boxParameters.getCenterZ();
		int halfSize = boxParameters.getBoxSize()/2;
		
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
	
}
