package patterns;

public class Phase implements Slope {
	private Slope slope;
	private float fPhase;
	
	public Phase(Slope slope, float phase) {
		this.slope = slope;
		fPhase = phase;
	}
	
	public float valueAt(float pos) {
		return slope.valueAt((pos + fPhase) % 1);
	}
}
