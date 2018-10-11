package nl.tue.vc.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.projection.BoundingBox;
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.voxelengine.DeltaStruct;
import nl.tue.vc.voxelengine.SimpleRectangle;

public class VolumeGenerator {

	private List<ProjectedPoint> projectedPoints;
	private List<BoundingBox> boundingBoxes;
	private List<Box> voxels;
	private Group octreeVolume;

	
	public VolumeGenerator() {
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
		voxels = new ArrayList<Box>();
	}
	
	public void generateVolume(Octree refinedOctree, BoxParameters volumeBoxParameters){
				
		int scaleFactor = 10;
		BoxParameters scaledBoxParameters = new BoxParameters();
		scaledBoxParameters.setCenterX(0);
		scaledBoxParameters.setCenterY(0);
		scaledBoxParameters.setCenterZ(0);
		scaledBoxParameters.setSizeX(volumeBoxParameters.getSizeX()*scaleFactor);
		scaledBoxParameters.setSizeY(volumeBoxParameters.getSizeY()*scaleFactor);
		scaledBoxParameters.setSizeZ(volumeBoxParameters.getSizeZ()*scaleFactor);
		
		DeltaStruct rootDeltas = new DeltaStruct();
		rootDeltas.deltaX = 0;
		rootDeltas.deltaY = 0;
		rootDeltas.deltaZ = 0;
		rootDeltas.index = 0;
		
		voxels = generateVolumeAux(refinedOctree.getRoot(), scaledBoxParameters, rootDeltas);
	}
	

	
	
	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {

		List<Box> voxels = new ArrayList<Box>();
		// System.out.println("========================== generateVolumeAux: " +
		// currentNode + "| " + currentParameters.getBoxSize());
		if (currentNode == null || currentNode.getColor() == NodeColor.WHITE) {
			//Utils.debugNewLine("Current color is white", true);
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			//Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
			Box box = generateVoxelAux(currentParameters, currentDeltas, currentNode.getColor());
			voxels.add(box);
		} else {
			
			
			Node[] children = currentNode.getChildren();
			double childrenSizeX = currentParameters.getSizeX() / 2;
			double childrenSizeY = currentParameters.getSizeY() / 2;
			double childrenSizeZ = currentParameters.getSizeZ() / 2;			
			

			for (int i = 0; i < children.length; i++) {
				// compute deltaX, deltaY, and deltaZ for new voxels
				
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
					
					List<Box> innerBoxes = generateVolumeAux(childNode, childrenParameters, displacementDirections);
					voxels.addAll(innerBoxes);
				}
			}
		}
		return voxels;
	}




	
	private Box generateVoxelAux(BoxParameters boxParameters, DeltaStruct deltas, NodeColor nodeColor){
		Box box = new Box(boxParameters.getSizeX(), boxParameters.getSizeY(), boxParameters.getSizeZ());
		box.setTranslateX(boxParameters.getCenterX());
		box.setTranslateY(boxParameters.getCenterY());
		box.setTranslateZ(boxParameters.getCenterZ());
		PhongMaterial textureMaterial = new PhongMaterial();
		 
		 Color diffuseColor = nodeColor == NodeColor.BLACK ? Color.BLACK : Color.TRANSPARENT;
		 //diffuseColor = nodeColor;
		 //diffuseColor = nodeColor == Color.WHITE ? Color.TRANSPARENT: nodeColor;
		
		 textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}

	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {

		double sceneWidth = boxParameters.getCenterX();
		double sceneHeight = boxParameters.getCenterY();
		double sceneDepth = boxParameters.getCenterZ();
		double sizeX = boxParameters.getSizeX();
		
		// invert the axis
		double sizeY = boxParameters.getSizeY();
		double sizeZ = boxParameters.getSizeZ();
		
		
		Box box = new Box(sizeX, sizeY, sizeZ);
		double posx = sceneWidth + (deltas.deltaX * (sizeX / 2));
		
		// invert the axis
		double posy = sceneHeight + (deltas.deltaY * (sizeY / 2));
		double posz = sceneDepth + (deltas.deltaZ * (sizeZ / 2));
		
		//Mirror the position in the y axis. So far only works for a maximum value of 80		
		box.setTranslateX(posx);
		
		
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();

		Color diffuseColor = nodeColor;
		 
		 diffuseColor = nodeColor == Color.BLACK ? nodeColor : Color.TRANSPARENT;
		 //diffuseColor = nodeColor == Color.WHITE ? Color.TRANSPARENT: nodeColor;
		
		 textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}

	public List<BoundingBox> getBoundingBoxes() {
		return boundingBoxes;
	}

	public List<ProjectedPoint> getProjections() {
		return projectedPoints;
	}

	public Group getProjectedVolume() {
		Group root2D = new Group();

		//Utils.debugNewLine("Projected points length: " + projectedPoints.size(), true);
		for (ProjectedPoint projection : projectedPoints) {
			Ellipse circle = new Ellipse(projection.getScaledX(), projection.getScaledY(), 5, 5);
			circle.setFill(Color.RED);
			root2D.getChildren().add(circle);
		}


		//Utils.debugNewLine("Bounding boxes length: " + boundingBoxes.size(), true);
		for (BoundingBox boundingBox : boundingBoxes) {
			// Ellipse circle = new Ellipse(boundingBox.getScaledRectangle().getX(),
			// (boundingBox.getScaledRectangle().getY()+boundingBox.getScaledRectangle().getHeight()),
			// 5, 5);
			// circle.setFill(Color.YELLOW);
			// root2D.getChildren().add(circle);

			/**
			 * System.out.println(boundingBox.getScaledRectangle().getFill());
			 * System.out.println(boundingBox.getScaledRectangle().getX());
			 * System.out.println(boundingBox.getScaledRectangle().getY());
			 * System.out.println(boundingBox.getScaledRectangle().getWidth());
			 * System.out.println(boundingBox.getScaledRectangle().getHeight());
			 **/

			SimpleRectangle sr = boundingBox.getScaledRectangle();
			Rectangle visualRectangle = new Rectangle(sr.getX(), sr.getY(), sr.getWidth(), sr.getHeight());
			visualRectangle.setStroke(Color.BLACK);
			visualRectangle.setFill(Color.TRANSPARENT);

			root2D.getChildren().add(visualRectangle);
		}
		return root2D;
	}

	public void projectOctreeIntoImage(Mat testImage) {

	}


	private List<ProjectedPoint> projectionsAsList(MatOfPoint2f encodedProjections) {
		List<ProjectedPoint> projections = new ArrayList<ProjectedPoint>();
		for (Point point : encodedProjections.toList()) {
			// TODO: change this way of assigning the scale factor
			projections.add(new ProjectedPoint(point.x, point.y, 2.0));
		}
		return projections;
	}

	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;

		// if (currentColor == Color.WHITE)
		// currentColor = Color.TRANSPARENT;
		//
		// if (newColor == Color.WHITE)
		// newColor = Color.TRANSPARENT;

		if (currentColor == Color.GRAY) {
			if (newColor == Color.WHITE || newColor == Color.GRAY)
				result = newColor;
			else
				result = currentColor;
		} else if (currentColor == Color.WHITE) {
			result = currentColor;
		} else {
			result = newColor;
		}

		return result;
	}

	public NodeColor computeColor(NodeColor oldColor, IntersectionStatus testResult){
		// Color is white by default unless previous color is white or gray
		NodeColor newColor = NodeColor.WHITE;
		if (oldColor.equals(NodeColor.BLACK)){
			if (testResult == IntersectionStatus.INSIDE){
				newColor = NodeColor.BLACK;
			} else if (testResult == IntersectionStatus.PARTIAL){
				newColor = NodeColor.GRAY;
			} else {
				newColor = NodeColor.WHITE;
			}
		} else if (oldColor.equals(NodeColor.GRAY)){
			if (testResult == IntersectionStatus.INSIDE){
				newColor = NodeColor.GRAY;
			} else if (testResult == IntersectionStatus.PARTIAL){
				newColor = NodeColor.GRAY;
			} else {
				newColor = NodeColor.WHITE;
			}
		}
		
		return newColor;
	}
	
	public Group getVolume() {
		return octreeVolume;
	}

	public Group getDefaultVolume(BoxParameters boxParameters) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		Box box = generateVoxel(boxParameters, deltas, Color.GRAY);
		Group volume = new Group();
		volume.getChildren().addAll(box);
		return volume;
	}

	public List<Box> getVoxels(){
		return voxels;
	}
	
}
