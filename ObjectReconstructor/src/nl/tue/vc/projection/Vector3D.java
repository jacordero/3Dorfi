package nl.tue.vc.projection;

public class Vector3D {

	private double[] values;
	
	public Vector3D(double x, double y, double z, double w) {
		values = new double[] {x, y, z, w};
	}
	
	public Vector3D substract(Vector3D element) {
		return new Vector3D(
				values[0] - element.getX(),
				values[1] - element.getY(),
				values[2] - element.getZ(),
				values[3] - element.getW()
				);
	}
	
	public Vector3D add(Vector3D element) {
		return new Vector3D(
				values[0] - element.getX(),
				values[1] - element.getY(),
				values[2] - element.getZ(),
				values[3] - element.getW()
				);
	}
	
	
	public void normalizeXYZ() {
		double length = Math.sqrt((values[0]* values[0]) + (values[1]*values[1]) + (values[2]*values[2]));
		values[0] = values[0] / length;
		values[1] =	values[1] / length;
		values[2] =	values[2] / length;
	}
	
	public void normalizeXYZW() {
		double length = Math.sqrt((values[0]* values[0]) + (values[1]*values[1]) + (values[2]*values[2]) + (values[3]*values[3]));
		values[0] = values[0] / length;
		values[1] =	values[1] / length;
		values[2] =	values[2] / length;
		values[3] = values[3] / length;
	}
	
	public static Vector3D crossProduct(Vector3D a, Vector3D b) {
		double icomponent = a.getY()*b.getZ() - a.getZ()*b.getY();
		double jcomponent = a.getZ()*b.getX() - a.getX()*b.getZ();
		double kcomponent = a.getX()*b.getY() - a.getY()*b.getX();
		return new Vector3D(icomponent, jcomponent, kcomponent, 1.0);
	}
	
	public Vector3D(double[] values) {
		this.values = values;
	}
	
	public double[] getArray() {
		return values;
	}
	
	public double getX() {
		return values[0];
	}
	
	public void setX(double val) {
		values[0] = val;
	}
	
	public double getY() {
		return values[1];
	}

	public void setY(double val) {
		values[1] = val;
	}
	
	public double getZ() {
		return values[2];
	}
	
	public void setZ(double val) {
		values[2] = val;
	}
	
	public double getW() {
		return values[3];
	}
	
	public void setW(double val) {
		values[3] = val;
	}
	
	@Override
	public String toString() {
		return "[" + String.format( "%.2f", values[0]) + ", " + String.format( "%.2f", values[1]) +
				", " + String.format( "%.2f", values[2]) + ", " + String.format( "%.2f", values[3]) + "]";
	}
	
}
