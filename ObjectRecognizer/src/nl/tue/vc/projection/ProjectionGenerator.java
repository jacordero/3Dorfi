package nl.tue.vc.projection;

import java.util.HashMap;
import java.util.Map;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;

public class ProjectionGenerator {

	private Map<String, ProjectionMatrices> projectionMatricesMap;
	
	private static final String DEFAULT_PROJECTION_INDEX = "deg-0";
	
	/**
	 * By default we select the projection matrices for zero rotation around the y axis
	 * @param projectionMatrices
	 */
	
	public ProjectionGenerator(){
		projectionMatricesMap = new HashMap<String, ProjectionMatrices>();
	}
	
	public ProjectionGenerator(Map<String, ProjectionMatrices> projectionMatricesMap){
		this.projectionMatricesMap = projectionMatricesMap;
	}
	
	public void addProjectionMatrices(String projectionIndex, ProjectionMatrices matrices){
		projectionMatricesMap.put(projectionIndex, matrices);
	}
	
	public MatOfPoint2f projectPoints(MatOfPoint3f pointsToProject){
		return projectPoints(pointsToProject, DEFAULT_PROJECTION_INDEX);
	}
	
	public MatOfPoint2f projectPoints(MatOfPoint3f pointsToProject, String projectionMatricesIndex) {
		
		ProjectionMatrices projectionMatrices;
		if (projectionMatricesMap.get(projectionMatricesIndex) != null){
			projectionMatrices = projectionMatricesMap.get(projectionMatricesIndex);
		} else {
			projectionMatrices = projectionMatricesMap.get(DEFAULT_PROJECTION_INDEX);
		}
		
		MatOfPoint2f projectedPoints = new MatOfPoint2f();
		Calib3d.projectPoints(pointsToProject, projectionMatrices.getRotationVector(),
				projectionMatrices.getTranslationVector(), projectionMatrices.getIntrinsicParameters(), 
				projectionMatrices.getDistorsionCoefficients(), projectedPoints);
		return projectedPoints;
	}
		
}
