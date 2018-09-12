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
import nl.tue.vc.application.utils.OctreeVisualUtils;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.projection.BoundingBox;
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.voxelengine.DeltaStruct;
import nl.tue.vc.voxelengine.SimpleRectangle;

public class VolumeGenerator {

	private CameraCalibrator cameraCalibrator;
	private ProjectionGenerator projectionGenerator;

	private Map<String, Mat> calibrationImages;
	private List<ProjectedPoint> projectedPoints;
	private List<BoundingBox> boundingBoxes;
	private List<Box> voxels;
	private Octree octree;
	private Group octreeVolume;
	private Map<String, int[][]> invertedDistanceArrays;
	private Map<String, int[][]> distanceArrays;
	private Map<String, BufferedImage> imagesForDistanceComputation;
	private int octreeHeight;
	private BoxParameters volumeBoxParameters;

	
	public VolumeGenerator(){
		octree = null;
		imagesForDistanceComputation = new HashMap<String, BufferedImage>();
		distanceArrays = new HashMap<String, int[][]>();
		invertedDistanceArrays = new HashMap<String, int[][]>();
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
		octreeHeight = -1;
		projectionGenerator = null;
		voxels = new ArrayList<Box>();		
	}
	
	public VolumeGenerator(Octree octree, BoxParameters boxParameters) {
		this.octree = octree;
		this.imagesForDistanceComputation = new HashMap<String, BufferedImage>();
		distanceArrays = new HashMap<String, int[][]>();
		invertedDistanceArrays = new HashMap<String, int[][]>();
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
		octreeHeight = -1;
		projectionGenerator = null;
		voxels = new ArrayList<Box>();
	}

	public VolumeGenerator(Octree octree, BoxParameters boxParameters, Map<String, int[][]> distanceArrays,
			Map<String, int[][]> invertedDistanceArrays, int octreeHeight) {
		this.octree = octree;
		volumeBoxParameters = boxParameters;
		this.distanceArrays = distanceArrays;
		this.invertedDistanceArrays = invertedDistanceArrays;		
		this.imagesForDistanceComputation = new HashMap<String, BufferedImage>();
		this.octreeHeight = octreeHeight;

		projectionGenerator = null;// cameraCalibrator.calibrateMultipleMatrices(calibrationImages, true);
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
		voxels = new ArrayList<Box>();
	}

	public void setProjectionGenerator(ProjectionGenerator projectionGenerator) {
		this.projectionGenerator = projectionGenerator;
	}

	// TODO: make the calibration matrices id values be automatically detected

	public void generateTestVoxels(Octree octree){
			Utils.debugNewLine("[VolumeGenerator.generateTestVoxels]", true);
			voxels = new ArrayList<Box>();
			
			Node root = octree.getRoot();
			BoxParameters rootBoxParameters = octree.getBoxParameters();
			//this.octree.setRoot(root);
			
			int scaleFactor = 10;
			BoxParameters scaledBoxParameters = new BoxParameters();
			scaledBoxParameters.setCenterX(0);
			scaledBoxParameters.setCenterY(0);
			scaledBoxParameters.setCenterZ(0);
			scaledBoxParameters.setSizeX(rootBoxParameters.getSizeX()*scaleFactor);
			scaledBoxParameters.setSizeY(rootBoxParameters.getSizeY()*scaleFactor);
			scaledBoxParameters.setSizeZ(rootBoxParameters.getSizeZ()*scaleFactor);
			
			DeltaStruct rootDeltas = new DeltaStruct();
			rootDeltas.deltaX = 0;
			rootDeltas.deltaY = 0;
			rootDeltas.deltaZ = 0;
			rootDeltas.index = 0;
			
			voxels = OctreeVisualUtils.colorForDebug(root, scaledBoxParameters, rootDeltas);
	}
	
