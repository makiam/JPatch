package jpatch.control.edit;

import jpatch.entity.*;
import javax.vecmath.*;

public abstract class AtomicModifyMotionCurve extends JPatchAtomicEdit {
	boolean bNewKey;
	
	public void undo() {
		System.out.println(getClass().getName() + " undo()");
		swap();
		if (bNewKey)
			removeKey();
	}

	public void redo() {
		System.out.println(getClass().getName() + " redo()");
		swap();
	}

	abstract void swap();
	
	abstract void removeKey();
	
	public static class Color3f extends AtomicModifyMotionCurve {
		private MotionCurve.Color3f motionCurve;
		private javax.vecmath.Color3f color;
		private float position;
		
		public Color3f(MotionCurve.Color3f motionCurve, float position, javax.vecmath.Color3f color) {
			this.motionCurve = motionCurve;
			this.position = position;
			this.color = color;
			bNewKey = !motionCurve.hasKeyAt(position);
			swap();
		}
		
		void swap() {
			javax.vecmath.Color3f dummy = new javax.vecmath.Color3f(motionCurve.getColor3fAt(position));
			motionCurve.setColor3fAt(position, color);
			color = dummy;
		}

		void removeKey() {
			motionCurve.removeKey(motionCurve.getKeyAt(position));
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 8 + 4 + 4 + 4;
		}
	}
	
	public static class Float extends AtomicModifyMotionCurve {
		private MotionCurve.Float motionCurve;
		private float value;
		private float position;
		
		public Float(MotionCurve.Float motionCurve, float position, float newValue) {
			this.motionCurve = motionCurve;
			this.position = position;
			this.value = newValue;
			bNewKey = !motionCurve.hasKeyAt(position);
			swap();
		}
		
		void swap() {
			System.out.println(hashCode() + " swap " + motionCurve.getFloatAt(position) + " " + value);
			float dummy = motionCurve.getFloatAt(position);
			motionCurve.setFloatAt(position, value);
			value = dummy;
		}

		void removeKey() {
			motionCurve.removeKey(motionCurve.getKeyAt(position));
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4;
		}
	}
	
	public static class Point3d extends AtomicModifyMotionCurve {
		private MotionCurve.Point3d motionCurve;
		private javax.vecmath.Point3d point;
		private float position;
		
		public Point3d(MotionCurve.Point3d motionCurve, float position, javax.vecmath.Point3d point) {
			this.motionCurve = motionCurve;
			this.position = position;
			this.point = point;
			bNewKey = !motionCurve.hasKeyAt(position);
			swap();
		}
		
		void swap() {
//			System.out.println("swap " + motionCurve.getPoint3dAt(position) + " " + point);
			javax.vecmath.Point3d dummy = new javax.vecmath.Point3d(motionCurve.getPoint3dAt(position));
			motionCurve.setPoint3dAt(position, point);
			point = dummy;
		}

		void removeKey() {
			motionCurve.removeKey(motionCurve.getKeyAt(position));
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 8 + 4 + 4 + 4;
		}
	}
	
	public static class Quat4f extends AtomicModifyMotionCurve {
		private MotionCurve.Quat4f motionCurve;
		private javax.vecmath.Quat4f quat;
		private float position;
		
		public Quat4f(MotionCurve.Quat4f motionCurve, float position, javax.vecmath.Quat4f quat) {
			this.motionCurve = motionCurve;
			this.position = position;
			this.quat = quat;
			bNewKey = !motionCurve.hasKeyAt(position);
			swap();
		}
		
		void swap() {
//			System.out.println("swap " + motionCurve.getQuat4fAt(position) + " " + quat);
			javax.vecmath.Quat4f dummy = new javax.vecmath.Quat4f(motionCurve.getQuat4fAt(position));
			motionCurve.setQuat4fAt(position, quat);
			quat = dummy;
		}

		void removeKey() {
			motionCurve.removeKey(motionCurve.getKeyAt(position));
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 8 + 4 + 4 + 4 + 4;
		}
	}
}
