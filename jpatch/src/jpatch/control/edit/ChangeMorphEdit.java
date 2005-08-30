package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public class ChangeMorphEdit extends JPatchAbstractUndoableEdit {
	private Morph morph;
	private Map map;
	
	
	public ChangeMorphEdit(Morph morph) {
		this.morph = morph;
		map = morph.getMorphMap();
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
		Map dummy;
		dummy = new HashMap(morph.getMorphMap());
		morph.setMorphMap(map);
		map = dummy;
	}
}
