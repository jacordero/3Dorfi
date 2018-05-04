package nl.tue.vc.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ProjectedPoint{

	private double x;
	
	private double y;
	
	private double scaleFactor;
	
	public ProjectedPoint(double x, double y, double scaleFactor){
		this.x = x;
		this.y = y;
		this.scaleFactor = scaleFactor;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double getScaledX(){
		return x / scaleFactor;
	}
	
	public double getScaledY(){
		return y / scaleFactor;
	}
	
	@Override
	public String toString(){
		NumberFormat formatter = new DecimalFormat("#0.00"); 
		String message = "x: " + formatter.format(x) + ", y: " + formatter.format(y) + " scaled x: " + formatter.format(x / scaleFactor) + ", scaled y: " + formatter.format(y / scaleFactor);
		return message;
	}
}
