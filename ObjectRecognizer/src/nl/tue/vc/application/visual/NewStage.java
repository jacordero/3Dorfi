package nl.tue.vc.application.visual;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;

import org.opencv.core.Mat;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.tue.vc.voxelengine.BoxParameters;
import nl.tue.vc.voxelengine.Octree;
import nl.tue.vc.voxelengine.VolumeGenerator;

public class NewStage {
	private static final int SCENE_WIDTH = 800;
	private static final int SCENE_HEIGHT = 600;
	private static final int SCENE_DEPTH = 400;

	public NewStage(Mat processedImage) {
		System.out.println("NewStage called");
		try {
			
			BufferedImage convertedMat = IntersectionTest.Mat2BufferedImage(processedImage);
			
			//Raster raster = IntersectionTest.loadImageRaster("C:\\Tools\\eclipse\\workspace\\objectrecognizer\\ObjectRecognizer\\images\\football.jpg");
			Raster raster = IntersectionTest.binarizeImage(convertedMat).getData();
			for(int x = 0; x<raster.getWidth(); x++) {
				for(int y = 0; y<raster.getHeight(); y++) {
					System.out.println("pixel("+x+", "+y+") = " + raster.getSampleDouble(x, y, 0));
				}
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Stage primaryStage = new Stage();

		// configure values for the volume to render
		BoxParameters boxParameters = new BoxParameters();

		int boxSize = 256;
		boxParameters.setBoxSize(boxSize);
		boxParameters.setCenterX(SCENE_WIDTH / 2);
		boxParameters.setCenterY(SCENE_HEIGHT / 2);
		boxParameters.setCenterZ(SCENE_DEPTH / 2);

		Octree octree = new Octree(boxSize);
		octree.generateOctreeFractal(boxSize, 2);
		System.out.println(octree.getRoot().toString());
		VolumeGenerator volGenerator = new VolumeGenerator(octree, boxParameters);

		Group root = volGenerator.getVolume();
		// FlowPane root = new FlowPane();
		// root.setAlignment(Pos.CENTER);
		// root.getChildren().add(new Button("New Stage"));

		// Create a Light
		PointLight light = new PointLight();
		light.setTranslateX(SCENE_WIDTH / 2 + 350);
		light.setTranslateY(SCENE_HEIGHT + 100);
		light.setTranslateX(300);

		// Create a Camera to view the 3D shape
		PerspectiveCamera camera = new PerspectiveCamera(false);
		camera.setTranslateX(100);
		camera.setTranslateY(-50);
		camera.setTranslateZ(300);

		// Add the shapes and the light to the group

		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(-45);
		rotation.setToAngle(45);
		rotation.setAutoReverse(true);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();

		// Create a Scene with depth buffer enabled
		// width and height
		Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, true);
		// Add the Camera to the scene
		scene.setCamera(camera);
		// Add the Scene to the Stage
		primaryStage.setScene(scene);
		// Set the Title of the Stage
		primaryStage.setTitle("An Example with Predefined 3D Shapes");
		// Display the Stage
		primaryStage.show();
	}
}
