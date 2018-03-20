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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		try {
//			Raster raster = loadImageRaster("C:\\Tools\\eclipse\\workspace\\objectrecognizer\\ObjectRecognizer\\images\\football.jpg");
//			for(int x = 0; x<raster.getWidth(); x++) {
//				for(int y = 0; y<raster.getHeight(); y++) {
//					System.out.print(raster.getSample(x, y, 0)+" ");
//				}
//				System.out.println("");
//			}
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
		int[][] binaryArray = new int[8][8];
		int[][] transformedArray = new int[8][8];
		
		for(int x=0;x<8;x++) {
			for(int y=0;y<8;y++) {
				binaryArray[x][y] = 0;
			}
		}
		
		for(int x=1;x<6;x++) {
			for(int y=1;y<6;y++) {
				binaryArray[x][y] = 1;
			}
		}
		
		binaryArray[0][2] = 1;
		binaryArray[0][3] = 1;
		binaryArray[0][4] = 1;
		binaryArray[6][2] = 1;
		binaryArray[6][3] = 1;
		binaryArray[6][4] = 1;
		
		for(int x=0;x<8;x++) {
			for(int y=0;y<8;y++) {
				System.out.print(binaryArray[x][y] + " ");
			}
			System.out.println("");
		}
		
		for(int x=0;x<8;x++) {
			for(int y=0;y<8;y++) {
				transformedArray[x][y] = getSquareSize(binaryArray, x, y);
			}
		}
		
		for(int x=0;x<8;x++) {
			for(int y=0;y<8;y++) {
				System.out.print(transformedArray[x][y] + " ");
			}
			System.out.println("");
		}
	}
	
	public static int getSquareSize(int[][] binaryArray, int xValue, int yValue)
	{
		int sum = 0;
		int size = binaryArray[0].length;
		if(size>binaryArray.length)
			size=binaryArray.length;
		
		for(int i=1;i<size;i++) {
			Boolean sizeIsValid = checkSquareSize(i, binaryArray, xValue, yValue);
			System.out.println("Result for i="+i+" for ("+xValue+","+yValue+") = " + sizeIsValid);
			if(sizeIsValid) 
				sum++;
			else 
				break;
		}
		return sum;
	}

	public static Boolean checkSquareSize(int size, int[][] binaryArray, int maxX, int minY)
	{
		int max = maxX>size?(maxX-size):0;
		for(int x=maxX;x>max;x--) {
			for(int y=minY;y<(minY+size);y++) {
				System.out.println("x="+x+", y="+"y");
				int pixel = binaryArray[x][y];
				System.out.println("("+x+","+y+") = " + pixel);
				if(pixel==0)
					return false;
			}
		}
		return true;
	}
	
	public static Raster loadImageRaster(String file_path) throws IOException
	{
	    File input = new File(file_path);
	    BufferedImage buf_image = ImageIO.read(input);
	    buf_image = binarizeImage(buf_image);
	    return buf_image.getData(); //return raster
	}
	
	public static BufferedImage binarizeImage(BufferedImage img_param)
	{
	    //to binary
	    BufferedImage image = new BufferedImage(img_param.getWidth(), img_param.getHeight(),
	    BufferedImage.TYPE_BYTE_BINARY);
	    Graphics g = image.getGraphics();
	    g.drawImage(img_param, 0, 0, null);
	    g.dispose();
	    return image;
	}
	
	public static BufferedImage Mat2BufferedImage(Mat matrix)throws Exception {        
	    MatOfByte mob=new MatOfByte();
	    Imgcodecs.imencode(".jpg", matrix, mob);
	    byte ba[]=mob.toArray();

	    BufferedImage bi=ImageIO.read(new ByteArrayInputStream(ba));
	    return bi;
	}

}
