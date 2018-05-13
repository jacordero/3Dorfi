package nl.tue.vc.projectiontests;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.projection.ProjectionGenerator;

public class OctreeProjectionTest {

	
	private CameraCalibrator cameraCalibrator;
	
	private ProjectionGenerator projectionGenerator;
	
	private static final String CALIBRATION_IMAGE = "images/calibrationImage.png";
	
	private Mat calibrationImage;
	
	private OctreeTest octree;
	
	private List<Point> projectedPoints;
	
	private List<Rectangle> boundingBoxes;
	
	public OctreeProjectionTest(){
		calibrationImage = loadCalibrationImage();
		octree = generateOctree();
		System.out.println(octree);
		cameraCalibrator = new CameraCalibrator();
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
		projectedPoints = new ArrayList<Point>();
		boundingBoxes = new ArrayList<Rectangle>();
	}
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		OctreeProjectionTest projectionTest = new OctreeProjectionTest();
		projectionTest.projectCubes();
		
		//projectionTest.generateOctree();
		//System.out.println(projectionTest.octree);
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
		NodeTest root = octree.getRoot();
		iterateCubesAux(root, 0);
	}
	
	public void iterateCubesAux(NodeTest node, int level){
		MatOfPoint3f encodedCorners = node.getCorners();
		List<Point3> corners = encodedCorners.toList();
		MatOfPoint2f encodedProjections = projectionGenerator.projectPoints(encodedCorners);
		List<Point> projections = encodedProjections.toList();
		NumberFormat formatter = new DecimalFormat("#0.00"); 

		Utils.debugNewLine("\n************ Projecting parent ****************", false);
		for (int i = 0; i < corners.size(); i++){
			Point3 corner = corners.get(i);
			Point projection = projections.get(i);
			String infoStr = "BoxSize: " + node.getBoxSize();
			infoStr += "\tCorner: [x: " + formatter.format(corner.x) + ", y: " + formatter.format(corner.y) + ", z: " + formatter.format(corner.z) + "]";
			infoStr += "\tProjection: [x: " + formatter.format(projection.x) + ", y:" + formatter.format(projection.y) + "]";
			Utils.debugNewLine(infoStr, false);
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
			Utils.debugNewLine("\n********** Projecting children *************", false);
			for (NodeTest children: node.getChildren()){
				iterateCubesAux(children, level + 1);				
			}
		}
		//
		
		//for (Point projection: )
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
		
		return new OctreeTest(8, 4, 4, 4, 2);
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
	
	public void projectOctreeIntoImage(Mat testImage){
		
	}
	
}
