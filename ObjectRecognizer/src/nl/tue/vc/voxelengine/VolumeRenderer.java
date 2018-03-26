package nl.tue.vc.voxelengine;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import nl.tue.vc.application.ApplicationConfiguration;

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
	
	// Variables used to control the camera
	private PerspectiveCamera camera;    
    private Group root3D = new Group();
	
	
	private PointLight light;
	private SubScene subScene;
	private Octree octree;
	private VolumeGenerator volumeGenerator;
	
	
	public VolumeRenderer() {
		this.octree = null;
		//this.octree = new Octree(ApplicationConfiguration.getInstance().getVolumeBoxSize());
		configVolumeScene();
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters, new int[1][1], new int[1][1]);
	}
	
	public VolumeRenderer(Octree octree) {
		this.octree = octree;
		configVolumeScene();
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters, new int[1][1], new int[1][1]);
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
		lightPositionZ = 0;
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
		
		buildCamera();
	}
	
	private void buildCamera() {
		camera = new PerspectiveCamera(false);
	    camera.setTranslateX(cameraPosition.positionAxisX);
		camera.setTranslateY(cameraPosition.positionAxisY);
		camera.setTranslateZ(cameraPosition.positionAxisZ);
		
		Rotate rx = new Rotate(-30, Rotate.X_AXIS);
		Rotate ry = new Rotate(30, Rotate.Y_AXIS);
		camera.getTransforms().add(rx);
		camera.getTransforms().add(ry);
	}
	
	public void generateVolumeScene() {
		// add the camera and the shapes
		//System.out.println(octree.getRoot().toString());
		//volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters);
		if (octree == null) {
			root3D = volumeGenerator.getDefaultVolume(volumeBoxParameters);
		} else {
			root3D = volumeGenerator.generateVolume(volumeBoxParameters);
		}
		
		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root3D);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAutoReverse(false);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();
		
		
		subScene = new SubScene(root3D, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
		subScene.setFill(Color.CADETBLUE);
		//setListeners(true);
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
