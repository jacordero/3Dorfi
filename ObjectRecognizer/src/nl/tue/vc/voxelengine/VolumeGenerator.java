package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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
import javafx.scene.shape.Rectangle;
import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.model.ProjectedPoint;
import nl.tue.vc.projection.BoundingBox;
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.projection.TransformMatrices;

public class VolumeGenerator {

	private CameraCalibrator cameraCalibrator;
	private ProjectionGenerator projectionGenerator;
	private static final String CALIBRATION_IMAGE = "images/calibrationImage.png";
	private Mat calibrationImage;
	private List<ProjectedPoint> projectedPoints;
	private List<BoundingBox> boundingBoxes;
	private Octree octree;
	private Group octreeVolume;
	private List<int[][]> transformedInvertedArrays;
	private List<int[][]> transformedArrays;
	private List<BufferedImage> bufferedImagesForTest;
	private TransformMatrices transformMatrices;
	private int fieldOfView;

	public VolumeGenerator(Octree octree, BoxParameters boxParameters) {
		this.octree = octree;
		this.bufferedImagesForTest = new ArrayList<BufferedImage>();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
		transformedInvertedArrays = new ArrayList<int[][]>();
		transformedArrays = new ArrayList<int[][]>();

		calibrationImage = loadCalibrationImage();
		System.out.println(octree);
		cameraCalibrator = new CameraCalibrator();
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
	}

	public VolumeGenerator(Octree octree, BoxParameters boxParameters, List<int[][]> transformedInvertedBinArrays,
			List<int[][]> transformedBinaryArrays) {
		this.octree = octree;
		this.bufferedImagesForTest = new ArrayList<BufferedImage>();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		this.transformedInvertedArrays = transformedInvertedBinArrays;
		this.transformedArrays = transformedBinaryArrays;
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);

