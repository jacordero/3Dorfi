package nl.tue.vc.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opencv.core.Mat;

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
import javafx.stage.Stage;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.application.visual.IntersectionTest;
import nl.tue.vc.gui.SidePanelImageSelector;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.imgproc.CameraController;
import nl.tue.vc.imgproc.ConcurrentSilhouetteExtractor;
import nl.tue.vc.imgproc.SegmentedImageStruct;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.voxelengine.CameraPosition;
import nl.tue.vc.voxelengine.VolumeRenderer;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.Octree;
import nl.tue.vc.model.OctreeCubeProjector;
import nl.tue.vc.model.OctreeTest;
import nl.tue.vc.model.VolumeGenerator;
import nl.tue.vc.model.VolumeGeneratorTest;

public class ObjectReconstructorController {
	// images to show in the view
	@FXML
	private ImageView originalImage;

	@FXML
	private ImageView originalImage2;

	@FXML
	private VBox objectImageArea;

	@FXML
	private ImageView transformedImage;

	@FXML
	private ImageView antitransformedImage;

	@FXML
	private Button extractButton;

	@FXML
	private Button generate3DModelButton;

	@FXML
	private Button applyButton;

	@FXML
	private Button calibrateExtrinsicParamsButton;

	@FXML
	private Button calibrationSnapshotButton;

	@FXML
	private Button objectSnapshotButton;

	@FXML
	private ImageView originalFrame;

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
	private ComboBox<String> exampleSelection;

	@FXML
	private CheckBox debugSegmentation;

	@FXML
	private CheckBox thresholdForAll;

	@FXML
	private CheckBox enableCameraCalibrationWebcam;

	@FXML
	private CheckBox enableObjectWebcam;

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
	private Button generateTestModelButton;

	private int fieldOfView;

	private Timer imageTimer;

	// Image used for calibration of extrinsic parameters
	private Map<String, Mat> calibrationImagesMap = new HashMap<String, Mat>();
	private int calibrationImageCounter;
	private int objectImageCounter;
	private List<String> calibrationIndices;

	private int MAX_OCTREE_LEVELS = 7;
	private int INITIAL_OCTREE_LEVELS = 2;
	
	private String thresholdImageIndex = "ALL_IMAGES";
	Map<String, Integer> imageThresholdMap = new HashMap<String, Integer>();
	private Map<String, Mat> objectImagesMap = new HashMap<String, Mat>();

	private ListView<String> objectImagesSelectionView = new ListView<>();
	private SidePanelImageSelector objectImagesPanel;
	
	//private List<Image> objectImagesToDisplay = new ArrayList<Image>();
	//private ObservableList<String> objectImagesNames = FXCollections.observableArrayList();
	//private Map<String, Integer> objectImagesDescription = new HashMap<>();

	// camera calibration images to display
	private ListView<String> cameraCalibrationImageSelectionView = new ListView<>();
	private SidePanelImageSelector cameraCalibrationImagesPanel;
	// private List<Image> cameraCalibrationImagesToDisplay = new
	// ArrayList<Image>();
	// private ObservableList<String> cameraCalibrationImagesNames =
	// FXCollections.observableArrayList();
	// private Map<String, Integer> cameraCalibrationImagesDescription = new
	// HashMap<>();

	// object images to display
	private Map<String, Mat> binarizedImagesMap = new HashMap<String, Mat>();
	private ListView<String> binaryImagesSelectionView = new ListView<>();
	private SidePanelImageSelector binaryImagesPanel;
	
	//private List<Mat> binaryImagesToDisplay = new ArrayList<Mat>();
	//private ObservableList<String> binaryImagesNames = FXCollections.observableArrayList();
	//private Map<String, Integer> binaryImagesDescription = new HashMap<>();

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
	// private FileChooser fileChooser;
	private DirectoryChooser directoryChooser;

	// The rootGroup
	private AnchorPane rootGroup;
	private TabPane tabPane;

	private Tab cameraCalibrationTab;
	private AnchorPane cameraCalibrationAnchorPane;
	private BorderPane cameraCalibrationBorderPane;
	@FXML
	private VBox cameraCalibrationImageSelectionArea;
	@FXML
	private VBox cameraCalibrationDisplayArea;
	@FXML
	private ImageView cameraCalibrationDisplayView;

	private Tab objectImagesTab;
	private AnchorPane objectImagesAnchorPane;
	private BorderPane objectImagesBorderPane;
	@FXML
	private VBox objectImagesSelectionArea;
	@FXML
	private VBox objectImagesDisplayArea;
	@FXML
	private ImageView objectImagesDisplayView;

	private Tab modelRenderingTab;
	private AnchorPane modelRenderingAnchorPane;
	private BorderPane modelRenderingBorderPane;

