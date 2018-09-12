package nl.tue.vc.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.application.visual.IntersectionTest;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.imgproc.CameraController;
import nl.tue.vc.imgproc.SilhouetteExtractor;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.Octree;
import nl.tue.vc.model.OctreeCubeProjector;
import nl.tue.vc.model.VolumeGenerator;
import nl.tue.vc.model.test.OctreeTest;
import nl.tue.vc.model.test.VolumeGeneratorTest;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.voxelengine.CameraPosition;
import nl.tue.vc.voxelengine.VolumeRenderer;

public class ObjectReconstructorController {
	// images to show in the view
	@FXML
	private ImageView originalImage;
	
	@FXML
	private ImageView originalImage2;

	@FXML
	private VBox objectImageArea;

	@FXML
	private VBox imageProcessingArea;

	@FXML
	private ImageView transformedImage;
	
	@FXML
	private ImageView antitransformedImage;

	@FXML
	private Button extractButton;
	
	@FXML
	private Button constructButton;
	
	@FXML
	private Button visualizeButton;

	@FXML
	private Button applyButton;

	@FXML
	private Button calibrateExtrinsicParamsButton;

	@FXML
	private Button snapshotButton;

	@FXML
	private ImageView originalFrame;

	@FXML
	private ImageView imageOperationsFrame;

	// the FXML area for showing the current frame (after calibration)
	@FXML
	private ImageView calibrationFrame;

	@FXML
	private VBox projectedVolumeArea;

	@FXML
	private VBox projectionImagesArea;

	@FXML
	private ImageView projectedVolumeView;

	@FXML
	private Slider cameraDistanceSlider;

	private double cameraDistance;
	
	@FXML
	private Slider binaryThresholdSlider;

	@FXML
	private Label thresholdLabel;

	@FXML
	private ComboBox<String> segmentationAlgorithm;

	@FXML
	private ComboBox<String> exampleSelection;
	
	@FXML
	private CheckBox debugSegmentation;

	@FXML
	private CheckBox turnOnCamera;

	@FXML
	private CheckBox enableCameraCalibration;
	
	@FXML
	private CheckBox thresholdForAll;

	@FXML
	private ImageView cameraFrameView;
	
	@FXML
	private ImageView binaryFrameView;
	
	@FXML
	private TextField textFieldOctreeLengthX;
	
	@FXML
	private TextField textFieldOctreeLengthY;
	
	@FXML
	private TextField textFieldOctreeLengthZ;

	@FXML
	private TextField textFieldOctreeDisplacementX;

	@FXML
	private TextField textFieldOctreeDisplacementY;
	
	@FXML
	private TextField textFieldOctreeDisplacementZ;

	@FXML
	private TextField levelsField;

	@FXML
	private Button update3dModelButton;

	@FXML
	private Button modelGenerationTestButton;

	@FXML
	private Button modelGenerationSlowTestButton;
	
	private int fieldOfView;

	// old timer
	private Timer calibrationTimer;

	private Timer imageTimer;
	// the OpenCV object that performs the video capture
	private VideoCapture calibrationCapture;
	// a flag to change the button behavior
	private boolean cameraActive;
	// the saved chessboard image
	private Mat savedImage;
	// the calibrated camera frame
	private Image CamStream;

	// Image used for calibration of extrinsic parameters
	private Map<String, Mat> calibrationImagesMap = new HashMap<String, Mat>();
	private int calibrationImageCounter;
	private List<String> calibrationIndices;

	private Mat calibrationImage = null;

	private int OCTREE_LEVELS = 7;
	
	private String thresholdImageIndex = "ALL_IMAGES";
	Map<String, Integer> imageThresholdMap = new HashMap<String, Integer>();
	private Map<String, Mat> objectImagesMap = new HashMap<String, Mat>();
	private List<Mat> objectImagesToDisplay = new ArrayList<Mat>();
	private ObservableList<String> objectImagesNames = FXCollections.observableArrayList();
	private ListView<String> objectImagesView = new ListView<>();
	private Map<String, Integer> objectImagesDescription = new HashMap<>();

	private Map<String, Mat> binarizedImagesMap = new HashMap<String, Mat>();
	private List<Mat> binaryImagesToDisplay = new ArrayList<Mat>();
	private ObservableList<String> binaryImagesNames = FXCollections.observableArrayList();
	private ListView<String> binaryImagesView = new ListView<>();
	private Map<String, Integer> binaryImagesDescription = new HashMap<>();

	private Map<String, Mat> projectionImagesMap = new HashMap<String, Mat>();
	private ObservableList<String> projectionImagesNames = FXCollections.observableArrayList();
	private ListView<String> projectionImagesView = new ListView<>();
	private Map<String, Integer> projectionImagesDescription = new HashMap<>();

	private Map<String, BufferedImage> imagesForDistanceComputation = new HashMap<String, BufferedImage>();
	private Map<String, int[][]> distanceArrays = new HashMap<String, int[][]>();
	private Map<String, int[][]> invertedDistanceArrays = new HashMap<String, int[][]>();

	// the main stage
	private Stage stage;
	// the JavaFX file chooser
	private FileChooser fileChooser;
	private DirectoryChooser directoryChooser;
	// support variables
	private Mat image;
	private List<Mat> planes;
	private double calibrationResult = 0;

	// The rootGroup
	private AnchorPane rootGroup;
	private TabPane tabPane;
	private Tab cameraCalibrationTab;
	private AnchorPane cameraCalibrationAnchorPane;
	private BorderPane cameraCalibrationBorderPane;
	private Tab imageSelectionTab;
	private AnchorPane imageSelectionAnchorPane;
	private BorderPane imageSelectionBorderPane;
	private Tab modelRenderingTab;
	private AnchorPane modelRenderingAnchorPane;
	private BorderPane modelRenderingBorderPane;
	private Tab modelConfigTab;
	private AnchorPane modelConfigAnchorPane;
	private BorderPane modelConfigBorderPane;
	private Tab silhouettesConfigTab;
	private AnchorPane silhouettesConfigAnchorPane;
	private BorderPane silhouettesConfigBorderPane;
	
	private int CAMERA_CALIBRATION_TAB_ORDER = 0;
	private int IMAGE_SELECTION_TAB_ORDER = 1;
	private int MODEL_RENDERING_TAB_ORDER = 2;
	private int SILHOUETTES_CONFIG_TAB_ORDER = 3;
	private int MODEL_CONFIG_TAG_ORDER = 4;
	

	private ToggleGroup calibrateCameraOptions;
	
	@FXML
	private RadioButton calibrateCameraFromDirectory;
	
	@FXML
	private RadioButton calibrateCameraFromWebcam;
	
	@FXML
	private Button cameraCalibrationDirectoryButton;
	
	
	private VolumeRenderer volumeRenderer;
	private VolumeGenerator volumeGenerator;

	private SilhouetteExtractor silhouetteExtractor;

	private CameraController cameraController;

	private CameraCalibrator cameraCalibrator;

	private ProjectionGenerator projectionGenerator;

	private Mat cameraFrame;

	private Timer videoTimer;

	private boolean videoTimerActive;

	public static int SNAPSHOT_DELAY = 250;
	public static final boolean TEST_PROJECTIONS = true;
	private int levels;

	private float DISPLACEMENT_X;
	private float DISPLACEMENT_Y;
	private float DISPLACEMENT_Z;

	private float CUBE_LENGTH_X;
	private float CUBE_LENGTH_Y;
	private float CUBE_LENGTH_Z;

	private Octree octree;
	private String calibrationImagesDir = "images/calibrationImages/";

