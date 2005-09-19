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
	
	public static final class NextAttached extends AtomicChangeControlPoint {
		private ControlPoint cpNextAttached;
		
		public NextAttached(ControlPoint cp, ControlPoint nextAttached) {
			this.cp = cp;
			cpNextAttached = nextAttached;
			swap();
		}
		
		void swap() {
			ControlPoint dummy = cp.getNextAttached();
			cp.setNextAttached(cpNextAttached);
			cpNextAttached = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class PrevAttached extends AtomicChangeControlPoint {
		private ControlPoint cpPrevAttached;
		
		public PrevAttached(ControlPoint cp, ControlPoint prevAttached) {
			this.cp = cp;
			cpPrevAttached = prevAttached;
			swap();
		}
		
		void swap() {
			ControlPoint dummy = cp.getPrevAttached();
			cp.setPrevAttached(cpPrevAttached);
			cpPrevAttached = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	

	public static final class ParentHook extends AtomicChangeControlPoint {
		private ControlPoint cpParentHook;
		
		public ParentHook(ControlPoint cp, ControlPoint parentHook) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + parentHook + ")");
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
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + childHook + ")");
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
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ")");
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
	
	public static final class Deleted extends AtomicChangeControlPoint {	
		public Deleted(ControlPoint cp) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ")");
			this.cp = cp;
			swap();
		}
		
		void swap() {
			cp.setDeleted(!cp.isDeleted());
		}
		
		public int sizeOf() {
			return 8 + 4;
		}
	}
	
	public static final class HookPos extends AtomicChangeControlPoint {
		private float fHookPos;
		
		public HookPos(ControlPoint cp, float hookPos) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + hookPos + ")");
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
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + magnitude + ")");
			this.cp = cp;
			fMagnitude = magnitude;
//			swap();
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
	
	public static final class TangentMode extends AtomicChangeControlPoint implements JPatchRootEdit {
		private int iTangentMode;
		
		public TangentMode(ControlPoint cp, int tangentMode) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + tangentMode + ")");
			this.cp = cp;
			iTangentMode = tangentMode;
			swap();
		}
		
		void swap() {
			int dummy = cp.getMode();
			cp.setMode(iTangentMode);
			iTangentMode = dummy;
			cp.invalidateTangents();
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
		
		public String getName() {
			switch (iTangentMode) {
				case ControlPoint.PEAK: return "peak tangents";
				default: return "round tangents";
			}
		}
	}
	
	public static final class Position extends AtomicChangeControlPoint {
		private float x, y, z;
		
		public Position(ControlPoint cp, Point3f position) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + position + ")");
			this.cp = cp;
			x = position.x;
			y = position.y;
			z = position.z;
//			swap();
		}
		
		void swap() {
			Point3f p = cp.getPosition();
			float dummyX = p.x;
			float dummyY = p.y;
			float dummyZ = p.z;
			cp.setPosition(x, y, z);
			x = dummyX;
			y = dummyY;
			z = dummyZ;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 4;
		}
	}
}