package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

public class AtomicChangeAnimObjectTransform extends JPatchAtomicEdit {
	private AnimObject animObject;
	private Matrix4d transform = new Matrix4d();
	
	public AtomicChangeAnimObjectTransform(AnimObject animObject, Matrix4d transform) {
		this.animObject = animObject;
		this.transform.set(transform);
	}
	
	public void undo() {
		swap();
	}

	public void redo() {
		swap();
	}

	public int sizeOf() {
		return 8 + 4 + 4 + 16 * 8;
	}
	
	private void swap() {
		Matrix4d dummy = new Matrix4d(transform);
		transform.set(animObject.getTransform());
		animObject.setTransform(dummy);
	}
}
