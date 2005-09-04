package jpatch.control.edit;

public abstract class JPatchAtomicEdit implements JPatchUndoableEdit {
	public final boolean isAtomic() {
		return true;
	}
}
