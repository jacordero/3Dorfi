package nl.tue.vc.model;

import javafx.scene.Group;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.model.BoxParameters;

public class Octree {

	private Node root;
	private Group octreeVolume;
	private double sizeX;
	private double sizeY;
	private double sizeZ;
	private double centerX;
	private double centerY;
	private double centerZ;
	private int octreeHeight;

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

	public Octree(BoxParameters boxParams, int octreeHeight) {
		this.sizeX = boxParams.getSizeX();
		this.sizeY = boxParams.getSizeY();
		this.sizeZ = boxParams.getSizeZ();
		this.centerX = boxParams.getCenterX();
		this.centerY = boxParams.getCenterY();
		this.centerZ = boxParams.getCenterZ();
		this.octreeHeight = octreeHeight;
		this.root = constructRootNode(NodeColor.BLACK, sizeX, sizeY, sizeZ, this.centerX, this.centerY, this.centerZ, this.octreeHeight);
		this.octreeVolume = new Group();
	}
	
	public Octree(double sizeX, double sizeY, double sizeZ, double centerValX, double centerValY, double centerValZ, int octreeHeight) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.centerX = centerValX;
		this.centerY = centerValY;
		this.centerZ = centerValZ;
		this.octreeHeight = octreeHeight;
		this.root = constructRootNode(NodeColor.BLACK, sizeX, sizeY, sizeZ, centerX, centerY, centerZ, octreeHeight);
		this.octreeVolume = new Group();
	}
	
	public void splitNodes(int newOctreeHeight){
		int deltaHeight = newOctreeHeight - octreeHeight;
		if (deltaHeight > 0){
			root = root.splitNode(deltaHeight, newOctreeHeight);
			octreeHeight = newOctreeHeight;
		}
	}

	private Node constructRootNode(NodeColor Color, double sizeX, double sizeY, double sizeZ, double centerX, double centerY, double centerZ, int octreeHeight){
		String message = "RootNode: {SizeX: " + sizeX + ", SizeY: " + sizeY + ", SizeZ: " + sizeZ;
		message += ", CenterX: " + centerX + ", CenterY: " + centerY + ", CenterZ: " + centerZ + ", Height: " + octreeHeight + "}";
		
		Utils.debugNewLine(message, true);
		if (octreeHeight > 0){
			return new InternalNode(Color, sizeX, sizeY, sizeZ, centerX, centerY, centerZ, octreeHeight, 1);
		} else {
			return new Leaf(Color, sizeX, sizeY, sizeZ, centerX, centerY, centerZ, 1);
		}
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void setRoot(Node root){
		this.root = root;
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

	public int getOctreeHeight() {
		return octreeHeight;
	}

	public void setLevels(int octreeHeight) {
		this.octreeHeight = octreeHeight;
	}

}
