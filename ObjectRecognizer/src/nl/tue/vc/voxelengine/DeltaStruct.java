package nl.tue.vc.voxelengine;

public class DeltaStruct {

	public int deltaX;
	public int deltaY;
	public int deltaZ;
	
	public DeltaStruct() {
		deltaX = 0;
		deltaY = 0;
		deltaZ = 0;
	}
	
	@Override
	public String toString() {
		return "deltaX: " + deltaX + ", deltaY: " + deltaY + ", deltaZ: " + deltaZ;
	}
	
}
