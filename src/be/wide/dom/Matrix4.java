package be.wide.dom;

/**
 * 
 * @author Maarten Taeymans
 *
 */
public class Matrix4 {
	
	public float[] m = new float[16];
	
	/**
	 * Standard constructor.
	 * Creates a 4 dim matrix as identity matrix.
	 */
	public Matrix4(){
		setIdentityMatrix();
	}
	
	/**
	 * Overloaded constructor.
	 * Creates a 4 dim matrix from a given matrix.
	 * @param a 4 dim matrix
	 */
	public Matrix4(Matrix4 a){
		for(int i=0; i<16; i++){
			m[i] = a.m[i];
		}
	}
	
	/**
	 * Sets this matrix to the identity matrix.
	 */
	public void setIdentityMatrix(){
		m[0] = m[5] = m[10] = m[15] = 1f;
		m[1] = m[2] = m[3] = m[4] = 0f;
		m[6] = m[7] = m[8] = m[9] = 0f;
		m[11] = m[12] = m[13] = m[14] = 0f;		
	}
	
	/**
	 * Sets this matrix to the product of the given matrix with this matrix.
	 */
	public void preMult(Matrix4 a){
		float sum = 0;
		Matrix4 tmp = new Matrix4(this);
		for(int c=0; c<4; c++){
			for(int r=0; r<4; r++){
				sum = 0;
				for(int k=0; k<4; k++){
					sum +=a.m[4*k+r]*tmp.m[4*c+k];
				}
				m[4*c+r] = sum;
			}
		}
	}
	
	/**
	 * Sets this matrix to the product of this matrix with the given matrix a.
	 */
	public void postMult(Matrix4 a){
		float sum = 0;
		Matrix4 tmp = new Matrix4(this);
		for(int c=0; c<4; c++){
			for(int r=0; r<4; r++){
				sum = 0;
				for(int k=0; k<4; k++){
					sum +=tmp.m[4*k+r]*a.m[4*c+k];
				}
				m[4*c+r] = sum;
			}
		}
	}
	
	/**
	 * Returns the point obtained by multiplying this matrix with the given point.
	 * x, y, z, 1
	 */
	public Point3 mult(Point3 p){
		Point3 retPoint;
		float x = p.x * m[0] + p.y * m[4] + p.z * m[8] + m[12] * 1;
		float y = p.x * m[1] + p.y * m[5] + p.z * m[9] + m[13] * 1;
		float z = p.x * m[2] + p.y * m[6] + p.z * m[10] + m[14] * 1;
		retPoint = new Point3(x, y, z);
		return retPoint;
	}
	
	/**
	 * Returns the vector obtained by multiplying this matrix with the given vector.
	 * x, y, z, 0
	 */
	public Vector3 mult(Vector3 v){
		Vector3 retVector;
		float x = v.x * m[0] + v.y * m[4] + v.z * m[8] + m[12] * 0;
		float y = v.x * m[1] + v.y * m[5] + v.z * m[9] + m[13] * 0;
		float z = v.x * m[2] + v.y * m[6] + v.z * m[10] + m[14] * 0;
		retVector = new Vector3(x, y, z);
		return retVector;
	}
	
	/**
	 * Returns the transposed matrix of this matrix.
	 * @return transposed matrix
	 */
	public Matrix4 transpose()
	{
		float[] newM = new float[16];
		newM[0] = m[0];
		newM[1] = m[4];
		newM[2] = m[8];
		newM[3] = m[12];
		newM[4] = m[1];
		newM[5] = m[5];
		newM[6] = m[9];
		newM[7] = m[13];
		newM[8] = m[2];
		newM[9] = m[6];
		newM[10] = m[10];
		newM[11] = m[14];
		newM[12] = m[3];
		newM[13] = m[7];
		newM[14] = m[11];
		newM[15] = m[15];
		Matrix4 mat = new Matrix4();
		mat.m = newM;
		return mat;
	}

}
