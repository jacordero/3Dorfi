package nl.tue.vc.imgproc;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import nl.tue.vc.application.utils.Utils;

public class SilhouetteExtractor {

	
	//private static Mat 
	
	private static final int LEFT_LIMIT = 30;
	private static final int RIGHT_LIMIT = 370;
	private static final int TOP_LIMIT = 20;
	private static final int LOWER_LIMIT = 280;
	
	public static Mat extract(Mat image) {

		// first convert to grayscale
		Mat grayImage = new Mat();			
		if (image.channels() == 3) {
			Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		} else {
			image.copyTo(grayImage);
		}		
				
		// apply binarization process
		Mat binaryImage = new Mat(grayImage.rows(), grayImage.cols(), CvType.CV_32SC1);
		Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_OTSU);
		
		// noise removal
		Mat kernel = Mat.ones(3, 3, CvType.CV_32S);
		Mat noiseFree = new Mat();
		Imgproc.morphologyEx(binaryImage, noiseFree, Imgproc.MORPH_OPEN, kernel);
		
		// sure background area (dilation shrinks the black pixels)
		Mat sureBackground = new Mat();
		Imgproc.dilate(noiseFree, sureBackground, kernel);
		
		// sure foreground area (erosion shrinks the white pixels)
		Mat sureForeground = new Mat();
		Imgproc.erode(noiseFree, sureForeground, kernel);
		
		
		Mat unknown = new Mat();
		Core.subtract(sureBackground, sureForeground, unknown);
				
		// Marker labelling
		Mat markers = new Mat();
		Imgproc.connectedComponents(binaryImage, markers);
		Mat scaledMarkers = new Mat();
		Core.add(markers, new Scalar(1), scaledMarkers);
		
		byte[] pixelValue = new byte[1];	
		int[] unknownLabel = {0};
		for (int i = 0; i < unknown.rows(); i++) {
			for (int j = 0; j < unknown.cols(); j++) {
				unknown.get(i, j, pixelValue);
				if (pixelValue[0] == -1) {
					scaledMarkers.put(i, j, unknownLabel);
				}
			}
		}
		
		// Apply watersheed algorithm		
		Mat newBinaryImage = new Mat();
		Imgproc.cvtColor(binaryImage, newBinaryImage, Imgproc.COLOR_GRAY2BGR);
		Imgproc.watershed(newBinaryImage, scaledMarkers);
		Mat segmentedImage = new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
		int[] blackPixel = {0};
		int[] whitePixel = {255};
		int[] labelValue = new int[1];
		for (int i = 0; i < scaledMarkers.rows(); i++) {
			int startRegion = -1;
			int endRegion = -1;
			for (int j = 0; j < scaledMarkers.cols(); j++) {
				scaledMarkers.get(i, j, labelValue);
				if (labelValue[0] == -1 && startRegion == -1 && j > LEFT_LIMIT && i > TOP_LIMIT && i < LOWER_LIMIT) {
					startRegion = j;
				} else if (labelValue[0] == -1 && startRegion > -1 && j < RIGHT_LIMIT && i > TOP_LIMIT && i < LOWER_LIMIT){
					endRegion = j;
				}
			}
			
			for (int j = 0; j < scaledMarkers.cols(); j++) {
				segmentedImage.put(i, j, whitePixel);
			}
			
			
			if (endRegion > startRegion) {
				for (int j = startRegion; j <= endRegion; j++) {
					segmentedImage.put(i, j, blackPixel);
				}
			}
			
		}
		
		return segmentedImage;
		
	}
	
	
}
