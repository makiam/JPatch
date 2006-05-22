package patterns;

public class Wood extends AbstractPattern {
	private Turbulence turbulence = new Turbulence(0.5f, 3, 2.0f, 0.5f);
	
	public Wood() { }
	
	public Wood(Turbulence turbulence) {
		this.turbulence = turbulence;
	}
	
	public float valueAt(float x, float y, float z) {
		float r = (float) Math.sqrt(x * x + y * y);
		float rr = r + turbulence.valueAt(x, y, z);
		return rr - (float) Math.floor(rr);
	}
}

