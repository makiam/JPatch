package jpatch.auxilary;

import javax.vecmath.*;

public class AliasWavefrontMat {
	public Color3f Ka = new Color3f(0.25f, 0.25f, 0.50f);
	public Color3f Kd = new Color3f(0.25f, 0.25f, 0.50f);
	public Color3f Ks = new Color3f(1.00f, 1.00f, 1.00f);
	public float Ns = 10f;
	
	public AliasWavefrontMat() { }
	
	public AliasWavefrontMat(Color3f Ka, Color3f Kd, Color3f Ks, float Ns) {
		this.Ka.set(Ka);
		this.Kd.set(Kd);
		this.Ks.set(Ks);
		this.Ns = Ns;
	}
	
	public String toString() {
		return (
			"Ka " + Ka.x + " " + Ka.y + " " + Ka.z + "\n"
			+ "Kd " + Kd.x + " " + Kd.y + " " + Kd.z + "\n"
			+ "Ks " + Ks.x + " " + Ks.y + " " + Ks.z + "\n"
			+ "Ns " + Ns
		);
	}
}
