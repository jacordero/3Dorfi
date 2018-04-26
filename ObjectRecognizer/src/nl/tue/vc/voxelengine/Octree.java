package nl.tue.vc.voxelengine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Mat;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
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
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.TransformMatrices;
import nl.tue.vc.projection.Vector3D;
import nl.tue.vc.projection.VolumeModel;

public class Octree {

	private Node root;

	private int boxSize;
	private InternalNode node;
	private BoxParameters boxParameters;
	private List<BufferedImage> bufferedImagesForTest;
	private Group octreeVolume;
	private List<int[][]> sourceArrays;
	private List<int[][]> transformedArrays;
	private TransformMatrices transformMatrices;
	private int fieldOfView;

	/**
	 *        +---------+-----------+
	 *       +     2   +    3     + |
	 *     + -------+-----------+   |
	 *   +    6   +     7    +  | 3 |
	 *  ---------------------   | + |
	 *  |        |          | 7 +   |
	 *  |    6   |    7     | + | 1 + 
	 *  --------------------+   | + 
	 *  |        |          | 5 + 
	 *  |    4   |    5     | + 
	 *  --------------------+ 
	 *    
	 */

	public Octree(int boxSize) {
		this.boxSize = boxSize;
		root = new InternalNode(Color.BLACK, boxSize);
		this.node = new InternalNode(Color.BLACK, boxSize);
		bufferedImagesForTest = new ArrayList<BufferedImage>();
		this.octreeVolume = new Group();
		this.fieldOfView = 32;
		this.transformMatrices = new TransformMatrices(400, 290, fieldOfView);
	}

	public Octree(int boxSize, int level, BoxParameters boxParameters) {
		this(boxSize, boxParameters);
	}

	public Octree(int boxSize, BoxParameters boxParameters) {
		this(boxSize);
		//this.boxSize = boxSize;
		//root = new InternalNode(Color.BLACK, boxSize);
		//this.node = new InternalNode(Color.BLACK, boxSize);
		//bufferedImagesForTest = new ArrayList<BufferedImage>();
		//this.octreeVolume = new Group();
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		root.setDeltaStruct(deltas);
		root.setBoxParameters(boxParameters);
		this.setBoxParameters(boxParameters);
	}

	public Node getRoot() {
		return root;
	}

	public Node generateOctreeFractal(int level) {
		DeltaStruct deltas = root.getDeltaStruct();
		BoxParameters params = root.getBoxParameters();
		root = generateOctreeFractalAux(level);
		root.setBoxParameters(params);
		root.setDeltaStruct(deltas);
		return root;
	}

	public void generateOctreeTest(int parentBoxSize) {
		Random random = new Random();
		int level = random.nextInt(3) + 1;
		System.out.println("Level for octree test: " + level);
		generateOctreeFractal(level);
	}

	public void generateOctreeTest(int parentBoxSize, int level) {
		System.out.println("Level for octree test: " + level);
		generateOctreeFractal(level);
	}

	private Node generateOctreeFractalAux(int level) {
		int nodesBoxSize = this.boxSize / 2;
		if (level == 0) {
			return generateInternalNode(nodesBoxSize);
		} else {
			Node internalNode = new InternalNode(Color.BLACK, this.boxSize);
			// create node 0
			// internalNode.getChildren()[0] = new Leaf(Color.BLACK, nodesBoxSize);
			internalNode.getChildren()[0] = generateOctreeFractalAux(level - 1);

			// create node 1
			// internalNode.getChildren()[1] = new Leaf(Color.BLUE, nodesBoxSize);
			internalNode.getChildren()[1] = generateOctreeFractalAux(level - 1);

			// create node 2
			// internalNode.getChildren()[2] = new Leaf(Color.BLUEVIOLET, nodesBoxSize);
			internalNode.getChildren()[2] = generateOctreeFractalAux(level - 1);

			// create node 3
			// internalNode.getChildren()[3] = new Leaf(Color.DARKGREEN, nodesBoxSize);
			internalNode.getChildren()[3] = generateOctreeFractalAux(level - 1);

			// create node 4
			internalNode.getChildren()[4] = generateOctreeFractalAux(level - 1);
			// internalNode.getChildren()[4] = new Leaf(Color.DARKORANGE, nodesBoxSize);

			// create node 5
			// internalNode.getChildren()[5] = new Leaf(Color.MAROON, nodesBoxSize);
			internalNode.getChildren()[5] = generateOctreeFractalAux(level - 1);

			// create node 6
			// internalNode.getChildren()[6] = new Leaf(Color.RED, nodesBoxSize);
			internalNode.getChildren()[6] = generateOctreeFractalAux(level - 1);

			// create node 7
			internalNode.getChildren()[7] = generateOctreeFractalAux(level - 1);
			// internalNode.getChildren()[7] = new Leaf(Color.PINK, nodesBoxSize);

			return internalNode;
		}

	}

