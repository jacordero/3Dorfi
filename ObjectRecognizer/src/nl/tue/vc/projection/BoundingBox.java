package nl.tue.vc.projection;

import javafx.scene.shape.Rectangle;

public class BoundingBox {

	private Rectangle scaledRectangle;
	private Rectangle unScaledRectangle;
	
	public BoundingBox() {
		
	}

	public Rectangle getScaledRectangle() {
		return scaledRectangle;
	}

	public void setScaledRectangle(Rectangle scaledRectangle) {
		this.scaledRectangle = scaledRectangle;
	}

	public Rectangle getUnScaledRectangle() {
		return unScaledRectangle;
	}

	public void setUnScaledRectangle(Rectangle unScaledRectangle) {
		this.unScaledRectangle = unScaledRectangle;
	}
}
