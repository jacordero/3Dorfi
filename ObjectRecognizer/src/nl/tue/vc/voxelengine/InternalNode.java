package nl.tue.vc.voxelengine;

import javafx.scene.paint.Color;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.projectiontests.InternalNodeTest;
import nl.tue.vc.projectiontests.LeafTest;
import nl.tue.vc.projectiontests.NodeTest;

public class InternalNode extends Node{

	private Node[] children;
	
	// this is here for testing purposes
	//private NodeColor[] colors = {NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE,
	//		NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE};
	
	public InternalNode(Color color, double boxSize, double parentCenterX, double parentCenterY, double parentCenterZ, int levels) {
		this.color = color;
		children = new Node[8];	
		this.boxSize = boxSize;
		
		positionCenterX = parentCenterX;
		positionCenterY = parentCenterY;
		positionCenterZ = parentCenterZ;
		
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setBoxSize((int)boxSize);
		this.boxParameters.setCenterX((int)parentCenterX);
		this.boxParameters.setCenterY((int)parentCenterY);
		this.boxParameters.setCenterZ((int)parentCenterZ);

		if (levels < 0){
			children = null;
		} else {
			double childrenBoxSize = boxSize / 2;
			for (int i = 0; i < children.length; i++){
				DeltaStruct displacementDirections = computeDisplacementDirections(i);
				double displacementSize = childrenBoxSize / 2;
				
				//compute center of each children
				double newParentCenterX = parentCenterX + (displacementDirections.deltaX * displacementSize);
				double newParentCenterY = parentCenterY + (displacementDirections.deltaY * displacementSize);
				double newParentCenterZ = parentCenterZ + (displacementDirections.deltaZ * displacementSize);
				
				//positionCenterX = newParentCenter;
				
				Utils.debugNewLine("Parent center: [" + parentCenterX + ", " + parentCenterY + ", " + parentCenterZ + "]", true);
				Utils.debugNewLine("Node center: [" + newParentCenterX + ", " + newParentCenterY + ", " + newParentCenterZ + "]",  true);
				
				if (levels > 0){
					children[i] = new InternalNode(color, childrenBoxSize, newParentCenterX, newParentCenterY, newParentCenterZ, levels - 1);
				} else {
					children[i] = new Leaf(color, childrenBoxSize, newParentCenterX, newParentCenterY, newParentCenterZ);
				} 
			}			
		}
		
		//initializeChildren();
	}

	public InternalNode(Color color, double boxSize) {
		this.color = color;
		children = new Node[8];	
		this.boxSize = boxSize;
		
		positionCenterX = 0;
		positionCenterY = 0;
		positionCenterZ = 0;
		
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setBoxSize((int)boxSize);
		this.boxParameters.setCenterX((int)positionCenterX);
		this.boxParameters.setCenterY((int)positionCenterY);
		this.boxParameters.setCenterZ((int)positionCenterZ);
	}

	
	public void addChildren(Node[] children){
		this.children = children;
	}
	
	@Override
	public Node[] getChildren() {
		return children;
	}
	
	@Override
	boolean isLeaf() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Internal node -> " + super.toString() + "\n");
		if (children != null){
			for(int i = 0; i < children.length; i++) {
				//builder.append(children[i].toString() + "\n");
			}			
		}
		return builder.toString();
	}
}
