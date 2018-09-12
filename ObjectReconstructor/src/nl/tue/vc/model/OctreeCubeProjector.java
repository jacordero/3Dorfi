package nl.tue.vc.model;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import nl.tue.vc.model.ProjectedPoint;
import nl.tue.vc.projection.ProjectionGenerator;

public class OctreeCubeProjector {
	
	private float cubeLengthX;
	private float cubeLengthY;
	private float cubeLengthZ;
	private float cubeDisplacementX;
	private float cubeDisplacementY;
	private float cubeDisplacementZ;
	private MatOfPoint3f pointsToProject;
	
	public OctreeCubeProjector(float cubeLengthX, float cubeLengthY, float cubeLengthZ, 
			float cubeDisplacementX, float cubeDisplacementY, float cubeDisplacementZ){
		this.cubeLengthX = cubeLengthX;
		this.cubeLengthY = cubeLengthY;
		this.cubeLengthZ = cubeLengthZ;
		this.cubeDisplacementX = cubeDisplacementX;
		this.cubeDisplacementY = cubeDisplacementY;
		this.cubeDisplacementZ = cubeDisplacementZ;
		pointsToProject = createPointsToProject();
	}
	
	private MatOfPoint3f createPointsToProject(){
		Point3[] corners = new Point3[8];
		corners[0] = new Point3(cubeDisplacementX + 0, cubeDisplacementY + 0, cubeDisplacementZ + 0);
		corners[1] = new Point3(cubeDisplacementX + cubeLengthX, cubeDisplacementY + 0 , cubeDisplacementZ + 0);
		corners[2] = new Point3(cubeDisplacementX + cubeLengthX, cubeDisplacementY + 0 , cubeDisplacementZ + cubeLengthZ);
		corners[3] = new Point3(cubeDisplacementX + 0, cubeDisplacementY + 0 , cubeDisplacementZ + cubeLengthZ);
		corners[4] = new Point3(cubeDisplacementX + 0, cubeDisplacementY + cubeLengthY , cubeDisplacementZ + 0);
		corners[5] = new Point3(cubeDisplacementX + cubeLengthX, cubeDisplacementY + cubeLengthY , cubeDisplacementZ + 0);
		corners[6] = new Point3(cubeDisplacementX + cubeLengthX, cubeDisplacementY + cubeLengthY , cubeDisplacementZ + cubeLengthZ);
		corners[7] = new Point3(cubeDisplacementX + 0, cubeDisplacementY + cubeLengthY , cubeDisplacementZ + cubeLengthZ);

		return new MatOfPoint3f(corners);
	}
	
	
	public Mat drawCube(Mat img, List<ProjectedPoint> projectedPoints){
		Scalar red = new Scalar(0, 0, 255);
		Scalar green = new Scalar(0, 255, 0);
		Scalar blue = new Scalar(255, 0, 0);
		Scalar black = new Scalar(0, 0, 0);
		Scalar magenta = new Scalar(139, 0, 139);
		
		Mat imgCopy = img.clone();
		
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
		Imgproc.line(imgCopy, point4, point5, red, thickness);
		Imgproc.line(imgCopy, point5, point6, red, thickness);
		Imgproc.line(imgCopy, point6, point7, red, thickness);
		Imgproc.line(imgCopy, point7, point4, red, thickness);
		
		// Bottom
		Imgproc.line(imgCopy, point0, point1, red, thickness);
		Imgproc.line(imgCopy, point1, point2, red, thickness);
		Imgproc.line(imgCopy, point2, point3, red, thickness);
		Imgproc.line(imgCopy, point3, point0, red, thickness);
		
		// Sides
		Imgproc.line(imgCopy, point0, point4, red, thickness);
		Imgproc.line(imgCopy, point1, point5, red, thickness);
		Imgproc.line(imgCopy, point2, point6, red, thickness);
		Imgproc.line(imgCopy, point3, point7, red, thickness);
				
		
		return imgCopy;
	}
		
	public List<ProjectedPoint> projectPoints(String matrixIndex, ProjectionGenerator projectionGenerator){
		MatOfPoint2f projections = projectionGenerator.projectPoints(this.pointsToProject, matrixIndex);

		List<ProjectedPoint> projectedPoints = new ArrayList<ProjectedPoint>();
		for (Point point : projections.toList()) {
			projectedPoints.add(new ProjectedPoint(point.x, point.y, 1.0));
		}
		
		return projectedPoints;
	}
	

	public Mat drawProjection(Mat image, String matrixIndex, ProjectionGenerator projectionGenerator){
		List<ProjectedPoint> projectedPoints = projectPoints(matrixIndex, projectionGenerator);
		return drawCube(image, projectedPoints);
	}
		
}