	private Node generateInternalNode(int boxSize) {

		node.getChildren()[0] = new Leaf(Color.BLACK, boxSize / 2);

		// create node 1
		node.getChildren()[1] = new Leaf(Color.RED, boxSize / 2);

		// create node 2
		node.getChildren()[2] = new Leaf(Color.GREEN, boxSize / 2);

		// create node 3
		node.getChildren()[3] = new Leaf(Color.YELLOW, boxSize / 2);

		// create node 4
		node.getChildren()[4] = new Leaf(Color.GRAY, boxSize / 2);

		// create node 5
		node.getChildren()[5] = new Leaf(Color.BROWN, boxSize / 2);

		// create node 6
		node.getChildren()[6] = new Leaf(Color.CYAN, boxSize / 2);

		// create node 7
		node.getChildren()[7] = new Leaf(Color.ORANGE, boxSize / 2);

		return node;
	}

	public InternalNode getInernalNode() {
		return this.node;
	}

	public BoxParameters getBoxParameters() {
		return boxParameters;
	}

	public void setBoxParameters(BoxParameters boxParameters) {
		this.boxParameters = boxParameters;
	}

	public List<BufferedImage> getBufferedImagesForTest() {
		return bufferedImagesForTest;
	}

	public void setBufferedImagesForTest(List<BufferedImage> bufferedImagesForTest) {
		this.bufferedImagesForTest = bufferedImagesForTest;
	}

	public Group getOctreeVolume() {
		return generateVolume();
	}

	public Group getOctreeTestVolume(int level) {
		return generateTestedVolume(level);
	}

	public void setOctreeVolume(Group octreeVolume) {
		this.octreeVolume = octreeVolume;
	}

	public Group generateVolume() {
		Group volume = new Group();
		//int level = 2;
		//for(int i=0; i<level;i++) {
			//System.out.println("###################### Iteration " + i+1 + " ########################");
			Node root = generateOctreeFractal(2);
			System.out.println("Children: " + root.getChildren().length);
			//List<Box> voxels = generateVolumeAux(root, getBoxParameters(), root.getDeltaStruct());
			Group voxels = generateVolumeAux(root, getBoxParameters(), root.getDeltaStruct());
			volume.getChildren().addAll(voxels);
		//}
		
		return volume;
	}

