package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import nl.tue.vc.application.ApplicationConfiguration;
import nl.tue.vc.projection.TransformMatrices;
import nl.tue.vc.projection.Vector3D;
import nl.tue.vc.projection.VolumeModel;

import java.lang.Math;

public class VolumeGenerator {

	private Octree octree;
	private Group octreeVolume;
	private List<int[][]> sourceArrays;
	private List<int[][]> transformedArrays;
	private List<BufferedImage> bufferedImagesForTest;
	private TransformMatrices transformMatrices;
	private int fieldOfView;

	public VolumeGenerator(Octree octree, BoxParameters boxParameters) 
	{
		this.octree = octree;
		this.bufferedImagesForTest = this.octree.getBufferedImagesForTest();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		if (octree == null) {
			this.octreeVolume = getDefaultVolume(boxParameters);
		} else {
			this.octreeVolume = generateVolume();
		}
		
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
	}
	
	public VolumeGenerator(Octree octree, BoxParameters boxParameters, List<int[][]> sourceBinaryArrays,
			List<int[][]> transformedBinaryArrays) {
//		this(octree, boxParameters);		
		this.sourceArrays = sourceBinaryArrays;
		this.transformedArrays = transformedBinaryArrays;
		
		this.octree = octree;
		this.bufferedImagesForTest = this.octree.getBufferedImagesForTest();
		System.out.println("BufferedImagesForTest: " + this.bufferedImagesForTest.size());
		if (octree == null) {
			this.octreeVolume = getDefaultVolume(boxParameters);
		} else {
			this.octreeVolume = generateVolume();
		}
		
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
	}

	private Box generateTestBox(int size, int posx, int posy, int posz, Color color) {
		Box box = new Box(size, size, size);
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = color;
		textureMaterial.setDiffuseColor(diffuseColor);
		// textureMaterial.setDiffuseMap()
		box.setMaterial(textureMaterial);
		return box;
	}

	public Group generateVolume() {
		//System.out.println("BOX = " + boxParameters);
		Group volume = new Group();
		Node root = octree.getRoot();
		root = octree.generateOctreeFractal(0);
		BoxParameters boxParameters = root.getBoxParameters();
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		System.out.println("Children: " + root.getChildren().length);

		// First line of children
//		for (int i = 0; i < root.getChildren().length; i++) {
//
//			DeltaStruct displacementDirections = computeDeltaDirections(i);
//			int newBoxSize = boxParameters.getBoxSize() / 2;
//			BoxParameters childrenParameters = new BoxParameters();
//			childrenParameters.setBoxSize(newBoxSize);
//			childrenParameters.setCenterX(boxParameters.getCenterX());
//			childrenParameters.setCenterY(boxParameters.getCenterY());
//			childrenParameters.setCenterZ(boxParameters.getCenterZ());
//
//			Node childNode = root.getChildren()[i];
//			// System.out.println("Index: "+ i + ", " + displacementDirections.toString());
//			//System.out.println("generateVolume | Nodecolor = " + childNode.getColor().toString());
//			List<Box> voxels = generateVolumeAux(childNode, childrenParameters, displacementDirections);
//			volume.getChildren().addAll(voxels);
//		}

//		List<Box> voxels = generateVolumeAux(root, boxParameters, deltas);
		Group voxels = generateVolumeAux(root, boxParameters, deltas);
		volume.getChildren().addAll(voxels);
		return volume;
	}

