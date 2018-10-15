package nl.tue.vc.application.visual;

import java.util.Random;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.NodeColor;
import nl.tue.vc.voxelengine.DeltaStruct;

public class ColoredBoxGenerator implements VoxelGenerator {

	private static final Color[] colors = { Color.AQUA, Color.BLACK, Color.BLUE, Color.BLUEVIOLET, Color.CHOCOLATE,
			Color.DARKGREEN, Color.DARKMAGENTA, Color.LIMEGREEN, Color.RED };

	public Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, NodeColor nodeColor) {
		Box box = new Box(boxParameters.getSizeX(), boxParameters.getSizeY(), boxParameters.getSizeZ());
		box.setTranslateX(boxParameters.getCenterX());
		box.setTranslateY(boxParameters.getCenterY());
		box.setTranslateZ(boxParameters.getCenterZ());
		PhongMaterial textureMaterial = new PhongMaterial();

		Color diffuseColor;
		if (nodeColor == NodeColor.BLACK) {
			Random rn = new Random();
			int index = rn.nextInt(colors.length);
			diffuseColor = colors[index];
		} else {
			diffuseColor = Color.TRANSPARENT;
		}

		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}

}