	private Group generateVolumeAux(Node currentNode, BoxParameters currentParameters, DeltaStruct currentDeltas) {
		//List<Box> voxels = new ArrayList<Box>();
		Group voxels = new Group();

		if (currentNode == null) {
			return voxels;
		}

		if (currentNode.isLeaf()) {
			currentNode.setBoxParameters(currentParameters);
			currentNode.setDeltaStruct(currentDeltas);
			//Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
			//voxels.getChildren().addAll(box);
			
			Color boxColor = Color.GRAY;
			IntersectionStatus status = testIntersection(currentParameters);
			Box box = new Box();
			if(status == IntersectionStatus.INSIDE) {
				boxColor = getPaintColor(currentNode.getColor(), Color.BLACK);
				box = generateVoxel(currentParameters, currentDeltas, boxColor);
			} else if(status == IntersectionStatus.PARTIAL){
				boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
				box = generateVoxel(currentParameters, currentDeltas, boxColor);
			} else {
				boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
				box = generateVoxel(currentParameters, currentDeltas, boxColor);
			}
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
//					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
//					voxels.addAll(innerBoxes);
					Color boxColor = Color.GRAY;
					IntersectionStatus status = testIntersection(newParameters);
					Box box = new Box();
					if(status == IntersectionStatus.INSIDE) {
						boxColor = getPaintColor(childNode.getColor(), Color.BLACK);
						box = generateVoxel(newParameters, displacementDirections, boxColor);
					} else if(status == IntersectionStatus.PARTIAL){
						boxColor = getPaintColor(childNode.getColor(), Color.GRAY);
						box = generateVoxel(newParameters, displacementDirections, boxColor);
						Group innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
						voxels.getChildren().addAll(innerBoxes);
					} else {
						boxColor = getPaintColor(childNode.getColor(), Color.WHITE);
						box = generateVoxel(newParameters, displacementDirections, boxColor);
					}
					voxels.getChildren().addAll(box);
					
//					
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
		
		Group root2D = new Group();
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
		
//		if (transformedValue >= boxParameters.getBoxSize()) {
//			diffuseColor = getPaintColor(nodeColor, Color.BLACK);
//		} else if((transformedValue < boxParameters.getBoxSize()) && (transformedValue > 0)) {
//			diffuseColor = getPaintColor(nodeColor, Color.GRAY);
//		}
//		else {
//			diffuseColor = getPaintColor(nodeColor, Color.WHITE);
//		}
		
		for (Vector3D point: projectedPoints) {
			Ellipse circle = new Ellipse(point.getX(), point.getY(), 2, 2);
			circle.setFill(Color.BLACK);
			root2D.getChildren().add(circle);
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
		Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(0), null);
		Rectangle imageRect = new Rectangle();
		imageRect.setX(0);//imageBoxParameters.getCenterX() - (img.getWidth() / 2));
		imageRect.setY(0);//imageBoxParameters.getCenterY() - (img.getHeight() / 2));
		imageRect.setWidth(img.getWidth());
		System.out.println("img width: " + img.getWidth() + ", height: " + img.getHeight());
		imageRect.setHeight(img.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.BLACK);
		root2D.getChildren().add(imageRect);
		return root2D;
	}
	
	public IntersectionStatus testIntersection(BoxParameters boxParameters) {
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		IntersectionStatus status = IntersectionStatus.INSIDE;
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
		
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		System.out.println("("+boundingBox.getX()+","+boundingBox.getY()+") - ("+boundingBox.getX()+","+(boundingBox.getY()+boundingBox.getHeight())+")");
		
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
			status = IntersectionStatus.INSIDE;
		} else if((transformedValue < determiningValue) && (transformedValue > 0)) {
			System.out.println("Projection is partially inside");
			status = IntersectionStatus.PARTIAL;
		}
		else {
			System.out.println("Projection is outside");
			status = IntersectionStatus.OUTSIDE;
		}
		return status;
	}
	
	public Group getProjections() {
		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
		
		//TransformMatrices transformMatrices = new TransformMatrices(400, 290, 22.3);
		VolumeModel volumeModel = new VolumeModel();
		
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
		
		
		Group root2D = new Group();
		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
		boundingBox.setFill(Color.CHARTREUSE);
		boundingBox.setStroke(Color.BLACK);
		System.out.println("("+boundingBox.getX()+","+boundingBox.getY()+") - ("+boundingBox.getX()+","+(boundingBox.getY()+boundingBox.getHeight())+")");
		root2D.getChildren().add(boundingBox);
		
		for (Vector3D point: projectedPoints) {
			Ellipse circle = new Ellipse(point.getX(), point.getY(), 2, 2);
			circle.setFill(Color.BLACK);
			root2D.getChildren().add(circle);
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
		Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(0), null);
		Rectangle imageRect = new Rectangle();
		imageRect.setX(0);//imageBoxParameters.getCenterX() - (img.getWidth() / 2));
		imageRect.setY(0);//imageBoxParameters.getCenterY() - (img.getHeight() / 2));
		imageRect.setWidth(img.getWidth());
		System.out.println("img width: " + img.getWidth() + ", height: " + img.getHeight());
		imageRect.setHeight(img.getHeight());
		imageRect.setFill(new ImagePattern(img));
		imageRect.setStroke(Color.YELLOW);
		root2D.getChildren().add(imageRect);
		return root2D;
	}
	
