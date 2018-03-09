package nl.tue.vc.voxelengine;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class Octree {
	
	private Node root;
	
	private int boxSize;
	
	/**
	 *        +---------+-----------+
	 *       +     2   +    3     + |
	 *     + -------+-----------+   |
	 *   +    6   +     7    +  | 3 |
	 *  ---------------------   | + |
	 *  |        |          | 7 +   |
	 *  |    6   |    7     | + | 1 + 
	 *  --------------------+   | + 
	 *  |        |          | 5 + 
	 *  |    4   |    5     | + 
	 *  --------------------+ 
	 *  
	 *  cubes 0, 1, 2, 4, 5, and 6 get a black value
	 *  cubes 3, and 7 get a white value
	 *  
	 */
	
	public Octree(int boxSize) {
		this.boxSize = boxSize;
		root = new InternalNode(Color.GRAY, boxSize);
	}
	
	
	public Node getRoot() {
		return root;
	}
	
	
	public void generateOctreeFractal(int depth) {
		
		int nodesBoxSize = boxSize / 2;
		
		// create node 0
		root.getChildren()[0] = new Leaf(Color.WHITE, nodesBoxSize);
		
		// create node 1
		root.getChildren()[1] = new Leaf(Color.WHITE, nodesBoxSize);
		
		// create node 2
		root.getChildren()[2] = new Leaf(Color.WHITE, nodesBoxSize);

		// create node 3
		root.getChildren()[3] = new Leaf(Color.WHITE, nodesBoxSize);
		
		// create node 4
		root.getChildren()[4] = generateInternalNode(nodesBoxSize);

		// create node 5
		root.getChildren()[5] = new Leaf(Color.BLACK, nodesBoxSize);

		// create node 6
		root.getChildren()[6] = new Leaf(Color.BLACK, nodesBoxSize);

		// create node 7
		root.getChildren()[7] = generateInternalNode(nodesBoxSize);

	}
	
	private Node generateInternalNode(int boxSize) {
		
		InternalNode node = new InternalNode(Color.BLACK, boxSize);
		
		node.getChildren()[0] = new Leaf(Color.BLACK, boxSize/2);
		
		// create node 1
		node.getChildren()[1] = new Leaf(Color.BLUE, boxSize/2);
		
		// create node 2
		node.getChildren()[2] = new Leaf(Color.BLUEVIOLET, boxSize/2);

		// create node 3
		node.getChildren()[3] = new Leaf(Color.DARKGREEN, boxSize/2);
		
		// create node 4
		node.getChildren()[4] = new Leaf(Color.DARKORANGE, boxSize/2);

		// create node 5
		node.getChildren()[5] = new Leaf(Color.MAROON, boxSize/2);

		// create node 6
		node.getChildren()[6] = new Leaf(Color.RED, boxSize/2);

		// create node 7
		node.getChildren()[7] = new Leaf(Color.SIENNA, boxSize/2);
		
		return node;
	}
	//public generate
}
