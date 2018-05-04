package nl.tue.vc.projection.test;

import javafx.scene.paint.Color;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.voxelengine.DeltaStruct;

public class InternalNodeTest extends NodeTest {

	private NodeTest[] children;
	
	// this is here for testing purposes
	//private NodeColor[] colors = {NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE,
	//		NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE};
	
	
	
	public InternalNodeTest(double boxSize, double parentCenterX, double parentCenterY, double parentCenterZ, int levels) {
		this.color = Color.BLACK;
		children = new NodeTest[8];	
		this.boxSize = boxSize;
		
		positionCenterX = parentCenterX;
		positionCenterY = parentCenterY;
		positionCenterZ = parentCenterZ;

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
					children[i] = new InternalNodeTest(childrenBoxSize, newParentCenterX, newParentCenterY, newParentCenterZ, levels - 1);
				} else {
					children[i] = new LeafTest(childrenBoxSize, newParentCenterX, newParentCenterY, newParentCenterZ);
				} 
			}			
		}
		
		//initializeChildren();
	}

	

	
	public void addChildren(NodeTest[] children){
		this.children = children;
	}
	
	@Override
	public NodeTest[] getChildren() {
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
				builder.append(children[i].toString() + "\n");
			}			
		}
		return builder.toString();
	}
}
