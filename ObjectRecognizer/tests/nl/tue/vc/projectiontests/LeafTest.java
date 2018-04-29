package nl.tue.vc.projection;

import javafx.scene.paint.Color;

public class LeafTest extends NodeTest {
	
	public LeafTest(double boxSize, double centerX, double centerY, double centerZ) {
		this.boxSize = boxSize;
		this.positionCenterX = centerX;
		this.positionCenterY = centerY;
		this.positionCenterZ = centerZ;
		color = Color.BLACK;
	}
	
	@Override
	boolean isLeaf() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	NodeTest[] getChildren() {
		return null;
	}
	
	@Override
	public String toString() {
		return "Leaf -> " + super.toString();
	}
}
