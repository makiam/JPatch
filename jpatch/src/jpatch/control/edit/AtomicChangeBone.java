package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public abstract class AtomicChangeBone extends JPatchAtomicEdit {
	Bone bone;
	
	private AtomicChangeBone() { }
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	abstract void swap();
	
	public static final class Point extends AtomicChangeBone {
		private Point3f p3New = new Point3f();
		private Point3f p3Point;
		
		public Point(Bone bone, Point3f newValue, Point3f point) {
			this.bone = bone;
			p3New.set(newValue);
			p3Point = point;
		}
		
		void swap() {
			Point3f dummy = new Point3f(p3Point);
			p3Point.set(p3New);
			p3New.set(dummy);
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4;
		}
	}
	
	public static final class Parent extends AtomicChangeBone {
		private Bone parentBone;
		
		public Parent(Bone bone, Bone parent) {
			this.bone = bone;
			this.parentBone = parent;
			swap();
		}
		
		void swap() {
			Bone dummy = parentBone;
			parentBone = bone.getParentBone();
			bone.setParentBone(dummy);
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
}