		calibrationImage = loadCalibrationImage();
		System.out.println(octree);
		cameraCalibrator = new CameraCalibrator();
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
		projectedPoints = new ArrayList<ProjectedPoint>();
		boundingBoxes = new ArrayList<BoundingBox>();
	}

	public Group generateVolume() {
		Group volume = new Group();
		//Node root = octree.generateOctreeFractal(0);
		Node root = octree.getRoot();
		BoxParameters boxParameters = octree.getBoxParameters();
		DeltaStruct deltas = new DeltaStruct();
		if (octree.getInernalNode().isLeaf()){
			System.out.println("Octree children: 1");
		} else {
			System.out.println("Octree children: " + root.getChildren().length);			
		}

//		List<Box> voxels = generateVolumeAux(root, boxParameters, deltas);
//		volume.getChildren().addAll(voxels);

		 Group imageProjection = getImageProjections(0);
		 volume.getChildren().addAll(imageProjection);
		
		 projectCubes();
		 volume.getChildren().addAll(getProjectedVolume());
		 
		 ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		 int sceneWidth = 3*appConfig.getVolumeSceneWidth()/4;
		 int sceneHeight = 3*appConfig.getVolumeSceneHeight()/4;
		 int sceneDepth = appConfig.getVolumeSceneDepth()/2;
		 BoxParameters volumeBoxParameters = new BoxParameters();		
		 volumeBoxParameters.setBoxSize(100);
		 volumeBoxParameters.setCenterX(sceneWidth);
		 volumeBoxParameters.setCenterY(sceneHeight);
		 volumeBoxParameters.setCenterZ(sceneDepth);
		 
//		 List<Box> voxels = generateVolumeAux(root, volumeBoxParameters, deltas);
//		 volume.getChildren().addAll(voxels);
			
		 List<Box> testedVoxels = generateTestedVolume(root, volumeBoxParameters, deltas);
		 volume.getChildren().addAll(testedVoxels);

		return volume;
	}

	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		List<Box> voxels = new ArrayList<Box>();
		//System.out.println("========================== generateVolumeAux: " + currentNode + "| " + currentParameters.getBoxSize());
		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
			voxels.add(box);
		} else {
			Node[] children = currentNode.getChildren();
			int newBoxSize = currentParameters.getBoxSize() / 2;

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

					// System.out.println("Index: "+ i + ", " + displacementDirections.toString());
					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
				}
			}
		}

		return voxels;
	}
	
	private Node getTestedNode(Node currentNode, BoxParameters currentParameters,DeltaStruct currentDeltas) {
		Node testedNode = currentNode;
		if (currentNode.isLeaf()) {
			currentNode.setBoxParameters(currentParameters);
			currentNode.setDisplacementDirection(currentDeltas);
			Color boxColor = Color.GRAY;
			IntersectionStatus status = testIntersection(currentNode, 0);
			if (status == IntersectionStatus.INSIDE) {
				boxColor = Color.BLACK;//getPaintColor(currentNode.getColor(), Color.BLACK);
			} else if (status == IntersectionStatus.PARTIAL) {
				boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
			} else {
				boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
				
			}
			currentNode.setColor(boxColor);
			testedNode = currentNode;
		} else {
			Node[] children = currentNode.getChildren();
			int newBoxSize = currentParameters.getBoxSize() / 2;
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
					Color boxColor = Color.GRAY;
					IntersectionStatus status = testIntersection(childNode, 0);
					if (status == IntersectionStatus.INSIDE) {
						boxColor = getPaintColor(childNode.getColor(), Color.BLACK);
					} else if (status == IntersectionStatus.PARTIAL) {
						Node innerNode = getTestedNode(childNode, newParameters, displacementDirections);
					} else {
						boxColor = getPaintColor(childNode.getColor(), Color.WHITE);
					}
				}
			}
		}
		return testedNode;
	}

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
			for(int i=0;i<this.transformedArrays.size();i++) {
				IntersectionStatus status = testIntersection(currentNode, i);
				if (status == IntersectionStatus.INSIDE) {
					boxColor = Color.BLACK;//getPaintColor(currentNode.getColor(), Color.BLACK);
					finalColor = boxColor;
				} else if (status == IntersectionStatus.PARTIAL) {
					boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
					if(finalColor!=Color.BLACK) {
						finalColor = boxColor;
					}
				} else {
					boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
					if(finalColor!=Color.BLACK) {
						finalColor = boxColor;
					}
				}
				
			}
			
			box = generateVoxel(currentParameters, currentDeltas, finalColor);
			voxels.add(box);
			System.out.println("Root is leaf");
		} else {
			System.out.println("Root is Node");
			Node[] children = currentNode.getChildren();
			int newBoxSize = currentParameters.getBoxSize() / 2;
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
					//Box box = new Box();
					Color boxColor = Color.GRAY;
					Color finalColor = Color.WHITE;
					for(int i1=0;i1<this.transformedArrays.size();i1++) {
						IntersectionStatus status = testIntersection(currentNode, i1);
						if (status == IntersectionStatus.INSIDE) {
							boxColor = Color.BLACK;//getPaintColor(currentNode.getColor(), Color.BLACK);
							finalColor = boxColor;
						} else if (status == IntersectionStatus.PARTIAL) {
							boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
							if(finalColor!=Color.BLACK) {
								finalColor = boxColor;
							}
						} else {
							boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
							if(finalColor!=Color.BLACK) {
								finalColor = boxColor;
							}
						}
						
					}
					
					//box = generateVoxel(newParameters, displacementDirections, finalColor);
					List<Box> innerBoxes = generateTestedVolume(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
					//voxels.add(box);
				}
			}
		}
		return voxels;
	}

	public Group getImageProjections(int index) {
		Group root2D = new Group();
		Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(index), null);
		Rectangle imageRect = new Rectangle();
		imageRect.setX(0);
		imageRect.setY(0);
		imageRect.setWidth(calibrationImage.cols()/2);
		imageRect.setHeight(calibrationImage.rows()/2);
		System.out.println("img width: " + imageRect.getWidth() + ", height: " + imageRect.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.BLACK);
		root2D.getChildren().add(imageRect);
		return root2D;
	}

	public IntersectionStatus testIntersection(Node node, int index) {
		BoundingBox boundingBox = getBoundingBox(node, 0);
		Rectangle boundingRectangle = boundingBox.getUnScaledRectangle();
		IntersectionStatus status = IntersectionStatus.INSIDE;
		int[][] transformedArray = transformedArrays.get(index);
		int[][] transformedInvertedArray = transformedInvertedArrays.get(index);
		int xVal = (int) boundingRectangle.getX();
		int yVal = (int) (boundingRectangle.getY() + boundingRectangle.getHeight());
		int arrayRows = transformedArray.length;
		int arrayCols = transformedArray[0].length;
		
		if (xVal < 0) {
			xVal = 0;
		}
		if (xVal >= arrayRows) {
			xVal = arrayRows-1;
		}
		
		if (yVal < 0) {
			yVal = 0;
		}
		
		if (yVal >= arrayCols) {
			yVal = arrayCols-1;
		}
		
		System.out.println("xVal = " + xVal + ", yVal = " + yVal);
		
		int transformedValue = transformedArray[xVal][yVal];
		int transformedInvertedValue = transformedInvertedArray[xVal][yVal];

		int determiningValue = (int) boundingRectangle.getWidth();
		if (determiningValue < boundingRectangle.getHeight()) {
			determiningValue = (int) boundingRectangle.getHeight();
		}

		System.out.println("transformedValue: " + transformedValue + ", projected box size: " + determiningValue);
		System.out.println("transformedInvertedValue: " + transformedInvertedValue + ", projected box size: " + determiningValue);

		if (determiningValue <= transformedValue) {
			System.out.println("Projection is totally inside iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
			status = IntersectionStatus.INSIDE;
		} else if (determiningValue <= transformedInvertedValue) {
			System.out.println("Projection is totally outside oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
			status = IntersectionStatus.OUTSIDE;
		} else {
			System.out.println("Projection is partially inside ====================================================================================");
			status = IntersectionStatus.PARTIAL;
		} 
		return status;
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
				
		int sceneWidth = boxParameters.getCenterX();
		int sceneHeight = boxParameters.getCenterY();
		int sceneDepth = boxParameters.getCenterZ();
		int boxSize = boxParameters.getBoxSize();

		Box box = new Box(boxSize, boxSize, boxSize);
		int posx = sceneWidth + (deltas.deltaX * boxSize / 2);
		int posy = sceneHeight + (deltas.deltaY * boxSize / 2);
		int posz = sceneDepth + (deltas.deltaZ * boxSize / 2);
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}

	public SubScene generateProjectionScene() {

		Group root2D = new Group();

		for (ProjectedPoint projection : projectedPoints) {
			System.out.println(projection);
			Ellipse circle = new Ellipse(projection.getScaledX(), projection.getScaledY(), 5, 5);
			circle.setFill(Color.RED);
			root2D.getChildren().add(circle);
		}

		for (BoundingBox boundingBox : boundingBoxes) {
			root2D.getChildren().add(boundingBox.getScaledRectangle());
		}

		SubScene subScene = new SubScene(root2D, calibrationImage.cols() / 2, calibrationImage.rows() / 2, true,
				SceneAntialiasing.BALANCED);

		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(false);
		perspectiveCamera.setTranslateX(140);
		perspectiveCamera.setTranslateY(-100);
		perspectiveCamera.setTranslateZ(-40);

		subScene.setCamera(perspectiveCamera);
		subScene.setFill(Color.WHITE);
		return subScene;
	}

	public void projectCubes() {
		Node root = octree.getRoot();
		iterateCubesAux(root, octree.getLevels());
	}

	public void iterateCubesAux(Node node, int level) {
		MatOfPoint3f encodedCorners = node.getCorners();
		List<Point3> corners = encodedCorners.toList();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners);
		
		List<ProjectedPoint> projections = projectionsAsList(encodedProjections);
		NumberFormat formatter = new DecimalFormat("#0.00");

		System.out.println("\n************ Projecting parent ****************");
		for (int i = 0; i < corners.size(); i++) {
			Point3 corner = corners.get(i);
			ProjectedPoint projection = projections.get(i);
			String infoStr = "BoxSize: " + node.getBoxSize();
			infoStr += "\tCorner: [x: " + formatter.format(corner.x) + ", y: " + formatter.format(corner.y) + ", z: "
					+ formatter.format(corner.z) + "]";
			infoStr += "\tProjection: [x: " + formatter.format(projection.getX()) + ", y:" + formatter.format(projection.getY())
					+ "]";
			System.out.println(infoStr);
		}

		BoundingBox boundingBox = computeBoundingBox(projections, calibrationImage.cols(), calibrationImage.rows(),
				level);

		boundingBoxes.add(boundingBox);

		System.out.println(boundingBox);
		
		
		// scale to fit the visualization canvas
		/**
		for (Point projection : projections) {
			Point scaledProjection = new Point(projection.x / 2, projection.y / 2);
			projectedPoints.add(scaledProjection);
		}
		**/
		projectedPoints.addAll(projections);

		if (!node.isLeaf()) {
			System.out.println("\n********** Projecting children *************");
			for (Node children : node.getChildren()) {
				iterateCubesAux(children, level + 1);
			}
		}
	}

	private BoundingBox computeBoundingBox(List<ProjectedPoint> projections, double screenWidth, double screenHeight, int level) {
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
		
		Rectangle unScaledRectangle = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos,
				bottomMostPos - topMostPos);
		
		leftMostPos = leftMostPos / 2;
		rightMostPos = rightMostPos / 2;
		topMostPos = topMostPos / 2;
		bottomMostPos = bottomMostPos / 2;

		Rectangle scaledRectangle = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos,
				bottomMostPos - topMostPos);
		if (level == 1) {
			scaledRectangle.setFill(Color.YELLOW);
		} else if (level > 1) {
			scaledRectangle.setFill(Color.BLUE);
		} else {
			scaledRectangle.setFill(Color.CHARTREUSE);
		}
		//scaledRectangle.setFill(Color.TRANSPARENT);
		scaledRectangle.setFill(Color.YELLOW);
		//scaledRectangle.setStroke(Color.BLACK);
		
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setScaledRectangle(scaledRectangle);
		boundingBox.setUnScaledRectangle(unScaledRectangle);
		return boundingBox;
	}

	private Mat loadCalibrationImage() {
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(new File(CALIBRATION_IMAGE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Mat calibrationImage = null;
		if (bufferedImage != null) {
			calibrationImage = Utils.bufferedImageToMat(bufferedImage);
		}
		return calibrationImage;
	}

	public void calibrateCamera() {
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
	}

	public List<BoundingBox> getBoundingBoxes() {
		return boundingBoxes;
	}

	public List<ProjectedPoint> getProjections() {
		return projectedPoints;
	}

	public Group getProjectedVolume() {
		Group root2D = new Group();

		for (ProjectedPoint projection : projectedPoints) {
			Ellipse circle = new Ellipse(projection.getScaledX(), projection.getScaledY(), 5, 5);
			circle.setFill(Color.RED);
			root2D.getChildren().add(circle);
		}

		
		System.out.println("Bounding boxes length: " + boundingBoxes.size());
		for (BoundingBox boundingBox : boundingBoxes) {
//			 Ellipse circle = new Ellipse(boundingBox.getScaledRectangle().getX(),
//			 (boundingBox.getScaledRectangle().getY()+boundingBox.getScaledRectangle().getHeight()), 5, 5);
//			 circle.setFill(Color.YELLOW);
//			 root2D.getChildren().add(circle);

			
			System.out.println(boundingBox.getScaledRectangle().getFill());
			System.out.println(boundingBox.getScaledRectangle().getX());
			System.out.println(boundingBox.getScaledRectangle().getY());
			System.out.println(boundingBox.getScaledRectangle().getWidth());
			System.out.println(boundingBox.getScaledRectangle().getHeight());
			
			root2D.getChildren().add(boundingBox.getScaledRectangle());
			
		}
		return root2D;
	}

	public void projectOctreeIntoImage(Mat testImage) {

	}

	public BoundingBox getBoundingBox(Node node, int level) {
		MatOfPoint3f encodedCorners = node.getCorners();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners);
		List<ProjectedPoint> projections = projectionsAsList(encodedProjections);

		BoundingBox boundingBox = computeBoundingBox(projections, calibrationImage.cols(), calibrationImage.rows(),
				level);
		return boundingBox;
	}

	private List<ProjectedPoint> projectionsAsList(MatOfPoint2f encodedProjections){
		List<ProjectedPoint> projections = new ArrayList<ProjectedPoint>();
		for (Point point: encodedProjections.toList()){
			// TODO: change this way of assigning the scale factor
			projections.add(new ProjectedPoint(point.x, point.y, 2.0));
		}
		return projections;
	}
	
	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;		
		
		if(currentColor == Color.WHITE)
			currentColor = Color.TRANSPARENT;
		
		if(newColor == Color.WHITE)
			newColor = Color.TRANSPARENT;
		
		if (currentColor == Color.GRAY) {
			if (newColor == Color.TRANSPARENT || newColor == Color.GRAY)
				result = newColor;
			else
				result = currentColor;
		} else if (currentColor == Color.TRANSPARENT) {
			if (newColor == Color.TRANSPARENT)
				result = newColor;
			else
				result = currentColor;
		} else {
			result = newColor;
		}

		return result;
	}

//	public Color getPaintColor(Color currentColor, Color newColor) {
//		Color result = Color.GRAY;		
//		if (currentColor == Color.GRAY) {
//			if (newColor == Color.WHITE || newColor == Color.GRAY)
//				result = newColor;
//			else
//				result = currentColor;
//		} else if (currentColor == Color.WHITE) {
//			if (newColor == Color.WHITE)
//				result = newColor;
//			else
//				result = currentColor;
//		} else {
//			result = newColor;
//		}
//
//		return result;
//	}

	public Group getVolume() {
		return octreeVolume;
	}

	public Group getDefaultVolume(BoxParameters boxParameters) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		Box box = generateVoxel(boxParameters, deltas, Color.CYAN);

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

	public TransformMatrices getTransformMatrices() {
		return transformMatrices;
	}

	public void setTransformMatrices(TransformMatrices transformMatrices) {
		this.transformMatrices = transformMatrices;
	}

	public int getFieldOfView() {
		return fieldOfView;
	}

	public void setFieldOfView(int fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

}
