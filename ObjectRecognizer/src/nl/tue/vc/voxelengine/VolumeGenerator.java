package nl.tue.vc.voxelengine;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class VolumeGenerator {
	
	
	private Box generateTestBox(int size, int posx, int posy, int posz, Color color) {
		Box box = new Box(size, size, size);
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = color;
		textureMaterial.setDiffuseColor(diffuseColor);
		//textureMaterial.setDiffuseMap()
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
		//Coordinates for first box corner are {x: 400, y: 300, z: 400}
		volume.getChildren().add(generateTestBox(100, centerX - 50, centerY + 50, centerZ + 50, Color.BLACK));
				
		//Coordinates for second box corner are {x: 200, y: 300, z: 400}
		volume.getChildren().add(generateTestBox(100, centerX + 50, centerY + 50, centerZ + 50, Color.BLUE));

		//Coordinates for third box corner are {x: 400, y: 100, z: 400}
		volume.getChildren().add(generateTestBox(100, centerX - 50, centerY - 50, centerZ + 50, Color.BLUEVIOLET));
		
		//Coordinates for fourth box corner are {x: 200, y: 100, z: 400}
		volume.getChildren().add(generateTestBox(100, centerX + 50, centerY - 50, centerZ + 50, Color.DARKMAGENTA));
		**/

		//Coordinates for fifth box corner are {x: 400, y: 300, z: 200}
		int newBoxSize = boxSize/2;
		int newDelta = delta/2;
		
		int newCenterX = centerX - newBoxSize;
		int newCenterY = centerY + newBoxSize;
		int newCenterZ = centerZ - newBoxSize;
		
		debugPosition(newBoxSize, newDelta, newCenterX - newDelta, newCenterY + newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY + newDelta, newCenterZ + newDelta, Color.BLACK));
		
		//Coordinates for second box corner are {x: 200, y: 300, z: 400}
		debugPosition(newBoxSize, newDelta, newCenterX + newDelta, newCenterY + newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta, newCenterZ + newDelta, Color.BLUE));

		//Coordinates for third box corner are {x: 400, y: 100, z: 400}
		debugPosition(newBoxSize, newDelta, newCenterX - newDelta, newCenterY - newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta, newCenterZ + newDelta, Color.BLUEVIOLET));
		
		debugPosition(newBoxSize, newDelta, newCenterX + newDelta, newCenterY - newDelta, newCenterZ + newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY - newDelta, newCenterZ + newDelta, Color.DARKMAGENTA));
		
		// ignore cubes in position fifth and eight
		//Coordinates for sixth box corner are {x: 200, y: 300, z: 200}
		debugPosition(newBoxSize, newDelta, newCenterX + newDelta, newCenterY + newDelta, newCenterZ - newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta, newCenterZ - newDelta, Color.MAROON));
		
		//Coordinates for seventh box corner are {x: 400, y: 100, z: 200}
		debugPosition(newBoxSize, newDelta, newCenterX - newDelta, newCenterY - newDelta, newCenterZ - newDelta);
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta, newCenterZ - newDelta, Color.RED));
		
		
		//Coordinates for sixth box corner are {x: 200, y: 300, z: 200}
		debugPosition(boxSize, delta, centerX + delta, centerY + delta, centerZ - delta);
		volume.getChildren().add(generateTestBox(100, centerX + 50, centerY + 50, centerZ - 50, Color.MAROON));
		
		//Coordinates for seventh box corner are {x: 400, y: 100, z: 200}
		debugPosition(boxSize, delta, centerX - delta, centerY - delta, centerZ - delta);
		volume.getChildren().add(generateTestBox(100, centerX - 50, centerY - 50, centerZ - 50, Color.RED));
		
		
		newCenterX = centerX + newBoxSize;
		newCenterY = centerY - newBoxSize;
		newCenterZ = centerZ - newBoxSize;
		
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY + newDelta, newCenterZ + newDelta, Color.BLACK));
		
		//Coordinates for second box corner are {x: 200, y: 300, z: 400}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta, newCenterZ + newDelta, Color.BLUE));

		//Coordinates for third box corner are {x: 400, y: 100, z: 400}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta, newCenterZ + newDelta, Color.BLUEVIOLET));
		
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY - newDelta, newCenterZ + newDelta, Color.DARKMAGENTA));
		
		// ignore cubes in position fifth and eight
		//Coordinates for sixth box corner are {x: 200, y: 300, z: 200}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX + newDelta, newCenterY + newDelta, newCenterZ - newDelta, Color.MAROON));
		
		//Coordinates for seventh box corner are {x: 400, y: 100, z: 200}
		volume.getChildren().add(generateTestBox(newBoxSize, newCenterX - newDelta, newCenterY - newDelta, newCenterZ - newDelta, Color.RED));

		
		//Coordinates for eight box corner are {x: 200, y: 100, z: 200}
		return volume;
	}
	
	private void debugPosition(int boxSize, int delta, int centerX, int centerY, int centerZ) {
		String str = "BoxSize: " + boxSize + ", delta: " + delta + ", centerX: " + centerX + ", centerY: " + centerY + ", centerZ: " + centerZ;
		System.out.println(str);
	}
	
	public static Group generateVolume(Octree octree, BoxParameters boxParameters) {
		Group volume = new Group();
		Node root = octree.getRoot();
		
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		
		// First line of children
		for (int i = 0; i < root.getChildren().length; i++) {

			DeltaStruct displacementDirections = computeDeltaDirections(i);

			int newBoxSize = boxParameters.getBoxSize()/2;
			BoxParameters childrenParameters = new BoxParameters();
			childrenParameters.setBoxSize(newBoxSize);
			childrenParameters.setCenterX(boxParameters.getCenterX());
			childrenParameters.setCenterY(boxParameters.getCenterY());
			childrenParameters.setCenterZ(boxParameters.getCenterZ());

			Node childNode = root.getChildren()[i];
			//System.out.println("Index: "+ i + ", " + displacementDirections.toString());
			
			List<Box> voxels = generateVolumeAux(childNode, childrenParameters, displacementDirections);
			volume.getChildren().addAll(voxels);
		}
		
		return volume;
	}
	
	
	private static List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		// now we only care about the relative center position and the size of each cube
		String debugStr = "gva center position {x: " + currentParameters.getCenterX() + ", y: " + 
		currentParameters.getCenterY() + ", z: " + currentParameters.getCenterZ() + " }";		
		//System.out.println(debugStr);
		
				
		List<Box> voxels = new ArrayList<Box>();
		
		if (currentNode.isLeaf()) {
			// working with leafs
			// ignore nodes with white color
			if (currentNode.getColor() != Color.WHITE) {
			Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
				voxels.add(box);				
			}
			
		} else {
			// working internal nodes
			//System.out.println(">> Internal node ..");
			Node[] children = currentNode.getChildren();

			int newBoxSize = currentParameters.getBoxSize()/2;

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

					//System.out.println("Index: "+ i + ", " + displacementDirections.toString());
										
					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);					
				} 
			}			
			//System.out.println("<< Internal node ..");
		}
		
		return voxels;		
	}
	
	private static DeltaStruct computeDeltaDirections(int index) {
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
	private static Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {
		Box box = new Box(boxParameters.getBoxSize(), boxParameters.getBoxSize(), boxParameters.getBoxSize());
		
		int posx = boxParameters.getCenterX() + (deltas.deltaX * boxParameters.getBoxSize() / 2);
		int posy = boxParameters.getCenterY() + (deltas.deltaY * boxParameters.getBoxSize() / 2);
		int posz = boxParameters.getCenterZ() + (deltas.deltaZ * boxParameters.getBoxSize() / 2);		
		//System.out.println("Position {x: " + posx + ", y: " + posy + ", z: " + posz + "}, Size: " + boxParameters.getBoxSize() + "\n");
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);
		
		
		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = nodeColor;
		
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);

		return box;
	}

	public static Group getDefaultVolume(BoxParameters boxParameters) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		Box box = generateVoxel(boxParameters, deltas, Color.CYAN);
		Group volume = new Group();
		volume.getChildren().addAll(box);
		return volume;
	}
	
}
