package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
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
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.projection.TransformMatrices;
import nl.tue.vc.projection.Vector3D;
import nl.tue.vc.projection.VolumeModel;
import nl.tue.vc.projectiontests.OctreeTest;

import java.lang.Math;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class VolumeGenerator {
	
	private CameraCalibrator cameraCalibrator;
	private ProjectionGenerator projectionGenerator;
	private static final String CALIBRATION_IMAGE = "images/calibrationImage.png";
	private Mat calibrationImage;
	private List<Point> projectedPoints;
	private List<Rectangle> boundingBoxes;
	private Octree octree;
	private Group octreeVolume;
	private List<int[][]> sourceArrays;
	private List<int[][]> transformedArrays;
	private List<BufferedImage> bufferedImagesForTest;
	private TransformMatrices transformMatrices;
	private int fieldOfView;

	public VolumeGenerator(Octree octree, BoxParameters boxParameters) 
	{
		this.octree = octree;
		this.bufferedImagesForTest = new ArrayList<BufferedImage>();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
		sourceArrays = new ArrayList<int[][]>();
		transformedArrays = new ArrayList<int[][]>();
		
		calibrationImage = loadCalibrationImage();
		System.out.println(octree);
		cameraCalibrator = new CameraCalibrator();
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
		projectedPoints = new ArrayList<Point>();
		boundingBoxes = new ArrayList<Rectangle>();
	}
	
	public VolumeGenerator(Octree octree, BoxParameters boxParameters, List<int[][]> sourceBinaryArrays,
			List<int[][]> transformedBinaryArrays) {
		this.octree = octree;
		this.bufferedImagesForTest = new ArrayList<BufferedImage>();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		this.sourceArrays = sourceBinaryArrays;
		this.transformedArrays = transformedBinaryArrays;
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
		
		calibrationImage = loadCalibrationImage();
		System.out.println(octree);
		cameraCalibrator = new CameraCalibrator();
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
		projectedPoints = new ArrayList<Point>();
		boundingBoxes = new ArrayList<Rectangle>();
	}

	public Group generateVolume() {
		Group volume = new Group();
		Node root = octree.generateOctreeFractal(0);
		BoxParameters boxParameters = octree.getBoxParameters();
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		System.out.println("Children: " + root.getChildren().length);

		Group voxels = generateVolumeAux(root, boxParameters, deltas);
		volume.getChildren().addAll(voxels);
		
		projectCubes();
		volume.getChildren().addAll(getProjectedVolume());
		
		return volume;
	}

	private Group generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		//System.out.println("generateVolumeAux | Nodecolor = " + currentNode.getColor().toString());
//		List<Box> voxels = new ArrayList<Box>();
		Group voxels = new Group();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			if (currentNode.getColor() != Color.WHITE) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
				voxels.getChildren().add(box);
			}
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
//					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
//					voxels.addAll(innerBoxes);
					
					Group innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.getChildren().add(innerBoxes);
					
