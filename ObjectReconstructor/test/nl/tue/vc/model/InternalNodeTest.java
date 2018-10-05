package nl.tue.vc.model;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.voxelengine.DeltaStruct;

public class InternalNodeTest extends NodeTest{

	private NodeTest[] children;
	
	// this is here for testing purposes
	//private NodeColor[] colors = {NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE,
	//		NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE};
	
	public InternalNodeTest(Color color, double sizeX, double sizeY, double sizeZ, double parentCenterX, double parentCenterY, double parentCenterZ, int octreeHeight) {
		//Utils.debugNewLine("[InternalNodeTest] -> octreeHeight: " + octreeHeight, true);
		
		this.color = color;
		children = new NodeTest[8];	
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		
		positionCenterX = parentCenterX;
		positionCenterY = parentCenterY;
		positionCenterZ = parentCenterZ;
		
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX((int) sizeX);
		this.boxParameters.setSizeY((int) sizeY);
		this.boxParameters.setSizeZ((int) sizeZ);
		this.boxParameters.setCenterX((int)parentCenterX);
		this.boxParameters.setCenterY((int)parentCenterY);
		this.boxParameters.setCenterZ((int)parentCenterZ);

		if (octreeHeight < 0){
			children = null;
		} else {
			double childrenSizeX = sizeX / 2;
			double childrenSizeY = sizeY / 2;
			double childrenSizeZ = sizeZ / 2;
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
				double displacementX = childrenSizeX / 2;
				double displacementY = childrenSizeY / 2;
				double displacementZ = childrenSizeZ / 2;
				
				//compute center of each children
				double newParentCenterX = parentCenterX + (displacementDirections.deltaX * displacementX);
				double newParentCenterY = parentCenterY + (displacementDirections.deltaY * displacementY);
				double newParentCenterZ = parentCenterZ + (displacementDirections.deltaZ * displacementZ);
				
				//positionCenterX = newParentCenter;
				
				/**
				Utils.debugNewLine("Parent center: [" + parentCenterX + ", " + parentCenterY + ", " + parentCenterZ + "]", false);
				Utils.debugNewLine("Node center: [" + newParentCenterX + ", " + newParentCenterY + ", " + newParentCenterZ + "]",  false);
				**/
				
				if (octreeHeight > 1){
					children[i] = new InternalNodeTest(Color.GRAY, childrenSizeX, childrenSizeY, childrenSizeZ, newParentCenterX, newParentCenterY, newParentCenterZ, octreeHeight - 1);
				} else {
					children[i] = new LeafTest(Color.BLACK, childrenSizeX, childrenSizeY, childrenSizeZ, newParentCenterX, newParentCenterY, newParentCenterZ);
				} 
			}			
		}
		
		//initializeChildren();
	}

	public InternalNodeTest(Color color, double sizeX, double sizeY, double sizeZ) {
		this.color = color;
		children = new NodeTest[8];	
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		
		positionCenterX = 0;
		positionCenterY = 0;
		positionCenterZ = 0;
		
		this.boxParameters = new BoxParameters();		
		this.boxParameters.setSizeX((int) sizeX);
		this.boxParameters.setSizeY((int) sizeY);
		this.boxParameters.setSizeZ((int) sizeZ);

		this.boxParameters.setCenterX((int)positionCenterX);
		this.boxParameters.setCenterY((int)positionCenterY);
		this.boxParameters.setCenterZ((int)positionCenterZ);
	}

	
	public void addChildren(NodeTest[] children){
		this.children = children;
	}
	
	public void setChildNode(NodeTest childNode, int childIndex){
		this.children[childIndex] = childNode;
	}
	
	@Override
	public NodeTest[] getChildren() {
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
	public NodeTest splitNode(int deltaHeight){
		if (deltaHeight > 0){
			Utils.debugNewLine("++++++++++ split internal node", false);
			NodeTest[] splittedChildren = new NodeTest[8];
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
