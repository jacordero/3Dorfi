package nl.tue.vc.projection;

import javafx.scene.paint.Color;

public class OctreeTest {

	private NodeTest root;
	
	private double boxSize;
	
	private double centerX;
	
	private double centerY;
	
	private double centerZ;

	private int levels;
	
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

	public OctreeTest(int boxSize, double centerX, double centerY, double centerZ, int levels) {
		this.boxSize = boxSize;
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.levels = levels;
		if (levels > 0){
			root = new InternalNodeTest(boxSize, centerX, centerY, centerZ, levels - 1);			
		} else {
			root = new LeafTest(boxSize, centerX, centerY, centerZ);
		}
	}

	public NodeTest getRoot() {
		return root;
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
	
	@Override
	public String toString(){
		return root.toString();
	}

}