//					Group projections = getProjections(newParameters);
//					voxels.getChildren().addAll(projections);
				}
			}
		}

		return voxels;
	}
	
	public Group getProjections(BoxParameters boxParameters) {
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		VolumeModel volumeModel = new VolumeModel(boxParameters);
		
		double leftMostPos = transformMatrices.screenWidth;
		double rightMostPos = 0;
		double topMostPos = transformMatrices.screenHeight;
		double bottomMostPos = 0;
		
		for (Vector3D vector: volumeModel.modelVertices) {
			//System.out.println("\nVector in world coordinates");
			//System.out.println(vector);
			
			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);
			//System.out.println("\nVector in view coordinates");
			//System.out.println(viewVector);
			
			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);
			//System.out.println("\nVector in clip coordinates");
			//System.out.println(clipVector);
			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getY()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
				//System.out.println("We should ignore: " + clipVector.toString());
			}
			
			Vector3D ndcVector = transformMatrices.toNDCCoordinates(clipVector);
			//System.out.println("\nVector in ndc coordinates");
			//System.out.println(ndcVector);
			
			Vector3D windowVector = transformMatrices.toWindowCoordinates(ndcVector);
			//System.out.println("\nVector in window coordinates");
			//System.out.println(windowVector);
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
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		boundingBox.setFill(Color.CHARTREUSE);
		boundingBox.setStroke(Color.BLACK);
		System.out.println("("+boundingBox.getX()+","+boundingBox.getY()+") - ("+boundingBox.getX()+","+(boundingBox.getY()+boundingBox.getHeight())+")");
		root2D.getChildren().add(boundingBox);
		
		int[][] transformedArray = transformedArrays.get(0);
		int xVal = (int) boundingBox.getX();
		int yVal = (int) (boundingBox.getY()+boundingBox.getHeight());
		if(xVal < 0) {
			xVal = 0;
		}
		if(yVal < 0) {
			yVal = 0;
		}
		System.out.println("xVal = " + xVal + ", yVal = " + yVal);
		int transformedValue = transformedArray[xVal][yVal];
		
		System.out.println("transformedValue: " + transformedValue);
		
		int determiningValue = (int) boundingBox.getWidth();
		if(boundingBox.getHeight()<boundingBox.getWidth()) {
			determiningValue = (int) boundingBox.getHeight();
		}
		
		if (transformedValue >= determiningValue) {
			System.out.println("Projection is totally inside");
		} else if((transformedValue < determiningValue) && (transformedValue > 0)) {
			System.out.println("Projection is partially inside");
		}
		else {
			System.out.println("Projection is outside");
		}
		
		for (Vector3D point: projectedPoints) {
			Ellipse circle = new Ellipse(point.getX(), point.getY(), 2, 2);
			circle.setFill(Color.BLACK);
			root2D.getChildren().add(circle);
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
		Box volume = new Box(volumeModel.xLength * scalingParameter, 
				volumeModel.yLength * scalingParameter,
				volumeModel.zLength * scalingParameter);
		
		volume.setTranslateX(sceneWidth);
		volume.setTranslateY(sceneHeight);
		//volume.setTranslateZ(volumePositionZ);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(Color.BLUE);
		volume.setMaterial(textureMaterial);
		//root2D.getChildren().add(volume);
		Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(0), null);
		Rectangle imageRect = new Rectangle();
		imageRect.setX(0);//imageBoxParameters.getCenterX() - (img.getWidth() / 2));
		imageRect.setY(0);//imageBoxParameters.getCenterY() - (img.getHeight() / 2));
		imageRect.setWidth(img.getWidth());
		System.out.println("img width: " + img.getWidth() + ", height: " + img.getHeight());
		imageRect.setHeight(img.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.BLACK);
		root2D.getChildren().add(imageRect);
		return root2D;
	}
	
	public IntersectionStatus testIntersection(BoxParameters boxParameters) {
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		IntersectionStatus status = IntersectionStatus.INSIDE;
		//TransformMatrices transformMatrices = new TransformMatrices(400, 290, 32.3);
		VolumeModel volumeModel = new VolumeModel(boxParameters);
		
		double leftMostPos = transformMatrices.screenWidth;
		double rightMostPos = 0;
		double topMostPos = transformMatrices.screenHeight;
		double bottomMostPos = 0;
		
		for (Vector3D vector: volumeModel.modelVertices) {
			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);			
			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);
			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getY()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
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
		
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		System.out.println("("+boundingBox.getX()+","+boundingBox.getY()+") - ("+boundingBox.getX()+","+(boundingBox.getY()+boundingBox.getHeight())+")");
		
		int[][] transformedArray = transformedArrays.get(0);
		int xVal = (int) boundingBox.getX();
		int yVal = (int) (boundingBox.getY()+boundingBox.getHeight());
		if(xVal < 0) {
			xVal = 0;
		}
		if(yVal < 0) {
			yVal = 0;
		}
		System.out.println("xVal = " + xVal + ", yVal = " + yVal);
		int transformedValue = transformedArray[xVal][yVal];
		
		System.out.println("transformedValue: " + transformedValue);
		
		int determiningValue = (int) boundingBox.getWidth();
		if(boundingBox.getHeight()<boundingBox.getWidth()) {
			determiningValue = (int) boundingBox.getHeight();
		}
		
		if (transformedValue >= determiningValue) {
			System.out.println("Projection is totally inside");
			status = IntersectionStatus.INSIDE;
		} else if((transformedValue < determiningValue) && (transformedValue > 0)) {
			System.out.println("Projection is partially inside");
			status = IntersectionStatus.PARTIAL;
		}
		else {
			System.out.println("Projection is outside");
			status = IntersectionStatus.OUTSIDE;
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
	// Make the X, Y, and Z coordinates start at the corner of the first (0) node
	// and translate the rest of the nodes to their respective positions
	// Get rid of the center stuff
	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {
		//get the scene dimensions
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		int sceneWidth = 400;//appConfig.getVolumeSceneWidth();
		int sceneHeight = 290;//appConfig.getVolumeSceneHeight();
		int sceneDepth = 100;//appConfig.getVolumeSceneDepth();
//		int volumeBoxSize = appConfig.getVolumeBoxSize();
		int scalingParameter = 1;
		Box box = new Box(boxParameters.getBoxSize()*scalingParameter, boxParameters.getBoxSize()*scalingParameter, boxParameters.getBoxSize()*scalingParameter);
		scalingParameter = 1;
		int posx = (sceneWidth + (deltas.deltaX * boxParameters.getBoxSize() / 2))*scalingParameter;
		int posy = (sceneHeight + (deltas.deltaY * boxParameters.getBoxSize() / 2))*scalingParameter;
		int posz = (sceneDepth + (deltas.deltaZ * boxParameters.getBoxSize() / 2))*scalingParameter;
		
		System.out.println("center: [" + boxParameters.getCenterX() + ", " + boxParameters.getCenterY()
				+ ", " + boxParameters.getCenterZ()+"]");

		System.out.println("deltas: [" + deltas.deltaX + ", " + deltas.deltaY
		+ ", " + deltas.deltaZ+"]");
		
		/**
		 * compute the coordinates of all 8 corners of the cube according to the following numbering 
		 *       
		 *     5 ------------------ 6   
		 *    /|                   / |
		 *  1 -|------------------ 2 |
		 *   | |                  |  |
		 *   | |                  |  |
		 *   | 7 -----------------|- 8
		 *   |/                   | / 
		 *  3 ------------------- 4 
		 *    
		 */
	
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		
		//TransformMatrices transformMatrices = new TransformMatrices(400, 290, 32.3);
		VolumeModel volumeModel = new VolumeModel(boxParameters);
		
		double leftMostPos = transformMatrices.screenWidth;
		double rightMostPos = 0;
		double topMostPos = transformMatrices.screenHeight;
		double bottomMostPos = 0;
		
		for (Vector3D vector: volumeModel.modelVertices) {
			//System.out.println("\nVector in world coordinates");
			//System.out.println(vector);
			
			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);
			//System.out.println("\nVector in view coordinates");
			//System.out.println(viewVector);
			
			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);
			//System.out.println("\nVector in clip coordinates");
			//System.out.println(clipVector);
			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getY()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
				//System.out.println("We should ignore: " + clipVector.toString());
			}
			
			Vector3D ndcVector = transformMatrices.toNDCCoordinates(clipVector);
			//System.out.println("\nVector in ndc coordinates");
			//System.out.println(ndcVector);
			
			Vector3D windowVector = transformMatrices.toWindowCoordinates(ndcVector);
			//System.out.println("\nVector in window coordinates");
			//System.out.println(windowVector);
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
		
		

		//root2D = new Group();
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		boundingBox.setFill(Color.CHARTREUSE);
		boundingBox.setStroke(Color.BLACK);
		//root2D.getChildren().add(boundingBox);
		//System.out.println("Bounding box width = " + boundingBox.getWidth() + ", h = " + boundingBox.getHeight());
		