	private Tab silhouettesConfigTab;
	private AnchorPane silhouettesConfigAnchorPane;
	private BorderPane silhouettesConfigBorderPane;
	@FXML
	private VBox binaryImagesSelectionArea;
	@FXML
	private VBox binaryImagesDisplayArea;
	@FXML
	private ImageView binaryImagesDisplayView;
	
	
	private Tab modelConfigTab;
	private AnchorPane modelConfigAnchorPane;
	private BorderPane modelConfigBorderPane;


	private int CAMERA_CALIBRATION_TAB_ORDER = 0;
	private int OBJECT_IMAGES_TAB_ORDER = 1;
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

	@FXML
	private TextField cameraCalibrationDirectoryText;

	private ToggleGroup loadObjectImagesOptions;

	@FXML
	private RadioButton loadObjectImagesFromDirectory;

	@FXML
	private RadioButton loadObjectImagesFromWebcam;

	@FXML
	private Button objectImagesDirectoryButton;

	@FXML
	private TextField objectImagesDirectoryText;

	@FXML
	private Button loadObjectImagesButton;

	@FXML
	private RadioButton select3DTestModel;

	private VolumeRenderer volumeRenderer;
	private VolumeGenerator volumeGenerator;

	private CameraController cameraController;

	private CameraCalibrator cameraCalibrator;

	private ProjectionGenerator projectionGenerator;

	private Mat cameraFrame;

	private Timer videoTimer;

	private boolean videoTimerActive;

	public static int SNAPSHOT_DELAY = 250;
	public static final boolean TEST_PROJECTIONS = true;

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
	List<String> objectImageFilenames;

	private String OBJECT_IMAGES_DIR_SLOW_TEST = "examples/blackCup242/";
	private String CALIBRATION_IMAGES_DIR_SLOW_TEST = "examples/blackCup242/calibrationImages/";

