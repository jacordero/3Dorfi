package nl.tue.vc.voxelengine;

import javafx.scene.paint.Color;

public abstract class Node {

	protected Color color;
	
	protected int boxSize;
	
	public Color getColor() {
		return color;
	}
	
	public int getBoxSize() {
		return boxSize;
	}
	
	public void setBoxSize(int boxSize) {
		this.boxSize = boxSize;
	}
	
	abstract Node[] getChildren();
	
	abstract boolean isLeaf();
	
	@Override
	public String toString() {
		return "{BoxSize: " + boxSize + ", Color: " + color.toString() +"}";
	}
	
}
