package patterns;

public class RampDown implements Slope {
	public float valueAt(float pos) {
		return 1f - pos;
	}
}

