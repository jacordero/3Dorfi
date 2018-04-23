package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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

import java.lang.Math;

public class VolumeGenerator {

	private Octree octree;
	private Group octreeVolume;
	private List<int[][]> sourceArrays;
	private List<int[][]> transformedArrays;
	private List<BufferedImage> bufferedImagesForTest;

	public VolumeGenerator(Octree octree, BoxParameters boxParameters) 
	{
		this.octree = octree;
		this.bufferedImagesForTest = this.octree.getBufferedImagesForTest();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		if (octree == null) {
			this.octreeVolume = getDefaultVolume(boxParameters);
		} else {
			this.octreeVolume = generateVolume(boxParameters);
		}
	}
	
	public VolumeGenerator(Octree octree, BoxParameters boxParameters, List<int[][]> sourceBinaryArrays,
			List<int[][]> transformedBinaryArrays) {
		this.octree = octree;
		this.bufferedImagesForTest = this.octree.getBufferedImagesForTest();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		this.sourceArrays = sourceBinaryArrays;
		this.transformedArrays = transformedBinaryArrays;

		if (octree == null) {
			this.octreeVolume = getDefaultVolume(boxParameters);
		} else {
			this.octreeVolume = generateVolume(boxParameters);
		}
	}

	private Box generateTestBox(int size, int posx, int posy, int posz, Color color) {
		Box box = new Box(size, size, size);
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = color;
		textureMaterial.setDiffuseColor(diffuseColor);
		// textureMaterial.setDiffuseMap()
		box.setMaterial(textureMaterial);
		return box;
	}

	private Group generateTestVolume() {
		Group volume = new Group();

		int centerX = 400;
		int centerY = 300;
		int centerZ = 400;
		int boxSize = 100;
		int delta = 50;

		/**
		 * //Coordinates for first box corner are {x: 400, y: 300, z: 400}
		 * volume.getChildren().add(generateTestBox(100, centerX - 50, centerY + 50,
		 * centerZ + 50, Color.BLACK));
		 * 
		 * //Coordinates for second box corner are {x: 200, y: 300, z: 400}
		 * volume.getChildren().add(generateTestBox(100, centerX + 50, centerY + 50,
		 * centerZ + 50, Color.BLUE));
		 * 
		 * //Coordinates for third box corner are {x: 400, y: 100, z: 400}
		 * volume.getChildren().add(generateTestBox(100, centerX - 50, centerY - 50,
		 * centerZ + 50, Color.BLUEVIOLET));
		 * 
		 * //Coordinates for fourth box corner are {x: 200, y: 100, z: 400}
		 * volume.getChildren().add(generateTestBox(100, centerX + 50, centerY - 50,
		 * centerZ + 50, Color.DARKMAGENTA));
		 **/

		// Coordinates for fifth box corner are {x: 400, y: 300, z: 200}
		int newBoxSize = boxSize / 2;
		int newDelta = delta / 2;

		int newCenterX = centerX - newBoxSize;
		int newCenterY = centerY + newBoxSize;
		int newCenterZ = centerZ - newBoxSize;

		debugPosition(newBoxSize, newDelta, newCenterX - newDelta, newCenterY + newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY + newDelta,
				newCenterZ + newDelta, Color.BLACK));

