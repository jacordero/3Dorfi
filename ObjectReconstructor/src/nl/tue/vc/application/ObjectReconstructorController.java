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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.opencv.core.Mat;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.application.visual.ColoredBoxGenerator;
import nl.tue.vc.application.visual.IntersectionTest;
import nl.tue.vc.application.visual.LineBoxGenerator;
import nl.tue.vc.application.visual.OctreeCubeProjector;
import nl.tue.vc.application.visual.SolidBoxGenerator;
import nl.tue.vc.application.visual.VolumeGenerator;
import nl.tue.vc.application.visual.VoxelGenerator;
import nl.tue.vc.gui.SidePanelImageSelector;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.imgproc.CameraController;
import nl.tue.vc.imgproc.ConcurrentSilhouetteExtractor;
import nl.tue.vc.imgproc.SegmentedImageStruct;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.voxelengine.VolumeRenderer;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.OctreeModelGenerator;

public class ObjectReconstructorController {

	@FXML
	private VBox objectImageArea;

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

	@FXML
	private ComboBox<String> voxelTypeSelection;

	@FXML
	private ComboBox<String> octreeLevelsSelection;

	private Timer imageTimer;

	// Image used for calibration of extrinsic parameters
	private Map<String, Mat> calibrationImagesMap = new HashMap<String, Mat>();
	private int calibrationImageCounter;
	private int objectImageCounter;
	private List<String> calibrationIndices;

	private int INITIAL_OCTREE_LEVELS = 2;
	private int MAX_OCTREE_LEVELS = 8;
	private int octreeRenderingLevels;
	private VoxelGenerator voxelGenerator;

	private String thresholdImageIndex = "ALL_IMAGES";
	Map<String, Integer> imageThresholdMap = new HashMap<String, Integer>();
	private Map<String, Mat> objectImagesMap = new HashMap<String, Mat>();
	private Map<String, Mat> binarizedImagesMap = new HashMap<String, Mat>();
	private Map<String, BufferedImage> imagesForDistanceComputation = new HashMap<String, BufferedImage>();
	private Map<String, int[][]> distanceArrays = new HashMap<String, int[][]>();
	private Map<String, int[][]> invertedDistanceArrays = new HashMap<String, int[][]>();

	// the main stage
	private Stage stage;
	// the JavaFX file chooser
	private DirectoryChooser directoryChooser;

	// The rootGroup
	private AnchorPane rootGroup;
	private TabPane tabPane;

	@FXML
	private VBox cameraCalibrationImageSelectionArea;
	@FXML
	private VBox cameraCalibrationDisplayArea;
	@FXML
	private ImageView cameraCalibrationDisplayView;
	private ListView<String> cameraCalibrationImageSelectionView = new ListView<>();
	private SidePanelImageSelector cameraCalibrationImagesPanel;

	@FXML
	private VBox objectImagesSelectionArea;
	@FXML
	private VBox objectImagesDisplayArea;
	@FXML
	private ImageView objectImagesDisplayView;
	private ListView<String> objectImagesSelectionView = new ListView<>();
	private SidePanelImageSelector objectImagesPanel;

	private Tab modelRenderingTab;
	private AnchorPane modelRenderingAnchorPane;
	private BorderPane modelRenderingBorderPane;

	@FXML
	private VBox binaryImagesSelectionArea;
	@FXML
	private VBox binaryImagesDisplayArea;
	@FXML
	private ImageView binaryImagesDisplayView;
	private ListView<String> binaryImagesSelectionView = new ListView<>();
	private SidePanelImageSelector binaryImagesPanel;

	@FXML
	private VBox projectedVolumesSelectionArea;
	@FXML
	private VBox projectedVolumesDisplayArea;
	@FXML
	private ImageView projectedVolumesDisplayView;
	private ListView<String> projectedVolumesSelectionView = new ListView<>();
	private SidePanelImageSelector projectedVolumesPanel;

	private int MODEL_RENDERING_TAB_ORDER = 2;
	
	private int SIDE_PANEL_WIDTH = 140;

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

	public static int SNAPSHOT_DELAY;

	private float DISPLACEMENT_X;
	private float DISPLACEMENT_Y;
	private float DISPLACEMENT_Z;

	private float CUBE_LENGTH_X;
	private float CUBE_LENGTH_Y;
	private float CUBE_LENGTH_Z;

