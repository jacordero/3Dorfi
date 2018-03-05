package nl.tue.vc.voxelengine;


import java.util.Random;

import javafx.scene.paint.Color;

public class Octree {
	
	private Node root;
	
	private int boxSize;
	private InternalNode node;
	private BoxParameters boxParameters;
	
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
	 */
	
	public Octree(int boxSize) {
		this.boxSize = boxSize;
		root = new InternalNode(Color.GRAY, boxSize);
		this.node = new InternalNode(Color.GRAY, boxSize);
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void generateOctreeFractal(int parentBoxSize, int level) {
		root = generateOctreeFractalAux(parentBoxSize, level);
	}
	
	public void generateOctreeTest(int parentBoxSize) {
		Random random = new Random();
		int level = random.nextInt(3) + 1;
		System.out.println("Level for octree test: " + level);
		generateOctreeFractal(parentBoxSize, level);
	}
	
	
	private Node generateOctreeFractalAux(int parentBoxSize, int level) {
		int nodesBoxSize = parentBoxSize / 2;
		if (level == 0) {
			return generateInternalNode(nodesBoxSize);
		} else {
			Node internalNode = new InternalNode(Color.BLACK, parentBoxSize);
			// create node 0
			internalNode.getChildren()[0] = new Leaf(Color.BLACK, nodesBoxSize);
			//root.getChildren()[0] = generateInternalNode(nodesBoxSize);
			
			// create node 1
			internalNode.getChildren()[1] = new Leaf(Color.BLUE, nodesBoxSize);
			//root.getChildren()[1] = generateInternalNode(nodesBoxSize);
			
			// create node 2
			internalNode.getChildren()[2] = new Leaf(Color.BLUEVIOLET, nodesBoxSize);
			//root.getChildren()[2] = generateInternalNode(nodesBoxSize);
			
			// create node 3
			internalNode.getChildren()[3] = new Leaf(Color.DARKGREEN, nodesBoxSize);
			//root.getChildren()[3] = generateInternalNode(nodesBoxSize);
			
			// create node 4
			internalNode.getChildren()[4] = generateOctreeFractalAux(nodesBoxSize, level-1);
			//internalNode.getChildren()[4] = new Leaf(Color.DARKORANGE, nodesBoxSize);
			
			// create node 5
			internalNode.getChildren()[5] = new Leaf(Color.MAROON, nodesBoxSize);
			//root.getChildren()[5] = generateInternalNode(nodesBoxSize);
			
			// create node 6
			internalNode.getChildren()[6] = new Leaf(Color.RED, nodesBoxSize);
			//internalNode.getChildren()[6] = generateOctreeFractalAux(nodesBoxSize, level-1);
			
			// create node 7
			//internalNode.getChildren()[7] = generateOctreeFractalAux(nodesBoxSize, level-1);
			internalNode.getChildren()[7] = new Leaf(Color.PINK, nodesBoxSize);
			
			// create node 8
			//internalNode.getChildren()[8] = new Leaf(Color.YELLOW, nodesBoxSize);
			
			return internalNode;
		}
				
	}
	
	private Node generateInternalNode(int boxSize) {
		
		node.getChildren()[0] = new Leaf(Color.BLACK, boxSize/2);
		
		// create node 1
		node.getChildren()[1] = new Leaf(Color.BLACK, boxSize/2);
		
		// create node 2
		node.getChildren()[2] = new Leaf(Color.BLACK, boxSize/2);

		// create node 3
		node.getChildren()[3] = new Leaf(Color.BLACK, boxSize/2);
		
		// create node 4
		node.getChildren()[4] = new Leaf(Color.GRAY, boxSize/2);

		// create node 5
		node.getChildren()[5] = new Leaf(Color.BLACK, boxSize/2);

		// create node 6
		node.getChildren()[6] = new Leaf(Color.BLACK, boxSize/2);

		// create node 7
		node.getChildren()[7] = new Leaf(Color.GRAY, boxSize/2);
		
		return node;
	}
	
	public InternalNode getInernalNode()
	{
		return this.node;
	}


	public BoxParameters getBoxParameters() {
		return boxParameters;
	}


	public void setBoxParameters(BoxParameters boxParameters) {
		this.boxParameters = boxParameters;
	}
}
