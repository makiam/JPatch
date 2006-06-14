package jpatch.entity;

import java.io.PrintStream;
import java.util.*;

import javax.vecmath.Quat4f;

import jpatch.auxilary.*;
import jpatch.entity.MotionKey.TangentMode;

public abstract class MotionCurve {
	
//	public static enum InterpolationMethod { DISCRETE, LINEAR, CUBIC };
//	
//	InterpolationMethod interpolationMethod = InterpolationMethod.CUBIC;
	String name = "*";
	List<MotionKey> list = new ArrayList<MotionKey>();

	public static MotionCurve.Float createMorphCurve(Morph morph) {
		MotionCurve.Float mc = new MotionCurve.Float();
		mc.name = morph.getId();
		mc.fMin = morph.getMin();
		mc.fMax = morph.getMax();
		return mc;
	}
	
	public static MotionCurve.Float createMorphCurve(Morph morph, MotionKey.Float key) {
		MotionCurve.Float mc = createMorphCurve(morph);
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Float createScaleCurve() {
		MotionCurve.Float mc = new MotionCurve.Float();
		mc.name = "Scale";
		mc.fMin = 0;
		mc.fMax = 10;
		return mc;
	}
	
	public static MotionCurve.Float createScaleCurve(MotionKey.Float key) {
		MotionCurve.Float mc = createScaleCurve();
		mc.addKey(key);
		return mc;
	}
		
	public static MotionCurve.Float createSizeCurve() {
		MotionCurve.Float mc = new MotionCurve.Float();
		mc.name = "Size";
		mc.fMin = 0;
		mc.fMax = 100;
		return mc;
	}
	
	public static MotionCurve.Float createSizeCurve(MotionKey.Float key) {
		MotionCurve.Float mc = createSizeCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Float createIntensityCurve() {
		MotionCurve.Float mc = new MotionCurve.Float();
		mc.name = "Intensity";
		mc.fMin = 0;
		mc.fMax = 100;
		return mc;
	}
	
	public static MotionCurve.Float createIntensityCurve(MotionKey.Float key) {
		MotionCurve.Float mc = createIntensityCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Float createFocalLengthCurve() {
		MotionCurve.Float mc = new MotionCurve.Float();
		mc.name = "Focal length";
		mc.fMin = 20;
		mc.fMax = 500;
		return mc;
	}
	
	public static MotionCurve.Float createFocalLengthCurve(MotionKey.Float key) {
		MotionCurve.Float mc = createFocalLengthCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Point3d createPositionCurve() {
		MotionCurve.Point3d mc = new MotionCurve.Point3d();
		mc.name = "Position";
		return mc;
	}
	
	public static MotionCurve.Point3d createPositionCurve(MotionKey.Point3d key) {
		MotionCurve.Point3d mc = createPositionCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Color3f createColorCurve() {
		MotionCurve.Color3f mc = new MotionCurve.Color3f();
		mc.name = "Color";
		return mc;
	}
	
	public static MotionCurve.Color3f createColorCurve(MotionKey.Color3f key) {
		MotionCurve.Color3f mc = createColorCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Quat4f createOrientationCurve() {
		MotionCurve.Quat4f mc = new MotionCurve.Quat4f();
		mc.name = "Orientation";
		return mc;
	}
	
	public static MotionCurve.Quat4f createOrientationCurve(MotionKey.Quat4f key) {
		MotionCurve.Quat4f mc = createOrientationCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Object createAnchorCurve() {
		MotionCurve.Object mc = new MotionCurve.Object();
		mc.name = "Anchor";
		return mc;
	}
	
	public static MotionCurve.Object createAnchorCurve(MotionKey.Object key) {
		MotionCurve.Object mc = createAnchorCurve();
		mc.addKey(key);
		return mc;
	}
	
	public static MotionCurve.Proxy createProxyCurve(MotionCurve motionCurve, MotionKey.Axis axis) {
		MotionCurve.Proxy mc = new MotionCurve.Proxy(motionCurve, axis);
		return mc;
	}
	
	/**
	 * Sets the name of this curve
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name of this curve
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Adds a MotionKey to this curve. Note that the position (where the key will be added)
	 * is already specified in the passed MotionKey object.
	 * @param key The key to add
	 */
	public void addKey(MotionKey key) {
		System.out.println("addKey " + key.hashCode() + " pos " + key.getPosition());
		list.add(binarySearch(key.getPosition()), key);
		key.setMotionCurve(this);
		key.computeDerivatives();
	}
	
	/**
	 * Inserts a new key at the given position and returns that key.
	 * @param position The position to insert the new key.
	 * @return The newly inserted key.
	 */
	public abstract MotionKey insertKeyAt(float position);
	
	/**
	 * Removes a key from this curve only if it was not the last key on the curve
	 * @param The key to remove
	 * @return The key if it has been removed (if it wasn't the last on the curve), null otherwise
	 */
	public MotionKey removeKey(MotionKey key) {
		System.out.println("removeKey " + key.hashCode() + " pos " + key.getPosition());
		if (list.size() > 1) {
			list.remove(key);
			return key;
		} else {
			return null;
		}
	}
	
	/**
	 * Removes a key from this curve, even if it is the last key and the curve will be empty then.
	 * @param key The key to remove
	 */
	public void forceRemoveKey(MotionKey key) {
		System.out.println("removeKey " + key.hashCode() + " pos " + key.getPosition());
		list.remove(key);
	}
	
	/**
	 * Moves a key on this curve to a new position
	 * @param key The key to move
	 * @param position The new position
	 */
	public void moveKey(MotionKey key, float position) {
		System.out.println("movekey " + key.hashCode() + " from " + key.getPosition() + " to " + position);
		list.remove(key);
		key.setPosition(position);
		list.add(binarySearch(key.getPosition()), key);
		key.computeDerivatives();
	}
	
	/**
	 * Returns the number of keys on this curve.
	 * @return The number of keys on this curve.
	 */
	public int getKeyCount() {
		return list.size();
	}
	
	public MotionKey getKey(int number) {
		return (MotionKey) list.get(number);
	}
	
	public float getStart() {
		return ((MotionKey) list.get(0)).getPosition();
	}
	
	public float getEnd() {
		return ((MotionKey) list.get(list.size() - 1)).getPosition();
	}
	
	public boolean hasKeyAt(float position) {
		return getKeyAt(position) != null;
	}
	
	public MotionKey getKeyAt(float position) {
		if (list.size() == 0)
			return null;
		if (position == ((MotionKey) list.get(list.size() - 1)).getPosition()) return (MotionKey) list.get(list.size() - 1);
		int index = binarySearch(position) - 1;
		if (index >= 0) {
			MotionKey mk = (MotionKey) list.get(index);
			if (mk != null && mk.getPosition() == position) return mk;
			else return null;
		} else return null;
	}
	
	abstract void computeDerivatives(MotionKey k);
	
	public void xml(PrintStream out, String prefix, String type) {
		out.append(prefix).append("<motioncurve " + type + ">\n");
		for (MotionKey key : list) {
			out.append(prefix).append("\t");
			key.xml(out);
		}
		out.append(prefix).append("</motioncurve>").append("\n");
	}
	
	public void dump() {
		for (MotionKey key : list) {
			System.out.println("\t" + key);
		}
	}
//	public MotionKey getPrevKey(float position) {
//		int index = binarySearch(position) - 1;
//		if (index >= 0) {
//			MotionKey key = (MotionKey) list.get(index);
//			if (key.getPosition() < position) return key;
//			else if (index == 0) return key;
//			else return (MotionKey) list.get(index - 1);
//		}
//		return (MotionKey) list.get(0);
//	}
	

//	public MotionKey getNextKey(float position) {
//		int index = binarySearch(position) - 1;
//		if (index < list.size() && index >= 0) {
//			MotionKey key = (MotionKey) list.get(index);
//			if (key.getPosition() > position) return key;
//			else if (index == list.size() - 1) return key;
//			else return (MotionKey) list.get(index + 1);
//		}
//		return (MotionKey) list.get(list.size() - 1);
//	}
	
//	public InterpolationMethod getInterpolationMethod() {
//		return interpolationMethod;
//	}
//	
//	public void setInterpolationMethod(InterpolationMethod interpolationMethod) {
//		this.interpolationMethod = interpolationMethod;
//	}
	
	public void clear() {
		list.clear();
	}
	
	public int getIndexAt(float position) {
		return binarySearch(position);
	}
	
	int binarySearch(float position) {
		if (list.size() == 0) return 0;
		if (position < ((MotionKey) list.get(0)).getPosition()) return 0;
		if (position >= ((MotionKey) list.get(list.size() - 1)).getPosition()) return list.size();
		int min = 0;
		int max = list.size() - 1;
		int i = max >> 1;
		while (max > min + 1) {
			if (((MotionKey) list.get(i)).getPosition() > position) {
				max = i;
				i -= ((i - min) >> 1);
			} else {
				min = i;
				i += ((max - i) >> 1);
			}
		}
		return max;
	}
	
//	java.lang.Object[] getInterpolationKeysFor(float position) {
//		int index = binarySearch(position) - 1;
//		return new java.lang.Object[] {
//			(index > 0) ? list.get(index - 1) : null,
//			list.get(index),
//			list.get(index + 1),
//			(index < list.size() - 2) ? list.get(index + 2) : null
//		};
//	}
	
	static float cubicInterpolate(float p0, float m0, float p1, float m1, float t) {
		float t2 = t * t;
		float t3 = t * t2;
		float H0 = 2 * t3 - 3 * t2 + 1;
		float H1 = t3 - 2 * t2 + t;
		float H2 = -2 * t3 + 3 * t2;
		float H3 = t3 - t2;
		return H0 * p0 + H1 * m0 + H2 * p1 + H3 * m1;
	}
	
	static double cubicInterpolate(double p0, double m0, double p1, double m1, float t) {
		float t2 = t * t;
		float t3 = t * t2;
		float H0 = 2 * t3 - 3 * t2 + 1;
		float H1 = t3 - 2 * t2 + t;
		float H2 = -2 * t3 + 3 * t2;
		float H3 = t3 - t2;
		return H0 * p0 + H1 * m0 + H2 * p1 + H3 * m1;
	}
	
	public static class Float extends MotionCurve {
		float fMin, fMax;
		
		private Float() { }
		
		@Override
		void computeDerivatives(MotionKey k) {
			MotionKey.Float key = (MotionKey.Float) k;
			
			/* if tangent mode is manual, do nothing and return */
			if (key.getTangentMode() == MotionKey.TangentMode.MANUAL)
				return;
			
			int keyIndex = key.getIndex();
			
			/* for the first and last key, return 0 tangent */
			if ((keyIndex == 0) || (keyIndex == list.size() - 1)) {
				key.setDfIn(0);
				key.setDfOut(0);
				return;
			}
			
			MotionKey.Float prevKey = (MotionKey.Float) list.get(keyIndex - 1);
			MotionKey.Float nextKey = (MotionKey.Float) list.get(keyIndex + 1);
			float fp = prevKey.getFloat();
			float f = key.getFloat();
			float fn = nextKey.getFloat();
			
			/* compute tangent */
			float d = 0;
			float p = key.getPosition();
			float pn = nextKey.getPosition();
			float pp = prevKey.getPosition();
			if (prevKey.interpolation == MotionKey.Interpolation.DISCRETE)
				d = 0;
			else if (prevKey.interpolation == MotionKey.Interpolation.LINEAR)
				d = (f - fp) / (key.getPosition() - prevKey.getPosition());
			else if (key.interpolation == MotionKey.Interpolation.DISCRETE)
				d = 0;
			else if (key.interpolation == MotionKey.Interpolation.LINEAR)
				d = (fn - f) / (pn - p);
			else
				d = (fn - fp) / (pn - pp);
			
			/* apply overshoot limitation */
			if (key.getTangentMode() == TangentMode.OVERSHOOT) {
				if (d > 0) {
					if (d * (pn - p) / 3 > (fn - f))
						d = (fn - f) / (pn - p) * 3;
					if (d * (p - pp) / 3 > (f - fp))
						d = (f - fp) / (p - pp) * 3;
				} else if (d < 0) {
					if (d * (pn - p) / 3 < (fn - f))
						d = (fn - f) / (pn - p) * 3;
					if (d * (p - pp) / 3 < (f - fp))
						d = (f - fp) / (p - pp) * 3;
				}
			}
			key.setDfIn(d);
			key.setDfOut(d);
			return;
		}
		
		public float getFloatAt(float position) {
			
			/* if outside of range, return value of first or last key, respectively */
			if (position <= getStart()) return ((MotionKey.Float) list.get(0)).getFloat();
			if (position >= getEnd()) return ((MotionKey.Float) list.get(list.size() - 1)).getFloat();
			
			/* binary search for keys */
			int index = getIndexAt(position);
			MotionKey.Float k0 = (MotionKey.Float) list.get(index - 1);	// the key before position
			MotionKey.Float k1 = (MotionKey.Float) list.get(index);		// the key after position
			
			/* compute some values needed for interpolation */
			float l = k1.getPosition() - k0.getPosition();	// the distance (in frames) between the two keys
			float t = (position - k0.getPosition()) / l;	// relative position: 0 = at k0, 1 = at k1, 0..1 inbetween
			float p0 = k0.getFloat();						// value at k0
			float p1 = k1.getFloat();						// value at k1
			
			/* switch by interpolation method */
			switch (k0.getInterpolation()) {
			case DISCRETE:
				return p0;									// return k0's value
			case LINEAR:
				return (p0 * (1 - t) + p1 * t);				// return linear interpolation
			case CUBIC:
				float m0 = k0.getDfOut() * l;				// get derivative at k0
				float m1 = k1.getDfIn() * l;				// get derivative at k1
				return cubicInterpolate(p0, m0, p1, m1, t);	// return cubic interpolation
			}
			throw new IllegalStateException();
		}
		
		public MotionKey.Float setFloatAt(float position, float f) {
			MotionKey.Float mk = (MotionKey.Float) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey.Float(position, f);
				addKey(mk);
			}
			else mk.setFloat(f);
			return mk;
		}
		
		public MotionKey insertKeyAt(float position) {
			if (!(getKeyAt(position) != null))
				setFloatAt(position, getFloatAt(position));
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
	
	public static class Proxy extends MotionCurve.Float {
		private MotionCurve motionCurve;
		private MotionKey.Proxy proxyKey;
		
		public Proxy(MotionCurve motionCurve, MotionKey.Axis axis) {
			this.motionCurve = motionCurve;
			this.proxyKey = new MotionKey.Proxy(axis);
			proxyKey.setMotionCurve(this);
		}
		
		public float getFloatAt(float position) {
			if (motionCurve instanceof MotionCurve.Color3f) {
				javax.vecmath.Color3f color = ((MotionCurve.Color3f) motionCurve).getColor3fAt(position);
				switch (proxyKey.getAxis()) {
				case X:
					return color.x;
				case Y:
					return color.y;
				case Z:
					return color.z;
				}
			} else if (motionCurve instanceof MotionCurve.Point3d) {
				javax.vecmath.Point3d point = ((MotionCurve.Point3d) motionCurve).getPoint3dAt(position);
				switch (proxyKey.getAxis()) {
				case X:
					return (float) point.x;
				case Y:
					return (float) point.y;
				case Z:
					return (float) point.z;
				}
			} else if (motionCurve instanceof MotionCurve.Quat4f) {
				javax.vecmath.Quat4f quat = ((MotionCurve.Quat4f) motionCurve).getQuat4fAt(position);
				switch (proxyKey.getAxis()) {
				case X:
					return quat.x;
				case Y:
					return quat.y;
				case Z:
					return quat.z;
				case W:
					return quat.w;
				}
			}
			throw new IllegalStateException();
		}
		
		public MotionKey.Float setFloatAt(float position, float f) {
			MotionKey mk = motionCurve.getKeyAt(position);
			if (mk == null) {
				mk = motionCurve.insertKeyAt(position);
			}
			proxyKey.setKey((MotionKey.ProxyAccessible) mk);
			proxyKey.setFloat(f);
			return proxyKey;
		}
		
		public float getMin() {
			return -200;
		}
		
		public float getMax() {
			return 200;
		}
		
		@Override
		public String getName() {
			return motionCurve.getName() + " " + proxyKey.getAxis();
		}
		
		@Override
		public void addKey(MotionKey key) {
			motionCurve.addKey(key);
		}
		
		@Override
		public MotionKey insertKeyAt(float position) {
			return motionCurve.insertKeyAt(position);
		}
		
		@Override
		public MotionKey removeKey(MotionKey key) {
			return motionCurve.removeKey(key);
		}
		
		@Override
		public void forceRemoveKey(MotionKey key) {
			motionCurve.forceRemoveKey(key);
		}
		
		@Override
		public void moveKey(MotionKey key, float position) {
			motionCurve.moveKey(key, position);
		}
		
		@Override
		public int getKeyCount() {
			return motionCurve.getKeyCount();
		}
		
		@Override
		public MotionKey getKey(int number) {
			proxyKey.setKey((MotionKey.ProxyAccessible) motionCurve.getKey(number));
			return proxyKey;
		}
		
		@Override
		public float getStart() {
			return motionCurve.getStart();
		}
		
		@Override
		public float getEnd() {
			return motionCurve.getEnd();
		}
		
		@Override
		public boolean hasKeyAt(float position) {
			return motionCurve.hasKeyAt(position);
		}
		
		@Override
		public MotionKey getKeyAt(float position) {
			MotionKey.ProxyAccessible key = (MotionKey.ProxyAccessible) motionCurve.getKeyAt(position);
			if (key == null)
				return null;
			proxyKey.setKey(key);
			return proxyKey;
		}
		
		@Override
		void computeDerivatives(MotionKey k) {
			motionCurve.computeDerivatives(k);
		}
		
		@Override
		public void xml(PrintStream out, String prefix, String type) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int getIndexAt(float position) {
			return motionCurve.binarySearch(position);
		}
		
		@Override
		int binarySearch(float position) {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class Point3d extends MotionCurve {
		
		private Point3d() { }
		
		@Override
		void computeDerivatives(MotionKey k) {
			MotionKey.Point3d key = (MotionKey.Point3d) k;
			
			/* if tangent mode is manual, do nothing and return */
			if (key.getTangentMode() == MotionKey.TangentMode.MANUAL)
				return;
			
			int keyIndex = key.getIndex();
			
			/* for the first and last key, return 0 tangent */
			if ((keyIndex == 0) || (keyIndex == list.size() - 1)) {
				key.setDpIn(0, 0, 0);
				key.setDpOut(0, 0, 0);
				return;
			}
			
			MotionKey.Point3d prevKey = (MotionKey.Point3d) list.get(keyIndex - 1);
			MotionKey.Point3d nextKey = (MotionKey.Point3d) list.get(keyIndex + 1);
			javax.vecmath.Point3d p3p = prevKey.getPoint3d();
			javax.vecmath.Point3d p3 = key.getPoint3d();
			javax.vecmath.Point3d p3n = nextKey.getPoint3d();
			
			/* compute tangent */
			javax.vecmath.Vector3d d = new javax.vecmath.Vector3d();
			float p = key.getPosition();
			float pn = nextKey.getPosition();
			float pp = prevKey.getPosition();
			if (prevKey.interpolation == MotionKey.Interpolation.DISCRETE) {
				d.set(0, 0, 0);
			} else if (prevKey.interpolation == MotionKey.Interpolation.LINEAR) {
				d.sub(p3, p3p);
				d.scale(1.0 / (p - pp));
			} else if (key.interpolation == MotionKey.Interpolation.DISCRETE) {
				d.set(0, 0, 0);
			} else if (key.interpolation == MotionKey.Interpolation.LINEAR) {
				d.sub(p3n, p3);
				d.scale(1.0 / (pn - p));
			} else {
				d.sub(p3n, p3p);
				d.scale(1.0 / pn - pp);
			}
			key.setDpIn(d);
			key.setDpOut(d);
			return;
		}
		
		public javax.vecmath.Point3d getPoint3dAt(float position) {
			
			/* if outside of range, return value of first or last key, respectively */
			if (position <= getStart()) return ((MotionKey.Point3d) list.get(0)).getPoint3d();
			if (position >= getEnd()) return ((MotionKey.Point3d) list.get(list.size() - 1)).getPoint3d();
			
			/* binary search for keys */
			int index = getIndexAt(position);
			MotionKey.Point3d k0 = (MotionKey.Point3d) list.get(index - 1);	// the key before position
			MotionKey.Point3d k1 = (MotionKey.Point3d) list.get(index);		// the key after position
			
			/* compute some values needed for interpolation */
			float l = k1.getPosition() - k0.getPosition();	// the distance (in frames) between the two keys
			float t = (position - k0.getPosition()) / l;	// relative position: 0 = at k0, 1 = at k1, 0..1 inbetween
			javax.vecmath.Point3d p0 = k0.getPoint3d();		// value at k0
			javax.vecmath.Point3d p1 = k1.getPoint3d();		// value at k1
			
			/* switch by interpolation method */
			javax.vecmath.Point3d p;
			switch (k0.getInterpolation()) {
			case DISCRETE:
				return p0;									// return k0's value
			case LINEAR:
				p = new javax.vecmath.Point3d(p0);
				p.interpolate(p1, (double) t);				// return linear interpolation
				return p;
			case CUBIC:
				javax.vecmath.Point3d m0 = k0.getDpOut(); // get derivative at k0
				javax.vecmath.Point3d m1 = k1.getDpIn();  // get derivative at k1
				p = new javax.vecmath.Point3d(
						cubicInterpolate(p0.x, m0.x * l, p1.x, m1.x * l, t),
						cubicInterpolate(p0.y, m0.y * l, p1.y, m1.y * l, t),
						cubicInterpolate(p0.z, m0.z * l, p1.z, m1.z * l, t)
				);
				return p;	// return cubic interpolation
			}
			throw new IllegalStateException();
		}
		
		public MotionKey.Point3d setPoint3dAt(float position, javax.vecmath.Point3d p) {
			MotionKey.Point3d mk = (MotionKey.Point3d) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey.Point3d(position, p);
				addKey(mk);
			}
			else mk.setPoint3d(p);
			return mk;
		}
		
		public MotionKey insertKeyAt(float position) {
			if (!(getKeyAt(position) != null)) setPoint3dAt(position, getPoint3dAt(position));
			return getKeyAt(position);
		}
	}
	
	public static class Color3f extends MotionCurve {
		
		private Color3f() { }
		
		@Override
		void computeDerivatives(MotionKey k) {
			MotionKey.Color3f key = (MotionKey.Color3f) k;
			
			/* if tangent mode is manual, do nothing and return */
			if (key.getTangentMode() == MotionKey.TangentMode.MANUAL)
				return;
			
			int keyIndex = key.getIndex();
			
			/* for the first and last key, return 0 tangent */
			if ((keyIndex == 0) || (keyIndex == list.size() - 1)) {
				key.setDcIn(0, 0, 0);
				key.setDcOut(0, 0, 0);
				return;
			}
			
			MotionKey.Color3f prevKey = (MotionKey.Color3f) list.get(keyIndex - 1);
			MotionKey.Color3f nextKey = (MotionKey.Color3f) list.get(keyIndex + 1);
			javax.vecmath.Color3f c3p = prevKey.getColor3f();
			javax.vecmath.Color3f c3 = key.getColor3f();
			javax.vecmath.Color3f c3n = nextKey.getColor3f();
			
			/* compute tangent */
			javax.vecmath.Color3f d = new javax.vecmath.Color3f();
			float p = key.getPosition();
			float pn = nextKey.getPosition();
			float pp = prevKey.getPosition();
			if (prevKey.interpolation == MotionKey.Interpolation.DISCRETE) {
				d.set(0, 0, 0);
			} else if (prevKey.interpolation == MotionKey.Interpolation.LINEAR) {
				d.sub(c3, c3p);
				d.scale(1.0f / (p - pp));
			} else if (key.interpolation == MotionKey.Interpolation.DISCRETE) {
				d.set(0, 0, 0);
			} else if (key.interpolation == MotionKey.Interpolation.LINEAR) {
				d.sub(c3n, c3);
				d.scale(1.0f / (pn - p));
			} else {
				d.sub(c3n, c3p);
				d.scale(1.0f / pn - pp);
			}
			key.setDcIn(d);
			key.setDcOut(d);
			return;
		}
		
		public javax.vecmath.Color3f getColor3fAt(float position) {
			
			/* if outside of range, return value of first or last key, respectively */
			if (position <= getStart()) return ((MotionKey.Color3f) list.get(0)).getColor3f();
			if (position >= getEnd()) return ((MotionKey.Color3f) list.get(list.size() - 1)).getColor3f();
			
			/* binary search for keys */
			int index = getIndexAt(position);
			MotionKey.Color3f k0 = (MotionKey.Color3f) list.get(index - 1);	// the key before position
			MotionKey.Color3f k1 = (MotionKey.Color3f) list.get(index);		// the key after position
			
			/* compute some values needed for interpolation */
			float l = k1.getPosition() - k0.getPosition();	// the distance (in frames) between the two keys
			float t = (position - k0.getPosition()) / l;	// relative position: 0 = at k0, 1 = at k1, 0..1 inbetween
			javax.vecmath.Color3f c0 = k0.getColor3f();		// value at k0
			javax.vecmath.Color3f c1 = k1.getColor3f();		// value at k1
			
			/* switch by interpolation method */
			javax.vecmath.Color3f c;
			switch (k0.getInterpolation()) {
			case DISCRETE:
				return c0;									// return k0's value
			case LINEAR:
				c = new javax.vecmath.Color3f(c0);
				c.interpolate(c1, t);				// return linear interpolation
				return c;
			case CUBIC:
				javax.vecmath.Color3f m0 = k0.getDcOut(); // get derivative at k0
				javax.vecmath.Color3f m1 = k1.getDcIn();  // get derivative at k1
				c = new javax.vecmath.Color3f(
						cubicInterpolate(c0.x, m0.x * l, c1.x, m1.x * l, t),
						cubicInterpolate(c0.y, m0.y * l, c1.y, m1.y * l, t),
						cubicInterpolate(c0.z, m0.z * l, c1.z, m1.z * l, t)
				);
				return c;	// return cubic interpolation
			}
			throw new IllegalStateException();
		}
		
		public MotionKey.Color3f setColor3fAt(float position, javax.vecmath.Color3f c) {
			MotionKey.Color3f mk = (MotionKey.Color3f) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey.Color3f(position, c);
				addKey(mk);
			}
			else mk.setColor3f(c);
			return mk;
		}
		
		public MotionKey insertKeyAt(float position) {
			if (!(getKeyAt(position) != null)) setColor3fAt(position, getColor3fAt(position));
			return getKeyAt(position);
		}
	}
	
	public static class Quat4f extends MotionCurve {
		private static javax.vecmath.Quat4f qa = new javax.vecmath.Quat4f();
		private static javax.vecmath.Quat4f qb = new javax.vecmath.Quat4f();
		private static javax.vecmath.Quat4f qc = new javax.vecmath.Quat4f();
		private static javax.vecmath.Quat4f qd = new javax.vecmath.Quat4f();
		private static javax.vecmath.Quat4f qe = new javax.vecmath.Quat4f();
		
		private Quat4f() { }
		
		@Override
		void computeDerivatives(MotionKey k) {
			MotionKey.Quat4f key = (MotionKey.Quat4f) k;
			
			/* if tangent mode is manual, do nothing and return */
			if (key.getTangentMode() == MotionKey.TangentMode.MANUAL)
				return;
			
			int keyIndex = key.getIndex();
			
			/* for the first and last key, return 0 tangent */
			if ((keyIndex == 0) || (keyIndex == list.size() - 1)) {
				key.setDqIn(key.getQuat4f());
				key.setDqOut(key.getQuat4f());
				return;
			}
			
			MotionKey.Quat4f prevKey = (MotionKey.Quat4f) list.get(keyIndex - 1);
			MotionKey.Quat4f nextKey = (MotionKey.Quat4f) list.get(keyIndex + 1);
			javax.vecmath.Quat4f q4p = prevKey.getQuat4f();
			javax.vecmath.Quat4f q4 = key.getQuat4f();
			javax.vecmath.Quat4f q4n = nextKey.getQuat4f();
			
			/* invert quaternions if necessary to get shortest path */
			shortenQuaternionPath(q4p, q4);
			shortenQuaternionPath(q4, q4n);
			
			/* compute tangent */
			javax.vecmath.Quat4f d = new javax.vecmath.Quat4f();
			float p = key.getPosition();
			float pn = nextKey.getPosition();
			float pp = prevKey.getPosition();
			if (prevKey.interpolation == MotionKey.Interpolation.DISCRETE) {
				d.set(q4);
			} else if (prevKey.interpolation == MotionKey.Interpolation.LINEAR) {
				d.interpolate(q4p, q4, 1.0 / (p - pp));
			} else if (key.interpolation == MotionKey.Interpolation.DISCRETE) {
				d.set(q4);
			} else if (key.interpolation == MotionKey.Interpolation.LINEAR) {
				d.interpolate(q4, q4n, 1.0 / (pn - p));
			} else {
				qa.interpolate(q4p, q4, 2.0);
				qb.interpolate(q4p, q4n, 2.0);
				qc.interpolate(qa, qb, 0.5);
				d.interpolate(q4, qc, (pn - p) / (pn - pp) / 3.0);
			}
			key.setDqIn(d);
			key.setDqOut(d);
			return;
		}
		
		public javax.vecmath.Quat4f getQuat4fAt(float position) {
			
			/* if outside of range, return value of first or last key, respectively */
			if (position <= getStart()) return ((MotionKey.Quat4f) list.get(0)).getQuat4f();
			if (position >= getEnd()) return ((MotionKey.Quat4f) list.get(list.size() - 1)).getQuat4f();
			
			/* binary search for keys */
			int index = getIndexAt(position);
			MotionKey.Quat4f k0 = (MotionKey.Quat4f) list.get(index - 1);	// the key before position
			MotionKey.Quat4f k1 = (MotionKey.Quat4f) list.get(index);		// the key after position
			
			/* compute some values needed for interpolation */
			float l = k1.getPosition() - k0.getPosition();	// the distance (in frames) between the two keys
			double t = (position - k0.getPosition()) / l;	// relative position: 0 = at k0, 1 = at k1, 0..1 inbetween
			javax.vecmath.Quat4f q0 = k0.getQuat4f();		// value at k0
			javax.vecmath.Quat4f q1 = k1.getQuat4f();		// value at k1
			
			/* switch by interpolation method */
			javax.vecmath.Quat4f q;
			switch (k0.getInterpolation()) {
			case DISCRETE:
				return q0;									// return k0's value
			case LINEAR:
				q = new javax.vecmath.Quat4f(q0);
				q.interpolate(q1, t);				// return linear interpolation
				return q;
			case CUBIC:
				javax.vecmath.Quat4f m0 = k0.getDqOut(); // get derivative at k0
				javax.vecmath.Quat4f m1 = k1.getDqIn();  // get derivative at k1
				q = new javax.vecmath.Quat4f();
				qa.interpolate(q0, m0, t);
				qb.interpolate(m0, m1, t);
				qc.interpolate(m1, q1, t);
				qd.interpolate(qa, qb, t);
				qe.interpolate(qb, qc, t);
				q.interpolate(qd, qe, t);
				return q;	// return cubic interpolation
			}
			throw new IllegalStateException();
		}

//		public javax.vecmath.Quat4f getQuat4fAt_OLD(float position) {
//			if (position <= getStart()) return ((MotionKey.Quat4f) list.get(0)).getQuat4f();
//			if (position >= getEnd()) return ((MotionKey.Quat4f) list.get(list.size() - 1)).getQuat4f();
//			java.lang.Object[] key = getInterpolationKeysFor(position);
//			MotionKey.Quat4f key0 = (MotionKey.Quat4f) key[0];
//			MotionKey.Quat4f key1 = (MotionKey.Quat4f) key[1];
//			MotionKey.Quat4f key2 = (MotionKey.Quat4f) key[2];
//			MotionKey.Quat4f key3 = (MotionKey.Quat4f) key[3];
//			
//			/* invert quaternions if necessary to get shortest path */
//			if (key0 != null) shortenQuaternionPath(key0.getQuat4f(), key1.getQuat4f());
//			shortenQuaternionPath(key1.getQuat4f(), key2.getQuat4f());
//			if (key3 != null) shortenQuaternionPath(key2.getQuat4f(), key3.getQuat4f());
//			
//			float l = key2.getPosition() - key1.getPosition();
//			float t = (position - key1.getPosition()) / l;
//			
//			javax.vecmath.Quat4f qa, qb, qc, qd, qe, q0, q1, q2, q3, m1, m2;
//			
//			q1 = key1.getQuat4f();
//			q2 = key2.getQuat4f();
//			
//			switch (key1.getInterpolation()) {
//			case DISCRETE:
//				return new javax.vecmath.Quat4f(q1);
//			case LINEAR:
//				return slerp(q1, q2, t);
//			case CUBIC:
//				if (key0 != null) {
//					q0 = key0.getQuat4f();
//					qa = slerp(q0, q1, 2.0f);
//					qb = slerp(q0, q2, 2.0f);
//					qc = slerp(qa, qb, 0.5f);
//					m1 = slerp(q1, qc, 0.333f * l / (key2.getPosition() - key0.getPosition()));
//				} else m1 = new javax.vecmath.Quat4f(q1);
//				if (key3 != null) {
//					q3 = key3.getQuat4f();
//					qa = slerp(q3, q2, 2.0f);
//					qb = slerp(q3, q1, 2.0f);
//					qc = slerp(qb, qa, 0.5f);
//					m2 = slerp(q2, qc, 0.333f * l / (key3.getPosition() - key1.getPosition()));
//				} else m2 = new javax.vecmath.Quat4f(q2);
//				qa = slerp(q1, m1, t);
//				qb = slerp(m1, m2, t);
//				qc = slerp(m2, q2, t);
//				qd = slerp(qa, qb, t);
//				qe = slerp(qb, qc, t);
//				return slerp(qd, qe, t);
//			}
//			throw new IllegalStateException();
//		}
		
		public MotionKey.Quat4f setQuat4fAt(float position, javax.vecmath.Quat4f q) {
			MotionKey.Quat4f mk = (MotionKey.Quat4f) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey.Quat4f(position, q);
				addKey(mk);
			}
			else mk.setQuat4f(q);
			return mk;
		}
		
		public MotionKey insertKeyAt(float position) {
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
	
	public static class Object extends MotionCurve {
		
		private Object() { }

		@Override
		void computeDerivatives(MotionKey k) {
			;	// do nothing
		}
		
		public java.lang.Object getObjectAt(float position) {
			if (position <= getStart()) return ((MotionKey.Object) list.get(0)).getObject();
			if (position >= getEnd()) return ((MotionKey.Object) list.get(list.size() - 1)).getObject();
			return ((MotionKey.Object) getKeyAt(position)).getObject();
		}
		
		public MotionKey.Object setObjectAt(float position, java.lang.Object o) {
			MotionKey.Object mk = (MotionKey.Object) getKeyAt(position);
			if (mk == null) {
				mk = new MotionKey.Object(position, o);
				addKey(mk);
			}
			else mk.setObject(o);
			return mk;
		}
		
		@Override
		public MotionKey insertKeyAt(float position) {
			if (!(getKeyAt(position) != null))
				setObjectAt(position, getObjectAt(position));
			return getKeyAt(position);
		}
		
//		@Override
//		public InterpolationMethod getInterpolationMethod() {
//			return InterpolationMethod.DISCRETE;
//		}
	}
}

