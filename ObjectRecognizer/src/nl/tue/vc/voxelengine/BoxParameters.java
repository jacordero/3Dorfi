package nl.tue.vc.voxelengine;

public class BoxParameters {

	private double boxSize;
	private double centerX;
	private double centerY;
	private double centerZ;
	
	public void setBoxSize(double boxSize) {
		this.boxSize = boxSize;
	}
	
	public double getBoxSize() {
		return boxSize;
	}
	
	public void setCenterX(double centerX) {
		this.centerX = centerX;
	}
	
	public double getCenterX() {
		return centerX;
	}
	
	public void setCenterY(double centerY) {
		this.centerY = centerY;
	}
	
	public double getCenterY() {
		return centerY;
	}

	public void setCenterZ(double centerZ) {
		this.centerZ = centerZ;
	}
	
	public double getCenterZ() {
		return centerZ;
	}

	@Override
	public String toString() {
		return "CenterX: " + centerX + ", CenterY: " + centerY + ", CenterZ: " + centerZ + ", BoxSize: " + boxSize;
	}
	
}
