package nl.tue.vc.model;

import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;

import javafx.scene.paint.Color;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.voxelengine.DeltaStruct;

public abstract class NodeTest {

	protected Color color;
	
	protected double sizeX;
	
	protected double sizeY;
	
	protected double sizeZ;
	
	protected double positionCenterX;
	
	protected double positionCenterY;
	
	protected double positionCenterZ;
	
	protected BoxParameters boxParameters;
	
	DeltaStruct displacementDirection;
	
	public DeltaStruct getDisplacementDirection() {
		return displacementDirection;
	}

	public void setDisplacementDirection(DeltaStruct displacementDirection) {
		this.displacementDirection = displacementDirection;
	}

	public BoxParameters getBoxParameters() {
		return boxParameters;
	}

	public void setBoxParameters(BoxParameters boxParameters) {
		this.boxParameters = boxParameters;
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color newColor) {
		color = newColor;
	}
	
	public double getSizeX() {
		return sizeX;
	}
	
	public void setSizeX(int sizeX) {
		this.sizeX = sizeX;
	}

	public double getSizeY() {
		return sizeY;
	}
	
	public void setSizeY(int sizeY) {
		this.sizeY = sizeY;
	}

	public double getSizeZ() {
		return sizeZ;
	}
	
	public void setSizeZ(int sizeZ) {
		this.sizeZ = sizeZ;
	}
	
	public double getPositionCenterX(){
		return positionCenterX;
	}
	
	public void setPositionCenterX(double positionCenterX){
		this.positionCenterX = positionCenterX;
	}
	
	public double getPositionCenterY(){
		return positionCenterY;
	}
	
	public void setPositionCenterY(double positionCenterY){
		this.positionCenterY = positionCenterY;
	}
	
	public double getPositionCenterZ(){
		return positionCenterZ;
	}
	
	public void setPositionCenterZ(double positionCenterZ){
		this.positionCenterZ = positionCenterZ;
	}
	
	
	public MatOfPoint3f getCorners(){
		
		double displacementX = sizeX / 2;
		double displacementY = sizeY / 2;
		double displacementZ = sizeZ / 2;
		Point3[] corners = new Point3[8];
		for (int i = 0; i < 8; i++){
			displacementDirection = computeDisplacementDirections(i);
			double xPosition = positionCenterX + (displacementDirection.deltaX * displacementX);
			double yPosition = positionCenterY + (displacementDirection.deltaY * displacementY);
			double zPosition = positionCenterZ + (displacementDirection.deltaZ * displacementZ);
			Point3 corner = new Point3(xPosition, yPosition, zPosition);
			corners[i] = corner;
		}
		
		return new MatOfPoint3f(corners);
	}
	
	protected DeltaStruct computeDisplacementDirections(int index) {
		DeltaStruct deltas = new DeltaStruct();
		switch (index) {
		case 0:
			deltas.deltaX = -1;
			deltas.deltaY = 1;
			deltas.deltaZ = 1;
			break;
		case 1:
			deltas.deltaX = 1;
			deltas.deltaY = 1;
			deltas.deltaZ = 1;
			break;
		case 2:
			deltas.deltaX = -1;
			deltas.deltaY = -1;
			deltas.deltaZ = 1;
			break;
		case 3:
			deltas.deltaX = 1;
			deltas.deltaY = -1;
			deltas.deltaZ = 1;
			break;
		case 4:
			deltas.deltaX = -1;
			deltas.deltaY = 1;
			deltas.deltaZ = -1;
			break;
		case 5:
			deltas.deltaX = 1;
			deltas.deltaY = 1;
			deltas.deltaZ = -1;
			break;
		case 6:
			deltas.deltaX = -1;
			deltas.deltaY = -1;
			deltas.deltaZ = -1;
			break;
		case 7:
			deltas.deltaX = 1;
			deltas.deltaY = -1;
			deltas.deltaZ = -1;
			break;
		default:
			throw new RuntimeException("Invalid index value " + index);
		}

		return deltas;
	}

	
	abstract public NodeTest[] getChildren();
	
	abstract public boolean isLeaf();
	
	public abstract void setChildNode(NodeTest childNode, int childIndex);
	
	abstract public NodeTest splitNode(int levels);
	
	@Override
	public String toString() {
		String str = "{sizeX: " + sizeX + ", sizeY: " + sizeY + ", sizeZ: " + sizeZ + ", centerX: " + positionCenterX + ", centerY: " + positionCenterY + ", centerZ: " + positionCenterZ + ", Color: ";
		if (color == Color.BLACK){
			str += " black}";
		} else if (color == Color.GRAY){
			str += " gray}";
		} else if (color == Color.WHITE){
			str += " white}";
		}
		return str;
	}
	
}