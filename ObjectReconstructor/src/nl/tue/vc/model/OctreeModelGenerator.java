package nl.tue.vc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;

import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.projection.BoundingBox;
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.voxelengine.SimpleRectangle;

public class OctreeModelGenerator {
	
	private Octree octree;
	private Map<String, int[][]> distanceArrays;
	private Map<String, int[][]> invertedDistanceArrays;
	private ProjectionGenerator projectionGenerator;
	
	public OctreeModelGenerator(Octree octree, Map<String, int[][]> distanceArrays,
			Map<String, int[][]> invertedDistanceArrays, ProjectionGenerator projectionGenerator){
		this.octree = octree;
		this.distanceArrays = distanceArrays;
		this.invertedDistanceArrays = invertedDistanceArrays;
		this.projectionGenerator = projectionGenerator;
	}
	
	
	public void refineOctreeModel(int octreeDepth){
				
		Node root = octree.getRoot();
		
		Utils.debugNewLine("Octree height: "  + octree.getOctreeHeight(), false);
		if (root.isLeaf()) {
			Utils.debugNewLine("Octree children: 1", false);
		} else {
			Utils.debugNewLine("Octree children: " + root.getChildren().length, false);
		}
		root = refineOctreeModelAux(root, octreeDepth);
		octree.setRoot(root);
	}
	
	private Node refineOctreeModelAux(Node currentNode, int octreeDepth){

		if (currentNode.isLeaf()) {
			int blackCounter = 0;
			int grayCounter = 0;
			int whiteCounter = 0;
			
			for (String imageKey: distanceArrays.keySet()){
				Leaf copyNode = new Leaf(currentNode.getColor(), currentNode.getSizeX(), 
						currentNode.getSizeY(), currentNode.getSizeZ(), currentNode.getPositionCenterX(), 
						currentNode.getPositionCenterY(), currentNode.getPositionCenterZ(), currentNode.getDepth());
				
				IntersectionStatus testResult = computeIntersectionStatus(copyNode, imageKey);
				NodeColor boxColor = computeColor(copyNode.getColor(), testResult);
								
				if (boxColor == NodeColor.WHITE){
					whiteCounter++;
					break;
				} else if (boxColor == NodeColor.BLACK){
					blackCounter++;
				} else if (boxColor == NodeColor.GRAY){
					grayCounter++;
				}
			}
			
			if (whiteCounter > 0 ){
				currentNode.setColor(NodeColor.WHITE);
			} else if (blackCounter > 0 && grayCounter == 0){
				currentNode.setColor(NodeColor.BLACK);
			} else if (grayCounter > 0){
				currentNode.setColor(NodeColor.GRAY);
			}			
		} else {
			if (currentNode.getColor() == NodeColor.GRAY || (currentNode.getColor() == NodeColor.BLACK && currentNode.getDepth() < octreeDepth)) {
				Node[] children = currentNode.getChildren();
				for (int i = 0; i < children.length; i++) {
					Node childNode = children[i];
					if (childNode != null) {
						childNode = refineOctreeModelAux(childNode, octreeDepth);
						currentNode.setChildNode(childNode, i);
					}
				}
			}
		}
		return currentNode;

	}
	
	public IntersectionStatus computeIntersectionStatus(Node node, String imgIndex) {

		BoundingBox boundingBox = getBoundingBox(node, imgIndex, 0);
		SimpleRectangle boundingRectangle = boundingBox.getUnScaledRectangle();
		IntersectionStatus status = IntersectionStatus.INSIDE;
		int[][] distanceArray = distanceArrays.get(imgIndex);
		int[][] invertedDistanceArray = invertedDistanceArrays.get(imgIndex);
		int xVal = (int) boundingRectangle.getX();
		int yVal = (int) (boundingRectangle.getY() + boundingRectangle.getHeight());
		int arrayRows = distanceArray.length;
		int arrayCols = distanceArray[0].length;
		
		// Check for values outside of the limits
		if (xVal < 0) {
			xVal = 0;
		}
		if (xVal >= arrayCols) {
			xVal = arrayCols - 1;
		}

		if (yVal < 0) {
			yVal = 0;
		}

		if (yVal >= arrayRows) {
			yVal = arrayRows - 1;
		}

		int transformedValue = distanceArray[yVal][xVal];
		int transformedInvertedValue = invertedDistanceArray[yVal][xVal];

		int determiningValue = (int) boundingRectangle.getWidth();
		if (determiningValue < boundingRectangle.getHeight()) {
			determiningValue = (int) boundingRectangle.getHeight();
		}

		if (determiningValue <= transformedValue && transformedInvertedValue == 0) {
			status = IntersectionStatus.INSIDE;
		} else if (determiningValue <= transformedInvertedValue && transformedValue == 0) {
			status = IntersectionStatus.OUTSIDE;
		} else {
			status = IntersectionStatus.PARTIAL;
		}

		return status;
	}

	

