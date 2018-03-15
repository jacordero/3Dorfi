package nl.tue.vc.application;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nl.tue.vc.voxelengine.Octree;
import nl.tue.vc.voxelengine.VolumeRenderer;

/**
 *
 * This application opens an image stored on disk and perform the Object recognition and transformation
 * transformation and anti transformation.
 *
 */
public class ObjectRecognizer extends Application
{
	// the main stage
	private Stage primaryStage;

	public static final double SCENE_WIDTH = 800;
	public static final double SCENE_HEIGHT = 600;
	public static final double SCENE_DEPTH = 400;
	//private Scene volumeScene;
	//private BorderPane rootGroup;
	
	
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ObjectRecognizer.fxml"));
			//loader.setController(new ObjectRecognizerController());
			int boxSize = 256;
			Octree octree = new Octree(boxSize);
			octree.generateOctreeFractal(boxSize, 2);
			VolumeRenderer volumeRenderer = new VolumeRenderer(octree);
			
			
			BorderPane rootGroup = (BorderPane) loader.load();
			
			//root.setCen
			// set a whitesmoke background
			rootGroup.setStyle("-fx-background-color: whitesmoke;");
			rootGroup.setCenter(volumeRenderer.getSubScene());
			
			Scene scene = new Scene(rootGroup, SCENE_WIDTH, SCENE_HEIGHT);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created scene
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("ObjectRecognizer");
			this.primaryStage.setScene(scene);
			this.primaryStage.show();

			// init the controller
			ObjectRecognizerController controller = loader.getController();
			controller.setStage(this.primaryStage);
			//controller.setBorderPane(this.)
			controller.init();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		launch(args);
	}
	
	/**
	public void updateCentralView(Group volumeGroup) {
		rootGroup.setCenter(volumeGroup);
		// maybe refresh the scene?
	}
	**/
}
