package nl.tue.vc.projection;

import java.text.DecimalFormat;
import java.text.NumberFormat;

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
	
	@Override
	public String toString(){
		double leftCoordinate = unScaledRectangle.getX();
		double topCoordinate = unScaledRectangle.getY();
		double height = unScaledRectangle.getHeight();
		double width = unScaledRectangle.getWidth();
		
		NumberFormat formatter = new DecimalFormat("#0.00"); 
		
		String message = "Original rectangle: [(" + formatter.format(leftCoordinate) + ", " + formatter.format(topCoordinate) + ")";
		message += ", (" + formatter.format((leftCoordinate + width)) + ", " + formatter.format(topCoordinate) + ")";
		message += ", (" + formatter.format(leftCoordinate) + ", " + formatter.format(topCoordinate + height) + ")";
		message += ", (" + formatter.format(leftCoordinate + width) + ", " + formatter.format(topCoordinate + height) + ")]";
		
		return message;
	}
}