	private Group generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		Group voxels = new Group();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			currentNode.setBoxParameters(currentParameters);
			currentNode.setDeltaStruct(currentDeltas);
			Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
			voxels.getChildren().addAll(box);
			System.out.println("Root is leaf");
		} else {
			System.out.println("Root is Node");
			Node[] children = currentNode.getChildren();
			int newBoxSize = currentParameters.getBoxSize() / 2;
			BoxParameters newParameters = new BoxParameters();
			newParameters.setBoxSize(newBoxSize);
			newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newBoxSize));
			newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newBoxSize));
			newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newBoxSize));

			for (int i = 0; i < children.length; i++) {
				// compute deltaX, deltaY, and deltaZ for new voxels
				Node childNode = children[i];
				if (childNode != null) {
					childNode.setBoxParameters(newParameters);
					DeltaStruct displacementDirections = computeDeltaDirections(i);
					childNode.setDeltaStruct(displacementDirections);
				
					Group innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
					voxels.getChildren().addAll(innerBoxes);				
					Group projections = getProjections(newParameters);
					voxels.getChildren().addAll(projections);
				}
			}
		}
		return voxels;
	}
	
	public Group getProjections(BoxParameters boxParameters) {
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		
		//TransformMatrices transformMatrices = new TransformMatrices(400, 290, 32.3);
		VolumeModel volumeModel = new VolumeModel(boxParameters);
		
		double leftMostPos = transformMatrices.screenWidth;
		double rightMostPos = 0;
		double topMostPos = transformMatrices.screenHeight;
		double bottomMostPos = 0;
		
		for (Vector3D vector: volumeModel.modelVertices) {
			
			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);
			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);
			
			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getY()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
			}
			
			Vector3D ndcVector = transformMatrices.toNDCCoordinates(clipVector);
			Vector3D windowVector = transformMatrices.toWindowCoordinates(ndcVector);
			projectedPoints.add(windowVector);
			
			if (windowVector.getX() > rightMostPos) {
				rightMostPos = windowVector.getX();
			} else if (windowVector.getX() < leftMostPos) {
				leftMostPos = windowVector.getX();
			}
			
			if (windowVector.getY() > bottomMostPos) {
				bottomMostPos = windowVector.getY();
			} else if (windowVector.getY() < topMostPos) {
				topMostPos = windowVector.getY();
			}
		}
		
		Group root2D = new Group();
		
		Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(0), null);
		Rectangle imageRect = new Rectangle();
		imageRect.setX(0);//imageBoxParameters.getCenterX() - (img.getWidth() / 2));
		imageRect.setY(0);//imageBoxParameters.getCenterY() - (img.getHeight() / 2));
		imageRect.setWidth(img.getWidth());
		System.out.println("img width: " + img.getWidth() + ", height: " + img.getHeight());
		imageRect.setHeight(img.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.BLACK);
		//root2D.getChildren().add(imageRect);
		
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		boundingBox.setFill(Color.CHARTREUSE);
		boundingBox.setStroke(Color.BLACK);
		System.out.println("("+boundingBox.getX()+","+boundingBox.getY()+") - ("+boundingBox.getX()+","+(boundingBox.getY()+boundingBox.getHeight())+")");
		root2D.getChildren().add(boundingBox);
		
		int[][] transformedArray = transformedArrays.get(0);
		int xVal = (int) boundingBox.getX();
		int yVal = (int) (boundingBox.getY()+boundingBox.getHeight());
		if(xVal < 0) {
			xVal = 0;
		}
		if(yVal < 0) {
			yVal = 0;
		}
		System.out.println("xVal = " + xVal + ", yVal = " + yVal);
		int transformedValue = transformedArray[xVal][yVal];
		
		System.out.println("transformedValue: " + transformedValue);
		
		int determiningValue = (int) boundingBox.getWidth();
		if(boundingBox.getHeight()<boundingBox.getWidth()) {
			determiningValue = (int) boundingBox.getHeight();
		}
		
		if (transformedValue >= determiningValue) {
			System.out.println("Projection is totally inside");
		} else if((transformedValue < determiningValue) && (transformedValue > 0)) {
			System.out.println("Projection is partially inside");
		}
		else {
			System.out.println("Projection is outside");
		}
		
		List<Color> cornerColors = new ArrayList<Color>(); 
		cornerColors.add(Color.RED);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		cornerColors.add(Color.BLACK);
		
//		cornerColors.add(Color.RED);
//		cornerColors.add(Color.BLACK);
//		cornerColors.add(Color.GREEN);
//		cornerColors.add(Color.YELLOW);
//		cornerColors.add(Color.GRAY);
//		cornerColors.add(Color.BROWN);
//		cornerColors.add(Color.CYAN);
//		cornerColors.add(Color.ORANGE);
		
		int i=0;
		for (Vector3D point: projectedPoints) {
			Ellipse circle = new Ellipse(point.getX(), point.getY(), 4, 4);
			circle.setFill(cornerColors.get(i));
			root2D.getChildren().add(circle);
			i++;
		}
		
		
