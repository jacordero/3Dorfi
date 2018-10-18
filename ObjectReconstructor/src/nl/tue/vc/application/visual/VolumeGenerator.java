package nl.tue.vc.application.visual;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.Group;
import javafx.scene.shape.Box;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.Node;
import nl.tue.vc.model.NodeColor;
import nl.tue.vc.model.Octree;
import nl.tue.vc.model.OctreeUtils;
import nl.tue.vc.model.ProjectedPoint;
import nl.tue.vc.projection.BoundingBox;
import nl.tue.vc.voxelengine.DeltaStruct;

public class VolumeGenerator {

	private List<ProjectedPoint> projectedPoints;
	private List<BoundingBox> boundingBoxes;
	private List<Box> voxels;
	private Group octreeVolume;
	private VoxelGenerator voxelGenerator;
	private static final int SCALE_FACTOR = 10;
	private int maximumDepth;
	private static final Logger logger = Logger.getLogger(VolumeGenerator.class.getName());
	
	public VolumeGenerator(VoxelGenerator voxelGenerator, int maximumDepth) {
		this.voxelGenerator = voxelGenerator;
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
		voxels = new ArrayList<Box>();
		this.maximumDepth = maximumDepth;
	}
	
	public void generateVolume(Octree octree, BoxParameters volumeBoxParameters){
		logger.log(Level.INFO, "Generating volume");
		BoxParameters scaledBoxParameters = new BoxParameters();
		scaledBoxParameters.setCenterX(0);
		scaledBoxParameters.setCenterY(0);
		scaledBoxParameters.setCenterZ(0);
		scaledBoxParameters.setSizeX(volumeBoxParameters.getSizeX()*SCALE_FACTOR);
		scaledBoxParameters.setSizeY(volumeBoxParameters.getSizeY()*SCALE_FACTOR);
		scaledBoxParameters.setSizeZ(volumeBoxParameters.getSizeZ()*SCALE_FACTOR);
		
		DeltaStruct rootDeltas = new DeltaStruct();
		rootDeltas.deltaX = 0;
		rootDeltas.deltaY = 0;
		rootDeltas.deltaZ = 0;
		rootDeltas.index = 0;

		voxels = generateVolumeAux(octree.getRoot(), scaledBoxParameters, rootDeltas, 0);
	}
	
	
	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas, int depth) {

		List<Box> voxels = new ArrayList<Box>();
		if (currentNode == null || depth > maximumDepth || currentNode.getColor() == NodeColor.WHITE) {
			return voxels;
		}

		if (currentNode.isLeaf() || depth == maximumDepth || currentNode.getColor() == NodeColor.BLACK) {
			Box box = voxelGenerator.generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
			voxels.add(box);
		} else {
			Node[] children = currentNode.getChildren();
			double childrenSizeX = currentParameters.getSizeX() / 2;
			double childrenSizeY = currentParameters.getSizeY() / 2;
			double childrenSizeZ = currentParameters.getSizeZ() / 2;			
			

			for (int i = 0; i < children.length; i++) {
				
				Node childNode = children[i];
				if (childNode != null) {

					DeltaStruct displacementDirections = OctreeUtils.computeDisplacementDirections(i);
					double displacementX = displacementDirections.deltaX * (childrenSizeX / 2);
					double displacementY = displacementDirections.deltaY * (childrenSizeY / 2);
					double displacementZ = displacementDirections.deltaZ * (childrenSizeZ / 2);

					BoxParameters childrenParameters = new BoxParameters();
					childrenParameters.setSizeX(childrenSizeX);
					childrenParameters.setSizeY(childrenSizeY);
					childrenParameters.setSizeZ(childrenSizeZ);
		
					childrenParameters.setCenterX(currentParameters.getCenterX() + displacementX);
					childrenParameters.setCenterY(currentParameters.getCenterY() + displacementY);
					childrenParameters.setCenterZ(currentParameters.getCenterZ() + displacementZ);
					
					List<Box> innerBoxes = generateVolumeAux(childNode, childrenParameters, displacementDirections, depth+1);
					voxels.addAll(innerBoxes);
				}
			}
		}
		return voxels;
	}
	
	public List<BoundingBox> getBoundingBoxes() {
		return boundingBoxes;
	}

	public List<ProjectedPoint> getProjections() {
		return projectedPoints;
	}
	
	public Group getVolume() {
		return octreeVolume;
	}

	public List<Box> getVoxels(){
		return voxels;
	}
}
