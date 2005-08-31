package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.*;

/**
 * Use this class for changing selections (with the default tool)
 * @see AddControlPointsToSelection
 */

public class AddControlPointsToSelectionEdit extends AddOrRemoveControlPointsSelectionEdit {

	public AddControlPointsToSelectionEdit(NewSelection selection, Map objects) {
		super(selection ,objects);
		add();
	}
        
	public void undo() {
		remove();
	}
	
	public void redo() {
		add();
	}
	
	public void dump(String prefix) {
		System.out.println(prefix + getClass().getName() + " \"" + name() + "\" (" + selection + " " + mapCPs + ")");
	}
}