	private String OBJECT_IMAGES_DIR;
	private String CALIBRATION_IMAGES_DIR;
	List<String> objectImageFilenames;

	private OctreeModelGenerator octreeModelGenerator;
	
	private static final Logger logger = Logger.getLogger(ObjectReconstructor.class.getName());

	public ObjectReconstructorController() {

		cameraController = new CameraController();
		cameraFrame = new Mat();
		originalFrame = new ImageView();
		cameraCalibrationDisplayView = new ImageView();
		objectImagesDisplayView = new ImageView();
		binaryImagesDisplayView = new ImageView();
		projectedVolumesDisplayView = new ImageView();
		videoTimer = new Timer();
		videoTimerActive = false;
		cameraCalibrator = new CameraCalibrator();
		calibrationImageCounter = 0;
		objectImageCounter = 0;
		projectionGenerator = null;
		cameraDistance = 300;
		directoryChooser = new DirectoryChooser();
		calibrationIndices = Utils.PROJECTION_MATRICES_IDS;
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		MAX_OCTREE_LEVELS = appConfig.getMaxOctreeLevels();
		SNAPSHOT_DELAY = appConfig.getSnapshotDelay();
		octreeRenderingLevels = appConfig.getRenderingLevels();
	}

	// Method called to initialize all FXML variables
	@FXML
	private void initialize() {
		configureGUI();
	}

