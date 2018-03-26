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
	
	
	public static Mat extract(Mat image) {
		
		System.out.println("Extract silhouettes method was called...");
		//Mat oldImage = this.image;
		Mat grayImage = new Mat();
			
		// first convert to grayscale
		System.out.println("Image channels: " + image.channels());
		if (image.channels() == 3) {
			Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		} else {
			image.copyTo(grayImage);
		}		
		
		// create markers for the watershed algorithm
		
		
		
		// apply binarization process
		Mat binaryImage = new Mat(grayImage.rows(), grayImage.cols(), CvType.CV_32SC1);
		//Imgproc.threshold()
		Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_OTSU);
		//updateViewAndPause(transformedImage, Utils.mat2Image(binaryImage));
		//System.out.println(binaryImage.dump());
		
		
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
		
		/**
		System.out.println(unknown.type());
		System.out.println(CvType.CV_16U);
		System.out.println(CvType.CV_32F);
		System.out.println(CvType.CV_64F);
		System.out.println(CvType.CV_8S);
		System.out.println(CvType.CV_8U);
		**/
		
		// Marker labelling
		Mat markers = new Mat();
		Imgproc.connectedComponents(binaryImage, markers);
		
		//System.out.println(markers.size());
		//System.out.println(markers.toString());
		// We use markers.dump to print the values of a matrix of type Mat
		//System.out.println(markers.dump());
		
		//System.out.println("\n\n************************************************\n\n");
		Mat scaledMarkers = new Mat();
		Core.add(markers, new Scalar(1), scaledMarkers);
		System.out.println("***********************************");
		System.out.println(binaryImage.type());
		System.out.println(scaledMarkers.type());
		System.out.println("***********************************");
		//System.out.println(scaledMarkers.dump());

		
		
		byte[] pixelValue = new byte[1];	
		int[] unknownLabel = {0};
		for (int i = 0; i < unknown.rows(); i++) {
			for (int j = 0; j < unknown.cols(); j++) {
				unknown.get(i, j, pixelValue);
				//System.out.println(pixelValue[0]);
				// instead of getting 255 we might be getting -1
				if (pixelValue[0] == -1) {
					scaledMarkers.put(i, j, unknownLabel);
					//System.out.println("**** Found unknown boundary *****");
				}
			}
		}
		
		//System.out.println(scaledMarkers.dump());
		//byte[] pixelValue = new byte[1];	
		/**
		Mat newBinaryImage = new Mat(binaryImage.rows(), binaryImage.cols(), scaledMarkers.type());
		int[] binZero = {0};
		int[] binOne = {255};
		for (int i = 0; i < binaryImage.rows(); i++) {
			for (int j = 0; j < binaryImage.cols(); j++) {
				binaryImage.get(i, j, pixelValue);
				//System.out.println(pixelValue[0]);
				// instead of getting 255 we might be getting -1
				if (pixelValue[0] == -1) {
					//scaledMarkers.put(i, j, unknownLabel);
					newBinaryImage.put(i,  j, binOne);
					//System.out.println("**** Found unknown boundary *****");
				} else {
					newBinaryImage.put(i,  j, binZero);
					//System.out.println("Something weird is happening");
				}
			}
		}
		**/
		
		// Apply watersheed algorithm
		
		Mat newBinaryImage = new Mat();
		Imgproc.cvtColor(binaryImage, newBinaryImage, Imgproc.COLOR_GRAY2BGR);
		
		//System.out.println(binaryImage.dump());
		System.out.println(binaryImage.type());
		Imgproc.watershed(newBinaryImage, scaledMarkers);

		
		Mat segmentedImage = new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
		int[] blackPixel = {0};
		int[] whitePixel = {255};
		int[] labelValue = new int[1];
		int leftLimit = 30;
		int rightLimit = 370;
		int topLimit = 20;
		int lowerLimit = 280;
		for (int i = 0; i < scaledMarkers.rows(); i++) {
			//boolean fillRegion = false;
			int startRegion = -1;
			int endRegion = -1;
			for (int j = 0; j < scaledMarkers.cols(); j++) {
				scaledMarkers.get(i, j, labelValue);
				//System.out.println(pixelValue[0]);
				// instead of getting 255 we might be getting -1
				if (labelValue[0] == -1 && startRegion == -1 && j > leftLimit && i > topLimit && i < lowerLimit) {
					startRegion = j;
				} else if (labelValue[0] == -1 && startRegion > -1 && j < rightLimit && i > topLimit && i < lowerLimit){
					endRegion = j;
				}
			}
			
			for (int j = 0; j < scaledMarkers.cols(); j++) {
				segmentedImage.put(i, j, whitePixel);
			}
			
			//System.out.println("Start: " + startRegion + ", end: " + endRegion);
			
			if (endRegion > startRegion) {
				for (int j = startRegion; j <= endRegion; j++) {
					segmentedImage.put(i, j, blackPixel);
				}
			}
			
		}
		

		
		/**
		System.out.println("Number of rows: " + scaledMarkers.rows());
		scaledMarkers.get(0, 0, oneValue);
		System.out.println("Value at position [0, 0]: " + oneValue[0]);
		scaledMarkers.get(0, 1, oneValue);
		System.out.println("Value at position [0, 1]: " + oneValue[0]);
		scaledMarkers.get(0, 2, oneValue);
		System.out.println("Value at position [0, 2]: " + oneValue[0]);
		scaledMarkers.get(0, 3, oneValue);
		System.out.println("Value at position [0, 3]: " + oneValue[0]);
		scaledMarkers.get(0, 4, oneValue);
		System.out.println("Value at position [0, 4]: " + oneValue[0]);
		scaledMarkers.get(0, 5, oneValue);
		System.out.println("Value at position [0, 5]: " + oneValue[0]);
		scaledMarkers.get(0, 6, oneValue);
		System.out.println("Value at position [0, 6]: " + oneValue[0]);
		scaledMarkers.get(0, 7, oneValue);
		System.out.println("Value at position [0, 7]: " + oneValue[0]);

		System.out.println(oneValue.length);
		**/
		//for (int i )
		
		//return unknown;
		//System.out.println(newBinaryImage.channels());
		//System.out.println(markedImage.dump());
		return segmentedImage;
		
		/**
		// apply morphological operations
	    int kernelWindow = 2;
	    Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2*kernelWindow + 1, 2*kernelWindow+1));
	    //Imgproc.morphologyEx(binaryImage, binaryImage, Imgproc.MORPH_OPEN, kernelElement);
	    //Imgproc.morphologyEx(binaryImage, binaryImage, Imgproc.MORPH_CLOSE, kernelElement);
	    Imgproc.erode(binaryImage, binaryImage, erodeKernel);
		//updateView(transformedImage, Utils.mat2Image(binaryImage));
	     
	    Mat dilatationKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2*kernelWindow + 1, 2*kernelWindow+1));
	    Imgproc.dilate(binaryImage, binaryImage, dilatationKernel);
		updateView(transformedImage, Utils.mat2Image(binaryImage));	
		 **/
		//public static void watershed(Mat image, Mat markers)
		
	}
	
	
}
