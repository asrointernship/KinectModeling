package be.wide.dom;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class Point3 {
	
	public float x;
	public float y;
	public float z;
	
	/**
	 * Standard constructor.
	 * Creates an empty point.
	 */
	public Point3(){
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a point with the specified values.
	 * @param x x-value
	 * @param y y-value
	 * @param z z-value
	 */
	public Point3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a point from a existing point.
	 * @param p point
	 */
	public Point3(Point3 p){
		set(p);
	}
	
	/**
	 * Sets this point to the specified point.
	 * @param p point
	 */
	public void set(Point3 p){
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}
	
	/**
	 * Sets the values of this point.
	 * @param x x-value
	 * @param y y-value
	 * @param z z-value
	 */
	public void set(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Adds a vector to this point.
	 * @param v vector
	 */
	public void add(Vector3 v){
		x += v.x;
		y += v.y;
		z += v.z;
	}
	
	/**
	 * Gets the point as 4tuple.
	 * @return 4tuple of this point
	 */
	public float[] get4Tuple(){
		return new float[]{x,y,z,1f};
	}
	
	/**
	 * Gets this point as string.
	 * Format: (x, y, z)
	 */
	public String toString()
	{
		String temp = "(" + x + ", " + y + ", " + z + ")";
		return temp;
	}

}