		// Coordinates for second box corner are {x: 200, y: 300, z: 400}
		debugPosition(newBoxSize, newDelta, newCenterX + newDelta, newCenterY + newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta,
				newCenterZ + newDelta, Color.BLUE));

		// Coordinates for third box corner are {x: 400, y: 100, z: 400}
		debugPosition(newBoxSize, newDelta, newCenterX - newDelta, newCenterY - newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta,
				newCenterZ + newDelta, Color.BLUEVIOLET));

		debugPosition(newBoxSize, newDelta, newCenterX + newDelta, newCenterY - newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY - newDelta,
				newCenterZ + newDelta, Color.DARKMAGENTA));

		// ignore cubes in position fifth and eight
		// Coordinates for sixth box corner are {x: 200, y: 300, z: 200}
		debugPosition(newBoxSize, newDelta, newCenterX + newDelta, newCenterY + newDelta, newCenterZ - newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta,
				newCenterZ - newDelta, Color.MAROON));

		// Coordinates for seventh box corner are {x: 400, y: 100, z: 200}
		debugPosition(newBoxSize, newDelta, newCenterX - newDelta, newCenterY - newDelta, newCenterZ - newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta,
				newCenterZ - newDelta, Color.RED));

		// Coordinates for sixth box corner are {x: 200, y: 300, z: 200}
		debugPosition(boxSize, delta, centerX + delta, centerY + delta, centerZ - delta);
		volume.getChildren().add(generateTestBox(100, centerX + 50, centerY + 50, centerZ - 50, Color.MAROON));

		// Coordinates for seventh box corner are {x: 400, y: 100, z: 200}
		debugPosition(boxSize, delta, centerX - delta, centerY - delta, centerZ - delta);
		volume.getChildren().add(generateTestBox(100, centerX - 50, centerY - 50, centerZ - 50, Color.RED));

		newCenterX = centerX + newBoxSize;
		newCenterY = centerY - newBoxSize;
		newCenterZ = centerZ - newBoxSize;

		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY + newDelta,
				newCenterZ + newDelta, Color.BLACK));

		// Coordinates for second box corner are {x: 200, y: 300, z: 400}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta,
				newCenterZ + newDelta, Color.BLUE));

		// Coordinates for third box corner are {x: 400, y: 100, z: 400}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta,
				newCenterZ + newDelta, Color.BLUEVIOLET));

		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY - newDelta,
				newCenterZ + newDelta, Color.DARKMAGENTA));

		// ignore cubes in position fifth and eight
		// Coordinates for sixth box corner are {x: 200, y: 300, z: 200}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta,
				newCenterZ - newDelta, Color.MAROON));

		// Coordinates for seventh box corner are {x: 400, y: 100, z: 200}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta,
				newCenterZ - newDelta, Color.RED));

		// Coordinates for eight box corner are {x: 200, y: 100, z: 200}
		return volume;
	}

	private void debugPosition(int boxSize, int delta, int centerX, int centerY, int centerZ) {
		String str = "BoxSize: " + boxSize + ", delta: " + delta + ", centerX: " + centerX + ", centerY: " + centerY
				+ ", centerZ: " + centerZ;
		System.out.println(str);
	}

	public Group generateVolume(BoxParameters boxParameters) {
		System.out.println("BOX = " + boxParameters);
		Group volume = new Group();
		Node root = octree.getRoot();

		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		System.out.println("Children: " + root.getChildren().length);

		// First line of children
		for (int i = 0; i < root.getChildren().length; i++) {

			DeltaStruct displacementDirections = computeDeltaDirections(i);
			int newBoxSize = boxParameters.getBoxSize() / 2;
			BoxParameters childrenParameters = new BoxParameters();
			childrenParameters.setBoxSize(newBoxSize);
			childrenParameters.setCenterX(boxParameters.getCenterX());
			childrenParameters.setCenterY(boxParameters.getCenterY());
			childrenParameters.setCenterZ(boxParameters.getCenterZ());

			Node childNode = root.getChildren()[i];
			// System.out.println("Index: "+ i + ", " + displacementDirections.toString());
			//System.out.println("generateVolume | Nodecolor = " + childNode.getColor().toString());
			List<Box> voxels = generateVolumeAux(childNode, childrenParameters, displacementDirections);
			volume.getChildren().addAll(voxels);
		}

		List<Box> voxels = generateVolumeAux(root, boxParameters, deltas);
		volume.getChildren().addAll(voxels);
		return volume;
	}

	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		//System.out.println("generateVolumeAux | Nodecolor = " + currentNode.getColor().toString());
		List<Box> voxels = new ArrayList<Box>();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			if (currentNode.getColor() != Color.WHITE) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor(), false);
				voxels.add(box);
			}
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
				if (childNode != null) {
					DeltaStruct displacementDirections = computeDeltaDirections(i);

					// System.out.println("Index: "+ i + ", " + displacementDirections.toString());
					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
				}
			}
			// System.out.println("<< Internal node ..");
		}

		return voxels;
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

	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor, Boolean testIntersection) {
		Box box = new Box(boxParameters.getBoxSize(), boxParameters.getBoxSize(), boxParameters.getBoxSize());

		int posx = boxParameters.getCenterX() + (deltas.deltaX * boxParameters.getBoxSize() / 2);
		int posy = boxParameters.getCenterY() + (deltas.deltaY * boxParameters.getBoxSize() / 2);
		int posz = boxParameters.getCenterZ() + (deltas.deltaZ * boxParameters.getBoxSize() / 2);

		System.out.println("x: " + boxParameters.getCenterX() + ", y: " + boxParameters.getCenterY() + ", z: "
				+ boxParameters.getCenterY());
		System.out.println("Position {x: " + posx + ", y: " + posy + ", z: " + posz + "}, Size: "
				+ boxParameters.getBoxSize() + "\n");
		Color diffuseColor = nodeColor;

			int transformedValue;
//			for (int i=0; i<transformedArrays.size();i++) {
////				
//				int[][] transformedArray = transformedArrays.get(i);
//				transformedValue = transformedArray[xVal][yVal];
//				
//				System.out.println("transformedValue: " + transformedValue);
//
//				if (transformedValue >= boxParameters.getBoxSize()) {
//					diffuseColor = getPaintColor(nodeColor, Color.BLACK);
//				} else if((transformedValue < boxParameters.getBoxSize()) && (transformedValue > 0)) {
//					diffuseColor = getPaintColor(nodeColor, Color.GRAY);
//				}
//				else {
//					diffuseColor = getPaintColor(nodeColor, Color.WHITE);
//				}
//				
//			}
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		Bounds boxBounds = box.getBoundsInLocal();
		return box;
	}
	
	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;
		if(currentColor == Color.BLACK) {
			result = newColor;
		}
		else if(currentColor == Color.GRAY) {
			if(newColor==Color.WHITE)
				result = Color.WHITE;
			else
				result = currentColor;
		}
		else {
			result = Color.WHITE;
		}
		
		return result;
	}

	public Group getVolume() {
		return octreeVolume;
	}

	public Group getDefaultVolume(BoxParameters boxParameters) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		Box box = generateVoxel(boxParameters, deltas, Color.CYAN, false);
		
		Group volume = new Group();
		volume.getChildren().addAll(box);
		
		String file_path = "C:\\Tools\\eclipse\\workspace\\objectrecognizer\\ObjectRecognizer\\images\\football.jpg";
		File input = new File(file_path);
		Rectangle rec = new Rectangle();
		rec.setX(5+boxParameters.getCenterX()-(boxParameters.getBoxSize()/2));
		rec.setY(boxParameters.getCenterY());//-(boxParameters.getBoxSize()/2));
		rec.setWidth(boxParameters.getBoxSize());
		rec.setHeight(boxParameters.getBoxSize());
		Image img = new Image(input.toURI().toString());
		rec.setFill(new ImagePattern(img));
		//volume.getChildren().add(rec);
		
		return volume;
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

	public List<BufferedImage> getBufferedImagesForTest() {
		return bufferedImagesForTest;
	}

	public void setBufferedImagesForTest(List<BufferedImage> bufferedImagesForTest) {
		this.bufferedImagesForTest = bufferedImagesForTest;
	}

}
