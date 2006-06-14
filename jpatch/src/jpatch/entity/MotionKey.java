package jpatch.entity;

import java.io.PrintStream;

public abstract class MotionKey {
	public static enum Axis { X, Y, Z, W };
	public static enum Interpolation { DISCRETE, LINEAR, CUBIC };
	public static enum TangentMode { AUTO, OVERSHOOT, MANUAL };
	
	Interpolation interpolation = Interpolation.CUBIC;
	TangentMode tangentMode;
	boolean smooth = true;
	float position;
	MotionCurve motionCurve;
	
	public int getIndex() {
		return motionCurve.getIndexAt(position) - 1;
	}

	public MotionCurve getMotionCurve() {
		return motionCurve;
	}

	void setMotionCurve(MotionCurve motionCurve) {
		this.motionCurve = motionCurve;
	}
	
	private MotionKey() { }
	
	private MotionKey(float position) {
		this.position = position;
	}
	
	public float getPosition() {
		return position;
	}
	
	public void setPosition(float position) {
		this.position = position;
	}
	
	public Interpolation getInterpolation() {
		return interpolation;
	}

	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
		computeDerivatives();
	}

	public boolean isSmooth() {
		return smooth;
	}

	public void setSmooth(boolean smooth) {
		this.smooth = smooth;
	}

	public TangentMode getTangentMode() {
		return tangentMode;
	}

	public void setTangentMode(TangentMode tangentMode) {
		this.tangentMode = tangentMode;
		computeDerivatives();
	}
	
	public void computeDerivatives() {
		if (motionCurve == null)
			return;
//		if (motionCurve == null) {
//			System.err.println("computeDerivatives() called but motionCurve = null!");
//			for (StackTraceElement ste : Thread.currentThread().getStackTrace())
//				System.out.println("\t" + ste);
//			return;
//		}
		motionCurve.computeDerivatives(this);
		int index = getIndex();
		if (index > 0)
			motionCurve.computeDerivatives(motionCurve.getKey(index - 1));
		if (index < motionCurve.getKeyCount() - 1)
			motionCurve.computeDerivatives(motionCurve.getKey(index + 1));
	}
	
	@Override
	public String toString() {
		return getClass().getName() + " position=" + position;
	}
	
	public abstract MotionKey copy();
	
	public abstract void xml(PrintStream out);
	
	public static class Float extends MotionKey {
		private float f;
		private float dfIn;
		private float dfOut;
		
		private Float() { }
		
		public Float(float position, float f) {
			super(position);
			this.f = f;
			tangentMode = TangentMode.OVERSHOOT;
		}
		
		public float getFloat() {
			return f;
		}
		
		public void setFloat(float f) {
			this.f = f;
			computeDerivatives();
		}
		
		public float getDfIn() {
			return dfIn;
		}
		
		public void setDfIn(float dfIn) {
			this.dfIn = dfIn;
			System.out.println(this + ".setDfIn(" + dfOut + ")");
		}
		
		public float getDfOut() {
			if (smooth)
				return dfIn;
			return dfOut;
		}
		
		public void setDfOut(float dfOut) {
			this.dfOut = dfOut;
			System.out.println(this + ".setDfOut(" + dfOut + ")");
		}
		
		@Override
		public void xml(PrintStream out) {
			out.append("<key frame=\"").append(java.lang.Float.toString(position)).append("\"");
			out.append(" value=\"").append(java.lang.Float.toString(f)).append("\"");
			out.append(" interpolation=\"").append(interpolation.toString().toLowerCase()).append("\"");
			if (interpolation == Interpolation.CUBIC) {
				out.append(" tangentMode=\"").append(tangentMode.toString().toLowerCase()).append("\"");
				if (tangentMode == TangentMode.MANUAL) {
					if (smooth) {
						out.append(" delta=\"").append(java.lang.Float.toString(dfIn)).append("\"");
					} else {
						out.append(" deltaIn=\"").append(java.lang.Float.toString(dfIn)).append("\"");
						out.append(" deltaOut=\"").append(java.lang.Float.toString(dfOut)).append("\"");
					}
				}
			}
			out.append("/>");
			out.println();
		}
		
		@Override
		public MotionKey copy() {
			MotionKey.Float copy = new MotionKey.Float(position, f);
			copy.interpolation = interpolation;
			copy.tangentMode = tangentMode;
			copy.smooth = smooth;
			copy.dfIn = dfIn;
			copy.dfOut = dfOut;
			return copy;
		}
		
		@Override
		public String toString() {
			return super.toString() + " (" + f + ")";
		}
	}
	
	public static class Proxy extends Float {
		private ProxyAccessible key;
		private Axis axis;
		
		public Proxy(Axis axis) {
			this.axis = axis;
		}
		
		public void setKey(ProxyAccessible key) {
			this.key = key;
		}
		
		public Axis getAxis() {
			return axis;
		}
		
		@Override
		public float getFloat() {
			return key.proxyGet(axis);
		}
		
		@Override
		public float getDfIn() {
			return key.proxyGetDin(axis);
		}
		
		@Override
		public float getDfOut() {
			return key.proxyGetDout(axis);
		}
		
		@Override
		public void setFloat(float f) {
			key.proxySet(axis, f);
		}
		
		@Override
		public void setDfIn(float f) {
			key.proxySetDin(axis, f);
		}
		
		@Override
		public void setDfOut(float f) {
			key.proxySetDout(axis, f);
		}
		
		@Override
		public int getIndex() {
			return key.getIndex();
		}
		
		@Override
		public float getPosition() {
			return key.getPosition();
		}
		
		@Override
		public void setPosition(float position) {
			key.setPosition(position);
		}
		
		@Override
		public Interpolation getInterpolation() {
			return key.getInterpolation();
		}

		@Override
		public void setInterpolation(Interpolation interpolation) {
			key.setInterpolation(interpolation);
		}

		@Override
		public boolean isSmooth() {
			return key.isSmooth();
		}

		@Override
		public void setSmooth(boolean smooth) {
			key.setSmooth(smooth);
		}

		@Override
		public TangentMode getTangentMode() {
			return key.getTangentMode();
		}

		@Override
		public void setTangentMode(TangentMode tangentMode) {
			key.setTangentMode(tangentMode);
		}
		
		@Override
		public void computeDerivatives() {
			key.computeDerivatives();
		}
		
		@Override
		public MotionKey copy() {
			return key.copy();
		}
		
		@Override
		public String toString() {
			return super.toString() + " (" + getFloat() + ")";
		}
	}
	
	public static class Point3d extends MotionKey implements ProxyAccessible {
		private javax.vecmath.Point3d p;
		private javax.vecmath.Point3d dpIn = new javax.vecmath.Point3d();
		private javax.vecmath.Point3d dpOut = new javax.vecmath.Point3d();
		
		public Point3d(float position, javax.vecmath.Point3d p) {
			super(position);
			this.p = new javax.vecmath.Point3d(p);
		}
		
		public javax.vecmath.Point3d getPoint3d() {
			return p;
		}
		
		public void setPoint3d(javax.vecmath.Point3d p) {
			this.p.set(p);
			computeDerivatives();
		}
		
		public void setPoint3d(float x, float y, float z) {
			this.p.set(x, y, z);
			computeDerivatives();
		}
		
		public javax.vecmath.Point3d getDpIn() {
			return dpIn;
		}
		
		public void setDpIn(javax.vecmath.Tuple3d t) {
			this.dpIn.set(t);
		}
		
		public void setDpIn(double x, double y, double z) {
			this.dpIn.set(x, y, z);
		}
		
		public javax.vecmath.Point3d getDpOut() {
			return dpOut;
		}
		
		public void setDpOut(javax.vecmath.Tuple3d t) {
			this.dpOut.set(t);
		}
		
		public void setDpOut(double x, double y, double z) {
			this.dpOut.set(x, y, z);
		}
		
		/*
		 * ProxyAccessible implementation
		 */
		public float proxyGet(Axis axis) {
			return proxyGet(axis, p);
		}
		
		public float proxyGetDin(Axis axis) {
			return proxyGet(axis, dpIn);
		}
		
		public float proxyGetDout(Axis axis) {
			return proxyGet(axis, dpOut);
		}
		
		public void proxySet(Axis axis, float value) {
			proxySet(axis, value, p);
		}
		
		public void proxySetDin(Axis axis, float value) {
			proxySet(axis, value, dpIn);
		}
		
		public void proxySetDout(Axis axis, float value) {
			proxySet(axis, value, dpOut);
		}
		/*
		 * End of ProxyAccessible implementation
		 */
		
		/**
		 * A Helper method for ProxyAccessible.
		 * @return The selected axis of the passed javax.vecmath.Point3d object.
		 */
		private float proxyGet(Axis axis, javax.vecmath.Point3d point) {
			switch(axis) {
			case X:
				return (float) point.x;
			case Y:
				return (float) point.y;
			case Z:
				return (float) point.z;
			}
			throw new IllegalArgumentException("axis=" + axis);
		}
		
		/**
		 * A Helper method for ProxyAccessible.
		 * It sets the selected axis of the passed javax.vecmath.Point3d object to the passed float value.
		 */
		private void proxySet(Axis axis, float value, javax.vecmath.Point3d point) {
			switch(axis) {
			case X:
				point.x = value;
				break;
			case Y:
				point.y = value;
				break;
			case Z:
				point.z = value;
				break;
			default:
				throw new IllegalArgumentException("axis=" + axis);
			}
			computeDerivatives();
		}
		
		@Override
		public void xml(PrintStream out) {
			out.append("<key frame=\"").append(java.lang.Float.toString(position)).append("\"");
			out.append(toXml("", p));
			out.append(" interpolation=\"").append(interpolation.toString().toLowerCase()).append("\"");
			if (interpolation == Interpolation.DISCRETE) {
				out.append(" tangentMode=\"").append(tangentMode.toString().toLowerCase()).append("\"");
				if (tangentMode == TangentMode.MANUAL) {
					if (smooth) {
						out.append(" delta=\"").append(toXml("d", dpIn)).append("\"");
					} else {
						out.append(" deltaIn=\"").append(toXml("dIn", dpIn)).append("\"");
						out.append(" deltaOut=\"").append(toXml("dOut", dpOut)).append("\"");
					}
				}
			}
			out.append("/>");
			out.println();
		}
		
		@Override
		public MotionKey.Point3d copy() {
			MotionKey.Point3d copy = new MotionKey.Point3d(position, p);
			copy.interpolation = interpolation;
			copy.tangentMode = tangentMode;
			copy.smooth = smooth;
			copy.dpIn = dpIn;
			copy.dpOut = dpOut;
			return copy;
		}
		
		@Override
		public String toString() {
			return super.toString() + " " + p;
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
	
	public static class Color3f extends MotionKey implements ProxyAccessible {
		private javax.vecmath.Color3f c;
		private javax.vecmath.Color3f dcIn = new javax.vecmath.Color3f();
		private javax.vecmath.Color3f dcOut = new javax.vecmath.Color3f();
		
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
		
		public javax.vecmath.Color3f getDcIn() {
			return dcIn;
		}
		
		public void setDcIn(javax.vecmath.Color3f dcIn) {
			this.dcIn.set(dcIn);
		}
		
		public void setDcIn(float r, float g, float b) {
			this.dcIn.set(r, g, b);
		}
		
		public javax.vecmath.Color3f getDcOut() {
			return dcOut;
		}
		
		public void setDcOut(javax.vecmath.Color3f dcOut) {
			this.dcOut.set(dcOut);
		}
		
		public void setDcOut(float r, float g, float b) {
			this.dcOut.set(r, g, b);
		}
		
		/*
		 * ProxyAccessible implementation
		 */
		public float proxyGet(Axis axis) {
			return proxyGet(axis, c);
		}
		
		public float proxyGetDin(Axis axis) {
			return proxyGet(axis, dcIn) - proxyGet(axis, c);
		}
		
		public float proxyGetDout(Axis axis) {
			return proxyGet(axis, dcOut);
		}
		
		public void proxySet(Axis axis, float value) {
			proxySet(axis, value, c);
		}
		
		public void proxySetDin(Axis axis, float value) {
			proxySet(axis, proxyGet(axis, c) - value, dcIn);
		}
		
		public void proxySetDout(Axis axis, float value) {
			proxySet(axis, proxyGet(axis, c) + value, dcOut);
		}
		/*
		 * End of ProxyAccessible implementation
		 */
		
		/**
		 * A Helper method for ProxyAccessible.
		 * @return The selected axis of the passed javax.vecmath.Color3f object.
		 */
		private float proxyGet(Axis axis, javax.vecmath.Color3f color) {
			switch(axis) {
			case X:
				return (float) color.x;
			case Y:
				return (float) color.y;
			case Z:
				return (float) color.z;
			}
			throw new IllegalArgumentException("axis=" + axis);
		}
		
		/**
		 * A Helper method for ProxyAccessible.
		 * It sets the selected axis of the passed javax.vecmath.Color3f object to the passed float value.
		 */
		private void proxySet(Axis axis, float value, javax.vecmath.Color3f color) {
			switch(axis) {
			case X:
				color.x = value;
				break;
			case Y:
				color.y = value;
				break;
			case Z:
				color.z = value;
				break;
			default:
				throw new IllegalArgumentException("axis=" + axis);
			}
			computeDerivatives();
		}
		
		@Override
		public void xml(PrintStream out) {
			out.append("<key frame=\"").append(java.lang.Float.toString(position)).append("\"");
			out.append(toXml("", c));
			out.append(" interpolation=\"").append(interpolation.toString().toLowerCase()).append("\"");
			if (interpolation == Interpolation.DISCRETE) {
				out.append(" tangentMode=\"").append(tangentMode.toString().toLowerCase()).append("\"");
				if (tangentMode == TangentMode.MANUAL) {
					if (smooth) {
						out.append(" delta=\"").append(toXml("d", dcIn)).append("\"");
					} else {
						out.append(" deltaIn=\"").append(toXml("dIn", dcIn)).append("\"");
						out.append(" deltaOut=\"").append(toXml("dOut", dcOut)).append("\"");
					}
				}
			}
			out.append("/>");
			out.println();
		}
		
		@Override
		public MotionKey.Color3f copy() {
			MotionKey.Color3f copy = new MotionKey.Color3f(position, c);
			copy.interpolation = interpolation;
			copy.tangentMode = tangentMode;
			copy.smooth = smooth;
			copy.dcIn = dcIn;
			copy.dcOut = dcOut;
			return copy;
		}
		
		@Override
		public String toString() {
			return super.toString() + " " + c;
		}
	}
	
	public static class Quat4f extends MotionKey implements ProxyAccessible {
		private javax.vecmath.Quat4f q;
		private javax.vecmath.Quat4f dqIn = new javax.vecmath.Quat4f();
		private javax.vecmath.Quat4f dqOut = new javax.vecmath.Quat4f();
		
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
		
		public javax.vecmath.Quat4f getDqIn() {
			return dqIn;
		}
		
		public void setDqIn(float x, float y, float z, float w) {
			this.dqIn.set(x, y, z, w);
		}
		
		public void setDqIn(javax.vecmath.Quat4f dqIn) {
			this.dqIn.set(dqIn);
		}
		
		public javax.vecmath.Quat4f getDqOut() {
			return dqOut;
		}
		
		public void setDqOut(float x, float y, float z, float w) {
			this.dqOut.set(x, y, z, w);
		}
		
		public void setDqOut(javax.vecmath.Quat4f dqOut) {
			this.dqOut.set(dqOut);		
		}
		
		/*
		 * ProxyAccessible implementation
		 */
		public float proxyGet(Axis axis) {
			return proxyGet(axis, q);
		}
		
		public float proxyGetDin(Axis axis) {
			return proxyGet(axis, dqIn);
		}
		
		public float proxyGetDout(Axis axis) {
			return proxyGet(axis, dqOut);
		}
		
		public void proxySet(Axis axis, float value) {
			proxySet(axis, value, q);
		}
		
		public void proxySetDin(Axis axis, float value) {
			proxySet(axis, value, dqIn);
		}
		
		public void proxySetDout(Axis axis, float value) {
			proxySet(axis, value, dqOut);
		}
		/*
		 * End of ProxyAccessible implementation
		 */
		
		/**
		 * A Helper method for ProxyAccessible.
		 * @return The selected axis of the passed javax.vecmath.Quat4f object.
		 */
		private float proxyGet(Axis axis, javax.vecmath.Quat4f quat) {
			switch(axis) {
			case X:
				return (float) quat.x;
			case Y:
				return (float) quat.y;
			case Z:
				return (float) quat.z;
			case W:
				return (float) quat.w;
			}
			throw new IllegalArgumentException("axis=" + axis);
		}
		
		/**
		 * A Helper method for ProxyAccessible.
		 * It sets the selected axis of the passed javax.vecmath.Quat4f object to the passed float value.
		 */
		private void proxySet(Axis axis, float value, javax.vecmath.Quat4f quat) {
			switch(axis) {
			case X:
				quat.x = value;
				break;
			case Y:
				quat.y = value;
				break;
			case Z:
				quat.z = value;
				break;
			case W:
				quat.w = value;
				break;
			default:
				throw new IllegalArgumentException("axis=" + axis);
			}
			computeDerivatives();
		}
		
		@Override
		public void xml(PrintStream out) {
			out.append("<key frame=\"").append(java.lang.Float.toString(position)).append("\"");
			out.append(toXml("", q));
			out.append(" interpolation=\"").append(interpolation.toString().toLowerCase()).append("\"");
			if (interpolation == Interpolation.DISCRETE) {
				out.append(" tangentMode=\"").append(tangentMode.toString().toLowerCase()).append("\"");
				if (tangentMode == TangentMode.MANUAL) {
					if (smooth) {
						out.append(" delta=\"").append(toXml("d", dqIn)).append("\"");
					} else {
						out.append(" deltaIn=\"").append(toXml("dIn", dqIn)).append("\"");
						out.append(" deltaOut=\"").append(toXml("dOut", dqOut)).append("\"");
					}
				}
			}
			out.append("/>");
			out.println();
		}
		
		@Override
		public MotionKey.Quat4f copy() {
			MotionKey.Quat4f copy = new MotionKey.Quat4f(position, q);
			copy.interpolation = interpolation;
			copy.tangentMode = tangentMode;
			copy.smooth = smooth;
			copy.dqIn = dqIn;
			copy.dqOut = dqOut;
			return copy;
		}
		
		@Override
		public String toString() {
			return super.toString() + " " + q;
		}
	}
	
	public static class Object extends MotionKey {
		private java.lang.Object o;
		
		public Object(float position, java.lang.Object o) {
			super(position);
			this.o = o;
		}
		
		public java.lang.Object getObject() {
			return o;
		}
		
		public void setObject(java.lang.Object o) {
			this.o = o;
		}
		
		@Override
		public Interpolation getInterpolation() {
			return Interpolation.DISCRETE;
		}

		@Override
		public void setInterpolation(Interpolation interpolation) {
			// do nothing
		}
		
		@Override
		public void xml(PrintStream out) {
			if (o == null)
				out.println("<key frame=\"" + position + "\" null=\"null\"/>");
			else if (o instanceof ControlPoint)
				out.println("<key frame=\"" + position + "\" cp=\"" + ((ControlPoint) o).getId() + "\"/>");
			else if (o instanceof Bone.BoneTransformable)
				out.println("<key frame=\"" + position + "\" bone=\"" + ((Bone.BoneTransformable) o).getBone().getName() + "\"/>");
			else
				throw new IllegalStateException("Object key of unknown type " + o);
		}
		
		@Override
		public MotionKey.Object copy() {
			return new MotionKey.Object(position, o);
		}
		
		@Override
		public String toString() {
			return super.toString() + " (" + o + ")";
		}
	}
	
	public static interface ProxyAccessible {
		float proxyGet(Axis axis);
		float proxyGetDin(Axis axis);
		float proxyGetDout(Axis axis);
		void proxySet(Axis axis, float value);
		void proxySetDin(Axis axis, float value);
		void proxySetDout(Axis axis, float value);
		
		int getIndex();
		float getPosition();
		void setPosition(float position);
		Interpolation getInterpolation();
		void setInterpolation(Interpolation interpolation);
		boolean isSmooth();
		void setSmooth(boolean smooth);
		TangentMode getTangentMode();
		void setTangentMode(TangentMode tangentMode);
		void computeDerivatives();
		MotionKey copy();
	}
	
	private static String toXml(String prefix, javax.vecmath.Point3d p) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ").append(prefix).append("x=\"").append(java.lang.Double.toString(p.x)).append("\"");
		sb.append(" ").append(prefix).append("y=\"").append(java.lang.Double.toString(p.y)).append("\"");
		sb.append(" ").append(prefix).append("z=\"").append(java.lang.Double.toString(p.z)).append("\"");
		return sb.toString();
	}
	
	private static String toXml(String prefix, javax.vecmath.Quat4f q) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ").append(prefix).append("x=\"").append(java.lang.Double.toString(q.x)).append("\"");
		sb.append(" ").append(prefix).append("y=\"").append(java.lang.Double.toString(q.y)).append("\"");
		sb.append(" ").append(prefix).append("z=\"").append(java.lang.Double.toString(q.z)).append("\"");
		sb.append(" ").append(prefix).append("w=\"").append(java.lang.Double.toString(q.z)).append("\"");
		return sb.toString();
	}
	
	private static String toXml(String prefix, javax.vecmath.Color3f c) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ").append(prefix).append("r=\"").append(java.lang.Double.toString(c.x)).append("\"");
		sb.append(" ").append(prefix).append("g=\"").append(java.lang.Double.toString(c.y)).append("\"");
		sb.append(" ").append(prefix).append("b=\"").append(java.lang.Double.toString(c.z)).append("\"");
		return sb.toString();
	}
}
