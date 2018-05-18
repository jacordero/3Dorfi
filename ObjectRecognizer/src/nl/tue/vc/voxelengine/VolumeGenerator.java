package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.model.ProjectedPoint;
import nl.tue.vc.projection.BoundingBox;
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.projection.TransformMatrices;
import nl.tue.vc.projection.Vector3D;
import nl.tue.vc.projection.VolumeModel;

public class VolumeGenerator {

	private CameraCalibrator cameraCalibrator;
	private ProjectionGenerator projectionGenerator;

	private List<String> projectionMatricesForImages;
	private Map<String, Mat> calibrationImages;
	private List<ProjectedPoint> projectedPoints;
	private List<BoundingBox> boundingBoxes;
	private Octree octree;
	private Group octreeVolume;
	private List<int[][]> transformedInvertedArrays;
	private List<int[][]> transformedArrays;
	private List<BufferedImage> bufferedImagesForTest;
	private TransformMatrices transformMatrices;
	private int fieldOfView;
	private int octreeHeight;

	public VolumeGenerator(Octree octree, BoxParameters boxParameters) {
		this.octree = octree;
		this.bufferedImagesForTest = new ArrayList<BufferedImage>();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
		transformedInvertedArrays = new ArrayList<int[][]>();
		transformedArrays = new ArrayList<int[][]>();
		System.out.println(octree);
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
		octreeHeight = -1;
		projectionGenerator = null;
		projectionMatricesForImages = new ArrayList<String>();
	}

	public VolumeGenerator(Octree octree, BoxParameters boxParameters, List<int[][]> transformedInvertedBinArrays,
			List<int[][]> transformedBinaryArrays, int octreeHeight) {
		// this(octree, boxParameters);
		this.transformedArrays = transformedBinaryArrays;

		this.octree = octree;
		this.bufferedImagesForTest = new ArrayList<BufferedImage>();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		this.transformedInvertedArrays = transformedInvertedBinArrays;
		this.transformedArrays = transformedBinaryArrays;
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
		System.out.println(octree);
		projectionGenerator = null;// cameraCalibrator.calibrateMultipleMatrices(calibrationImages, true);
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
		this.octreeHeight = octreeHeight;
		projectionGenerator = null;
		projectionMatricesForImages = new ArrayList<String>();
	}

	public void setProjectionGenerator(ProjectionGenerator projectionGenerator) {
		this.projectionGenerator = projectionGenerator;
	}

	// TODO: make the calibration matrices id values be automatically detected
	private void buildProjectionGenerator() {
		calibrationImages = Utils.loadCalibrationImages();
		cameraCalibrator = new CameraCalibrator();
		// Map<String, Mat> calibrationImages = Utils.loadCalibrationImages();
		projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImages, true);

