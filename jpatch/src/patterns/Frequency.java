package patterns;

/**
* This implementation of the Slope interface creates an slope that alters the frequency
* of the pattern it is applied to
**/

public class Frequency implements Slope {
	private Slope slope;
	private float fFrequency;
	
	/**
	* Creates and frequency changing slope
	* @param frequency the frequency to use
	**/
	public Frequency(Slope slope, float frequency) {
		this.slope = slope;
		fFrequency = frequency;
	}
	
	public float valueAt(float pos) {
		return slope.valueAt((pos * fFrequency) % 1);
	}
}
