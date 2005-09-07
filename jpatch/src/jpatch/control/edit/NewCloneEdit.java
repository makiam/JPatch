package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.selection.*;

public class NewCloneEdit extends CloneCommonEdit {
	
	private static int iSequenceNumber = 1;
	
	public NewCloneEdit(ControlPoint[] controlPointsToClone) {
		super(controlPointsToClone);
		buildCloneMap(true);
		cloneControlPoints();
		cloneHooks();
		cloneCurves();
		clonePatches();
		PointSelection ps = createNewSelection();
		if (ps.getSize() > 0) {
			ps.setName("*clone #" + iSequenceNumber++);
			addEdit(new AtomicChangeSelection(ps));
			addEdit(new AddSelectionEdit(ps));
		}
	}
}