	public ObjectReconstructorController() {

		cameraController = new CameraController();
		cameraFrame = new Mat();
		originalFrame = new ImageView();
		cameraCalibrationDisplayView = new ImageView();
		objectImagesDisplayView = new ImageView();
		binaryImagesDisplayView = new ImageView();
		videoTimer = new Timer();
		videoTimerActive = false;
		cameraCalibrator = new CameraCalibrator();

		DISPLACEMENT_X = -2;
		DISPLACEMENT_Y = -1;
		DISPLACEMENT_Z = (float) -2;

		CUBE_LENGTH_X = 12;
		CUBE_LENGTH_Y = (float) 8;
		CUBE_LENGTH_Z = (float) 10;

		calibrationImageCounter = 0;
		objectImageCounter = 0;
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

	private void initCalibrationIndicesSlowTest() {
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

	// Method called to initialize all FXML variables
	@FXML
	private void initialize() {
		configureGUI();
	}

	protected void configureGUI() {

		cameraDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera distance changed (newValue: " + newValue.intValue() + ")");
			updateCameraDistance(newValue.intValue());
		});

		binaryThresholdSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging,
					Boolean changing) {
				if (!changing) {
					updateBinaryThreshold((int) binaryThresholdSlider.getValue());
				} else {
					thresholdLabel.setText("Threshold: " + String.format("%.2f", binaryThresholdSlider.getValue()));
				}
			}
		});

		binaryThresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			thresholdLabel.setText("Threshold: " + String.format("%.2f", newValue));
			updateBinaryThreshold(newValue.intValue());
		});

		exampleSelection.getItems().add("Charger");
		exampleSelection.getItems().add("Cup");
		exampleSelection.getItems().add("Hexagon");
		exampleSelection.setValue("Charger");

		binaryImagesSelectionArea.getChildren().add(binaryImagesSelectionView);
		binaryImagesSelectionView.setMaxWidth(140);

		projectionImagesArea.getChildren().add(projectionImagesView);
		projectionImagesView.setMaxWidth(140);

		cameraCalibrationImageSelectionArea.getChildren().add(cameraCalibrationImageSelectionView);
		cameraCalibrationImageSelectionView.setMaxWidth(140);

		objectImagesSelectionArea.getChildren().add(objectImagesSelectionView);
		objectImagesSelectionView.setMaxWidth(140);

		calibrateCameraOptions = new ToggleGroup();
		calibrateCameraFromWebcam.setToggleGroup(calibrateCameraOptions);
		calibrateCameraFromDirectory.setToggleGroup(calibrateCameraOptions);
		calibrateCameraFromDirectory.setSelected(true);

		calibrateCameraOptions.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				RadioButton radioButton = (RadioButton) calibrateCameraOptions.getSelectedToggle();
				if (radioButton != null) {
					String selection = radioButton.getText();
					System.out.println("Radio button selected: " + selection);
					if (selection.equals("Webcam")) {
						cameraCalibrationDirectoryButton.setDisable(true);
						enableCameraCalibrationWebcam.setDisable(false);
						calibrationSnapshotButton.setDisable(false);
					} else if (selection.equals("Directory")) {
						cameraCalibrationDirectoryButton.setDisable(false);
						enableCameraCalibrationWebcam.setDisable(true);
						calibrationSnapshotButton.setDisable(true);
					}
				}
			}
		});

		cameraCalibrationDirectoryButton.setOnAction(event -> {
			File selectedDirectory = directoryChooser.showDialog(stage);
			if (selectedDirectory != null) {
				CALIBRATION_IMAGES_DIR = selectedDirectory.getAbsolutePath();
				System.out.println(CALIBRATION_IMAGES_DIR);
				cameraCalibrationDirectoryText.setText(CALIBRATION_IMAGES_DIR);
			}
		});

		enableCameraCalibrationWebcam.setOnAction((event) -> {
			boolean selected = enableCameraCalibrationWebcam.isSelected();
			if (selected) {
				System.out.println("Starting video ...");
				calibrationSnapshotButton.setDisable(false);
				startVideo(cameraCalibrationDisplayView);
			} else {
				System.out.println("Stopping video ...");
				calibrationSnapshotButton.setDisable(true);
				stopVideo();
			}
		});

		loadObjectImagesOptions = new ToggleGroup();
		loadObjectImagesFromWebcam.setToggleGroup(loadObjectImagesOptions);
		loadObjectImagesFromDirectory.setToggleGroup(loadObjectImagesOptions);
		loadObjectImagesFromDirectory.setSelected(true);

		loadObjectImagesOptions.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				RadioButton radioButton = (RadioButton) loadObjectImagesOptions.getSelectedToggle();
				if (radioButton != null) {
					String selection = radioButton.getText();
					System.out.println("Radio button selected: " + selection);
					if (selection.equals("Webcam")) {
						objectImagesDirectoryButton.setDisable(true);
						enableObjectWebcam.setDisable(false);
						objectSnapshotButton.setDisable(false);
					} else if (selection.equals("Directory")) {
						objectImagesDirectoryButton.setDisable(false);
						enableObjectWebcam.setDisable(true);
						objectSnapshotButton.setDisable(true);
					}
				}
			}
		});

		objectImagesDirectoryButton.setOnAction(event -> {
			File selectedDirectory = directoryChooser.showDialog(stage);
			if (selectedDirectory != null) {
				OBJECT_IMAGES_DIR = selectedDirectory.getAbsolutePath();
				System.out.println(OBJECT_IMAGES_DIR);
				objectImagesDirectoryText.setText(OBJECT_IMAGES_DIR);
			}
		});

		enableObjectWebcam.setOnAction((event) -> {
			boolean selected = enableObjectWebcam.isSelected();
			if (selected) {
				System.out.println("Starting video ...");
				objectSnapshotButton.setDisable(false);
				startVideo(objectImagesDisplayView);
			} else {
				System.out.println("Stopping video ...");
				objectSnapshotButton.setDisable(true);
				stopVideo();
			}
		});

		// Configure model generation buttons
		select3DTestModel.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected,
					Boolean isNowSelected) {
				if (isNowSelected) {
					generate3DModelButton.setDisable(true);
					generateTestModelButton.setDisable(false);
					exampleSelection.setDisable(false);
				} else {
					generate3DModelButton.setDisable(false);
					generateTestModelButton.setDisable(true);
					exampleSelection.setDisable(true);
				}
			}
		});

		// Some elements are disabled by default
		generateTestModelButton.setDisable(true);
		enableCameraCalibrationWebcam.setDisable(true);
		calibrationSnapshotButton.setDisable(true);
		enableObjectWebcam.setDisable(true);
		objectSnapshotButton.setDisable(true);
		exampleSelection.setDisable(true);

		// Initialize side panel image selectors
		cameraCalibrationImagesPanel = new SidePanelImageSelector(cameraCalibrationImageSelectionView, 
				cameraCalibrationDisplayView, "cameraCalibration");
		objectImagesPanel = new SidePanelImageSelector(objectImagesSelectionView, objectImagesDisplayView, "objectImages");
		binaryImagesPanel = new SidePanelImageSelector(binaryImagesSelectionView, binaryImagesDisplayView, "binaryImages");
	}

	protected void init() {
		directoryChooser = new DirectoryChooser();
	}

	
