package nl.tue.vc.projection;


import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ProjectionViewController {

	@FXML
	private Stage primaryStage;
	
	@FXML
	private Slider fieldOfViewSlider;
	
	private double sceneWidth;
	private double sceneHeight;
	private Group root3D;
	private Box volume;
	private SubScene volumeScene;
	private PerspectiveCamera camera;
	
	private Group root2D;
	private SubScene projectionScene;
	private GridPane visualPane;
	
	private BorderPane rootGroup;
	private Group volumeGroup;
	private Group projectionGroup;
	
	private VolumeModel volumeModel;
	private double scalingParameter;
	
	private int fieldOfView;
	
	private TransformMatrices transformMatrices;
	
	
	public ProjectionViewController() {
		this.sceneWidth = 440;//650.5;//440;
		this.sceneHeight = 320;//328.0;//320;
		volumeModel = new VolumeModel();
		scalingParameter = 10;
		transformMatrices = new TransformMatrices(sceneWidth, sceneHeight, 32.3);
	}
	
	@FXML
	public void initialize() {
		//fieldOfView = (int) fieldOfViewSlider.getValue();
		
		fieldOfViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Field of view changed (newValue: " +  newValue.intValue() + ")");
			fieldOfView = newValue.intValue();
		});


		fieldOfViewSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging, Boolean isNowChanging) {
				if (!isNowChanging) {
					System.out.println("It stopped changing");
					transformMatrices.updateFieldOfView(fieldOfViewSlider.getValue());
					updateViews();
				}
			}
		});
	}
	
	protected void init() {
		
	}
	
	public void updateViews() {
		SubScene projectionScene =renderProjection();
		projectionScene.setWidth(440);
		projectionScene.setHeight(320);
		projectionScene.setFill(Color.AZURE);
		
		SubScene volumeScene = renderVolume();
		volumeScene.setWidth(440);
		volumeScene.setHeight(320);
		//volumeScene.setFill(Color.BLUE);

		visualPane.add(projectionScene, 0, 0);			
		visualPane.add(volumeScene, 1, 0);
		rootGroup.setCenter(visualPane);
		//Scene scene = new Scene(rootGroup, 960, 640);
		//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());			
		//primaryStage.setScene(scene);
		//primaryStage.show();
	}
	
	
	public void render() {
		renderVolume();
		renderProjection();
	}
	
	public SubScene renderVolume() {
		createVolume();
		buildCamera();
		root3D = new Group();
		root3D.getChildren().add(volume);
		
		// Where should I add the lights?
//		light = new PointLight();
//		light.setTranslateX(offsetX);
//		light.setTranslateY(offsetY);
//		light.setTranslateZ(0);
		
		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root3D);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setAutoReverse(true);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();

		volumeScene = new SubScene(root3D, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		volumeScene.setCamera(camera);
		volumeScene.setFill(Color.BEIGE);
		return volumeScene;
	}
	
	public void createVolume() {
		volume = new Box(volumeModel.xLength * scalingParameter, 
				volumeModel.yLength * scalingParameter,
				volumeModel.zLength * scalingParameter);
		
		volume.setTranslateX(sceneWidth/2);
		volume.setTranslateY(sceneHeight/2);
		//volume.setTranslateZ(volumePositionZ);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(Color.BLUE);
		volume.setMaterial(textureMaterial);
	}
	
	public void buildCamera() {
		camera = new PerspectiveCamera(false);
		camera.setTranslateX(140);
		camera.setTranslateY(-100);
		camera.setTranslateZ(-40);
	}
	
	
	public SubScene renderProjection() {
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		
		double leftMostPos = transformMatrices.screenWidth;
		double rightMostPos = 0;
		double topMostPos = transformMatrices.screenHeight;
		double bottomMostPos = 0;
		
		for (Vector3D vector: volumeModel.modelVertices) {
			//System.out.println("\nVector in world coordinates");
			//System.out.println(vector);
			
			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);
			//System.out.println("\nVector in view coordinates");
			//System.out.println(viewVector);
			
			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);
			//System.out.println("\nVector in clip coordinates");
			//System.out.println(clipVector);
			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getY()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
				System.out.println("We should ignore: " + clipVector.toString());
			}
			
			Vector3D ndcVector = transformMatrices.toNDCCoordinates(clipVector);
			//System.out.println("\nVector in ndc coordinates");
			//System.out.println(ndcVector);
			
			Vector3D windowVector = transformMatrices.toWindowCoordinates(ndcVector);
			//System.out.println("\nVector in window coordinates");
			//System.out.println(windowVector);
			projectedPoints.add(windowVector);
			
			if (windowVector.getX() > rightMostPos) {
				rightMostPos = windowVector.getX();
			} else if (windowVector.getX() < leftMostPos) {
				leftMostPos = windowVector.getX();
			}
			
			if (windowVector.getY() > bottomMostPos) {
				bottomMostPos = windowVector.getY();
			} else if (windowVector.getY() < topMostPos) {
				topMostPos = windowVector.getY();
			}
		}
		
		

		root2D = new Group();
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		boundingBox.setFill(Color.CHARTREUSE);
		boundingBox.setStroke(Color.BLACK);
		root2D.getChildren().add(boundingBox);
		
		for (Vector3D point: projectedPoints) {
			Ellipse circle = new Ellipse(point.getX(), point.getY(), 2, 2);
			circle.setFill(Color.BLACK);
			root2D.getChildren().add(circle);
		}
		
		// draw the lines
		Line line1 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(7).getX(), projectedPoints.get(7).getY());
		line1.getStrokeDashArray().addAll(2d);
		line1.setFill(Color.BLUE);
		root2D.getChildren().add(line1);
		
		Line line2 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(5).getX(), projectedPoints.get(5).getY());
		line2.getStrokeDashArray().addAll(2d);
		line2.setFill(Color.BLUE);
		root2D.getChildren().add(line2);

		Line line3 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(0).getX(), projectedPoints.get(0).getY());
		line3.getStrokeDashArray().addAll(2d);
		line3.setFill(Color.BLUE);
		root2D.getChildren().add(line3);
		
		Line line4 = new Line(projectedPoints.get(7).getX(), projectedPoints.get(7).getY(),
				projectedPoints.get(3).getX(), projectedPoints.get(3).getY());
		line4.getStrokeDashArray().addAll(2d);
		line4.setFill(Color.BLUE);
		root2D.getChildren().add(line4);
		
		Line line5 = new Line(projectedPoints.get(7).getX(), projectedPoints.get(7).getY(),
				projectedPoints.get(6).getX(), projectedPoints.get(6).getY());
		line5.getStrokeDashArray().addAll(2d);
		line5.setFill(Color.BLUE);
		root2D.getChildren().add(line5);

		Line line6 = new Line(projectedPoints.get(5).getX(), projectedPoints.get(5).getY(),
				projectedPoints.get(6).getX(), projectedPoints.get(6).getY());
		line6.getStrokeDashArray().addAll(2d);
		line6.setFill(Color.BLUE);
		root2D.getChildren().add(line6);

		Line line7 = new Line(projectedPoints.get(5).getX(), projectedPoints.get(5).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line7.getStrokeDashArray().addAll(2d);
		line7.setFill(Color.BLUE);
		root2D.getChildren().add(line7);
		
		Line line8 = new Line(projectedPoints.get(6).getX(), projectedPoints.get(6).getY(),
				projectedPoints.get(2).getX(), projectedPoints.get(2).getY());
		line8.getStrokeDashArray().addAll(2d);
		line8.setFill(Color.BLUE);
		root2D.getChildren().add(line8);
		
		Line line9 = new Line(projectedPoints.get(0).getX(), projectedPoints.get(0).getY(),
				projectedPoints.get(3).getX(), projectedPoints.get(3).getY());
		line9.getStrokeDashArray().addAll(2d);
		line9.setFill(Color.BLUE);
		root2D.getChildren().add(line9);
		
		Line line10 = new Line(projectedPoints.get(0).getX(), projectedPoints.get(0).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line10.getStrokeDashArray().addAll(2d);
		line10.setFill(Color.BLUE);
		root2D.getChildren().add(line10);

		Line line11 = new Line(projectedPoints.get(3).getX(), projectedPoints.get(3).getY(),
				projectedPoints.get(2).getX(), projectedPoints.get(2).getY());
		line11.getStrokeDashArray().addAll(2d);
		line11.setFill(Color.BLUE);
		root2D.getChildren().add(line11);

		Line line12 = new Line(projectedPoints.get(2).getX(), projectedPoints.get(2).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line12.getStrokeDashArray().addAll(2d);
		line12.setFill(Color.BLUE);
		root2D.getChildren().add(line12);

		projectionScene = new SubScene(root2D, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		return projectionScene;
	}
	
	public void setProjectionScene(SubScene projectionScene) {
		this.projectionScene = projectionScene;
	}
	
	public void setVolumeScene(SubScene volumeScene) {
		this.volumeScene = volumeScene;
	}
	
	public void setRootGroup(BorderPane rootGroup) {
		this.rootGroup = rootGroup;
	}
	
	public void setVisualPane(GridPane visualPane) {
		this.visualPane = visualPane;
	}
	
	public void setVolumeGroup(Group volumeGroup) {
		this.volumeGroup = volumeGroup;
	}
	
	public void setProjectionGroup(Group projectionGroup) {
		this.projectionGroup = projectionGroup;
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
}
