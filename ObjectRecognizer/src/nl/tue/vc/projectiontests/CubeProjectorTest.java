package nl.tue.vc.projectiontests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.model.ProjectedPoint;
import nl.tue.vc.projection.ProjectionGenerator;

public class CubeProjectorTest {

	private ProjectionGenerator projectionGenerator;
	private CameraCalibrator cameraCalibrator;
	private MatOfPoint3f pointsToProject;
	
	public CubeProjectorTest(){
		cameraCalibrator = new CameraCalibrator();
		projectionGenerator = new ProjectionGenerator();
		pointsToProject = createPointsToProject();
	}
	
	private MatOfPoint3f createPointsToProject(){
		float dx = -1	;
		float dy = 0;
		float dz = -2;
		float cube_length = 12;
		float clx = 16;
		float cly = 8;
		float clz = 12;
		Point3[] corners = new Point3[8];
		corners[0] = new Point3(dx + 0, dy + 0, dz + 0);
		corners[1] = new Point3(dx + clx, dy + 0 , dz + 0);
		corners[2] = new Point3(dx + clx, dy + 0 , dz + clz);
		corners[3] = new Point3(dx + 0, dy + 0 , dz + clz);
		corners[4] = new Point3(dx + 0, dy + cly , dz + 0);
		corners[5] = new Point3(dx + clx, dy + cly , dz + 0);
		corners[6] = new Point3(dx + clx, dy + cly , dz + clz);
		corners[7] = new Point3(dx + 0, dy + cly , dz + clz);

		return new MatOfPoint3f(corners);
	}
	
