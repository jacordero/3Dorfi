package nl.tue.vc.imgproc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.application.utils.Utils;

public class SilhouetteExtractor {

	
	private int deltaWidth;
	private int deltaHeight;
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
	
	private Mat grayImage;
	
	private Mat equalizedImage;
	
	private Mat cleanedBinaryImage;
	
	private int imageWidth;
	
	private int imageHeight;

	private boolean DEBUG_OPS;
	
	public SilhouetteExtractor() {
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		Map<String, Integer> silhouetteConfig = appConfig.getSilhouetteConfiguration();
		
//		leftLimit = silhouetteConfig.get("imageWidthFirstPixel");
//		rightLimit = silhouetteConfig.get("imageWidthLastPixel");
//		topLimit = silhouetteConfig.get("imageHeightFirstPixel");
//		lowerLimit = silhouetteConfig.get("imageHeightLastPixel");
		DEBUG_OPS = false;
		binaryThreshold = silhouetteConfig.get("binaryThreshold");
	}
	
	
	public void extract(Mat image, String method) {
		imageWidth = image.cols();
		imageHeight = image.rows();
		deltaWidth = imageWidth / 4;//3*(imageWidth/8);//imageWidth / 4;
		deltaHeight = 3*(imageHeight/8); //imageHeight / 4;
		
		Utils.debugNewLine("Silhouette extration for image with dimensions: [" + imageWidth + ", " + imageHeight + "]", DEBUG_OPS);
		Utils.debugNewLine("Segmentation algorithm: " + method, DEBUG_OPS);
		

		// first convert to grayscale
		grayImage = new Mat();			
		if (image.channels() == 3) {
			Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		} else {
			image.copyTo(grayImage);
		}		
		
		
		// apply binarization process
		
		equalizedImage = equalization(grayImage);
		
		binaryImage = new Mat(equalizedImage.rows(), equalizedImage.cols(), CvType.CV_32SC1);
		
		// simple tresholding
		//binaryThreshold = 160;
		Imgproc.threshold(equalizedImage, binaryImage, binaryThreshold, 255, Imgproc.THRESH_BINARY);
		
		cleanedBinaryImage = cleanImage(binaryImage);
		
		// adaptive thresholding
		//Imgproc.adaptiveThreshold(grayImage, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		
		// noise removal
		Mat kernel = Mat.ones(5, 5, CvType.CV_32S);
		noiseFreeImage = new Mat();
		Imgproc.morphologyEx(cleanedBinaryImage, noiseFreeImage, Imgproc.MORPH_OPEN, kernel);
		
		if (method.equals("Binarization")) {
			simpleBinarization();
		} else if (method.equals("Watersheed")) {
			watersheed();
		} else if (method.equals("Equalized")){
			equalizedBinarization();
		} else {
			throw new RuntimeException("Invalid binarization algorithm!");
		}		
		//return binaryImage;
		
	}
	
	private void simpleBinarization() {
		Utils.debugNewLine("Applying Binarization!!!", DEBUG_OPS);			
		segmentedImage = noiseFreeImage;
		sureBackgroundImage = noiseFreeImage;
		sureForegroundImage = noiseFreeImage;
		unknownImage = noiseFreeImage;		
	}
	
	private void watersheed() {		
		Utils.debugNewLine("Applying Watersheed!", DEBUG_OPS);			
		
		Mat kernel = Mat.ones(5, 5, CvType.CV_32S);

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
	}
	
