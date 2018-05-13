package nl.tue.vc.imgproc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.projection.ProjectionMatrices;

public class CameraCalibrator {

	private MatOfPoint3f chessboardCornerPoints;

	private Mat intrinsicParameters; 

	private MatOfDouble distorsionCoefficients;
	
	private Size chessboardPatternSize;

	
	// Object points
	
	public CameraCalibrator() {
			configure();
	}
	
	private void configure() {
		Point3[] arrayOfCornerPoints = new Point3[] {
				new Point3(0, 0, 0), new Point3(1, 0, 0), new Point3(2, 0, 0), new Point3(3, 0, 0),
				new Point3(4, 0, 0), new Point3(5, 0, 0), new Point3(6, 0, 0), new Point3(7, 0, 0),
				new Point3(8, 0, 0), new Point3(0, 0, 1), new Point3(1, 0, 1), new Point3(2, 0, 1),
				new Point3(3, 0, 1), new Point3(4, 0, 1), new Point3(5, 0, 1), new Point3(6, 0, 1),
				new Point3(7, 0, 1), new Point3(8, 0, 1), new Point3(0, 0, 2), new Point3(1, 0, 2),
				new Point3(2, 0, 2), new Point3(3, 0, 2), new Point3(4, 0, 2), new Point3(5, 0, 2),
				new Point3(6, 0, 2), new Point3(7, 0, 2), new Point3(8, 0, 2), new Point3(0, 0, 3),
				new Point3(1, 0, 3), new Point3(2, 0, 3), new Point3(3, 0, 3), new Point3(4, 0, 3),
				new Point3(5, 0, 3), new Point3(6, 0, 3), new Point3(7, 0, 3), new Point3(8, 0, 3),
				new Point3(0, 0, 4), new Point3(1, 0, 4), new Point3(2, 0, 4), new Point3(3, 0, 4),
				new Point3(4, 0, 4), new Point3(5, 0, 4), new Point3(6, 0, 4), new Point3(7, 0, 4),
				new Point3(8, 0, 4), new Point3(0, 0, 5), new Point3(1, 0, 5), new Point3(2, 0, 5),
				new Point3(3, 0, 5), new Point3(4, 0, 5), new Point3(5, 0, 5), new Point3(6, 0, 5),
				new Point3(7, 0, 5), new Point3(8, 0, 5)				
		};
		chessboardCornerPoints = new MatOfPoint3f(arrayOfCornerPoints);
		
		intrinsicParameters = new Mat(new Size(3, 3), CvType.CV_32FC1);
		intrinsicParameters.put(0, 0, new float[] {(float) 1422.6417});
		intrinsicParameters.put(0, 1, new float[] {0});
		intrinsicParameters.put(0, 2, new float[] {(float) 640.4154});
		intrinsicParameters.put(1, 0, new float[] {0});
		intrinsicParameters.put(1, 1, new float[] {(float) 1418.4124});
		intrinsicParameters.put(1, 2, new float[] {(float) 446.3054});
		intrinsicParameters.put(2, 0, new float[] {(float) 0});
		intrinsicParameters.put(2, 1, new float[] {(float) 0});
		intrinsicParameters.put(2, 2, new float[] {(float) 1});		

		distorsionCoefficients = new MatOfDouble(new double[] {-0.0379, 0.2845, 0.0006, -0.0024});
		chessboardPatternSize = new Size(9, 6);
	}
	
