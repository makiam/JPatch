package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicChangeMotionKeyValue extends JPatchAtomicEdit {
	private MotionKey.Float motionKey;
	private float value;
	
	public AtomicChangeMotionKeyValue(MotionKey.Float motionKey, float value) {
		this.motionKey = motionKey;
		this.value = value;
		swap();
	}
	
	public void undo() {
		swap();
	}

	public void redo() {
		swap();
	}
	
	private void swap() {
		float dummy = motionKey.getFloat();
		motionKey.setFloat(value);
		value = dummy;
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
