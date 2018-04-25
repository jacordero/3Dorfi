package nl.tue.vc.voxelengine;
	
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;


public class VolumeRendererApplication extends Application {
	
	private static final int SCENE_WIDTH = 800;
	private static final int SCENE_HEIGHT = 600;
	private static final int SCENE_DEPTH = 400;
	
	@Override
	public void start(Stage primaryStage) {
		
		// configure values for the volume to render
		BoxParameters boxParameters = new BoxParameters();
		
		int boxSize = 256;
		boxParameters.setBoxSize(boxSize);
		boxParameters.setCenterX(SCENE_WIDTH/2);
		boxParameters.setCenterY(SCENE_HEIGHT/2);
		boxParameters.setCenterZ(SCENE_DEPTH/2);
		
		
		// Create a Light
		PointLight light = new PointLight();
		light.setTranslateX(SCENE_WIDTH/2 + 350);
		light.setTranslateY(SCENE_HEIGHT + 100);
		light.setTranslateX(300);
		
		// Create a Camera to view the 3D shape
		PerspectiveCamera camera = new PerspectiveCamera(false);
		camera.setTranslateX(0);
		camera.setTranslateY(0);
		camera.setTranslateZ(0);

		Octree octree = new Octree(boxSize, boxParameters);
		octree.generateOctreeFractal(0);

		// Add the shapes and the light to the group
		//octree.setBoxParameters(boxParameters);
		//VolumeGenerator volumeGenerator = new VolumeGenerator(octree, boxParameters);
		//Group root = volumeGenerator.getVolume();
		Group root = octree.getOctreeTestVolume(1);
		
	
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
	
	// add function to construct set of squares
	
	// add function to rotate the view
	
	public static void main(String[] args) {
		launch(args);
	}
}
