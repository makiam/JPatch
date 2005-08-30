package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public class ChangeMorphVectorsEdit extends JPatchAbstractUndoableEdit {
	private Morph morph;
	private Map mapChange;
	
	
	public ChangeMorphVectorsEdit(Morph morph, Map change) {
		this.morph = morph;
		mapChange = change;
		swap();
		//morph.dump();
		//System.out.println(mapChange);
		//morph.set();
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
		Map dummy = morph.modifyMorphMap(mapChange);
		mapChange = dummy;
//		dummy = new ArrayList(morph.getPointList());
//		morph.setPointList(listPoints);
//		listPoints = dummy;
//		dummy = new ArrayList(morph.getVectorList());
//		morph.setVectorList(listVectors);
//		listVectors = dummy;
	}
}
