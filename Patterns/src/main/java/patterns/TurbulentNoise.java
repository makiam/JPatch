package patterns;

public class TurbulentNoise extends AbstractPattern {
	private final Slope slope;
	private final int iOctaves;
	private final float fLacunarity;
	private final float fGain;
	private final float fAtt;
	
	public TurbulentNoise(int octaves) {
		this(new RampUp(), octaves, 2.0f, 0.5f);
	}
	
	public TurbulentNoise(int octaves, float lacunarity, float gain) {
		this(new RampUp(), octaves, lacunarity, gain);
	}
	
	public TurbulentNoise(Slope slope, int octaves) {
		this(slope, octaves, 2.0f, 0.5f);
	}
	
	public TurbulentNoise(Slope slope, int octaves, float lacunarity, float gain) {
		this.slope = slope;
		iOctaves = octaves;
		fLacunarity = lacunarity;
		fGain = gain;
		fAtt = 1f / (2f - (float) Math.pow(gain, octaves - 1));
	}
	
	public float valueAt(float x, float y, float z) {
		return slope.valueAt(fAtt * Functions.turbulence3f(x, y, z, iOctaves, fLacunarity, fGain));
	}
}
