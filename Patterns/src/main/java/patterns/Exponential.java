package patterns;

/**
* This implementation of the Slope interface creates an exponential slope function
**/
public class Exponential implements Slope {
	private float exponent;
	
	/**
	* Creates and exponential slope
	* @param exponent the exponent to use
	**/
	public Exponential(float exponent) {
		this.exponent = exponent;
	}
	
	public float valueAt(float pos) {
		return (float) Math.pow(pos,exponent);
	}
}

