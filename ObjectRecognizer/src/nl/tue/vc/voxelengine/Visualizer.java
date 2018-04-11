package nl.tue.vc.voxelengine;
	
import java.io.File;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.geometry.Point3D;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.tue.vc.application.ApplicationConfiguration;


public class Visualizer extends Application {
	
	private static final int SCENE_WIDTH = 800;
	private static final int SCENE_HEIGHT = 600;
	private static final int SCENE_DEPTH = 400;
	
	@Override
	public void start(Stage primaryStage) {
		
		// configure values for the volume to render
		BoxParameters boxParameters = new BoxParameters();
		
		int boxSize = 100;
		boxParameters.setBoxSize(boxSize);
		boxParameters.setCenterX(SCENE_WIDTH/2);
		boxParameters.setCenterY(SCENE_HEIGHT/2);
		boxParameters.setCenterZ(SCENE_DEPTH/2);
		
		
		// Create a Light
//		PointLight light = new PointLight();
//		light.setTranslateX(SCENE_WIDTH/2 + 350);
//		light.setTranslateY(SCENE_HEIGHT + 100);
//		light.setTranslateX(300);
		
		// Create a Camera to view the 3D shape
		PerspectiveCamera camera = new PerspectiveCamera(false);
		camera.setFieldOfView(45);
		camera.setTranslateX(0);
		camera.setTranslateY(0);
		camera.setTranslateZ(0);

		Group root = getDefaultVolume(boxParameters);
		
	
		RotateTransition rotation = new RotateTransition(Duration.seconds(20), root);
		rotation.setCycleCount(Animation.INDEFINITE);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		//rotation.setAutoReverse(true);
		rotation.setAxis(Rotate.Y_AXIS);
		rotation.play();
		
		
		// Create a Scene with depth buffer enabled
		// width and height
		Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, true);
		// Add the Camera to the scene
		scene.setCamera(camera);
		// Add the Scene to the Stage
		primaryStage.setScene(scene);
		// Set the Title of the Stage
		primaryStage.setTitle("An Example with Predefined 3D Shapes");
		// Display the Stage
		primaryStage.show();
		
	}
	
	// add function to construct set of squares
	
	// add function to rotate the view
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public Group getDefaultVolume(BoxParameters boxParameters) {
		Group volume = new Group();
		Box imagePlane = generateVoxel(400,300,1,300,300,0, Color.AQUA);
		//volume.getChildren().addAll(imagePlane);
		//boxParameters.setCenterZ(-1*boxParameters.getCenterZ());
		Box box = generateVoxel(boxParameters.getCenterX(),boxParameters.getCenterY(),boxParameters.getCenterZ(),boxParameters.getBoxSize(),boxParameters.getBoxSize(),boxParameters.getBoxSize(), Color.BLUE);
		volume.getChildren().addAll(box);
		
		int centerX = boxParameters.getCenterX();
		int centerY = boxParameters.getCenterY();
		int centerZ = boxParameters.getCenterZ();
		int halfSize = boxParameters.getBoxSize()/2;
		
		Point3D boxCorner1 = new Point3D(centerX - halfSize, centerY + halfSize, centerZ + halfSize);
		Point3D boxCorner2 = new Point3D(centerX + halfSize, centerY + halfSize, centerZ + halfSize);
		Point3D boxCorner3 = new Point3D(centerX - halfSize, centerY - halfSize, centerZ + halfSize);
		Point3D boxCorner4 = new Point3D(centerX + halfSize, centerY - halfSize, centerZ + halfSize);
		Point3D boxCorner5 = new Point3D(centerX - halfSize, centerY + halfSize, centerZ - halfSize);
		Point3D boxCorner6 = new Point3D(centerX + halfSize, centerY + halfSize, centerZ - halfSize);
		Point3D boxCorner7 = new Point3D(centerX - halfSize, centerY - halfSize, centerZ - halfSize);
		Point3D boxCorner8 = new Point3D(centerX + halfSize, centerY - halfSize, centerZ - halfSize);
		System.out.println("boxCorner1: [" + boxCorner1.getX() + ", " + boxCorner1.getY()
		+ ", " + boxCorner1.getZ() +"]");
		System.out.println("boxCorner2: [" + boxCorner2.getX() + ", " + boxCorner2.getY()
		+ ", " + boxCorner2.getZ() +"]");
		System.out.println("boxCorner3: [" + boxCorner3.getX() + ", " + boxCorner3.getY()
		+ ", " + boxCorner3.getZ() +"]");
		System.out.println("boxCorner4: [" + boxCorner4.getX() + ", " + boxCorner4.getY()
		+ ", " + boxCorner4.getZ() +"]");
		System.out.println("boxCorner5: [" + boxCorner5.getX() + ", " + boxCorner5.getY()
		+ ", " + boxCorner5.getZ() +"]");
		System.out.println("boxCorner6: [" + boxCorner6.getX() + ", " + boxCorner6.getY()
		+ ", " + boxCorner6.getZ() +"]");
		System.out.println("boxCorner7: [" + boxCorner7.getX() + ", " + boxCorner7.getY()
		+ ", " + boxCorner7.getZ() +"]");
		System.out.println("boxCorner8: [" + boxCorner8.getX() + ", " + boxCorner8.getY()
		+ ", " + boxCorner8.getZ() +"]");
				
		Sphere pointCorner1 = getPoint(centerX - halfSize, centerY + halfSize, centerZ + halfSize, Color.RED);
		Sphere pointCorner2 = getPoint(centerX + halfSize, centerY + halfSize, centerZ + halfSize, Color.GREEN);
		Sphere pointCorner3 = getPoint(centerX - halfSize, centerY - halfSize, centerZ + halfSize, Color.YELLOW);
		Sphere pointCorner4 = getPoint(centerX + halfSize, centerY - halfSize, centerZ + halfSize, Color.BLUE);
		Sphere pointCorner5 = getPoint(centerX - halfSize, centerY + halfSize, centerZ - halfSize, Color.BLACK);
		Sphere pointCorner6 = getPoint(centerX + halfSize, centerY + halfSize, centerZ - halfSize, Color.BROWN);
		Sphere pointCorner7 = getPoint(centerX - halfSize, centerY - halfSize, centerZ - halfSize, Color.CYAN);
		Sphere pointCorner8 = getPoint(centerX + halfSize, centerY - halfSize, centerZ - halfSize, Color.PURPLE);
	    volume.getChildren().addAll(pointCorner1);
	    volume.getChildren().addAll(pointCorner2);
	    volume.getChildren().addAll(pointCorner3);
	    volume.getChildren().addAll(pointCorner4);
	    volume.getChildren().addAll(pointCorner5);
	    volume.getChildren().addAll(pointCorner6);
	    volume.getChildren().addAll(pointCorner7);
	    volume.getChildren().addAll(pointCorner8);
	    
	    int focalLength = 1;
	    int scaleFactor = 100;
	    int xTranslate = 0;//boxParameters.getCenterX();
	    int yTranslate = 0;//boxParameters.getCenterY();
	    double[][] rotationMatrix = { { scaleFactor, 0, 0 }, { 0, scaleFactor, 0 }, { xTranslate, yTranslate, 1} };
	    
	    Point3D projectedCorner1 = new Point3D(focalLength*boxCorner1.getX()/boxCorner1.getZ(), focalLength*boxCorner1.getY()/boxCorner1.getZ(), 1);
		Point3D projectedCorner2 = new Point3D(focalLength*boxCorner2.getX()/boxCorner2.getZ(), focalLength*boxCorner2.getY()/boxCorner2.getZ(), 1);
		Point3D projectedCorner3 = new Point3D(focalLength*boxCorner3.getX()/boxCorner3.getZ(), focalLength*boxCorner3.getY()/boxCorner3.getZ(), 1);
		Point3D projectedCorner4 = new Point3D(focalLength*boxCorner4.getX()/boxCorner4.getZ(), focalLength*boxCorner4.getY()/boxCorner4.getZ(), 1);
		Point3D projectedCorner5 = new Point3D(focalLength*boxCorner5.getX()/boxCorner5.getZ(), focalLength*boxCorner5.getY()/boxCorner5.getZ(), 1);
		Point3D projectedCorner6 = new Point3D(focalLength*boxCorner6.getX()/boxCorner6.getZ(), focalLength*boxCorner6.getY()/boxCorner6.getZ(), 1);
		Point3D projectedCorner7 = new Point3D(focalLength*boxCorner7.getX()/boxCorner7.getZ(), focalLength*boxCorner7.getY()/boxCorner7.getZ(), 1);
		Point3D projectedCorner8 = new Point3D(focalLength*boxCorner8.getX()/boxCorner8.getZ(), focalLength*boxCorner8.getY()/boxCorner8.getZ(), 1);
	    projectedCorner1 = Matrix.multiplyPoint(projectedCorner1, rotationMatrix);
		projectedCorner2 = Matrix.multiplyPoint(projectedCorner2, rotationMatrix);
		projectedCorner3 = Matrix.multiplyPoint(projectedCorner3, rotationMatrix);
		projectedCorner4 = Matrix.multiplyPoint(projectedCorner4, rotationMatrix);
		projectedCorner5 = Matrix.multiplyPoint(projectedCorner5, rotationMatrix);
		projectedCorner6 = Matrix.multiplyPoint(projectedCorner6, rotationMatrix);
		projectedCorner7 = Matrix.multiplyPoint(projectedCorner7, rotationMatrix);
		projectedCorner8 = Matrix.multiplyPoint(projectedCorner8, rotationMatrix);
		
		System.out.println("projectedCorner1: [" + projectedCorner1.getX() + ", " + projectedCorner1.getY() +"]");
		System.out.println("projectedCorner2: [" + projectedCorner2.getX() + ", " + projectedCorner2.getY() +"]");
		System.out.println("projectedCorner3: [" + projectedCorner3.getX() + ", " + projectedCorner3.getY() +"]");
		System.out.println("projectedCorner4: [" + projectedCorner4.getX() + ", " + projectedCorner4.getY() +"]");
		System.out.println("projectedCorner5: [" + projectedCorner5.getX() + ", " + projectedCorner5.getY() +"]");
		System.out.println("projectedCorner6: [" + projectedCorner6.getX() + ", " + projectedCorner6.getY() +"]");
		System.out.println("projectedCorner7: [" + projectedCorner7.getX() + ", " + projectedCorner7.getY() +"]");
		System.out.println("projectedCorner8: [" + projectedCorner8.getX() + ", " + projectedCorner8.getY() +"]");
		
	    Sphere projPointCorner1 = getPoint(projectedCorner1.getX(), projectedCorner1.getY(), projectedCorner1.getZ(), Color.RED);
		Sphere projPointCorner2 = getPoint(projectedCorner2.getX(), projectedCorner2.getY(), projectedCorner2.getZ(), Color.GREEN);
		Sphere projPointCorner3 = getPoint(projectedCorner3.getX(), projectedCorner3.getY(), projectedCorner3.getZ(), Color.YELLOW);
		Sphere projPointCorner4 = getPoint(projectedCorner4.getX(), projectedCorner4.getY(), projectedCorner4.getZ(), Color.BLUE);
		Sphere projPointCorner5 = getPoint(projectedCorner5.getX(), projectedCorner5.getY(), projectedCorner5.getZ(), Color.BLACK);
		Sphere projPointCorner6 = getPoint(projectedCorner6.getX(), projectedCorner6.getY(), projectedCorner6.getZ(), Color.BROWN);
		Sphere projPointCorner7 = getPoint(projectedCorner7.getX(), projectedCorner7.getY(), projectedCorner7.getZ(), Color.CYAN);
		Sphere projPointCorner8 = getPoint(projectedCorner8.getX(), projectedCorner8.getY(), projectedCorner8.getZ(), Color.PURPLE);
		
		//add a polygon
	    Polygon polygon = new Polygon();
	    polygon.getPoints().addAll(new Double[]{
	    	projectedCorner1.getX(), projectedCorner1.getY(),
	    	projectedCorner3.getX(), projectedCorner3.getY(),
	    	projectedCorner4.getX(), projectedCorner4.getY(),
	    	projectedCorner8.getX(), projectedCorner8.getY(),
	    	projectedCorner6.getX(), projectedCorner6.getY(),	    	
	    	projectedCorner5.getX(), projectedCorner5.getY(),
//	    	projectedCorner2.getX(), projectedCorner2.getY(),
//	    	projectedCorner7.getX(), projectedCorner7.getY(),
	        });
	    polygon.setFill(Color.TRANSPARENT);
	    polygon.setStroke(Color.RED);
	    Bounds b = polygon.getBoundsInLocal();
	    BoundingBox polygonBox = new BoundingBox(projectedCorner3.getX(), projectedCorner3.getY(), b.getWidth(),b.getHeight());
	    
	    volume.getChildren().addAll(polygon);
		
		volume.getChildren().addAll(projPointCorner1);
	    volume.getChildren().addAll(projPointCorner2);
	    volume.getChildren().addAll(projPointCorner3);
	    volume.getChildren().addAll(projPointCorner4);
	    volume.getChildren().addAll(projPointCorner5);
	    volume.getChildren().addAll(projPointCorner6);
	    volume.getChildren().addAll(projPointCorner7);
	    volume.getChildren().addAll(projPointCorner8);
				
		//String file_path = "C:\\Tools\\eclipse\\workspace\\objectrecognizer\\ObjectRecognizer\\images\\football.jpg";
		//File input = new File(file_path);
		Rectangle rec = new Rectangle();
		//rec.setX(boxParameters.getCenterX()-100);
		//rec.setY(boxParameters.getCenterY()-100);
		//rec.setWidth(200);
		//rec.setHeight(200);
		//Image img = new Image(input.toURI().toString());
		//rec.setFill(new ImagePattern(img));
		rec.setFill(Color.AQUA);
		rec.setX(polygonBox.getMinX());
		rec.setY(polygonBox.getMinY());
		rec.setWidth(polygonBox.getWidth());
		rec.setHeight(polygonBox.getHeight());
		//volume.getChildren().addAll(rec);
		
		//y-axis
		Line line = new Line();
	    line.setStartX(400.0f);
	    line.setStartY(0.0f);
	    line.setEndX(400.0f);
	    line.setEndY(600.0f);		    
	    line.setStroke(Color.GREEN);
	    volume.getChildren().addAll(line);
	    NumberAxis yAxis = new NumberAxis(0, 600, 50);
	    yAxis.setSide(Side.LEFT);
	    yAxis.setMinHeight(600);
	    //yAxis
	    volume.getChildren().addAll(yAxis);
	    
	    //x-axis
	    Line line2 = new Line();
	    line2.setStartX(0.0f);
	    line2.setStartY(300.0f);
	    line2.setEndX(800.0f);
	    line2.setEndY(300.0f);	
	    line2.setStroke(Color.RED);
	    volume.getChildren().addAll(line2);
	    NumberAxis xAxis = new NumberAxis(0, 800, 50);
	    xAxis.setSide(Side.TOP);
	    xAxis.setMinWidth(800);
	    volume.getChildren().addAll(xAxis);
	    
	    //z-axis
	    Line line3 = new Line();
	    line3.setStartX(0.0f);
	    line3.setStartY(300.0f);
	    line3.setEndX(800.0f);
	    line3.setEndY(300.0f);	
	    line3.setStroke(Color.YELLOW);
	    //volume.getChildren().addAll(line3);
	    
	    //point
	    Sphere sphere = new Sphere();     
	    sphere.setRadius(50.0);
	    sphere.setTranslateX(0);
	    sphere.setTranslateY(0);
	    sphere.setTranslateZ(0);
	    //volume.getChildren().addAll(sphere);

		return volume;
	}

	public Sphere getPoint(double x, double y, double z, Color color) {
		//point
	    Sphere sphere = new Sphere();     
	    sphere.setRadius(5.0);
	    sphere.setTranslateX(x);
	    sphere.setTranslateY(y);
	    sphere.setTranslateZ(z);
	    PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = color; 
		textureMaterial.setDiffuseColor(diffuseColor);
		sphere.setMaterial(textureMaterial);
		return sphere;
	}
	
	private Box generateVoxel(int x, int y, int z, double width, double height, double depth, Color nodeColor) {
		Box box = new Box(width, height, depth);

		int posx = x;//boxParameters.getCenterX();// + (deltas.deltaX * boxParameters.getBoxSize() / 2);
		int posy = y;//boxParameters.getCenterY();// + (deltas.deltaY * boxParameters.getBoxSize() / 2);
		int posz = z;//boxParameters.getCenterZ();// + (deltas.deltaZ * boxParameters.getBoxSize() / 2);
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = nodeColor; 
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}
}
