package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.selection.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public abstract class AddOrRemoveControlPointsSelectionEdit extends JPatchAbstractUndoableEdit {
	protected PointSelection ps;
	protected Collection colCPs;

	public AddOrRemoveControlPointsSelectionEdit(PointSelection ps, Collection controlPoints) {
		this.ps = ps;
		colCPs = controlPoints;
	}

	public String name() {
		return "change selection";
	}

	protected void remove() {
		ps.getSelectedControlPoints().removeAll(colCPs);
		ps.resetPivotToCenter();
	}
	 
	protected void add() {
		ps.getSelectedControlPoints().addAll(colCPs);
		ps.resetPivotToCenter();
	}
 }