	public void generateOctreeVoxels(int octreeDepth){
		Utils.debugNewLine("[VolumeGenerator.generateOctreeVoxels]", true);
		Utils.debugNewLine("ImagesForDistanceComputation: " + imagesForDistanceComputation.size(), true);
		//return generateTestVoxels();
		voxels = new ArrayList<Box>();
		
		
		Node root = octree.getRoot();
		
		Utils.debugNewLine("Octree height: "  + octree.getOctreeHeight(), false);
		if (octree.getInternalNode().isLeaf()) {
			Utils.debugNewLine("Octree children: 1", false);
		} else {
			Utils.debugNewLine("Octree children: " + root.getChildren().length, false);
		}
		root = getTestedNodeAux(root, octreeDepth);
		octree.setRoot(root);
		
		int scaleFactor = 10;
		BoxParameters scaledBoxParameters = new BoxParameters();
		scaledBoxParameters.setCenterX(0);
		scaledBoxParameters.setCenterY(0);
		scaledBoxParameters.setCenterZ(0);
		scaledBoxParameters.setSizeX(volumeBoxParameters.getSizeX()*scaleFactor);
		scaledBoxParameters.setSizeY(volumeBoxParameters.getSizeY()*scaleFactor);
		scaledBoxParameters.setSizeZ(volumeBoxParameters.getSizeZ()*scaleFactor);
		
		int octreeRegion = 2;
		//if (root.isLeaf()){
		DeltaStruct rootDeltas = new DeltaStruct();
		rootDeltas.deltaX = 0;
		rootDeltas.deltaY = 0;
		rootDeltas.deltaZ = 0;
		rootDeltas.index = 0;
		
		voxels = generateVolumeAux(root, scaledBoxParameters, rootDeltas);
		//voxels = OctreeVisualUtils.colorForDebug(root, scaledBoxParameters, rootDeltas);
		
		//} else {
		//	voxels = generateVolumeAux(root.getChildren()[octreeRegion], scaledBoxParameters, deltas);

		//}
	}
	

	
	
	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {

		List<Box> voxels = new ArrayList<Box>();
		// System.out.println("========================== generateVolumeAux: " +
		// currentNode + "| " + currentParameters.getBoxSize());
		if (currentNode == null || currentNode.getColor() == Color.WHITE) {
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

	private Node getTestedNodeAux(Node currentNode, int octreeDepth) {

		//Utils.debugNewLine("#################### Intersection test for node: " + currentNode, false);

		if (currentNode.isLeaf()) {
			// Create a copy of the leaf
			//boolean foundBlack = false;
			int blackCounter = 0;
			int grayCounter = 0;
			int whiteCounter = 0;
			//boolean foundGray = false;
			//boolean foundWhite = false;
			
			for (String imageKey: imagesForDistanceComputation.keySet()){
				Leaf copyNode = new Leaf(currentNode.getColor(), currentNode.getSizeX(), 
						currentNode.getSizeY(), currentNode.getSizeZ(), currentNode.getPositionCenterX(), 
						currentNode.getPositionCenterY(), currentNode.getPositionCenterZ(), currentNode.getDepth());
				
				//Utils.debugNewLine("########## Testing against image " + (j + 1) + " ##########", false);
				//Color boxColor = Color.GRAY;
				IntersectionStatus testResult = testIntersection(copyNode, imageKey);
				Color boxColor = computeColor(copyNode.getColor(), testResult);
				
				/**
				if (status == IntersectionStatus.INSIDE) {
					boxColor = getPaintColor(copyNode.getColor(), Color.BLACK);
				} else if (status == IntersectionStatus.PARTIAL) {
					boxColor = getPaintColor(copyNode.getColor(), Color.GRAY);
				} else {
					boxColor = getPaintColor(copyNode.getColor(), Color.WHITE);
				}**/
				

				if (boxColor == Color.WHITE){
					whiteCounter++;
					break;
				} else if (boxColor == Color.BLACK){
					blackCounter++;
				} else if (boxColor == Color.GRAY){
					grayCounter++;
				}
			}
			
			if (whiteCounter > 0 ){
				currentNode.setColor(Color.WHITE);
			} else if (blackCounter > 0 && grayCounter == 0){
				currentNode.setColor(Color.BLACK);
			} else if (grayCounter > 0){
				currentNode.setColor(Color.GRAY);
			}			
		} else {
			if (currentNode.getColor() == Color.GRAY || (currentNode.getColor() == Color.BLACK && currentNode.getDepth() < octreeDepth)) {
				Node[] children = currentNode.getChildren();
				for (int i = 0; i < children.length; i++) {
					Node childNode = children[i];
					if (childNode != null) {
						childNode = getTestedNodeAux(childNode, octreeDepth);
						currentNode.setChildNode(childNode, i);
					}
				}
			}
		}

		return currentNode;

		// //Utils.debugNewLine("#################### Intersection test for node: " +
		// currentNode, false);
		// //Utils.debugNewLine("########## Testing against image " + (j + 1) + "
		// ##########", false);
		// if (currentNode.isLeaf()) {
		// for (int j = 0; j < this.bufferedImagesForTest.size(); j++) {
		//
		// NodeColor boxColor = Color.GRAY;
		// IntersectionStatus status = testIntersection(currentNode, j);
		// if (status == IntersectionStatus.INSIDE) {
		// boxColor = getPaintColor(currentNode.getColor(), Color.BLACK);
		// } else if (status == IntersectionStatus.PARTIAL) {
		// boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
		// } else {
		// boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
		//
		// }
		// currentNode.setColor(boxColor);
		// // No need to test for the rest of the images because the node
		// // will be black
		//
		//
		// if (currentNode.getColor() == Color.BLACK) {
		// break;
		// }
		//
		// }
		// } else if ((currentNode.getColor() != Color.WHITE) && (currentNode.getColor()
		// != Color.BLACK)){
		// Node[] children = currentNode.getChildren();
		// //boolean paintAsBlack = true;
		// for (int i = 0; i < children.length; i++) {
		// Node childNode = children[i];
		// if (childNode != null && (childNode.getColor() != Color.BLACK &&
		// childNode.getColor() != Color.WHITE)) {
		// childNode = getTestedNodeAux(childNode);
		// currentNode.setChildNode(childNode, i);
		// /**
		// if (Color.BLACK != childNode.getColor()){
		// paintAsBlack = false;
		// }
		// **/
		// }
		// }
		//
		// // all children are black or white
		// /**
		// if (paintAsBlack){
		// currentNode.setColor(Color.BLACK);
		// }
		// **/
		// }
		//
		// return currentNode;
	}

	/**
	 * private Node getTestedNodeAux(Node currentNode) {
	 * 
	 * //Utils.debugNewLine("#################### Intersection test for node: " +
	 * currentNode, false); for (int j = 0; j < this.bufferedImagesForTest.size();
	 * j++) { //Utils.debugNewLine("########## Testing against image " + (j + 1) + "
	 * ##########", false); if (currentNode.isLeaf()) { NodeColor boxColor =
	 * Color.GRAY; IntersectionStatus status = testIntersection(currentNode, j); if
	 * (status == IntersectionStatus.INSIDE) { boxColor =
	 * getPaintColor(currentNode.getColor(), Color.BLACK); } else if (status ==
	 * IntersectionStatus.PARTIAL) { boxColor =
	 * getPaintColor(currentNode.getColor(), Color.GRAY); } else { boxColor =
	 * getPaintColor(currentNode.getColor(), Color.WHITE);
	 * 
	 * } currentNode.setColor(boxColor); } else if ((currentNode.getColor() !=
	 * Color.WHITE) && (currentNode.getColor() != Color.BLACK)){ Node[] children =
	 * currentNode.getChildren(); boolean paintAsBlack = true; for (int i = 0; i <
	 * children.length; i++) { Node childNode = children[i]; if (childNode != null
	 * && (childNode.getColor() != Color.BLACK && childNode.getColor() !=
	 * Color.WHITE)) { childNode = getTestedNodeAux(childNode);
	 * currentNode.setChildNode(childNode, i); if (Color.BLACK !=
	 * childNode.getColor()){ paintAsBlack = false; } } }
	 * 
	 * // all children are black or white if (paintAsBlack){
	 * currentNode.setColor(Color.BLACK); } }
	 * 
	 * // No need to test for the rest of the images because the node will be black
	 * if (currentNode.getColor() == Color.BLACK){ break; } } return currentNode; }
	 **/

	private List<Box> generateTestedVolume(Node currentNode, BoxParameters currentParameters,
			DeltaStruct currentDeltas) {
		List<Box> voxels = new ArrayList<Box>();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			currentNode.setBoxParameters(currentParameters);
			currentNode.setDisplacementDirection(currentDeltas);
			Box box = new Box();
			Color boxColor = Color.GRAY;
			Color finalColor = Color.WHITE;
			for (String key: distanceArrays.keySet()){
			//for (int i = 0; i < this.transformedArrays.size(); i++) {
				
				
				IntersectionStatus status = testIntersection(currentNode, key);
				if (status == IntersectionStatus.INSIDE) {
					boxColor = Color.BLACK;// getPaintColor(currentNode.getColor(), Color.BLACK);
					finalColor = boxColor;
				} else if (status == IntersectionStatus.PARTIAL) {
					boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
					if (finalColor != Color.BLACK) {
						finalColor = boxColor;
					}
				} else {
					boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
					if (finalColor != Color.BLACK) {
						finalColor = boxColor;
					}
				}

			}

			box = generateVoxel(currentParameters, currentDeltas, finalColor);
			voxels.add(box);
			//Utils.debugNewLine("Root is leaf", false);
		} else {
			//Utils.debugNewLine("Root is Node", false);
			Node[] children = currentNode.getChildren();
			//double newBoxSize = currentParameters.getBoxSize() / 2;
			double newSizeX = currentParameters.getSizeX() / 2;
			double newSizeY = currentParameters.getSizeY() / 2;
			double newSizeZ = currentParameters.getSizeZ() / 2;
			BoxParameters newParameters = new BoxParameters();
			//newParameters.setBoxSize(newBoxSize);
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
					childNode.setBoxParameters(newParameters);
					DeltaStruct displacementDirections = OctreeUtils.computeDisplacementDirections(i);
					childNode.setDisplacementDirection(displacementDirections);
					// Box box = new Box();
					Color boxColor = Color.GRAY;
					Color finalColor = Color.WHITE;
					for (String imageKey: imagesForDistanceComputation.keySet()) {
						IntersectionStatus status = testIntersection(currentNode, imageKey);
						if (status == IntersectionStatus.INSIDE) {
							boxColor = Color.BLACK;// getPaintColor(currentNode.getColor(), Color.BLACK);
							finalColor = boxColor;
						} else if (status == IntersectionStatus.PARTIAL) {
							boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
							if (finalColor != Color.BLACK) {
								finalColor = boxColor;
							}
						} else {
							boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
							if (finalColor != Color.BLACK) {
								finalColor = boxColor;
							}
						}

					}

					// box = generateVoxel(newParameters, displacementDirections, finalColor);
					List<Box> innerBoxes = generateTestedVolume(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
					// voxels.add(box);
				}
			}
		}
		return voxels;
	}

