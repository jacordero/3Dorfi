package nl.tue.vc.voxelengine;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.tue.vc.application.ObjectRecognizer;

public class VolumeRenderer {

	private BoxParameters volumeBoxParameters;
	private int sceneWidth;
	private int sceneHeight;
	private int sceneDepth;
	private int lightPositionX;
	private int lightPositionY;
	private int lightPositionZ;
	private int cameraPositionX;
	private int cameraPositionY;
	private int cameraPositionZ;

	private PerspectiveCamera camera;
	private PointLight light;
	private SubScene subScene;

	private Octree octree;
	private VolumeGenerator volumeGenerator;
	
	public VolumeRenderer(Octree octree) {
		this.octree = octree;
		
		sceneWidth = 640;
		sceneHeight = 480;
		sceneDepth = (int) (ObjectRecognizer.SCENE_DEPTH/2);
		lightPositionX = sceneWidth/2;
		lightPositionY = sceneHeight/2;
		lightPositionZ = 300;
		cameraPositionX = 320;
		cameraPositionY = 240;
		cameraPositionZ = 300;
		camera = new PerspectiveCamera(false);
		light = new PointLight();
		configScene();
	}
	
	private void configScene() {
		volumeBoxParameters = new BoxParameters();
		int boxSize = 256;
		volumeBoxParameters.setBoxSize(boxSize);
		volumeBoxParameters.setCenterX(sceneWidth);
		volumeBoxParameters.setCenterY(sceneHeight);
		volumeBoxParameters.setCenterZ(sceneDepth);

		// Create point light
		light.setTranslateX(lightPositionX);
		light.setTranslateY(lightPositionY);
		light.setTranslateX(lightPositionZ);
		
		// Create camera to view the 3D shape
		camera.setTranslateX(cameraPositionX);
		camera.setTranslateY(cameraPositionY);
		camera.setTranslateZ(cameraPositionZ);
		
		// add the camera and the shapes
		//System.out.println(octree.getRoot().toString());
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters);
		Group root = volumeGenerator.getVolume();
		
		// add the volume to render
		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(-45);
		rotation.setToAngle(45);
		rotation.setAutoReverse(true);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();
	
		subScene = new SubScene(root, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
	}
	
	public SubScene getSubScene() {
		return subScene;
	}

	/**
	public void render(Stage primaryStage) {		
		primaryStage.setScene(scene);
		// Set the Title of the Stage
		primaryStage.setTitle("An Example with Predefined 3D Shapes");
		// Display the Stage
		primaryStage.show();
	}
	**/
	
}
