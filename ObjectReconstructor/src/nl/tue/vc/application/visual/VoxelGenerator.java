package nl.tue.vc.application.visual;

import javafx.scene.shape.Box;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.NodeColor;
import nl.tue.vc.voxelengine.DeltaStruct;

public interface VoxelGenerator {
	public Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, NodeColor nodeColor, boolean debugMode);
}