	private Mat equalization(Mat binaryImage) {
		Mat equalized = binaryImage.clone();//new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
		int minimum = 255;
		int maximum = 0;
		byte[] byteValue = new byte[1];
		int value;
		for (int i = 0; i < grayImage.rows(); i++) {
			for (int j = 0; j < grayImage.cols(); j++) {
				grayImage.get(i, j, byteValue);
				value = convertByteToInt(byteValue);
				if (value > maximum) {
					maximum = value;
				}				
				if (value < minimum) {
					minimum = value;
				}
			}
		}
		
		Utils.debugNewLine("Equalization info: Minimum grayscale value = " + minimum + ", Maximum grayscale value= " + maximum, DEBUG_OPS);			
		
		for (int i = 0; i < grayImage.rows(); i++) {
			for (int j = 0; j < grayImage.cols(); j++) {
				grayImage.get(i, j, byteValue);
				value = convertByteToInt(byteValue);
				double newValue = (1.0*(value - minimum)/(maximum - minimum))*255;
				//System.out.println("Old value: " + value + ", new value: " + newValue);
				//equalized.put(i, j, convertIntToByte((int) newValue));
				equalized.put(i, j, (int) newValue);
			}
		}
		
		return equalized;
	}
	
	private Mat cleanImage(Mat binImage) {
		Mat cleaned = binImage.clone();//new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
		
		//Mat markers = new Mat();
		Mat markers = connectedComponents(cleaned);
		
		//Imgproc.connectedComponents(cleaned, markers, 8, CvType.CV_32SC1);
		//Imgproc.connectedComponents(cleaned, markers);
		
		Set<Integer> selectedMarks = new HashSet<>();
		int firstColumn = (imageWidth / 2) - deltaWidth;
		int lastColumn = (imageWidth / 2) + deltaWidth;
		int firstRow = (imageHeight / 2) - deltaHeight;
		int lastRow = (imageHeight / 2) + deltaHeight;
		
		Utils.debugNewLine("Selecting region [" + firstColumn + ":" + lastColumn + ", " + firstRow + ":" + lastRow + "]", DEBUG_OPS);
		
		int[] labelValue = new int[1];
		for (int i = firstRow; i < lastRow; i++) {
			for (int j = firstColumn; j < lastColumn; j++) {
				markers.get(i, j, labelValue);
				selectedMarks.add(new Integer(labelValue[0]));
			}
		}
		
		if (DEBUG_OPS) {
			for(Integer mark: selectedMarks) {
				System.out.println("sm: " + mark.toString());
			}			
		}
		
		byte[] whitePixel = {(byte)-1};
		byte[] value = new byte[1];
		int[] lvalue = new int[1];
		for (int i = 0; i < markers.rows(); i++) {
			for (int j = 0; j < markers.cols(); j++) {
				markers.get(i,  j, labelValue);
				Integer label = new Integer(labelValue[0]);
				Utils.debugNewLine(String.format("%02d", label) + " ", DEBUG_OPS);					
				
				//markers.get(i, j, lvalue);
				//System.out.print(lvalue[0]);
				//System.out.print(" ");
				if (!selectedMarks.contains(label)) {
					//System.out.println("nsm: " + label.toString());
					//cleaned.put
					binImage.get(i, j, value);
					//System.out.print(value[0]);
					//System.out.print(" ");
					cleaned.put(i, j, whitePixel);
				}
			}
			Utils.debugNewLine("", DEBUG_OPS);
		}
		
		return cleaned;
	}
	
	
	private void equalizedBinarization() {
		Mat equalized = new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
		int minimum = 255;
		int maximum = 0;
		byte[] byteValue = new byte[1];
		int value;
		for (int i = 0; i < grayImage.rows(); i++) {
			for (int j = 0; j < grayImage.cols(); j++) {
				grayImage.get(i, j, byteValue);
				
				
				value = convertByteToInt(byteValue);
				/**
				if (value > 128) {
					System.out.print(".");
				} else {
					System.out.print("x");
				}
				**/
				//System.out.print(value + ", ");
				//System.out.print((new BigInteger(byteValue)).toString() + ", ");
				if (value > maximum) {
					maximum = value;
				}
				
				if (value < minimum) {
					minimum = value;
				}
				
			}
			//System.out.println("");
		}
		
		Utils.debugNewLine("Equalized values: Minimum = " + minimum + ", Maximum = " + maximum, DEBUG_OPS);
		
		for (int i = 0; i < grayImage.rows(); i++) {
			for (int j = 0; j < grayImage.cols(); j++) {
				grayImage.get(i, j, byteValue);
				value = convertByteToInt(byteValue);
				double newValue = (1.0*(value - minimum)/(maximum - minimum))*255;
				//System.out.println("Old value: " + value + ", new value: " + newValue);
				equalized.put(i, j, (int)newValue);
			}
		}
		
		segmentedImage = equalized;
		sureBackgroundImage = equalized;
		sureForegroundImage = equalized;
		unknownImage = equalized;		
	}
	
