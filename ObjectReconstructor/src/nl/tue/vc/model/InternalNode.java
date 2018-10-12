package nl.tue.vc.model;

import javafx.scene.paint.Color;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.voxelengine.DeltaStruct;

public class InternalNode extends Node{

	private Node[] children;
	
	
	public InternalNode(NodeColor color, double sizeX, double sizeY, double sizeZ, double centerX, double centerY, double centerZ, int octreeHeight, int nodeDepth) {
		
		this.color = color;
		children = new Node[8];	
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.depth = nodeDepth;
		
		positionCenterX = centerX;
		positionCenterY = centerY;
		positionCenterZ = centerZ;
		
		// TODO: remove box parameters?
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX(sizeX);
		this.boxParameters.setSizeY(sizeY);
		this.boxParameters.setSizeZ(sizeZ);
		this.boxParameters.setCenterX(centerX);
		this.boxParameters.setCenterY(centerY);
		this.boxParameters.setCenterZ(centerZ);

		if (octreeHeight < 0){
			children = null;
		} else {
			double childrenSizeX = sizeX / 2;
			double childrenSizeY = sizeY / 2;
			double childrenSizeZ = sizeZ / 2;
			for (int i = 0; i < children.length; i++){
				DeltaStruct displacementDirections = computeDisplacementDirections(i);
				double displacementX = childrenSizeX / 2;
				double displacementY = childrenSizeY / 2;
				double displacementZ = childrenSizeZ / 2;
				
				//compute new center for each child
				double childCenterX = centerX + (displacementDirections.deltaX * displacementX);
				double childCenterY = centerY + (displacementDirections.deltaY * displacementY);
				double childCenterZ = centerZ + (displacementDirections.deltaZ * displacementZ);
								
				if (octreeHeight > 1){
					children[i] = new InternalNode(NodeColor.BLACK, childrenSizeX, childrenSizeY, childrenSizeZ, childCenterX, childCenterY, childCenterZ, octreeHeight - 1, nodeDepth + 1);
				} else {
					children[i] = new Leaf(NodeColor.BLACK, childrenSizeX, childrenSizeY, childrenSizeZ, childCenterX, childCenterY, childCenterZ, nodeDepth + 1);
				} 
			}			
		}
	}

	public InternalNode(NodeColor color, double sizeX, double sizeY, double sizeZ) {
		this.color = color;
		children = new Node[8];	
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		
		positionCenterX = 0;
		positionCenterY = 0;
		positionCenterZ = 0;
		
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX(sizeX);
		this.boxParameters.setSizeY(sizeY);
		this.boxParameters.setSizeZ(sizeZ);

		this.boxParameters.setCenterX(positionCenterX);
		this.boxParameters.setCenterY(positionCenterY);
		this.boxParameters.setCenterZ(positionCenterZ);
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
	public String printContent(String space){
		StringBuilder builder = new StringBuilder();
		builder.append(space + "Internal node -> " + super.toString() + "\n");
		if (children != null){
			for(int i = 0; i < children.length; i++) {
				builder.append(space + children[i].printContent(space + "\t") + "\n");
			}			
		}
		return builder.toString();
	}

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Internal node -> " + super.toString() + "\n");
		if (children != null){
			for(int i = 0; i < children.length; i++) {
				builder.append(children[i].toString() + "\n");
			}			
		}
		return builder.toString();
	}
	
	/**
	 * deltaHeight corresponds to the levels of splitting that are going to be done at the leaf level
	 */
	@Override
	public Node splitNode(int deltaHeight, int maxDepth){
		
		if (deltaHeight > 0){
			Node[] splittedChildren = new Node[8];
			if (children != null){
				for (int i = 0; i < children.length; i++){
					/**
					if (children[i] != null && children[i].getColor() == NodeColor.BLACK && children[i].getDepth() < maxDepth){
						splittedChildren[i] = children[i].splitNode(deltaHeight, maxDepth);
					} else if (children[i] != null && children[i].getColor() == NodeColor.GRAY){
						splittedChildren[i] = children[i].splitNode(deltaHeight, maxDepth);
					} else {
						splittedChildren[i] = children[i];
					}
					**/
					if (children[i] != null && children[i].getColor() == NodeColor.GRAY){
						splittedChildren[i] = children[i].splitNode(deltaHeight, maxDepth);
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