	public Group generateTestedVolume(int level) {
		Group volume = new Group();
		Node root = getRoot();
		System.out.println("Children: " + root.getChildren().length);
		// List<Box> voxels = generateTestedVolumeAux(root, getBoxParameters(),
		// root.getDeltaStruct(), 0, level);
		List<Box> voxels = new ArrayList<Box>();
		Node[] children = root.getChildren();
		int newBoxSize = root.getBoxSize() / 2;
		BoxParameters newParameters = new BoxParameters();
		newParameters.setBoxSize(newBoxSize);
		newParameters.setCenterX(getBoxParameters().getCenterX() + (root.getDeltaStruct().deltaX * newBoxSize));
		newParameters.setCenterY(getBoxParameters().getCenterY() + (root.getDeltaStruct().deltaY * newBoxSize));
		newParameters.setCenterZ(getBoxParameters().getCenterZ() + (root.getDeltaStruct().deltaZ * newBoxSize));

		for (int i = 0; i < children.length; i++) {
			Node childNode = children[i];
			if (childNode != null) {
				Node[] childNodes = childNode.getChildren();
				System.out.println("Root Child node: " + childNode);
				childNode.setBoxParameters(newParameters);
				DeltaStruct displacementDirections = computeDeltaDirections(i);
				childNode.setDeltaStruct(displacementDirections);
				voxels.addAll(generateTestedVolumeAux(childNode, newParameters, displacementDirections, 0, level));
			}
		}
		volume.getChildren().addAll(voxels);
		return volume;
	}

	private List<Box> generateTestedVolumeAux(Node currentNode, BoxParameters currentParameters,
			DeltaStruct currentDeltas, int initLevel, int maxLevel) {
		List<Box> voxels = new ArrayList<Box>();

		if (initLevel <= maxLevel) {
			if (currentNode == null) {
				return voxels;
			}

			System.out.println("***************** level = " + initLevel + ", Node color = " + currentNode.getColor());

			if (currentNode.getColor() == Color.GRAY) {
				currentNode.getChildren()[0] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[1] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[2] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[3] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[4] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[5] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[6] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);
				currentNode.getChildren()[7] = new InternalNode(Color.BLACK, currentNode.getBoxSize() / 2);

				Node[] children = currentNode.getChildren();
				int newBoxSize = currentParameters.getBoxSize() / 2;
				BoxParameters newParameters = new BoxParameters();
				newParameters.setBoxSize(newBoxSize);
				newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newBoxSize));
				newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newBoxSize));
				newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newBoxSize));

				for (int i = 0; i < children.length; i++) {
					Node childNode = children[i];
					if (childNode != null) {
						System.out.println("Child node in generateTestedVolumeAux: " + childNode);
						childNode.setBoxParameters(newParameters);
						DeltaStruct displacementDirections = computeDeltaDirections(i);
						childNode.setDeltaStruct(displacementDirections);
						voxels.addAll(generateTestedVolumeAux(childNode, newParameters, displacementDirections,
								initLevel + 1, maxLevel));
					}
				}
			} else {// if (currentNode.getColor() == Color.BLACK) {
				Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
				voxels.add(box);
				// } else {
			}
		}
		return voxels;
	}

	// Make the X, Y, and Z coordinates start at the corner of the first (0) node
	// and translate the rest of the nodes to their respective positions
	// Get rid of the center stuff
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
		
//		for (Vector3D point: projectedPoints) {
//			Ellipse circle = new Ellipse(point.getX(), point.getY(), 2, 2);
//			circle.setFill(Color.BLACK);
//			root2D.getChildren().add(circle);
//		}
		
