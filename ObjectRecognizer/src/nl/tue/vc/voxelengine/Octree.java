package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import nl.tue.vc.application.ApplicationConfiguration;

public class Octree {

	private Node root;

	private int boxSize;
	private InternalNode node;
	private BoxParameters boxParameters;
	private List<BufferedImage> bufferedImagesForTest;
	private Group octreeVolume;
	private List<int[][]> sourceArrays;
	private List<int[][]> transformedArrays;

	/**
	 * +---------+-----------+ + 2 + 3 + | + -------+-----------+ | + 6 + 7 + | 3 |
	 * --------------------- | + | | | | 7 + | | 6 | 7 | + | 1 +
	 * --------------------+ | + | | | 5 + | 4 | 5 | + --------------------+
	 * 
	 */

	public Octree(int boxSize) {
		this.boxSize = boxSize;
		root = new InternalNode(Color.BLACK, boxSize);
		this.node = new InternalNode(Color.BLACK, boxSize);
		bufferedImagesForTest = new ArrayList<BufferedImage>();
		this.octreeVolume = new Group();
	}

	public Octree(int boxSize, int level, BoxParameters boxParameters) {
		this(boxSize, boxParameters);
	}

	public Octree(int boxSize, BoxParameters boxParameters) {
		this.boxSize = boxSize;
		root = new InternalNode(Color.BLACK, boxSize);
		this.node = new InternalNode(Color.BLACK, boxSize);
		bufferedImagesForTest = new ArrayList<BufferedImage>();
		this.octreeVolume = new Group();
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		root.setDeltaStruct(deltas);
		root.setBoxParameters(boxParameters);
		this.setBoxParameters(boxParameters);
	}

	public Node getRoot() {
		return root;
	}

	public void generateOctreeFractal(int level) {
		DeltaStruct deltas = root.getDeltaStruct();
		BoxParameters params = root.getBoxParameters();
		root = generateOctreeFractalAux(level);
		root.setBoxParameters(params);
		root.setDeltaStruct(deltas);
	}

	public void generateOctreeTest(int parentBoxSize) {
		Random random = new Random();
		int level = random.nextInt(3) + 1;
		System.out.println("Level for octree test: " + level);
		generateOctreeFractal(level);
	}

	public void generateOctreeTest(int parentBoxSize, int level) {
		System.out.println("Level for octree test: " + level);
		generateOctreeFractal(level);
	}

	private Node generateOctreeFractalAux(int level) {
		int nodesBoxSize = this.boxSize / 2;
		if (level == 0) {
			return generateInternalNode(nodesBoxSize);
		} else {
			Node internalNode = new InternalNode(Color.BLACK, this.boxSize);
			// create node 0
			// internalNode.getChildren()[0] = new Leaf(Color.BLACK, nodesBoxSize);
			internalNode.getChildren()[0] = generateOctreeFractalAux(level - 1);

			// create node 1
			// internalNode.getChildren()[1] = new Leaf(Color.BLUE, nodesBoxSize);
			internalNode.getChildren()[1] = generateOctreeFractalAux(level - 1);

			// create node 2
			// internalNode.getChildren()[2] = new Leaf(Color.BLUEVIOLET, nodesBoxSize);
			internalNode.getChildren()[2] = generateOctreeFractalAux(level - 1);

			// create node 3
			// internalNode.getChildren()[3] = new Leaf(Color.DARKGREEN, nodesBoxSize);
			internalNode.getChildren()[3] = generateOctreeFractalAux(level - 1);

			// create node 4
			internalNode.getChildren()[4] = generateOctreeFractalAux(level - 1);
			// internalNode.getChildren()[4] = new Leaf(Color.DARKORANGE, nodesBoxSize);

			// create node 5
			// internalNode.getChildren()[5] = new Leaf(Color.MAROON, nodesBoxSize);
			internalNode.getChildren()[5] = generateOctreeFractalAux(level - 1);

			// create node 6
			// internalNode.getChildren()[6] = new Leaf(Color.RED, nodesBoxSize);
			internalNode.getChildren()[6] = generateOctreeFractalAux(level - 1);

			// create node 7
			internalNode.getChildren()[7] = generateOctreeFractalAux(level - 1);
			// internalNode.getChildren()[7] = new Leaf(Color.PINK, nodesBoxSize);

			return internalNode;
		}

	}

