package nl.tue.vc.imgproc;


import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class CameraController {

	private static final int CAPTURE_DEVICE_NUMBER = 1;
	
	private boolean isCameraActive;
	
	private VideoCapture captureDevice;
	
	private Mat lastPicture;
		
	public CameraController() {
		isCameraActive = false;
		captureDevice = new VideoCapture();
		lastPicture = new Mat();
	}
	
	public void startCamera() {
		if (!isCameraActive) {
			// start the video capture
			captureDevice.open(CAPTURE_DEVICE_NUMBER);
			// is the video stream available?
			if (captureDevice.isOpened()) {
				isCameraActive = true;
			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
	}
	
	// TODO: add the calibration parameters here
	public Mat grabFrame() {
		Mat frame = new Mat();
		if (captureDevice.isOpened()) {
			try {
				captureDevice.read(frame);
			} catch (Exception e) {
				System.err.print("ERROR");
				e.printStackTrace();
			}
		}
		return frame;
	}
	
	public void release() {
		if (captureDevice.isOpened()) {
			captureDevice.release();			
		}
		isCameraActive = false;
	}
	
	public Mat getLastPicture() {
		return lastPicture;
	}
	
	
}
