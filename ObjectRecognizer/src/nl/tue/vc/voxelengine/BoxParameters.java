package nl.tue.vc.voxelengine;

public class BoxParameters {

	private int boxSize;
	private int centerX;
	private int centerY;
	private int centerZ;
	
	public void setBoxSize(int boxSize) {
		this.boxSize = boxSize;
	}
	
	public int getBoxSize() {
		return boxSize;
	}
	
	public void setCenterX(int centerX) {
		this.centerX = centerX;
	}
	
	public int getCenterX() {
		return centerX;
	}
	
	public void setCenterY(int centerY) {
		this.centerY = centerY;
	}
	
	public int getCenterY() {
		return centerY;
	}

	public void setCenterZ(int centerZ) {
		this.centerZ = centerZ;
	}
	
	public int getCenterZ() {
		return centerZ;
	}

	@Override
	public String toString() {
		return "CenterX: " + centerX + ", CenterY: " + centerY + ", CenterZ: " + centerZ + ", BoxSize: " + boxSize;
	}
	
}
