package nl.tue.vc.model.test;

import javafx.scene.paint.Color;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.model.BoxParameters;

public class LeafTest extends NodeTest{

	
	
	public LeafTest(Color color, double sizeX, double sizeY, double sizeZ, double centerX, double centerY, double centerZ) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.positionCenterX = centerX;
		this.positionCenterY = centerY;
		this.positionCenterZ = centerZ;
		this.color = color;
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX((int)sizeX);
		this.boxParameters.setSizeY((int)sizeY);
		this.boxParameters.setSizeZ((int)sizeZ);		
		this.boxParameters.setCenterX((int)centerX);
		this.boxParameters.setCenterY((int)centerY);
		this.boxParameters.setCenterZ((int)centerZ);
	}
	
	public LeafTest(Color color, double sizeX, double sizeY, double sizeZ) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.positionCenterX = 0;
		this.positionCenterY = 0;
		this.positionCenterZ = 0;
		this.color = color;
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX((int)sizeX);
		this.boxParameters.setSizeY((int)sizeY);
		this.boxParameters.setSizeZ((int)sizeZ);		
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
	public NodeTest[] getChildren() {
		return null;
	}
	
	public void setChildNode(NodeTest childNode, int childIndex){
		
	}
	
	@Override
	public NodeTest splitNode(int deltaHeight){
		if (deltaHeight > 0 && color == Color.GRAY){
			Utils.debugNewLine("@@@@@@@@@@@@@ Leaf splitted up to " + deltaHeight + " levels", false);
			return new InternalNodeTest(color, sizeX, sizeY, sizeZ, positionCenterX, positionCenterY, positionCenterZ, deltaHeight);
		}
		return this;
	}
	
	@Override
	public String toString() {
		return "Leaf -> " + super.toString();
	}
}
