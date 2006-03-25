package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicAddMotionKey extends JPatchAtomicEdit implements JPatchRootEdit {
	private MotionCurve motionCurve;
	private MotionKey motionKey;
	
	public String getName() {
		return "insert key";
	}
	
	public AtomicAddMotionKey(MotionCurve motionCurve, MotionKey motionKey) {
		this.motionCurve = motionCurve;
		this.motionKey = motionKey;
		if (motionCurve.hasKeyAt(motionKey.getPosition()))
			throw new IllegalArgumentException("MotionCurve " + motionCurve + " already has key at " + motionKey.getPosition());
		redo();
	}
	
	public void undo() {
		motionCurve.removeKey(motionKey);
	}

	public void redo() {
		motionCurve.addKey(motionKey);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
