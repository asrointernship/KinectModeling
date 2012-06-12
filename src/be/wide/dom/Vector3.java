package be.wide.dom;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class Vector3 {
	
	public float x;
	public float y;
	public float z;
	
	/**
	 * Standard constructor.
	 * Creates an empty vector.
	 */
	public Vector3(){
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a vector with specified values.
	 * @param x x-value
	 * @param y y-value
	 * @param z z-value
	 */
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a vector from a specified vector.
	 * @param v vector
	 */
	public Vector3(Vector3 v){
		set(v);
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a vector from a specified point.
	 * @param p point
	 */
	public Vector3(Point3 p){
		x = p.x;
		y = p.y;
		z = p.z;
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a vector from 2 specified points.
	 * @param p1 point
	 * @param p2 point
	 */
	public Vector3(Point3 p1, Point3 p2){
		this.x = p2.x - p1.x;
		this.y = p2.y - p1.y;
		this.z = p2.z - p1.z;
	}
	
	/**
	 * Sets the vector to a specified vector.
	 * @param v vector
	 */
	public void set(Vector3 v){
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	/**
	 * Multiplies this vector with a float.
	 * @param a float
	 */
	public void mult(float a){
		x *= a;
		y *= a;
		z *= a;
	}
	
	/**
	 * Adds a vector to this vector
	 * @param v vector
	 */
	public void add(Vector3 v){
		x += v.x;
		y += v.y;
		z += v.z;
	}
	
	/**
	 * Calculates the dot product of this vector with a specified vector.
	 * @param v vector
	 * @return dot product of vectors
	 */
	public float dot(Vector3 v){
		float temp = this.x * v.x  + this.y * v.y + this.z * v.z;
		return temp;
	}

	/**
	 * Calculates the cross product of this vector with a specified vector.
	 * @param v vector
	 * @return cross product vector
	 */
	public Vector3 cross(Vector3 v){
		Vector3 temp = new Vector3((this.y*v.z-this.z*v.y), (this.z*v.x-this.x*v.z), (this.x*v.y-this.y*v.x));
		return temp;
	}
	
	/**
	 * Normalizes this vector.
	 */
	public void normalize(){
		float length = getLength();
		
		if (length != 0)
		{
			this.x = this.x / length;
			this.y = this.y / length;
			this.z = this.z / length;
		}
	}
	
	/**
	 * Gets the length of this vector.
	 * @return length
	 */
	public float getLength()
	{
		float temp = (float) Math.sqrt((Math.pow(x, 2)+Math.pow(y, 2)+Math.pow(z, 2)));
		return temp;
	}
	
	/**
	 * Reverses this vector.
	 * @return reversed vector
	 */
	public Vector3 reverse(){
		return new Vector3(-x, -y, -z);
	}
	
	/**
	 * Gets this vector as string.
	 * Format: (x, y, z)
	 */
	public String toString()
	{
		String temp = "(" + x + ", " + y + ", " + z + ")";
		return temp;
	}

}
