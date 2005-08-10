package patterns;

import javax.vecmath.*;

public class PatternNormal3D implements Vector3D {
	private Pattern pattern;
	private float epsilon = 0.0001f;
	//private ColorMap colorMap;
	//private PigmentMap pigmentMap;
	private Point3f point = new Point3f();
	private Matrix4f m4Transform = new Matrix4f();
	private float size;
	
	public PatternNormal3D(Matrix4f transform, Pattern pattern, float size) {
		if (pattern == null) throw new NullPointerException();
		this.pattern = pattern;
		this.size = size;
		//this.colorMap = colorMap;
		m4Transform.set(transform);
	}
	
	//public PatternPigment3D(Matrix4f transform, Pattern pattern, PigmentMap pigmentMap) {
	//	if (pattern == null || pigmentMap == null) throw new NullPointerException();
	//	this.pattern = pattern;
	//	this.pigmentMap = pigmentMap;
	//	m4Transform.set(transform);
	//}
	
	public void transform(Matrix4f matrix) {
		m4Transform.mul(matrix);
	}
	
	public Vector3f vectorAt(float x, float y, float z) {
		point.set(x, y, z);
		m4Transform.transform(point);
		//if (colorMap != null) return colorMap.colorAt(pattern.valueAt(point.x, point.y, point.z));
		//else return pigmentMap.colorAt(pattern.valueAt(point.x, point.y, point.z), x, y, z);
		float f = pattern.valueAt(point.x, point.y, point.z);
		float dx = (pattern.valueAt(point.x + epsilon, point.y, point.z) - f) / epsilon;
		float dy = (pattern.valueAt(point.x, point.y + epsilon, point.z) - f) / epsilon;
		float dz = (pattern.valueAt(point.x, point.y, point.z + epsilon) - f) / epsilon;
		Vector3f v = new Vector3f(dx, dy, dz);
		//v.normalize();
		v.scale(size);
		return v;
	}
}

