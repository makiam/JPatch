package patterns;

public class Step extends AbstractPattern {
	private Turbulence turbulence = new Turbulence(0, 0, 2.0f, 0.5f);
	private float fStep;
	
	public Step(float step) {
		fStep = step;
	}
	
	public Step(float step, Turbulence turbulence) {
		this(step);
		this.turbulence = turbulence;
	}
	
	public float valueAt(float x, float y, float z) {
		float xx = x + turbulence.valueAt(x, y, z);
		float s = xx - (float) Math.floor(xx);
		return (s < fStep) ? 0f : 1f;
	}
}

