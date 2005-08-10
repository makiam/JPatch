package patterns;

import javax.vecmath.*;

public class AveragePigment3D implements Pigment3D {
	private float[] aFactor = new float[0];
	private Pigment3D[] aPigment = new Pigment3D[0];
	
	public void addPigment(float factor, Pigment3D pigment) {
		int l = aFactor.length;
		float[] newFactor = new float[l + 1];
		Pigment3D[] newPigment = new Pigment3D[l + 1];
		System.arraycopy(aFactor, 0, newFactor, 0, l);
		System.arraycopy(aPigment, 0, newPigment, 0, l);
		newFactor[l] = factor;
		newPigment[l] = pigment;
		aFactor = newFactor;
		aPigment = newPigment;
	}
	
	public void normalize() {
		float sum = 0;
		for (int i = 0; i < aFactor.length; sum += aFactor[i++]);
		for (int i = 0; i < aFactor.length; aFactor[i++] /= sum);
	}
	
	public Color3f colorAt(float x, float y, float z) {
		Color3f color = new Color3f();
		for (int i = 0; i < aFactor.length; i++) {
			Color3f c = aPigment[i].colorAt(x, y, z);
			color.x += c.x * aFactor[i];
			color.y += c.y * aFactor[i];
			color.z += c.z * aFactor[i];
		}
		return color;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < aFactor.length; i++) {
			sb.append(aFactor[i] + "\t" + aPigment[i] + "\n");
		}
		return sb.toString();
	}
}
