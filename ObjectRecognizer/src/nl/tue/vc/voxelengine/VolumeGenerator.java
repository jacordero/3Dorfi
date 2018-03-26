package nl.tue.vc.voxelengine;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class VolumeGenerator {
	
	private Octree octree;
	private Group octreeVolume;
	private int[][] sourceArray;
	private int[][] transformedArray;
	
	public VolumeGenerator(Octree octree, BoxParameters boxParameters, int[][] sourceBinaryArray, int[][] transformedBinaryArray) {
		this.octree = octree;
		this.sourceArray = sourceBinaryArray;
		this.transformedArray = transformedBinaryArray;
		this.octreeVolume = generateVolume(boxParameters);
	}
	
	public Group generateVolume(BoxParameters boxParameters) {
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
		
		List<Box> voxels = generateVolumeAux(root, boxParameters, deltas);
		volume.getChildren().addAll(voxels);
		return volume;
	}
	
	
	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		// now we only care about the relative center position and the size of each cube
		//String debugStr = "gva center position {x: " + currentParameters.getCenterX() + ", y: " + 
		//currentParameters.getCenterY() + ", z: " + currentParameters.getCenterZ() + " }";		
		//System.out.println(debugStr);
		
		List<Box> voxels = new ArrayList<Box>();
		
		if (currentNode.isLeaf()) {
			// working with leafs
			//System.out.println(">> Leaf ..");
			// ignore nodes with white color
			if (currentNode.getColor() != Color.WHITE) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
				voxels.add(box);				
			}
			//currentNode.setColor(Color.GRAY);
			//return voxels;
			//System.out.println("<< Leaf ..");			
		} else {
			//currentNode.setColor(Color.GRAY);
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
	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {
		Box box = new Box(boxParameters.getBoxSize(), boxParameters.getBoxSize(), boxParameters.getBoxSize());
		
		int posx = boxParameters.getCenterX() + (deltas.deltaX * boxParameters.getBoxSize() / 2);
		int posy = boxParameters.getCenterY() + (deltas.deltaY * boxParameters.getBoxSize() / 2);
		int posz = boxParameters.getCenterZ() + (deltas.deltaZ * boxParameters.getBoxSize() / 2);		
		System.out.println("x: " + boxParameters.getCenterX() + ", y: " + boxParameters.getCenterY() + ", z: " + boxParameters.getCenterY());
		System.out.println("Position {x: " + posx + ", y: " + posy + ", z: " + posz + "}, Size: " + boxParameters.getBoxSize() + "\n");
		int projectedX = posx/posz;
		int projectedY = posy/posz;
		System.out.println("Projected x: " + projectedX + ", projected y: " + projectedY);
		int lowerLeftYValue = projectedY + boxParameters.getBoxSize();
		int transformedValue = this.transformedArray[projectedX][lowerLeftYValue];
		System.out.println("transformedValue: " + transformedValue);
		Color diffuseColor;
		if(transformedValue >= boxParameters.getBoxSize()) {
			diffuseColor = Color.BLACK;
		}
		else {
			diffuseColor = Color.GRAY;
		}
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		//diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		//textureMaterial.setDiffuseMap()
		box.setMaterial(textureMaterial);

		return box;
	}
	
	
	public Group getVolume() {
		return octreeVolume;
	}

	public int[][] getSourceArray() {
		return sourceArray;
	}

	public void setSourceArray(int[][] sourceArray) {
		this.sourceArray = sourceArray;
	}

	public int[][] getTransformedArray() {
		return transformedArray;
	}

	public void setTransformedArray(int[][] transformedArray) {
		this.transformedArray = transformedArray;
	}

}
