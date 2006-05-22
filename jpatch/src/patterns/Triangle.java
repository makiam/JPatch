package patterns;

public class Triangle implements Slope {
	public float valueAt(float pos) {
		return (pos <= 0.5) ? 2f * pos : 2f - 2f * pos;
	}
}

