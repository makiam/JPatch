package jpatch.entity;

public abstract class MotionKey {
	float fPosition;
	
	private MotionKey(float position) {
		fPosition = position;
	}
	
	public float getPosition() {
		return fPosition;
	}
	
	public void setPosition(float position) {
		fPosition = position;
	}
	
	public abstract MotionKey copy();
//	public boolean equals(Object object) {
//		if (!(object instanceof MotionKey))
//			return false;
//		return fPosition == ((MotionKey) object).fPosition;
//	}
//	
//	public int compareTo(Object object) {
//		MotionKey other = (MotionKey) object;
//		return java.lang.Float.compare(fPosition, other.fPosition);
//	}
	
//	public static class Compound extends MotionKey {
//		private MotionKey[] keys;
//		
//		public Compound(MotionKey[] keys) {
//			super(keys[0].fPosition);
//			for (int i = 1; i < keys.length; i++)
//				if (keys[i].fPosition != fPosition)
//					throw new IllegalArgumentException("Compound Key can only hold keys at the same position!!!");
//		}
//		
//		@Override
//		public void setPosition(float position) {
//			for (MotionKey key : keys)
//				key.setPosition(position);
//		}
//		
//	}
	
	public static class Float extends MotionKey {
		private float f;
		
		public Float(float position, float f) {
			super(position);
			this.f = f;
		}
		
		public float getFloat() {
			return f;
		}
		
		public void setFloat(float f) {
			this.f = f;
		}
		
		public String toString() {
			return ("<key frame=\"" + fPosition + "\" value=\"" + f + "\"/>");
		}
		
		public MotionKey.Float copy() {
			return new MotionKey.Float(fPosition, f);
		}
	}
	
	public static class Point3d extends MotionKey {
		private javax.vecmath.Point3d p;
		
		public Point3d(float position, javax.vecmath.Point3d p) {
			super(position);
			this.p = new javax.vecmath.Point3d(p);
		}
		
		public javax.vecmath.Point3d getPoint3d() {
			return p;
		}
		
		public void setPoint3d(javax.vecmath.Point3d p) {
			this.p.set(p);
		}
		
		public void setPoint3d(float x, float y, float z) {
			this.p.set(x, y, z);
		}
		
		public String toString() {
			return ("<key frame=\"" + fPosition + "\" x=\"" + p.x + "\" y=\"" + p.y + "\" z=\"" + p.z + "\"/>");
		}
		
		public MotionKey.Point3d copy() {
			return new MotionKey.Point3d(fPosition, p);
		}
	}
	
	//public static class Rotation3f extends MotionKey2 {
	//	private jpatch.auxilary.Rotation3f r;
	//	
	//	public Rotation3f(float position, jpatch.auxilary.Rotation3f r) {
	//		super(position);
	//		this.r = new jpatch.auxilary.Rotation3f(r);
	//	}
	//	
	//	public jpatch.auxilary.Rotation3f getRotation3f() {
	//		return r;
	//	}
	//	
	//	public void setRotation3f(jpatch.auxilary.Rotation3f r) {
	//		this.r.set(r);
	//	}
	//	
	//	public void setRotation3f(float x, float y, float z) {
	//		this.r.set(x, y, z);
	//	}
	//	
	//	public String toString() {
	//		return ("<key frame=\"" + fPosition + "\" x=\"" + r.x + "\" y=\"" + r.y + "\" z=\"" + r.z + "\"/>");
	//	}
	//}
	
	public static class Color3f extends MotionKey {
		private javax.vecmath.Color3f c;
		
		public Color3f(float position, javax.vecmath.Color3f c) {
			super(position);
			this.c = new javax.vecmath.Color3f(c);
		}
		
		public javax.vecmath.Color3f getColor3f() {
			return c;
		}
		
		public void setColor3f(javax.vecmath.Color3f c) {
			this.c.set(c);
		}
		
		public void setColor3f(float r, float g, float b) {
			this.c.set(r, g, b);
		}
		
		public String toString() {
			return ("<key frame=\"" + fPosition + "\" r=\"" + c.x + "\" g=\"" + c.y + "\" b=\"" + c.z + "\"/>");
		}
		
		public MotionKey.Color3f copy() {
			return new MotionKey.Color3f(fPosition, c);
		}
	}
	
	public static class Quat4f extends MotionKey {
		private javax.vecmath.Quat4f q;
		
		public Quat4f(float position, javax.vecmath.Quat4f q) {
			super(position);
			this.q = new javax.vecmath.Quat4f(q);
		}
		
		public javax.vecmath.Quat4f getQuat4f() {
			return q;
		}
		
		public void setQuat4f(javax.vecmath.Quat4f q) {
			this.q.set(q);
		}
		
		public void setQuat4f(float x, float y, float z, float w) {
			this.q.set(x, y, z, w);
		}
		
		public String toString() {
			return ("<key frame=\"" + fPosition + "\" x=\"" + q.x + "\" y=\"" + q.y + "\" z=\"" + q.z + "\" w=\"" + q.w + "\"/>");
		}
		public MotionKey.Quat4f copy() {
			return new MotionKey.Quat4f(fPosition, q);
		}
	}
}
