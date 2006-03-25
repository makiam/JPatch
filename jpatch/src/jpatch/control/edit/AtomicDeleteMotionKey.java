package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicDeleteMotionKey extends JPatchAtomicEdit implements JPatchRootEdit {
	private MotionCurve motionCurve;
	private MotionKey motionKey;
	
	public AtomicDeleteMotionKey(MotionCurve motionCurve, MotionKey motionKey) {
		this.motionCurve = motionCurve;
		this.motionKey = motionKey;
		if (motionCurve.getKeyAt(motionKey.getPosition()) != motionKey)
			throw new IllegalArgumentException("MotionKey " + motionKey + " is not on curve " + motionCurve);
		redo();
	}
	
	public String getName() {
		return "delete key";
	}
	
	public void undo() {
		motionCurve.addKey(motionKey);
	}

	public void redo() {
		motionCurve.removeKey(motionKey);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