//		Point3D boxCorner1 = new Point3D(centerX - halfSize, centerY + halfSize, centerZ + halfSize);
//		Point3D boxCorner2 = new Point3D(centerX + halfSize, centerY + halfSize, centerZ + halfSize);
//		Point3D boxCorner3 = new Point3D(centerX - halfSize, centerY - halfSize, centerZ + halfSize);
//		Point3D boxCorner4 = new Point3D(centerX + halfSize, centerY - halfSize, centerZ + halfSize);
//		Point3D boxCorner5 = new Point3D(centerX - halfSize, centerY + halfSize, centerZ - halfSize);
//		Point3D boxCorner6 = new Point3D(centerX + halfSize, centerY + halfSize, centerZ - halfSize);
//		Point3D boxCorner7 = new Point3D(centerX - halfSize, centerY - halfSize, centerZ - halfSize);
//		Point3D boxCorner8 = new Point3D(centerX + halfSize, centerY - halfSize, centerZ - halfSize);
//		
//	
//		System.out.println("boxCorner1: [" + boxCorner1.getX() + ", " + boxCorner1.getY()
//		+ ", " + boxCorner1.getZ() +"]");
//		System.out.println("boxCorner2: [" + boxCorner2.getX() + ", " + boxCorner2.getY()
//		+ ", " + boxCorner2.getZ() +"]");
//		System.out.println("boxCorner3: [" + boxCorner3.getX() + ", " + boxCorner3.getY()
//		+ ", " + boxCorner3.getZ() +"]");
//		System.out.println("boxCorner4: [" + boxCorner4.getX() + ", " + boxCorner4.getY()
//		+ ", " + boxCorner4.getZ() +"]");
//		System.out.println("boxCorner5: [" + boxCorner5.getX() + ", " + boxCorner5.getY()
//		+ ", " + boxCorner5.getZ() +"]");
//		System.out.println("boxCorner6: [" + boxCorner6.getX() + ", " + boxCorner6.getY()
//		+ ", " + boxCorner6.getZ() +"]");
//		System.out.println("boxCorner7: [" + boxCorner7.getX() + ", " + boxCorner7.getY()
//		+ ", " + boxCorner7.getZ() +"]");
//		System.out.println("boxCorner8: [" + boxCorner8.getX() + ", " + boxCorner8.getY()
//		+ ", " + boxCorner8.getZ() +"]");
//
//		Point2D projectedCorner1 = new Point2D(focalLength*boxCorner1.getX()/boxCorner1.getZ(), focalLength*boxCorner1.getY()/boxCorner1.getZ());
//		Point2D projectedCorner2 = new Point2D(focalLength*boxCorner2.getX()/boxCorner2.getZ(), focalLength*boxCorner2.getY()/boxCorner2.getZ());
//		Point2D projectedCorner3 = new Point2D(focalLength*boxCorner3.getX()/boxCorner3.getZ(), focalLength*boxCorner3.getY()/boxCorner3.getZ());
//		Point2D projectedCorner4 = new Point2D(focalLength*boxCorner4.getX()/boxCorner4.getZ(), focalLength*boxCorner4.getY()/boxCorner4.getZ());
//		Point2D projectedCorner5 = new Point2D(focalLength*boxCorner5.getX()/boxCorner5.getZ(), focalLength*boxCorner5.getY()/boxCorner5.getZ());
//		Point2D projectedCorner6 = new Point2D(focalLength*boxCorner6.getX()/boxCorner6.getZ(), focalLength*boxCorner6.getY()/boxCorner6.getZ());
//		Point2D projectedCorner7 = new Point2D(focalLength*boxCorner7.getX()/boxCorner7.getZ(), focalLength*boxCorner7.getY()/boxCorner7.getZ());
//		Point2D projectedCorner8 = new Point2D(focalLength*boxCorner8.getX()/boxCorner8.getZ(), focalLength*boxCorner8.getY()/boxCorner8.getZ());
//		
//		System.out.println("projectedCorner1: [" + projectedCorner1.getX() + ", " + projectedCorner1.getY() +"]");
//		System.out.println("projectedCorner2: [" + projectedCorner2.getX() + ", " + projectedCorner2.getY() +"]");
//		System.out.println("projectedCorner3: [" + projectedCorner3.getX() + ", " + projectedCorner3.getY() +"]");
//		System.out.println("projectedCorner4: [" + projectedCorner4.getX() + ", " + projectedCorner4.getY() +"]");
//		System.out.println("projectedCorner5: [" + projectedCorner5.getX() + ", " + projectedCorner5.getY() +"]");
//		System.out.println("projectedCorner6: [" + projectedCorner6.getX() + ", " + projectedCorner6.getY() +"]");
//		System.out.println("projectedCorner7: [" + projectedCorner7.getX() + ", " + projectedCorner7.getY() +"]");
//		System.out.println("projectedCorner8: [" + projectedCorner8.getX() + ", " + projectedCorner8.getY() +"]");
		