		int index = 0;
		//Utils.debugNewLine("transformed arrays size: " + transformedArrays.size(), true);
		for (int i = 0; i < transformedArrays.size(); i++) {
			projectionMatricesForImages.add(Utils.PROJECTION_MATRICES_IDS.get(index));
			index++;
			if (index >= calibrationImages.keySet().size()) {
				index = 0;
			}
		}
	}

	public Group generateVolume() {
		if (projectionGenerator == null) {
			buildProjectionGenerator();
		}

		Group volume = new Group();

		Node root = octree.getRoot();
		BoxParameters boxParameters = octree.getBoxParameters();
		DeltaStruct deltas = new DeltaStruct();
		if (octree.getInternalNode().isLeaf()) {
			System.out.println("Octree children: 1");
		} else {
			System.out.println("Octree children: " + root.getChildren().length);
		}

		// List<Box> voxels = generateVolumeAux(root, boxParameters, deltas);
		// volume.getChildren().addAll(voxels);

		Group imageProjection = getImageProjections(0);
		volume.getChildren().addAll(imageProjection);

		long lStartTime = System.nanoTime();

		projectCubesForVisualization();
		if (octreeHeight <= 3) {
			volume.getChildren().addAll(getProjectedVolume());
		}

		long lEndTime = System.nanoTime();
		long output = lEndTime - lStartTime;
		//System.out.println("Elapsed time for projectCubes in milliseconds: " + output / 1000000);

		// start
		lStartTime = System.nanoTime();

		// here we are supposed to use the multiple images
		root = getTestedNodeAux(root);
		octree.setRoot(root);

		// end
		lEndTime = System.nanoTime();

		// time elapsed
		output = lEndTime - lStartTime;
		//System.out.println("Elapsed time for getTestedNodeAux in milliseconds: " + output / 1000000);

		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		int sceneWidth = 3 * appConfig.getVolumeSceneWidth() / 4;
		int sceneHeight = 3 * appConfig.getVolumeSceneHeight() / 4;
		int sceneDepth = appConfig.getVolumeSceneDepth() / 2;
		BoxParameters volumeBoxParameters = new BoxParameters();
		volumeBoxParameters.setBoxSize(100);
		volumeBoxParameters.setCenterX(sceneWidth);
		volumeBoxParameters.setCenterY(sceneHeight);
		volumeBoxParameters.setCenterZ(sceneDepth);

		// start
		lStartTime = System.nanoTime();
		List<Box> voxels = generateVolumeAux(root, volumeBoxParameters, deltas);
		volume.getChildren().addAll(voxels);
		// end
		lEndTime = System.nanoTime();

		// time elapsed
		output = lEndTime - lStartTime;

		//System.out.println("Elapsed time for generateVolumeAux in milliseconds: " + output / 1000000);

		// List<Box> testedVoxels = generateTestedVolume(root, volumeBoxParameters,
		// deltas);
		// volume.getChildren().addAll(testedVoxels);

		return volume;
	}

	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {

		List<Box> voxels = new ArrayList<Box>();
		// System.out.println("========================== generateVolumeAux: " +
		// currentNode + "| " + currentParameters.getBoxSize());
		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
			voxels.add(box);
		} else {
			Node[] children = currentNode.getChildren();
			double newBoxSize = currentParameters.getBoxSize() / 2;

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
					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
				}
			}
		}

		return voxels;
	}

	private Node getTestedNodeAux(Node currentNode) {

		//Utils.debugNewLine("#################### Intersection test for node: " + currentNode, false);

		if (currentNode.isLeaf()) {
			for (int j = 0; j < this.bufferedImagesForTest.size(); j++) {
				//Utils.debugNewLine("########## Testing against image " + (j + 1) + " ##########", false);
				Color boxColor = Color.GRAY;
				IntersectionStatus status = testIntersection(currentNode, j);
				if (status == IntersectionStatus.INSIDE) {
					boxColor = getPaintColor(currentNode.getColor(), Color.BLACK);
				} else if (status == IntersectionStatus.PARTIAL) {
					boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
				} else {
					boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);

				}
				currentNode.setColor(boxColor);
				if(currentNode.getColor()==Color.BLACK) {
					break;
				}
			}
		} else {
			if (currentNode.getColor() == Color.GRAY) {
				Node[] children = currentNode.getChildren();
				for (int i = 0; i < children.length; i++) {
					Node childNode = children[i];
					if (childNode != null) {
						childNode = getTestedNodeAux(childNode);
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
			for (int i = 0; i < this.transformedArrays.size(); i++) {
				IntersectionStatus status = testIntersection(currentNode, i);
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
			double newBoxSize = currentParameters.getBoxSize() / 2;
			BoxParameters newParameters = new BoxParameters();
			newParameters.setBoxSize(newBoxSize);
			newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newBoxSize));
			newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newBoxSize));
			newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newBoxSize));

			for (int i = 0; i < children.length; i++) {
				// compute deltaX, deltaY, and deltaZ for new voxels
				Node childNode = children[i];
				if (childNode != null) {
					childNode.setBoxParameters(newParameters);
					DeltaStruct displacementDirections = computeDeltaDirections(i);
					childNode.setDisplacementDirection(displacementDirections);
					// Box box = new Box();
					Color boxColor = Color.GRAY;
					Color finalColor = Color.WHITE;
					for (int i1 = 0; i1 < this.transformedArrays.size(); i1++) {
						IntersectionStatus status = testIntersection(currentNode, i1);
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

	public Group getImageProjections(int index) {
		// start
		long lStartTime = System.nanoTime();

		Group root2D = new Group();
		Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(index), null);
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

	public Group getProjections(BoxParameters boxParameters) {
		// start
		long lStartTime = System.nanoTime();

		ArrayList<Vector3D> projectedPoints = new ArrayList<>();

		// TransformMatrices transformMatrices = new TransformMatrices(400, 290, 32.3);
		VolumeModel volumeModel = new VolumeModel(boxParameters);

		double leftMostPos = transformMatrices.screenWidth;
		double rightMostPos = 0;
		double topMostPos = transformMatrices.screenHeight;
		double bottomMostPos = 0;

		for (Vector3D vector : volumeModel.modelVertices) {

			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);
			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);

			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW())
					|| Math.abs(clipVector.getY()) > Math.abs(clipVector.getW())
					|| Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
			}

			Vector3D ndcVector = transformMatrices.toNDCCoordinates(clipVector);
			Vector3D windowVector = transformMatrices.toWindowCoordinates(ndcVector);
			projectedPoints.add(windowVector);

			if (windowVector.getX() > rightMostPos) {
				rightMostPos = windowVector.getX();
			} else if (windowVector.getX() < leftMostPos) {
				leftMostPos = windowVector.getX();
			}

			if (windowVector.getY() > bottomMostPos) {
				bottomMostPos = windowVector.getY();
			} else if (windowVector.getY() < topMostPos) {
				topMostPos = windowVector.getY();
			}
		}

		Group root2D = new Group();

		Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(0), null);
		Rectangle imageRect = new Rectangle();
		imageRect.setX(0);// imageBoxParameters.getCenterX() - (img.getWidth() / 2));
		imageRect.setY(0);// imageBoxParameters.getCenterY() - (img.getHeight() / 2));
		imageRect.setWidth(img.getWidth());
		System.out.println("img width: " + img.getWidth() + ", height: " + img.getHeight());
		imageRect.setHeight(img.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.BLACK);
		// root2D.getChildren().add(imageRect);

		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos,
				bottomMostPos - topMostPos);
		boundingBox.setFill(Color.CHARTREUSE);
		boundingBox.setStroke(Color.BLACK);
		System.out.println("(" + boundingBox.getX() + "," + boundingBox.getY() + ") - (" + boundingBox.getX() + ","
				+ (boundingBox.getY() + boundingBox.getHeight()) + ")");
		root2D.getChildren().add(boundingBox);

		List<Color> cornerColors = new ArrayList<Color>();
		cornerColors.add(Color.RED);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);

		int i = 0;
		for (Vector3D point : projectedPoints) {
			Ellipse circle = new Ellipse(point.getX(), point.getY(), 4, 4);
			circle.setFill(cornerColors.get(i));
			root2D.getChildren().add(circle);
			i++;
		}

		// draw the lines
		Line line1 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(7).getX(), projectedPoints.get(7).getY());
		line1.getStrokeDashArray().addAll(2d);
		line1.setFill(Color.BLUE);
		root2D.getChildren().add(line1);

		Line line2 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(5).getX(), projectedPoints.get(5).getY());
		line2.getStrokeDashArray().addAll(2d);
		line2.setFill(Color.BLUE);
		root2D.getChildren().add(line2);

		Line line3 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(0).getX(), projectedPoints.get(0).getY());
		line3.getStrokeDashArray().addAll(2d);
		line3.setFill(Color.BLUE);
		root2D.getChildren().add(line3);

		Line line4 = new Line(projectedPoints.get(7).getX(), projectedPoints.get(7).getY(),
				projectedPoints.get(3).getX(), projectedPoints.get(3).getY());
		line4.getStrokeDashArray().addAll(2d);
		line4.setFill(Color.BLUE);
		root2D.getChildren().add(line4);

		Line line5 = new Line(projectedPoints.get(7).getX(), projectedPoints.get(7).getY(),
				projectedPoints.get(6).getX(), projectedPoints.get(6).getY());
		line5.getStrokeDashArray().addAll(2d);
		line5.setFill(Color.BLUE);
		root2D.getChildren().add(line5);

		Line line6 = new Line(projectedPoints.get(5).getX(), projectedPoints.get(5).getY(),
				projectedPoints.get(6).getX(), projectedPoints.get(6).getY());
		line6.getStrokeDashArray().addAll(2d);
		line6.setFill(Color.BLUE);
		root2D.getChildren().add(line6);

		Line line7 = new Line(projectedPoints.get(5).getX(), projectedPoints.get(5).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line7.getStrokeDashArray().addAll(2d);
		line7.setFill(Color.BLUE);
		root2D.getChildren().add(line7);

		Line line8 = new Line(projectedPoints.get(6).getX(), projectedPoints.get(6).getY(),
				projectedPoints.get(2).getX(), projectedPoints.get(2).getY());
		line8.getStrokeDashArray().addAll(2d);
		line8.setFill(Color.BLUE);
		root2D.getChildren().add(line8);

		Line line9 = new Line(projectedPoints.get(0).getX(), projectedPoints.get(0).getY(),
				projectedPoints.get(3).getX(), projectedPoints.get(3).getY());
		line9.getStrokeDashArray().addAll(2d);
		line9.setFill(Color.BLUE);
		root2D.getChildren().add(line9);

		Line line10 = new Line(projectedPoints.get(0).getX(), projectedPoints.get(0).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line10.getStrokeDashArray().addAll(2d);
		line10.setFill(Color.BLUE);
		root2D.getChildren().add(line10);

		Line line11 = new Line(projectedPoints.get(3).getX(), projectedPoints.get(3).getY(),
				projectedPoints.get(2).getX(), projectedPoints.get(2).getY());
		line11.getStrokeDashArray().addAll(2d);
		line11.setFill(Color.BLUE);
		root2D.getChildren().add(line11);

		Line line12 = new Line(projectedPoints.get(2).getX(), projectedPoints.get(2).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line12.getStrokeDashArray().addAll(2d);
		line12.setFill(Color.BLUE);
		root2D.getChildren().add(line12);

		int sceneWidth = 440;
		int sceneHeight = 320;
		int sceneDepth = 320;
		int scalingParameter = 10;
		Box volume = new Box(volumeModel.xLength * scalingParameter, volumeModel.yLength * scalingParameter,
				volumeModel.zLength * scalingParameter);

		volume.setTranslateX(sceneWidth);
		volume.setTranslateY(sceneHeight);
		// volume.setTranslateZ(volumePositionZ);

		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(Color.BLUE);
		volume.setMaterial(textureMaterial);
		// root2D.getChildren().add(volume);

		// end
		long lEndTime = System.nanoTime();

		// time elapsed
		long output = lEndTime - lStartTime;
		//System.out.println("Elapsed time for getProjections in milliseconds: " + output / 1000000);

		return root2D;
	}

	public IntersectionStatus testIntersection(Node node, int imgIndex) {

		// Utils.debugNewLine(">> testIntersection: imgIndex = " + imgIndex, false);
		BoundingBox boundingBox = getBoundingBox(node, imgIndex, 0);
		SimpleRectangle boundingRectangle = boundingBox.getUnScaledRectangle();
		IntersectionStatus status = IntersectionStatus.INSIDE;
		int[][] transformedArray = transformedArrays.get(imgIndex);
		int[][] transformedInvertedArray = transformedInvertedArrays.get(imgIndex);
		int xVal = (int) boundingRectangle.getX();
		int yVal = (int) (boundingRectangle.getY() + boundingRectangle.getHeight());
		int arrayRows = Utils.IMAGES_HEIGHT;// transformedArray.length;
		int arrayCols = Utils.IMAGES_WIDTH;// transformedArray[0].length;

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

		int transformedValue = transformedArray[yVal][xVal];
		int transformedInvertedValue = transformedInvertedArray[yVal][xVal];

		int determiningValue = (int) boundingRectangle.getWidth();
		if (determiningValue < boundingRectangle.getHeight()) {
			determiningValue = (int) boundingRectangle.getHeight();
		}

//		Utils.debugNewLine("transformedValue: " + transformedValue + ", projected box size: " + determiningValue, true);
//		Utils.debugNewLine(
//				"transformedInvertedValue: " + transformedInvertedValue + ", projected box size: " + determiningValue,
//				true);

		if (determiningValue <= transformedValue) {

//			Utils.debugNewLine(
//					"Projection is totally inside iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
//					true);

			status = IntersectionStatus.INSIDE;
		} else if (determiningValue <= transformedInvertedValue) {

//			Utils.debugNewLine(
//					"Projection is totally outside oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo",
//					true);

			status = IntersectionStatus.OUTSIDE;
		} else if (checkForPartial(determiningValue, transformedInvertedValue, xVal, yVal, arrayCols, arrayRows,
				(int) boundingRectangle.getWidth(), (int) boundingRectangle.getHeight())) {

//			Utils.debugNewLine(
//					"Projection is partially inside ====================================================================================",
//					true);

			status = IntersectionStatus.PARTIAL;
		} else if (checkForOutsideInCorners(determiningValue, transformedValue, transformedInvertedValue, xVal, yVal,
				arrayCols)) {

//			Utils.debugNewLine("Projection out of bounds but totally outside oooooooooooooooooooooooooooooooooo", true);

			status = IntersectionStatus.OUTSIDE;
		} else {
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
		if ((boundingSize > yPos) && (invertedSquareSize > 0) && (boundingSize > invertedSquareSize)) {
			result = true;
		} else if ((boundingSize > (width - xPos)) && (invertedSquareSize > 0) && (boundingSize > invertedSquareSize)) {
			// check for the right boundary
			result = true;
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

	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {

		double sceneWidth = boxParameters.getCenterX();
		double sceneHeight = boxParameters.getCenterY();
		double sceneDepth = boxParameters.getCenterZ();
		double boxSize = boxParameters.getBoxSize();

		Box box = new Box(boxSize, boxSize, boxSize);
		double posx = sceneWidth + (deltas.deltaX * boxSize / 2);
		double posy = sceneHeight + (deltas.deltaY * boxSize / 2);
		double posz = sceneDepth + (deltas.deltaZ * boxSize / 2);

		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();

		Color diffuseColor = nodeColor;
		if (nodeColor == Color.WHITE){
			 diffuseColor = Color.TRANSPARENT;
		}
		// if (nodeColor == Color.WHITE || nodeColor == Color.GRAY){
		// diffuseColor = Color.TRANSPARENT;
		// } else if (nodeColor == Color.BLACK){
		// diffuseColor = Color.BLACK;
		// } else {
		// diffuseColor = Color.GRAY;
		// }
		// Color diffuseColor = nodeColor == Color.BLACK ? nodeColor :
		// Color.TRANSPARENT;
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
			System.out.println(projection);
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

		if (bufferedImagesForTest.size() == 1) {
			iterateCubesForVisualizationAux(root, octree.getOctreeHeight());
		} else {
			for (int i = 0; i < bufferedImagesForTest.size(); i++) {
				BoundingBox boundingBox = getBoundingBox(root, i, i);
				boundingBoxes.add(boundingBox);
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

		/**
		 * if (level == 0) { scaledRectangle.setFill(Color.RED);
		 * scaledRectangle.setStroke(Color.RED); } else if (level >= 1) {
		 * scaledRectangle.setFill(Color.BLUE); scaledRectangle.setStroke(Color.BLACK);
		 * } else { scaledRectangle.setFill(Color.CHARTREUSE);
		 * scaledRectangle.setStroke(Color.BLACK);
		 * 
		 * } scaledRectangle.setFill(Color.TRANSPARENT);
		 **/
		// scaledRectangle.setFill(Color.YELLOW);

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

	public BoundingBox getBoundingBox(Node node, int imageToProcessId, int level) {

		// Utils.debugNewLine("Computing bounding box for image: " + imageToProcessId +
		// ", projection matrix id: " +
		// projectionMatricesForImages.get(imageToProcessId), false);

		MatOfPoint3f encodedCorners = node.getCorners();

		// long lStartTime = System.nanoTime();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners,
				projectionMatricesForImages.get(imageToProcessId));
		// long lEndTime = System.nanoTime();
		// long output = lEndTime - lStartTime;
		// System.out.println("Elapsed time for projectPoints in milliseconds: " +
		// output);

		List<ProjectedPoint> projections = projectionsAsList(encodedProjections);

		// lStartTime = System.nanoTime();
		BoundingBox boundingBox = computeBoundingBox(projections, Utils.IMAGES_WIDTH, Utils.IMAGES_HEIGHT, level);
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

	public List<int[][]> getTransformedInvertedArrays() {
		return transformedInvertedArrays;
	}

	public void setTransformedInvertedArrays(List<int[][]> transformedInvertedBinArrays) {
		this.transformedInvertedArrays = transformedInvertedBinArrays;
	}

	public List<int[][]> getTransformedArrays() {
		return transformedArrays;
	}

	public void setTransformedArrays(List<int[][]> transformedBinaryArray) {
		this.transformedArrays = transformedBinaryArray;
	}

	public List<BufferedImage> getBufferedImagesForTest() {
		return bufferedImagesForTest;
	}

	public void setBufferedImagesForTest(List<BufferedImage> bufferedImagesForTest) {
		this.bufferedImagesForTest = bufferedImagesForTest;
	}

	public int getFieldOfView() {
		return fieldOfView;
	}

	public void setFieldOfView(int fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

	public TransformMatrices getTransformMatrices() {
		return transformMatrices;
	}

	public void setTransformMatrices(TransformMatrices transformMatrices) {
		this.transformMatrices = transformMatrices;
	}

	public Octree getOctree() {
		return octree;
	}

}
