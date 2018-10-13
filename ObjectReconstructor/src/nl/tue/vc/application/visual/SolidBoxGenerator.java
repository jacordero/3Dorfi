package nl.tue.vc.application.visual;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.NodeColor;
import nl.tue.vc.voxelengine.DeltaStruct;

public class SolidBoxGenerator implements VoxelGenerator {

	public Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, NodeColor nodeColor, boolean debugMode){
		Box box = new Box(boxParameters.getSizeX(), boxParameters.getSizeY(), boxParameters.getSizeZ());
		box.setTranslateX(boxParameters.getCenterX());
		box.setTranslateY(boxParameters.getCenterY());
		box.setTranslateZ(boxParameters.getCenterZ());
		PhongMaterial textureMaterial = new PhongMaterial();
		 
		Color diffuseColor;
		if (debugMode){
			 if (nodeColor == NodeColor.WHITE){
				 diffuseColor = Color.TRANSPARENT;
			 } else if (nodeColor == NodeColor.BLACK){
				 diffuseColor = Color.BLACK;
			 } else {
				 diffuseColor = Color.GRAY;
			 }			
		} else {
			diffuseColor = nodeColor == NodeColor.BLACK ? Color.BLACK : Color.TRANSPARENT;
		}
		
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}

}
