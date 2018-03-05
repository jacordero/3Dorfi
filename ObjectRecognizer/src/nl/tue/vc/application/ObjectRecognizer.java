package nl.tue.vc.application;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SubScene;
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
	BorderPane rootGroup;
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ObjectRecognizer.fxml"));
			ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
			VolumeRenderer volumeRenderer = new VolumeRenderer();
			volumeRenderer.generateVolumeScene();
			volumeScene = volumeRenderer.getSubScene();			
			rootGroup = (BorderPane) loader.load();
			// set a whitesmoke background
			rootGroup.setStyle("-fx-background-color: whitesmoke;");
			rootGroup.setCenter(volumeScene);
			
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
			controller.setVolumeRenderer(volumeRenderer);
			controller.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		String libPath = System.getProperty("java.library.path");
		System.out.println("Library path; " + libPath);
		System.out.println("Library; " + Core.NATIVE_LIBRARY_NAME);
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
