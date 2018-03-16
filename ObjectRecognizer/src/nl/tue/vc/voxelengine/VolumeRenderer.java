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
import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.application.ObjectRecognizer;

public class VolumeRenderer {

	private BoxParameters volumeBoxParameters;
	private int sceneWidth;
	private int sceneHeight;
	private int sceneDepth;
	
	private CameraPosition cameraPosition;
	private int lightPositionX;
	private int lightPositionY;
	private int lightPositionZ;
	private int boxSize;
	
	private PerspectiveCamera camera;
	private PointLight light;
	private SubScene subScene;

	private Octree octree;
	
	public VolumeRenderer() {
		this.octree = null;
		configVolumeScene();
	}
	
	public VolumeRenderer(Octree octree) {
		this.octree = octree;
		configVolumeScene();
	}
	
	private void configVolumeScene() {
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		sceneWidth = appConfig.getVolumeSceneWidth();
		sceneHeight = appConfig.getVolumeSceneHeight();
		sceneDepth = appConfig.getVolumeSceneDepth();
		cameraPosition = appConfig.getCameraPosition();
		boxSize = appConfig.getVolumeBoxSize();		
		
		lightPositionX = sceneWidth/2;
		lightPositionY = sceneHeight/2;
		lightPositionZ = 300;
		camera = new PerspectiveCamera(false);
		light = new PointLight();		

		volumeBoxParameters = new BoxParameters();		
		volumeBoxParameters.setBoxSize(boxSize);
		volumeBoxParameters.setCenterX(sceneWidth/2);
		volumeBoxParameters.setCenterY(sceneHeight/2);
		volumeBoxParameters.setCenterZ(sceneDepth/2);

		// Create point light
		light.setTranslateX(lightPositionX);
		light.setTranslateY(lightPositionY);
		light.setTranslateZ(lightPositionZ);
		
		// Create camera to view the 3D shape
		camera.setTranslateX(cameraPosition.positionAxisX);
		camera.setTranslateY(cameraPosition.positionAxisY);
		camera.setTranslateZ(cameraPosition.positionAxisZ);
	}
	
	public void generateVolumeScene() {
		// add the camera and the shapes
		//System.out.println(octree.getRoot().toString());
		//volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters);
		Group root = null;
		if (octree == null) {
			root = VolumeGenerator.getDefaultVolume(volumeBoxParameters);
		} else {
			root = VolumeGenerator.generateVolume(octree, volumeBoxParameters);
		}
		
		// add the volume to render
		/**
		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAutoReverse(false);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();
		*/
		
		subScene = new SubScene(root, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
	}
	
	public SubScene getSubScene() {
		return subScene;
	}

	public CameraPosition getCameraPosition() {
		return cameraPosition;
	}
	
	public void updateCameraPosition(CameraPosition cameraPosition) {
		camera.setTranslateX(cameraPosition.positionAxisX);
		camera.setTranslateY(cameraPosition.positionAxisY);
		camera.setTranslateZ(cameraPosition.positionAxisZ);
	}
		
}
