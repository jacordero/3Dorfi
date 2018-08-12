package nl.tue.vc.application;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nl.tue.vc.voxelengine.CameraPosition;
import nl.tue.vc.voxelengine.VolumeRenderer;

/**
 *
 * This application opens an image stored on disk and perform the Object recognition and transformation
 * transformation and anti transformation.
 *
 */
public class ObjectRecognizer extends Application {
	// the main stage
	private Stage primaryStage;

	public static final double SCENE_DEPTH = 400;
	//private Scene volumeScene;
	//private BorderPane rootGroup;
	private SubScene volumeScene;
	private AnchorPane rootGroup;
	private TabPane tabPane;
	private BorderPane displayBorderPane;
	private Tab mainTab;
	private AnchorPane mainTabAnchor;
	private BorderPane renderingDisplayBorderPane;
	private Tab renderingTab;
	private AnchorPane renderingTabAnchor;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ObjectRecognizer.fxml"));
			ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
			//VolumeRenderer volumeRenderer = new VolumeRenderer();
			//volumeRenderer.generateVolumeScene();
			//volumeScene = volumeRenderer.getSubScene();			
			rootGroup = (AnchorPane) loader.load();

			// set a whitesmoke background
			rootGroup.setStyle("-fx-background-color: whitesmoke;");
			//rootGroup.setCenter(volumeScene);
			//setVolumeSubScene(volumeScene);
			
			Scene scene = new Scene(rootGroup, appConfig.getWindowWidth(), appConfig.getWindowHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created scene
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("ObjectRecognizer");
			this.primaryStage.setScene(scene);
			this.primaryStage.show();

			// init the controller
			ObjectRecognizerController controller = loader.getController();
			controller.setStage(this.primaryStage);
			controller.setRootGroup(rootGroup);
			//controller.setVolumeRenderer(volumeRenderer);
			controller.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setVolumeSubScene(SubScene volumeSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		mainTab = tabPane.getTabs().get(0);
		mainTabAnchor = (AnchorPane) mainTab.getContent();
		displayBorderPane = (BorderPane) mainTabAnchor.getChildren().get(0);
		displayBorderPane.setCenter(volumeSubScene);
	}
	
	public void setProjectionSubScene(SubScene projectionSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		renderingTab = tabPane.getTabs().get(0);
		renderingTabAnchor = (AnchorPane) renderingTab.getContent();
		renderingDisplayBorderPane = (BorderPane) renderingTabAnchor.getChildren().get(0);
		renderingDisplayBorderPane.setCenter(projectionSubScene);
	}

	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