	protected void configureGUI() {

		cameraDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
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
		configValuesForExample();

		voxelTypeSelection.getItems().add("Black blocks");
		voxelTypeSelection.getItems().add("Black and gray blocks");
		voxelTypeSelection.getItems().add("Transparent blocks");
		voxelTypeSelection.getItems().add("Randomly colored blocks");
		voxelTypeSelection.setValue("Black blocks");
		voxelGenerator = new SolidBoxGenerator(false);
		
		voxelTypeSelection.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String previousValue, String newValue) {
				if (newValue.equals("Black blocks")){
					voxelGenerator = new SolidBoxGenerator(false);
				} else if (newValue.equals("Black and gray blocks")){
					voxelGenerator = new SolidBoxGenerator(true);
				} else if (newValue.equals("Transparent blocks")){
					voxelGenerator = new LineBoxGenerator();
				} else if (newValue.equals("Randomly colored blocks")){
					voxelGenerator = new ColoredBoxGenerator();
				}
				renderModel();
			}
		});
		
		
		
		for (int level = INITIAL_OCTREE_LEVELS; level <= MAX_OCTREE_LEVELS; level++){
			octreeLevelsSelection.getItems().add("Model with " + level + " levels");
			if (level == octreeRenderingLevels){
				octreeLevelsSelection.setValue("Model with " + level + " levels");
			}
		}

		octreeLevelsSelection.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String previousValue, String newValue) {
				updateOctreeRenderingLevels(newValue);
				if (octreeModelGenerator != null){
					renderModel();					
				}
			}
		});

		binaryImagesSelectionArea.getChildren().add(binaryImagesSelectionView);
		binaryImagesSelectionView.setMaxWidth(SIDE_PANEL_WIDTH);

		projectedVolumesSelectionArea.getChildren().add(projectedVolumesSelectionView);
		projectedVolumesSelectionView.setMaxWidth(SIDE_PANEL_WIDTH);

		cameraCalibrationImageSelectionArea.getChildren().add(cameraCalibrationImageSelectionView);
		cameraCalibrationImageSelectionView.setMaxWidth(SIDE_PANEL_WIDTH);

		objectImagesSelectionArea.getChildren().add(objectImagesSelectionView);
		objectImagesSelectionView.setMaxWidth(SIDE_PANEL_WIDTH);

		calibrateCameraOptions = new ToggleGroup();
		calibrateCameraFromWebcam.setToggleGroup(calibrateCameraOptions);
		calibrateCameraFromDirectory.setToggleGroup(calibrateCameraOptions);
		calibrateCameraFromDirectory.setSelected(true);

		calibrateCameraOptions.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				RadioButton radioButton = (RadioButton) calibrateCameraOptions.getSelectedToggle();
				if (radioButton != null) {
					String selection = radioButton.getText();
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
				cameraCalibrationDirectoryText.setText(CALIBRATION_IMAGES_DIR);
			}
		});

		enableCameraCalibrationWebcam.setOnAction((event) -> {
			boolean selected = enableCameraCalibrationWebcam.isSelected();
			if (selected) {
				logger.log(Level.INFO, "Starting video ...");
				calibrationSnapshotButton.setDisable(false);
				startVideo(cameraCalibrationDisplayView);
			} else {
				logger.log(Level.INFO, "Stopping video ...");
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
				objectImagesDirectoryText.setText(OBJECT_IMAGES_DIR);
			}
		});

		enableObjectWebcam.setOnAction((event) -> {
			boolean selected = enableObjectWebcam.isSelected();
			if (selected) {
				logger.log(Level.INFO, "Starting video ...");
				objectSnapshotButton.setDisable(false);
				startVideo(objectImagesDisplayView);
			} else {
				logger.log(Level.INFO, "Stopping video ...");
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
		objectImagesPanel = new SidePanelImageSelector(objectImagesSelectionView, objectImagesDisplayView,
				"objectImages");
		binaryImagesPanel = new SidePanelImageSelector(binaryImagesSelectionView, binaryImagesDisplayView,
				"binaryImages");
		projectedVolumesPanel = new SidePanelImageSelector(projectedVolumesSelectionView, projectedVolumesDisplayView,
				"projectedVolumes");
	}

	private void updateOctreeRenderingLevels(String selectedLevels) {
		if (selectedLevels.contains("2")) {
			octreeRenderingLevels = 2;
		} else if (selectedLevels.contains("3")) {
			octreeRenderingLevels = 3;
		} else if (selectedLevels.contains("4")) {
			octreeRenderingLevels = 4;
		} else if (selectedLevels.contains("5")) {
			octreeRenderingLevels = 5;
		} else if (selectedLevels.contains("6")) {
			octreeRenderingLevels = 6;
		} else if (selectedLevels.contains("7")) {
			octreeRenderingLevels = 7;
		} else {
			octreeRenderingLevels = 8;
		}
	}

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


		binaryImagesSelectionView = new ListView<String>();
		binaryImagesPanel.clearImages(binaryImagesSelectionView);

		logger.log(Level.INFO, "Silhouette extraction for " + thresholdImageIndex);
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
				logger.log(Level.WARNING, "It cannot retrieve binarized images:\n" + e.getMessage());
			}

			for (String imageKey : binarizedImagesMap.keySet()) {
				Mat binaryImage = binarizedImagesMap.get(imageKey);
				try {
					BufferedImage bufImage = IntersectionTest.Mat2BufferedImage(binaryImage);
					imagesForDistanceComputation.put(imageKey, bufImage);
				} catch (Exception e) {
					logger.log(Level.WARNING, "It cannot compute intersection images:\n" + e.getMessage());					
				}

				Image resizedImage = Utils.mat2Image(binaryImage, 500, 0, true);
				binaryImagesPanel.addImageInfo(resizedImage, imageKey, imagesForDistanceComputation.size() - 1);
			}

		} else {
			Mat imageToBinarize = objectImagesMap.get(thresholdImageIndex);
			int binaryThreshold = imageThresholdMap.get(thresholdImageIndex);
			logger.log(Level.INFO, "Extracting Image " + thresholdImageIndex + " with binary threshold " + binaryThreshold);

			ConcurrentSilhouetteExtractor cse = new ConcurrentSilhouetteExtractor(imageToBinarize, thresholdImageIndex,
					segmentationMethod, binaryThreshold);
			Mat binaryImage = cse.segment();
			binarizedImagesMap.put(thresholdImageIndex, binaryImage);

			try {
				BufferedImage bufImage = IntersectionTest.Mat2BufferedImage(binaryImage);
				imagesForDistanceComputation.put(thresholdImageIndex, bufImage);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage());
			}

			Image resizedImage = Utils.mat2Image(binaryImage, 500, 0, true);
			binaryImagesPanel.updateImageInfo(resizedImage, thresholdImageIndex);
		}

		binaryImagesPanel.showImagesForSelection(binaryImagesSelectionArea);

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

				String imageKey = calibrationIndices.get(calibrationImageCounter);
				Mat image = cameraFrame;
				Image resizedImage = Utils.mat2Image(image);
				calibrationImagesMap.put(imageKey, image);
				cameraCalibrationImagesPanel.addImageInfo(resizedImage, imageKey, calibrationImageCounter);

				calibrationImageCounter += 1;
				if (calibrationImageCounter > calibrationIndices.size() - 1) {
					calibrationImageCounter = 0;
				}
				cameraCalibrationImagesPanel.showImagesForSelection(cameraCalibrationImageSelectionArea);
			}
		};

		imageTimer = new Timer();
		imageTimer.schedule(frameGrabber, SNAPSHOT_DELAY);
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
		imageTimer.schedule(frameGrabber, SNAPSHOT_DELAY);
	}

	@FXML
	private void calibrateCameraForExtrinsicParams() {

		logger.log(Level.INFO, "Calibrating camera to find extrinsic parameters");

		if (calibrateCameraFromDirectory.isSelected()) {
			deleteCameraCalibrationImages();
			// Load calibration images
			final File folder = new File(CALIBRATION_IMAGES_DIR);
			List<String> calibrationImageFilenames = Utils.listFilesForFolder(folder);
			objectImageFilenames = new ArrayList<String>();

			int calibrationIndex = 0;
			for (String filename : calibrationImageFilenames) {
				filename = CALIBRATION_IMAGES_DIR + "/" + filename;
				String[] splittedName = filename.split("-");
				String objectImageFilename = "object-" + splittedName[1];

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
		logger.log(Level.INFO, "Loading object images from directory ");
		// TODO: replace this error message
		if (objectImageFilenames.isEmpty()) {
			logger.log(Level.INFO, "There is an error: calibrate the camera again!!");
		} else {
			deleteObjectImages();
			int calibrationIndex = 0;
			for (String filename : objectImageFilenames) {
				String fullPathFilename = OBJECT_IMAGES_DIR + "/" + filename;

				logger.log(Level.INFO, "Loading: " + fullPathFilename);
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

	/**
	 * The action triggered by pushing the button for constructing the model
	 * from the loaded images
	 */
	@FXML
	protected void constructOctreeModel() {

		// TODO: make this block of code a concurrent operation
		for (String imageKey : imagesForDistanceComputation.keySet()) {
			BufferedImage image = imagesForDistanceComputation.get(imageKey);
			int[][] sourceArray = IntersectionTest.getBinaryArray(image);
			int[][] transformedArray = IntersectionTest.computeDistanceTransform(sourceArray);
			distanceArrays.put(imageKey, transformedArray);

			int[][] invertedArray = IntersectionTest.getInvertedArray(sourceArray);
			int[][] transformedInvertedArray = IntersectionTest.computeDistanceTransform(invertedArray);
			invertedDistanceArrays.put(imageKey, transformedInvertedArray);
		}

		BoxParameters volumeBoxParameters = createRootNodeParameters();
		octreeModelGenerator = new OctreeModelGenerator(distanceArrays, invertedDistanceArrays, projectionGenerator);
		octreeModelGenerator.prepareInitialModel(volumeBoxParameters, INITIAL_OCTREE_LEVELS);
		
		
		octreeModelGenerator.generateFinalModel(INITIAL_OCTREE_LEVELS + 1, MAX_OCTREE_LEVELS);
		
	}

	private void createOctreeProjections() {

		projectedVolumesSelectionView = new ListView<String>();
		projectedVolumesPanel.clearImages(projectedVolumesSelectionView);

		OctreeCubeProjector octreeCubeProjector = new OctreeCubeProjector(CUBE_LENGTH_X, CUBE_LENGTH_Y, CUBE_LENGTH_Z,
				DISPLACEMENT_X, DISPLACEMENT_Y, DISPLACEMENT_Z);

		int projectionCounter = 0;
		for (String imageKey : objectImagesMap.keySet()) {
			Mat binaryImage = binarizedImagesMap.get(imageKey);

			Mat projectedVolume = octreeCubeProjector.drawProjection(binaryImage, imageKey, projectionGenerator);
			Image imageToDisplay = Utils.mat2Image(projectedVolume);
			projectedVolumesPanel.addImageInfo(imageToDisplay, imageKey, projectionCounter);
			projectionCounter++;
		}

		projectedVolumesPanel.showImagesForSelection(projectedVolumesSelectionArea);

	}

	/**
	 * The action triggered by pushing the button for visualizing the model from
	 * the loaded images
	 */
	@FXML
	protected void visualizeOctreeModel() {
		renderModel();
	}

	// TODO: fix bug when model rendering is selected before having a constructed model
	public void renderModel() {
		volumeGenerator = new VolumeGenerator(voxelGenerator, octreeRenderingLevels);
		volumeGenerator.generateVolume(octreeModelGenerator.getOctree(), createRootNodeParameters());

		volumeRenderer = new VolumeRenderer(cameraDistance);
		volumeRenderer.generateVolumeScene(volumeGenerator.getVoxels());
		setModelRenderingSubScene(volumeRenderer.getSubScene());
	}

	private void updateCameraDistance(int cameraDistance) {
		this.cameraDistance = cameraDistance;
		renderModel();
	}

	private void updateBinaryThreshold(int binaryThreshold) {
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
			CUBE_LENGTH_Y = (float) 8.0;
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
			CUBE_LENGTH_Y = (float) 5.1;
			CUBE_LENGTH_Z = (float) 10;

			OBJECT_IMAGES_DIR = "examples/laptopCharger/";
			CALIBRATION_IMAGES_DIR = "examples/laptopCharger/calibrationImages/";
		}
		
		textFieldOctreeLengthX.setText(Float.toString(CUBE_LENGTH_X));
		textFieldOctreeLengthY.setText(Float.toString(CUBE_LENGTH_Y));
		textFieldOctreeLengthZ.setText(Float.toString(CUBE_LENGTH_Z));
		
		textFieldOctreeDisplacementX.setText(Float.toString(DISPLACEMENT_X));
		textFieldOctreeDisplacementY.setText(Float.toString(DISPLACEMENT_Y));
		textFieldOctreeDisplacementZ.setText(Float.toString(DISPLACEMENT_Z));
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
		cameraCalibrationImageSelectionView = new ListView<String>();
		cameraCalibrationImagesPanel.clearImages(cameraCalibrationImageSelectionView);
		int calIndex = 0;
		for (String filename : calibrationImageFilenames) {
			Mat image = Utils.loadImage(filename);
			if (image != null) {
				String imageKey = calibrationIndices.get(calIndex);
				calibrationImagesMap.put(imageKey, image);
				cameraCalibrationImagesPanel.addImageInfo(Utils.mat2Image(image, 500, 0, true), imageKey, calIndex);
				calIndex++;
			}
		}
		cameraCalibrationImagesPanel.showImagesForSelection(cameraCalibrationImageSelectionArea);

		// compute calibration matrices
		projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImagesMap, true);

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

		objectImagesSelectionView = new ListView<String>();
		objectImagesPanel.clearImages(objectImagesSelectionView);
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
		configValuesForExample();

		loadDefaultImages();
		extractSilhouettes();

		constructOctreeModel();
		visualizeOctreeModel();
		long lEndTime = System.nanoTime();
		long output = (lEndTime - lStartTime) / 1000000000;
		logger.log(Level.INFO, "The model was generated in: " + output + " seconds!!!");
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

	public void setModelRenderingSubScene(SubScene volumeSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		modelRenderingTab = tabPane.getTabs().get(MODEL_RENDERING_TAB_ORDER);
		modelRenderingAnchorPane = (AnchorPane) modelRenderingTab.getContent();
		modelRenderingBorderPane = (BorderPane) modelRenderingAnchorPane.getChildren().get(0);
		modelRenderingBorderPane.setCenter(volumeSubScene);
	}

	public void saveCalibrationImages(String imageKey, Mat calibrationImage) {
		Utils.saveImage(calibrationImage, CALIBRATION_IMAGES_DIR + imageKey + ".jpg");
	}

	@FXML
	public void deleteCameraCalibrationImages() {
		calibrationImagesMap = new HashMap<String, Mat>();
		cameraCalibrationImageSelectionView = new ListView<String>();
		cameraCalibrationImagesPanel.clearImages(cameraCalibrationImageSelectionView);
		cameraCalibrationImagesPanel.showImagesForSelection(cameraCalibrationImageSelectionArea);
		cameraCalibrationImagesPanel.showSelectedImage(null);
	}

	@FXML
	public void deleteObjectImages() {
		objectImagesMap = new HashMap<String, Mat>();
		objectImagesSelectionView = new ListView<String>();
		objectImagesPanel.clearImages(objectImagesSelectionView);
		objectImagesPanel.showImagesForSelection(objectImagesSelectionArea);
		objectImagesPanel.showSelectedImage(null);
	}

}
