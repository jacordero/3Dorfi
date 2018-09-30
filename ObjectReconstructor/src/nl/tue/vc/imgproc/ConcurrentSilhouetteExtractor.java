package nl.tue.vc.imgproc;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import nl.tue.vc.application.utils.Utils;

public class ConcurrentSilhouetteExtractor implements Callable<SegmentedImageStruct> {
	
	private int deltaWidth;
	private int deltaHeight;
	private int leftLimit;
	private int rightLimit;
	private int topLimit;
	private int lowerLimit;
	private int binaryThreshold;
	
	private String segmentationMethod;
	
	private Mat originalImage;
	
	private Mat segmentedImage;
	
	private String imageName;
	
	private int imageWidth;
	
	private int imageHeight;

	private boolean DEBUG_OPS;
	
	public ConcurrentSilhouetteExtractor(Mat image, String imageName, String method, int binaryThreshold) {
		DEBUG_OPS = false;
		originalImage = image;
		segmentationMethod = method;
		this.binaryThreshold = binaryThreshold;
		this.imageName = imageName;
	}
	
	public SegmentedImageStruct call(){
		return extract(originalImage, segmentationMethod, binaryThreshold);
	}
	
	public Mat segment(){
		SegmentedImageStruct struct = extract(originalImage, segmentationMethod, binaryThreshold);
		return struct.getImage();
	}
	
	private SegmentedImageStruct extract(Mat image, String method, int binaryThreshold) {
		imageWidth = image.cols();
		imageHeight = image.rows();
		deltaWidth = imageWidth / 4;
		deltaHeight = 3*(imageHeight/8);
		
		Utils.debugNewLine("Silhouette extration for image with dimensions: [" + imageWidth + ", " + imageHeight + "]", DEBUG_OPS);
		Utils.debugNewLine("Segmentation algorithm: " + method, DEBUG_OPS);
		

		// first convert to grayscale
		Mat grayImage = new Mat();			
		if (image.channels() == 3) {
			Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
		} else {
			image.copyTo(grayImage);
		}		
		
		// apply binarization process		
		Mat equalizedImage = equalization(grayImage);
		
		Mat binaryImage = new Mat(equalizedImage.rows(), equalizedImage.cols(), CvType.CV_32SC1);
		
		// simple tresholding
		Imgproc.threshold(equalizedImage, binaryImage, binaryThreshold, 255, Imgproc.THRESH_BINARY);
		
		Mat cleanedBinaryImage = cleanImage(binaryImage);
				
		// noise removal
		Mat kernel = Mat.ones(5, 5, CvType.CV_32S);
		Mat noiseFreeImage = new Mat();
		Imgproc.morphologyEx(cleanedBinaryImage, noiseFreeImage, Imgproc.MORPH_OPEN, kernel);
		
		if (method.equals("Binarization")) {
			simpleBinarization(noiseFreeImage);
		} else if (method.equals("Watersheed")) {
			watersheed(noiseFreeImage, binaryImage);
		} else if (method.equals("Equalized")){
			equalizedBinarization(grayImage, binaryImage);
		} else {
			throw new RuntimeException("Invalid binarization algorithm!");
		}
		
		SegmentedImageStruct segmentedImageStruct = new SegmentedImageStruct();
		segmentedImageStruct.setImage(segmentedImage);
		segmentedImageStruct.setImageName(imageName);
		
		return segmentedImageStruct;
	}
	
	private void simpleBinarization(Mat noiseFreeImage) {
		Utils.debugNewLine("Applying Binarization!!!", DEBUG_OPS);			
		segmentedImage = noiseFreeImage;
	}
	
	private void watersheed(Mat noiseFreeImage, Mat binaryImage) {		
		Utils.debugNewLine("Applying Watersheed!", DEBUG_OPS);			
		
		Mat kernel = Mat.ones(5, 5, CvType.CV_32S);

		// sure background area (dilation shrinks the black pixels)
		Mat sureBackgroundImage = new Mat();
		Imgproc.dilate(noiseFreeImage, sureBackgroundImage, kernel);
		
		// sure foreground area (erosion shrinks the white pixels)
		Mat sureForegroundImage = new Mat();
		Imgproc.erode(noiseFreeImage, sureForegroundImage, kernel);
		
		Mat unknownImage = new Mat();
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
	
	private Mat equalization(Mat grayImage) {
		Mat equalized = grayImage.clone();//new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
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
	
	
	private void equalizedBinarization(Mat grayImage, Mat binaryImage) {
		Mat equalized = new Mat(binaryImage.rows(), binaryImage.cols(), CvType.CV_32SC1);
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
	}
	
	private byte convertIntToByte(int value) {
		return (byte) value;
	}
	
	public void setBinaryThreshold(int binaryTreshold) {
		this.binaryThreshold = binaryTreshold;
	}


	public int getBinaryThreshold() {
		return binaryThreshold;
	}
}
