package nl.tue.vc.application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
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
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.tue.vc.application.utils.OctreeVisualUtils;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.application.visual.IntersectionTest;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.imgproc.CameraController;
import nl.tue.vc.imgproc.SilhouetteExtractor;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.voxelengine.CameraPosition;
import nl.tue.vc.voxelengine.VolumeRenderer;
import nl.tue.vc.model.BoxParameters;
import nl.tue.vc.model.Octree;
import nl.tue.vc.model.OctreeCubeProjector;
import nl.tue.vc.model.VolumeGenerator;
import nl.tue.vc.model.test.OctreeTest;
import nl.tue.vc.model.test.VolumeGeneratorTest;

/**
 * The controller associated to the only view of our application. The
 * application logic is implemented here. It handles the button for opening an
 * image and perform all the operation related to the ObjectRecognizer
 * transformation and antitransformation.
 *
 */
public class ObjectRecognizerController {
	// images to show in the view
	@FXML
	private ImageView originalImage;
	// images to show in the view
	@FXML
	private ImageView originalImage2;
	// a FXML button for performing the antitransformation
	@FXML
	private VBox objectImageArea;

	@FXML
	private VBox imageProcessingArea;

	@FXML
	private ImageView transformedImage;
	@FXML
	private ImageView antitransformedImage;
	// a FXML button for performing the transformation
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

	// the FXML area for showing the current frame (before calibration)
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
	private CheckBox debugSegmentation;

	@FXML
	private CheckBox turnOnCamera;

	@FXML
	private CheckBox enableCameraCalibration;

	@FXML
	private ImageView cameraFrameView;

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

	private int fieldOfView;

	// old timer
	private Timer calibrationTimer;
	private boolean calibrationTimerActive;
	// a timer for acquiring the video stream

	private Timer imageTimer;
	// the OpenCV object that performs the video capture
	private VideoCapture calibrationCapture;
	// a flag to change the button behavior
	private boolean cameraActive;
	// the saved chessboard image
	private Mat savedImage, processedExtractedImage;
	// the calibrated camera frame
	private Image undistoredImage, CamStream;

	// Image used for calibration of extrinsic parameters
	private Map<String, Mat> calibrationImagesMap = new HashMap<String, Mat>();
	private int calibrationImageCounter;
	private List<String> calibrationIndices;

	private Mat calibrationImage = null;
	// various variables needed for the calibration
	private List<Mat> imagePoints;
	private List<Mat> objectPoints;
	private MatOfPoint3f obj;
	private MatOfPoint2f imageCorners;
	private int boardsNumber;
	private int numCornersHor;
	private int numCornersVer;
	private int successes;
	private Mat intrinsic;
	private Mat distCoeffs;
	private boolean isCalibrated;

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
	// support variables
	private Mat image;
	private List<Mat> planes;
	private double calibrationResult = 0;

	// The rootGroup
	private AnchorPane rootGroup;
	private TabPane tabPane;
	private BorderPane displayBorderPane;
	private Tab mainTab;
	private AnchorPane mainTabAnchor;
	private BorderPane renderingDisplayBorderPane;
	private Tab renderingTab;
	private AnchorPane renderingTabAnchor;

	private VolumeRenderer volumeRenderer;
	private VolumeGenerator volumeGenerator;

	private SilhouetteExtractor silhouetteExtractor;

	private CameraController cameraController;

	private CameraCalibrator cameraCalibrator;

	private ProjectionGenerator projectionGenerator;

	private Mat cameraFrame;

	private Timer videoTimer;

	private boolean videoTimerActive;

	private Image defaultVideoImage;

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

	private String DEFAULT_IMAGES_DIR = "images/multiOctreesTest/";

