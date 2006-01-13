package jpatch.entity;

import java.util.*;
import jpatch.auxilary.*;

public abstract class MotionCurve2 {
	
	public final static InterpolationMethod LINEAR = new InterpolationMethod("linear");
	public final static InterpolationMethod CUBIC = new InterpolationMethod("cubic");
	
	InterpolationMethod interpolationMethod = CUBIC;
	String name = "*";
	ArrayList list = new ArrayList();

	public static MotionCurve2.Float createMorphCurve(Morph morph) {
		MotionCurve2.Float mc = new MotionCurve2.Float();
		mc.name = morph.getId();
		mc.fMin = morph.getMin();
		mc.fMax = morph.getMax();
		return mc;
	}
	
	public static MotionCurve2.Float createMorphCurve(Morph morph, MotionKey2.Float key) {
		MotionCurve2.Float mc = createMorphCurve(morph);
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve2.Float createScaleCurve() {
		MotionCurve2.Float mc = new MotionCurve2.Float();
		mc.name = "Scale";
		mc.fMin = -10;
		mc.fMax = 10;
		return mc;
	}
	
	public static MotionCurve2.Float createScaleCurve(MotionKey2.Float key) {
		MotionCurve2.Float mc = createScaleCurve();
		mc.addKey(key);
		return mc;
	}
		
	public static MotionCurve2.Float createSizeCurve() {
		MotionCurve2.Float mc = new MotionCurve2.Float();
		mc.name = "Size";
		mc.fMin = 0;
		mc.fMax = 100;
		return mc;
	}
	
	public static MotionCurve2.Float createSizeCurve(MotionKey2.Float key) {
		MotionCurve2.Float mc = createSizeCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve2.Float createIntensityCurve() {
		MotionCurve2.Float mc = new MotionCurve2.Float();
		mc.name = "Intensity";
		mc.fMin = 0;
		mc.fMax = 100;
		return mc;
	}
	
	public static MotionCurve2.Float createIntensityCurve(MotionKey2.Float key) {
		MotionCurve2.Float mc = createIntensityCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve2.Float createFocalLengthCurve() {
		MotionCurve2.Float mc = new MotionCurve2.Float();
		mc.name = "Focal length";
		mc.fMin = 20;
		mc.fMax = 500;
		return mc;
	}
	
	public static MotionCurve2.Float createFocalLengthCurve(MotionKey2.Float key) {
		MotionCurve2.Float mc = createFocalLengthCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve2.Point3d createPositionCurve() {
		MotionCurve2.Point3d mc = new MotionCurve2.Point3d();
		mc.name = "Position";
		return mc;
	}
	
	public static MotionCurve2.Point3d createPositionCurve(MotionKey2.Point3d key) {
		MotionCurve2.Point3d mc = createPositionCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve2.Color3f createColorCurve() {
		MotionCurve2.Color3f mc = new MotionCurve2.Color3f();
		mc.name = "Color";
		return mc;
	}
	
	public static MotionCurve2.Color3f createColorCurve(MotionKey2.Color3f key) {
		MotionCurve2.Color3f mc = createColorCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve2.Quat4f createOrientationCurve() {
		MotionCurve2.Quat4f mc = new MotionCurve2.Quat4f();
		mc.name = "Orientation";
		return mc;
	}
	
	public static MotionCurve2.Quat4f createOrientationCurve(MotionKey2.Quat4f key) {
		MotionCurve2.Quat4f mc = createOrientationCurve();
		mc.addKey(key);
		return mc;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addKey(MotionKey2 key) {
		list.add(binarySearch(key.getPosition()), key);
	}
	
	public abstract MotionKey2 insertKeyAt(float position);
	
	public void removeKey(MotionKey2 key) {
		if (list.size() > 1) list.remove(key);
	}
	
	public void moveKey(MotionKey2 key, float position) {
		list.remove(key);
		list.add(binarySearch(key.getPosition()), key);
	}
	
	public int getKeyCount() {
		return list.size();
	}
	
	public MotionKey2 getKey(int number) {
		return (MotionKey2) list.get(number);
	}
	
	public float getStart() {
		return ((MotionKey2) list.get(0)).getPosition();
	}
	
	public float getEnd() {
		return ((MotionKey2) list.get(list.size() - 1)).getPosition();
	}
	
	public MotionKey2 getKeyAt(float position) {
		if (position == ((MotionKey2) list.get(list.size() - 1)).getPosition()) return (MotionKey2) list.get(list.size() - 1);
		int index = binarySearch(position) - 1;
		if (index >= 0) {
			MotionKey2 mk = (MotionKey2) list.get(index);
			if (mk != null && mk.getPosition() == position) return mk;
			else return null;
		} else return null;
	}
	
	public void xml(StringBuffer sb, String prefix, String type) {
//		StringBuffer indent = XMLutils.indent(tab);
//		StringBuffer indent2 = XMLutils.indent(tab + 1);
		sb.append(prefix).append("<motioncurve " + type + " interpolation=\"" + interpolationMethod.toString() + "\">\n");
		for (Iterator it = list.iterator(); it.hasNext(); sb.append(prefix + "\t").append(it.next().toString()).append("\n"));
		sb.append(prefix).append("</motioncurve>").append("\n");
	}
	
	public MotionKey2 getPrevKey(float position) {
		int index = binarySearch(position) - 1;
		if (index >= 0) {
			MotionKey2 key = (MotionKey2) list.get(index);
			if (key.getPosition() < position) return key;
			else if (index == 0) return key;
			else return (MotionKey2) list.get(index - 1);
		}
		return (MotionKey2) list.get(0);
	}
	

	public MotionKey2 getNextKey(float position) {
		int index = binarySearch(position) - 1;
		if (index < list.size() && index >= 0) {
			MotionKey2 key = (MotionKey2) list.get(index);
			if (key.getPosition() > position) return key;
			else if (index == list.size() - 1) return key;
			else return (MotionKey2) list.get(index + 1);
		}
		return (MotionKey2) list.get(list.size() - 1);
	}
	
	public InterpolationMethod getInterpolationMethod() {
		return interpolationMethod;
	}
	
	public void setInterpolationMethod(InterpolationMethod interpolationMethod) {
		this.interpolationMethod = interpolationMethod;
	}
	
	int binarySearch(float position) {
		if (list.size() == 0) return 0;
		if (position < ((MotionKey2) list.get(0)).getPosition()) return 0;
		if (position > ((MotionKey2) list.get(list.size() - 1)).getPosition()) return list.size();
		int min = 0;
		int max = list.size() - 1;
		int i = max >> 1;
		while (max > min + 1) {
			if (((MotionKey2) list.get(i)).getPosition() > position) {
				max = i;
				i -= ((i - min) >> 1);
			} else {
				min = i;
				i += ((max - i) >> 1);
			}
		}
		return max;
	}
	
	Object[] getInterpolationKeysFor(float position) {
		int index = binarySearch(position) - 1;
		return new Object[] {
			(index > 0) ? list.get(index - 1) : null,
			list.get(index),
			list.get(index + 1),
			(index < list.size() - 2) ? list.get(index + 2) : null
		};
	}
	
	float cubicInterpolate(float p0, float m0, float p1, float m1, float t) {
		float t2 = t * t;
		float t3 = t * t2;
		float H0 = 2 * t3 - 3 * t2 + 1;
		float H1 = t3 - 2 * t2 + t;
		float H2 = -2 * t3 + 3 * t2;
		float H3 = t3 - t2;
		return H0 * p0 + H1 * m0 + H2 * p1 + H3 * m1;
	}
	
	double cubicInterpolate(double p0, double m0, double p1, double m1, float t) {
		float t2 = t * t;
		float t3 = t * t2;
		float H0 = 2 * t3 - 3 * t2 + 1;
		float H1 = t3 - 2 * t2 + t;
		float H2 = -2 * t3 + 3 * t2;
		float H3 = t3 - t2;
		return H0 * p0 + H1 * m0 + H2 * p1 + H3 * m1;
	}
	
	public static class Float extends MotionCurve2 {
		float fMin, fMax;
		
		private Float() { }
		
		public float getFloatAt(float position) {
			boolean limitOvershoot = true;
			if (position <= getStart()) return ((MotionKey2.Float) list.get(0)).getFloat();
			if (position >= getEnd()) return ((MotionKey2.Float) list.get(list.size() - 1)).getFloat();
			Object[] key = getInterpolationKeysFor(position);
			MotionKey2.Float key0 = (MotionKey2.Float) key[0];
			MotionKey2.Float key1 = (MotionKey2.Float) key[1];
			MotionKey2.Float key2 = (MotionKey2.Float) key[2];
			MotionKey2.Float key3 = (MotionKey2.Float) key[3];
			float l = key2.getPosition() - key1.getPosition();
			float t = (position - key1.getPosition()) / l;
			float p0 = key1.getFloat();
			float p1 = key2.getFloat();
			
			if (interpolationMethod == LINEAR) {
				return (p0 * (1 - t) + p1 * t);
			}
			else {
				float m0 = 0;
				float m1 = 0;
				if (key0 != null) {
					float v0 = key0.getFloat();
					if (limitOvershoot && (p0 == v0 || p0 == p1 || (p0 > v0 && p0 > p1) || (p0 < v0 && p0 < p1))) m0 = 0;
					else m0 = (p1 - v0) / (key2.getPosition() - key0.getPosition()) * l;
				}
				if (key3 != null) {
					float v1 = key3.getFloat();
					if (limitOvershoot && (p1 == p0 || p1 == v1 || (p1 > v1 && p1 > p0) || (p1 < v1 && p1 < p0))) m1 = 0;
					else m1 = (v1 - p0) / (key3.getPosition() - key1.getPosition()) * l;
				}
				return cubicInterpolate(p0, m0, p1, m1, t);
			}
		}
		
		public MotionKey2.Float setFloatAt(float position, float f) {
			MotionKey2.Float mk = (MotionKey2.Float) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey2.Float(position, f);
				addKey(mk);
			}
			else mk.setFloat(f);
			return mk;
		}
		
		public MotionKey2 insertKeyAt(float position) {
			if (!(getKeyAt(position) != null)) setFloatAt(position, getFloatAt(position));
			return getKeyAt(position);
		}
		
		public float getMin() {
			return fMin;
		}
		
		public float getMax() {
			return fMax;
		}
		
		public void setRange(float min, float max) {
			fMin = min;
			fMax = max;
		}
	}
	
	public static class Point3d extends MotionCurve2 {
		
		private Point3d() { }
		
		public javax.vecmath.Point3d getPoint3dAt(float position) {
			if (position <= getStart()) return ((MotionKey2.Point3d) list.get(0)).getPoint3d();
			if (position >= getEnd()) return ((MotionKey2.Point3d) list.get(list.size() - 1)).getPoint3d();
			Object[] key = getInterpolationKeysFor(position);
			MotionKey2.Point3d key0 = (MotionKey2.Point3d) key[0];
			MotionKey2.Point3d key1 = (MotionKey2.Point3d) key[1];
			MotionKey2.Point3d key2 = (MotionKey2.Point3d) key[2];
			MotionKey2.Point3d key3 = (MotionKey2.Point3d) key[3];
			float l = key2.getPosition() - key1.getPosition();
			float t = (position - key1.getPosition()) / l;
			javax.vecmath.Point3d p0 = key1.getPoint3d();
			javax.vecmath.Point3d p1 = key2.getPoint3d();
			
			if (interpolationMethod == LINEAR) {
				javax.vecmath.Point3d p = new javax.vecmath.Point3d();
				p.interpolate(p0, p1, t);
				return p;
			}
			else {
				javax.vecmath.Vector3d m0 = new javax.vecmath.Vector3d();
				if (key0 != null && !key0.getPoint3d().equals(key1.getPoint3d()) && !key1.getPoint3d().equals(key2.getPoint3d())) {
					m0.sub(p1, key0.getPoint3d());
					m0.scale(l / (key2.getPosition() - key0.getPosition()));
				}
				javax.vecmath.Vector3d m1 = new javax.vecmath.Vector3d();
				if (key3 != null && !key3.getPoint3d().equals(key2.getPoint3d()) && !key2.getPoint3d().equals(key1.getPoint3d())) {
					m1.sub(key3.getPoint3d(), p0);
					m1.scale(l / (key3.getPosition() - key1.getPosition()));
				}
				//System.out.println(key0.getPoint3d());
				//System.out.println(key1.getPoint3d());
				//System.out.println(key2.getPoint3d());
				//System.out.println(key3.getPoint3d());
				//System.out.println(key0.getPoint3d().equals(key1.getPoint3d()));
				//System.out.println(key0.getPoint3d().equals(key1.getPoint3d()));
				//System.out.println(m0);
				//System.out.println(m1);
				//System.out.println();
				return new javax.vecmath.Point3d(
					cubicInterpolate(p0.x, m0.x, p1.x, m1.x, t),
					cubicInterpolate(p0.y, m0.y, p1.y, m1.y, t),
					cubicInterpolate(p0.z, m0.z, p1.z, m1.z, t)
				);
			}
		}
		
		public MotionKey2.Point3d setPoint3dAt(float position, javax.vecmath.Point3d p) {
			MotionKey2.Point3d mk = (MotionKey2.Point3d) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey2.Point3d(position, p);
				addKey(mk);
			}
			else mk.setPoint3d(p);
			return mk;
		}
		
		public MotionKey2 insertKeyAt(float position) {
			if (!(getKeyAt(position) != null)) setPoint3dAt(position, getPoint3dAt(position));
			return getKeyAt(position);
		}
	}
	
	public static class Color3f extends MotionCurve2 {
		
		private Color3f() { }
		
		public javax.vecmath.Color3f getColor3fAt(float position) {
			if (position <= getStart()) return ((MotionKey2.Color3f) list.get(0)).getColor3f();
			if (position >= getEnd()) return ((MotionKey2.Color3f) list.get(list.size() - 1)).getColor3f();
			Object[] key = getInterpolationKeysFor(position);
			MotionKey2.Color3f key0 = (MotionKey2.Color3f) key[0];
			MotionKey2.Color3f key1 = (MotionKey2.Color3f) key[1];
			MotionKey2.Color3f key2 = (MotionKey2.Color3f) key[2];
			MotionKey2.Color3f key3 = (MotionKey2.Color3f) key[3];
			float l = key2.getPosition() - key1.getPosition();
			float t = (position - key1.getPosition()) / l;
			javax.vecmath.Color3f p0 = key1.getColor3f();
			javax.vecmath.Color3f p1 = key2.getColor3f();
				
			if (interpolationMethod == LINEAR) {
				javax.vecmath.Color3f c = new javax.vecmath.Color3f();
				c.interpolate(p0, p1, t);
				return c;
			}
			else {
				javax.vecmath.Vector3f m0 = new javax.vecmath.Vector3f();
				if (key0 != null) {
					m0.sub(p1, key0.getColor3f());
					m0.scale(l / (key2.getPosition() - key0.getPosition()));
				}
				javax.vecmath.Vector3f m1 = new javax.vecmath.Vector3f();
				if (key3 != null) {
					m1.sub(key3.getColor3f(), p0);
					m1.scale(l / (key3.getPosition() - key1.getPosition()));
				}
				return new javax.vecmath.Color3f(
					cubicInterpolate(p0.x, m0.x, p1.x, m1.x, t),
					cubicInterpolate(p0.y, m0.y, p1.y, m1.y, t),
					cubicInterpolate(p0.z, m0.z, p1.z, m1.z, t)
				);
			}
		}
		
		public MotionKey2.Color3f setColor3fAt(float position, javax.vecmath.Color3f c) {
			MotionKey2.Color3f mk = (MotionKey2.Color3f) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey2.Color3f(position, c);
				addKey(mk);
			}
			else mk.setColor3f(c);
			return mk;
		}
		
		public MotionKey2 insertKeyAt(float position) {
			if (!(getKeyAt(position) != null)) setColor3fAt(position, getColor3fAt(position));
			return getKeyAt(position);
		}
	}
	
	public static class Quat4f extends MotionCurve2 {
		
		private Quat4f() { }
		
		public javax.vecmath.Quat4f getQuat4fAt(float position) {
			if (position <= getStart()) return ((MotionKey2.Quat4f) list.get(0)).getQuat4f();
			if (position >= getEnd()) return ((MotionKey2.Quat4f) list.get(list.size() - 1)).getQuat4f();
			Object[] key = getInterpolationKeysFor(position);
			MotionKey2.Quat4f key0 = (MotionKey2.Quat4f) key[0];
			MotionKey2.Quat4f key1 = (MotionKey2.Quat4f) key[1];
			MotionKey2.Quat4f key2 = (MotionKey2.Quat4f) key[2];
			MotionKey2.Quat4f key3 = (MotionKey2.Quat4f) key[3];
			
			/* invert quaternions if necessary to get shortest path */
			if (key0 != null) shortenQuaternionPath(key0.getQuat4f(), key1.getQuat4f());
			shortenQuaternionPath(key1.getQuat4f(), key2.getQuat4f());
			if (key3 != null) shortenQuaternionPath(key2.getQuat4f(), key3.getQuat4f());
			
			float l = key2.getPosition() - key1.getPosition();
			float t = (position - key1.getPosition()) / l;
			
			javax.vecmath.Quat4f qa, qb, qc, qd, qe, q0, q1, q2, q3, m1, m2;
			
			q1 = key1.getQuat4f();
			q2 = key2.getQuat4f();
			
			if (interpolationMethod == LINEAR) {
				return slerp(q1, q2, t);
			}
			else {
				if (key0 != null) {
					q0 = key0.getQuat4f();
					qa = slerp(q0, q1, 2.0f);
					qb = slerp(q0, q2, 2.0f);
					qc = slerp(qa, qb, 0.5f);
					m1 = slerp(q1, qc, 0.333f * l / (key2.getPosition() - key0.getPosition()));
				} else m1 = new javax.vecmath.Quat4f(q1);
				if (key3 != null) {
					q3 = key3.getQuat4f();
					qa = slerp(q3, q2, 2.0f);
					qb = slerp(q3, q1, 2.0f);
					qc = slerp(qb, qa, 0.5f);
					m2 = slerp(q2, qc, 0.333f * l / (key3.getPosition() - key1.getPosition()));
				} else m2 = new javax.vecmath.Quat4f(q2);
				qa = slerp(q1, m1, t);
				qb = slerp(m1, m2, t);
				qc = slerp(m2, q2, t);
				qd = slerp(qa, qb, t);
				qe = slerp(qb, qc, t);
				return slerp(qd, qe, t);
			}
		}
		
		public MotionKey2.Quat4f setQuat4fAt(float position, javax.vecmath.Quat4f q) {
			MotionKey2.Quat4f mk = (MotionKey2.Quat4f) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey2.Quat4f(position, q);
				addKey(mk);
			}
			else mk.setQuat4f(q);
			return mk;
		}
		
		public MotionKey2 insertKeyAt(float position) {
			if (!(getKeyAt(position) != null)) setQuat4fAt(position, getQuat4fAt(position));
			return getKeyAt(position);
		}
		
		
		
		/**
		* Spherical linear interpolation
		**/
		static javax.vecmath.Quat4f slerp(javax.vecmath.Tuple4f p, javax.vecmath.Tuple4f q, float t) {
  			float alpha, beta;
  			float cosom = p.x * q.x + p.y * q.y + p.z * q.z + p.w * q.w; 
  			
  			if (Math.abs(cosom) < 0.9999) {
  				double omega = Math.acos (cosom);
  				double sinom = Math.sin (omega);
  				alpha = (float) (Math.sin ((1.0 - t) * omega) / sinom);
  				beta = (float) (Math.sin (t * omega) / sinom);
  			} else {
  				alpha = 1.0f - t;
  				beta = t;
  			}
  			return new javax.vecmath.Quat4f(
				alpha * p.x + beta * q.x,
				alpha * p.y + beta * q.y,
				alpha * p.z + beta * q.z,
				alpha * p.w + beta * q.w
			);
		}
		
		static void shortenQuaternionPath(javax.vecmath.Quat4f q0, javax.vecmath.Quat4f q1) {
			if (q0.x * q1.x + q0.y * q1.y + q0.z * q1.z + q0.w * q1.w < 0) q1.set(-q1.x, -q1.y, -q1.z, -q1.w);
		}
	}
	
	public static final class InterpolationMethod {
		private String name;
		
		private InterpolationMethod(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}
}

