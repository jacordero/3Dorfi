package nl.tue.vc.model.test;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.model.BoxParametersTest;
import nl.tue.vc.voxelengine.BoxParameters;
import nl.tue.vc.voxelengine.CameraPosition;
import nl.tue.vc.voxelengine.Octree;

public class VolumeRendererTest {

	private BoxParametersTest volumeBoxParameters;
	private int sceneWidth;
	private int sceneHeight;
	private int sceneDepth;
	
	private CameraPosition cameraPosition;
	private int lightPositionX;
	private int lightPositionY;
	private int lightPositionZ;
	private float boxSizeX;
	private float boxSizeY;
	private float boxSizeZ;
	private float boxSizeFactor;
	
	// Variables used to control the camera
	private PerspectiveCamera camera;    
    private Group root3D = new Group();
	
	private PointLight light;
	private SubScene subScene;
	private VolumeGeneratorTest volumeGenerator;
	
	
	public VolumeRendererTest() {
		boxSizeFactor = 20;
		//this.octree = new Octree(ApplicationConfiguration.getInstance().getVolumeBoxSize());
		configVolumeScene();
		//volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters);
	}
		
	private void configVolumeScene() {
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		sceneWidth = appConfig.getVolumeSceneWidth();
		sceneHeight = appConfig.getVolumeSceneHeight();
		sceneDepth = appConfig.getVolumeSceneDepth();
		cameraPosition = appConfig.getCameraPosition();
		boxSizeX = boxSizeFactor * 12;
		boxSizeY = boxSizeFactor * 6;
		boxSizeZ = boxSizeFactor * 12;
		
		lightPositionX = sceneWidth/2;
		lightPositionY = sceneHeight/2;
		lightPositionZ = 0;
		light = new PointLight();		

		volumeBoxParameters = new BoxParametersTest();		
		volumeBoxParameters.setSizeX(boxSizeX);
		volumeBoxParameters.setSizeY(boxSizeY);
		volumeBoxParameters.setSizeZ(boxSizeZ);		
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
//		camera.getTransforms().add(rx);
//		camera.getTransforms().add(ry);
	}
	
	public void generateVolumeScene() {
		Utils.debugNewLine("generateVolumeScene", true);
		// add the camera and the shapes
		//System.out.println(octree.getRoot().toString());
		//volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters);
		
		
		root3D = volumeGenerator.generateVolume();
		
		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root3D);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAutoReverse(false);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();
		
		
		subScene = new SubScene(root3D, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
		//subScene.setFill(Color.CADETBLUE);
		subScene.setFill(Color.WHITE);
		//setListeners(true);
	}
	
	public void generateVolumeScene(Group root3D) {
		Utils.debugNewLine("generateVolumeScene", true);

		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root3D);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAutoReverse(false);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();
		
		
		subScene = new SubScene(root3D, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
		//subScene.setFill(Color.CADETBLUE);
		subScene.setFill(Color.WHITE);
		//setListeners(true);
	}
	
	public void generateVolumeScene(List<Box> volumeVoxels){
		Utils.debugNewLine("GenerateVolumeScene from voxels", true);

		
		Group volumeGroup = new Group();
		volumeGroup.getChildren().addAll(volumeVoxels);

		RotateTransition rotation = new RotateTransition(Duration.seconds(20), volumeGroup);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAutoReverse(false);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();

		
		subScene = new SubScene(volumeGroup, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
		//subScene.setFill(Color.CADETBLUE);
		subScene.setFill(Color.WHITE);
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

	public BoxParametersTest getVolumeBoxParameters() {
		return volumeBoxParameters;
	}

	public void setVolumeBoxParametersTest(BoxParametersTest volumeBoxParameters) {
		this.volumeBoxParameters = volumeBoxParameters;
	}
		
}
