package patterns;

public class Turbulence implements Perturberation{
	private float fAmount;
	private int iOctaves;
	private float fLacunarity;
	private float fGain;
	
	public Turbulence(float amount, int octaves) {
		fAmount = amount;
		iOctaves = octaves;
		fLacunarity = 2.0f;
		fGain = 0.5f;
	}
	
	public Turbulence(float amount, int octaves, float lacunarity, float gain) {
		fAmount = amount;
		iOctaves = octaves;
		fLacunarity = lacunarity;
		fGain = gain;
	}
	
	public float valueAt(float x, float y, float z) {
		return fAmount * Functions.turbulence3f(x, y, z, iOctaves, fLacunarity, fGain);
	}
}
