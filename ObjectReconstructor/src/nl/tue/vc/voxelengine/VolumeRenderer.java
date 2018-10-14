package nl.tue.vc.voxelengine;

import java.util.List;

import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.application.utils.Utils;
import nl.tue.vc.application.utils.Xform;
import nl.tue.vc.model.BoxParameters;

public class VolumeRenderer {

	private BoxParameters volumeBoxParameters;
	private int sceneWidth;
	private int sceneHeight;
	private int sceneDepth;
	
	private float boxSizeX;
	private float boxSizeY;
	private float boxSizeZ;
	private float boxSizeFactor;
		
	private SubScene subScene;
	
	private Group root;
	private Group axisGroup;
	private Xform world;
	private PerspectiveCamera camera;
	private Xform cameraXform;
	private Xform cameraXform2;
	private Xform cameraXform3;
	private double cameraDistance;
	private Xform volumeGroup;
	 
	    private Timeline timeline;
	    boolean timelinePlaying = false;
	    double ONE_FRAME = 1.0/24.0;
	    double DELTA_MULTIPLIER = 200.0;
	    double CONTROL_MULTIPLIER = 0.1;
	    double SHIFT_MULTIPLIER = 0.1;
	    double ALT_MULTIPLIER = 0.5;
	        
	    double mousePosX;
	    double mousePosY;
	    double mouseOldX;
	    double mouseOldY;
	    double mouseDeltaX;
	    double mouseDeltaY;

	
	public VolumeRenderer(double cameraDistance) {
		boxSizeFactor = 20;
		this.cameraDistance = cameraDistance;
		configVolumeScene();
		initConfig();
	}
	
	private void initConfig(){
		root = new Group();
		axisGroup = new Group();
		world = new Xform();
		camera = new PerspectiveCamera(true);
		cameraXform = new Xform();
		cameraXform2 = new Xform();
		cameraXform3 = new Xform();
		volumeGroup = new Xform();
	}
		
	private void configVolumeScene() {
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		sceneWidth = appConfig.getVolumeSceneWidth();
		sceneHeight = appConfig.getVolumeSceneHeight();
		sceneDepth = appConfig.getVolumeSceneDepth();
		boxSizeX = boxSizeFactor * 10;
		boxSizeY = boxSizeFactor * 10;
		boxSizeZ = boxSizeFactor * 10;
		
		volumeBoxParameters = new BoxParameters();		
		volumeBoxParameters.setSizeX(boxSizeX);
		volumeBoxParameters.setSizeY(boxSizeY);
		volumeBoxParameters.setSizeZ(boxSizeZ);		
		volumeBoxParameters.setCenterX(sceneWidth/2);
		volumeBoxParameters.setCenterY(sceneHeight/2);
		volumeBoxParameters.setCenterZ(sceneDepth/2);

		// Create point light
	}
	
