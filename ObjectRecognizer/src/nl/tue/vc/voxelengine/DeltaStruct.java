package nl.tue.vc.voxelengine;

public class DeltaStruct {

	public int deltaX;
	
	public int deltaY;
	
	public int deltaZ;
	
	public int index;
	
	@Override
	public String toString() {
		return "index: " + index + ", deltaX: " + deltaX + ", deltaY: " + deltaY + ", deltaZ: " + deltaZ;
	}
	
}
