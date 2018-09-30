package nl.tue.vc.silhouetteExtraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;

import nl.tue.vc.application.utils.Utils;

public class ImageLoader {

	
	
	public static Map<String, Mat> loadObjectImages(){
		// Load images 
		String OBJECT_IMAGES_DIR = "examples/blackCup242/";
		
		List<String> calibrationIndices = new ArrayList<String>();
		calibrationIndices.add("deg-0");
		calibrationIndices.add("deg-30");
		calibrationIndices.add("deg-60");
		calibrationIndices.add("deg-90");
		calibrationIndices.add("deg-120");
		calibrationIndices.add("deg-150");
		calibrationIndices.add("deg-180");
		calibrationIndices.add("deg-210");
		calibrationIndices.add("deg-240");
		calibrationIndices.add("deg-270");
		calibrationIndices.add("deg-300");
		calibrationIndices.add("deg-330");

		
		List<String> objectImageFilenames = new ArrayList<String>();
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-0.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-30.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-60.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-90.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-120.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-150.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-180.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-210.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-240.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-270.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-300.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-330.jpg");
		
		
		Map<String, Mat> objectImagesMap = new HashMap<String, Mat>();
		int calIndex = 0;
		for (String filename : objectImageFilenames) {
			Mat image = Utils.loadImage(filename);
			if (image != null) {
				String imageIdentifier = calibrationIndices.get(calIndex);
				objectImagesMap.put(imageIdentifier, image);
				calIndex++;
			}
		}

		return objectImagesMap;
	}
}
