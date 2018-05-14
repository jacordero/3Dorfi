package nl.tue.vc.voxelengine;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import nl.tue.vc.application.utils.Utils;

public class InternalNode extends Node{

	private Node[] children;
	
	// this is here for testing purposes
	//private NodeColor[] colors = {NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE,
	//		NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE};
	
	public InternalNode(Color color, double boxSize, double parentCenterX, double parentCenterY, double parentCenterZ, int octreeHeight) {
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

		if (octreeHeight < 0){
			children = null;
		} else {
			double childrenBoxSize = boxSize / 2;
			List<Color> childrenColors = new ArrayList<Color>();
			childrenColors.add(Color.GREEN);
			childrenColors.add(Color.RED);
			childrenColors.add(Color.YELLOW);
			childrenColors.add(Color.BROWN);
			childrenColors.add(Color.ORANGE);
			childrenColors.add(Color.CYAN);
			childrenColors.add(Color.BLUE);
			childrenColors.add(Color.MAGENTA);
			for (int i = 0; i < children.length; i++){
				DeltaStruct displacementDirections = computeDisplacementDirections(i);
				double displacementSize = childrenBoxSize / 2;
				
				//compute center of each children
				double newParentCenterX = parentCenterX + (displacementDirections.deltaX * displacementSize);
				double newParentCenterY = parentCenterY + (displacementDirections.deltaY * displacementSize);
				double newParentCenterZ = parentCenterZ + (displacementDirections.deltaZ * displacementSize);
				
				//positionCenterX = newParentCenter;
				
				/**
				Utils.debugNewLine("Parent center: [" + parentCenterX + ", " + parentCenterY + ", " + parentCenterZ + "]", false);
				Utils.debugNewLine("Node center: [" + newParentCenterX + ", " + newParentCenterY + ", " + newParentCenterZ + "]",  false);
				**/
				
				if (octreeHeight > 1){
					children[i] = new InternalNode(Color.GRAY, childrenBoxSize, newParentCenterX, newParentCenterY, newParentCenterZ, octreeHeight - 1);
				} else {
					children[i] = new Leaf(Color.BLACK, childrenBoxSize, newParentCenterX, newParentCenterY, newParentCenterZ);
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
	
	public void setChildNode(Node childNode, int childIndex){
		this.children[childIndex] = childNode;
	}
	
	@Override
	public Node[] getChildren() {
		return children;
	}
	
	@Override
	public boolean isLeaf() {
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
	
	/**
	 * deltaHeight corresponds to the levels of splitting that are going to be done at the leaf level
	 */
	@Override
	public Node splitNode(int deltaHeight){
		if (deltaHeight > 0){
			Utils.debugNewLine("++++++++++ split internal node", false);
			Node[] splittedChildren = new Node[8];
			if (children != null){
				for (int i = 0; i < children.length; i++){
					if (children[i] != null && children[i].getColor() == Color.GRAY){
						splittedChildren[i] = children[i].splitNode(deltaHeight);
					} else {
						splittedChildren[i] = children[i];
					}
				}
			}
			children = splittedChildren;			
		}
		return this;
	}
}
