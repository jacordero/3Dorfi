package nl.tue.vc.voxelengine;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import nl.tue.vc.model.OctreeUtils;

public class Octree {

	private Node root;
	private InternalNode node;
	private BoxParameters boxParameters;
	private Group octreeVolume;
	private double boxSize;
	private double centerX;
	private double centerY;
	private double centerZ;
	private int levels;

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

	public Octree(){
		
	}
	
	public Octree(BoxParameters boxParams, int levels) {
		this.boxSize = boxParams.getBoxSize();
		this.centerX = boxParams.getCenterX();
		this.centerY = boxParams.getCenterY();
		this.centerZ = boxParams.getCenterZ();
		this.levels = levels;
		this.node = new InternalNode(Color.BLACK, boxSize, this.centerX, this.centerY, this.centerZ, this.levels);
		root = node;		
		this.octreeVolume = new Group();
		this.boxParameters = boxParams;
	}
	
	public Octree(double size, double centerValX, double centerValY, double centerValZ, int levels) {
		this.boxSize = size;
		this.centerX = centerValX;
		this.centerY = centerValY;
		this.centerZ = centerValZ;
		this.levels = levels;
		this.node = new InternalNode(Color.BLACK, boxSize, centerX, centerY, centerZ, levels);
		root = node;		
		this.octreeVolume = new Group();
		this.boxParameters = new BoxParameters();
		this.boxParameters.setBoxSize((int)boxSize);
		this.boxParameters.setCenterX((int)centerX);
		this.boxParameters.setCenterY((int)centerY);
		this.boxParameters.setCenterZ((int)centerZ);
	}

	public void setRoot(Node root){
		this.root = root;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public Octree createScaledOctreeCopy(double boxSize){
		double centerX = boxSize / 2;
		double centerY = boxSize / 2;
		double centerZ = boxSize / 2;
		Octree octreeCopy = new Octree();
		Node rootCopy = createScaledNodeCopy(this.root, boxSize, centerX, centerY, centerZ);
		octreeCopy.setRoot(rootCopy);
		return octreeCopy;
	}
	
	private Node createScaledNodeCopy(Node currentNode, double boxSize, double centerX, double centerY, double centerZ){
		
		Node copyNode;
		if (currentNode.isLeaf()){
		
			copyNode = new Leaf();
			copyNode.setColor(currentNode.getColor());
			copyNode.setBoxSize(boxSize);
			copyNode.setPositionCenterX(centerX);
			copyNode.setPositionCenterY(centerY);
			copyNode.setPositionCenterZ(centerZ);
		
		} else {
			
			copyNode = new InternalNode();
			copyNode.setColor(currentNode.getColor());
			copyNode.setBoxSize(boxSize);
			copyNode.setPositionCenterX(centerX);
			copyNode.setPositionCenterY(centerY);
			copyNode.setPositionCenterZ(centerZ);
			
			Node[] children = new Node[8];
			double childrenBoxSize = boxSize / 2;
			for (int i = 0; i < currentNode.getChildren().length; i++){
				Node childrenNode = currentNode.getChildren()[i];
				DeltaStruct displacementDirections = OctreeUtils.computeDisplacementDirections(i);
				double displacementSize = childrenBoxSize / 2;
				
				//compute center of each children
				double childrenCenterX = centerX + (displacementDirections.deltaX * displacementSize);
				double childrenCenterY = centerY + (displacementDirections.deltaY * displacementSize);
				double childrenCenterZ = centerZ + (displacementDirections.deltaZ * displacementSize);
				
				children[i] = createScaledNodeCopy(childrenNode, childrenBoxSize, childrenCenterX, childrenCenterY, childrenCenterZ);
			}
			copyNode.addChildren(children);
		}
		return copyNode;
	}
	
	
	
	
	public Node generateOctreeFractal() {
		System.out.println("========================== Levels: " + this.levels);
		root = generateOctreeFractalAux(this.levels);
		return root;
	}

	public Node generateOctreeFractal(int level) {
//		DeltaStruct deltas = root.getDisplacementDirection();
//		BoxParameters params = root.getBoxParameters();
		root = generateOctreeFractalAux(level);
//		root.setBoxParameters(params);
//		root.setDisplacementDirection(deltas);
		return root;
	}

	private Node generateOctreeFractalAux(int level) {
		double nodesBoxSize = this.boxSize / 2;
		if (level == 0) {
			return generateInternalNode(nodesBoxSize);
		} else {
			Node internalNode = new InternalNode(Color.BLACK, nodesBoxSize);
			// create node 0
			internalNode.getChildren()[0] = generateOctreeFractalAux(level - 1);

			// create node 1
			internalNode.getChildren()[1] = generateOctreeFractalAux(level - 1);

			// create node 2
			internalNode.getChildren()[2] = generateOctreeFractalAux(level - 1);

			// create node 3
			internalNode.getChildren()[3] = generateOctreeFractalAux(level - 1);

			// create node 4
			internalNode.getChildren()[4] = generateOctreeFractalAux(level - 1);

			// create node 5
			internalNode.getChildren()[5] = generateOctreeFractalAux(level - 1);

			// create node 6
			internalNode.getChildren()[6] = generateOctreeFractalAux(level - 1);

			// create node 7
			internalNode.getChildren()[7] = generateOctreeFractalAux(level - 1);

			return internalNode;
		}

	}

	private Node generateInternalNode(double nodesBoxSize) {

		node.getChildren()[0] = new Leaf(Color.BLACK, nodesBoxSize/2);

		// create node 1
		node.getChildren()[1] = new Leaf(Color.RED, nodesBoxSize/2);

		// create node 2
		node.getChildren()[2] = new Leaf(Color.DARKGREEN, nodesBoxSize/2);

		// create node 3
		node.getChildren()[3] = new Leaf(Color.YELLOW, nodesBoxSize/2);

		// create node 4
		node.getChildren()[4] = new Leaf(Color.GRAY, nodesBoxSize/2);

		// create node 5
		node.getChildren()[5] = new Leaf(Color.WHITE, nodesBoxSize/2);

		// create node 6
		node.getChildren()[6] = new Leaf(Color.DARKBLUE, nodesBoxSize/2);

		// create node 7
		node.getChildren()[7] = new Leaf(Color.DARKVIOLET, nodesBoxSize/2);

		return node;
	}

	public InternalNode getInernalNode() {
		return this.node;
	}

	public BoxParameters getBoxParameters() {
		return boxParameters;
	}

	public void setBoxParameters(BoxParameters boxParameters) {
		this.boxParameters = boxParameters;
	}

	public Group getOctreeVolume() {
		return this.octreeVolume;
	}

	public void setOctreeVolume(Group octreeVolume) {
		this.octreeVolume = octreeVolume;
	}
	
	@Override
	public String toString(){
		return root.toString();
	}

}
