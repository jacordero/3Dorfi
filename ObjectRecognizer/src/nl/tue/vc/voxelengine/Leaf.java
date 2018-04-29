package nl.tue.vc.voxelengine;

import javafx.scene.paint.Color;

public class Leaf extends Node{

	
	
	public Leaf(Color color, double boxSize, double centerX, double centerY, double centerZ) {
		this.boxSize = boxSize;
		this.positionCenterX = centerX;
		this.positionCenterY = centerY;
		this.positionCenterZ = centerZ;
		this.color = color;
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setBoxSize((int)boxSize);
		this.boxParameters.setCenterX((int)centerX);
		this.boxParameters.setCenterY((int)centerY);
		this.boxParameters.setCenterZ((int)centerZ);
	}
	
	@Override
	boolean isLeaf() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	Node[] getChildren() {
		return null;
	}
	
	@Override
	public String toString() {
		return "Leaf -> " + super.toString();
	}
}