	public ProjectionGenerator calibrateMatrices(Map<String, Mat> calibrationImages, boolean debugProjectionParameters){
		ProjectionGenerator projector = new ProjectionGenerator();
		
		for (String calibrationIndex: calibrationImages.keySet()){
			Utils.debugNewLine("Calibration for " + calibrationIndex, true);
			Mat calibrationImage = calibrationImages.get(calibrationIndex);
			ProjectionMatrices projectionMatrices = calibrateForOneImage(calibrationImage, true);
			projector.addProjectionMatrices(calibrationIndex, projectionMatrices);
		}
		
		return projector;
	}
	
	
	private ProjectionMatrices calibrateForOneImage(Mat calibrationImage, boolean debugProjectionParameters) {

		Mat grayImage = new Mat();			
		if (calibrationImage.channels() == 3) {
			Imgproc.cvtColor(calibrationImage, grayImage, Imgproc.COLOR_BGR2GRAY);
		} else {
			calibrationImage.copyTo(grayImage);
		}		
		
		Utils.debugNewLine("Size of the image:[ width = " + calibrationImage.cols() + ", height = " + calibrationImage.rows() + "]", false);		
		
		MatOfPoint2f corners = new MatOfPoint2f();
		boolean cornersFound = Calib3d.findChessboardCorners(calibrationImage, chessboardPatternSize, corners);
		
		if (cornersFound) {
			//System.out.println("**** Corners were found ****");
			List<Point> cornerPoints = corners.toList();
			for (Point corner: cornerPoints){
				Utils.debugNewLine(corner.toString(), false);
			}
			
			Size cornersWindowSize = new Size(7, 7);
			Size zeroZone = new Size(-1, -1);
			TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 30, 0.001);	
			Imgproc.cornerSubPix(grayImage, corners, cornersWindowSize, zeroZone, criteria);
			
			// check for the refined corners
			/**
			System.out.println("Refined corners");
			for (Point corner: corners.toList()) {
				System.out.println(corner);
			}
			**/
			
			// compute the projection matrix and vector using the solvePnpRansac function
			Mat rotationVector = new Mat();
			Mat translationVector = new Mat();

			boolean extrinsicParametersFound = Calib3d.solvePnPRansac(chessboardCornerPoints, corners, intrinsicParameters,
                    distorsionCoefficients, rotationVector, translationVector);
			
			if (extrinsicParametersFound) {
				/**
				System.out.println("Extrinsic Parameters Found!!!");
				double[] firstRotationParam = new double[1];
				rotationVector.get(0, 0, firstRotationParam);
				double[] secondRotationParam = new double[1];
				rotationVector.get(1, 0, secondRotationParam);
				double[] thirdRotationParam = new double[1];
				rotationVector.get(2, 0, thirdRotationParam);
				System.out.println("Rotation vector: [" + firstRotationParam[0] + ", " + secondRotationParam[0] + ", " + thirdRotationParam[0] + "]");
				
				double[] firstTranslationParam = new double[1];
				translationVector.get(0, 0, firstTranslationParam);
				double[] secondTranslationParam = new double[1];
				translationVector.get(1, 0, secondTranslationParam);
				double[] thirdTranslationParam = new double[1];
				translationVector.get(2, 0, thirdTranslationParam);
				System.out.println("Translation vector: [" + firstTranslationParam[0] + ", " + secondTranslationParam[0] + ", " + thirdTranslationParam[0] + "]");
				//System.out.println(translationVector);
				**/
				
				
				if (debugProjectionParameters) {
					testCameraCalibration(calibrationImage, rotationVector, translationVector, 
							intrinsicParameters, distorsionCoefficients);					
				}
				
				return new ProjectionMatrices(rotationVector, translationVector, intrinsicParameters, distorsionCoefficients);
							}
		}	
		return null;

	}
	
	private void testCameraCalibration(Mat calibrationImage, Mat rotationVector, Mat translationVector,
			Mat intrinsicParameters, MatOfDouble distorsionCoefficients) {
		
		Point3[] arrayOfCubePoints = new Point3[] {
				new Point3(0, 0, 0), new Point3(3, 0, 0), new Point3(3, 0, 2), new Point3(0, 0, 2),
				new Point3(0, 5, 0), new Point3(3, 5, 0), new Point3(3, 5, 2), new Point3(0, 5, 2)
		};
		
		MatOfPoint3f cubePoints = new MatOfPoint3f(arrayOfCubePoints);

		MatOfPoint2f projectedPoints = new MatOfPoint2f();
		Calib3d.projectPoints(cubePoints, rotationVector, translationVector, 
				intrinsicParameters, distorsionCoefficients, projectedPoints);

		drawProjectedPoints(calibrationImage, projectedPoints);
		
	}
	
	private void drawProjectedPoints(Mat image, MatOfPoint2f projectedPoints) {
		List<Point> points = projectedPoints.toList();
		int pointRadius = 10;
		Scalar red = new Scalar(0, 0, 255);
		Scalar green = new Scalar(0, 255, 0);
		Scalar blue = new Scalar(255, 0, 0);
		Scalar black = new Scalar(0, 0, 0);
		Scalar magenta = new Scalar(139, 0, 139);
		Scalar gray = new Scalar(125, 125, 125);
		
		// draw circles
		//Imgproc.circle
		Imgproc.circle(image, points.get(0), pointRadius, black, -1);
		Imgproc.circle(image, points.get(1), pointRadius, blue, -1);
		Imgproc.circle(image, points.get(2), pointRadius, green, -1);
		Imgproc.circle(image, points.get(3), pointRadius, red, -1);
		Imgproc.circle(image, points.get(4), pointRadius, black, -1);
		Imgproc.circle(image, points.get(5), pointRadius, blue, -1);
		Imgproc.circle(image, points.get(6), pointRadius, magenta, -1);
		Imgproc.circle(image, points.get(7), pointRadius, red, -1);
		
		// draw lines
		Imgproc.line(image, points.get(4), points.get(7), gray, 3);
		Imgproc.line(image, points.get(4), points.get(5), gray, 3);
		Imgproc.line(image, points.get(4), points.get(0), gray, 3);
		Imgproc.line(image, points.get(7), points.get(3), gray, 3);
		Imgproc.line(image, points.get(7), points.get(6), gray, 3);
		Imgproc.line(image, points.get(5), points.get(6), gray, 3);
		Imgproc.line(image, points.get(5), points.get(1), gray, 3);
		Imgproc.line(image, points.get(6), points.get(2), gray, 3);
		Imgproc.line(image, points.get(0), points.get(3), gray, 3);
		Imgproc.line(image, points.get(0), points.get(1), gray, 3);
		Imgproc.line(image, points.get(3), points.get(2), gray, 3);
		Imgproc.line(image, points.get(2), points.get(1), gray, 3);
		
		
		BufferedImage imgToSave = Utils.matToBufferedImage(image);
		try {
		    // retrieve image
		    File outputfile = new File("images/projectedImage.png");
		    ImageIO.write(imgToSave, "png", outputfile);
		    System.out.println("Projected image was saved");
		} catch (IOException e) {
		    System.out.println("There was an error while saving projected image!!");
		}
		
	}
	
}
