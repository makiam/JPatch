package jpatch.control.edit;

import jpatch.entity.*;
import javax.vecmath.*;

public abstract class AtomicChangeControlPoint extends JPatchAtomicEdit {
	ControlPoint cp;
	
	private AtomicChangeControlPoint() { }
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	abstract void swap();
	
//	public static final class Next extends AtomicChangeControlPoint {
//		private ControlPoint cpNext;
//		
//		public Next(ControlPoint cp, ControlPoint next) {
//			super(cp);
//			cpNext = next;
//			swap();
//		}
//		
//		void swap() {
//			ControlPoint dummy = cp.getNext();
//			cp.setNext(cpNext);
//			cpNext = dummy;
//		}
//	}
//	
//	public static final class Prev extends AtomicChangeControlPoint {
//		private ControlPoint cpPrev;
//		
//		public Prev(ControlPoint cp, ControlPoint prev) {
//			super(cp);
//			cpPrev = prev;
//			swap();
//		}
//		
//		void swap() {
//			ControlPoint dummy = cp.getPrev();
//			cp.setPrev(cpPrev);
//			cpPrev = dummy;
//		}
//	}
	

	public static final class ParentHook extends AtomicChangeControlPoint {
		private ControlPoint cpParentHook;
		
		public ParentHook(ControlPoint cp, ControlPoint parentHook) {
			this.cp = cp;
			cpParentHook = parentHook;
			swap();
		}
		
		void swap() {
			ControlPoint dummy = cp.getParentHook();
			cp.setParentHook(cpParentHook);
			cpParentHook = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class ChildHook extends AtomicChangeControlPoint {
		private ControlPoint cpChildHook;
		
		public ChildHook(ControlPoint cp, ControlPoint childHook) {
			this.cp = cp;
			cpChildHook = childHook;
			swap();
		}
		
		void swap() {
			ControlPoint dummy = cp.getChildHook();
			cp.setChildHook(cpChildHook);
			cpChildHook = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class Loop extends AtomicChangeControlPoint {
		public Loop(ControlPoint cp) {
			this.cp = cp;
			swap();
		}
		
		void swap() {
			cp.setLoop(!cp.getLoop());
		}
		
		public int sizeOf() {
			return 8 + 4;
		}
	}
	
	public static final class Curve extends AtomicChangeControlPoint {
		private jpatch.entity.Curve curve;
		
		public Curve(ControlPoint cp, jpatch.entity.Curve curve) {
			this.cp = cp;
			this.curve = curve;
			swap();
		}
		
		void swap() {
			jpatch.entity.Curve dummy = cp.getCurve();
			cp.setCurve(curve);
			curve = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class HookPos extends AtomicChangeControlPoint {
		private float fHookPos;
		
		public HookPos(ControlPoint cp, float hookPos) {
			this.cp = cp;
			fHookPos = hookPos;
			swap();
		}
		
		void swap() {
			float dummy = cp.getHookPos();
			cp.setHookPos(fHookPos);
			fHookPos = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class Magnitude extends AtomicChangeControlPoint implements JPatchRootEdit {
		private float fMagnitude;
		
		public Magnitude(ControlPoint cp, float magnitude) {
			this.cp = cp;
			fMagnitude = magnitude;
			swap();
		}
		
		void swap() {
			float dummy = cp.getInMagnitude();
			cp.setMagnitude(fMagnitude);
			fMagnitude = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
		
		public String getName() {
			return "change tangent mgnitude";
		}
	}
	
	public static final class TangentMode extends AtomicChangeControlPoint {
		private int iTangentMode;
		
		public TangentMode(ControlPoint cp, int tangentMode) {
			this.cp = cp;
			iTangentMode = tangentMode;
			swap();
		}
		
		void swap() {
			int dummy = cp.getMode();
			cp.setMode(iTangentMode);
			iTangentMode = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class Position extends AtomicChangeControlPoint {
		private Point3f p3Position = new Point3f();
		
		public Position(ControlPoint cp, Point3f position) {
			this.cp = cp;
			p3Position.set(position);
//			swap();
		}
		
		void swap() {
			Point3f dummy = new Point3f(cp.getPosition());
			cp.setPosition(p3Position);
			p3Position.set(dummy);
		}
		
		public int sizeOf() {
			return 8 + 4 + (8 + 4 + 4 + 4);
		}
	}
}