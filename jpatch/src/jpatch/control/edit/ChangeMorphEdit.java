package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public class ChangeMorphEdit extends JPatchAbstractUndoableEdit {
	private Morph morph;
	private List listPoints;
	private List listVectors;
	
	
	public ChangeMorphEdit(Morph morph) {
		this.morph = morph;
		listPoints = morph.getPointList();
		listVectors = morph.getVectorList();
		morph.set();
	}
	
	public void undo() {
		morph.unapply();
		swap();
		morph.apply();
	}
	
	public void redo() {
		morph.unapply();
		swap();
		morph.apply();
	}
	
	private void swap() {
		List dummy;
		dummy = new ArrayList(morph.getPointList());
		morph.setPointList(listPoints);
		listPoints = dummy;
		dummy = new ArrayList(morph.getVectorList());
		morph.setVectorList(listVectors);
		listVectors = dummy;
	}
}