//	public void showImages() {
//
//		objectImagesSelectionView.setItems(objectImagesNames);
//		objectImagesSelectionView.setCellFactory(param -> {
//			ListCell<String> cell = new ListCell<String>() {
//				private ImageView imageView = new ImageView();
//
//				@Override
//				public void updateItem(String name, boolean empty) {
//					super.updateItem(name, empty);
//					if (empty) {
//						setText(null);
//						setGraphic(null);
//					} else {
//						int imagePosition = objectImagesDescription.get(name);
//						Utils.debugNewLine("Image name: " + name + ", position: " + imagePosition, true);
//						Utils.debugNewLine("ObjectImagesToDisplay size: " + objectImagesToDisplay.size(), true);
//						imageView.setImage(objectImagesToDisplay.get(imagePosition));
//						imageView.setFitWidth(100);
//						imageView.setPreserveRatio(true);
//						setText(name);
//						setGraphic(imageView);
//					}
//				}
//			};
//
//			cell.setOnMouseClicked(e -> {
//				if (cell.getItem() != null) {
//					ImageView imageView = (ImageView) cell.getGraphic();
//					setImageOperationFrameImage(imageView.getImage());
//					thresholdImageIndex = cell.getText();
//					Utils.debugNewLine("Click on image " + thresholdImageIndex, true);
//				}
//			});
//
//			return cell;
//		});
//	}

	/**
	 * private void showCameraCalibrationImages() {
	 * cameraCalibrationImageSelectionView.setItems(cameraCalibrationImagesNames);
	 * cameraCalibrationImageSelectionView.setCellFactory(param -> {
	 * System.out.println("setCellFactory"); ListCell<String> cell = new
	 * ListCell<String>() { private ImageView imageView = new ImageView();
	 * 
	 * @Override public void updateItem(String name, boolean empty) {
	 *           System.out.println("Updating item!!"); super.updateItem(name,
	 *           empty); if (empty) { System.out.println("Is empty!!");
	 *           setText(null); setGraphic(null); } else {
	 *           System.out.println("Name: " + name); int imagePosition =
	 *           cameraCalibrationImagesDescription.get(name);
	 *           System.out.println("Image position: " + imagePosition);
	 *           imageView.setImage(cameraCalibrationImagesToDisplay.get(imagePosition));
	 *           imageView.setFitWidth(100); imageView.setPreserveRatio(true);
	 *           setText(name); setGraphic(imageView);
	 * 
	 *           if (cameraCalibrationDisplayFrame.getImage() == null) { Mat
	 *           selectedImage = calibrationImagesMap.get(name);
	 *           setCameraCalibrationSelectedImage(Utils.mat2Image(selectedImage));
	 *           } } } };
	 * 
	 *           cell.setOnMouseClicked(e -> { if (cell.getItem() != null) {
	 *           String imageName = cell.getText(); Mat selectedImage =
	 *           calibrationImagesMap.get(imageName);
	 *           setCameraCalibrationSelectedImage(Utils.mat2Image(selectedImage));
	 *           } }); return cell; });
	 * 
	 *           cameraCalibrationImageSelectionArea.getChildren().clear();
	 *           cameraCalibrationImageSelectionArea.getChildren().add(cameraCalibrationImageSelectionView);
	 *           }
	 **/

	/**
	private void showObjectImages() {
		objectImagesSelectionView.setItems(objectImagesNames);
		objectImagesSelectionView.setCellFactory(param -> {
			ListCell<String> cell = new ListCell<String>() {
				private ImageView imageView = new ImageView();

				@Override
				public void updateItem(String name, boolean empty) {
					super.updateItem(name, empty);
					if (empty) {
						setText(null);
						setGraphic(null);
					} else {
						System.out.println("Name: " + name);
						int imagePosition = objectImagesDescription.get(name);
						imageView.setImage(objectImagesToDisplay.get(imagePosition));
						imageView.setFitWidth(100);
						imageView.setPreserveRatio(true);
						setText(name);
						setGraphic(imageView);

						if (objectImagesDisplayView.getImage() == null) {
							Mat selectedImage = objectImagesMap.get(name);
							setImageSelectionSelectedImage(Utils.mat2Image(selectedImage));
						}
					}
				}
			};

			cell.setOnMouseClicked(e -> {
				if (cell.getItem() != null) {
					String imageName = cell.getText();
					Mat selectedImage = objectImagesMap.get(imageName);
					setImageSelectionSelectedImage(Utils.mat2Image(selectedImage));
				}
			});
			return cell;
		});

		// TODO: Fix this issue using Platform.runLater
		objectImagesDisplayArea.getChildren().clear();
		objectImagesDisplayArea.getChildren().add(objectImagesView);
	}
	**/

	/**
	 * The action triggered by pushing the button for apply the dft to the
	 * loaded image
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
		
		
		binaryImagesSelectionView = new ListView<String>();
		binaryImagesPanel.clearImages(binaryImagesSelectionView);		

		Utils.debugNewLine("Silhouette extraction for " + thresholdImageIndex, true);
		String segmentationMethod = "Binarization";
		if (thresholdImageIndex.equals("ALL_IMAGES") || thresholdForAll.isSelected()) {
			binarizedImagesMap = new HashMap<String, Mat>();

			int nImages = objectImagesMap.size();
			List<CompletableFuture<SegmentedImageStruct>> futures = new ArrayList<CompletableFuture<SegmentedImageStruct>>();

			for (String imageKey : objectImagesMap.keySet()) {
				Mat image = objectImagesMap.get(imageKey);
				int binaryThreshold = imageThresholdMap.get(imageKey);
				ConcurrentSilhouetteExtractor cse = new ConcurrentSilhouetteExtractor(image, imageKey,
						segmentationMethod, binaryThreshold);
				CompletableFuture<SegmentedImageStruct> future = CompletableFuture.supplyAsync(() -> cse.call());
				futures.add(future);
			}

			// Using asynchronous method to perform image segmentation tasks
			CompletableFuture<Map<String, Mat>> combinedFuture = CompletableFuture
					.allOf(futures.toArray(new CompletableFuture[nImages]))
					.thenApply(v -> futures.stream().map(CompletableFuture::join)
							.collect(Collectors.toMap(x -> x.getImageName(), x -> x.getImage())));

			try {
				binarizedImagesMap = combinedFuture.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

			for (String imageKey : binarizedImagesMap.keySet()) {
				Mat binaryImage = binarizedImagesMap.get(imageKey);
				try {
					BufferedImage bufImage = IntersectionTest.Mat2BufferedImage(binaryImage);
					imagesForDistanceComputation.put(imageKey, bufImage);
				} catch (Exception e) {
					System.out.println("Something went really wrong!!!");
					e.printStackTrace();
				}
				
				Image resizedImage = Utils.mat2Image(binaryImage, 500, 0, true);
				binaryImagesPanel.addImageInfo(resizedImage, imageKey, imagesForDistanceComputation.size() - 1);
			}

		} else {
			Mat imageToBinarize = objectImagesMap.get(thresholdImageIndex);
			int binaryThreshold = imageThresholdMap.get(thresholdImageIndex);
			Utils.debugNewLine("Extracting Image " + thresholdImageIndex + " with binary threshold " + binaryThreshold,
					true);

			ConcurrentSilhouetteExtractor cse = new ConcurrentSilhouetteExtractor(imageToBinarize, thresholdImageIndex,
					segmentationMethod, binaryThreshold);
			Mat binaryImage = cse.segment();
			binarizedImagesMap.put(thresholdImageIndex, binaryImage);

			try {
				BufferedImage bufImage = IntersectionTest.Mat2BufferedImage(binaryImage);
				imagesForDistanceComputation.put(thresholdImageIndex, bufImage);
			} catch (Exception e) {
				System.out.println("Something went really wrong!!!");
				e.printStackTrace();
			}


			Image resizedImage = Utils.mat2Image(binaryImage, 500, 0, true);
			binaryImagesPanel.updateImageInfo(resizedImage, thresholdImageIndex);			
		}

		
		binaryImagesPanel.showImagesForSelection(binaryImagesDisplayArea);
		
//		binaryImagesSelectionView.setItems(binaryImagesNames);
//		System.out.println(binaryImagesDescription.keySet());
//
//		binaryImagesSelectionView.setCellFactory(param -> {
//			ListCell<String> cell = new ListCell<String>() {
//				private ImageView imageView = new ImageView();
//
//				@Override
//				public void updateItem(String name, boolean empty) {
//					super.updateItem(name, empty);
//					if (empty) {
//						// System.out.println("Null information");
//						setText(null);
//						setGraphic(null);
//					} else {
//						// Add thumpnails here?
//						System.out.println("Binary image name: " + name);
//						imageView.setImage(Utils.mat2Image(binarizedImagesMap.get(name)));
//						imageView.setFitWidth(100);
//						imageView.setPreserveRatio(true);
//						setText(name);
//						setGraphic(imageView);
//						if (binaryImagesDisplayView.getImage() == null) {
//							setImageOperationFrameImage(imageView.getImage());
//						}
//					}
//				}
//
//			};
//
//			cell.setOnMouseClicked(e -> {
//				if (cell.getItem() != null) {
//					ImageView imageView = (ImageView) cell.getGraphic();
//					setImageOperationFrameImage(imageView.getImage());
//					thresholdImageIndex = cell.getText();
//					int binaryThreshold = imageThresholdMap.get(thresholdImageIndex);
//					binaryThresholdSlider.setValue(binaryThreshold);
//					Utils.debugNewLine("Binary image name " + cell.getText(), true);
//				}
//			});
//
//			return cell;
//		});

		// to allow updating new elements for the list view
//		binaryImagesSelectionArea.getChildren().clear();
//		binaryImagesSelectionArea.getChildren().add(binaryImagesSelectionView);

		
		
		createOctreeProjections();
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
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {

	}

	// @FXML
	protected void startVideo(ImageView displayFrame) {

		cameraController.startCamera();
		TimerTask frameGrabber = new TimerTask() {

			@Override
			public void run() {
				cameraFrame = cameraController.grabFrame();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (!cameraFrame.empty()) {
							// cameraFrameView.setImage(Utils.mat2Image(cameraFrame));
							// resize to image with width of 500 while
							// preserving its ratio
							displayFrame.setImage(Utils.mat2Image(cameraFrame));
							displayFrame.setFitWidth(500);
							displayFrame.setPreserveRatio(true);
						}
					}
				});
			}
		};

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
	protected void takeCalibrationSnapshot() {

		// take snapshots for the camera calibration process
		TimerTask frameGrabber;
		frameGrabber = new TimerTask() {
			@Override
			public void run() {

				// cameraController.startCamera();
				String imageKey = calibrationIndices.get(calibrationImageCounter);
				Mat image = cameraFrame;
				Image resizedImage = Utils.mat2Image(image);
				calibrationImagesMap.put(imageKey, image);
				cameraCalibrationImagesPanel.addImageInfo(resizedImage, imageKey, calibrationImageCounter);

				calibrationImageCounter += 1;
				if (calibrationImageCounter > calibrationIndices.size() - 1) {
					calibrationImageCounter = 0;
				}
				// TODO: show the image corresponding to the main frame
				cameraCalibrationImagesPanel.showImagesForSelection(cameraCalibrationImageSelectionArea);
				// cameraCalibrationImageSelectionArea.getChildren().clear();
				// cameraCalibrationImageSelectionArea.getChildren().add(cameraCalibrationImageSelectionView);

				// showCameraCalibrationImages();
			}
		};

		imageTimer = new Timer();
		imageTimer.schedule(frameGrabber, 250);
	}

	/**
	 * Take a snapshot to be used for the calibration process
	 */
	@FXML
	protected void takeObjectSnapshot() {

		// take snapshots for the camera calibration process
		TimerTask frameGrabber;
		frameGrabber = new TimerTask() {
			@Override
			public void run() {

				// cameraController.startCamera();
				String imageKey = calibrationIndices.get(objectImageCounter);
				Mat image = cameraFrame;
				Image resizedImage = Utils.mat2Image(image, 100, 0, true);
				objectImagesMap.put(imageKey, image);
				
				objectImagesPanel.addImageInfo(resizedImage, imageKey, objectImageCounter);

				objectImageCounter += 1;
				if (objectImageCounter > calibrationIndices.size() - 1) {
					objectImageCounter = 0;
				}
				
				objectImagesPanel.showImagesForSelection(objectImagesSelectionArea);
			}
		};

		imageTimer = new Timer();
		imageTimer.schedule(frameGrabber, 250);
	}

	@FXML
	private void calibrateCameraForExtrinsicParams() {

		Utils.debugNewLine("*** Calibrating camera to find extrinsic parameters ***", true);
		Utils.debugNewLine("Calibration images Map size: " + calibrationImagesMap.size(), true);

		calibrationImagesMap = new HashMap<String, Mat>();
		cameraCalibrationImageSelectionView = new ListView<String>();
		cameraCalibrationImagesPanel.clearImages(cameraCalibrationImageSelectionView);

		// cameraCalibrationImageSelectionView = new ListView<String>();
		// cameraCalibrationImagesNames = FXCollections.observableArrayList();
		// cameraCalibrationImagesDescription = new HashMap<String, Integer>();

		if (calibrateCameraFromDirectory.isSelected()) {
			Utils.debugNewLine("*** Load calibration images ***", true);
			// Load calibration images
			final File folder = new File(CALIBRATION_IMAGES_DIR);
			List<String> calibrationImageFilenames = Utils.listFilesForFolder(folder);
			objectImageFilenames = new ArrayList<String>();

			int calibrationIndex = 0;
			for (String filename : calibrationImageFilenames) {
				filename = CALIBRATION_IMAGES_DIR + "/" + filename;
				String[] splittedName = filename.split("-");
				String objectImageFilename = "object-" + splittedName[1];

				System.out.println("Calibration image filename: " + filename);
				System.out.println("Object image filename: " + objectImageFilename);
				objectImageFilenames.add(objectImageFilename);
				Mat image = Utils.loadImage(filename);

				if (image != null) {
					String imageIdentifier = calibrationIndices.get(calibrationIndex);
					calibrationImagesMap.put(calibrationIndices.get(calibrationIndex), image);
					Image scrollingImage = Utils.mat2Image(image, 500, 0, true);
					cameraCalibrationImagesPanel.addImageInfo(scrollingImage, imageIdentifier, calibrationIndex);
					calibrationIndex++;
				}
			}
		}

		projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImagesMap, true);

		// TODO: move this part to another class
		cameraCalibrationImagesPanel.showImagesForSelection(cameraCalibrationImageSelectionArea);
	}

	@FXML
	private void loadObjectImagesFromDirectory() {
		Utils.debugNewLine("*** Loading object images from directory ***", true);
		// TODO: replace this error message
		if (objectImageFilenames.isEmpty()) {
			System.out.println("There is an error: calibrate the camera again!!");
		} else {
			objectImagesSelectionView = new ListView<String>();
			objectImagesPanel.clearImages(objectImagesSelectionView);
			int calibrationIndex = 0;
			for (String filename : objectImageFilenames) {
				String fullPathFilename = OBJECT_IMAGES_DIR + "/" + filename;

				System.out.println("Loading: " + fullPathFilename);
				Mat image = Utils.loadImage(fullPathFilename);
				if (image != null) {
					String imageIdentifier = calibrationIndices.get(calibrationIndex);
					objectImagesMap.put(calibrationIndices.get(calibrationIndex), image);
					Image resizedImage = Utils.mat2Image(image, 500, 0, true);
					objectImagesPanel.addImageInfo(resizedImage, imageIdentifier, calibrationIndex);					
					int threshold = (int) binaryThresholdSlider.getValue();
					imageThresholdMap.put(calibrationIndices.get(calibrationIndex), threshold);
					calibrationIndex++;
				}
			}
		}
		
		objectImagesPanel.showImagesForSelection(objectImagesSelectionArea);
	}

	public BoxParameters createRootNodeParameters() {
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

	public void constructOctreeModelAux(int octreeLevels) {
		Utils.debugNewLine("ObjectRecognizerController.constructModelAux(" + octreeLevels + ")", true);

		// If there is no octree, create one. Otherwise, update the current one
		Utils.debugNewLine("++++++++++++++++++++++++ Updating octree", false);
		BoxParameters volumeBoxParameters = createRootNodeParameters();
		octree.setBoxParameters(volumeBoxParameters);
		octree.splitNodes(octreeLevels);

		if (octree == null) {
			Utils.debugNewLine("***************** something weird happened here", true);
		}

		// TODO: Maybe this generator could be a builder
		octree = updateOctreeLeafs(octree, volumeBoxParameters, octreeLevels);// volumeGenerator.getOctree();
	}

	public Octree updateOctreeLeafs(Octree octree, BoxParameters volumeBoxParameters, int octreeDepth) {
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters, distanceArrays, invertedDistanceArrays,
				octreeDepth);
		volumeGenerator.setImagesForDistanceComputation(this.imagesForDistanceComputation);
		volumeGenerator.setDistanceArrays(distanceArrays);
		volumeGenerator.setInvertedDistanceArrays(invertedDistanceArrays);
		volumeGenerator.setProjectionGenerator(projectionGenerator);
		volumeGenerator.generateOctreeVoxels(octreeDepth);
		return volumeGenerator.getOctree();
	}

	/**
	 * The action triggered by pushing the button for constructing the model
	 * from the loaded images
	 */
	@FXML
	protected void constructOctreeModel() {
		// System.out.println("height = " +
		// this.processedExtractedImage.size().height +
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

//		int initialLevels = 2;
//		int maxLevels = 8;

		Utils.debugNewLine("+++++++ Creating octree ++++++++++++", false);
		BoxParameters volumeBoxParameters = createRootNodeParameters();
		octree = new Octree(volumeBoxParameters, INITIAL_OCTREE_LEVELS);
		octree = updateOctreeLeafs(octree, volumeBoxParameters, INITIAL_OCTREE_LEVELS);
		Utils.debugNewLine(octree.toString(), false);

		for (int i = INITIAL_OCTREE_LEVELS + 1; i <= MAX_OCTREE_LEVELS; i++) {
			Utils.debugNewLine("+++++ Update octree to depth: " + i + " +++++++", true);
			constructOctreeModelAux(i);
		}

		/**
		 * octree = OctreeVisualUtils.generateOctreeTest(); volumeGenerator =
		 * new VolumeGenerator(); volumeGenerator.generateTestVoxels(octree);
		 **/
		System.out.println("+++++++ Model is ready ++++++++++");
	}

	private void createOctreeProjections() {
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
						setText(null);
						setGraphic(null);
					} else {
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
					ImageView imageView = (ImageView) cell.getGraphic();
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
	 * The action triggered by pushing the button for visualizing the model from
	 * the loaded images
	 */
	@FXML
	protected void visualizeOctreeModel() {
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
		// selectedBinaryThreshold = binaryThreshold;

		if (!thresholdImageIndex.equals("ALL_IMAGES")) {
			if (thresholdForAll.isSelected()) {
				for (String imageKey : imageThresholdMap.keySet()) {
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

	private void configValuesForExample() {
		String selection = exampleSelection.getValue();
		if (selection.equals("Charger")) {
			DISPLACEMENT_X = -2;
			DISPLACEMENT_Y = (float) -0.5;
			DISPLACEMENT_Z = (float) -2;

			CUBE_LENGTH_X = 12;
			CUBE_LENGTH_Y = (float) 5;
			CUBE_LENGTH_Z = (float) 10;

			OBJECT_IMAGES_DIR = "examples/laptopCharger/";
			CALIBRATION_IMAGES_DIR = "examples/laptopCharger/calibrationImages/";

		} else if (selection.equals("Cup")) {
			DISPLACEMENT_X = -2;
			DISPLACEMENT_Y = (float) -0.5;
			DISPLACEMENT_Z = (float) -2;

			CUBE_LENGTH_X = 12;
			CUBE_LENGTH_Y = (float) 8.5;
			CUBE_LENGTH_Z = (float) 10;

			OBJECT_IMAGES_DIR = "examples/blackCup242/";
			CALIBRATION_IMAGES_DIR = "examples/blackCup242/calibrationImages/";

		} else if (selection.equals("Hexagon")) {
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
			DISPLACEMENT_Y = (float) -0.5;
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
		objectImageFilenames = new ArrayList<String>();
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
			if (image != null) {
				String imageIdentifier = calibrationIndices.get(calIndex);
				objectImagesMap.put(imageIdentifier, image);
				Image resizedImage = Utils.mat2Image(image, 500, 0, true);

				objectImagesPanel.addImageInfo(resizedImage, imageIdentifier, calIndex);
				int threshold = (int) binaryThresholdSlider.getValue();
				imageThresholdMap.put(imageIdentifier, threshold);

				calIndex++;
			}
		}
		objectImagesPanel.showImagesForSelection(objectImagesSelectionArea);
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
			if (image != null) {
				String imageIdentifier = calibrationIndices.get(calIndex);
				objectImagesMap.put(imageIdentifier, image);
				Image resizedImage = Utils.mat2Image(image, 500, 0, true);
				
				objectImagesPanel.addImageInfo(resizedImage, imageIdentifier, calIndex);
				int threshold = (int) binaryThresholdSlider.getValue();
				imageThresholdMap.put(imageIdentifier, threshold);

				calIndex++;
			}
		}

		objectImagesPanel.showImagesForSelection(cameraCalibrationImageSelectionArea);
	}

	@FXML
	public void generate3DModel() {
		extractSilhouettes();
		constructOctreeModel();
		visualizeOctreeModel();
	}

	/**
	 * Button used to test the generation of the model using predefined images
	 */
	@FXML
	public void generateTestModel() {

		long lStartTime = System.nanoTime();
		Utils.debugNewLine("generateTestModel", true);
		configValuesForExample();

		loadDefaultImages();
		Utils.debugNewLine("ObjectImagesMap size: " + objectImagesMap.size(), true);
		extractSilhouettes();

		constructOctreeModel();
		visualizeOctreeModel();
		long lEndTime = System.nanoTime();
		long output = (lEndTime - lStartTime) / 1000000000;
		System.out.println("The model was generated in: " + output + " seconds!!!");
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

	public void setCameraCalibrationSubScene(SubScene imageSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		cameraCalibrationTab = tabPane.getTabs().get(CAMERA_CALIBRATION_TAB_ORDER);
		cameraCalibrationAnchorPane = (AnchorPane) cameraCalibrationTab.getContent();
		cameraCalibrationBorderPane = (BorderPane) cameraCalibrationAnchorPane.getChildren().get(0);
		cameraCalibrationBorderPane.setCenter(imageSubScene);
	}

	public void setImageSelectionSubScene(SubScene imageSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		objectImagesTab = tabPane.getTabs().get(OBJECT_IMAGES_TAB_ORDER);
		objectImagesAnchorPane = (AnchorPane) objectImagesTab.getContent();
		objectImagesBorderPane = (BorderPane) objectImagesAnchorPane.getChildren().get(0);
		objectImagesBorderPane.setCenter(imageSubScene);
	}

	public void setImageSelectionSelectedImage(Image image) {
		objectImagesDisplayView.setImage(image);
		objectImagesDisplayView.setFitWidth(500);
		objectImagesDisplayView.setPreserveRatio(true);
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

	/**
	public void setImageOperationFrameImage(Image image) {
		binaryImagesDisplayView.setImage(image);
		binaryImagesDisplayView.setFitWidth(500);
		binaryImagesDisplayView.setPreserveRatio(true);
	}
	**/

	public void setProjectedOctreeViewImage(Image image) {
		projectedVolumeView.setImage(image);
		projectedVolumeView.setFitWidth(500);
		projectedVolumeView.setPreserveRatio(true);
	}

	public void saveCalibrationImages(String imageKey, Mat calibrationImage) {
		Utils.saveImage(calibrationImage, calibrationImagesDir + imageKey + ".jpg");
	}

}
