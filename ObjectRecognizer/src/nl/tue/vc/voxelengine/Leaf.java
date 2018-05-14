package nl.tue.vc.voxelengine;

import javafx.scene.paint.Color;
import nl.tue.vc.application.utils.Utils;

public class Leaf extends Node{

	
	
	public Leaf(NodeColor color, double boxSize, double centerX, double centerY, double centerZ) {
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
	
	public Leaf(NodeColor color, double boxSize) {
		this.boxSize = boxSize;
		this.positionCenterX = 0;
		this.positionCenterY = 0;
		this.positionCenterZ = 0;
		this.color = color;
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setBoxSize((int)boxSize);
		this.boxParameters.setCenterX((int)positionCenterX);
		this.boxParameters.setCenterY((int)positionCenterY);
		this.boxParameters.setCenterZ((int)positionCenterZ);
	}
	
	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Node[] getChildren() {
		return null;
	}
	
	public void setChildNode(Node childNode, int childIndex){
		
	}
	
	@Override
	public Node splitNode(int deltaHeight){
		if (deltaHeight > 0 && color == NodeColor.GRAY){
			Utils.debugNewLine("@@@@@@@@@@@@@ Leaf splitted up to " + deltaHeight + " levels", false);
			return new InternalNode(color, boxSize, positionCenterX, positionCenterY, positionCenterZ, deltaHeight);
		}
		return this;
	}
	
	@Override
	public String toString() {
		return "Leaf -> " + super.toString();
	}
}
