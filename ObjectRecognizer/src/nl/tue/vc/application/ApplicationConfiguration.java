package nl.tue.vc.application;

import java.util.HashMap;
import java.util.Map;

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
	
	private Map<String, Integer> silhouetteConfiguration;
	
	
	
	// private constructor to make this a singleton
	private ApplicationConfiguration() {
		windowWidth = 960;
		windowHeight = 680;
		
		volumeSceneWidth = 640;
		volumeSceneHeight = 480;
		volumeSceneDepth = 200;
		
		cameraPosition = new CameraPosition();
		cameraPosition.positionAxisX = -70;
		cameraPosition.positionAxisY = -50;
		cameraPosition.positionAxisZ = 100;
		volumeBoxSize = 100;
		
		silhouetteConfiguration = new HashMap<String, Integer>();
		silhouetteConfiguration.put("imageWidthFirstPixel", 30);
		silhouetteConfiguration.put("imageWidthLastPixel", 370);
		silhouetteConfiguration.put("imageHeightFirstPixel", 20);
		silhouetteConfiguration.put("imageHeightLastPixel", 280);
		silhouetteConfiguration.put("binaryThreshold", 80);
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
	
	public Map<String, Integer> getSilhouetteConfiguration(){
		return silhouetteConfiguration;
	}
}
