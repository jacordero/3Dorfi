package nl.tue.vc.application.visual;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class IntersectionTest {
	
	public static int[][] computeDistanceTransform(int[][] binaryArray) {
		DistanceTransformGenerator dtg = new DistanceTransformGenerator(binaryArray);
		return dtg.getDistanceTransform();
	}

	public static int[][] getTransformedArray(int[][] binaryArray) {
		int[][] transformedArray = new int[binaryArray.length][binaryArray[0].length];
		// populate transformedArray
		for (int x = 0; x < binaryArray.length; x++) {
			for (int y = 0; y < binaryArray[x].length; y++) {
				transformedArray[x][y] = getSquareSize(binaryArray, x, y);
			}
		}
		return transformedArray;
	}
	
	public static boolean compareDistanceTransformMethods(int[][] binaryArray){
		int[][] firstTransform = getTransformedArray(binaryArray);
		int[][] secondTransform = computeDistanceTransform(binaryArray);
		
		boolean equalContent = true;
		for (int i = 0; i < binaryArray.length; i++){
			for (int j = 0; j < binaryArray[0].length; j++){
				if (firstTransform[i][j] != secondTransform[i][j]){
					equalContent = false;
				}
			}
		}
		return equalContent;
	}

	public static int getSquareSize(int[][] binaryArray, int xValue, int yValue) {
		int sum = 0;
		int size = binaryArray[0].length - yValue;
		if (size > (xValue + 1))
			size = xValue + 1;
		for (int i = 1; i <= size; i++) {
			int validSize = checkSquareSize(i, binaryArray, xValue, yValue);
			if (validSize > 0)
				sum++;
			else
				break;
		}
		return sum;
	}

	public static int checkSquareSize(int size, int[][] binaryArray, int maxX, int minY) {
		int max = 0;
		max = maxX - size;
		int result = 0;
		int temp = 0;

		for (int x = maxX; x > max; x--) {
			for (int y = minY; y < (minY + size); y++) {
				int pixel = binaryArray[x][y];
				if (pixel == 0) {
					result -= temp;
					return result;
				} else {
					result++;
					temp++;
				}
			}
		}
		return result;
	}

	public static Raster loadImageRaster(String file_path) throws IOException {
		File input = new File(file_path);
		BufferedImage buf_image = ImageIO.read(input);
		buf_image = binarizeImage(buf_image);
		return buf_image.getData(); // return raster
	}

	public static BufferedImage binarizeImage(BufferedImage img_param) {
		// to binary
		BufferedImage image = new BufferedImage(img_param.getWidth(), img_param.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY);
		Graphics g = image.getGraphics();
		g.drawImage(img_param, 0, 0, null);
		g.dispose();
		return image;
	}

	public static int[][] getBinaryArray(BufferedImage img_param) {
		// to binary
		Raster raster = IntersectionTest.binarizeImage(img_param).getData();
		int[][] result = new int[raster.getHeight()][raster.getWidth()];
		for (int x = 0; x < raster.getHeight(); x++) {
			for (int y = 0; y < raster.getWidth(); y++) {
				int binaryValue = (int)raster.getSampleDouble(y, x, 0);
				result[x][y] = (binaryValue == 0) ? 1 : 0;
			}
		}
		return result;
	}
	/***
	 * 
	 * @param sourceArray
	 * @return An array containing the opposite binary values
	 */
	public static int[][] getInvertedArray(int[][] sourceArray) {
		int rowCount = sourceArray.length;
		int colCount = sourceArray[0].length;
		int[][] resultArray = new int[rowCount][colCount];
		for (int x = 0; x < rowCount; x++) {
			for (int y = 0; y < colCount; y++) {
				resultArray[x][y] = 1 - sourceArray[x][y];
			}
		}
		return resultArray;
	}

	public static BufferedImage Mat2BufferedImage(Mat matrix) throws Exception {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		byte ba[] = mob.toArray();

		BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
		return bi;
	}

}
