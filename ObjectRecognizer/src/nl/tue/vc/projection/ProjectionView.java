package nl.tue.vc.projection;
	
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class ProjectionView extends Application {
	
	private static final int SCENE_WIDTH = 640;
	private static final int SCENE_HEIGHT = 480;
	
	//@FXML
	//GridPane visualPane;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProjectionView.fxml"));
			BorderPane rootGroup = (BorderPane) loader.load();
						
			GridPane visualPane = new GridPane();//(GridPane) rootGroup.getCenter();
			//centerPane.setMinSize(SCENE_WIDTH, SCENE_HEIGHT - 100);
			//visualPane.setMinWidth(545);
			//visualPane.setMinHeight(380);
			
			System.out.println(visualPane.getHeight());
			System.out.println(visualPane.getWidth());
			
			ProjectionViewController controller = loader.getController();
			
			SubScene projectionScene = controller.renderProjection();
			projectionScene.setWidth(440);
			projectionScene.setHeight(320);
			projectionScene.setFill(Color.AZURE);
			
			SubScene volumeScene = controller.renderVolume();
			volumeScene.setWidth(440);
			volumeScene.setHeight(320);
			//volumeScene.setFill(Color.BLUE);

			visualPane.add(projectionScene, 0, 0);			
			visualPane.add(volumeScene, 1, 0);
			
			rootGroup.setCenter(visualPane);

			Scene scene = new Scene(rootGroup, 960, 640);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());			
			primaryStage.setScene(scene);

			primaryStage.show();

			controller.setPrimaryStage(primaryStage);
			controller.setRootGroup(rootGroup);
			controller.setVisualPane(visualPane);
			controller.setProjectionScene(projectionScene);
			controller.setVolumeScene(volumeScene);
//			controller.setVolumeGroup(volumeGroup);
//			controller.setProjectionGroup(projectionGroup);
//			controller.render();
			//controller.init();
//			ObjectRecognizerController controller = loader.getController();
//			controller.setStage(this.primaryStage);
//			controller.setRootGroup(rootGroup);
//			controller.setVolumeRenderer(volumeRenderer);
//			controller.init();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
