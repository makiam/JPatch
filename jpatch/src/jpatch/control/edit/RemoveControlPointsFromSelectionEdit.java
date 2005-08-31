package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.*;

/**
 * Use this class for changing selections (with the default tool)
 * @see AddControlPointsToSelection
 */

public class RemoveControlPointsFromSelectionEdit extends AddOrRemoveControlPointsSelectionEdit {

	public RemoveControlPointsFromSelectionEdit(NewSelection selection, Map objects) {
		super(selection, objects);
		remove();
	}
        
	public void undo() {
		add();
	}
	
	public void redo() {
		remove();
	}
	
	public void dump(String prefix) {
		System.out.println(prefix + getClass().getName() + " \"" + name() + "\" (" + selection + " " + mapCPs + ")");
	}
}
