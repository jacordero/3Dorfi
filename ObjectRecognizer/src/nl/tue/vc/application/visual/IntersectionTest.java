package nl.tue.vc.application.visual;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import nl.tue.vc.projection.IntersectionStatus;
import nl.tue.vc.projection.Vector3D;
import nl.tue.vc.projection.VolumeModel;
import nl.tue.vc.voxelengine.BoxParameters;
import nl.tue.vc.voxelengine.DeltaStruct;
import nl.tue.vc.voxelengine.Node;
import nl.tue.vc.voxelengine.Octree;

public class IntersectionTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// try {
		// Raster raster =
		// loadImageRaster("C:\\Tools\\eclipse\\workspace\\objectrecognizer\\ObjectRecognizer\\images\\football.jpg");
		// for(int x = 0; x<raster.getWidth(); x++) {
		// for(int y = 0; y<raster.getHeight(); y++) {
		// System.out.print(raster.getSample(x, y, 0)+" ");
		// }
		// System.out.println("");
		// }
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// create the arrays
		int[][] binaryArray = new int[8][8];
		int[][] transformedArray = new int[8][8];

		// initialize some rows to 0
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				binaryArray[x][y] = 0;
			}
		}

		// initialize some rows to 1
		for (int x = 1; x < 6; x++) {
			for (int y = 1; y < 6; y++) {
				binaryArray[x][y] = 1;
			}
		}

		// initialize the remaining rows
		binaryArray[0][2] = 1;
		binaryArray[0][3] = 1;
		binaryArray[0][4] = 1;
		binaryArray[6][2] = 1;
		binaryArray[6][3] = 1;
		binaryArray[6][4] = 1;

		// print the contents of binaryArray
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				System.out.print(binaryArray[x][y] + " ");
			}
			System.out.println("");
		}

		transformedArray = getTransformedArray(binaryArray);

		// print the contents of transformedArray
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				System.out.print(transformedArray[x][y] + " ");
			}
			System.out.println("");
		}
	}

	public static int[][] getTransformedArray(int[][] binaryArray) {
		int[][] transformedArray = new int[binaryArray.length][binaryArray[0].length];
		// populate transformedArray
		for (int x = 0; x < binaryArray.length; x++) {
			for (int y = 0; y < binaryArray[x].length; y++) {
				transformedArray[x][y] = getSquareSize(binaryArray, x, y);
			}
		}
		return transformedArray;
	}

	public static int getSquareSize(int[][] binaryArray, int xValue, int yValue) {
		int sum = 0;
		int size = binaryArray[0].length - yValue;
		// System.out.println("init Size for (" + xValue +","+yValue+") = " + size);
		if (size > (xValue + 1))
			size = xValue + 1;
		// if(xValue==1 && yValue==1)
		// System.out.println("Size for (" + xValue +","+yValue+") = " + size);
		for (int i = 1; i <= size; i++) {
			int validSize = checkSquareSize(i, binaryArray, xValue, yValue);
			// if(xValue==1 && yValue==1)
			// System.out.println("Result for i="+i+" for ("+xValue+","+yValue+") = " +
			// validSize);
			if (validSize > 0)
				sum++;
			else
				break;
		}
		return sum;
	}

	public static int checkSquareSize(int size, int[][] binaryArray, int maxX, int minY) {
		int max = 0;
		max = maxX - size;
		int result = 0;
		int temp = 0;

		for (int x = maxX; x > max; x--) {
			for (int y = minY; y < (minY + size); y++) {
				// if(maxX==1 && minY==1)
				// System.out.println("x="+x+", y="+y);
				int pixel = binaryArray[x][y];
				// if(maxX==1 && minY==1)
				// System.out.println("("+x+","+y+") = " + pixel);
				if (pixel == 0) {
					result -= temp;
					return result;
				} else {
					result++;
					temp++;
				}
			}
		}
		return result;
	}

	public static Raster loadImageRaster(String file_path) throws IOException {
		File input = new File(file_path);
		BufferedImage buf_image = ImageIO.read(input);
		buf_image = binarizeImage(buf_image);
		return buf_image.getData(); // return raster
	}

	public static BufferedImage binarizeImage(BufferedImage img_param) {
		// to binary
		BufferedImage image = new BufferedImage(img_param.getWidth(), img_param.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY);
		Graphics g = image.getGraphics();
		g.drawImage(img_param, 0, 0, null);
		g.dispose();
		return image;
	}

	public static int[][] getBinaryArray(BufferedImage img_param) {
		// to binary
		Raster raster = IntersectionTest.binarizeImage(img_param).getData();
		int[][] result = new int[raster.getHeight()][raster.getWidth()];
		// System.out.println("raster width: " + img_param.getWidth() + " raster height:
		// " + img_param.getHeight());
		// System.out.println("result array rows = " + result.length + ", cols = " +
		// result[0].length);
		// System.out.println("raster minx, y = " + raster.getMinX() + ", " +
		// raster.getMinY());
		for (int x = 0; x < raster.getHeight(); x++) {
			for (int y = 0; y < raster.getWidth(); y++) {
				// System.out.println("pixel(" + x + ", " + y + ")" );
				result[x][y] = (int) raster.getSampleDouble(y, x, 0);
				// System.out.println("pixel(" + x + ", " + y + ") = " +
				// raster.getSampleDouble(x, y, 0));
			}
		}
		return result;
	}

	public static BufferedImage Mat2BufferedImage(Mat matrix) throws Exception {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		byte ba[] = mob.toArray();

		BufferedImage bi = ImageIO.read(new ByteArrayInputStream(ba));
		return bi;
	}

	public static Octree testIntersection(Octree octree) {

		octree.getInernalNode().getChildren()[4].setColor(Color.GREEN);
		
//		Node root = octree.getRoot();
//		BoxParameters boxParameters = octree.getBoxParameters();
//		DeltaStruct currentDeltas = root.getDeltaStruct();
//		//List<Box> voxels = new ArrayList<Box>();
//		Group voxels = new Group();
//
//		if (root == null) {
//			return voxels;
//		}
//
//		if (root.isLeaf()) {
//			root.setBoxParameters(boxParameters);
//			root.setDeltaStruct(currentDeltas);
//			//Box box = generateVoxel(currentParameters, currentDeltas, currentNode.getColor());
//			//voxels.getChildren().addAll(box);
//			
//			Color boxColor = Color.GRAY;
//			IntersectionStatus status = testIntersection(boxParameters);
//			Box box = new Box();
//			if(status == IntersectionStatus.INSIDE) {
//				boxColor = getPaintColor(currentNode.getColor(), Color.BLACK);
//				box = generateVoxel(currentParameters, currentDeltas, boxColor);
//			} else if(status == IntersectionStatus.PARTIAL){
//				boxColor = getPaintColor(currentNode.getColor(), Color.GRAY);
//				box = generateVoxel(currentParameters, currentDeltas, boxColor);
//			} else {
//				boxColor = getPaintColor(currentNode.getColor(), Color.WHITE);
//				box = generateVoxel(currentParameters, currentDeltas, boxColor);
//			}
//			voxels.getChildren().addAll(box);
//			System.out.println("Root is leaf");
//		} else {
//			System.out.println("Root is Node");
//			Node[] children = currentNode.getChildren();
//			int newBoxSize = currentParameters.getBoxSize() / 2;
//			BoxParameters newParameters = new BoxParameters();
//			newParameters.setBoxSize(newBoxSize);
//			newParameters.setCenterX(currentParameters.getCenterX() + (currentDeltas.deltaX * newBoxSize));
//			newParameters.setCenterY(currentParameters.getCenterY() + (currentDeltas.deltaY * newBoxSize));
//			newParameters.setCenterZ(currentParameters.getCenterZ() + (currentDeltas.deltaZ * newBoxSize));
//
//			for (int i = 0; i < children.length; i++) {
//				// compute deltaX, deltaY, and deltaZ for new voxels
//				Node childNode = children[i];
//				if (childNode != null) {
//					childNode.setBoxParameters(newParameters);
//					DeltaStruct displacementDirections = octree.computeDeltaDirections(i);
//					childNode.setDeltaStruct(displacementDirections);
////					List<Box> innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
////					voxels.addAll(innerBoxes);
//					Color boxColor = Color.GRAY;
//					IntersectionStatus status = testIntersection(newParameters);
//					Box box = new Box();
//					if(status == IntersectionStatus.INSIDE) {
//						boxColor = getPaintColor(childNode.getColor(), Color.BLACK);
//						box = generateVoxel(newParameters, displacementDirections, boxColor);
//					} else if(status == IntersectionStatus.PARTIAL){
//						boxColor = getPaintColor(childNode.getColor(), Color.GRAY);
//						box = generateVoxel(newParameters, displacementDirections, boxColor);
//						Group innerBoxes = generateVolumeAux(childNode, newParameters, displacementDirections);
//						voxels.getChildren().addAll(innerBoxes);
//					} else {
//						boxColor = getPaintColor(childNode.getColor(), Color.WHITE);
//						box = generateVoxel(newParameters, displacementDirections, boxColor);
//					}
//					voxels.getChildren().addAll(box);
//					
////					
//					Group projections = getProjections(newParameters);
//					voxels.getChildren().addAll(projections);
//				}
//			}
//		}
		
		return octree;
	}
	