	public Mat drawCube(Mat img, List<ProjectedPoint> projectedPoints){
		Scalar red = new Scalar(0, 0, 255);
		Scalar green = new Scalar(0, 255, 0);
		Scalar blue = new Scalar(255, 0, 0);
		Scalar black = new Scalar(0, 0, 0);
		Scalar magenta = new Scalar(139, 0, 139);
		
		List<MatOfPoint> topPoints = new ArrayList<MatOfPoint>();		
		topPoints.add(new MatOfPoint(new Point(projectedPoints.get(4).getX(), projectedPoints.get(4).getY())));
		topPoints.add(new MatOfPoint(new Point(projectedPoints.get(5).getX(), projectedPoints.get(5).getY())));
		topPoints.add(new MatOfPoint(new Point(projectedPoints.get(6).getX(), projectedPoints.get(6).getY())));
		topPoints.add(new MatOfPoint(new Point(projectedPoints.get(7).getX(), projectedPoints.get(7).getY())));

		Point point0 = new Point(projectedPoints.get(0).getX(), projectedPoints.get(0).getY());
		Point point1 = new Point(projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		Point point2 = new Point(projectedPoints.get(2).getX(), projectedPoints.get(2).getY());
		Point point3 = new Point(projectedPoints.get(3).getX(), projectedPoints.get(3).getY());
		Point point4 = new Point(projectedPoints.get(4).getX(), projectedPoints.get(4).getY());
		Point point5 = new Point(projectedPoints.get(5).getX(), projectedPoints.get(5).getY());
		Point point6 = new Point(projectedPoints.get(6).getX(), projectedPoints.get(6).getY());
		Point point7 = new Point(projectedPoints.get(7).getX(), projectedPoints.get(7).getY());
		
		int thickness = 5;
		// Top
		Imgproc.line(img, point4, point5, red, thickness);
		Imgproc.line(img, point5, point6, red, thickness);
		Imgproc.line(img, point6, point7, red, thickness);
		Imgproc.line(img, point7, point4, red, thickness);
		
		// Bottom
		Imgproc.line(img, point0, point1, red, thickness);
		Imgproc.line(img, point1, point2, red, thickness);
		Imgproc.line(img, point2, point3, red, thickness);
		Imgproc.line(img, point3, point0, red, thickness);
		
		// Sides
		Imgproc.line(img, point0, point4, red, thickness);
		Imgproc.line(img, point1, point5, red, thickness);
		Imgproc.line(img, point2, point6, red, thickness);
		Imgproc.line(img, point3, point7, red, thickness);
				
		
		return img;
	}
	
	public void saveImage(Mat image, String outputFilename){
		Utils.saveImage(image, outputFilename);
	}
	
	
	public void computeProjectionMatrices(Map<String, Mat> projectionImages){
		projectionGenerator = cameraCalibrator.calibrateMatrices(projectionImages, true);
	}
	
	public List<ProjectedPoint> projectPoints( String matrixIndex){
		MatOfPoint2f projectedPoints = projectionGenerator.projectPoints(this.pointsToProject, matrixIndex);

		List<ProjectedPoint> projections = new ArrayList<ProjectedPoint>();
		for (Point point : projectedPoints.toList()) {
			// TODO: change this way of assigning the scale factor
			projections.add(new ProjectedPoint(point.x, point.y, 1.0));
		}
	
		return projections;
	}
	

	public void projectIntoImage(Mat image, String matrixIndex, String outputFilename){
		List<ProjectedPoint> projectedPoints = projectPoints(matrixIndex);
		Mat modifiedImage = drawCube(image, projectedPoints);
		saveImage(modifiedImage, outputFilename);
	}
	
	
	
	public static void main(String[] args){
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		List<String> calibrationImageFilenames = new ArrayList<String>();
		String imgPrefix = "images/projectionTest/calibration";
		calibrationImageFilenames.add(imgPrefix + "/calibration0.jpg");
		calibrationImageFilenames.add(imgPrefix + "/calibration90.jpg");
		calibrationImageFilenames.add(imgPrefix + "/calibration180.jpg");
		calibrationImageFilenames.add(imgPrefix + "/calibration270.jpg");
		Map<String, Mat> calibrationImages = new HashMap<String, Mat>();
		String[] calibrationIndices = {"cal0", "cal90", "cal180", "cal270"};
		int index = 0;
		for (String filename: calibrationImageFilenames){
			Mat image = Utils.loadImage(filename);
			if (image != null){
				calibrationImages.put(calibrationIndices[index], image);							
				index++;
			}
		}
		
		
		List<String> objectImageFilenames = new ArrayList<String>();
		objectImageFilenames.add(imgPrefix + "/cube-mod0.jpg");
		objectImageFilenames.add(imgPrefix + "/cube-mod90.jpg");
		objectImageFilenames.add(imgPrefix + "/cube-mod180.jpg");
		objectImageFilenames.add(imgPrefix + "/cube-mod270.jpg");
		List<Mat> objectImages = new ArrayList<Mat>();
		for (String filename: objectImageFilenames){
			Mat image = Utils.loadImage(filename);
			if (image != null){
				objectImages.add(image);							
			}
		}
		
		
		CubeProjectorTest cubeProjector = new CubeProjectorTest();
		cubeProjector.computeProjectionMatrices(calibrationImages);
		
		String outputFilename = imgPrefix + "/cal0-projected.png";
		cubeProjector.projectIntoImage(calibrationImages.get("cal0"), "cal0", outputFilename);

		outputFilename = imgPrefix + "/cal90-projected.png";
		cubeProjector.projectIntoImage(calibrationImages.get("cal90"), "cal90", outputFilename);

		outputFilename = imgPrefix + "/cal180-projected.png";
		cubeProjector.projectIntoImage(calibrationImages.get("cal180"), "cal180", outputFilename);

		outputFilename = imgPrefix + "/cal270-projected.png";
		cubeProjector.projectIntoImage(calibrationImages.get("cal270"), "cal270", outputFilename);
		
		outputFilename = imgPrefix + "/cube0-projected.png";
		cubeProjector.projectIntoImage(objectImages.get(0), "cal0", outputFilename);

		outputFilename = imgPrefix + "/cube90-projected.png";
		cubeProjector.projectIntoImage(objectImages.get(1), "cal90", outputFilename);

		outputFilename = imgPrefix + "/cube180-projected.png";
		cubeProjector.projectIntoImage(objectImages.get(2), "cal180", outputFilename);

		outputFilename = imgPrefix + "/cube270-projected.png";
		cubeProjector.projectIntoImage(objectImages.get(3), "cal270", outputFilename);
		
	
	}
}
