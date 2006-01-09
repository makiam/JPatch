package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

public class AtomicChangeAnimObjectScale extends JPatchAtomicEdit {
	private AnimObject animObject;
	private float scale;
	
	public AtomicChangeAnimObjectScale(AnimObject animObject, float scale) {
		this.animObject = animObject;
		this.scale = scale;
		swap();
	}
	
	public void undo() {
		swap();
	}

	public void redo() {
		swap();
	}

	public int sizeOf() {
		return 8 + 4 + 4;
	}
	
	private void swap() {
		float dummy = scale;
		scale = animObject.getScale();
		animObject.setScale(dummy);
	}
}