	private Node generateInternalNode(int boxSize) {

		node.getChildren()[0] = new InternalNode(Color.GREEN, boxSize / 2);

		// create node 1
		node.getChildren()[1] = new InternalNode(Color.RED, boxSize / 2);

		// create node 2
		node.getChildren()[2] = new InternalNode(Color.YELLOW, boxSize / 2);

		// create node 3
		node.getChildren()[3] = new InternalNode(Color.BLACK, boxSize / 2);

		// create node 4
		node.getChildren()[4] = new InternalNode(Color.GRAY, boxSize / 2);

		// create node 5
		node.getChildren()[5] = new InternalNode(Color.PURPLE, boxSize / 2);

		// create node 6
		node.getChildren()[6] = new InternalNode(Color.BLUE, boxSize / 2);

		// create node 7
		node.getChildren()[7] = new InternalNode(Color.MAROON, boxSize / 2);

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

	public List<BufferedImage> getBufferedImagesForTest() {
		return bufferedImagesForTest;
	}

	public void setBufferedImagesForTest(List<BufferedImage> bufferedImagesForTest) {
		this.bufferedImagesForTest = bufferedImagesForTest;
	}

	public Group getOctreeVolume() {
		return generateVolume();
	}

	public Group getOctreeTestVolume(int level) {
		return generateTestedVolume(level);
	}

	public void setOctreeVolume(Group octreeVolume) {
		this.octreeVolume = octreeVolume;
	}

	public Group generateVolume() {
		Group volume = new Group();
		Node root = getRoot();

		System.out.println("Children: " + root.getChildren().length);
		List<Box> voxels = generateVolumeAux(root, getBoxParameters(), root.getDeltaStruct());
		volume.getChildren().addAll(voxels);
		return volume;
	}

	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		List<Box> voxels = new ArrayList<Box>();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			currentNode.setBoxParameters(currentParameters);
			currentNode.setDeltaStruct(currentDeltas);
			Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
			voxels.add(box);
		} else {
			Node[] children = currentNode.getChildren();
			int newBoxSize = currentParameters.getBoxSize() / 2;
			BoxParameters newParameters = new BoxParameters();
			newParameters.setBoxSize(newBoxSize);
			newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newBoxSize));
			newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newBoxSize));
			newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newBoxSize));

			for (int i = 0; i < children.length; i++) {
				// compute deltaX, deltaY, and deltaZ for new voxels
				Node childNode = children[i];
				childNode.setBoxParameters(newParameters);
				if (childNode != null) {
					DeltaStruct displacementDirections = computeDeltaDirections(i);
					childNode.setDeltaStruct(displacementDirections);
					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
				}
			}
		}
		return voxels;
	}

	public Group generateTestedVolume(int level) {
		Group volume = new Group();
		Node root = getRoot();
		System.out.println("Children: " + root.getChildren().length);
		// List<Box> voxels = generateTestedVolumeAux(root, getBoxParameters(),
		// root.getDeltaStruct(), 0, level);
		List<Box> voxels = new ArrayList<Box>();
		Node[] children = root.getChildren();
		int newBoxSize = root.getBoxSize() / 2;
		BoxParameters newParameters = new BoxParameters();
		newParameters.setBoxSize(newBoxSize);
		newParameters.setCenterX(getBoxParameters().getCenterX() + (root.getDeltaStruct().deltaX * newBoxSize));
		newParameters.setCenterY(getBoxParameters().getCenterY() + (root.getDeltaStruct().deltaY * newBoxSize));
		newParameters.setCenterZ(getBoxParameters().getCenterZ() + (root.getDeltaStruct().deltaZ * newBoxSize));

		for (int i = 0; i < children.length; i++) {
			Node childNode = children[i];
			if (childNode != null) {
				System.out.println("Root Child node: " + childNode);
				childNode.setBoxParameters(newParameters);
				DeltaStruct displacementDirections = computeDeltaDirections(i);
				childNode.setDeltaStruct(displacementDirections);
				voxels.addAll(generateTestedVolumeAux(childNode, newParameters, displacementDirections, 0, level));
			}
		}
		volume.getChildren().addAll(voxels);
		return volume;
	}

	private List<Box> generateTestedVolumeAux(Node currentNode, BoxParameters currentParameters,
			DeltaStruct currentDeltas, int initLevel, int maxLevel) {
		List<Box> voxels = new ArrayList<Box>();

		if (initLevel <= maxLevel) {
			if (currentNode == null) {
				return voxels;
			}

			// if(initLevel==0)
			// {
			// currentNode.setColor(Color.GRAY);
			// }

			if (currentNode.getColor() == Color.GRAY) {
				currentNode.getChildren()[0] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[1] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[2] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[3] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[4] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[5] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[6] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[7] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);

				Node[] children = currentNode.getChildren();
				int newBoxSize = currentParameters.getBoxSize() / 2;
				BoxParameters newParameters = new BoxParameters();
				newParameters.setBoxSize(newBoxSize);
				newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newBoxSize));
				newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newBoxSize));
				newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newBoxSize));

				for (int i = 0; i < children.length; i++) {
					Node childNode = children[i];
					if (childNode != null) {
						System.out.println("Child node: " + childNode);
						childNode.setBoxParameters(newParameters);
						DeltaStruct displacementDirections = computeDeltaDirections(i);
						childNode.setDeltaStruct(displacementDirections);
						voxels.addAll(generateTestedVolumeAux(childNode, newParameters, displacementDirections,
								initLevel + 1, maxLevel));
					}
				}
			} else {// if (currentNode.getColor() == Color.BLACK) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
				voxels.add(box);
				// } else {
			}
		}
		return voxels;
	}

	// Make the X, Y, and Z coordinates start at the corner of the first (0) node
	// and translate the rest of the nodes to their respective positions
	// Get rid of the center stuff
	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {
		Box box = new Box(boxParameters.getBoxSize(), boxParameters.getBoxSize(), boxParameters.getBoxSize());

		int posx = boxParameters.getCenterX() + (deltas.deltaX * boxParameters.getBoxSize() / 2);
		int posy = boxParameters.getCenterY() + (deltas.deltaY * boxParameters.getBoxSize() / 2);
		int posz = boxParameters.getCenterZ() + (deltas.deltaZ * boxParameters.getBoxSize() / 2);

		// ============================================================================================================================================================================
		// get the scene dimensions
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		int sceneWidth = appConfig.getVolumeSceneWidth();
		int sceneHeight = appConfig.getVolumeSceneHeight();
		int sceneDepth = appConfig.getVolumeSceneDepth();
		int volumeBoxSize = appConfig.getVolumeBoxSize();

		// define BoxParameters for the image
		BoxParameters imageBoxParameters = new BoxParameters();
		imageBoxParameters.setBoxSize(volumeBoxSize);
		imageBoxParameters.setCenterX(sceneWidth / 2);
		imageBoxParameters.setCenterY(sceneHeight / 2);
		imageBoxParameters.setCenterZ(sceneDepth / 2);
		// ============================================================================================================================================================================

		int focalLength = 1;
		int xCoordLowerLeft = posx - (boxParameters.getCenterX() / 2);
		int yCoordLowerLeft = posy - (boxParameters.getCenterY() / 2);
		int zCoordLowerLeft = posz - (boxParameters.getCenterZ() / 2);
		System.out.println("xCoordLowerLeft: " + xCoordLowerLeft + ", yCoordLowerLeft: " + yCoordLowerLeft
				+ ", zCoordLowerLeft: " + zCoordLowerLeft);

		if(zCoordLowerLeft<0)
			zCoordLowerLeft *= -1;
		
		int projectedX = xCoordLowerLeft / zCoordLowerLeft;// *(focalLength/zCoordLowerLeft);
		int projectedY = yCoordLowerLeft / zCoordLowerLeft;// *(focalLength/zCoordLowerLeft);
		System.out.println("Projected x: " + projectedX + ", projected y: " + projectedY);

		// TODO: Test the computation of the transformed value for different generated
		// volumes.
		int lowerLeftYValue = projectedY;// + boxParameters.getBoxSize();
		int transformedValue;
		Color diffuseColor = Color.GRAY;
		for (int i = 0; i < this.getBufferedImagesForTest().size(); i++) {
			// if (projectedX >= transformedArray.length || projectedX<0 || lowerLeftYValue
			// >= transformedArray[0].length) {
			// transformedValue = -1;
			// System.out.println("Something weird happened here!!!");
			// } else {

			int[][] transformedArray = transformedArrays.get(i);

			// define the image object and it's corresponding rectangle
			Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(i), null);
			Rectangle imageRect = new Rectangle();
			imageRect.setX(imageBoxParameters.getCenterX() - (imageBoxParameters.getCenterX() / 2));
			imageRect.setY(imageBoxParameters.getCenterY() - (imageBoxParameters.getCenterY() / 2));
			imageRect.setWidth(img.getWidth());
			imageRect.setHeight(img.getHeight());
			imageRect.setFill(new ImagePattern(img));

			Bounds boxBounds = box.getBoundsInLocal();
			System.out.println("Bounds local ----- " + boxBounds);
			System.out.println("Bounds Image local ----- " + imageRect.getBoundsInLocal());

			int xVal = projectedX;// Math.abs(((int) (imageRect.getX())-projectedX-1));
			int yVal = projectedY;// Math.abs((int) (imageRect.getY())-projectedY-1);
			System.out.println("imageRect x: " + imageRect.getX() + ", imageRect y: " + imageRect.getY());
			System.out.println("Getting transformed value for x = " + xVal + ", y = " + yVal);
			transformedValue = transformedArray[xVal][yVal];
			// }

			System.out.println("transformedValue: " + transformedValue);

			if (transformedValue >= boxParameters.getBoxSize()) {
				diffuseColor = getPaintColor(nodeColor, Color.BLACK);
			} else if ((transformedValue < boxParameters.getBoxSize()) && (transformedValue > 0)) {
				diffuseColor = getPaintColor(nodeColor, Color.GRAY);
			} else {
				diffuseColor = getPaintColor(nodeColor, Color.WHITE);
			}

		}

		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}

	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;
		if (currentColor == Color.BLACK) {
			result = newColor;
		} else if (currentColor == Color.GRAY) {
			if (newColor == Color.WHITE)
				result = Color.WHITE;
			else
				result = currentColor;
		} else {
			result = Color.WHITE;
		}

		return result;
	}

	public Group getDefaultVolume(BoxParameters boxParameters) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		Box box = generateVoxel(boxParameters, deltas, Color.CYAN);

		Group volume = new Group();
		volume.getChildren().addAll(box);
		return volume;
	}
	
	private DeltaStruct computeDeltaDirections(int index) {
		DeltaStruct deltas = new DeltaStruct();
		switch (index) {
		case 0:
			deltas.deltaX = -1;
			deltas.deltaY = 1;
			deltas.deltaZ = 1;
			break;
		case 1:
			deltas.deltaX = 1;
			deltas.deltaY = 1;
			deltas.deltaZ = 1;
			break;
		case 2:
			deltas.deltaX = -1;
			deltas.deltaY = -1;
			deltas.deltaZ = 1;
			break;
		case 3:
			deltas.deltaX = 1;
			deltas.deltaY = -1;
			deltas.deltaZ = 1;
			break;
		case 4:
			deltas.deltaX = -1;
			deltas.deltaY = 1;
			deltas.deltaZ = -1;
			break;
		case 5:
			deltas.deltaX = 1;
			deltas.deltaY = 1;
			deltas.deltaZ = -1;
			break;
		case 6:
			deltas.deltaX = -1;
			deltas.deltaY = -1;
			deltas.deltaZ = -1;
			break;
		case 7:
			deltas.deltaX = 1;
			deltas.deltaY = -1;
			deltas.deltaZ = -1;
			break;
		default:
			throw new RuntimeException("Invalid index value " + index);
		}

		return deltas;
	}
	
	public List<int[][]> getSourceArrays() {
		return sourceArrays;
	}

	public void setSourceArrays(List<int[][]> sourceBinaryArrays) {
		this.sourceArrays = sourceBinaryArrays;
	}

	public List<int[][]> getTransformedArrays() {
		return transformedArrays;
	}

	public void setTransformedArrays(List<int[][]> transformedBinaryArray) {
		this.transformedArrays = transformedBinaryArray;
	}
}