//	public IntersectionStatus testIntersection(BoxParameters boxParameters) {
//		ArrayList<Vector3D> projectedPoints = new ArrayList<>();
//		IntersectionStatus status = IntersectionStatus.INSIDE;
//		//TransformMatrices transformMatrices = new TransformMatrices(400, 290, 32.3);
//		VolumeModel volumeModel = new VolumeModel(boxParameters);
//		
//		double leftMostPos = transformMatrices.screenWidth;
//		double rightMostPos = 0;
//		double topMostPos = transformMatrices.screenHeight;
//		double bottomMostPos = 0;
//		
//		for (Vector3D vector: volumeModel.modelVertices) {
//			Vector3D viewVector = transformMatrices.toViewCoordinates(vector);			
//			Vector3D clipVector = transformMatrices.toClipCoordinates(viewVector);
//			if (Math.abs(clipVector.getX()) > Math.abs(clipVector.getW()) ||
//					Math.abs(clipVector.getY()) > Math.abs(clipVector.getW()) ||
//					Math.abs(clipVector.getZ()) > Math.abs(clipVector.getW())) {
//			}
//			
//			Vector3D ndcVector = transformMatrices.toNDCCoordinates(clipVector);			
//			Vector3D windowVector = transformMatrices.toWindowCoordinates(ndcVector);
//			projectedPoints.add(windowVector);
//			
//			if (windowVector.getX() > rightMostPos) {
//				rightMostPos = windowVector.getX();
//			} else if (windowVector.getX() < leftMostPos) {
//				leftMostPos = windowVector.getX();
//			}
//			
//			if (windowVector.getY() > bottomMostPos) {
//				bottomMostPos = windowVector.getY();
//			} else if (windowVector.getY() < topMostPos) {
//				topMostPos = windowVector.getY();
//			}
//		}
//		
//		Rectangle boundingBox = new Rectangle(leftMostPos, topMostPos, rightMostPos - leftMostPos, bottomMostPos - topMostPos);		
//		System.out.println("("+boundingBox.getX()+","+boundingBox.getY()+") - ("+boundingBox.getX()+","+(boundingBox.getY()+boundingBox.getHeight())+")");
//		
//		int[][] transformedArray = .get(0);
//		int xVal = (int) boundingBox.getX();
//		int yVal = (int) (boundingBox.getY()+boundingBox.getHeight());
//		if(xVal < 0) {
//			xVal = 0;
//		}
//		if(yVal < 0) {
//			yVal = 0;
//		}
//		System.out.println("xVal = " + xVal + ", yVal = " + yVal);
//		int transformedValue = transformedArray[xVal][yVal];
//		
//		System.out.println("transformedValue: " + transformedValue);
//		
//		int determiningValue = (int) boundingBox.getWidth();
//		if(boundingBox.getHeight()<boundingBox.getWidth()) {
//			determiningValue = (int) boundingBox.getHeight();
//		}
//		
//		if (transformedValue >= determiningValue) {
//			System.out.println("Projection is totally inside");
//			status = IntersectionStatus.INSIDE;
//		} else if((transformedValue < determiningValue) && (transformedValue > 0)) {
//			System.out.println("Projection is partially inside");
//			status = IntersectionStatus.PARTIAL;
//		}
//		else {
//			System.out.println("Projection is outside");
//			status = IntersectionStatus.OUTSIDE;
//		}
//		return status;
//	}

}
