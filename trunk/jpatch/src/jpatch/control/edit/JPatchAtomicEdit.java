package jpatch.control.edit;

import jpatch.boundary.*;

public abstract class JPatchAtomicEdit implements JPatchUndoableEdit {
	
	public final boolean isAtomic() {
		return true;
	}
	
	public void debug(String prefix) {
		String name = this instanceof JPatchRootEdit ? getClass().getName() + " \"" + ((JPatchRootEdit) this).getName() + "\"" : getClass().getName();
		System.out.println(prefix + name + " " + sizeOf());
	}
	
	static final int selectionSize(NewSelection selection) {
		return selection == null ? 0 : (8 + 4 + 4 + 4 + 4 + 8 * selection.getMap().size() * 2);
	}
}
