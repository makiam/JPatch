package patterns;

public class Sine implements Slope {
	private float PI2 = (float) Math.PI * 2f;
	public float valueAt(float pos) {
		return 0.5f + 0.5f * (float) Math.sin(pos * PI2);
	}
}

