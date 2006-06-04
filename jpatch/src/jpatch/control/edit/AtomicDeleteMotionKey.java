package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicDeleteMotionKey extends JPatchAtomicEdit implements JPatchRootEdit {
	private MotionCurve motionCurve;
	private MotionKey motionKey;
	
	public AtomicDeleteMotionKey(MotionKey motionKey) {
		this.motionCurve = motionKey.getMotionCurve();
		this.motionKey = motionKey;
		/*
		 * check if the key really is on the curve, throw exception if it isn't.
		 */
		if (motionCurve.getKeyAt(motionKey.getPosition()) != motionKey)
			throw new IllegalArgumentException("MotionKey " + motionKey + " is not on curve " + motionCurve);
		redo();		// remove the key
	}
	
	public String getName() {
		return "delete key";
	}
	
	public void undo() {
		if (motionKey != null)							// only remove if we have a valid key
			motionCurve.addKey(motionKey);
	}

	public void redo() {
		motionKey = motionCurve.removeKey(motionKey);	// if the key can't be removed (because it's the only
														// one on the curve) this method returns null (to
														// prevent a subsequent undo() call to add a key that
														// hasn't been removed).
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
