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
			// System.out.println(">> Leaf ..");
			// ignore nodes with white color
			if (currentNode.getColor() != Color.WHITE) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor(), false);
				voxels.add(box);
			}
			// currentNode.setColor(Color.GRAY);
			// return voxels;
			// System.out.println("<< Leaf ..");
		} else {
			// currentNode.setColor(Color.GRAY);
			// working internal nodes
			// System.out.println(">> Internal node ..");
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

	// Make the X, Y, and Z coordinates start at the corner of the first (0) node
	// and translate the rest of the nodes to their respective positions
	// Get rid of the center stuff
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
		//============================================================================================================================================================================
		//get the scene dimensions
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		int sceneWidth = appConfig.getVolumeSceneWidth();
		int sceneHeight = appConfig.getVolumeSceneHeight();
		int sceneDepth = appConfig.getVolumeSceneDepth();
		int volumeBoxSize = appConfig.getVolumeBoxSize();
		
		//define BoxParameters for the image
		BoxParameters imageBoxParameters = new BoxParameters();		
		imageBoxParameters.setBoxSize(volumeBoxSize);
		imageBoxParameters.setCenterX(sceneWidth/2);
		imageBoxParameters.setCenterY(sceneHeight/2);
		imageBoxParameters.setCenterZ(sceneDepth/2);
		
		//String file_path = "C:\\Tools\\eclipse\\workspace\\objectrecognizer\\ObjectRecognizer\\images\\football.jpg";
		//File input = new File(file_path);
		//Image img = new Image(input.toURI().toString());
		
		
		
		
//============================================================================================================================================================================		
		if(testIntersection) {
			int focalLength = 1;
			int xCoordLowerLeft = posx-(boxParameters.getCenterX()/2);
			int yCoordLowerLeft = posy-(boxParameters.getCenterY()/2);
			int zCoordLowerLeft = posz-(boxParameters.getCenterZ()/2);
			System.out.println("xCoordLowerLeft: " + xCoordLowerLeft + ", yCoordLowerLeft: " + yCoordLowerLeft + ", zCoordLowerLeft: " + zCoordLowerLeft);
			
			int projectedX = xCoordLowerLeft/zCoordLowerLeft;//*(focalLength/zCoordLowerLeft);
			int projectedY = yCoordLowerLeft/zCoordLowerLeft;//*(focalLength/zCoordLowerLeft);
			System.out.println("Projected x: " + projectedX + ", projected y: " + projectedY);

			// TODO: Test the computation of the transformed value for different generated volumes.
			int lowerLeftYValue = projectedY;// + boxParameters.getBoxSize();
			int transformedValue;
			for (int i=0; i<transformedArrays.size();i++) {
//				if (projectedX >= transformedArray.length || projectedX<0 || lowerLeftYValue >= transformedArray[0].length) {
//					transformedValue = -1;
//					System.out.println("Something weird happened here!!!");
//				} else {
				
				int[][] transformedArray = transformedArrays.get(i);
				
				//define the image object and it's corresponding rectangle
				Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(i), null);
				Rectangle imageRect = new Rectangle();
				imageRect.setX(imageBoxParameters.getCenterX() - (imageBoxParameters.getCenterX()/2));
				imageRect.setY(imageBoxParameters.getCenterY() - (imageBoxParameters.getCenterY()/2));
				imageRect.setWidth(img.getWidth());
				imageRect.setHeight(img.getHeight());
				imageRect.setFill(new ImagePattern(img));
				
				Bounds boxBounds = box.getBoundsInLocal();
				System.out.println("Bounds local ----- " + boxBounds);
				System.out.println("Bounds Image local ----- " + imageRect.getBoundsInLocal());
				
				int xVal = projectedX;//Math.abs(((int) (imageRect.getX())-projectedX-1));
				int yVal = projectedY;//Math.abs((int) (imageRect.getY())-projectedY-1);
				System.out.println("imageRect x: " + imageRect.getX() + ", imageRect y: " + imageRect.getY());
				System.out.println("Getting transformed value for x = " + xVal + ", y = " + yVal);
				transformedValue = transformedArray[xVal][yVal];
				//}

				System.out.println("transformedValue: " + transformedValue);

				if (transformedValue >= boxParameters.getBoxSize()) {
					diffuseColor = getPaintColor(nodeColor, Color.BLACK);
				} else if((transformedValue < boxParameters.getBoxSize()) && (transformedValue > 0)) {
					diffuseColor = getPaintColor(nodeColor, Color.GRAY);
				}
				else {
					diffuseColor = getPaintColor(nodeColor, Color.WHITE);
				}
				
			}
		} 
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		Bounds boxBounds = box.getBoundsInLocal();
//		System.out.println("Bounds local ----- " + boxBounds);
//		System.out.println("Bounds parent ----- " + box.getBoundsInParent());
//		System.out.println("Bounds layout ----- " + box.getLayoutBounds());
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
