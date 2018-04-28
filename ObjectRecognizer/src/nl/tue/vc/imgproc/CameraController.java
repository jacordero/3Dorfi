package nl.tue.vc.imgproc;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.scene.image.Image;
import nl.tue.vc.application.utils.Utils;

public class CameraController {

	private static final int CAPTURE_DEVICE_NUMBER = 1;
	
	private boolean isCameraActive;
	
	private VideoCapture captureDevice;
	
	private Timer timer;
	
	private Mat lastPicture;
		
	public CameraController() {
		isCameraActive = false;
		captureDevice = new VideoCapture();
		timer = new Timer();
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
	
	public Mat getLastPicture() {
		return lastPicture;
	}
	
}
