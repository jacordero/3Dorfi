package nl.tue.vc.model;

public class BoxParametersTest {

	private double sizeX;
	private double sizeY;
	private double sizeZ;
	private double centerX;
	private double centerY;
	private double centerZ;
	
	public void setSizeX(double sizeX){
		this.sizeX = sizeX;
	}
	
	public double getSizeX(){
		return sizeX;
	}
	
	public void setSizeY(double sizeY){
		this.sizeY = sizeY;
	}
	
	public double getSizeY(){
		return sizeY;
	}
	
	public void setSizeZ(double sizeZ){
		this.sizeZ = sizeZ;
	}
	
	public double getSizeZ(){
		return sizeZ;
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
		return "CenterX: " + centerX + ", CenterY: " + centerY + ", CenterZ: " + centerZ + 
				", sizeX: " + sizeX + ", sizeY: " + sizeY + ", sizeZ: " + sizeZ;
	}
	
}
