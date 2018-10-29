package nl.tue.vc.imgproc;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class CameraController {

	private static final int CAPTURE_DEVICE_NUMBER = 1;
	
	private boolean isCameraActive;
	
	private VideoCapture captureDevice;
	
	private Mat lastPicture;
	
	private static final Logger logger = Logger.getLogger(CameraController.class.getName());

		
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
				logger.log(Level.WARNING, "It cannot capture image:\n" + e.getMessage());
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
