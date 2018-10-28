package nl.tue.vc.projection;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

public class ProjectionMatrices {

	private Mat rotationVector;
	
	private Mat translationVector;
	
	private Mat intrinsicParameters;
	
	private MatOfDouble distorsionCoefficients;
	
	public ProjectionMatrices(Mat rotationVector, Mat translationVector, 
			Mat intrinsicParameters, MatOfDouble distorsionCoefficients) {
		this.rotationVector = rotationVector;
		this.translationVector = translationVector;
		this.intrinsicParameters = intrinsicParameters;
		this.distorsionCoefficients = distorsionCoefficients;
	}
	
	public Mat getRotationVector(){
		return rotationVector;
	}
	
	public Mat getTranslationVector(){
		return translationVector;
	}
	
	public Mat getIntrinsicParameters(){
		return intrinsicParameters;
	}
	
	public MatOfDouble getDistorsionCoefficients(){
		return distorsionCoefficients;
	}
	
}