	public Group getImageProjections(String index) {
		// start
		long lStartTime = System.nanoTime();

		Group root2D = new Group();
		Image img = SwingFXUtils.toFXImage(this.imagesForDistanceComputation.get(index), null);
		Rectangle imageRect = new Rectangle();
		imageRect.setX(0);
		imageRect.setY(0);
		imageRect.setWidth(Utils.IMAGES_WIDTH / 2);
		imageRect.setHeight(Utils.IMAGES_HEIGHT / 2);
		System.out.println("img width: " + imageRect.getWidth() + ", height: " + imageRect.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.BLACK);
		root2D.getChildren().add(imageRect);

		// end
		long lEndTime = System.nanoTime();

		// time elapsed
		long output = lEndTime - lStartTime;
		//System.out.println("Elapsed time for getImageProjections in milliseconds: " + output / 1000000);

		return root2D;
	}


	public IntersectionStatus testIntersection(Node node, String imgIndex) {

		// Utils.debugNewLine(">> testIntersection: imgIndex = " + imgIndex, false);
		BoundingBox boundingBox = getBoundingBox(node, imgIndex, 0);
		SimpleRectangle boundingRectangle = boundingBox.getUnScaledRectangle();
		IntersectionStatus status = IntersectionStatus.INSIDE;
		int[][] distanceArray = distanceArrays.get(imgIndex);
		int[][] invertedDistanceArray = invertedDistanceArrays.get(imgIndex);
		int xVal = (int) boundingRectangle.getX();
		int yVal = (int) (boundingRectangle.getY() + boundingRectangle.getHeight());
		int arrayRows = distanceArray.length;
		int arrayCols = distanceArray[0].length;
		
		//Utils.debugNewLine("Rows: " + arrayRows + ", columns: " + arrayCols, true);

		// TODO: check this values
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

		// Utils.debugNewLine("xVal = " + xVal + ", yVal = " + yVal, false);

		int transformedValue = distanceArray[yVal][xVal];
		int transformedInvertedValue = invertedDistanceArray[yVal][xVal];

		int determiningValue = (int) boundingRectangle.getWidth();
		if (determiningValue < boundingRectangle.getHeight()) {
			determiningValue = (int) boundingRectangle.getHeight();
		}

//		Utils.debugNewLine("transformedValue: " + transformedValue + ", projected box size: " + determiningValue, true);
//		Utils.debugNewLine(
//				"transformedInvertedValue: " + transformedInvertedValue + ", projected box size: " + determiningValue,
//				true);

		if (determiningValue <= transformedValue && transformedInvertedValue == 0) {
			//Utils.debugNewLine( "++++ INSIDE +++++", true);
//			Utils.debugNewLine(
//					"Projection is totally inside iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
//					true);

			status = IntersectionStatus.INSIDE;
		} else if (determiningValue <= transformedInvertedValue && transformedValue == 0) {
			//Utils.debugNewLine( "++++ OUTSIDE +++++", true);
			
//			Utils.debugNewLine(
//					"Projection is totally outside oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo",
//					true);

			status = IntersectionStatus.OUTSIDE;
		}
		/**else if (checkForPartial(determiningValue, transformedInvertedValue, xVal, yVal, arrayCols, arrayRows,
				(int) boundingRectangle.getWidth(), (int) boundingRectangle.getHeight())) {
			//Utils.debugNewLine( "++++ C-PARTIALLY INSIDE +++++", true);

			status = IntersectionStatus.PARTIAL;
		} else if (checkForOutsideInCorners(determiningValue, transformedValue, transformedInvertedValue, xVal, yVal,
				arrayCols)) {

			//Utils.debugNewLine( "++++ C-OUTSIDE +++++", true);
//			Utils.debugNewLine("Projection out of bounds but totally outside oooooooooooooooooooooooooooooooooo", true);

			status = IntersectionStatus.OUTSIDE;
		} 
		**/else {
			//Utils.debugNewLine( "++++ PARTIALLY INSIDE +++++", true);
//			Utils.debugNewLine(
//					"Projection is partially inside ====================================================================================",
//					true);
			status = IntersectionStatus.PARTIAL;
		}

		return status;
	}

