package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public class ChangeSelectionHotEdit extends JPatchAbstractUndoableEdit {
	private Object hot;
	private NewSelection selection;
	
	public ChangeSelectionHotEdit(NewSelection selection, Object hot) {
		this.selection = selection;
		this.hot = hot;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		Object dummy = hot;
		hot = selection.getHotObject();
		selection.setHotObject(dummy);
	}
}
