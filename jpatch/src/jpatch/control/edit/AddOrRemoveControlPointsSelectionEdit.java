package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.*;

/**
 * Use this class for changing selections (with the default tool)
 */

public abstract class AddOrRemoveControlPointsSelectionEdit extends JPatchAbstractUndoableEdit {
	protected NewSelection selection;
	protected Map mapCPs;

	public AddOrRemoveControlPointsSelectionEdit(NewSelection selection, Map controlPoints) {
		this.selection = selection;
		mapCPs = controlPoints;
	}

	public String name() {
		return "change selection";
	}

	protected void remove() {
		for (Iterator it = mapCPs.keySet().iterator(); it.hasNext(); selection.getMap().remove(it.next()));
		selection.getPivot().set(selection.getCenter());
	}
	 
	protected void add() {
		selection.getMap().putAll(mapCPs);
		selection.getPivot().set(selection.getCenter());
	}
 }
