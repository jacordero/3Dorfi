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
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.projection.TransformMatrices;
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
		Node root = octree.generateOctreeFractal();//.getRoot();
		BoxParameters boxParameters = octree.getBoxParameters();
		DeltaStruct deltas = new DeltaStruct();
		System.out.println("Children: " + root.getChildren().length);

		List<Box> voxels = generateVolumeAux(root, boxParameters, deltas);
		volume.getChildren().addAll(voxels);
		
		Group imageProjection = getImageProjections(0);
		volume.getChildren().addAll(imageProjection);
		
		projectCubes();
		volume.getChildren().addAll(getProjectedVolume());
		
		List<Box> testedVoxels = generateTestedVolume(root, boxParameters, deltas);
		//volume.getChildren().addAll(testedVoxels);
		
		return volume;
	}

	private List<Box> generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		List<Box> voxels = new ArrayList<Box>();
		
		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			if (currentNode.getColor() != Color.WHITE) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
				voxels.add(box);
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
					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.addAll(innerBoxes);
				}
			}
		}

		return voxels;
	}
	
	private List<Box> generateTestedVolume(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		List<Box> voxels = new ArrayList<Box>();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			currentNode.setBoxParameters(currentParameters);
			currentNode.setDisplacementDirection(currentDeltas);
			
			Color boxColor = Color.GRAY;
			IntersectionStatus status = testIntersection(currentNode, 0);
			Box box = new Box();
			if(status == IntersectionStatus.INSIDE) {
				boxColor = getPaintColor(currentNode.getColor(), Color.BLACK);
				box = generateVoxel(currentParameters, currentDeltas, boxColor);
			} else if(status == IntersectionStatus.PARTIAL){
				boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
				box = generateVoxel(currentParameters, currentDeltas, boxColor);
			} else {
				boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
				box = generateVoxel(currentParameters, currentDeltas, boxColor);
			}
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
					Color boxColor = Color.GRAY;
					IntersectionStatus status = testIntersection(childNode, 0);
					Box box = new Box();
					if(status == IntersectionStatus.INSIDE) {
						boxColor = getPaintColor(childNode.getColor(), Color.BLACK);
						box = generateVoxel(newParameters, displacementDirections, boxColor);
					} else if(status == IntersectionStatus.PARTIAL){
						boxColor = getPaintColor(childNode.getColor(), Color.GRAY);
						box = generateVoxel(newParameters, displacementDirections, boxColor);
						List<Box> innerBoxes = generateTestedVolume(childNode, newParameters, displacementDirections);
						voxels.addAll(innerBoxes);
					} else {
						boxColor = getPaintColor(childNode.getColor(), Color.WHITE);
						box = generateVoxel(newParameters, displacementDirections, boxColor);
					}
					voxels.add(box);
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
		imageRect.setWidth(img.getWidth());
		System.out.println("img width: " + img.getWidth() + ", height: " + img.getHeight());
		imageRect.setHeight(img.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.BLACK);
		root2D.getChildren().add(imageRect);
		return root2D;
	}
	
	public IntersectionStatus testIntersection(Node node, int index) {
		Rectangle boundingBox = getBoundingBox(node, 0);
		IntersectionStatus status = IntersectionStatus.INSIDE;
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
		
		int determiningValue = (int) boundingBox.getWidth();
		if(boundingBox.getHeight()<boundingBox.getWidth()) {
			determiningValue = (int) boundingBox.getHeight();
		}
		
		System.out.println("transformedValue: " + transformedValue + ", projected box size: " + determiningValue);
		
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
	
	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {
		int sceneWidth = 500; //calibrationImage.cols()/2;
		int sceneHeight = 350; //calibrationImage.rows()/2;
		int sceneDepth = 0;
		int boxSize = 50; //boxParameters.getBoxSize()

		int scalingParameter = 1;
		Box box = new Box(boxSize*scalingParameter, boxSize*scalingParameter, boxSize*scalingParameter);
		scalingParameter = 1;
		int posx = (sceneWidth + (deltas.deltaX * boxSize / 2))*scalingParameter;
		int posy = (sceneHeight + (deltas.deltaY * boxSize / 2))*scalingParameter;
		int posz = (sceneDepth + (deltas.deltaZ * boxSize / 2))*scalingParameter;
		
//		System.out.println("center: [" + boxParameters.getCenterX() + ", " + boxParameters.getCenterY()
//				+ ", " + boxParameters.getCenterZ()+"]");
//
//		System.out.println("deltas: [" + deltas.deltaX + ", " + deltas.deltaY
//		+ ", " + deltas.deltaZ+"]");
		
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
		
		SubScene subScene = new SubScene(root2D, calibrationImage.cols()/2, calibrationImage.rows()/2, true, SceneAntialiasing.BALANCED);		
		
		PerspectiveCamera perspectiveCamera = new PerspectiveCamera(false);
		perspectiveCamera.setTranslateX(140);
		perspectiveCamera.setTranslateY(-100);
		perspectiveCamera.setTranslateZ(-40);

		subScene.setCamera(perspectiveCamera);
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
//			Ellipse circle = new Ellipse(boundingBox.getX(), (boundingBox.getY()+boundingBox.getHeight()), 5, 5);
//			circle.setFill(Color.YELLOW);
//			root2D.getChildren().add(circle);
			
			root2D.getChildren().add(boundingBox);
		}
		return root2D;
	}
	
	public void projectOctreeIntoImage(Mat testImage){
		
	}
	
	public Rectangle getBoundingBox(Node node, int level){
		MatOfPoint3f encodedCorners = node.getCorners();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners);
		List<Point> projections = encodedProjections.toList();
				
		Rectangle boundingBox = computeBoundingBox(projections, calibrationImage.cols(), calibrationImage.rows(), level);
		boundingBox.setStroke(Color.BLACK);
		return boundingBox;
	}
	
	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;
		if(currentColor == Color.GRAY) {
			if(newColor==Color.WHITE || newColor==Color.GRAY)
				result = newColor;
			else
				result = currentColor;
		}
		else if(currentColor == Color.WHITE) {
			if(newColor==Color.WHITE)
				result = newColor;
			else
				result = currentColor;
		}
		else {
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