	public boolean checkForOutsideInCorners(int boundingSize, int transformedSquareSize, int invertedSquareSize,
			int xPos, int yPos, int width) {
		boolean result = false;

		// check for the top boundary
		// If the bounding size is too big we may be getting errors by using octrees with too few subdivisions
		if (boundingSize < 100){
			if ((boundingSize > yPos) && (invertedSquareSize > 0) && (boundingSize > invertedSquareSize)) {
				result = true;
			} else if ((boundingSize > (width - xPos)) && (invertedSquareSize > 0) && (boundingSize > invertedSquareSize)) {
				// check for the right boundary
				result = true;
			}			
		}
		return result;
	}

	public boolean checkForPartial(int boundingSize, int invertedSquareSize, int xPos, int yPos, int width, int height,
			int xLen, int yLen) {
		boolean result = false;

		if (boundingSize > invertedSquareSize && invertedSquareSize > 0) {
			result = true;
		}

		if (xPos < 0 || yPos < 0) {
			result = false;
		} else if ((xPos + xLen) > width || (yPos + yLen) > height) {
			result = false;
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

	
	public BoundingBox getBoundingBox(Node node, String projectionMatrixId, int level) {

		MatOfPoint3f encodedCorners = node.getCorners();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners, projectionMatrixId);
		List<ProjectedPoint> projections = projectionsAsList(encodedProjections);

		int rows = distanceArrays.get(projectionMatrixId).length;
		int columns = distanceArrays.get(projectionMatrixId)[0].length;
		BoundingBox boundingBox = computeBoundingBox(projections, rows, columns, level);
		Utils.debugNewLine(boundingBox.toString(), false);

		return boundingBox;
	}

	
	private BoundingBox computeBoundingBox(List<ProjectedPoint> projections, double screenWidth, double screenHeight,
			int level) {
		double leftMostPos = screenWidth;
		double rightMostPos = 0;
		double topMostPos = screenHeight;
		double bottomMostPos = 0;

		boolean defaultValues = true;

		for (ProjectedPoint projection : projections) {
			if (defaultValues) {
				leftMostPos = projection.getX();
				topMostPos = projection.getY();
				rightMostPos = projection.getX();
				bottomMostPos = projection.getY();
				defaultValues = false;
			} else {
				if (projection.getX() > rightMostPos) {
					rightMostPos = projection.getX();
				} else if (projection.getX() < leftMostPos) {
					leftMostPos = projection.getX();
				}

				if (projection.getY() > bottomMostPos) {
					bottomMostPos = projection.getY();
				} else if (projection.getY() < topMostPos) {
					topMostPos = projection.getY();
				}
			}
		}

		SimpleRectangle unScaledRectangle = new SimpleRectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos,
				bottomMostPos - topMostPos);

		leftMostPos = leftMostPos / 2;
		rightMostPos = rightMostPos / 2;
		topMostPos = topMostPos / 2;
		bottomMostPos = bottomMostPos / 2;

		SimpleRectangle scaledRectangle = new SimpleRectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos,
				bottomMostPos - topMostPos);

		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setScaledRectangle(scaledRectangle);
		boundingBox.setUnScaledRectangle(unScaledRectangle);
		return boundingBox;
	}

	private List<ProjectedPoint> projectionsAsList(MatOfPoint2f encodedProjections) {
		List<ProjectedPoint> projections = new ArrayList<ProjectedPoint>();
		for (Point point : encodedProjections.toList()) {
			// TODO: change this way of assigning the scale factor
			projections.add(new ProjectedPoint(point.x, point.y, 2.0));
		}
		return projections;
	}

	public Octree getOctree(){
		return octree;
	}
}