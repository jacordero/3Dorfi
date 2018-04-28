package nl.tue.vc.voxelengine;

import javafx.scene.paint.Color;

public abstract class Node {

	protected Color color;
	
	protected int boxSize;
	protected BoxParameters boxParameters;
	protected DeltaStruct deltaStruct;
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color newColor) {
		color = newColor;
	}
	
	public int getBoxSize() {
		return boxSize;
	}
	
	public void setBoxSize(int boxSize) {
		this.boxSize = boxSize;
	}
	
	abstract Node[] getChildren();
	
	public abstract boolean isLeaf();
	
	@Override
	public String toString() {
		return "{BoxSize: " + boxSize + ", Color: " + color.toString() +"}";
	}

	public BoxParameters getBoxParameters() {
		return boxParameters;
	}

	public void setBoxParameters(BoxParameters boxParameters) {
		this.boxParameters = boxParameters;
	}

	public DeltaStruct getDeltaStruct() {
		return deltaStruct;
	}

	public void setDeltaStruct(DeltaStruct deltaStruct) {
		this.deltaStruct = deltaStruct;
	}
	
}
