package jpatch.control.edit;

import jpatch.entity.*;
import javax.vecmath.*;

public abstract class AtomicModifyMotionCurve extends JPatchAtomicEdit {

	public void undo() {
		swap();
	}

	public void redo() {
		swap();
	}

	abstract void swap();
	
	public static class Color3f extends AtomicModifyMotionCurve {
		private MotionCurve2.Color3f motionCurve;
		private javax.vecmath.Color3f color;
		private float position;
		
		public Color3f(MotionCurve2.Color3f motionCurve, float position, javax.vecmath.Color3f color) {
			this.motionCurve = motionCurve;
			this.color = color;
			swap();
		}
		
		void swap() {
			javax.vecmath.Color3f dummy = new javax.vecmath.Color3f(motionCurve.getColor3fAt(position));
			motionCurve.setColor3fAt(position, color);
			color = dummy;
		}

		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 8 + 4 + 4 + 4;
		}
	}
	
	public static class Float extends AtomicModifyMotionCurve {
		private MotionCurve2.Float motionCurve;
		private float value;
		private float position;
		
		public Float(MotionCurve2.Float motionCurve, float position, float value) {
			this.motionCurve = motionCurve;
			this.value = value;
			swap();
		}
		
		void swap() {
			float dummy = motionCurve.getFloatAt(position);
			motionCurve.setFloatAt(position, value);
			value = dummy;
		}

		public int sizeOf() {
			return 8 + 4 + 4 + 4;
		}
	}
	
	public static class Point3d extends AtomicModifyMotionCurve {
		private MotionCurve2.Point3d motionCurve;
		private javax.vecmath.Point3d point;
		private float position;
		
		public Point3d(MotionCurve2.Point3d motionCurve, float position, javax.vecmath.Point3d point) {
			this.motionCurve = motionCurve;
			this.point = point;
			swap();
		}
		
		void swap() {
//			System.out.println("swap " + motionCurve.getPoint3dAt(position) + " " + point);
			javax.vecmath.Point3d dummy = new javax.vecmath.Point3d(motionCurve.getPoint3dAt(position));
			motionCurve.setPoint3dAt(position, point);
			point = dummy;
		}

		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 8 + 4 + 4 + 4;
		}
	}
	
	public static class Quat4f extends AtomicModifyMotionCurve {
		private MotionCurve2.Quat4f motionCurve;
		private javax.vecmath.Quat4f quat;
		private float position;
		
		public Quat4f(MotionCurve2.Quat4f motionCurve, float position, javax.vecmath.Quat4f quat) {
			this.motionCurve = motionCurve;
			this.quat = quat;
			swap();
		}
		
		void swap() {
//			System.out.println("swap " + motionCurve.getQuat4fAt(position) + " " + quat);
			javax.vecmath.Quat4f dummy = new javax.vecmath.Quat4f(motionCurve.getQuat4fAt(position));
			motionCurve.setQuat4fAt(position, quat);
			quat = dummy;
		}

		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 8 + 4 + 4 + 4 + 4;
		}
	}
}
