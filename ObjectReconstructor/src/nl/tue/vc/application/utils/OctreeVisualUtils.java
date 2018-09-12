package nl.tue.vc.application.utils;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.Node;
import nl.tue.vc.model.Octree;
import nl.tue.vc.voxelengine.DeltaStruct;

public class OctreeVisualUtils {

	
	public static List<Box> colorForDebug(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas){
		List<Box> voxels = new ArrayList<Box>();
		// System.out.println("========================== generateVolumeAux: " +
		// currentNode + "| " + currentParameters.getBoxSize());
		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			Box box = generateColoredVoxel(currentParameters, currentDeltas, currentNode.getColor());
			voxels.add(box);
		} else {
			Node[] children = currentNode.getChildren();
			double newSizeX = currentParameters.getSizeX() / 2;
			double newSizeY = currentParameters.getSizeY() / 2;
			double newSizeZ = currentParameters.getSizeZ() / 2;			
			
			BoxParameters newParameters = new BoxParameters();
			newParameters.setSizeX(newSizeX);
			newParameters.setSizeY(newSizeY);
			newParameters.setSizeZ(newSizeZ);
			newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newSizeX));
			newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newSizeY));
			newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newSizeZ));

			for (int i = 0; i < children.length; i++) {
				// compute deltaX, deltaY, and deltaZ for new voxels
				Node childNode = children[i];
				if (childNode != null) {
					DeltaStruct displacementDirections = computeDeltaDirections(i);
					List<Box> innerBoxes = colorForDebug(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
				}
			}
		}

		return voxels;

	}
	
	private static Box generateColoredVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {

		double sizeX = boxParameters.getSizeX();		
		double sizeY = boxParameters.getSizeY();
		double sizeZ = boxParameters.getSizeZ();
		Box box = new Box(sizeX, sizeY, sizeZ);
		
		double voxelCenterX = boxParameters.getCenterX();
		double voxelCenterY = boxParameters.getCenterY();
		double voxelCenterZ = boxParameters.getCenterZ();
		
		double posx = voxelCenterX + (deltas.deltaX * (sizeX / 2));
		double posy = voxelCenterY + (deltas.deltaY * (sizeY / 2));
		double posz = voxelCenterZ + (deltas.deltaZ * (sizeZ / 2));
		
		//Mirror the position in the y axis. So far only works for a maximum value of 80		
		box.setTranslateX(posx);
		
		
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();

		Color diffuseColor = nodeColor;

		if (nodeColor.equals(Color.BLACK)){
			switch(deltas.index){
			case 0:
				diffuseColor = Color.MAROON;
				break;
			case 1:
				diffuseColor = Color.RED;
				break;
			case 2:
				diffuseColor = Color.ORANGE;
				break;
			case 3:
				diffuseColor = Color.YELLOW;
				break;
			case 4:
				diffuseColor = Color.DARKBLUE;
				break;
			case 5:
				diffuseColor = Color.BLUE;
				break;
			case 6:
				diffuseColor = Color.STEELBLUE;
				break;
			case 7:
				diffuseColor = Color.SKYBLUE;
				break;
			default:
				diffuseColor = nodeColor;
				break;	
			}
		}
		 
		 
		 //diffuseColor = nodeColor == Color.BLACK ? nodeColor : Color.TRANSPARENT;
		 diffuseColor = nodeColor == Color.WHITE ? Color.TRANSPARENT: diffuseColor;
		
		 textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}
	
	
	private static DeltaStruct computeDeltaDirections(int index) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.index = index;
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

	public static Octree generateOctreeTest(){
		BoxParameters boxParameters = new BoxParameters();
		boxParameters.setCenterX(2.0);
		boxParameters.setCenterY(2.0);
		boxParameters.setCenterZ(2.0);
		boxParameters.setSizeX(4);
		boxParameters.setSizeY(4);
		boxParameters.setSizeZ(4);
		
		return new Octree(boxParameters, 4);
	}
	
	
}
