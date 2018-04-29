package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Mat;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.TransformMatrices;
import nl.tue.vc.projection.Vector3D;
import nl.tue.vc.projection.VolumeModel;

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

	public Octree(BoxParameters boxParams, int levels) {
		this.boxSize = boxParams.getBoxSize();
		this.centerX = boxParams.getCenterX();
		this.centerY = boxParams.getCenterY();
		this.centerZ = boxParams.getCenterZ();
		this.levels = levels;
		this.node = new InternalNode(Color.BLACK, boxSize, centerX, centerY, centerZ, levels);
		root = node;		
		this.octreeVolume = new Group();
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		this.boxParameters = boxParams;
	}

	public Node getRoot() {
		return root;
	}

	public Node generateOctreeFractal(int level) {
		root = generateOctreeFractalAux(level);
		return root;
	}

	private Node generateOctreeFractalAux(int level) {
		double nodesBoxSize = this.boxSize / 2;
		if (level == 0) {
			return generateInternalNode(nodesBoxSize);
		} else {
			Node internalNode = new InternalNode(Color.BLACK, nodesBoxSize, this.centerX, this.centerY, this.centerZ, level);
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

		node.getChildren()[0] = new Leaf(Color.BLACK, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

		// create node 1
		node.getChildren()[1] = new Leaf(Color.RED, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

		// create node 2
		node.getChildren()[2] = new Leaf(Color.GREEN, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

		// create node 3
		node.getChildren()[3] = new Leaf(Color.YELLOW, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

		// create node 4
		node.getChildren()[4] = new Leaf(Color.GRAY, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

		// create node 5
		node.getChildren()[5] = new Leaf(Color.BROWN, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

		// create node 6
		node.getChildren()[6] = new Leaf(Color.CYAN, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

		// create node 7
		node.getChildren()[7] = new Leaf(Color.ORANGE, nodesBoxSize, this.centerX, this.centerY, this.centerZ);

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