	private String OBJECT_IMAGES_DIR = "examples/laptopCharger/";
	private String CALIBRATION_IMAGES_DIR = "examples/laptopCharger/calibrationImages/";
	private String OBJECT_IMAGES_DIR_SLOW_TEST = "examples/blackCup242/";
	private String CALIBRATION_IMAGES_DIR_SLOW_TEST = "examples/blackCup242/calibrationImages/";
	
	public ObjectReconstructorController() {

		silhouetteExtractor = new SilhouetteExtractor();
		cameraController = new CameraController();
		cameraFrame = new Mat();
		cameraFrameView = new ImageView();
		originalFrame = new ImageView();
		videoTimer = new Timer();
		videoTimerActive = false;
		calibrationTimer = new Timer();
		cameraCalibrator = new CameraCalibrator();
		
		/** Xbox control 
		DISPLACEMENT_X = -2;
		DISPLACEMENT_Y = -1;
		DISPLACEMENT_Z = (float) -6.5;
		
		CUBE_LENGTH_X = 11;
		CUBE_LENGTH_Y = (float) 6.5;
		CUBE_LENGTH_Z = (float) 14.5;
		**/
		
		DISPLACEMENT_X = -2;
		DISPLACEMENT_Y = -1;
		DISPLACEMENT_Z = (float) -2;
		
		CUBE_LENGTH_X = 12;
		CUBE_LENGTH_Y = (float) 8;
		CUBE_LENGTH_Z = (float) 10;
		
		
		this.levels = 0;// Integer.parseInt(this.levelsField.getText());

		calibrationImageCounter = 0;
		initCalibrationIndices();
		initCalibrationIndicesSlowTest();
		projectionGenerator = null;
		octree = null;
		cameraDistance = 300;
	}

	// TODO: generate this indices automatically
	private void initCalibrationIndices() {
		calibrationIndices = new ArrayList<String>();
		calibrationIndices.add("deg-0");
		calibrationIndices.add("deg-30");
		calibrationIndices.add("deg-60");
		calibrationIndices.add("deg-90");
		calibrationIndices.add("deg-120");
		calibrationIndices.add("deg-150");
		calibrationIndices.add("deg-180");
		calibrationIndices.add("deg-210");
		calibrationIndices.add("deg-240");
		calibrationIndices.add("deg-270");
		calibrationIndices.add("deg-300");
		calibrationIndices.add("deg-330");
	}

	private void initCalibrationIndicesSlowTest(){
		calibrationIndices = new ArrayList<String>();
		calibrationIndices.add("deg-0");
		calibrationIndices.add("deg-15");		
		calibrationIndices.add("deg-30");
		calibrationIndices.add("deg-45");
		calibrationIndices.add("deg-60");
		calibrationIndices.add("deg-75");
		calibrationIndices.add("deg-90");
		calibrationIndices.add("deg-105");
		calibrationIndices.add("deg-120");
		calibrationIndices.add("deg-135");
		calibrationIndices.add("deg-150");
		calibrationIndices.add("deg-165");
		calibrationIndices.add("deg-180");
		calibrationIndices.add("deg-195");
		calibrationIndices.add("deg-210");
		calibrationIndices.add("deg-240");
		calibrationIndices.add("deg-255");
		calibrationIndices.add("deg-270");
		calibrationIndices.add("deg-285");
		calibrationIndices.add("deg-300");
		calibrationIndices.add("deg-315");
		calibrationIndices.add("deg-330");
		calibrationIndices.add("deg-345");
	}
	
