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
import org.opencv.core.Point3;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.application.visual.IntersectionTest;
import nl.tue.vc.imgproc.CameraCalibrator;
import nl.tue.vc.imgproc.CameraController;
import nl.tue.vc.imgproc.HistogramGenerator;
import nl.tue.vc.imgproc.SilhouetteExtractor;
import nl.tue.vc.projection.ProjectionGenerator;
import nl.tue.vc.projection.TransformMatrices;
import nl.tue.vc.voxelengine.BoxParameters;
import nl.tue.vc.voxelengine.CameraPosition;
import nl.tue.vc.voxelengine.Octree;
import nl.tue.vc.voxelengine.VolumeGenerator;
import nl.tue.vc.voxelengine.VolumeRenderer;

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
	private VBox vboxLeft;

	@FXML
	private VBox vboxRight;

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
	// a FXML button for performing the antitransformation
	// @FXML
	// private Button cameraButton;
	@FXML
	private Button applyButton;

	@FXML
	private Button calibrateExtrinsicParamsButton;

	@FXML
	private Button snapshotButton;

	// the FXML area for showing the current frame (before calibration)
	@FXML
	private ImageView originalFrame;

	// the FXML area for showing the current frame (after calibration)
	@FXML

	private ImageView calibrationFrame;
	// info related to the calibration process
	@FXML
	private TextField numBoards;
	@FXML
	private TextField numHorCorners;
	@FXML
	private TextField numVertCorners;

	@FXML
	private Slider cameraAxisX;

	@FXML
	private Slider cameraAxisY;

	@FXML
	private Slider cameraAxisZ;

	@FXML
	private Slider binaryThreshold;

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
	private Slider fieldOfViewSlider;

	@FXML
	private Slider worldRotationYAngleSlider;

	@FXML
	private TextField boxSizeField;

	@FXML
	private TextField levelsField;

	@FXML
	private Slider centerAxisX;

	@FXML
	private Slider centerAxisY;

	@FXML
	private Slider centerAxisZ;

	@FXML
	private Button generateButton;

	private int fieldOfView;
	private TransformMatrices transformMatrices;

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
	private List<Mat> calibrationImages = new ArrayList<Mat>();
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
	List<int[][]> transformedArrays = new ArrayList<int[][]>();
	List<int[][]> transformedInvertedArrays = new ArrayList<int[][]>();

	// List<ImageView> imageViews = new ArrayList<>();

	List<Mat> loadedImages = new ArrayList<>();
	Map<String, Integer> loadedImagesDescription = new HashMap<>();
	ListView<String> loadedImagesView = new ListView<>();
	ObservableList<String> loadedImagesNames = FXCollections.observableArrayList();

	List<Mat> segmentedImages = new ArrayList<>();
	List<Mat> complementSegmentedImages = new ArrayList<>();
	Map<String, Integer> processedImagesDescription = new HashMap<>();
	ListView<String> processedImagesView = new ListView<>();
	ObservableList<String> processedImagesNames = FXCollections.observableArrayList();

	List<BufferedImage> bufferedImagesForTest = new ArrayList<>();

	// the main stage
	private Stage stage;
	// the JavaFX file chooser
	private FileChooser fileChooser;
	// support variables
	private Mat image;
	private List<Mat> planes;
	// the final complex image
	private Mat complexImage;
	private double calibrationResult = 0;

	// The rootGroup
	private BorderPane rootGroup;

	private VolumeRenderer volumeRenderer;
	private VolumeGenerator volumeGenerator;

	private SilhouetteExtractor silhouetteExtractor;

	private CameraController cameraController;

	private CameraCalibrator cameraCalibrator;

	private ProjectionGenerator projectionGenerator;
	
	private List<String> projectorPerSilhouette;
	
	private Mat cameraFrame;

	private Timer videoTimer;

	private boolean videoTimerActive;

	private Image defaultVideoImage;

	public static int SNAPSHOT_DELAY = 250;
	public static final boolean TEST_PROJECTIONS = true;
	private double sceneWidth;
	private double sceneHeight;
	private int levels;
	private int boxSize;
	private int centerX, centerY, centerZ;

	public ObjectRecognizerController() {

		this.sceneWidth = 400;// 650.5;//440;
		this.sceneHeight = 290;// 328.0;//320;
		silhouetteExtractor = new SilhouetteExtractor();
		cameraController = new CameraController();
		cameraFrame = new Mat();
		cameraFrameView = new ImageView();
		videoTimer = new Timer();
		videoTimerActive = false;
		calibrationTimer = new Timer();
		calibrationTimerActive = false;
		transformMatrices = new TransformMatrices(sceneWidth, sceneHeight, 32.3);
		cameraCalibrator = new CameraCalibrator();
		this.boxSize = 15;// 11;//Integer.parseInt(this.boxSizeField.getText());
		this.levels = 0;// Integer.parseInt(this.levelsField.getText());
		this.centerX = 4;
		this.centerY = 1;
		this.centerZ = 0;
		
		calibrationImageCounter = 0;
		calibrationIndices = new ArrayList<String>();
		calibrationIndices.add("deg-0");
		calibrationIndices.add("deg-90");
		calibrationIndices.add("deg-180");
		calibrationIndices.add("deg-270");
		projectionGenerator = null;
	}

	@FXML
	private void initialize() {
		cameraAxisX.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera axis X changed (newValue: " + newValue.intValue() + ")");
			updateCameraPositionAxisX(newValue.intValue());
		});

		cameraAxisY.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera axis Y changed (newValue: " + newValue.intValue() + ")");
			updateCameraPositionAxisY(newValue.intValue());
		});

		cameraAxisZ.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera axis Z changed (newValue: " + newValue.intValue() + ")");
			updateCameraPositionAxisZ(newValue.intValue());
		});

		binaryThreshold.valueProperty().addListener((observable, oldValue, newValue) -> {
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
				cameraFrameView.setImage(new Image("images/bkg_img.jpg"));
			}
			// System.out.println("CheckBox Action (selected: " + selected + ")");
		});

		// Camera calibration is selected
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
		try {
			defaultVideoImage = new Image("images/bkg_img.jpg");
			cameraFrameView.setImage(defaultVideoImage);
		} catch (Exception e) {
			e.printStackTrace();
			defaultVideoImage = null;
		}

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
		this.vboxLeft.getChildren().add(loadedImagesView);
		loadedImagesView.setMaxWidth(140);
		this.vboxRight.getChildren().add(processedImagesView);
		processedImagesView.setMaxWidth(140);

		fieldOfViewSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Field of view changed (newValue: " + newValue.intValue() + ")");
			fieldOfView = newValue.intValue();
		});

		fieldOfViewSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging, Boolean isNowChanging) {
				if (!isNowChanging) {
					System.out.println("It stopped changing");
					transformMatrices.updateFieldOfView(fieldOfViewSlider.getValue());
					renderModel();
				}
			}
		});

		worldRotationYAngleSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> obs, Boolean wasChanging, Boolean isNowChanging) {
				if (!isNowChanging) {
					System.out.println("Rotation around Y angle was stopped");

					transformMatrices.updateWorldRotationYAngle(worldRotationYAngleSlider.getValue());
					// transformMatrices.updateFieldOfView(fieldOfViewSlider.getValue());
					renderModel();
				}
			}
		});

		centerAxisX.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Center axis X changed (newValue: " + newValue.intValue() + ")");
			this.centerX = newValue.intValue();
			this.renderModel();
		});

		centerAxisY.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Center axis Y changed (newValue: " + newValue.intValue() + ")");
			this.centerY = newValue.intValue();
			this.renderModel();
		});

		centerAxisZ.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Center axis Z changed (newValue: " + newValue.intValue() + ")");
			this.centerZ = newValue.intValue();
			this.renderModel();
		});

		generateButton.setOnKeyReleased((event) -> {
			this.boxSize = Integer.parseInt(this.boxSizeField.getText());
			this.levels = Integer.parseInt(this.levelsField.getText());
			System.out.println("New Boxsize: " + this.boxSize + ", New levels value: " + this.levels);
			this.renderModel();
		});
	}

	/**
	 * Init the needed variables
	 */
	protected void init() {
		this.fileChooser = new FileChooser();
		this.image = new Mat();
		this.planes = new ArrayList<>();
		this.complexImage = new Mat();
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
					//ImageView imageView = new ImageView();
					// read the image in gray scale
					// this.image = Imgcodecs.imread(file.getAbsolutePath(),
					// Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
					this.image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);

					if (enableCameraCalibration.isSelected()){
						Utils.debugNewLine("Loading calibration image: " + file.getName(), true);
						
						calibrationImage = this.image;
						calibrationFrame.setImage(Utils.mat2Image(calibrationImage));
						calibrationFrame.setFitWidth(100);
						calibrationFrame.setPreserveRatio(true);
						calibrationImagesMap.put(calibrationIndices.get(calibrationImageCounter), calibrationImage);
						calibrationImageCounter += 1;
						if (calibrationImageCounter > calibrationIndices.size() - 1){
							calibrationImageCounter = 0;
						}
					} else {
						// load the images into the listview
						String imgName = file.getName().split("\\.")[0];
						loadedImagesNames.add(imgName);
						loadedImages.add(this.image);
						loadedImagesDescription.put(imgName, loadedImages.size() - 1);

						// System.out.println(imgName);

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

		loadedImagesView.setItems(loadedImagesNames);
		loadedImagesView.setCellFactory(param -> new ListCell<String>() {
			private ImageView imageView = new ImageView();

			@Override
			public void updateItem(String name, boolean empty) {
				super.updateItem(name, empty);
				if (empty) {
					// System.out.println("Null information");
					setText(null);
					setGraphic(null);
				} else {
					System.out.println(name);
					int imagePosition = loadedImagesDescription.get(name);
					// System.out.println("Name: " + name +", Position: " + imagePosition);
					imageView.setImage(Utils.mat2Image(loadedImages.get(imagePosition)));
					imageView.setFitWidth(100);
					imageView.setPreserveRatio(true);
					setText("");
					setGraphic(imageView);
				}
			}
		});

		loadedImagesView.setMaxWidth(140);
		loadedImagesView.refresh();
	}

	/**
	 * The action triggered by pushing the button for apply the dft to the loaded
	 * image
	 * 
	 * @throws InterruptedException
	 */
	@FXML
	protected void extractSilhouettes() {

		// TODO: remove this hardcoded value
		// int binaryThreshold = 50;
		System.out.println("Extract silhouettes method was called...");

		// First, clear the previous content. Then, load the new content
		segmentedImages = new ArrayList<Mat>();
		complementSegmentedImages = new ArrayList<Mat>();

		processedImagesView = new ListView<String>();
		List<Mat> processedImages = new ArrayList<Mat>();
		processedImagesNames = FXCollections.observableArrayList();
		processedImagesDescription = new HashMap<String, Integer>();

		int imgId = 1;
		for (Mat image : loadedImages) {
			silhouetteExtractor.extract(image, segmentationAlgorithm.getValue());
			segmentedImages.add(silhouetteExtractor.getSegmentedImage());

			try {
				// bufferedImagesForTest.add(IntersectionTest.Mat2BufferedImage(silhouetteExtractor.getBinaryImage()));
				bufferedImagesForTest.add(IntersectionTest.Mat2BufferedImage(silhouetteExtractor.getSegmentedImage()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Something went really wrong!!!");
				e.printStackTrace();
			}

			processedImages.add(silhouetteExtractor.getSegmentedImage());
			processedImagesNames.add("sg_" + imgId);
			processedImagesDescription.put("sg_" + imgId, processedImages.size() - 1);

			// show the processed images during the segmentation process
			if (debugSegmentation.isSelected()) {

				processedImages.add(silhouetteExtractor.getSegmentedImage());
				processedImagesNames.add("comp_" + imgId);
				processedImagesDescription.put("comp_" + imgId, processedImages.size() - 1);

				processedImages.add(silhouetteExtractor.getEqualizedImage());
				processedImagesNames.add("eq_" + imgId);
				processedImagesDescription.put("eq_" + imgId, processedImages.size() - 1);

				processedImages.add(silhouetteExtractor.getBinaryImage());
				processedImagesNames.add("bi_" + imgId);
				processedImagesDescription.put("bi_" + imgId, processedImages.size() - 1);

				processedImages.add(silhouetteExtractor.getCleanedBinaryImage());
				processedImagesNames.add("cb_" + imgId);
				processedImagesDescription.put("cb_" + imgId, processedImages.size() - 1);

				Mat histImage = HistogramGenerator.histogram(image);
				processedImages.add(histImage);
				processedImagesNames.add("hi_" + imgId);
				processedImagesDescription.put("hi_" + imgId, processedImages.size() - 1);

				processedImages.add(silhouetteExtractor.getNoiseFreeImage());
				processedImagesNames.add("nf_" + imgId);
				processedImagesDescription.put("nf_" + imgId, processedImages.size() - 1);

				processedImages.add(silhouetteExtractor.getSureBackgroundImage());
				processedImagesNames.add("sb_" + imgId);
				processedImagesDescription.put("sb_" + imgId, processedImages.size() - 1);

				processedImages.add(silhouetteExtractor.getSureBackgroundImage());
				processedImagesNames.add("sf_" + imgId);
				processedImagesDescription.put("sf_" + imgId, processedImages.size() - 1);

				processedImages.add(silhouetteExtractor.getUnknownImage());
				processedImagesNames.add("u_" + imgId);
				processedImagesDescription.put("u_" + imgId, processedImages.size() - 1);
			}

			imgId++;
		}

		processedImagesView.setItems(processedImagesNames);
		System.out.println(processedImagesDescription.keySet());
		// System.out.println(x);

		processedImagesView.setCellFactory(param -> new ListCell<String>() {
			private ImageView imageView = new ImageView();

			@Override
			public void updateItem(String name, boolean empty) {
				super.updateItem(name, empty);
				if (empty) {
					// System.out.println("Null information");
					setText(null);
					setGraphic(null);
				} else {
					// System.out.println("Name: " + name);
					// System.out.println(processedImagesDescription.keySet());
					int imagePosition = processedImagesDescription.get(name);
					// System.out.println("Name: " + name +", Position: " + imagePosition);
					imageView.setImage(Utils.mat2Image(processedImages.get(imagePosition)));
					imageView.setFitWidth(100);
					imageView.setPreserveRatio(true);
					setText(name);
					setGraphic(imageView);
				}
			}
		});

		// to allow updating new elements for the list view
		this.vboxRight.getChildren().clear();
		this.vboxRight.getChildren().add(processedImagesView);
	}

	/**
	 * Store all the chessboard properties, update the UI and prepare other needed
	 * variables
	 */
	@FXML
	protected void updateSettings() {
		this.boardsNumber = Integer.parseInt(this.numBoards.getText());
		this.numCornersHor = Integer.parseInt(this.numHorCorners.getText());
		this.numCornersVer = Integer.parseInt(this.numVertCorners.getText());
		int numSquares = this.numCornersHor * this.numCornersVer;
		for (int j = 0; j < numSquares; j++)
			obj.push_back(new MatOfPoint3f(new Point3(j / this.numCornersHor, j % this.numCornersVer, 0.0f)));
		// this.cameraButton.setDisable(false);
	}

	/**
	 * updates the boxsize and levels of the octree
	 */
	@FXML
	protected void updateOctreeSettings() {
		this.boxSize = Integer.parseInt(this.boxSizeField.getText());
		this.levels = Integer.parseInt(this.levelsField.getText());
		System.out.println("New Boxsize: " + this.boxSize + ", New levels value: " + this.levels);
		this.renderModel();
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
							cameraFrameView.setImage(Utils.mat2Image(cameraFrame));
							originalFrame.setFitWidth(100);
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
					calibrationImageCounter += 1;
					if (calibrationImageCounter > calibrationIndices.size() - 1){
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
					loadedImages.add(cameraController.grabFrame());
					loadedImagesNames.add("sc_" + loadedImages.size());
					loadedImagesDescription.put("sc_" + loadedImages.size(), loadedImages.size() - 1);
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
		if (!calibrationImagesMap.isEmpty()) {
			projectionGenerator = cameraCalibrator.calibrateMatrices(calibrationImagesMap, true);
			//projector = cameraCalibrator.calibrateSingleMatrix(calibrationImage, true);
		} else {
			Utils.debugNewLine("*** Load calibration images ***", true);
		}
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
		loadedImagesNames.clear();
		loadedImages.clear();
		loadedImagesDescription.clear();
	}

	/**
	 * The action triggered by pushing the button for constructing the model from
	 * the loaded images
	 */
	@FXML
	protected void constructModel() {
		// System.out.println("height = " + this.processedExtractedImage.size().height +
		// ", width = " + this.processedExtractedImage.size().width);

		// Original rectangle: [(545., 432.), (684., 432.), (545., 609.), (684., 609.)]

		int xMinRange = 545;
		int xMaxRange = 684;
		int yMinRange = 432;
		int yMaxRange = 609;

		for (BufferedImage convertedMat : this.bufferedImagesForTest) {
			// System.out.println("Converted mat width = " + convertedMat.getWidth() + ",
			// height = " + convertedMat.getHeight());
			int[][] sourceArray = IntersectionTest.getBinaryArray(convertedMat);
			System.out.println("binary array rows = " + sourceArray.length + ", cols = " + sourceArray[0].length);
			for (int y = 0; y < sourceArray.length; y++) {
				for (int x = 0; x < sourceArray[y].length; x++) {
					if (x >= xMinRange && x <= xMaxRange && y >= yMinRange && y <= yMaxRange) {
						// System.out.print(sourceArray[y][x] + " ");
					}
				}
				if (y >= yMinRange && y <= yMaxRange) {
					// System.out.println("");
				}
			}

			int[][] invertedArray = IntersectionTest.getInvertedArray(sourceArray);
			Utils.debugNewLine("Inverted array rows = " + invertedArray.length + ", cols = " + invertedArray[0].length, false);
			for (int y = 0; y < invertedArray.length; y++) {
				for (int x = 0; x < invertedArray[y].length; x++) {
					if (x >= xMinRange && x <= xMaxRange && y >= yMinRange && y <= yMaxRange) {
						// System.out.print(invertedArray[y][x] + " ");
					}
				}
				if (y >= yMinRange && y <= yMaxRange) {
					// System.out.println("");
				}
			}

			int[][] transformedArray = IntersectionTest.computeDistanceTransform(sourceArray);
			Utils.debugNewLine("transformedArray array rows = " + transformedArray.length + ", cols = "
					+ transformedArray[0].length, false);
			// print the contents of transformedArray
			for (int y = 0; y < transformedArray.length; y++) {
				for (int x = 0; x < transformedArray[y].length; x++) {
					if (x >= xMinRange && x <= xMaxRange && y >= yMinRange && y <= yMaxRange) {
						// System.out.print(transformedArray[y][x] + " ");
					}
					// System.out.print(transformedArray[x][y] + " ");
				}
				if (y >= yMinRange && y <= yMaxRange) {
					// System.out.println("");
				}
			}

			int[][] transformedInvertedArray = IntersectionTest.computeDistanceTransform(invertedArray);
			Utils.debugNewLine("transformedInvertedArray array rows = " + transformedInvertedArray.length + ", cols = "
					+ transformedInvertedArray[0].length, false);
			// print the contents of transformedComplementArray
			for (int y = 0; y < transformedInvertedArray.length; y++) {
				for (int x = 0; x < transformedInvertedArray[y].length; x++) {
					if (x >= xMinRange && x <= xMaxRange && y >= yMinRange && y <= yMaxRange) {
						// System.out.print(transformedInvertedArray[y][x] + " ");
					}
				}
				if (y >= yMinRange && y <= yMaxRange) {
					// System.out.println("");
				}
			}

			transformedArrays.add(transformedArray);
			transformedInvertedArrays.add(transformedInvertedArray);
		}

	}

	/**
	 * The action triggered by pushing the button for visualizing the model from the
	 * loaded images
	 */
	@FXML
	protected void visualizeModel() {
		renderModel();
	}

	public void renderModel() {

		CameraPosition cameraPosition = new CameraPosition();
		cameraPosition.positionAxisX = 0;
		cameraPosition.positionAxisY = 0;
		cameraPosition.positionAxisZ = 0;

		BoxParameters volumeBoxParameters = new BoxParameters();
		volumeBoxParameters.setBoxSize(this.boxSize);
		volumeBoxParameters.setCenterX(this.centerX);
		volumeBoxParameters.setCenterY(this.centerY);
		volumeBoxParameters.setCenterZ(this.centerZ);
		Octree octree = new Octree(volumeBoxParameters, this.levels);

		// try not create another volume renderer object to recompute the octree
		// visualization
		volumeRenderer = new VolumeRenderer(octree);
		// instantiate the volume generator object
		volumeGenerator = new VolumeGenerator(octree, volumeBoxParameters, this.transformedInvertedArrays,
				this.transformedArrays, this.levels);
		volumeGenerator.setBufferedImagesForTest(this.bufferedImagesForTest);
		volumeGenerator.setTransformedInvertedArrays(this.transformedInvertedArrays);
		volumeGenerator.setTransformedArrays(this.transformedArrays);
		volumeGenerator.setFieldOfView(this.fieldOfView);
		volumeGenerator.setTransformMatrices(this.transformMatrices);
		volumeGenerator.setProjectionGenerator(projectionGenerator);
		volumeRenderer.generateVolumeScene(volumeGenerator.generateVolume());
		rootGroup.setCenter(volumeRenderer.getSubScene());
	}

	private void updateCameraPositionAxisX(int positionX) {
		CameraPosition cameraPosition = volumeRenderer.getCameraPosition();
		cameraPosition.positionAxisX = positionX;
		updateCameraPosition(cameraPosition);
	}

	private void updateCameraPositionAxisY(int positionY) {
		CameraPosition cameraPosition = volumeRenderer.getCameraPosition();
		cameraPosition.positionAxisY = positionY;
		updateCameraPosition(cameraPosition);
	}

	private void updateCameraPositionAxisZ(int positionZ) {
		CameraPosition cameraPosition = volumeRenderer.getCameraPosition();
		cameraPosition.positionAxisZ = positionZ;
		updateCameraPosition(cameraPosition);
	}

	private void updateBinaryThreshold(int binaryThreshold) {
		silhouetteExtractor.setBinaryThreshold(binaryThreshold);
	}

	public void updateCameraPosition(CameraPosition cameraPosition) {
		volumeRenderer.updateCameraPosition(cameraPosition);
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

	public void setRootGroup(BorderPane rootGroup) {
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

}