	public ObjectRecognizerController() {

		silhouetteExtractor = new SilhouetteExtractor();
		cameraController = new CameraController();
		cameraFrame = new Mat();
		cameraFrameView = new ImageView();
		originalFrame = new ImageView();
		videoTimer = new Timer();
		videoTimerActive = false;
		calibrationTimer = new Timer();
		calibrationTimerActive = false;
		cameraCalibrator = new CameraCalibrator();
		
		DISPLACEMENT_X = -2;
		DISPLACEMENT_Y = -1;
		DISPLACEMENT_Z = -2;
		
		CUBE_LENGTH_X = 10;
		CUBE_LENGTH_Y = (float) 6.5;
		CUBE_LENGTH_Z = 8;
		
		this.levels = 0;// Integer.parseInt(this.levelsField.getText());

		calibrationImageCounter = 0;
		initCalibrationIndices();
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

	@FXML
	private void initialize() {

		cameraDistanceSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera distance changed (newValue: " + newValue.intValue() + ")");
			updateCameraDistance(newValue.intValue());
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
	}

	/**
	 * Init the needed variables
	 */
	protected void init() {
		this.fileChooser = new FileChooser();
		this.image = new Mat();
		this.planes = new ArrayList<>();
		this.calibrationCapture = new VideoCapture();
		this.cameraActive = false;
		this.obj = new MatOfPoint3f();
		this.imageCorners = new MatOfPoint2f();
		this.savedImage = new Mat();
		this.processedExtractedImage = new Mat();
		this.undistoredImage = null;
		this.imagePoints = new ArrayList<>();
		this.objectPoints = new ArrayList<>();
		this.intrinsic = new Mat(3, 3, CvType.CV_32FC1);
		this.distCoeffs = new Mat();
		this.successes = 0;
		this.isCalibrated = false;
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
						// load the images into the listview
						String imgName = file.getName().split("\\.")[0];
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
						Utils.debugNewLine("Image name: ", true);
						int imagePosition = objectImagesDescription.get(name);
						imageView.setImage(Utils.mat2Image(objectImagesToDisplay.get(imagePosition)));
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
					Utils.debugNewLine("Click on image " + thresholdImageIndex, true);
				}
			});

			return cell;
		});

		objectImagesView.setMaxWidth(140);
		objectImagesView.refresh();
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
		if (thresholdImageIndex.equals("ALL_IMAGES")) {
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
	}

	/**
	 * Store all the chessboard properties, update the UI and prepare other needed
	 * variables
	 */
	@FXML
	protected void updateSettings() {
		Utils.debugNewLine("[updateSettings] called!", true);
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
				calibrationTimerActive = true;

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
					int imagePosition = objectImagesMap.size();
					String imageKey = calibrationIndices.get(imagePosition);
					objectImagesMap.put(imageKey, cameraController.grabFrame());
					objectImagesNames.add(imageKey);
					objectImagesDescription.put(imageKey, objectImagesMap.size() - 1);
					showImages();
				}
			};

		}

		imageTimer = new Timer();
		imageTimer.schedule(frameGrabber, 250);
	}

	/**
	 * Find and draws the points needed for the calibration on the chessboard
	 *
	 * @param frame
	 *            the current frame
	 * @return the current number of successfully identified chessboards as an int
	 */
	private void findAndDrawPoints(Mat frame) {

		System.out.println("*** findAndDrawPoints!!!");
		// init
		Mat grayImage = new Mat();

		// I would perform this operation only before starting the calibration
		// process
		if (this.successes < this.boardsNumber) {
			System.out.println("**** Successes < boardsNumber!!!");
			// convert the frame in gray scale
			Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);
			// the size of the chessboard
			Size boardSize = new Size(this.numCornersHor, this.numCornersVer);
			// look for the inner chessboard corners
			boolean found = Calib3d.findChessboardCorners(grayImage, boardSize, imageCorners,
					Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
			// all the required corners have been found...
			if (found) {
				System.out.println("**** found!!!");
				// optimization
				TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
				Imgproc.cornerSubPix(grayImage, imageCorners, new Size(11, 11), new Size(-1, -1), term);
				// save the current frame for further elaborations
				grayImage.copyTo(this.savedImage);
				// show the chessboard inner corners on screen
				Calib3d.drawChessboardCorners(frame, boardSize, imageCorners, found);

				// enable the option for taking a snapshot
				this.snapshotButton.setDisable(false);
				// take the snapshot
				// takeSnapshot();
			} else {
				this.snapshotButton.setDisable(true);
			}
		}
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

	/**
	 * The effective camera calibration, to be performed once in the program
	 * execution
	 */
	private void calibrateCamera() {
		// init needed variables according to OpenCV docs
		List<Mat> rvecs = new ArrayList<>();
		List<Mat> tvecs = new ArrayList<>();
		intrinsic.put(0, 0, 1);
		intrinsic.put(1, 1, 1);
		// calibrate!
		this.calibrationResult = Calib3d.calibrateCamera(objectPoints, imagePoints, savedImage.size(), intrinsic,
				distCoeffs, rvecs, tvecs);
		System.out.println("Calibration result = " + this.calibrationResult);

		this.isCalibrated = true;

		// you cannot take other snapshot, at this point...
		this.snapshotButton.setDisable(true);
	}

	@FXML
	protected void clearLoadedImages() {
		objectImagesNames.clear();
		objectImagesMap.clear();
		objectImagesDescription.clear();
	}

	public void constructModelAux(int octreeLevels) {
		Utils.debugNewLine("ObjectRecognizerController.constructModelAux(" + octreeLevels +")", true);

		CameraPosition cameraPosition = new CameraPosition();
		cameraPosition.positionAxisX = 0;
		cameraPosition.positionAxisY = 0;
		cameraPosition.positionAxisZ = 0;

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
			Utils.debugNewLine("++++++++++++++++++++++++ Creating octree", false);
			octree = new Octree(volumeBoxParameters, octreeLevels);
			Utils.debugNewLine(octree.toString(), false);
		} else {
			Utils.debugNewLine("++++++++++++++++++++++++ Updating octree", false);
			octree.setBoxParameters(volumeBoxParameters);
			octree.splitNodes(octreeLevels);
		}

		if (octree == null) {
			Utils.debugNewLine("***************** something weird happened here", true);
		}

		// TODO: Maybe this generator could be a builder 
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters, distanceArrays,
				invertedDistanceArrays, octreeLevels);
		volumeGenerator.setImagesForDistanceComputation(this.imagesForDistanceComputation);
		volumeGenerator.setDistanceArrays(distanceArrays);
		volumeGenerator.setInvertedDistanceArrays(invertedDistanceArrays);
		volumeGenerator.setProjectionGenerator(projectionGenerator);		
		volumeGenerator.generateOctreeVoxels();
		octree = volumeGenerator.getOctree();
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
		createOctreeProjections();
		
		
		int maxLevels = 7;
		for (int i = 0; i < maxLevels; i++){	
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

		Utils.debugNewLine("Silhouette extraction for " + thresholdImageIndex, true);
		if (thresholdImageIndex.equals("ALL_IMAGES")) {
			projectionImagesMap = new HashMap<String, Mat>();

			for (String imageKey : objectImagesMap.keySet()) {
				Mat binaryImage = binarizedImagesMap.get(imageKey);
				Utils.debugNewLine("Projecting octree for " + imageKey, true);

				Mat projectedImage = octreeCubeProjector.drawProjection(binaryImage, imageKey, projectionGenerator);
				projectionImagesMap.put(imageKey, projectedImage);
				projectionImagesNames.add(imageKey);
				projectionImagesDescription.put(imageKey, projectionImagesMap.size() - 1);
			}
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
		setVolumeSubScene(volumeRenderer.getSubScene());
		// The octree is update with the modified version in volume generator
	}


	private void updateCameraDistance(int cameraDistance) {
		this.cameraDistance = cameraDistance;
		renderModel();
	}

	private void updateBinaryThreshold(int binaryThreshold) {
		if (!thresholdImageIndex.equals("ALL_IMAGES")) {
			imageThresholdMap.put(thresholdImageIndex, binaryThreshold);
		}
	}

	public void updateCameraPosition(CameraPosition cameraPosition) {
		System.out.println("Do nothing!");
	}

	private void loadDefaultImages() {
		List<String> calibrationImageFilenames = new ArrayList<String>();
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-0.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-30.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-60.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-90.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-120.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-150.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-180.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-210.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-240.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-270.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-300.jpg");
		calibrationImageFilenames.add(DEFAULT_IMAGES_DIR + "chessboard-330.jpg");

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
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-0.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-30.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-60.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-90.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-120.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-150.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-180.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-210.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-240.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-270.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-300.jpg");
		objectImageFilenames.add(DEFAULT_IMAGES_DIR + "object-330.jpg");
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

		showImages();
	}

	/**
	 * Button used to test the generation of the model using predefined images
	 */
	@FXML
	public void modelGenerationTest() {

		Utils.debugNewLine("ModelGenerationTest", true);
		loadDefaultImages();
		Utils.debugNewLine("ObjectImagesMap size: " + objectImagesMap.size(), true);
		extractSilhouettes();
		List<String> binaryImageFilenames = new ArrayList<String>();
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-0.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-30.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-60.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-90.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-120.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-150.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-180.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-210.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-240.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-270.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-300.png");
		binaryImageFilenames.add(DEFAULT_IMAGES_DIR + "bin-object-330.png");

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

	private Map<String, Mat> extractSilhouettesTest(Map<String, Mat> images) {
		Utils.debugNewLine("extractSilhouettesTest", true);

		Map<String, Mat> binarizedImages = new HashMap<String, Mat>();

		for (String imageKey : images.keySet()) {
			Mat image = images.get(imageKey);
			silhouetteExtractor.extract(image, segmentationAlgorithm.getValue());
			binarizedImagesMap.put(imageKey, silhouetteExtractor.getSegmentedImage());

			try {
				imagesForDistanceComputation.put(imageKey,
						IntersectionTest.Mat2BufferedImage(silhouetteExtractor.getSegmentedImage()));
			} catch (Exception e) {
				System.out.println("Something went really wrong!!!");
				e.printStackTrace();
			}

			binarizedImages.put(imageKey, silhouetteExtractor.getSegmentedImage());
		}

		return binarizedImages;

	}

	/**
	 * This method could be executed using multiple threads
	 */
	private void computeDistanceArraysTest() {
		Utils.debugNewLine("computeDistanceArraysTest", true);

		for (String imageKey : imagesForDistanceComputation.keySet()) {
			// System.out.println("Converted mat width = " + convertedMat.getWidth() + ",
			// height = " + convertedMat.getHeight());
			BufferedImage image = imagesForDistanceComputation.get(imageKey);
			int[][] sourceArray = IntersectionTest.getBinaryArray(image);
			int[][] transformedArray = IntersectionTest.computeDistanceTransform(sourceArray);
			distanceArrays.put(imageKey, transformedArray);

			int[][] invertedArray = IntersectionTest.getInvertedArray(sourceArray);
			int[][] transformedInvertedArray = IntersectionTest.computeDistanceTransform(invertedArray);
			invertedDistanceArrays.put(imageKey, transformedInvertedArray);
		}
	}

	public void generateModelTest(int octreeLevels) {
		Utils.debugNewLine("generateModelTest", true);

		CameraPosition cameraPosition = new CameraPosition();
		cameraPosition.positionAxisX = 0;
		cameraPosition.positionAxisY = 0;
		cameraPosition.positionAxisZ = 0;

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
		volumeGenerator.generateOctreeVoxels();
		octree = volumeGenerator.getOctree();

		volumeRenderer.generateVolumeScene(volumeGenerator.getVoxels());
		setVolumeSubScene(volumeRenderer.getSubScene());
		// The octree is update with the modified version in volume generator
	}

	public void generateModelMultipleOctrees(int octreeLevels) {
		Utils.debugNewLine("generateModelTest", true);

		CameraPosition cameraPosition = new CameraPosition();
		cameraPosition.positionAxisX = 0;
		cameraPosition.positionAxisY = 0;
		cameraPosition.positionAxisZ = 0;

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
			setVolumeSubScene(volumeRenderer.getSubScene());

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
	 * Optimize the image dimensions
	 *
	 * @param image
	 *            the {@link Mat} to optimize
	 * @return the image whose dimensions have been optimized
	 */
	private Mat optimizeImageDim(Mat image) {
		// init
		Mat padded = new Mat();
		// get the optimal rows size for dft
		int addPixelRows = Core.getOptimalDFTSize(image.rows());
		// get the optimal cols size for dft
		int addPixelCols = Core.getOptimalDFTSize(image.cols());
		// apply the optimal cols and rows size to the image
		Core.copyMakeBorder(image, padded, 0, addPixelRows - image.rows(), 0, addPixelCols - image.cols(),
				Core.BORDER_CONSTANT, Scalar.all(0));

		return padded;
	}

	/**
	 * Optimize the magnitude of the complex image obtained from the DFT, to improve
	 * its visualization
	 *
	 * @param complexImage
	 *            the complex image obtained from the DFT
	 * @return the optimized image
	 */
	private Mat createOptimizedMagnitude(Mat complexImage) {
		// init
		List<Mat> newPlanes = new ArrayList<>();
		Mat mag = new Mat();
		// split the comples image in two planes
		Core.split(complexImage, newPlanes);
		// compute the magnitude
		Core.magnitude(newPlanes.get(0), newPlanes.get(1), mag);

		// move to a logarithmic scale
		Core.add(Mat.ones(mag.size(), CvType.CV_32F), mag, mag);
		Core.log(mag, mag);
		// optionally reorder the 4 quadrants of the magnitude image
		this.shiftDFT(mag);
		// normalize the magnitude image for the visualization since both JavaFX
		// and OpenCV need images with value between 0 and 255
		// convert back to CV_8UC1
		mag.convertTo(mag, CvType.CV_8UC1);
		Core.normalize(mag, mag, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

		// you can also write on disk the resulting image...
		// Imgcodecs.imwrite("../magnitude.png", mag);

		return mag;
	}

	/**
	 * Reorder the 4 quadrants of the image representing the magnitude, after the
	 * DFT
	 *
	 * @param image
	 *            the {@link Mat} object whose quadrants are to reorder
	 */
	private void shiftDFT(Mat image) {
		image = image.submat(new Rect(0, 0, image.cols() & -2, image.rows() & -2));
		int cx = image.cols() / 2;
		int cy = image.rows() / 2;

		Mat q0 = new Mat(image, new Rect(0, 0, cx, cy));
		Mat q1 = new Mat(image, new Rect(cx, 0, cx, cy));
		Mat q2 = new Mat(image, new Rect(0, cy, cx, cy));
		Mat q3 = new Mat(image, new Rect(cx, cy, cx, cy));

		Mat tmp = new Mat();
		q0.copyTo(tmp);
		q3.copyTo(q0);
		tmp.copyTo(q3);

		q1.copyTo(tmp);
		q2.copyTo(q1);
		tmp.copyTo(q2);
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

	public void setVolumeSubScene(SubScene volumeSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		mainTab = tabPane.getTabs().get(0);
		mainTabAnchor = (AnchorPane) mainTab.getContent();
		displayBorderPane = (BorderPane) mainTabAnchor.getChildren().get(0);
		displayBorderPane.setCenter(volumeSubScene);
	}

	public void setProjectionsSubScene(SubScene projectionsSubScene) {
		tabPane = (TabPane) rootGroup.getChildren().get(0);
		renderingTab = tabPane.getTabs().get(2);
		renderingTabAnchor = (AnchorPane) renderingTab.getContent();
		renderingDisplayBorderPane = (BorderPane) renderingTabAnchor.getChildren().get(0);
		renderingDisplayBorderPane.setCenter(projectionsSubScene);
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
