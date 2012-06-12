package be.wide.dom;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class Quaternion {

	public float a;
	public float b;
	public float c;
	public float d;
	
	/**
	 * Standard constructor.
	 * Creates a quaternion from specified values.
	 * @param a a-value
	 * @param b b-value
	 * @param c c-value
	 * @param d d-value
	 */
	public Quaternion(float a, float b, float c, float d)
	{
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a quaternion from a point.
	 * @param point point
	 */
	public Quaternion(Point3 point)
	{
		this(0f, point.x, point.y, point.z);
	}
	
	/**
	 * Overload constructor.
	 * Creates a quaternion from a vector and rotation.
	 * @param rotation rotation
	 * @param u vector
	 */
	public Quaternion(float rotation, Vector3 u)
	{
		this((float) Math.cos(Math.toRadians(rotation)/2f),
				(float) (Math.sin(Math.toRadians(rotation)/2f) * u.x),
				(float) (Math.sin(Math.toRadians(rotation)/2f) * u.y),
				(float) (Math.sin(Math.toRadians(rotation)/2f) * u.z));
	}
	
	/**
	 * Multiply a quaternion with this quaternion.
	 * @param q quaternion
	 * @return product of multiplication
	 */
	public Quaternion product(Quaternion q)
	{
		float newA = (this.a * q.a) - (this.b * q.b) - (this.c * q.c) - (this.d * q.d);
		float newB = (this.a * q.b) + (this.b * q.a) + (this.c * q.d) - (this.d * q.c);
		float newC = (this.a * q.c) - (this.b * q.d) + (this.c * q.a) + (this.d * q.b);
		float newD = (this.a * q.d) + (this.b * q.c) - (this.c * q.b) + (this.d * q.a);
		return new Quaternion(newA, newB, newC, newD);
	}
	
	/**
	 * Conjugates this quaternion.
	 * @return conjugated quaternion
	 */
	public Quaternion conjugate()
	{
		return new Quaternion(a, -b, -c, -d);
	}
}
