package nl.tue.vc.imgproc;

import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import nl.tue.vc.application.ApplicationConfiguration;

public class SilhouetteExtractor {

	
	//private static Mat 
	
//	private static final int LEFT_LIMIT = 30;
//	private static final int RIGHT_LIMIT = 370;
//	private static final int TOP_LIMIT = 20;
//	private static final int LOWER_LIMIT = 280;
	
	private int leftLimit;
	private int rightLimit;
	private int topLimit;
	private int lowerLimit;
	private int binaryThreshold;
	
	private Mat binaryImage;
	
	private Mat noiseFreeImage;
	
	private Mat sureBackgroundImage;
	
	private Mat sureForegroundImage;
	
	private Mat unknownImage;
	
	private Mat segmentedImage;
	
	public SilhouetteExtractor() {
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		Map<String, Integer> silhouetteConfig = appConfig.getSilhouetteConfiguration();
		leftLimit = silhouetteConfig.get("imageWidthFirstPixel");
		rightLimit = silhouetteConfig.get("imageWidthLastPixel");
		topLimit = silhouetteConfig.get("imageHeightFirstPixel");
		lowerLimit = silhouetteConfig.get("imageHeightLastPixel");
		binaryThreshold = silhouetteConfig.get("binaryThreshold");
	}
	
	
	public void extract(Mat image) {

		// first convert to grayscale
		Mat grayImage = new Mat();			
		if (image.channels() == 3) {
			Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		} else {
			image.copyTo(grayImage);
		}		
				
		// apply binarization process
		binaryImage = new Mat(grayImage.rows(), grayImage.cols(), CvType.CV_32SC1);
		
		// simple tresholding
		//binaryThreshold = 160;
		Imgproc.threshold(grayImage, binaryImage, binaryThreshold, 255, Imgproc.THRESH_BINARY);
		
		// adaptive thresholding
		//Imgproc.adaptiveThreshold(grayImage, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		
		// noise removal
		Mat kernel = Mat.ones(5, 5, CvType.CV_32S);
		noiseFreeImage = new Mat();
		Imgproc.morphologyEx(binaryImage, noiseFreeImage, Imgproc.MORPH_OPEN, kernel);
		
		// sure background area (dilation shrinks the black pixels)
		sureBackgroundImage = new Mat();
		Imgproc.dilate(noiseFreeImage, sureBackgroundImage, kernel);
		
		// sure foreground area (erosion shrinks the white pixels)
		sureForegroundImage = new Mat();
		Imgproc.erode(noiseFreeImage, sureForegroundImage, kernel);
		
		
		unknownImage = new Mat();
		Core.subtract(sureBackgroundImage, sureForegroundImage, unknownImage);
				
		// Marker labelling
		Mat markers = new Mat();
		Imgproc.connectedComponents(binaryImage, markers);
		Mat scaledMarkers = new Mat();
		Core.add(markers, new Scalar(1), scaledMarkers);
		
		byte[] pixelValue = new byte[1];	
		int[] unknownLabel = {0};
		for (int i = 0; i < unknownImage.rows(); i++) {
			for (int j = 0; j < unknownImage.cols(); j++) {
				unknownImage.get(i, j, pixelValue);
				if (pixelValue[0] == -1) {
					scaledMarkers.put(i, j, unknownLabel);
				}
			}
		}
		
		// Apply watersheed algorithm		
		Mat newBinaryImage = new Mat();
		Imgproc.cvtColor(binaryImage, newBinaryImage, Imgproc.COLOR_GRAY2BGR);
		Imgproc.watershed(newBinaryImage, scaledMarkers);
		segmentedImage = new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
		int[] blackPixel = {0};
		int[] whitePixel = {255};
		int[] labelValue = new int[1];
		for (int i = 0; i < scaledMarkers.rows(); i++) {
			int startRegion = -1;
			int endRegion = -1;
			for (int j = 0; j < scaledMarkers.cols(); j++) {
				scaledMarkers.get(i, j, labelValue);
				if (labelValue[0] == -1 && startRegion == -1 && j > leftLimit && i > topLimit && i < lowerLimit) {
					startRegion = j;
				} else if (labelValue[0] == -1 && startRegion > -1 && j < rightLimit && i > topLimit && i < lowerLimit){
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
		
		//return binaryImage;
		
	}
	
	
	public void setBinaryThreshold(int binaryTreshold) {
		this.binaryThreshold = binaryTreshold;
	}


	public int getBinaryThreshold() {
		return binaryThreshold;
	}


	public Mat getBinaryImage() {
		return binaryImage;
	}


	public Mat getNoiseFreeImage() {
		return noiseFreeImage;
	}


	public Mat getSureBackgroundImage() {
		return sureBackgroundImage;
	}


	public Mat getSureForegroundImage() {
		return sureForegroundImage;
	}


	public Mat getUnknownImage() {
		return unknownImage;
	}


	public Mat getSegmentedImage() {
		return segmentedImage;
	}
	
}