	private Mat connectedComponents(Mat binarizedImage) {
		int rows = binarizedImage.rows();
		int cols = binarizedImage.cols();
		Mat labeledImage = Mat.zeros(rows, cols, CvType.CV_32SC1);
		
		int labelCounter = 0;
		
		// first past
		
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				
				byte[] binValue = new byte[1];
				binarizedImage.get(row, col, binValue);
				
				// found a black value to label
				Utils.debugNewLine(Byte.toString(binValue[0]), DEBUG_OPS);
				Utils.debugNewLine(" ", DEBUG_OPS);
				if (binValue[0] == 0) {
					
					int left = col - 1;
					int right = col + 1;
					int top = row - 1;
					int bottom = row + 1;
					
					int[] currentLabel = new int[1];
					int[] finalLabel = {0};
					
					for (int i = top; i <= bottom; i++) {
						for (int j = left; j <= right; j++) {
							if (i >= 0 && i < rows && j >= 0 && j < cols) {
								labeledImage.get(i, j, currentLabel);
								if (currentLabel[0] != 0) {
									finalLabel[0] = currentLabel[0];
								}
							}
						}
					}
					
					// we need to create a new label
					if (finalLabel[0] == 0) {
						labelCounter++;
						finalLabel[0] = labelCounter;
						labeledImage.put(row, col, finalLabel);
					}// we can assign the previous label
					else {
						labeledImage.put(row, col, finalLabel);
					}
				}
			}
			Utils.debugNewLine("", DEBUG_OPS);
		}
		
		// second past
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				
				byte[] binValue = new byte[1];
				binarizedImage.get(row, col, binValue);
				
				// found a black value to label
				if (binValue[0] == 0) {
					
					int left = col - 1;
					int right = col + 1;
					int top = row - 1;
					int bottom = row + 1;
					
					int[] currentLabel = new int[1];
					int[] finalLabel = new int[1];
					
					finalLabel[0] = labelCounter;
					// select the smallest label among the 8 connectivity neighbors
					for (int i = top; i <= bottom; i++) {
						for (int j = left; j <= right; j++) {
							if (i >= 0 && i < rows && j >= 0 && j < cols) {
								labeledImage.get(i, j, currentLabel);
								if (currentLabel[0] != 0 && currentLabel[0] < finalLabel[0]) {
									finalLabel[0] = currentLabel[0];
								}
							}
						}
					}
					
					labeledImage.put(row, col, finalLabel);					
				}
			}
		}
		
		Utils.debugNewLine("Number of labels = " + labelCounter, DEBUG_OPS);
		return labeledImage;
	}
	
	
	private int convertByteToInt(byte[] bValue) {
		return bValue[0] & 0xff;
		/*
		int sign = bValue[0] & 0x80;
		// dealing with a negative number
		if (sign > 0) {
			return (~bValue[0] & 0xff) + 1;
		} else {
			return bValue[0] & 0xff;
		}
		*/
	}
	
	private byte convertIntToByte(int value) {
		return (byte) value;
		/**
		if (value > 127) {
			return (byte)(value - 255);
		} else {
			return (byte) value;
		}
		**/
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
	
	public Mat getEqualizedImage() {
		return equalizedImage;
	}
	
	public Mat getCleanedBinaryImage() {
		return cleanedBinaryImage;
	}
}
