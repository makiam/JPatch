package patterns;

import javax.vecmath.*;

public class PatternPigment3D implements Pigment3D {
	private Pattern pattern;
	private ColorMap colorMap;
	private PigmentMap pigmentMap;
	private Point3f point = new Point3f();
	private Matrix4f m4Transform = new Matrix4f();
	
	public PatternPigment3D(Matrix4f transform, Pattern pattern, ColorMap colorMap) {
		if (pattern == null || colorMap == null) throw new NullPointerException();
		this.pattern = pattern;
		this.colorMap = colorMap;
		m4Transform.set(transform);
	}
	
	public PatternPigment3D(Matrix4f transform, Pattern pattern, PigmentMap pigmentMap) {
		if (pattern == null || pigmentMap == null) throw new NullPointerException();
		this.pattern = pattern;
		this.pigmentMap = pigmentMap;
		m4Transform.set(transform);
	}
	
	public void transform(Matrix4f matrix) {
		m4Transform.mul(matrix);
	}
	
	public Color3f colorAt(float x, float y, float z) {
		point.set(x, y, z);
		m4Transform.transform(point);
		if (colorMap != null) return colorMap.colorAt(pattern.valueAt(point.x, point.y, point.z));
		else return pigmentMap.colorAt(pattern.valueAt(point.x, point.y, point.z), x, y, z);
	}
}

