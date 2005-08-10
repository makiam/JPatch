package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.selection.*;

/**
 * Use this class for changing selections (with the default tool)
 * @see AddControlPointsToSelection
 */

public class RemoveControlPointsFromSelectionEdit extends AddOrRemoveControlPointsSelectionEdit {

	public RemoveControlPointsFromSelectionEdit(PointSelection ps, Collection controlPoints) {
		super(ps,controlPoints);
		remove();
	}
        
	public void undo() {
		add();
	}
	
	public void redo() {
		remove();
	}
}
