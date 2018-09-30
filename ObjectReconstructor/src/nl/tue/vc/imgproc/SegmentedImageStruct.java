package nl.tue.vc.imgproc;

import org.opencv.core.Mat;

public class SegmentedImageStruct {
	
	private String imageName;
	
	private Mat image;
	
	public void setImageName(String imageName){
		this.imageName = imageName;
	}
	
	public void setImage(Mat image){
		this.image = image;
	}
	
	public String getImageName(){
		return imageName;
	}
	
	public Mat getImage(){
		return image;
	}
	
}
