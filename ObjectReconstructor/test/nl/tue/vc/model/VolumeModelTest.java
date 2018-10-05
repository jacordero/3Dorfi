package nl.tue.vc.model;

import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.projection.Vector3D;

public class VolumeModelTest {

	public Vector3D[] modelVertices;
	
	public double xLength;
	
	public double yLength;
	
	public double zLength;
	
	public VolumeModelTest() {
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
	
	public VolumeModelTest(BoxParameters boxParameters) {
		double centerX = boxParameters.getCenterX();
		double centerY = boxParameters.getCenterY();
		double centerZ = boxParameters.getCenterZ();
		double newSizeX = boxParameters.getSizeX() / 2;
		double newSizeY = boxParameters.getSizeY() / 2;
		double newSizeZ = boxParameters.getSizeZ() / 2;
		
		modelVertices = new Vector3D[] {
			new Vector3D(centerX - newSizeX, centerY + newSizeY, centerZ + newSizeZ, 1),
			new Vector3D(centerX + newSizeX, centerY + newSizeY, centerZ + newSizeZ, 1),
			new Vector3D(centerX - newSizeX, centerY - newSizeY, centerZ + newSizeZ, 1),
			new Vector3D(centerX + newSizeX, centerY - newSizeY, centerZ + newSizeZ, 1),
			new Vector3D(centerX - newSizeX, centerY + newSizeY, centerZ - newSizeZ, 1),
			new Vector3D(centerX + newSizeX, centerY + newSizeY, centerZ - newSizeZ, 1),
			new Vector3D(centerX - newSizeX, centerY - newSizeY, centerZ - newSizeZ, 1),
			new Vector3D(centerX + newSizeX, centerY - newSizeY, centerZ - newSizeZ, 1)
		};
	}
	
}
