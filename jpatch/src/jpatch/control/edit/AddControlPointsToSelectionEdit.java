package jpatch.control.edit;

import java.util.Collection;

import jpatch.boundary.selection.PointSelection;

/**
 * Use this class for changing selections (with the default tool)
 * @see AddControlPointsToSelection
 */

public class AddControlPointsToSelectionEdit extends AddOrRemoveControlPointsSelectionEdit {

	public AddControlPointsToSelectionEdit(PointSelection ps, Collection controlPoints) {
		super(ps,controlPoints);
		add();
	}
        
	public void undo() {
		remove();
	}
	
	public void redo() {
		add();
	}
}
