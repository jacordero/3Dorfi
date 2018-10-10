package nl.tue.vc.model;

public enum NodeColor {

	WHITE("white"), GRAY("gray"), BLACK("black");
	
	private String name;
	
	NodeColor(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