//		for (Vector3D point: projectedPoints) {
//			Ellipse circle = new Ellipse(point.getX(), point.getY(), 2, 2);
//			circle.setFill(Color.BLACK);
//			root2D.getChildren().add(circle);
//		}
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}
	
public SubScene generateProjectionScene(){
		
		Group root2D = new Group();
		
		for (Point projection: projectedPoints){
			Ellipse circle = new Ellipse(projection.x, projection.y, 5, 5);
			circle.setFill(Color.RED);
			root2D.getChildren().add(circle);
		}
		
		for (Rectangle boundingBox: boundingBoxes){
			root2D.getChildren().add(boundingBox);
		}
		
		
		/**
		// draw the lines
		Line line1 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(7).getX(), projectedPoints.get(7).getY());
		line1.getStrokeDashArray().addAll(2d);
		line1.setFill(Color.BLUE);
		root2D.getChildren().add(line1);
		 **/
		/**
		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root3D);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAutoReverse(false);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();
		**/
		
		SubScene subScene = new SubScene(root2D, calibrationImage.cols()/2, calibrationImage.rows()/2, true, SceneAntialiasing.BALANCED);		
		
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(false);
		perspectiveCamera.setTranslateX(140);
		perspectiveCamera.setTranslateY(-100);
		perspectiveCamera.setTranslateZ(-40);

		subScene.setCamera(perspectiveCamera);
		//subScene.setFill(Color.CADETBLUE);
		subScene.setFill(Color.WHITE);
		return subScene;
	}
	
	public void projectCubes(){
		Node root = octree.getRoot();
		iterateCubesAux(root, 2);
	}
	
	public void iterateCubesAux(Node node, int level){
		MatOfPoint3f encodedCorners = node.getCorners();
		List<Point3> corners = encodedCorners.toList();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners);
		List<Point> projections = encodedProjections.toList();
		NumberFormat formatter = new DecimalFormat("#0.00"); 

		System.out.println("\n************ Projecting parent ****************");
		for (int i = 0; i < corners.size(); i++){
			Point3 corner = corners.get(i);
			Point projection = projections.get(i);
			String infoStr = "BoxSize: " + node.getBoxSize();
			infoStr += "\tCorner: [x: " + formatter.format(corner.x) + ", y: " + formatter.format(corner.y) + ", z: " + formatter.format(corner.z) + "]";
			infoStr += "\tProjection: [x: " + formatter.format(projection.x) + ", y:" + formatter.format(projection.y) + "]";
			System.out.println(infoStr);
		}
		
		Rectangle boundingBox = computeBoundingBox(projections, calibrationImage.cols(), calibrationImage.rows(), level);
		boundingBox.setStroke(Color.BLACK);

		boundingBoxes.add(boundingBox);
		
		// scale to fit the visualization canvas
		for (Point projection: projections){
			Point scaledProjection = new Point(projection.x/ 2, projection.y/2);
			projectedPoints.add(scaledProjection);
		}
		//projectedPoints.addAll(projections);
		
		if (!node.isLeaf()){
			System.out.println("\n********** Projecting children *************");
			for (Node children: node.getChildren()){
				iterateCubesAux(children, level + 1);				
			}
		}
	}
	
	private Rectangle computeBoundingBox(List<Point> projections, double screenWidth, double screenHeight, int level){
		double leftMostPos = screenWidth;
		double rightMostPos = 0;
		double topMostPos = screenHeight;
		double bottomMostPos = 0;
		
		boolean defaultValues = true;
		
		for (Point projection: projections) {
			if (defaultValues) {
				leftMostPos = projection.x;
				topMostPos = projection.y;
				rightMostPos = projection.x;
				bottomMostPos = projection.y;
				defaultValues = false;
			} else {
				if (projection.x > rightMostPos) {
					rightMostPos = projection.x;
				} else if (projection.x < leftMostPos) {
					leftMostPos = projection.x;
				}
				
				if (projection.y > bottomMostPos) {
					bottomMostPos = projection.y;
				} else if (projection.y < topMostPos) {
					topMostPos = projection.y;
				}				
			}			
		}
		
		leftMostPos = leftMostPos / 2;
		rightMostPos = rightMostPos / 2;
		topMostPos = topMostPos / 2;
		bottomMostPos = bottomMostPos / 2;
		
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		if (level == 1){
			boundingBox.setFill(Color.YELLOW);
		} else if (level > 1){
			boundingBox.setFill(Color.BLUE);
		} else {
			boundingBox.setFill(Color.CHARTREUSE);			
		}
		boundingBox.setStroke(Color.BLACK);
		return boundingBox;
	}
	
	
	private Mat loadCalibrationImage(){
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(new File(CALIBRATION_IMAGE)); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Mat calibrationImage = null;
		if (bufferedImage != null){
			calibrationImage = Utils.bufferedImageToMat(bufferedImage);
		}
		return calibrationImage;
	}
	
	private OctreeTest generateOctree(){
		
		return new OctreeTest(8, 4, 4, 8, 2);
	}
	
	public void calibrateCamera(){
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
	}
	
	public List<Rectangle> getBoundingBoxes(){
		return boundingBoxes;
	}
	
	public List<Point> getProjections(){
		return projectedPoints;
	}
	
	public Group getProjectedVolume()
	{
		Group root2D = new Group();
		
		for (Point projection: projectedPoints){
			Ellipse circle = new Ellipse(projection.x, projection.y, 5, 5);
			circle.setFill(Color.RED);
			root2D.getChildren().add(circle);
		}
		
		for (Rectangle boundingBox: boundingBoxes){
			root2D.getChildren().add(boundingBox);
		}
		return root2D;
	}
	
	public void projectOctreeIntoImage(Mat testImage){
		
	}
	
	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;
		if(currentColor == Color.BLACK) {
			result = newColor;
		}
		else if(currentColor == Color.GRAY) {
			if(newColor==Color.WHITE)
				result = Color.WHITE;
			else
				result = currentColor;
		}
		else {
			result = Color.WHITE;
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
		Box box = generateVoxel(boxParameters, deltas, Color.CYAN);
		
		Group volume = new Group();
		volume.getChildren().addAll(box);
		return volume;
	}

	public List<int[][]> getSourceArrays() {
		return sourceArrays;
	}

	public void setSourceArrays(List<int[][]> sourceBinaryArrays) {
		this.sourceArrays = sourceBinaryArrays;
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