    private void buildAxes() {
        Utils.debugNewLine("[VolumeRenderer.buildAxes()]", true);
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
 
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
 
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
 
        final Box xAxis = new Box(240.0, 1, 1);
        final Box yAxis = new Box(1, 240.0, 1);
        final Box zAxis = new Box(1, 1, 240.0);
        
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
 
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }

	
    private void buildCamera(){
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);
 
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        cameraXform.ry.setAngle(320.0);
        cameraXform.rx.setAngle(40);
    }
	
	   private void handleMouse(SubScene scene, Node root) {
	        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
	            @Override public void handle(MouseEvent me) {
	                mousePosX = me.getSceneX();
	                mousePosY = me.getSceneY();
	                mouseOldX = me.getSceneX();
	                mouseOldY = me.getSceneY();
	            }
	        });
	        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
	            @Override public void handle(MouseEvent me) {
	                mouseOldX = mousePosX;
	                mouseOldY = mousePosY;
	                mousePosX = me.getSceneX();
	                mousePosY = me.getSceneY();
	                mouseDeltaX = (mousePosX - mouseOldX); 
	                mouseDeltaY = (mousePosY - mouseOldY); 
	                
	                double modifier = 1.0;
	                double modifierFactor = 0.1;
	                
	                if (me.isControlDown()) {
	                    modifier = 0.1;
	                } 
	                if (me.isShiftDown()) {
	                    modifier = 10.0;
	                }     
	                if (me.isPrimaryButtonDown()) {
	                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*modifierFactor*modifier*2.0);  // +
	                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*modifierFactor*modifier*2.0);  // -
	                }
	                else if (me.isSecondaryButtonDown()) {
	                    double z = camera.getTranslateZ();
	                    double newZ = z + mouseDeltaX*modifierFactor*modifier;
	                    camera.setTranslateZ(newZ);
	                }
	                else if (me.isMiddleButtonDown()) {
	                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*modifierFactor*modifier*0.3);  // -
	                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*modifierFactor*modifier*0.3);  // -
	                }
	            }
	        });
	    }
	    
	    private void handleKeyboard(SubScene scene, Node root) {
	        final boolean moveCamera = true;
	        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
	            @Override
	            public void handle(KeyEvent event) {
	                Duration currentTime;
	                switch (event.getCode()) {
	                    case Z:
	                        if (event.isShiftDown()) {
	                            cameraXform.ry.setAngle(0.0);
	                            cameraXform.rx.setAngle(0.0);
	                            camera.setTranslateZ(-300.0);
	                        }   
	                        cameraXform2.t.setX(0.0);
	                        cameraXform2.t.setY(0.0);
	                        break;
	                    case X:
	                        if (event.isControlDown()) {
	                            /**
	                        	if (axisGroup.isVisible()) {
	                                System.out.println("setVisible(false)");
	                                axisGroup.setVisible(false);
	                            }
	                            else {
	                                System.out.println("setVisible(true)");
	                                axisGroup.setVisible(true);
	                            }
	                            **/
	                        }   
	                        break;
	                    case S:
	                        if (event.isControlDown()) {
	                            if (volumeGroup.isVisible()) {
	                                volumeGroup.setVisible(false);
	                            }
	                            else {
	                                volumeGroup.setVisible(true);
	                            }
	                        }   
	                        break;
	                    case SPACE:
	                        if (timelinePlaying) {
	                            timeline.pause();
	                            timelinePlaying = false;
	                        }
	                        else {
	                            timeline.play();
	                            timelinePlaying = true;
	                        }
	                        break;
	                    case UP:
	                        if (event.isControlDown() && event.isShiftDown()) {
	                            cameraXform2.t.setY(cameraXform2.t.getY() - 10.0*CONTROL_MULTIPLIER);  
	                        }  
	                        else if (event.isAltDown() && event.isShiftDown()) {
	                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0*ALT_MULTIPLIER);  
	                        }
	                        else if (event.isControlDown()) {
	                            cameraXform2.t.setY(cameraXform2.t.getY() - 1.0*CONTROL_MULTIPLIER);  
	                        }
	                        else if (event.isAltDown()) {
	                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0*ALT_MULTIPLIER);  
	                        }
	                        else if (event.isShiftDown()) {
	                            double z = camera.getTranslateZ();
	                            double newZ = z + 5.0*SHIFT_MULTIPLIER;
	                            camera.setTranslateZ(newZ);
	                        }
	                        break;
	                    case DOWN:
	                        if (event.isControlDown() && event.isShiftDown()) {
	                            cameraXform2.t.setY(cameraXform2.t.getY() + 10.0*CONTROL_MULTIPLIER);  
	                        }  
	                        else if (event.isAltDown() && event.isShiftDown()) {
	                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0*ALT_MULTIPLIER);  
	                        }
	                        else if (event.isControlDown()) {
	                            cameraXform2.t.setY(cameraXform2.t.getY() + 1.0*CONTROL_MULTIPLIER);  
	                        }
	                        else if (event.isAltDown()) {
	                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0*ALT_MULTIPLIER);  
	                        }
	                        else if (event.isShiftDown()) {
	                            double z = camera.getTranslateZ();
	                            double newZ = z - 5.0*SHIFT_MULTIPLIER;
	                            camera.setTranslateZ(newZ);
	                        }
	                        break;
	                    case RIGHT:
	                        if (event.isControlDown() && event.isShiftDown()) {
	                            cameraXform2.t.setX(cameraXform2.t.getX() + 10.0*CONTROL_MULTIPLIER);  
	                        }  
	                        else if (event.isAltDown() && event.isShiftDown()) {
	                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0*ALT_MULTIPLIER);  
	                        }
	                        else if (event.isControlDown()) {
	                            cameraXform2.t.setX(cameraXform2.t.getX() + 1.0*CONTROL_MULTIPLIER);  
	                        }
	                        else if (event.isAltDown()) {
	                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0*ALT_MULTIPLIER);  
	                        }
	                        break;
	                    case LEFT:
	                        if (event.isControlDown() && event.isShiftDown()) {
	                            cameraXform2.t.setX(cameraXform2.t.getX() - 10.0*CONTROL_MULTIPLIER);  
	                        }  
	                        else if (event.isAltDown() && event.isShiftDown()) {
	                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0*ALT_MULTIPLIER);  // -
	                        }
	                        else if (event.isControlDown()) {
	                            cameraXform2.t.setX(cameraXform2.t.getX() - 1.0*CONTROL_MULTIPLIER);  
	                        }
	                        else if (event.isAltDown()) {
	                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0*ALT_MULTIPLIER);  // -
	                        }
	                        break;
	                }
	            }
	        });
	    }

		
	public void generateVolumeScene(Group root3D) {
		Utils.debugNewLine("generateVolumeScene", true);

		subScene.setFill(Color.GREEN);
	}
	
	public void buildVolume(List<Box> volumeVoxels){
		
		
		Xform volumeXform = new Xform();
	    for (Box voxel: volumeVoxels){
	    	volumeXform.getChildren().add(voxel); 
	    }
	    volumeGroup.getChildren().add(volumeXform);
	    world.getChildren().addAll(volumeGroup); 
	 		
	}
	
	public void generateVolumeScene(List<Box> volumeVoxels){
		Utils.debugNewLine("GenerateVolumeScene from voxels", true);
		initConfig();
        root.getChildren().add(world);
        buildCamera();
        buildAxes();
		buildVolume(volumeVoxels);
 	
		subScene = new SubScene(root, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
		handleKeyboard(subScene, world);
	    handleMouse(subScene, world);
		subScene.setFill(Color.WHITE);
	}
		
	public SubScene getSubScene() {
		return subScene;
	}

	public BoxParameters getVolumeBoxParameters() {
		return volumeBoxParameters;
	}

	public void setVolumeBoxParametersTest(BoxParameters volumeBoxParameters) {
		this.volumeBoxParameters = volumeBoxParameters;
	}

	public void updateCameraDistance(double cameraDistance){
		this.cameraDistance = cameraDistance;
		camera.setTranslateZ(-cameraDistance);
	}
	
}
