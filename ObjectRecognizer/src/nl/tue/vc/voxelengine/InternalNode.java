package nl.tue.vc.voxelengine;

import javafx.scene.paint.Color;

public class InternalNode extends Node{

	private Node[] children;
	
	// this is here for testing purposes
	//private NodeColor[] colors = {NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE,
	//		NodeColor.BLACK, NodeColor.BLACK, NodeColor.BLACK, NodeColor.WHITE};
	
	public InternalNode(Color color, int nodeBoxSize) {
		this.color = color;
		children = new Node[8];
		boxSize = nodeBoxSize;
		//initializeChildren();
	}

	/**
	private void initializeChildren() {
		for (int i = 0; i < children.length; i++) {
			Leaf leaf = new Leaf(colors[i], boxSize/2);
			children[i] = leaf;
		}
	}**/
	
	public void updateChildrenAt(Node child, int index) {
		children[index] = child;
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
		for(int i = 0; i < children.length; i++) {
			//builder.append(children[i].toString() + "\n");
		}
		return builder.toString();
	}
}