	@FXML
	private void initialize() {

		cameraDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera distance changed (newValue: " + newValue.intValue() + ")");
			updateCameraDistance(newValue.intValue());
		});

		binaryThresholdSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue,
					Boolean wasChanging,
					Boolean changing){
				if (!changing){
					updateBinaryThreshold((int)binaryThresholdSlider.getValue());
				} else {
					thresholdLabel.setText("Threshold: " + String.format("%.2f", binaryThresholdSlider.getValue()));
				}
			}
		});
		
		binaryThresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			// System.out.println("Binary treshold value changed (newValue: " +
			// newValue.intValue() + ")");
			thresholdLabel.setText("Threshold: " + String.format("%.2f", newValue));
			updateBinaryThreshold(newValue.intValue());
		});
		

		// configuration of the camera
		turnOnCamera.setOnAction((event) -> {
			boolean selected = turnOnCamera.isSelected();
			if (selected) {
				System.out.println("******* Turn on camera is selected!!!");
				startVideo();
				/**
				 * if (enableCameraCalibration.isSelected()) { startCameraCalibration(); } else
				 * { startVideo(); }
				 **/
			} else {
				System.out.println("******* Turn on camera is unselected!!!");

				/**
				 * if (calibrationTimerActive) { calibrationTimer.cancel();
				 * calibrationTimerActive = false; } if (calibrationCapture.isOpened()) {
				 * calibrationCapture.release(); }
				 **/

				stopVideo();
				System.out.println("Timers are cancelled!");
				// cameraFrameView.setImage(new Image("images/bkg_img.jpg"));
			}
			// System.out.println("CheckBox Action (selected: " + selected + ")");
		});

		// Camera calibration is selected
		enableCameraCalibration.setOnAction((event) -> {
			calibrationImageCounter = 0;
			Utils.debugNewLine("calibrationImageCounter is set to " + calibrationImageCounter, true);
		});

		/**
		 * enableCameraCalibration.setOnAction((event) -> {
		 * 
		 * //clearLoadedImages(); /** boolean selected =
		 * enableCameraCalibration.isSelected(); if (selected) {
		 * System.out.println("********** Calibrate camera is selected!!!"); if
		 * (turnOnCamera.isSelected()) { // cancel basic video display stopVideo();
		 * startCameraCalibration(); //stopCameraCalibration(); } } else {
		 * System.out.println("********** Calibrate camera is unselected!!!"); if
		 * (calibrationTimerActive) { calibrationTimer.cancel(); calibrationTimerActive
		 * = false; } if (calibrationCapture.isOpened()) { calibrationCapture.release();
		 * }
		 * 
		 * if (turnOnCamera.isSelected()) { startVideo(); } } });
		 **/

		// Set default image
		/*
		 * try { defaultVideoImage = new Image("images/bkg_img.jpg");
		 * cameraFrameView.setImage(defaultVideoImage); } catch (Exception e) {
		 * e.printStackTrace(); defaultVideoImage = null; }
		 */

		// segmentationAlgorithm;
		segmentationAlgorithm.getItems().add("Watersheed");
		segmentationAlgorithm.getItems().add("Binarization");
		segmentationAlgorithm.getItems().add("Equalized");
		segmentationAlgorithm.setValue("Binarization");

		System.out.println(segmentationAlgorithm.getValue());

		exampleSelection.getItems().add("Charger");
		exampleSelection.getItems().add("Cup");
		exampleSelection.getItems().add("Hexagon");
		exampleSelection.setValue("Charger");
		
		/*
		 * if (calibrateCamera.isSelected()) { startCameraCalibration(); } else {
		 * startVideo(); }
		 */

		// this.vboxLeft.getChildren().add(cameraFrameView);
		this.objectImageArea.getChildren().add(objectImagesView);
		objectImagesView.setMaxWidth(140);

		this.imageProcessingArea.getChildren().add(binaryImagesView);
		binaryImagesView.setMaxWidth(140);

		projectionImagesArea.getChildren().add(projectionImagesView);
		projectionImagesView.setMaxWidth(140);
		
		/**
		update3dModelButton.setOnKeyReleased((event) -> {
			CUBE_LENGTH_X = (float) Double.parseDouble(textFieldOctreeLengthX.getText());
			CUBE_LENGTH_Y = (float) Double.parseDouble(textFieldOctreeLengthY.getText());
			CUBE_LENGTH_Z = (float) Double.parseDouble(textFieldOctreeLengthZ.getText());
			
			DISPLACEMENT_X = (float) Double.parseDouble(textFieldOctreeDisplacementX.getText());
			DISPLACEMENT_Y = (float) Double.parseDouble(textFieldOctreeDisplacementY.getText());
			DISPLACEMENT_Z = (float) Double.parseDouble(textFieldOctreeDisplacementZ.getText());

			createOctreeProjections();
			//constructModel();
			//renderModel();
		});
		**/
		
		configureGUI();
	}

	protected void configureGUI(){
		calibrateCameraOptions = new ToggleGroup();
		calibrateCameraFromWebcam.setToggleGroup(calibrateCameraOptions);
		calibrateCameraFromDirectory.setToggleGroup(calibrateCameraOptions);
		calibrateCameraFromDirectory.setSelected(true);
		
		calibrateCameraOptions.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue){
				RadioButton radioButton = (RadioButton) calibrateCameraOptions.getSelectedToggle();
				if (radioButton != null){
					String selection = radioButton.getText();
					System.out.println("Radio button selected: " + selection);
					if (selection.equals("Webcam")){
						cameraCalibrationDirectoryButton.setDisable(true);
					} else if (selection.equals("Directory")){
						cameraCalibrationDirectoryButton.setDisable(false);						
					}
				}
			}
		});
		
		cameraCalibrationDirectoryButton.setOnAction(event -> {
			File selectedDirectory = directoryChooser.showDialog(stage);
			if (selectedDirectory != null){
				System.out.println(selectedDirectory.getAbsolutePath());
			}
		});
	}
	
	protected void init() {
		this.fileChooser = new FileChooser();
		directoryChooser = new DirectoryChooser();
		this.image = new Mat();
		this.planes = new ArrayList<>();
		this.calibrationCapture = new VideoCapture();
		this.cameraActive = false;
		this.savedImage = new Mat();
	}

	/**
	 * Load an image from disk
	 */
	@FXML
	protected void loadImage() {
		// imageViews.add(this.originalImage);
		// imageViews.add(this.originalImage2);

		List<File> list = fileChooser.showOpenMultipleDialog(stage);

		if (list != null) {
			// Clear content of previous images
			// loadedImagesNames.clear();
			// loadedImages.clear();
			// loadedImagesDescription.clear();

			for (int i = 0; i < list.size(); i++) {

				// show the open dialog window
				// File file = this.fileChooser.showOpenDialog(this.stage);
				File file = list.get(i);

				if (file != null) {
					// ImageView imageView = new ImageView();
					// read the image in gray scale
					// this.image = Imgcodecs.imread(file.getAbsolutePath(),
					// Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
					this.image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);

					if (enableCameraCalibration.isSelected()) {
						Utils.debugNewLine("Loading calibration image: " + file.getName(), false);
						calibrationImage = this.image;
						calibrationFrame.setImage(Utils.mat2Image(calibrationImage));
						calibrationFrame.setFitWidth(100);
						calibrationFrame.setPreserveRatio(true);
						calibrationImagesMap.put(calibrationIndices.get(calibrationImageCounter), calibrationImage);
						String calibrationIndex = calibrationIndices.get(calibrationImageCounter);
						Utils.debugNewLine("Calibration index: " + calibrationIndex, true);

						/**
						 * objectImagesNames.add(imageName);
						 * objectImagesToDisplay.add(calibrationImage);
						 * objectImagesDescription.put(imageName, objectImagesToDisplay.size() - 1);
						 **/
						calibrationImageCounter += 1;
						if (calibrationImageCounter > calibrationIndices.size() - 1) {
							calibrationImageCounter = 0;
						}
					} else {
						String calibrationIndex = calibrationIndices.get(calibrationImageCounter);

						objectImagesNames.add(calibrationIndex);
						objectImagesToDisplay.add(this.image);

						objectImagesDescription.put(calibrationIndex, objectImagesToDisplay.size() - 1);
						objectImagesMap.put(calibrationIndex, this.image);
						int threshold = (int) binaryThresholdSlider.getValue();
						imageThresholdMap.put(calibrationIndex, threshold);
						System.out.println(
								"set image threshold value for image " + calibrationIndex + " as " + threshold);

						calibrationImageCounter += 1;
						if (calibrationImageCounter > calibrationIndices.size() - 1) {
							calibrationImageCounter = 0;
						}

						// empty the image planes and the image views if it is not the first
						// loaded image
						if (!this.planes.isEmpty()) {
							this.planes.clear();
							this.transformedImage.setImage(null);
							this.antitransformedImage.setImage(null);
						}
					}
				}
			}
			showImages();
		}
	}

	public void showImages() {

		objectImagesView.setItems(objectImagesNames);
		objectImagesView.setCellFactory(param -> {
			ListCell<String> cell = new ListCell<String>() {
				private ImageView imageView = new ImageView();

				@Override
				public void updateItem(String name, boolean empty) {
					super.updateItem(name, empty);
					if (empty) {
						// System.out.println("Null information");
						setText(null);
						setGraphic(null);
					} else {
						int imagePosition = objectImagesDescription.get(name);
						Utils.debugNewLine("Image name: " + name + ", position: " + imagePosition, true);
						Utils.debugNewLine("ObjectImagesToDisplay size: " + objectImagesToDisplay.size(), true);
						imageView.setImage(Utils.mat2Image(objectImagesToDisplay.get(imagePosition)));
						imageView.setFitWidth(100);
						imageView.setPreserveRatio(true);
						setText(name);
						setGraphic(imageView);
						
						/**
						if (imageOperationsFrame.getImage() == null) {
							setImageOperationFrameImage(imageView.getImage());
						}
						**/
					}
				}
			};

			cell.setOnMouseClicked(e -> {
				if (cell.getItem() != null) {
					ImageView imageView = (ImageView) cell.getGraphic();
					setImageOperationFrameImage(imageView.getImage());
					thresholdImageIndex = cell.getText();
					Utils.debugNewLine("Click on image " + thresholdImageIndex, true);
				}
			});

			return cell;
		});

		//objectImagesView.setMaxWidth(140);
		//objectImagesView.refresh();
	}

	/**
	 * The action triggered by pushing the button for apply the dft to the loaded
	 * image
	 * 
	 * @throws InterruptedException
	 */
	@FXML
	protected void extractSilhouettes() {
		// We load test images from a directory
		if (objectImagesMap.isEmpty()) {
			loadDefaultImages();
		}

		System.out.println("Extract silhouettes method was called...");

		// First, clear the previous content. Then, load the new content
		binaryImagesView = new ListView<String>();
		binaryImagesNames = FXCollections.observableArrayList();
		binaryImagesDescription = new HashMap<String, Integer>();

		Utils.debugNewLine("Silhouette extraction for " + thresholdImageIndex, true);
		if (thresholdImageIndex.equals("ALL_IMAGES") || thresholdForAll.isSelected()) {
			binarizedImagesMap = new HashMap<String, Mat>();

			for (String imageKey : objectImagesMap.keySet()) {
				Mat imageToBinarize = objectImagesMap.get(imageKey);
				int binaryThreshold = imageThresholdMap.get(imageKey);
				Utils.debugNewLine("Extracting Image " + imageKey + " with binary threshold " + binaryThreshold, true);

				silhouetteExtractor.setBinaryThreshold(binaryThreshold);
				silhouetteExtractor.extract(imageToBinarize, segmentationAlgorithm.getValue());
				binarizedImagesMap.put(imageKey, silhouetteExtractor.getSegmentedImage());

				try {
					BufferedImage bufImage = IntersectionTest
							.Mat2BufferedImage(silhouetteExtractor.getSegmentedImage());
					imagesForDistanceComputation.put(imageKey, bufImage);
				} catch (Exception e) {
					System.out.println("Something went really wrong!!!");
					e.printStackTrace();
				}

				binaryImagesNames.add(imageKey);
				binaryImagesDescription.put(imageKey, binarizedImagesMap.size() - 1);
			}
		} else {
			Mat imageToBinarize = objectImagesMap.get(thresholdImageIndex);
			int binaryThreshold = imageThresholdMap.get(thresholdImageIndex);
			Utils.debugNewLine("Extracting Image " + thresholdImageIndex + " with binary threshold " + binaryThreshold,
					true);

			silhouetteExtractor.setBinaryThreshold(binaryThreshold);
			silhouetteExtractor.extract(imageToBinarize, segmentationAlgorithm.getValue());
			binarizedImagesMap.put(thresholdImageIndex, silhouetteExtractor.getSegmentedImage());

			try {
				BufferedImage bufImage = IntersectionTest.Mat2BufferedImage(silhouetteExtractor.getSegmentedImage());
				imagesForDistanceComputation.put(thresholdImageIndex, bufImage);
			} catch (Exception e) {
				System.out.println("Something went really wrong!!!");
				e.printStackTrace();
			}

			for (String imageKey : binarizedImagesMap.keySet()) {
				binaryImagesNames.add(imageKey);
				binaryImagesDescription.put(imageKey, binarizedImagesMap.size() - 1);
			}

		}

		binaryImagesView.setItems(binaryImagesNames);
		System.out.println(binaryImagesDescription.keySet());

		binaryImagesView.setCellFactory(param -> {
			ListCell<String> cell = new ListCell<String>() {
				private ImageView imageView = new ImageView();

				@Override
				public void updateItem(String name, boolean empty) {
					super.updateItem(name, empty);
					if (empty) {
						// System.out.println("Null information");
						setText(null);
						setGraphic(null);
					} else {
						// Add thumpnails here?
						System.out.println("Binary image name: " + name);
						imageView.setImage(Utils.mat2Image(binarizedImagesMap.get(name)));
						imageView.setFitWidth(100);
						imageView.setPreserveRatio(true);
						setText(name);
						setGraphic(imageView);
						if (imageOperationsFrame.getImage() == null) {
							setImageOperationFrameImage(imageView.getImage());
						}
					}
				}

			};

			cell.setOnMouseClicked(e -> {
				if (cell.getItem() != null) {
					ImageView imageView = (ImageView) cell.getGraphic();
					setImageOperationFrameImage(imageView.getImage());
					thresholdImageIndex = cell.getText();
					int binaryThreshold = imageThresholdMap.get(thresholdImageIndex);
					binaryThresholdSlider.setValue(binaryThreshold);
					Utils.debugNewLine("Binary image name " + cell.getText(), true);
				}
			});

			return cell;
		});

		// to allow updating new elements for the list view
		this.imageProcessingArea.getChildren().clear();
		this.imageProcessingArea.getChildren().add(binaryImagesView);
		
		createOctreeProjections();
	}

	/**
	 * Store all the chessboard properties, update the UI and prepare other needed
	 * variables
	 */
	@FXML
	protected void updateSettings() {
		Utils.debugNewLine("[updateSettings] called!", true);
		configValuesForExample();
	}

	/**
	 * updates the boxsize and levels of the octree
	 */
	@FXML
	protected void update3DModel() {
		CUBE_LENGTH_X = (float) Double.parseDouble(textFieldOctreeLengthX.getText());
		CUBE_LENGTH_Y = (float) Double.parseDouble(textFieldOctreeLengthY.getText());
		CUBE_LENGTH_Z = (float) Double.parseDouble(textFieldOctreeLengthZ.getText());
		
		DISPLACEMENT_X = (float) Double.parseDouble(textFieldOctreeDisplacementX.getText());
		DISPLACEMENT_Y = (float) Double.parseDouble(textFieldOctreeDisplacementY.getText());
		DISPLACEMENT_Z = (float) Double.parseDouble(textFieldOctreeDisplacementZ.getText());
		createOctreeProjections();
		//constructModel();
		//renderModel();
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {

	}

	private void startCameraCalibration() {
		if (!this.cameraActive) {
			// start the video capture
			this.calibrationCapture.open(1);
			// is the video stream available?
			if (this.calibrationCapture.isOpened()) {
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				TimerTask frameGrabber = new TimerTask() {
					@Override
					public void run() {
						CamStream = grabFrameCalibration();
						// show the original frames
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								cameraFrameView.setImage(CamStream);
								// set fixed width
								cameraFrameView.setFitWidth(100);
								// preserve image ratio
								cameraFrameView.setPreserveRatio(true);
								// show the original frames
								// calibratedFrame.setImage(undistoredImage);
								// set fixed width
								// calibratedFrame.setFitWidth(100);
								// preserve image ratio
								// calibratedFrame.setPreserveRatio(true);
							}
						});

						// System.out.println("Calibration timer is running!");
					}
				};
				calibrationTimer = new Timer();
				calibrationTimer.schedule(frameGrabber, 0, 33);

				// update the button content
				// this.cameraButton.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			// this.cameraButton.setText("Start Camera");
			// stop the timer
			if (calibrationTimer != null) {
				calibrationTimer.cancel();
				calibrationTimer = null;
			}
			// release the camera
			this.calibrationCapture.release();
			// clean the image areas
			cameraFrameView.setImage(null);
			calibrationFrame.setImage(null);
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Image} to show
	 */
	private Image grabFrameCalibration() {
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();

		// check if the capture is open
		if (this.calibrationCapture.isOpened()) {
			try {
				// read the current frame
				this.calibrationCapture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					/**
					 * // show the chessboard pattern this.findAndDrawPoints(frame);
					 * 
					 * if (this.isCalibrated) { // prepare the undistored image Mat undistored = new
					 * Mat(); Imgproc.undistort(frame, undistored, intrinsic, distCoeffs);
					 * undistoredImage = Utils.mat2Image(undistored); }
					 **/

					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = Utils.mat2Image(frame);
				}

			} catch (Exception e) {
				// log the (full) error
				System.err.print("ERROR");
				e.printStackTrace();
			}
		}

		return imageToShow;
	}

	// @FXML
	protected void startVideo() {

		cameraController.startCamera();
		TimerTask frameGrabber = new TimerTask() {

			@Override
			public void run() {
				cameraFrame = cameraController.grabFrame();
				// System.out.println("Frame grabbed!!");
				/*
				 * if (!cameraFrame.empty()) {
				 * cameraFrameView.setImage(Utils.mat2Image(cameraFrame));
				 * originalFrame.setFitWidth(100); originalFrame.setPreserveRatio(true); }
				 */

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!cameraFrame.empty()) {
							// cameraFrameView.setImage(Utils.mat2Image(cameraFrame));
							originalFrame.setImage(Utils.mat2Image(cameraFrame));
							originalFrame.setFitWidth(500);
							originalFrame.setPreserveRatio(true);
						}
					}
				});
				// System.out.println("videoTimer is running!");
			}
		};

		// System.out.println("Video timer is running!");
		videoTimer = new Timer();
		videoTimer.schedule(frameGrabber, 0, 33);
		videoTimerActive = true;
	}

	private void stopVideo() {
		if (videoTimerActive) {
			videoTimer.cancel();
			videoTimerActive = false;
		}
		cameraController.release();
	}

	/**
	 * Take a snapshot to be used for the calibration process
	 */
	@FXML
	protected void takeSnapshot() {

		// take snapshots for the camera calibration process
		TimerTask frameGrabber;
		if (enableCameraCalibration.isSelected()) {
			System.out.println("Snapshot for camera calibration");
			// clearLoadedImages();

			calibrationFrame.setImage(null);
			frameGrabber = new TimerTask() {
				@Override
				public void run() {
					cameraController.startCamera();
					calibrationImage = cameraController.grabFrame();
					calibrationFrame.setImage(Utils.mat2Image(calibrationImage));
					calibrationFrame.setFitWidth(100);
					calibrationFrame.setPreserveRatio(true);
					calibrationImagesMap.put(calibrationIndices.get(calibrationImageCounter), calibrationImage);
					saveCalibrationImages(calibrationIndices.get(calibrationImageCounter), calibrationImage);
					calibrationImageCounter += 1;
					if (calibrationImageCounter > calibrationIndices.size() - 1) {
						calibrationImageCounter = 0;
					}
				}
			};

		} // take snapshots for the silhouette extraction process
		else {
			// clearLoadedImages();
			frameGrabber = new TimerTask() {
				@Override
				public void run() {
					cameraController.startCamera();
					//int imagePosition = objectImagesMap.size();
					
					String imageKey = calibrationIndices.get(calibrationImageCounter);
					Mat image = cameraController.grabFrame();
					objectImagesToDisplay.add(image);
					objectImagesMap.put(imageKey, image);
					objectImagesNames.add(imageKey);
					objectImagesDescription.put(imageKey, objectImagesToDisplay.size() - 1);
					int threshold = (int) binaryThresholdSlider.getValue();
					imageThresholdMap.put(imageKey, threshold);

					calibrationImageCounter += 1;
					if (calibrationImageCounter > calibrationIndices.size() - 1){
						calibrationImageCounter = 0;
					}
					showImages();
				}
			};

		}

		imageTimer = new Timer();
		imageTimer.schedule(frameGrabber, 250);
	}

	@FXML
	private void calibrateCameraForExtrinsicParams() {

		Utils.debugNewLine("*** Calibrating camera to find extrinsic parameters ***", true);
		Utils.debugNewLine("Calibration images Map size: " + calibrationImagesMap.size(), true);
		if (calibrationImagesMap.isEmpty()) {
			Utils.debugNewLine("*** Load calibration images ***", true);
			// Load calibration images
			final File folder = new File(calibrationImagesDir);
			List<String> calibrationImageFilenames = Utils.listFilesForFolder(folder);

			int calIndex = 0;
			for (String filename : calibrationImageFilenames) {
				filename = calibrationImagesDir + filename;
				System.out.println("Filename: " + filename);
				Mat image = Utils.loadImage(filename);
				if (image != null) {
					calibrationImagesMap.put(calibrationIndices.get(calIndex), image);
					calIndex++;
				}
			}
		}
		projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImagesMap, true);
	}


	@FXML
	protected void clearLoadedImages() {
		objectImagesNames.clear();
		objectImagesMap.clear();
		objectImagesDescription.clear();
	}

	public BoxParameters createRootNodeParameters(){
		float centerX = (CUBE_LENGTH_X + DISPLACEMENT_X) / 2;
		float centerY = (CUBE_LENGTH_Y + DISPLACEMENT_Y) / 2;
		float centerZ = (CUBE_LENGTH_Z + DISPLACEMENT_Z) / 2;

		BoxParameters volumeBoxParameters = new BoxParameters();
		volumeBoxParameters.setSizeX(CUBE_LENGTH_X);
		volumeBoxParameters.setSizeY(CUBE_LENGTH_Y);
		volumeBoxParameters.setSizeZ(CUBE_LENGTH_Z);

		volumeBoxParameters.setCenterX(centerX);
		volumeBoxParameters.setCenterY(centerY);
		volumeBoxParameters.setCenterZ(centerZ);
		
		return volumeBoxParameters;
	}
	
	public void constructModelAux(int octreeLevels) {
		Utils.debugNewLine("ObjectRecognizerController.constructModelAux(" + octreeLevels +")", true);
		
		// If there is no octree, create one. Otherwise, update the current one
		Utils.debugNewLine("++++++++++++++++++++++++ Updating octree", false);
		BoxParameters volumeBoxParameters = createRootNodeParameters();
		octree.setBoxParameters(volumeBoxParameters);
		octree.splitNodes(octreeLevels);

		if (octree == null) {
			Utils.debugNewLine("***************** something weird happened here", true);
		}

		// TODO: Maybe this generator could be a builder 
		octree = updateOctreeLeafs(octree, volumeBoxParameters, octreeLevels);//volumeGenerator.getOctree();
	}

	public Octree updateOctreeLeafs(Octree octree, BoxParameters volumeBoxParameters, int octreeDepth){
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters, distanceArrays,
				invertedDistanceArrays, octreeDepth);
		volumeGenerator.setImagesForDistanceComputation(this.imagesForDistanceComputation);
		volumeGenerator.setDistanceArrays(distanceArrays);
		volumeGenerator.setInvertedDistanceArrays(invertedDistanceArrays);
		volumeGenerator.setProjectionGenerator(projectionGenerator);		
		volumeGenerator.generateOctreeVoxels(octreeDepth);
		return volumeGenerator.getOctree();	
	}
	
	/**
	 * The action triggered by pushing the button for constructing the model from
	 * the loaded images
	 */
	@FXML
	protected void constructModel() {
		// System.out.println("height = " + this.processedExtractedImage.size().height +
		// ", width = " + this.processedExtractedImage.size().width);

		for (String imageKey : imagesForDistanceComputation.keySet()) {
			BufferedImage image = imagesForDistanceComputation.get(imageKey);
			int[][] sourceArray = IntersectionTest.getBinaryArray(image);
			int[][] transformedArray = IntersectionTest.computeDistanceTransform(sourceArray);
			distanceArrays.put(imageKey, transformedArray);

			int[][] invertedArray = IntersectionTest.getInvertedArray(sourceArray);
			int[][] transformedInvertedArray = IntersectionTest.computeDistanceTransform(invertedArray);
			invertedDistanceArrays.put(imageKey, transformedInvertedArray);
		}

		// Create the projected octree images		
		
		int initialLevels = 2;
		int maxLevels = 7;
		
		Utils.debugNewLine("+++++++ Creating octree ++++++++++++", false);
		BoxParameters volumeBoxParameters = createRootNodeParameters();
		octree = new Octree(volumeBoxParameters, initialLevels);
		octree = updateOctreeLeafs(octree, volumeBoxParameters, initialLevels);
		Utils.debugNewLine(octree.toString(), false);
		
		for (int i = initialLevels + 1; i <= maxLevels; i++){	
			Utils.debugNewLine("+++++ Update octree to depth: " + i + " +++++++", true);
			constructModelAux(i);	
		}
		
		/**
		octree = OctreeVisualUtils.generateOctreeTest();
		volumeGenerator = new VolumeGenerator();
		volumeGenerator.generateTestVoxels(octree);
		**/
		System.out.println("+++++++ Model is ready ++++++++++");
	}
	
	
	
	private void createOctreeProjections(){				
		System.out.println("[ObjectRecognizerController.createOctreeProjections]");
		projectionImagesView = new ListView<String>();
		projectionImagesMap = new HashMap<String, Mat>();
		projectionImagesNames = FXCollections.observableArrayList();
		projectionImagesDescription = new HashMap<String, Integer>();

		OctreeCubeProjector octreeCubeProjector = new OctreeCubeProjector(CUBE_LENGTH_X, CUBE_LENGTH_Y, CUBE_LENGTH_Z,
				DISPLACEMENT_X, DISPLACEMENT_Y, DISPLACEMENT_Z);

		projectionImagesMap = new HashMap<String, Mat>();

		for (String imageKey : objectImagesMap.keySet()) {
			Mat binaryImage = binarizedImagesMap.get(imageKey);
			Utils.debugNewLine("Projecting octree for " + imageKey, true);

			Mat projectedImage = octreeCubeProjector.drawProjection(binaryImage, imageKey, projectionGenerator);
			projectionImagesMap.put(imageKey, projectedImage);
			projectionImagesNames.add(imageKey);
			projectionImagesDescription.put(imageKey, projectionImagesMap.size() - 1);
		}

		projectionImagesView.setItems(projectionImagesNames);
		Utils.debugNewLine(projectionImagesDescription.keySet().toString(), true);

		projectionImagesView.setCellFactory(param -> {
			ListCell<String> cell = new ListCell<String>() {
				private ImageView imageView = new ImageView();

				@Override
				public void updateItem(String name, boolean empty) {
					super.updateItem(name, empty);
					if (empty) {
						// System.out.println("Null information");
						setText(null);
						setGraphic(null);
					} else {
						// Add thumpnails here?
						System.out.println("Projected octree image name: " + name);
						imageView.setImage(Utils.mat2Image(projectionImagesMap.get(name)));
						imageView.setFitWidth(100);
						imageView.setPreserveRatio(true);
						setText(name);
						setGraphic(imageView);
						if (projectedVolumeView.getImage() == null) {
							setProjectedOctreeViewImage(imageView.getImage());
						}
					}
				}

			};

			cell.setOnMouseClicked(e -> {
                if (cell.getItem() != null) {
                	ImageView imageView = (ImageView)cell.getGraphic();
                	setProjectedOctreeViewImage(imageView.getImage());
                }
            });
			
			return cell;			
		});

		// to allow updating new elements for the list view
		projectionImagesArea.getChildren().clear();
		projectionImagesArea.getChildren().add(projectionImagesView);
	}

	/**
	 * The action triggered by pushing the button for visualizing the model from the
	 * loaded images
	 */
	@FXML
	protected void visualizeModel() {
		octree = null;
		renderModel();
		Utils.debugNewLine("+++ Model visualization is ready!", true);
	}

	public void renderModel() {
		volumeRenderer = new VolumeRenderer(cameraDistance);
		volumeRenderer.generateVolumeScene(volumeGenerator.getVoxels());
		setModelRenderingSubScene(volumeRenderer.getSubScene());
		// The octree is update with the modified version in volume generator
	}


	private void updateCameraDistance(int cameraDistance) {
		this.cameraDistance = cameraDistance;
		renderModel();
	}

	private void updateBinaryThreshold(int binaryThreshold) {
		Utils.debugNewLine("Updating binary threshold!!", true);
		//selectedBinaryThreshold = binaryThreshold;
		
		if (!thresholdImageIndex.equals("ALL_IMAGES")) {
			if (thresholdForAll.isSelected()){
				for (String imageKey: imageThresholdMap.keySet()){
					imageThresholdMap.put(imageKey, binaryThreshold);
				}
			} else {
				imageThresholdMap.put(thresholdImageIndex, binaryThreshold);				
			}
		}
	}

	public void updateCameraPosition(CameraPosition cameraPosition) {
		System.out.println("Do nothing!");
	}
	
	
	private void configValuesForExample(){
		String selection = exampleSelection.getValue();
		if(selection.equals("Charger")){
			DISPLACEMENT_X = -2;
			DISPLACEMENT_Y = (float) -0.5;
			DISPLACEMENT_Z = (float) -2;
			
			CUBE_LENGTH_X = 12;
			CUBE_LENGTH_Y = (float) 5;
			CUBE_LENGTH_Z = (float) 10;
			
			OBJECT_IMAGES_DIR = "examples/laptopCharger/";
			CALIBRATION_IMAGES_DIR = "examples/laptopCharger/calibrationImages/";

		} else if (selection.equals("Cup")){
			DISPLACEMENT_X = -2;
			DISPLACEMENT_Y = (float) -0.5;
			DISPLACEMENT_Z = (float) -2;
			
			CUBE_LENGTH_X = 12;
			CUBE_LENGTH_Y = (float) 8.5;
			CUBE_LENGTH_Z = (float) 10;

			OBJECT_IMAGES_DIR = "examples/blackCup242/";
			CALIBRATION_IMAGES_DIR = "examples/blackCup242/calibrationImages/";

		} else if (selection.equals("Hexagon")){
			DISPLACEMENT_X = -2;
			DISPLACEMENT_Y = (float) -0.5;
			DISPLACEMENT_Z = (float) -2;
			
			CUBE_LENGTH_X = 12;
			CUBE_LENGTH_Y = (float) 2.5;
			CUBE_LENGTH_Z = (float) 10;

			OBJECT_IMAGES_DIR = "examples/hexagon/";
			CALIBRATION_IMAGES_DIR = "examples/hexagon/calibrationImages/";

		} else {
			DISPLACEMENT_X = -2;
			DISPLACEMENT_Y = (float)-0.5;
			DISPLACEMENT_Z = (float) -2;
			
			CUBE_LENGTH_X = 12;
			CUBE_LENGTH_Y = (float) 5;
			CUBE_LENGTH_Z = (float) 10;

			OBJECT_IMAGES_DIR = "examples/laptopCharger/";
			CALIBRATION_IMAGES_DIR = "examples/laptopCharger/calibrationImages/";

		}

		textFieldOctreeLengthX.insertText(0, "" + CUBE_LENGTH_X);
		textFieldOctreeLengthY.insertText(0, "" + CUBE_LENGTH_Y);
		textFieldOctreeLengthZ.insertText(0, "" + CUBE_LENGTH_Z);
		
		textFieldOctreeDisplacementX.insertText(0, "" + DISPLACEMENT_X);
		textFieldOctreeDisplacementY.insertText(0, "" + DISPLACEMENT_Y);
		textFieldOctreeDisplacementZ.insertText(0, "" + DISPLACEMENT_Z);		
	}
	
	private void loadDefaultImages() {
		List<String> calibrationImageFilenames = new ArrayList<String>();
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-0.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-30.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-60.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-90.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-120.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-150.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-180.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-210.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-240.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-270.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-300.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR + "deg-330.jpg");

		calibrationImagesMap = new HashMap<String, Mat>();
		int calIndex = 0;
		for (String filename : calibrationImageFilenames) {
			Mat image = Utils.loadImage(filename);
			if (image != null) {
				calibrationImagesMap.put(calibrationIndices.get(calIndex), image);
				calIndex++;
			}
		}

		// compute calibration matrices
		projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImagesMap, true);
		System.out.println("Calibration map size: " + projectionGenerator.effectiveSize());

		// Load object images
		List<String> objectImageFilenames = new ArrayList<String>();
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-0.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-30.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-60.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-90.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-120.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-150.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-180.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-210.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-240.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-270.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-300.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR + "object-330.jpg");
		objectImagesMap = new HashMap<String, Mat>();
		calIndex = 0;
		for (String filename : objectImageFilenames) {
			Mat image = Utils.loadImage(filename);
			if (image != null){
				String imageIdentifier = calibrationIndices.get(calIndex);				
				objectImagesMap.put(imageIdentifier, image);
				objectImagesToDisplay.add(image);
				objectImagesNames.add(imageIdentifier);
				objectImagesDescription.put(imageIdentifier, calIndex);
				int threshold = (int) binaryThresholdSlider.getValue();
				imageThresholdMap.put(imageIdentifier, threshold);

				calIndex++;
			}
		}

		// Load specific values for the threshold map
		/**
		imageThresholdMap.put(calibrationIndices.get(11), 100);
		imageThresholdMap.put(calibrationIndices.get(4), 85);
		imageThresholdMap.put(calibrationIndices.get(2), 100);
		imageThresholdMap.put(calibrationIndices.get(7), 91);
		imageThresholdMap.put(calibrationIndices.get(10), 94);
		imageThresholdMap.put(calibrationIndices.get(1), 98);
		imageThresholdMap.put(calibrationIndices.get(0), 100);
		imageThresholdMap.put(calibrationIndices.get(6), 98);
		imageThresholdMap.put(calibrationIndices.get(5), 96);
		imageThresholdMap.put(calibrationIndices.get(3), 92);
		imageThresholdMap.put(calibrationIndices.get(9), 95);
		imageThresholdMap.put(calibrationIndices.get(8), 96);
		**/
		showImages();
	}


	private void loadDefaultImagesSlowTest() {
		List<String> calibrationImageFilenames = new ArrayList<String>();
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-0.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-15.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-30.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-45.jpg");		
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-60.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-75.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-90.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-105.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-120.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-135.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-150.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-165.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-180.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-195.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-210.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-240.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-255.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-270.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-285.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-300.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-315.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-330.jpg");
		calibrationImageFilenames.add(CALIBRATION_IMAGES_DIR_SLOW_TEST + "deg-345.jpg");
		
		calibrationImagesMap = new HashMap<String, Mat>();
		int calIndex = 0;
		for (String filename : calibrationImageFilenames) {
			Mat image = Utils.loadImage(filename);
			if (image != null) {
				calibrationImagesMap.put(calibrationIndices.get(calIndex), image);
				calIndex++;
			}
		}

		// compute calibration matrices
		projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImagesMap, true);
		System.out.println("Calibration map size: " + projectionGenerator.effectiveSize());

		// Load object images
		List<String> objectImageFilenames = new ArrayList<String>();
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-0.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-15.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-30.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-45.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-60.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-75.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-90.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-105.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-120.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-135.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-150.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-165.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-180.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-195.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-210.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-240.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-255.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-270.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-285.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-300.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-315.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-330.jpg");
		objectImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "object-345.jpg");
		objectImagesMap = new HashMap<String, Mat>();
		calIndex = 0;
		for (String filename : objectImageFilenames) {
			Mat image = Utils.loadImage(filename);
			if (image != null){
				String imageIdentifier = calibrationIndices.get(calIndex);				
				objectImagesMap.put(imageIdentifier, image);
				objectImagesToDisplay.add(image);
				objectImagesNames.add(imageIdentifier);
				objectImagesDescription.put(imageIdentifier, calIndex);
				int threshold = (int) binaryThresholdSlider.getValue();
				imageThresholdMap.put(imageIdentifier, threshold);

				calIndex++;
			}
		}

		// Load specific values for the threshold map
		/**
		imageThresholdMap.put(calibrationIndices.get(11), 100);
		imageThresholdMap.put(calibrationIndices.get(4), 85);
		imageThresholdMap.put(calibrationIndices.get(2), 100);
		imageThresholdMap.put(calibrationIndices.get(7), 91);
		imageThresholdMap.put(calibrationIndices.get(10), 94);
		imageThresholdMap.put(calibrationIndices.get(1), 98);
		imageThresholdMap.put(calibrationIndices.get(0), 100);
		imageThresholdMap.put(calibrationIndices.get(6), 98);
		imageThresholdMap.put(calibrationIndices.get(5), 96);
		imageThresholdMap.put(calibrationIndices.get(3), 92);
		imageThresholdMap.put(calibrationIndices.get(9), 95);
		imageThresholdMap.put(calibrationIndices.get(8), 96);
		**/
		showImages();
	}

	
	/**
	 * Button used to test the generation of the model using predefined images
	 */
	@FXML
	public void modelGenerationTest() {

		
		//clearLoadedImages();
		Utils.debugNewLine("ModelGenerationTest", true);
		
		configValuesForExample();
		loadDefaultImages();
		Utils.debugNewLine("ObjectImagesMap size: " + objectImagesMap.size(), true);
		extractSilhouettes();
		List<String> binaryImageFilenames = new ArrayList<String>();
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-0.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-30.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-60.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-90.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-120.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-150.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-180.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-210.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-240.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-270.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-300.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR + "bin-object-330.png");

		int imageFilenameIndex = 0;
		System.out.println("BinarizedImagesMap size: " + binarizedImagesMap.size());
		for (String imageKey : binarizedImagesMap.keySet()) {
			Mat binaryImage = binarizedImagesMap.get(imageKey);
			Utils.saveImage(binaryImage, binaryImageFilenames.get(imageFilenameIndex));
			imageFilenameIndex++;
		}

		constructModel();
		visualizeModel();
	}

	@FXML
	public void modelGenerationSlowTest(){
		//clearLoadedImages();
		Utils.debugNewLine("ModelGenerationsSlowTest", true);
		loadDefaultImagesSlowTest();
		Utils.debugNewLine("ObjectImagesMap size: " + objectImagesMap.size(), true);
		extractSilhouettes();
		List<String> binaryImageFilenames = new ArrayList<String>();
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-0.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-15.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-30.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-55.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-60.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-75.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-90.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-105.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-120.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-135.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-150.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-165.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-180.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-195.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-210.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-240.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-255.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-270.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-285.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-300.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-315.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-330.png");
		binaryImageFilenames.add(OBJECT_IMAGES_DIR_SLOW_TEST + "bin-object-345.png");

		int imageFilenameIndex = 0;
		System.out.println("BinarizedImagesMap size: " + binarizedImagesMap.size());
		for (String imageKey : binarizedImagesMap.keySet()) {
			Mat binaryImage = binarizedImagesMap.get(imageKey);
			Utils.saveImage(binaryImage, binaryImageFilenames.get(imageFilenameIndex));
			imageFilenameIndex++;
		}

		constructModel();
		visualizeModel();		
	}
	

	public void generateModelTest(int octreeLevels) {
		Utils.debugNewLine("generateModelTest", true);

		float centerX = (CUBE_LENGTH_X + DISPLACEMENT_X) / 2;
		float centerY = (CUBE_LENGTH_Y + DISPLACEMENT_Y) / 2;
		float centerZ = (CUBE_LENGTH_Z + DISPLACEMENT_Z) / 2;

		BoxParameters volumeBoxParameters = new BoxParameters();
		volumeBoxParameters.setSizeX(CUBE_LENGTH_X);
		volumeBoxParameters.setSizeY(CUBE_LENGTH_Y);
		volumeBoxParameters.setSizeZ(CUBE_LENGTH_Z);

		volumeBoxParameters.setCenterX(centerX);
		volumeBoxParameters.setCenterY(centerY);
		volumeBoxParameters.setCenterZ(centerZ);

		// If there is no octree, create one. Otherwise, update the current one
		if (octree == null) {
			Utils.debugNewLine("++++++++++++++++++++++++ Creating octree", true);
			octree = new Octree(volumeBoxParameters, octreeLevels);
			Utils.debugNewLine(octree.toString(), true);
		} else {
			Utils.debugNewLine("++++++++++++++++++++++++ Updating octree", true);
			octree.setBoxParameters(volumeBoxParameters);
			octree.splitNodes(octreeLevels);
		}

		// try not create another volume renderer object to recompute the octree
		// visualization

		if (octree == null) {
			Utils.debugNewLine("***************** something weird happened here", true);
		}

		volumeRenderer = new VolumeRenderer(cameraDistance);
		// instantiate the volume generator object
		// TODO: Maybe this generator could be a builder
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters, distanceArrays, invertedDistanceArrays,
				this.levels);
		volumeGenerator.setImagesForDistanceComputation(this.imagesForDistanceComputation);
		volumeGenerator.setDistanceArrays(distanceArrays);
		volumeGenerator.setInvertedDistanceArrays(invertedDistanceArrays);
		volumeGenerator.setProjectionGenerator(projectionGenerator);		
		volumeGenerator.generateOctreeVoxels(octreeLevels);
		octree = volumeGenerator.getOctree();

		volumeRenderer.generateVolumeScene(volumeGenerator.getVoxels());
		setModelRenderingSubScene(volumeRenderer.getSubScene());
		// The octree is update with the modified version in volume generator
	}

	public void generateModelMultipleOctrees(int octreeLevels) {
		Utils.debugNewLine("generateModelTest", true);

		float dx = -2;
		float dy = -1;
		float dz = -2;

		int nCubesX = 10;
		int nCubesY = 8;
		int nCubesZ = 8;

		float[] centersX = new float[nCubesX];
		centersX[0] = (float) (0.5 + dx);
		for (int i = 1; i < nCubesX; i++) {
			centersX[i] = centersX[i - 1] + 1;
		}

		float[] centersY = new float[nCubesY];
		centersY[0] = (float) (0.5 + dy);
		for (int i = 1; i < nCubesY; i++) {
			centersY[i] = centersY[i - 1] + 1;
		}

		float[] centersZ = new float[nCubesZ];
		centersZ[0] = (float) (0.5 + dz);
		for (int i = 1; i < nCubesZ; i++) {
			centersZ[i] = centersZ[i - 1] + 1;
		}

		List<BoxParameters> octreeParameters = new ArrayList<BoxParameters>();
		for (int idX = 0; idX < centersX.length; idX++) {
			for (int idY = 0; idY < centersY.length; idY++) {
				for (int idZ = 0; idZ < centersZ.length; idZ++) {
					float centerX = centersX[idX];
					float centerY = centersY[idY];
					float centerZ = centersZ[idZ];

					BoxParameters volumeBoxParameters = new BoxParameters();
					volumeBoxParameters.setSizeX(1.0);
					volumeBoxParameters.setSizeY(1.0);
					volumeBoxParameters.setSizeZ(1.0);
					volumeBoxParameters.setCenterX(centerX);
					volumeBoxParameters.setCenterY(centerY);
					volumeBoxParameters.setCenterZ(centerZ);
					octreeParameters.add(volumeBoxParameters);
				}
			}
		}

		List<OctreeTest> octrees = new ArrayList<OctreeTest>();
		// Create octree containing only the root node
		for (int i = 0; i < octreeParameters.size(); i++) {
			BoxParameters boxParameters = octreeParameters.get(i);
			Utils.debugNewLine("++++++++++++++++++++++++ Creating octree", true);
			OctreeTest octree = new OctreeTest(boxParameters, 0);
			octrees.add(octree);
		}

		for (int octreeLevel = 1; octreeLevel < octreeLevels; octreeLevel++) {
			List<Box> objectVolume = new ArrayList<Box>();
			for (int j = 0; j < octreeParameters.size(); j++) {
				BoxParameters boxParameters = octreeParameters.get(j);
				OctreeTest octree = octrees.get(j);
				// update octree using its corresponding box parameters
				Utils.debugNewLine("++++++++++++++++++++++++ Updating octree", false);
				octree.setBoxParametersTest(boxParameters);
				octree.splitNodes(octreeLevel);

				List<Box> octreeVolume = generateVolumeForOctree(octree, boxParameters, distanceArrays,
						invertedDistanceArrays, octreeLevel);
				objectVolume.addAll(octreeVolume);
			}
			volumeRenderer = new VolumeRenderer(cameraDistance);
			volumeRenderer.generateVolumeScene(objectVolume);
			setModelRenderingSubScene(volumeRenderer.getSubScene());

		}
	}

	public List<Box> generateVolumeForOctree(OctreeTest octree, BoxParameters volumeBoxParameters,
			Map<String, int[][]> distanceArrays, Map<String, int[][]> invertedDistanceArrays, int octreeHeight) {
		VolumeGeneratorTest volumeGenerator = new VolumeGeneratorTest(octree, volumeBoxParameters, distanceArrays,
				invertedDistanceArrays, octreeHeight);
		volumeGenerator.setImagesForDistanceComputation(this.imagesForDistanceComputation);
		volumeGenerator.setDistanceArrays(distanceArrays);
		volumeGenerator.setInvertedDistanceArrays(invertedDistanceArrays);
		volumeGenerator.setFieldOfView(this.fieldOfView);
		volumeGenerator.setProjectionGenerator(projectionGenerator);

		return volumeGenerator.generateOctreeVoxels();
	}




	/**
	 * Set the current stage (needed for the FileChooser modal window)
	 *
	 * @param stage
	 *            the stage
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setRootGroup(AnchorPane rootGroup) {
		this.rootGroup = rootGroup;
	}

	public void setVolumeRenderer(VolumeRenderer volumeRenderer) {
		this.volumeRenderer = volumeRenderer;
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 *
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	
	public void setCameraCalibrationSubScene(SubScene imageSubScene){
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		cameraCalibrationTab = tabPane.getTabs().get(CAMERA_CALIBRATION_TAB_ORDER);
		cameraCalibrationAnchorPane = (AnchorPane) cameraCalibrationTab.getContent();
		cameraCalibrationBorderPane = (BorderPane) cameraCalibrationAnchorPane.getChildren().get(0);
		cameraCalibrationBorderPane.setCenter(imageSubScene);
	}
	
	public void setImageSelectionSubScene(SubScene imageSubScene){
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		imageSelectionTab = tabPane.getTabs().get(IMAGE_SELECTION_TAB_ORDER);
		imageSelectionAnchorPane = (AnchorPane) imageSelectionTab.getContent();
		imageSelectionBorderPane = (BorderPane) imageSelectionAnchorPane.getChildren().get(0);
		imageSelectionBorderPane.setCenter(imageSubScene);
	}
	
	public void setModelRenderingSubScene(SubScene volumeSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		modelRenderingTab = tabPane.getTabs().get(MODEL_RENDERING_TAB_ORDER);
		modelRenderingAnchorPane = (AnchorPane) modelRenderingTab.getContent();
		modelRenderingBorderPane = (BorderPane) modelRenderingAnchorPane.getChildren().get(0);
		modelRenderingBorderPane.setCenter(volumeSubScene);
	}

	public void setSilhouettesSubScene(SubScene imageSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		silhouettesConfigTab = tabPane.getTabs().get(SILHOUETTES_CONFIG_TAB_ORDER);
		silhouettesConfigAnchorPane = (AnchorPane) silhouettesConfigTab.getContent();
		silhouettesConfigBorderPane = (BorderPane) silhouettesConfigAnchorPane.getChildren().get(0);
		silhouettesConfigBorderPane.setCenter(imageSubScene);
	}	
	
	public void setModelConfigSubScene(SubScene imageSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		modelConfigTab = tabPane.getTabs().get(MODEL_CONFIG_TAG_ORDER);
		modelConfigAnchorPane = (AnchorPane) modelConfigTab.getContent();
		modelConfigBorderPane = (BorderPane) modelConfigAnchorPane.getChildren().get(0);
		modelConfigBorderPane.setCenter(imageSubScene);
	}
	
	public void setImageOperationFrameImage(Image image) {
		imageOperationsFrame.setImage(image);
		imageOperationsFrame.setFitWidth(500);
		imageOperationsFrame.setPreserveRatio(true);
	}

	public void setProjectedOctreeViewImage(Image image) {
		projectedVolumeView.setImage(image);
		projectedVolumeView.setFitWidth(500);
		projectedVolumeView.setPreserveRatio(true);
	}
	
	public void saveCalibrationImages(String imageKey, Mat calibrationImage) {
		Utils.saveImage(calibrationImage, calibrationImagesDir + imageKey + ".jpg");
	}

}
