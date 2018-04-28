package nl.tue.vc.voxelengine;

import javafx.scene.paint.Color;

public class Leaf extends Node{

	
	
	public Leaf(Color color, int nodeBoxSize) {
		boxSize = nodeBoxSize;
		this.color = color;
	}
	
	@Override
	boolean isLeaf() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	Node[] getChildren() {
		return new Node[8];
	}
	
	@Override
	public String toString() {
		return "Leaf -> " + super.toString();
	}

}
