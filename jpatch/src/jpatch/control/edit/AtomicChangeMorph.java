package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;

/**
 * Use this class for changing morphs
 */

public abstract class AtomicChangeMorph extends JPatchAtomicEdit implements JPatchRootEdit {
	MorphTarget morphTarget;
	Map map;
	
	public AtomicChangeMorph(MorphTarget morphTarget, Map map) {
		this.morphTarget = morphTarget;
		this.map = map;
	}
	
	void addPoints() {
		morphTarget.getMorphMap().putAll(map);
	}
	
	void removePoints() {
		for (Iterator it = map.keySet().iterator(); it.hasNext(); )
			morphTarget.getMorphMap().remove(it.next());
	}
	
	public int sizeOf() {
		return map == null ? 8 + 4 + 4 : 8 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * map.size() * 2);
	}
	
	public static class AddPoints extends AtomicChangeMorph {
		public AddPoints(MorphTarget morphTarget, Map map) {
			super(morphTarget, map);
			addPoints();
		}
		
		public String getName() {
			return "add points to morph";
		}
		
		public void undo() {
			removePoints();
		}
		
		public void redo() {
			addPoints();
		}
	}
	
	public static class RemovePoints extends AtomicChangeMorph {
		public RemovePoints(MorphTarget morphTarget, Map map) {
			super(morphTarget, map);
			removePoints();
		}
		
		public String getName() {
			return "remove points from morph";
		}
		
		public void undo() {
			addPoints();
		}
		
		public void redo() {
			removePoints();
		}
	}
}
