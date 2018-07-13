package nl.tue.vc.application;

import java.util.HashMap;
import java.util.Map;

import nl.tue.vc.voxelengine.BoxParameters;
import nl.tue.vc.voxelengine.CameraPosition;

public class ApplicationConfiguration {

	private static ApplicationConfiguration instance = null;
	
	private int windowWidth;
	
	private int windowHeight;
	
	private int volumeSceneWidth;
	
	private int volumeSceneHeight;
	
	private int volumeSceneDepth;
	
	private CameraPosition cameraPosition;
	
	private int volumeBoxSize;
	
	private int imageWidth;
	
	private int imageHeight;
	
	private Map<String, Integer> silhouetteConfiguration;
	
	private BoxParameters volumeBoxParameters;
	
	
	
	// private constructor to make this a singleton
	private ApplicationConfiguration() {
		// original width resolution: 1280, original height resolution: 960
		imageWidth = 640;
		imageHeight = 480;
		
		windowWidth = 960;
		windowHeight = 680;
		
		volumeSceneWidth = 640;
		volumeSceneHeight = 480;
		volumeSceneDepth = 200;
		
		cameraPosition = new CameraPosition();
		cameraPosition.positionAxisX = 0;
		cameraPosition.positionAxisY = 0;
		cameraPosition.positionAxisZ = 0;
		volumeBoxSize = 256;
		
		silhouetteConfiguration = new HashMap<String, Integer>();
		silhouetteConfiguration.put("imageWidthFirstPixel", 30);
		silhouetteConfiguration.put("imageWidthLastPixel", 370);
		silhouetteConfiguration.put("imageHeightFirstPixel", 20);
		silhouetteConfiguration.put("imageHeightLastPixel", 280);
		silhouetteConfiguration.put("binaryThreshold", 105);
		
		volumeBoxParameters = new BoxParameters();		
		volumeBoxParameters.setBoxSize(volumeBoxSize);
		volumeBoxParameters.setCenterX(volumeSceneWidth/2);
		volumeBoxParameters.setCenterY(volumeSceneHeight/2);
		volumeBoxParameters.setCenterZ(volumeSceneDepth/2);
	}
	
	public static ApplicationConfiguration getInstance() {
		if (instance == null) {
			instance = new ApplicationConfiguration();
		}
		
		return instance;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}
	
	public void setCameraPosition(CameraPosition cameraPosition) {
		this.cameraPosition = cameraPosition;
	}
	
	public CameraPosition getCameraPosition() {
		return cameraPosition;
	}
	
	public void setVolumeBoxSize(int volumeBoxSize) {
		this.volumeBoxSize = volumeBoxSize;
	}
	
	public int getVolumeBoxSize() {
		return volumeBoxSize;
	}

	public int getVolumeSceneWidth() {
		return volumeSceneWidth;
	}

	public void setVolumeSceneWidth(int volumeSceneWidth) {
		this.volumeSceneWidth = volumeSceneWidth;
	}

	public int getVolumeSceneHeight() {
		return volumeSceneHeight;
	}

	public void setVolumeSceneHeight(int volumeSceneHeight) {
		this.volumeSceneHeight = volumeSceneHeight;
	}

	public int getVolumeSceneDepth() {
		return volumeSceneDepth;
	}

	public void setVolumeSceneDepth(int volumeSceneDepth) {
		this.volumeSceneDepth = volumeSceneDepth;
	}
	
	public int getImageWidth() {
		return imageWidth;
	}
	
	public int getImageHeight() {
		return imageHeight;
	}
	
	public Map<String, Integer> getSilhouetteConfiguration(){
		return silhouetteConfiguration;
	}

	public BoxParameters getVolumeBoxParameters() {
		return volumeBoxParameters;
	}

	public void setVolumeBoxParameters(BoxParameters volumeBoxParameters) {
		this.volumeBoxParameters = volumeBoxParameters;
	}
}
