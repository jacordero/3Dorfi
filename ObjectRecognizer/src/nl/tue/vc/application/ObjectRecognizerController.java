package nl.tue.vc.application;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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
import nl.tue.vc.imgproc.CameraController;
import nl.tue.vc.imgproc.HistogramGenerator;
import nl.tue.vc.application.visual.NewStage;
import nl.tue.vc.imgproc.SilhouetteExtractor;
import nl.tue.vc.voxelengine.CameraPosition;
import nl.tue.vc.voxelengine.Octree;
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
	@FXML
	private Button cameraButton;
	@FXML
	private Button applyButton;
	@FXML
	private Button snapshotButton;
	// the FXML area for showing the current frame (before calibration)
	@FXML
	private ImageView originalFrame;
	// the FXML area for showing the current frame (after calibration)
	@FXML
	private ImageView calibratedFrame;
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
	
	// old timer
	private Timer timer;
	// a timer for acquiring the video stream
	private Timer imageTimer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture;
	// a flag to change the button behavior
	private boolean cameraActive;
	// the saved chessboard image
	private Mat savedImage, processedExtractedImage;
	// the calibrated camera frame
	private Image undistoredImage,CamStream;
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
	List<int[][]> sourceArrays = new ArrayList<int[][]>();
	List<int[][]> transformedArrays = new ArrayList<int[][]>();
	
	//List<ImageView> imageViews = new ArrayList<>();

	List<Mat> loadedImages = new ArrayList<>();
	Map<String, Integer> loadedImagesDescription = new HashMap<>();
	ListView<String> loadedImagesView = new ListView<>();
	ObservableList<String> loadedImagesNames = FXCollections.observableArrayList();

	List<Mat> segmentedImages = new ArrayList<>();
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
	
	private SilhouetteExtractor silhouetteExtractor;
	
	private CameraController cameraController;
	
	private Mat cameraFrame;
	
	@FXML
	private ImageView cameraFrameView;

	private Timer videoTimer;

	public static int SECOND = 1000;
	
	public ObjectRecognizerController() {
		silhouetteExtractor = new SilhouetteExtractor();
		cameraController = new CameraController();
		cameraFrame = new Mat();
		cameraFrameView = new ImageView();
		videoTimer = new Timer();
	}

	@FXML
	private void initialize() {
		cameraAxisX.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera axis X changed (newValue: " +  newValue.intValue() + ")");
			updateCameraPositionAxisX(newValue.intValue());
		});
		
		cameraAxisY.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera axis Y changed (newValue: " +  newValue.intValue() + ")");
			updateCameraPositionAxisY(newValue.intValue());
		});

		cameraAxisZ.valueProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Camera axis Z changed (newValue: " +  newValue.intValue() + ")");
			updateCameraPositionAxisZ(newValue.intValue());
		});
		
		binaryThreshold.valueProperty().addListener((observable, oldValue, newValue) -> {
			//System.out.println("Binary treshold value changed (newValue: " +  newValue.intValue() + ")");
			thresholdLabel.setText("Threshold: " + String.format("%.2f", newValue));
			updateBinaryThreshold(newValue.intValue());
		});		
		
		
		//segmentationAlgorithm;
		segmentationAlgorithm.getItems().add("Watersheed");
		segmentationAlgorithm.getItems().add("Binarization");
		segmentationAlgorithm.setValue("Watersheed");
		
		System.out.println(segmentationAlgorithm.getValue());

		
		startVideo();
		//this.vboxLeft.getChildren().add(cameraFrameView);
		this.vboxLeft.getChildren().add(loadedImagesView);
		loadedImagesView.setMaxWidth(140);
		this.vboxRight.getChildren().add(processedImagesView);
		processedImagesView.setMaxWidth(140);
	}
	
	
	/**
	 * Init the needed variables
	 */
	protected void init() {
		this.fileChooser = new FileChooser();
		this.image = new Mat();
		this.planes = new ArrayList<>();
		this.complexImage = new Mat();
		this.capture = new VideoCapture();
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
		//imageViews.add(this.originalImage);
		//imageViews.add(this.originalImage2);

		List<File> list = fileChooser.showOpenMultipleDialog(stage);

		if (list != null) {			
			// Clear content of previous images
			//loadedImagesNames.clear();
			//loadedImages.clear();
			//loadedImagesDescription.clear();
			
			for (int i = 0; i < list.size(); i++) {

				// show the open dialog window
				// File file = this.fileChooser.showOpenDialog(this.stage);
				File file = list.get(i);
				
				if (file != null) {
					ImageView imageView = new ImageView();
					// read the image in gray scale
					this.image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
					
					// load the images into the listview
					String imgName = file.getName().split("\\.")[0];
					loadedImagesNames.add(imgName);
					loadedImages.add(this.image);
					loadedImagesDescription.put(imgName, loadedImages.size() - 1);
					
					//System.out.println(imgName);
					
					// empty the image planes and the image views if it is not the first
					// loaded image
					if (!this.planes.isEmpty()) {
						this.planes.clear();
						this.transformedImage.setImage(null);
						this.antitransformedImage.setImage(null);
					}
				}
			}
			showLoadedImages();
		}			
	}

	public void showLoadedImages() {
		loadedImagesView.setItems(loadedImagesNames);
		loadedImagesView.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                	System.out.println("Null information");
                    setText(null);
                    setGraphic(null);
                } else {
                	int imagePosition = loadedImagesDescription.get(name);
                	System.out.println("Name: " + name +", Position: " + imagePosition);
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
 * @throws InterruptedException 
 */
@FXML
protected void extractSilhouettes(){
	
	// TODO: remove this hardcoded value
	//int binaryThreshold = 50;
	System.out.println("Extract silhouettes method was called...");

	// First, clear the previous content. Then, load the new content
	segmentedImages = new ArrayList<Mat>();

	processedImagesView = new ListView<String>();
	List<Mat> processedImages = new ArrayList<Mat>();
	processedImagesNames = FXCollections.observableArrayList();
	processedImagesDescription = new HashMap<String, Integer>();
			
	int imgId = 1;
	for (Mat image: loadedImages) {
		silhouetteExtractor.extract(image, segmentationAlgorithm.getValue());
		segmentedImages.add(silhouetteExtractor.getSegmentedImage());
		
		try {
			bufferedImagesForTest.add(IntersectionTest.Mat2BufferedImage(silhouetteExtractor.getBinaryImage()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			processedImages.add(silhouetteExtractor.getSegmentedImage());
			processedImagesNames.add("sg_" + imgId);
			processedImagesDescription.put("sg_" + imgId, processedImages.size() - 1);

		// show the processed images during the segmentation process
		if (debugSegmentation.isSelected() && segmentationAlgorithm.getValue() == "Watersheed") {
			processedImages.add(silhouetteExtractor.getBinaryImage());
			processedImagesNames.add("bi_" + imgId);
			processedImagesDescription.put("bi_" + imgId, processedImages.size() - 1);
			
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
	//System.out.println(x);
	
	processedImagesView.setCellFactory(param -> new ListCell<String>() {
        private ImageView imageView = new ImageView();
        @Override
        public void updateItem(String name, boolean empty) {
            super.updateItem(name, empty);
            if (empty) {
            	//System.out.println("Null information");
                setText(null);
                setGraphic(null);
            } else {
            	System.out.println("Name: " + name);
            	System.out.println(processedImagesDescription.keySet());
            	int imagePosition = processedImagesDescription.get(name);
            	System.out.println("Name: " + name +", Position: " + imagePosition);
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

private void updateView(ImageView view, Image image){
	
	this.updateImageView(view, image);
	// set a fixed width
	this.transformedImage.setFitWidth(250);
	// preserve image ratio
	this.transformedImage.setPreserveRatio(true);
	//Thread.sleep(milliseconds);		
}


	/**
	 * Store all the chessboard properties, update the UI and prepare other
	 * needed variables
	 */
	@FXML
	protected void updateSettings()
	{
		this.boardsNumber = Integer.parseInt(this.numBoards.getText());
		this.numCornersHor = Integer.parseInt(this.numHorCorners.getText());
		this.numCornersVer = Integer.parseInt(this.numVertCorners.getText());
		int numSquares = this.numCornersHor * this.numCornersVer;
		for (int j = 0; j < numSquares; j++)
			obj.push_back(new MatOfPoint3f(new Point3(j / this.numCornersHor, j % this.numCornersVer, 0.0f)));
		this.cameraButton.setDisable(false);
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera()
	{
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(0);

			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				TimerTask frameGrabber = new TimerTask() {
					@Override
					public void run()
					{
						CamStream=grabFrame();
						// show the original frames
						Platform.runLater(new Runnable() {
							@Override
				            public void run() {
								originalFrame.setImage(CamStream);
								// set fixed width
								originalFrame.setFitWidth(380);
								// preserve image ratio
								originalFrame.setPreserveRatio(true);
								// show the original frames
								calibratedFrame.setImage(undistoredImage);
								// set fixed width
								calibratedFrame.setFitWidth(380);
								// preserve image ratio
								calibratedFrame.setPreserveRatio(true);
				            	}
							});

					}
				};
				this.timer = new Timer();
				this.timer.schedule(frameGrabber, 0, 33);

				// update the button content
				this.cameraButton.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.cameraButton.setText("Start Camera");
			// stop the timer
			if (this.timer != null)
			{
				this.timer.cancel();
				this.timer = null;
			}
			// release the camera
			this.capture.release();
			// clean the image areas
			originalFrame.setImage(null);
			calibratedFrame.setImage(null);
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Image} to show
	 */
	private Image grabFrame()
	{
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty())
				{
					// show the chessboard pattern
					this.findAndDrawPoints(frame);

					if (this.isCalibrated)
					{
						// prepare the undistored image
						Mat undistored = new Mat();
						Imgproc.undistort(frame, undistored, intrinsic, distCoeffs);
						undistoredImage = Utils.mat2Image(undistored);
					}

					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = Utils.mat2Image(frame);
				}

			}
			catch (Exception e)
			{
				// log the (full) error
				System.err.print("ERROR");
				e.printStackTrace();
			}
		}

		return imageToShow;
	}

	
	@FXML
	protected void startVideo() {
		cameraController.startCamera();
		TimerTask frameGrabber = new TimerTask() {
		
			@Override
			public void run() {
				cameraFrame = cameraController.grabFrame();
				//System.out.println("Frame grabbed!!");
/*				
				if (!cameraFrame.empty()) {
					cameraFrameView.setImage(Utils.mat2Image(cameraFrame));
					originalFrame.setFitWidth(100);
					originalFrame.setPreserveRatio(true);					
				}
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
			}
		};

		videoTimer.schedule(frameGrabber, 0, 33);		
	}

	
	/**
	 * Take a snapshot to be used for the calibration process
	 */
	@FXML
	protected void takeSnapshot() {
		
		TimerTask frameGrabber = new TimerTask() {
			@Override
			public void run() {
				cameraController.startCamera();
				loadedImages.add(cameraController.grabFrame());
				loadedImagesNames.add("sc_" + loadedImages.size());
				loadedImagesDescription.put("sc_" + loadedImages.size(), loadedImages.size() - 1);
				showLoadedImages();
			}
		};
		
		imageTimer = new Timer();
		imageTimer.schedule(frameGrabber, 1*SECOND);		
	}
	
	/*
	@FXML
	protected void takeSnapshot()
	{
		if (this.successes < this.boardsNumber)
		{
			// save all the needed values
			this.imagePoints.add(imageCorners);
			imageCorners = new MatOfPoint2f();
			this.objectPoints.add(obj);
			this.successes++;
		}

		// reach the correct number of images needed for the calibration
		if (this.successes == this.boardsNumber)
		{
			this.calibrateCamera();
		}
	}*/
	
	

	/**
	 * Find and draws the points needed for the calibration on the chessboard
	 *
	 * @param frame
	 *            the current frame
	 * @return the current number of successfully identified chessboards as an
	 *         int
	 */
	private void findAndDrawPoints(Mat frame)
	{
		// init
		Mat grayImage = new Mat();

		// I would perform this operation only before starting the calibration
		// process
		if (this.successes < this.boardsNumber)
		{
			// convert the frame in gray scale
			Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);
			// the size of the chessboard
			Size boardSize = new Size(this.numCornersHor, this.numCornersVer);
			// look for the inner chessboard corners
			boolean found = Calib3d.findChessboardCorners(grayImage, boardSize, imageCorners,
					Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
			// all the required corners have been found...
			if (found)
			{
				// optimization
				TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
				Imgproc.cornerSubPix(grayImage, imageCorners, new Size(11, 11), new Size(-1, -1), term);
				// save the current frame for further elaborations
				grayImage.copyTo(this.savedImage);
				// show the chessboard inner corners on screen
				Calib3d.drawChessboardCorners(frame, boardSize, imageCorners, found);

				// enable the option for taking a snapshot
				this.snapshotButton.setDisable(false);
			}
			else
			{
				this.snapshotButton.setDisable(true);
			}
		}
	}


	/**
	 * The effective camera calibration, to be performed once in the program
	 * execution
	 */
	private void calibrateCamera()
	{
		// init needed variables according to OpenCV docs
		List<Mat> rvecs = new ArrayList<>();
		List<Mat> tvecs = new ArrayList<>();
		intrinsic.put(0, 0, 1);
		intrinsic.put(1, 1, 1);
		// calibrate!
		this.calibrationResult = Calib3d.calibrateCamera(objectPoints, imagePoints, savedImage.size(), intrinsic, distCoeffs, rvecs, tvecs);
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
		//System.out.println("height = " + this.processedExtractedImage.size().height + ", width = " + this.processedExtractedImage.size().width);
		for(BufferedImage convertedMat : this.bufferedImagesForTest) {	
			System.out.println("-------- Image Bounds ----- " + convertedMat.getMinX() + " ----- " + convertedMat.getGraphics());
			//System.out.println("Converted mat width = " + convertedMat.getWidth() + ", height = " + convertedMat.getHeight());
			int[][] sourceArray = IntersectionTest.getBinaryArray(convertedMat);
			//System.out.println("binary array rows = " + sourceArray.length + ", cols = " + sourceArray[0].length);
			for (int x = 0; x < sourceArray.length; x++) {
				for (int y = 0; y < sourceArray[x].length; y++) {
					//System.out.print(sourceArray[x][y] + " ");
				}
				//System.out.println("");
			}
			sourceArrays.add(sourceArray);
			int[][] transformedArray = IntersectionTest.getTransformedArray(sourceArray);
			//System.out.println("transformedArray array rows = " + transformedArray.length + ", cols = " + transformedArray[0].length);
			// print the contents of transformedArray
			for (int x = 0; x < transformedArray.length; x++) {
				for (int y = 0; y < transformedArray[x].length; y++) {
					//System.out.print(transformedArray[x][y] + " ");
				}
				//System.out.println("");
			}
			transformedArrays.add(transformedArray);
		}
			
	}

	/**
	 * The action triggered by pushing the button for visualizing the model from the
	 * loaded images
	 */
	@FXML
	protected void visualizeModel() {
		int boxSize = 256;
		CameraPosition cameraPosition = new CameraPosition();
		//cameraPositionX = 320;
		//cameraPositionY = 240;
		//cameraPositionZ = 300;
		cameraPosition.positionAxisX = 0;
		cameraPosition.positionAxisY = 0;
		cameraPosition.positionAxisZ = 0;
		
		Octree octree = new Octree(boxSize);
		octree.generateOctreeTest(boxSize, 10);
		octree.setBufferedImagesForTest(this.bufferedImagesForTest);
		// try not create another volume renderer object to recompute the octree visualization
		volumeRenderer = new VolumeRenderer(octree, this.sourceArrays, this.transformedArrays);
		volumeRenderer.generateVolumeScene();
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
