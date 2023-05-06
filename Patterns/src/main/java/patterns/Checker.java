package patterns;

public class Checker extends AbstractPattern {	
	
	public float valueAt(float x, float y, float z) {
		float xx = x - (float) Math.floor(x);
		float yy = y - (float) Math.floor(y);
		float zz = z - (float) Math.floor(z);
		return ((xx < 0.5f) ^ (yy < 0.5f) ^ (zz < 0.5f)) ? 0f : 1f;
	}
}

