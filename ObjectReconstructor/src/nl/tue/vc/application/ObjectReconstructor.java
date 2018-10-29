package nl.tue.vc.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ObjectReconstructor extends Application {
	// the main stage
	private Stage primaryStage;

	private AnchorPane rootGroup;
	
	private static final Logger logger = Logger.getLogger(ObjectReconstructor.class.getName());

	
	@Override
	public void start(Stage primaryStage) {
		try {
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ObjectRecognizer.fxml"));
			ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
			rootGroup = (AnchorPane) loader.load();
			rootGroup.setStyle("-fx-background-color: whitesmoke;");			
			Scene scene = new Scene(rootGroup, appConfig.getWindowWidth(), appConfig.getWindowHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			// create the stage with the given title and the previously created scene
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle(appConfig.getTitle());
			this.primaryStage.setScene(scene);
			this.primaryStage.show();

			// init the controller
			ObjectReconstructorController controller = loader.getController();
			controller.setStage(this.primaryStage);
			controller.setRootGroup(rootGroup);
			
			// Stop the application when the window is closed
			this.primaryStage.setOnCloseRequest(e -> {
				Platform.exit();
				System.exit(0);
			});
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot start application\n: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
