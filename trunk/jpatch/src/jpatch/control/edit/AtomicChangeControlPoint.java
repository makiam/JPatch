package jpatch.control.edit;

import jpatch.entity.*;
import javax.vecmath.*;

public abstract class AtomicChangeControlPoint extends JPatchAtomicEdit {
	OLDControlPoint cp;
	
	private AtomicChangeControlPoint() { }
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	abstract void swap();
	
	public static final class NextAttached extends AtomicChangeControlPoint {
		private OLDControlPoint cpNextAttached;
		
		public NextAttached(OLDControlPoint cp, OLDControlPoint nextAttached) {
			this.cp = cp;
			cpNextAttached = nextAttached;
			swap();
		}
		
		void swap() {
			OLDControlPoint dummy = cp.getNextAttached();
			cp.setNextAttached(cpNextAttached);
			cpNextAttached = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class PrevAttached extends AtomicChangeControlPoint {
		private OLDControlPoint cpPrevAttached;
		
		public PrevAttached(OLDControlPoint cp, OLDControlPoint prevAttached) {
			this.cp = cp;
			cpPrevAttached = prevAttached;
			swap();
		}
		
		void swap() {
			OLDControlPoint dummy = cp.getPrevAttached();
			cp.setPrevAttached(cpPrevAttached);
			cpPrevAttached = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class Next extends AtomicChangeControlPoint {
		private OLDControlPoint cpNext;
		
		public Next(OLDControlPoint cp, OLDControlPoint next) {
			this.cp = cp;
			cpNext = next;
			swap();
		}
		
		void swap() {
			OLDControlPoint dummy = cp.getNext();
			cp.setNext(cpNext);
			cpNext = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class Prev extends AtomicChangeControlPoint {
		private OLDControlPoint cpPrev;
		
		public Prev(OLDControlPoint cp, OLDControlPoint prev) {
			this.cp = cp;
			cpPrev = prev;
			swap();
		}
		
		void swap() {
			OLDControlPoint dummy = cp.getPrev();
			cp.setPrev(cpPrev);
			cpPrev = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	

	public static final class ParentHook extends AtomicChangeControlPoint {
		private OLDControlPoint cpParentHook;
		
		public ParentHook(OLDControlPoint cp, OLDControlPoint parentHook) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + parentHook + ")");
			this.cp = cp;
			cpParentHook = parentHook;
			swap();
		}
		
		void swap() {
			OLDControlPoint dummy = cp.getParentHook();
			cp.setParentHook(cpParentHook);
			cpParentHook = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class ChildHook extends AtomicChangeControlPoint {
		private OLDControlPoint cpChildHook;
		
		public ChildHook(OLDControlPoint cp, OLDControlPoint childHook) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + childHook + ")");
			this.cp = cp;
			cpChildHook = childHook;
			swap();
		}
		
		void swap() {
			OLDControlPoint dummy = cp.getChildHook();
			cp.setChildHook(cpChildHook);
			cpChildHook = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
	
	public static final class Loop extends AtomicChangeControlPoint {	
		public Loop(OLDControlPoint cp) {
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
		public Deleted(OLDControlPoint cp) {
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
		
		public HookPos(OLDControlPoint cp, float hookPos) {
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
		
		public Magnitude(OLDControlPoint cp, float magnitude) {
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
		
		public TangentMode(OLDControlPoint cp, int tangentMode) {
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
//			cp.invalidateTangents();
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
		
		public String getName() {
			switch (iTangentMode) {
				case OLDControlPoint.PEAK: return "peak tangents";
				default: return "round tangents";
			}
		}
	}
	
	public static final class Position extends AtomicChangeControlPoint {
		private float x, y, z;
		
		public Position(OLDControlPoint cp, Point3f position) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + position + ")");
			this.cp = cp;
			x = position.x;
			y = position.y;
			z = position.z;
//			swap();
		}
		
		void swap() {
			Point3f p = cp.getReferencePosition();
			float dummyX = p.x;
			float dummyY = p.y;
			float dummyZ = p.z;
			cp.setReferencePosition(x, y, z);
			x = dummyX;
			y = dummyY;
			z = dummyZ;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 4;
		}
	}
	
	public static final class Bone extends AtomicChangeControlPoint {
		private jpatch.entity.OLDBone bone;
		
		public Bone(OLDControlPoint cp, jpatch.entity.OLDBone bone) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + cp + ", " + bone + ")");
			this.cp = cp;
			this.bone = bone;
			swap();
		}
		
		void swap() {
			jpatch.entity.OLDBone dummy = cp.getBone();
			cp.setBone(bone);
			bone = dummy;
		}
		
		public int sizeOf() {
			return 8 + 4 + 4;
		}
	}
}