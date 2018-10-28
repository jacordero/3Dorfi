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
import nl.tue.vc.application.utils.Utils;


public class ProjectionView extends Application {
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProjectionView.fxml"));
			BorderPane rootGroup = (BorderPane) loader.load();
						
			GridPane visualPane = new GridPane();
			
			ProjectionViewController controller = loader.getController();
			
			SubScene projectionScene = controller.renderProjection();
			projectionScene.setWidth(440);
			projectionScene.setHeight(320);
			projectionScene.setFill(Color.AZURE);
			
			SubScene volumeScene = controller.renderVolume();
			volumeScene.setWidth(440);
			volumeScene.setHeight(320);

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
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
