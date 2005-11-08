package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public class AtomicChangeMorph extends JPatchAtomicEdit implements JPatchRootEdit {
	private MorphTarget morph;
	private Map map;
	
	
	public AtomicChangeMorph(MorphTarget morph) {
		this.morph = morph;
		map = morph.getMorphMap();
		morph.set();
	}
	
	public void undo() {
//		morph.unapply();
		swap();
//		morph.apply();
	}
	
	public void redo() {
//		morph.unapply();
		swap();
//		morph.apply();
	}
	
	public int sizeOf() {
		return map == null ? 8 + 4 + 4 : 8 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * map.size() * 2);
	}
	
	public String getName() {
		return "modify morph";
	}
	
	private void swap() {
		Map dummy;
		dummy = new HashMap(morph.getMorphMap());
		morph.setMorphMap(map);
		map = dummy;
	}
}
