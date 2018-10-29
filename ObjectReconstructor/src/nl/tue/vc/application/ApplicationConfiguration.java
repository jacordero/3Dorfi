package nl.tue.vc.application;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationConfiguration {

	private static ApplicationConfiguration instance = null;
	
	private int windowWidth;
	
	private int windowHeight;
	
	private int volumeSceneWidth;
	
	private int volumeSceneHeight;
	
	private int volumeSceneDepth;
	
	private String title;
	
	private int maxOctreeLevels;
	
	private int renderingLevels;
	
	private int snapshotDelay;
	
	private final String PROPERTIES_FILENAME = "resources/config.properties";
	
	private static final Logger logger = Logger.getLogger(ApplicationConfiguration.class.getName());
	
	// private constructor to make this a singleton
	private ApplicationConfiguration() {
		windowWidth = 960;
		windowHeight = 680;
		
		volumeSceneWidth = 640;
		volumeSceneHeight = 480;
		volumeSceneDepth = 200;
		
		loadProperties();
	}
	
	private void loadProperties(){
		try {

			Properties properties = new Properties();
			FileInputStream input = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(input);
			
			title = properties.getProperty("title", "Object Reconstructor");
			maxOctreeLevels = Integer.parseInt(properties.getProperty("maxOctreeLevels", "8"));
			if (maxOctreeLevels >= 8){
				maxOctreeLevels = 8;
			} else if (maxOctreeLevels <= 2){
				maxOctreeLevels = 3;
			} 
			
			renderingLevels = Integer.parseInt(properties.getProperty("renderingLevels", "7"));
			renderingLevels = renderingLevels <= maxOctreeLevels ? renderingLevels : maxOctreeLevels;

			snapshotDelay = Integer.parseInt(properties.getProperty("snapshotDely", "250"));
			snapshotDelay = snapshotDelay <= 500 ? snapshotDelay : 500; 
		} catch (Exception e){
			logger.log(Level.WARNING, "Cannot load properties\n: " + e.getMessage());
		}
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

	public int getWindowHeight() {
		return windowHeight;
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
	
	public String getTitle(){
		return title;
	}
	
	public int getMaxOctreeLevels(){
		return maxOctreeLevels;
	}
	
	public int getRenderingLevels(){
		return renderingLevels;
	}
	
	public int getSnapshotDelay(){
		return snapshotDelay;
	}
}
