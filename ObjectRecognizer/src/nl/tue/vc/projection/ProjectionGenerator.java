package nl.tue.vc.projection;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;

public class ProjectionGenerator {

	private Mat rotationVector;
	
	private Mat translationVector;
	
	private Mat intrinsicParameters;
	
	private MatOfDouble distorsionCoefficients;
	
	public ProjectionGenerator(Mat rotationVector, Mat translationVector, 
			Mat intrinsicParameters, MatOfDouble distorsionCoefficients) {
		this.rotationVector = rotationVector;
		this.translationVector = translationVector;
		this.intrinsicParameters = intrinsicParameters;
		this.distorsionCoefficients = distorsionCoefficients;
	}
	
	public MatOfPoint2f projectPoints(MatOfPoint3f pointsToProject) {
		MatOfPoint2f projectedPoints = new MatOfPoint2f();
		Calib3d.projectPoints(pointsToProject, rotationVector, translationVector, 
				intrinsicParameters, distorsionCoefficients, projectedPoints);
		return projectedPoints;
	}

}
