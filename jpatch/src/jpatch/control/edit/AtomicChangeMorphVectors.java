package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public class AtomicChangeMorphVectors extends JPatchAtomicEdit {
	private MorphTarget morph;
	private Map mapChange;
	
	
	public AtomicChangeMorphVectors(MorphTarget morph, Map change) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + morph + ", " + change + ")");
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
	
	public int sizeOf() {
		return 8 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * mapChange.size() * 2);
	}
}
