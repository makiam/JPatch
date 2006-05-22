package patterns;

/**
* This class implements the Pattern interface and creates a Fractional Brownian Motion Noise pattern
**/
public class FbmNoise extends AbstractPattern {
	private final Slope slope;
	private final int iOctaves;
	private final float fLacunarity;
	private final float fGain;
	private final float fAtt;
	
	/**
	* Creates a Fractional Brownian Motion Noise pattern with default values for lacunarity (2.0) and
	* gain (0.5) and a default RampUp slope.
	* @param octaves the octaves parameter for the fbm function
	**/
	public FbmNoise(int octaves) {
		this(new RampUp(), octaves, 2.0f, 0.5f);
	}
	
	/**
	* Creates a Fractional Brownian Motion Noise pattern with a default RampUp slope.
	* @param octaves the octaves parameter for the fbm function
	* @param lacunarity the lacunarity parameter for the fbm function
	* @param gain the gain parameter for the fbm function
	**/
	public FbmNoise(int octaves, float lacunarity, float gain) {
		this(new RampUp(), octaves, lacunarity, gain);
	}
	
	/**
	* Creates a Fractional Brownian Motion Noise pattern with default values for lacunarity (2.0) and
	* gain (0.5).
	* @param octaves the octaves parameter for the fbm function
	* @param slope the sope function to use
	**/
	public FbmNoise(Slope slope, int octaves) {
		this(slope, octaves, 2.0f, 0.5f);
	}
	
	/**
	* Creates a Fractional Brownian Motion Noise pattern.
	* @param slope the sope function to use
	* @param octaves the octaves parameter for the fbm function
	* @param lacunarity the lacunarity parameter for the fbm function
	* @param gain the gain parameter for the fbm function
	**/
	public FbmNoise(Slope slope, int octaves, float lacunarity, float gain) {
		this.slope = slope;
		iOctaves = octaves;
		fLacunarity = lacunarity;
		fGain = gain;
		fAtt = 0.5f / (2f - (float) Math.pow(gain, octaves - 1));
	}
	
	/**
	* Returns the fbm noise value at the specified point in space
	* @param x the x coordinate
	* @param y the y coordinate
	* @param z the z coordinate
	* @return the value of the noise function
	**/
	public float valueAt(float x, float y, float z) {
		return slope.valueAt(0.5f + fAtt * Functions.fBm3f(x, y, z, iOctaves, fLacunarity, fGain));
	}
}
