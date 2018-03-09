package nl.tue.vc.voxelengine;

public enum NodeColor {

	WHITE("White"), BLACK("Black"), GRAY("Gray"), ;

	
	
	private String colorName;
	
	NodeColor(String colorName){
		this.colorName = colorName;
	}
		
	public String getColorName() {
		return colorName;
	}
	
	
}