//		int projectedX = xCoordLowerLeft / zCoordLowerLeft;// *(focalLength/zCoordLowerLeft);
//		int projectedY = yCoordLowerLeft / zCoordLowerLeft;// *(focalLength/zCoordLowerLeft);
//		System.out.println("Projected x: " + projectedX + ", projected y: " + projectedY);
		
		// TODO: Test the computation of the transformed value for different generated
		// volumes.
		//int lowerLeftYValue = projectedY;// + boxParameters.getBoxSize();
//		int transformedValue;
		Color diffuseColor = nodeColor;
//		for (int i = 0; i < this.getBufferedImagesForTest().size(); i++) {
//			// if (projectedX >= transformedArray.length || projectedX<0 || lowerLeftYValue
//			// >= transformedArray[0].length) {
//			// transformedValue = -1;
//			// System.out.println("Something weird happened here!!!");
//			// } else {
//
//			int[][] transformedArray = transformedArrays.get(i);
//
//			// define the image object and it's corresponding rectangle
//			Image img = SwingFXUtils.toFXImage(this.bufferedImagesForTest.get(i), null);
//			Rectangle imageRect = new Rectangle();
//			imageRect.setX(imageBoxParameters.getCenterX() - (img.getWidth() / 2));
//			imageRect.setY(imageBoxParameters.getCenterY() - (img.getHeight() / 2));
//			imageRect.setWidth(img.getWidth());
//			imageRect.setHeight(img.getHeight());
//			imageRect.setFill(new ImagePattern(img));
//			
////			Bounds boxBounds = box.getBoundsInLocal();
////			System.out.println("Bounds local ----- " + boxBounds);
////			System.out.println("Bounds Image local ----- " + imageRect.getBoundsInLocal());
//
//			int xVal = (int) Math.round(projectedCorner3.getX()) + centerX/2; //projectedX;// Math.abs(((int) (imageRect.getX())-projectedX-1));
//			int yVal = (int) Math.round(projectedCorner3.getY()) + centerY/2; //projectedY;// Math.abs((int) (imageRect.getY())-projectedY-1);
//			System.out.println("imageRect x: " + imageRect.getX() + ", imageRect y: " + imageRect.getY());
//			System.out.println("Getting transformed value for x = " + xVal + ", y = " + yVal);
//			transformedValue = transformedArray[xVal][yVal];
//			// }
//
//			System.out.println("transformedValue: " + transformedValue + " boxSize: " + boxParameters.getBoxSize());
//
//			if (transformedValue >= boxParameters.getBoxSize()) {
//				diffuseColor = getPaintColor(nodeColor, Color.BLACK);
//			} else if ((transformedValue < boxParameters.getBoxSize()) && (transformedValue > 0)) {
//				diffuseColor = getPaintColor(nodeColor, Color.GRAY);
//			} else {
//				diffuseColor = getPaintColor(nodeColor, Color.WHITE);
//			}
//
//		}
		
		box.setTranslateX(posx);
		box.setTranslateY(posy);
		box.setTranslateZ(posz);
		
		PhongMaterial textureMaterial = new PhongMaterial();
		// Color diffuseColor = nodeColor;
		textureMaterial.setDiffuseColor(diffuseColor);
		box.setMaterial(textureMaterial);
		return box;
	}

	public Color getPaintColor(Color currentColor, Color newColor) {
		Color result = Color.GRAY;
		if (currentColor == Color.BLACK) {
			result = newColor;
		} else if (currentColor == Color.GRAY) {
			if (newColor == Color.WHITE)
				result = Color.WHITE;
			else
				result = currentColor;
		} else {
			result = Color.WHITE;
		}

		return result;
	}

	public Group getDefaultVolume(BoxParameters boxParameters) {
		DeltaStruct deltas = new DeltaStruct();
		deltas.deltaX = 0;
		deltas.deltaY = 0;
		deltas.deltaZ = 0;
		Box box = generateVoxel(boxParameters, deltas, Color.CYAN);

		Group volume = new Group();
		volume.getChildren().addAll(box);
		return volume;
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
