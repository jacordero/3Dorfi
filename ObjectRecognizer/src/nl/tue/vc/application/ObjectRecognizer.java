package nl.tue.vc.application;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Box;
import javafx.fxml.FXMLLoader;

/**
 *
 * This application opens an image stored on disk and perform the Object
 * recognition and transformation transformation and antitranformation.
 *
 */
public class ObjectRecognizer extends Application {
	// the main stage
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		try {
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ObjectRecognizer.fxml"));
			// loader.setController(new ObjectRecognizerController());
			BorderPane root = (BorderPane) loader.load();

//			Box box = new Box();
//			Setting the properties of the Box
//			box.setWidth(200.0);
//			box.setHeight(400.0);
//			box.setDepth(200.0);
//			Creating a Group object
//			Group root = new Group(box);

			// set a whitesmoke background
			root.setStyle("-fx-background-color: whitesmoke;");
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created scene
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("ObjectRecognizer");
			this.primaryStage.setScene(scene);
			this.primaryStage.show();

			// init the controller
			ObjectRecognizerController controller = loader.getController();
			controller.setStage(this.primaryStage);
			controller.init();
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
