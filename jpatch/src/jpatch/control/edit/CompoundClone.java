package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

public class CompoundClone extends AbstractClone implements JPatchRootEdit {
	
	private static int iSequenceNumber = 1;
	
	public CompoundClone(ControlPoint[] controlPointsToClone) {
		super(controlPointsToClone);
		buildCloneMap(true);
		cloneControlPoints();
		cloneHooks();
		cloneCurves();
		clonePatches();
		NewSelection selection = createNewSelection();
		if (selection.getMap().size() > 0) {
			selection.setName("*clone #" + iSequenceNumber++);
			addEdit(new AtomicAddSelection(selection));
			addEdit(new AtomicChangeSelection(selection));
		}
	}
	
	public String getName() {
		return "clone";
	}
}