	public boolean checkForOutsideInCorners(int boundingSize, int transformedSquareSize, int invertedSquareSize,
			int xPos, int yPos, int width) {
		boolean result = false;

		// check for the top boundary
		/**
		System.out.println("[boundingSize: " + boundingSize + ", transformedSquareSize: " + transformedSquareSize);
		System.out.println(", invertedSquareSize: " + invertedSquareSize + ", xPos: " + xPos + ", yPos: " + yPos + ", width: " + width);
		**/
		
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

	
	private Box generateVoxelAux(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor){
		Box box = new Box(boxParameters.getSizeX(), boxParameters.getSizeY(), boxParameters.getSizeZ());
		box.setTranslateX(boxParameters.getCenterX());
		box.setTranslateY(boxParameters.getCenterY());
		box.setTranslateZ(boxParameters.getCenterZ());
		PhongMaterial textureMaterial = new PhongMaterial();

		Color diffuseColor = nodeColor;
		 
		 diffuseColor = nodeColor == Color.BLACK ? nodeColor : Color.TRANSPARENT;
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

	public SubScene generateProjectionScene() {
		// start
		long lStartTime = System.nanoTime();

		System.out.println("\nGenerateProjectionScene is called\n");

		Group root2D = new Group();

		for (ProjectedPoint projection : projectedPoints) {
			//System.out.println(projection);
			Ellipse circle = new Ellipse(projection.getScaledX(), projection.getScaledY(), 5, 5);
			circle.setFill(Color.RED);
			root2D.getChildren().add(circle);
		}

		for (BoundingBox boundingBox : boundingBoxes) {
			SimpleRectangle sr = boundingBox.getScaledRectangle();
			Rectangle visualRectangle = new Rectangle(sr.getX(), sr.getY(), sr.getWidth(), sr.getHeight());
			visualRectangle.setStroke(Color.BLACK);
			visualRectangle.setFill(Color.TRANSPARENT);
			root2D.getChildren().add(visualRectangle);
		}

		// Hardcoded corners
		/**
		 * int xMinRange = 545; int xMaxRange = 684; int yMinRange = 432; int yMaxRange
		 * = 609;
		 * 
		 * Ellipse corner1 = new Ellipse(xMinRange/2, yMinRange/2, 5, 5);
		 * corner1.setFill(Color.BLUE); root2D.getChildren().add(corner1);
		 * 
		 * Ellipse corner2 = new Ellipse(xMaxRange/2, yMinRange/2, 5, 5);
		 * corner2.setFill(Color.BLUE); root2D.getChildren().add(corner2);
		 * 
		 * Ellipse corner3 = new Ellipse(xMinRange/2, yMaxRange/2, 5, 5);
		 * corner3.setFill(Color.BLUE); root2D.getChildren().add(corner3);
		 * 
		 * Ellipse corner4 = new Ellipse(xMaxRange/2, yMaxRange/2, 5, 5);
		 * corner4.setFill(Color.BLUE); root2D.getChildren().add(corner4);
		 **/

		SubScene subScene = new SubScene(root2D, Utils.IMAGES_WIDTH / 2, Utils.IMAGES_HEIGHT / 2, true,
				SceneAntialiasing.BALANCED);

		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(false);
		perspectiveCamera.setTranslateX(140);
		perspectiveCamera.setTranslateY(-100);
		perspectiveCamera.setTranslateZ(-40);

		subScene.setCamera(perspectiveCamera);
		subScene.setFill(Color.WHITE);

		// end
		long lEndTime = System.nanoTime();

		// time elapsed
		long output = lEndTime - lStartTime;
		//System.out.println("Elapsed time for generateProjectionScene in milliseconds: " + output / 1000000);

		return subScene;
	}

	public void projectCubesForVisualization() {

		// start
		long lStartTime = System.nanoTime();
		Node root = octree.getRoot();
		boundingBoxes.clear();

		System.out.println("Calibration matrices keyset: " + projectionGenerator.keysStr());
		if (imagesForDistanceComputation.size() == 1) {
			iterateCubesForVisualizationAux(root, octree.getOctreeHeight());
		} else {
			int counter = 0;
			for (String imageKey: imagesForDistanceComputation.keySet()){
				BoundingBox boundingBox = getBoundingBox(root, imageKey, counter);
				boundingBoxes.add(boundingBox);
				counter++;
			}
		}

		// end
		long lEndTime = System.nanoTime();
		// time elapsed
		long output = lEndTime - lStartTime;
		//Utils.debugNewLine("Elapsed time for iterateCubesForVisualizationAux in milliseconds: " + output / 1000000, true);
	}

	public void iterateCubesForVisualizationAux(Node node, int level) {

		MatOfPoint3f encodedCorners = node.getCorners();
		// List<Point3> corners = encodedCorners.toList();

		long lStartTime = System.nanoTime();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners);
		long lEndTime = System.nanoTime();
		long output = lEndTime - lStartTime;
		//System.out.println("Elapsed time for projectPoints in milliseconds: " + output / 1000000);

		List<ProjectedPoint> projections = projectionsAsList(encodedProjections);

		// NumberFormat formatter = new DecimalFormat("#0.00");

		// Utils.debugNewLine("\n************ Projecting parent ****************",
		// false);

		/**
		 * for (int i = 0; i < corners.size(); i++) { Point3 corner = corners.get(i);
		 * ProjectedPoint projection = projections.get(i); String infoStr = "BoxSize: "
		 * + node.getBoxSize(); infoStr += "\tCorner: [x: " + formatter.format(corner.x)
		 * + ", y: " + formatter.format(corner.y) + ", z: " + formatter.format(corner.z)
		 * + "]"; infoStr += "\tProjection: [x: " + formatter.format(projection.getX())
		 * + ", y:" + formatter.format(projection.getY()) + "]";
		 * Utils.debugNewLine(infoStr, false);
		 * 
		 * }
		 **/

		lStartTime = System.nanoTime();
		BoundingBox boundingBox = computeBoundingBox(projections, Utils.IMAGES_WIDTH, Utils.IMAGES_HEIGHT, level);
		lEndTime = System.nanoTime();
		output = lEndTime - lStartTime;
		//System.out.println("Elapsed time for computeBoundingBox in milliseconds: " + output / 1000000);

		boundingBoxes.add(boundingBox);

		// System.out.println(boundingBox);

		// scale to fit the visualization canvas
		/**
		 * for (Point projection : projections) { Point scaledProjection = new
		 * Point(projection.x / 2, projection.y / 2);
		 * projectedPoints.add(scaledProjection); }
		 **/
		projectedPoints.addAll(projections);

		if (!node.isLeaf()) {
			//Utils.debugNewLine("\n********** Projecting children *************", false);
			for (Node children : node.getChildren()) {
				iterateCubesForVisualizationAux(children, level + 1);
			}
		}

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

	public void calibrateCamera() {
		projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImages, true);
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

	public BoundingBox getBoundingBox(Node node, String projectionMatrixId, int level) {

		// Utils.debugNewLine("Computing bounding box for image: " + imageToProcessId +
		// ", projection matrix id: " +
		// projectionMatricesForImages.get(imageToProcessId), false);

		MatOfPoint3f encodedCorners = node.getCorners();

		// long lStartTime = System.nanoTime();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners, projectionMatrixId);
		// long lEndTime = System.nanoTime();
		// long output = lEndTime - lStartTime;
		// System.out.println("Elapsed time for projectPoints in milliseconds: " +
		// output);

		List<ProjectedPoint> projections = projectionsAsList(encodedProjections);

		// lStartTime = System.nanoTime();
		int rows = distanceArrays.get(projectionMatrixId).length;
		int columns = distanceArrays.get(projectionMatrixId)[0].length;
		//Utils.debugNewLine("Rows: " + rows + ", Columns: " + columns, true);
		
		BoundingBox boundingBox = computeBoundingBox(projections, rows, columns, level);
		Utils.debugNewLine(boundingBox.toString(), false);
		// lEndTime = System.nanoTime();
		// output = lEndTime - lStartTime;
		// System.out.println("Elapsed time for computeBoundingBox in milliseconds: " +
		// output);

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

	public Color computeColor(Color oldColor, IntersectionStatus testResult){
		// Color is white by default unless previous color is white or gray
		Color newColor = Color.WHITE;
		if (oldColor.equals(Color.BLACK)){
			if (testResult == IntersectionStatus.INSIDE){
				newColor = Color.BLACK;
			} else if (testResult == IntersectionStatus.PARTIAL){
				newColor = Color.GRAY;
			} else {
				newColor = Color.WHITE;
			}
		} else if (oldColor.equals(Color.GRAY)){
			if (testResult == IntersectionStatus.INSIDE){
				newColor = Color.GRAY;
			} else if (testResult == IntersectionStatus.PARTIAL){
				newColor = Color.GRAY;
			} else {
				newColor = Color.WHITE;
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

	public Map<String, int[][]> getInvertedDistanceArrays() {
		return invertedDistanceArrays;
	}

	public void setInvertedDistanceArrays(Map<String, int[][]> invertedDistanceArrays) {
		this.invertedDistanceArrays = invertedDistanceArrays;
	}

	public Map<String, int[][]> getDistanceArrays() {
		return distanceArrays;
	}

	public void setDistanceArrays(Map<String, int[][]> distanceArrays) {
		this.distanceArrays = distanceArrays;
	}

	public Map<String, BufferedImage> getImagesForDistanceComputation() {
		return imagesForDistanceComputation;
	}

	public void setImagesForDistanceComputation(Map<String, BufferedImage> imagesForDistanceComputation) {
		this.imagesForDistanceComputation = imagesForDistanceComputation;
	}

	public Octree getOctree() {
		return octree;
	}

	public List<Box> getVoxels(){
		return voxels;
	}
	
}
