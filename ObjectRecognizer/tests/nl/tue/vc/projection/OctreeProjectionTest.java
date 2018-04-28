package nl.tue.vc.projection;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.imgproc.CameraCalibrator;

public class OctreeProjectionTest {

	
	private CameraCalibrator cameraCalibrator;
	
	private ProjectionGenerator projectionGenerator;
	
	private static final String CALIBRATION_IMAGE = "images/calibrationImage.png";
	
	private Mat calibrationImage;
	
	private OctreeTest octree;
	
	public OctreeProjectionTest(){
		calibrationImage = loadCalibrationImage();
		octree = generateOctree();
		System.out.println(octree);
		cameraCalibrator = new CameraCalibrator();
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
	}
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		OctreeProjectionTest projectionTest = new OctreeProjectionTest();
		projectionTest.projectCubes();
		
		//projectionTest.generateOctree();
		//System.out.println(projectionTest.octree);
	}
	
	public void projectCubes(){
		NodeTest root = octree.getRoot();
		iterateCubesAux(root);
	}
	
	public void iterateCubesAux(NodeTest node){
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

		if (!node.isLeaf()){
			System.out.println("\n********** Projecting children *************");
			for (NodeTest children: node.getChildren()){
				iterateCubesAux(children);				
			}
		}
		//
		
		//for (Point projection: )
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
		
		return new OctreeTest(8, 4, 4, 4, 1);
	}
	
	public void calibrateCamera(){
		projectionGenerator = cameraCalibrator.calibrate(calibrationImage, true);
	}
	
	public void projectOctreeIntoImage(Mat testImage){
		
	}
	
}
