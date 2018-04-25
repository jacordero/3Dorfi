package nl.tue.vc.projection;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TransformMatrices {
	
	public double screenWidth;
	
	public double screenHeight;
	
	private double fieldOfView;
	
	private double[][] worldRotationYMatrix;
	
	private double[][] viewMatrix;
	
	private double[][] projectionMatrix;
	
	private double[][] windowMatrix;
	
	private double[][] intrinsicParametersMatrix;
	
	private double[][] calibratedProjectionMatrix;
	
	private double[] distorsionParameters;
	
	private static final double DEG2RAD = 3.14159265/180;
	
	private final double near = 1.0;
	
	private Vector3D cameraPosition;
	
	// not being used so far
	private Vector3D targetPosition;

	private double worldRotationYAngle;
	
	private boolean useDefaultViewMatrix = true;
	
	private static final int IMAGE_SCALING_FACTOR = 2;
	
	
	public TransformMatrices(double screenWidth, double screenHeight, double fieldOfView) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.fieldOfView = fieldOfView;
		
		worldRotationYAngle = 0.0;
		cameraPosition = new Vector3D(27.1285, 28.9841, 14.7959, 1.0);	
		targetPosition = new Vector3D(0,0,0,1);
		buildMatrices();
	}
	
	
	private void buildMatrices() {
		// matrices obtained by using camera calibration procedure
		buildCalibrationCameraMatrices();
		
		buildWorldRotationYMatrix();
		buildViewMatrix();
		buildProjectionMatrix();
		
		windowMatrix = new double[][] {
			{screenWidth/2, 0, 0, screenWidth/2},
			{0, screenHeight/2, 0, screenHeight/2},
			{0, 0, 0.5, 0.5},
			{0, 0, 0, 1}
		};	
	}
	
	private void buildCalibrationCameraMatrices() {
		intrinsicParametersMatrix = new double[][] {
			{1422.6417 / IMAGE_SCALING_FACTOR, 0, 640.4154 / IMAGE_SCALING_FACTOR},
			{0, 1418.4124 / IMAGE_SCALING_FACTOR, 446.3054 / IMAGE_SCALING_FACTOR},
			{0, 0, 1}
		};
		
		calibratedProjectionMatrix = new double[][] {
			{0.0, 0.0, 0.0},
			{0.0, 0.0, 0.0},
			{0.0, 0.0, 0.0},
		};
		
		distorsionParameters = new double[] {-0.0379, 0.2845, 0.0006, -0.0024};
	}
	
	private void buildWorldRotationYMatrix() {
		worldRotationYMatrix = new double[][] {
			{Math.cos(Math.toRadians(worldRotationYAngle)), 0, Math.sin(Math.toRadians(worldRotationYAngle)), 0},
			{0, 1, 0, 0},
			{-1*Math.sin(Math.toRadians(worldRotationYAngle)), 0, Math.cos(Math.toRadians(worldRotationYAngle)), 0},
			{0, 0, 0, 1}
		};
	}
	
	private void buildViewMatrix() {

		if (useDefaultViewMatrix) {
			viewMatrix = new double[][] {
				{0.48, 0.00, -0.88, -0.00},
				{-0.60, 0.73, -0.33, 0.00},
				{0.64, 0.68, 0.35, -42.25},
				{0.00, 0.00, 0.00, 1.0}
			};			
		} else {
			viewMatrix = buildViewFromLookAt();
		}
	}
	
	private double[][] buildViewFromLookAt() {
		Vector3D zcAxis = cameraPosition.substract(targetPosition);
		zcAxis.normalizeXYZ();
		Vector3D zcNegativeAxis = new Vector3D(-1.0*zcAxis.getX(), -1.0*zcAxis.getY(), 
				-1.0*zcAxis.getZ(), -1.0*zcAxis.getZ());
		//zcNegativeAxis.setZ(-1.0*zcNegativeAxis.getZ());
		zcNegativeAxis.normalizeXYZ(); 
		
		Vector3D upVector = new Vector3D(0,1,0,0);
		
		Vector3D xcAxis = Vector3D.crossProduct(zcNegativeAxis, upVector);
		Vector3D ycAxis = Vector3D.crossProduct(xcAxis, zcNegativeAxis);
		
		double[][] lookAtRot = new double[][] {
			{xcAxis.getX(), ycAxis.getX(), zcNegativeAxis.getX()},
			{xcAxis.getY(), ycAxis.getY(), zcNegativeAxis.getY()},
			{xcAxis.getZ(), ycAxis.getZ(), zcNegativeAxis.getZ()}
		};
		
		double[][] lookAtRotTrans = new double[][] {
			{xcAxis.getX(), xcAxis.getY(), xcAxis.getZ()},
			{ycAxis.getX(), ycAxis.getY(), xcAxis.getZ()},
			{zcNegativeAxis.getX(), zcNegativeAxis.getY(), zcNegativeAxis.getZ()}
		};
		
		double[][] negLookAtRotTrans = new double[][] {
			{-1.0*xcAxis.getX(), -1.0*xcAxis.getY(), -1.0*xcAxis.getZ(), 0},
			{-1.0*ycAxis.getX(), -1.0*ycAxis.getY(), -1.0*ycAxis.getZ(), 0},
			{zcNegativeAxis.getX(), zcNegativeAxis.getY(), zcNegativeAxis.getZ(), 0},
			{0, 0, 0, 1}
		};

		/**
		double[][] negLookAtRotTrans = new double[][] {
			{-1.0*xcAxis.getX(), -1.0*ycAxis.getX(), -1.0*zcNegativeAxis.getX(), 0},
			{-1.0*xcAxis.getY(), -1.0*ycAxis.getY(), -1.0*zcNegativeAxis.getY(), 0},
			{-1.0*xcAxis.getZ(), -1.0*ycAxis.getZ(), -1.0*zcNegativeAxis.getZ(), 0},
			{0, 0, 0, 1}
		};**/
		
		Vector3D invTranslation = transformVector(cameraPosition, negLookAtRotTrans);
		
		return new double[][] {
			{xcAxis.getX(), xcAxis.getY(), xcAxis.getZ(), invTranslation.getX()},
			{ycAxis.getX(), ycAxis.getY(), ycAxis.getZ(), invTranslation.getY()},
			{zcAxis.getX(), zcAxis.getY(), zcAxis.getZ(), invTranslation.getZ()},
			{0, 0, 0, 1}
		};
		
		/**
		return new double[][] {
			{xcAxis.getX(), ycAxis.getX(), zcNegativeAxis.getX(), invTranslation.getX()},
			{xcAxis.getY(), ycAxis.getY(), zcNegativeAxis.getY(), invTranslation.getY()},
			{xcAxis.getZ(), ycAxis.getZ(), zcNegativeAxis.getZ(), invTranslation.getZ()},
			{0, 0, 0, 1}
		};
		**/		
	}
	
	private void buildProjectionMatrix() {
		double tangent = Math.tan((fieldOfView / 2) * DEG2RAD);
		double aspectRatio = screenWidth / screenHeight;
		double height = near * tangent;
		double width = height * aspectRatio;
		
		System.out.println(near/width);
		System.out.println(near/height);
		
		projectionMatrix = new double[][] {
			//{1.74, 0.00, 0.00, 0.00},
			//{0.00, 3.44, 0.00, 0.00},
			{near/width, 0.0, 0.0, 0.0},
			{0.0, near/height, 0.0, 0.0},
			{0.00, 0.00, -1.35, -27.70},
			{0.00, 0.00, -1.0, 0.00}
		};		
	}
	
	
	
	private static Vector3D transformVector(Vector3D vector, double[][] matrix) {
		double[] vecArray = vector.getArray();
		double[] result = new double[] {0.0, 0.0, 0.0, 0.0};
		for (int i = 0; i < matrix[0].length; i++) {
			double sum = 0.0;
			for (int j = 0; j < matrix[0].length; j++) {
				sum += matrix[i][j] * vecArray[j];
				//System.out.print(matrix[i][j] + ", ");
			}
			result[i] = sum;
		}
		
		//for (int i = 0; i < result.length; i++) {
		//	System.out.print(result[i] + " ");
		//}
		//System.out.println("");
		
		return new Vector3D(result);
	}
	
	public void updateCameraPositionX(double value) {
		this.cameraPosition.setX(value);
		buildViewMatrix();
	}

	public void updateCameraPositionY(double value) {
		this.cameraPosition.setY(value);
		buildViewMatrix();
	}

	public void updateCameraPositionZ(double value) {
		this.cameraPosition.setZ(value);
		buildViewMatrix();
	}
	
	public void updateViewMatrix(Vector3D cameraPosition, Vector3D targetPosition) {		
		this.cameraPosition = cameraPosition;
		// not being used so far
		this.targetPosition = targetPosition;
		useDefaultViewMatrix = false;		
		buildViewMatrix();
	}
	
	
	public void testViewMatrixConstruction(Vector3D cameraPosition, Vector3D targetPosition) {
		this.cameraPosition = cameraPosition;
		this.targetPosition = targetPosition;
		double[][] result = buildViewFromLookAt();
		print4DMatrix(result);
	}
	
	public void updateFieldOfView(double fieldOfView) {
		this.fieldOfView = fieldOfView;
		buildProjectionMatrix();
	}
	
	public Vector3D toWorldCoordinates(Vector3D vector) {
		return transformVector(vector, worldRotationYMatrix);
	}
	
	public void updateWorldRotationYAngle(double yAngle) {
		worldRotationYAngle = yAngle;
		buildWorldRotationYMatrix();
	}
	
	public Vector3D toViewCoordinates(Vector3D vector) {
		return transformVector(vector, viewMatrix);
	}
	
	public Vector3D toClipCoordinates(Vector3D vector) {
		return transformVector(vector, projectionMatrix);
	}
	
	public Vector3D toNDCCoordinates(Vector3D vector) {
		double w = vector.getW();
		Vector3D ndcVector = new Vector3D(vector.getX()/w, vector.getY()/w, vector.getZ()/w, 1.0);
		ndcVector.setY(-1.0*ndcVector.getY());
		//ndcVector.setX(-1.0*ndcVector.getX());

		//return result;

		return ndcVector;
		
	}
	
	public Vector3D toWindowCoordinates(Vector3D vector) {
		return transformVector(vector, windowMatrix);
		//return new double[]{0.0};
	}
	
	public void print4DMatrix(double[][] matrix) {
		NumberFormat formatter = new DecimalFormat("#0.00"); 
		for (int i = 0; i < matrix.length; i++) {
			System.out.print("[ ");
			for (int j = 0; j < matrix.length; j++) {
				System.out.print(formatter.format(matrix[i][j]));
				System.out.print(" ");
			}
			System.out.println("]");
		}
	}
}
