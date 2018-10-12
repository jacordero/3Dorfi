package nl.tue.vc.model;

import javafx.scene.paint.Color;
import nl.tue.vc.model.BoxParameters;

public class Leaf extends Node{

		
	public Leaf(NodeColor color, double sizeX, double sizeY, double sizeZ, double centerX, double centerY, double centerZ, int nodeDepth) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.positionCenterX = centerX;
		this.positionCenterY = centerY;
		this.positionCenterZ = centerZ;
		this.color = color;
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX(sizeX);
		this.boxParameters.setSizeY(sizeY);
		this.boxParameters.setSizeZ(sizeZ);		
		this.boxParameters.setCenterX(centerX);
		this.boxParameters.setCenterY(centerY);
		this.boxParameters.setCenterZ(centerZ);
		this.depth = nodeDepth;
	}
	
	public Leaf(NodeColor color, double sizeX, double sizeY, double sizeZ) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.positionCenterX = 0;
		this.positionCenterY = 0;
		this.positionCenterZ = 0;
		this.color = color;
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX(sizeX);
		this.boxParameters.setSizeY(sizeY);
		this.boxParameters.setSizeZ(sizeZ);		
		this.boxParameters.setCenterX(positionCenterX);
		this.boxParameters.setCenterY(positionCenterY);
		this.boxParameters.setCenterZ(positionCenterZ);
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public Node[] getChildren() {
		return null;
	}
	
	public void setChildNode(Node childNode, int childIndex){
		
	}
	
	@Override
	public Node splitNode(int deltaHeight, int maxDepth){
		if (deltaHeight > 0 && color == NodeColor.GRAY){
			return new InternalNode(NodeColor.GRAY, sizeX, sizeY, sizeZ, positionCenterX, positionCenterY, positionCenterZ, deltaHeight, depth);
		}
		return this;
	}
	
	@Override
	public String printContent(String space){
		return space + "Leaf -> " + super.toString();
	}
	
	@Override
	public String toString() {
		return "Leaf -> " + super.toString();
	}
}
