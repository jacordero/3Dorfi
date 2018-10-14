package nl.tue.vc.application;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * This application opens an image stored on disk and perform the Object recognition and transformation
 * transformation and anti transformation.
 *
 */
public class ObjectReconstructor extends Application {
	// the main stage
	private Stage primaryStage;

	public static final double SCENE_DEPTH = 400;
	private AnchorPane rootGroup;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ObjectRecognizer.fxml"));
			ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
			rootGroup = (AnchorPane) loader.load();

			// set a whitesmoke background
			rootGroup.setStyle("-fx-background-color: whitesmoke;");
			
			Scene scene = new Scene(rootGroup, appConfig.getWindowWidth(), appConfig.getWindowHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created scene
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("ObjectRecognizer");
			this.primaryStage.setScene(scene);
			this.primaryStage.show();

			// init the controller
			ObjectReconstructorController controller = loader.getController();
			controller.setStage(this.primaryStage);
			controller.setRootGroup(rootGroup);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
