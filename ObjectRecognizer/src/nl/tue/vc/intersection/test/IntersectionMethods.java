package nl.tue.vc.intersection.test;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import nl.tue.vc.application.visual.IntersectionTest;

public class IntersectionMethods {

	
	
	public static void main(String[] args) throws IOException{

		List<String> imagePaths = loadImagePaths();
		boolean comparisonResult;
		for (String path: imagePaths){
			File input = new File(path);
			BufferedImage buf_image = ImageIO.read(input);
			int[][] binaryImage = IntersectionTest.getBinaryArray(buf_image);
			System.out.println("**** Comparison being done in image: " + path);
			IntersectionTest.compareDistanceTransformMethods(binaryImage);
			System.out.println("\n");
			
		}
		
	}
	
	private static List<String> loadImagePaths(){
		List<String> imagePaths = new ArrayList<String>();
		imagePaths.add("images/cube/cube_0.png");
		imagePaths.add("images/cube/cube_20.png");
		imagePaths.add("images/cube/cube_40.png");
		imagePaths.add("images/cube/cube_60.png");
		imagePaths.add("images/cube/cube_80.png");
		imagePaths.add("images/cube/cube_100.png");
		imagePaths.add("images/cube/cube_120.png");
		imagePaths.add("images/cube/cube_140.png");
		imagePaths.add("images/cube/cube_160.png");
		imagePaths.add("images/cube/cube_180.png");
		imagePaths.add("images/piramid/piramid_0.png");
		imagePaths.add("images/piramid/piramid_20.png");
		imagePaths.add("images/piramid/piramid_40.png");
		imagePaths.add("images/piramid/piramid_60.png");
		imagePaths.add("images/piramid/piramid_80.png");
		imagePaths.add("images/piramid/piramid_100.png");
		imagePaths.add("images/piramid/piramid_120.png");
		imagePaths.add("images/piramid/piramid_140.png");
		imagePaths.add("images/piramid/piramid_160.png");
		imagePaths.add("images/piramid/piramid_180.png");
		
		return imagePaths;
		
	}
	
}
