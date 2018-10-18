package nl.tue.vc.application.visual;

import nl.tue.vc.application.utils.Utils;

public class DistanceTransformGenerator {

	private int[][] distanceMatrix;
	private int[][] binaryMatrix;
	private int rows; 
	private int columns;
	
	public DistanceTransformGenerator(int[][] binaryMatrix){
		this.binaryMatrix = binaryMatrix;
		rows = binaryMatrix.length;
		columns = binaryMatrix[0].length;
		distanceMatrix = new int[rows][columns];
		
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < columns; j++){
				distanceMatrix[i][j] = -1;
			}
		}		
	}
	
	public int[][] getDistanceTransform(){
		computeDistanceTransformMatrix();
		return distanceMatrix;
	}
	
	
	private void computeDistanceTransformMatrix(){
		computeDistanceTransformValue(rows - 1, 0);
	}
	
	private int computeDistanceTransformValue(int row, int column){
		if (row < 0 || column >= columns){
			return -1;
		} else {
			if (distanceMatrix[row][column] > -1){
				// we already computed the square size for this entry
				return distanceMatrix[row][column];
			} else if (row == 0 || column == columns - 1){
				// we can only get a square size of value 0 or 1 because we are in a boundary
				distanceMatrix[row][column] = binaryMatrix[row][column];
				return distanceMatrix[row][column];
			} else {
				// We have an entry with value one. Therefore, we compute it's square size
				int squareNeighborA = computeDistanceTransformValue(row - 1, column);
				int squareNeighborB = computeDistanceTransformValue(row -1, column + 1);
				int squareNeighborC = computeDistanceTransformValue(row, column + 1);
				// we are at the top or right boundaries of the image
				if (squareNeighborA == -1 || squareNeighborB == -1 || squareNeighborC == -1){
					distanceMatrix[row][column] = binaryMatrix[row][column];
				} else if (binaryMatrix[row][column] == 0){
					// the square size is zero because the binary matrix value is 0
					distanceMatrix[row][column] = 0;					
				}else {
					// we have to update the square size of the pixel
					int minimumSquare = squareNeighborA;
					if (squareNeighborB < minimumSquare){
						minimumSquare = squareNeighborB;
					} 
					if (squareNeighborC < minimumSquare){
						minimumSquare = squareNeighborC;
					}
					distanceMatrix[row][column] = minimumSquare + 1;
				} 
				return distanceMatrix[row][column];
			} 
		}	
	}
}