//		for (Vector3D point: volumeModel.modelVertices) {
//			Ellipse circle = new Ellipse(point.getX(), point.getY(), 2, 2);
//			circle.setFill(Color.RED);
//			root2D.getChildren().add(circle);
//		}
		
		// draw the lines
		Line line1 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(7).getX(), projectedPoints.get(7).getY());
		line1.getStrokeDashArray().addAll(2d);
		line1.setFill(Color.BLUE);
		root2D.getChildren().add(line1);
		
		Line line2 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(5).getX(), projectedPoints.get(5).getY());
		line2.getStrokeDashArray().addAll(2d);
		line2.setFill(Color.BLUE);
		root2D.getChildren().add(line2);

		Line line3 = new Line(projectedPoints.get(4).getX(), projectedPoints.get(4).getY(),
				projectedPoints.get(0).getX(), projectedPoints.get(0).getY());
		line3.getStrokeDashArray().addAll(2d);
		line3.setFill(Color.BLUE);
		root2D.getChildren().add(line3);
		
		Line line4 = new Line(projectedPoints.get(7).getX(), projectedPoints.get(7).getY(),
				projectedPoints.get(3).getX(), projectedPoints.get(3).getY());
		line4.getStrokeDashArray().addAll(2d);
		line4.setFill(Color.BLUE);
		root2D.getChildren().add(line4);
		
		Line line5 = new Line(projectedPoints.get(7).getX(), projectedPoints.get(7).getY(),
				projectedPoints.get(6).getX(), projectedPoints.get(6).getY());
		line5.getStrokeDashArray().addAll(2d);
		line5.setFill(Color.BLUE);
		root2D.getChildren().add(line5);

		Line line6 = new Line(projectedPoints.get(5).getX(), projectedPoints.get(5).getY(),
				projectedPoints.get(6).getX(), projectedPoints.get(6).getY());
		line6.getStrokeDashArray().addAll(2d);
		line6.setFill(Color.BLUE);
		root2D.getChildren().add(line6);

		Line line7 = new Line(projectedPoints.get(5).getX(), projectedPoints.get(5).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line7.getStrokeDashArray().addAll(2d);
		line7.setFill(Color.BLUE);
		root2D.getChildren().add(line7);
		
		Line line8 = new Line(projectedPoints.get(6).getX(), projectedPoints.get(6).getY(),
				projectedPoints.get(2).getX(), projectedPoints.get(2).getY());
		line8.getStrokeDashArray().addAll(2d);
		line8.setFill(Color.BLUE);
		root2D.getChildren().add(line8);
		
		Line line9 = new Line(projectedPoints.get(0).getX(), projectedPoints.get(0).getY(),
				projectedPoints.get(3).getX(), projectedPoints.get(3).getY());
		line9.getStrokeDashArray().addAll(2d);
		line9.setFill(Color.BLUE);
		root2D.getChildren().add(line9);
		
		Line line10 = new Line(projectedPoints.get(0).getX(), projectedPoints.get(0).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line10.getStrokeDashArray().addAll(2d);
		line10.setFill(Color.BLUE);
		root2D.getChildren().add(line10);

		Line line11 = new Line(projectedPoints.get(3).getX(), projectedPoints.get(3).getY(),
				projectedPoints.get(2).getX(), projectedPoints.get(2).getY());
		line11.getStrokeDashArray().addAll(2d);
		line11.setFill(Color.BLUE);
		root2D.getChildren().add(line11);

		Line line12 = new Line(projectedPoints.get(2).getX(), projectedPoints.get(2).getY(),
				projectedPoints.get(1).getX(), projectedPoints.get(1).getY());
		line12.getStrokeDashArray().addAll(2d);
		line12.setFill(Color.BLUE);
		root2D.getChildren().add(line12);
		
		int sceneWidth = 440;
		int sceneHeight = 320;
		int sceneDepth = 320;
		int scalingParameter = 10;
		Box volume = new Box(volumeModel.xLength * scalingParameter, 
				volumeModel.yLength * scalingParameter,
				volumeModel.zLength * scalingParameter);
		
		volume.setTranslateX(sceneWidth);
		volume.setTranslateY(sceneHeight);
		//volume.setTranslateZ(volumePositionZ);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(Color.BLUE);
		volume.setMaterial(textureMaterial);
		//root2D.getChildren().add(volume);
		
		return root2D;
	}
	
	private List<Box> generateVolumeAuxOld(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		//System.out.println("generateVolumeAux | Nodecolor = " + currentNode.getColor().toString());
		List<Box> voxels = new ArrayList<Box>();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			// working with leafs
			if (currentNode.getColor() != Color.WHITE) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
				voxels.add(box);
			}
		} else {
			Node[] children = currentNode.getChildren();
			int newBoxSize = currentParameters.getBoxSize() / 2;

			BoxParameters newParameters = new BoxParameters();
			newParameters.setBoxSize(newBoxSize);
			newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newBoxSize));
			newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newBoxSize));
			newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newBoxSize));

			for (int i = 0; i < children.length; i++) {

				// compute deltaX, deltaY, and deltaZ for new voxels

				Node childNode = children[i];
				if (childNode != null) {
					DeltaStruct displacementDirections = computeDeltaDirections(i);

					// System.out.println("Index: "+ i + ", " + displacementDirections.toString());
//					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
//					voxels.addAll(innerBoxes);
				}
			}
			// System.out.println("<< Internal node ..");
		}

		return voxels;
	}

	private DeltaStruct computeDeltaDirections(int index) {
		DeltaStruct deltas = new DeltaStruct();
		switch (index) {
		case 0:
			deltas.deltaX = -1;
			deltas.deltaY = 1;
			deltas.deltaZ = 1;
			break;
		case 1:
			deltas.deltaX = 1;
			deltas.deltaY = 1;
			deltas.deltaZ = 1;
			break;
		case 2:
			deltas.deltaX = -1;
			deltas.deltaY = -1;
			deltas.deltaZ = 1;
			break;
		case 3:
			deltas.deltaX = 1;
			deltas.deltaY = -1;
			deltas.deltaZ = 1;
			break;
		case 4:
			deltas.deltaX = -1;
			deltas.deltaY = 1;
			deltas.deltaZ = -1;
			break;
		case 5:
			deltas.deltaX = 1;
			deltas.deltaY = 1;
			deltas.deltaZ = -1;
			break;
		case 6:
			deltas.deltaX = -1;
			deltas.deltaY = -1;
			deltas.deltaZ = -1;
			break;
		case 7:
			deltas.deltaX = 1;
			deltas.deltaY = -1;
			deltas.deltaZ = -1;
			break;
		default:
			throw new RuntimeException("Invalid index value " + index);
		}

		return deltas;
	}

	private Box generateVoxel(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor) {
		//get the scene dimensions
		ApplicationConfiguration appConfig = ApplicationConfiguration.getInstance();
		int sceneWidth = 400;//appConfig.getVolumeSceneWidth();
		int sceneHeight = 290;//appConfig.getVolumeSceneHeight();
		int sceneDepth = 100;//appConfig.getVolumeSceneDepth();
//		int volumeBoxSize = appConfig.getVolumeBoxSize();
		int scalingParameter = 1;
		Box box = new Box(boxParameters.getBoxSize()*scalingParameter, boxParameters.getBoxSize()*scalingParameter, boxParameters.getBoxSize()*scalingParameter);
		scalingParameter = 1;
		int posx = (sceneWidth + (deltas.deltaX * boxParameters.getBoxSize() / 2))*scalingParameter;
		int posy = (sceneHeight + (deltas.deltaY * boxParameters.getBoxSize() / 2))*scalingParameter;
		int posz = (sceneDepth + (deltas.deltaZ * boxParameters.getBoxSize() / 2))*scalingParameter;
		
		System.out.println("center: [" + boxParameters.getCenterX() + ", " + boxParameters.getCenterY()
				+ ", " + boxParameters.getCenterZ()+"]");

		System.out.println("deltas: [" + deltas.deltaX + ", " + deltas.deltaY
		+ ", " + deltas.deltaZ+"]");
		
		/**
		 * compute the coordinates of all 8 corners of the cube according to the following numbering 
		 *       
		 *     5 ------------------ 6   
		 *    /|                   / |
		 *  1 -|------------------ 2 |
		 *   | |                  |  |
		 *   | |                  |  |
		 *   | 7 -----------------|- 8
		 *   |/                   | / 
		 *  3 ------------------- 4 
		 *    
		 */
		int centerX = boxParameters.getCenterX();
		int centerY = boxParameters.getCenterY();
		int centerZ = boxParameters.getCenterZ();
		int halfSize = boxParameters.getBoxSize()/2;
		
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		
		//TransformMatrices transformMatrices = new TransformMatrices(400, 290, 32.3);
		VolumeModel volumeModel = new VolumeModel(boxParameters);
		
		double leftMostPos = transformMatrices.screenWidth;
		double rightMostPos = 0;
		double topMostPos = transformMatrices.screenHeight;
		double bottomMostPos = 0;
		
		for (Vector3D vector: volumeModel.modelVertices) {
			//System.out.println("\nVector in world coordinates");
			//System.out.println(vector);
			
			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);
			//System.out.println("\nVector in view coordinates");
			//System.out.println(viewVector);
			
			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);
			//System.out.println("\nVector in clip coordinates");
			//System.out.println(clipVector);
			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getY()) > Math.abs(clipVector.getW()) ||
					Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
				//System.out.println("We should ignore: " + clipVector.toString());
			}
			
			Vector3D ndcVector = transformMatrices.toNDCCoordinates(clipVector);
			//System.out.println("\nVector in ndc coordinates");
			//System.out.println(ndcVector);
			
			Vector3D windowVector = transformMatrices.toWindowCoordinates(ndcVector);
			//System.out.println("\nVector in window coordinates");
			//System.out.println(windowVector);
			projectedPoints.add(windowVector);
			
			if (windowVector.getX() > rightMostPos) {
				rightMostPos = windowVector.getX();
			} else if (windowVector.getX() < leftMostPos) {
				leftMostPos = windowVector.getX();
			}
			
			if (windowVector.getY() > bottomMostPos) {
				bottomMostPos = windowVector.getY();
			} else if (windowVector.getY() < topMostPos) {
				topMostPos = windowVector.getY();
			}
		}

		//root2D = new Group();
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		boundingBox.setFill(Color.CHARTREUSE);
		boundingBox.setStroke(Color.BLACK);
		//root2D.getChildren().add(boundingBox);
		//System.out.println("Bounding box width = " + boundingBox.getWidth() + ", h = " + boundingBox.getHeight());
				
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}
	
	private Box generateVoxelOld(BoxParameters boxParameters, DeltaStruct deltas, Color nodeColor, Boolean testIntersection) {
		Box box = new Box(boxParameters.getBoxSize(), boxParameters.getBoxSize(), boxParameters.getBoxSize());

		int posx = boxParameters.getCenterX() + (deltas.deltaX * boxParameters.getBoxSize() / 2);
		int posy = boxParameters.getCenterY() + (deltas.deltaY * boxParameters.getBoxSize() / 2);
		int posz = boxParameters.getCenterZ() + (deltas.deltaZ * boxParameters.getBoxSize() / 2);

		System.out.println("x: " + boxParameters.getCenterX() + ", y: " + boxParameters.getCenterY() + ", z: "
				+ boxParameters.getCenterY());
		System.out.println("Position {x: " + posx + ", y: " + posy + ", z: " + posz + "}, Size: "
				+ boxParameters.getBoxSize() + "\n");
		Color diffuseColor = nodeColor;
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);

		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		Bounds boxBounds = box.getBoundsInLocal();
		return box;
	}
	
	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;
		if(currentColor == Color.BLACK) {
			result = newColor;
		}
		else if(currentColor == Color.GRAY) {
			if(newColor==Color.WHITE)
				result = Color.WHITE;
			else
				result = currentColor;
		}
		else {
			result = Color.WHITE;
		}
		
		return result;
	}

	public Group getVolume() {
		return octreeVolume;
	}

	public Group getDefaultVolume(BoxParameters boxParameters) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		//Box box = generateVoxel(boxParameters, deltas, Color.CYAN, false);
		Box box = generateVoxel(boxParameters, deltas, Color.CYAN);
		
		Group volume = new Group();
		volume.getChildren().addAll(box);
		
		String file_path = "C:\\Tools\\eclipse\\workspace\\objectrecognizer\\ObjectRecognizer\\images\\football.jpg";
		File input = new File(file_path);
		Rectangle rec = new Rectangle();
		rec.setX(5+boxParameters.getCenterX()-(boxParameters.getBoxSize()/2));
		rec.setY(boxParameters.getCenterY());//-(boxParameters.getBoxSize()/2));
		rec.setWidth(boxParameters.getBoxSize());
		rec.setHeight(boxParameters.getBoxSize());
		Image img = new Image(input.toURI().toString());
		rec.setFill(new ImagePattern(img));
		//volume.getChildren().add(rec);
		
		return volume;
	}

	public List<int[][]> getSourceArrays() {
		return sourceArrays;
	}

	public void setSourceArrays(List<int[][]> sourceBinaryArrays) {
		this.sourceArrays = sourceBinaryArrays;
	}

	public List<int[][]> getTransformedArrays() {
		return transformedArrays;
	}

	public void setTransformedArrays(List<int[][]> transformedBinaryArray) {
		this.transformedArrays = transformedBinaryArray;
	}

	public List<BufferedImage> getBufferedImagesForTest() {
		return bufferedImagesForTest;
	}

	public void setBufferedImagesForTest(List<BufferedImage> bufferedImagesForTest) {
		this.bufferedImagesForTest = bufferedImagesForTest;
	}
	
	public TransformMatrices getTransformMatrices() {
		return transformMatrices;
	}

	public void setTransformMatrices(TransformMatrices transformMatrices) {
		this.transformMatrices = transformMatrices;
	}

	public int getFieldOfView() {
		return fieldOfView;
	}

	public void setFieldOfView(int fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

}
