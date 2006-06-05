package jpatch.control.edit;

import jpatch.entity.*;

public class CompoundChangeTangentMode extends JPatchCompoundEdit implements JPatchRootEdit {
	
	MotionKey key;
	MotionKey.TangentMode tangentMode;
	
	/**
	 * changes the tangent mode and preserves manually set delta values
	 * @param key The MotionKey to change
	 * @param tangentMode The new TangentMode
	 */
	public CompoundChangeTangentMode(MotionKey key, MotionKey.TangentMode tangentMode) {
		this.key = key;
		this.tangentMode = tangentMode;
		if (key instanceof MotionKey.Float) {
			MotionKey.Float fKey = (MotionKey.Float) key;
			addEdit(new AtomicChangeMotionKey.DfIn(key, fKey.getDfIn(), false));
			addEdit(new AtomicChangeMotionKey.DfOut(key, fKey.getDfOut(), false));
			addEdit(new AtomicChangeMotionKey.TangentMode(key, tangentMode, true));
		}
	}
	
	public String getName() {
		return "change tangent mode";
	}

}
