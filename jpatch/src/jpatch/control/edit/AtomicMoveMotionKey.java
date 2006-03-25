package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicMoveMotionKey extends JPatchAtomicEdit {
	private MotionCurve motionCurve;
	private MotionKey motionKey;
	private float position;
	
	public AtomicMoveMotionKey(MotionCurve motionCurve, MotionKey motionKey, float position) {
		this.motionCurve = motionCurve;
		this.motionKey = motionKey;
		this.position = position;
		if (motionCurve.getKeyAt(motionKey.getPosition()) != motionKey)
			throw new IllegalArgumentException("MotionKey " + motionKey + " is not on curve " + motionCurve);
		swap();
	}
	
	public void undo() {
		swap();
	}

	public void redo() {
		swap();
	}
	
	private void swap() {
		System.out.println("moving key " + motionKey + " from " + motionKey.getPosition() + " tp " + position);
		float dummy = motionKey.getPosition();
		motionCurve.moveKey(motionKey, position);
		position = dummy;
	}
	
	public int sizeOf() {
		return 8 + 4 + 4 + 4;
	}
}
