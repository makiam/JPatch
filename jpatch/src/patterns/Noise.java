package patterns;

/**
* This class implements the Pattern interface and creates a Perlin noise pattern.
**/
public class Noise extends AbstractPattern {
	private final Slope slope;
	
	/**
	* Creates a Noise pattern with a default RampUp slope
	**/
	public Noise() {
		slope = new RampUp();
	}
	
	/**
	* Creates a Noise pattern with the specified Slope.
	* @param slope the slope to use
	**/
	public Noise(Slope slope) {
		this.slope = slope;
	}
	
	/**
	* Returns the noise value at the specified point in space
	* @param x the x coordinate
	* @param y the y coordinate
	* @param z the z coordinate
	* @return the value of the noise function
	**/
	public float valueAt(float x, float y, float z) {
		return slope.valueAt(0.5f + 0.5f * Functions.noise3f(x, y, z));
	}
}

