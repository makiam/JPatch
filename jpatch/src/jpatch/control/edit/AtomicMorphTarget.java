package jpatch.control.edit;

import jpatch.entity.*;

public abstract class AtomicMorphTarget extends JPatchAtomicEdit implements JPatchRootEdit {
	MorphTarget morphTarget;
	Morph morph;
	
	public AtomicMorphTarget(Morph morph, MorphTarget morphTarget) {
		this.morph = morph;
		this.morphTarget = morphTarget;
	}
	
	protected void add() {
		morph.addTarget(morphTarget);
	}
	
	protected void remove() {
		morph.removeTarget(morphTarget);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
	
	public static class Add extends AtomicMorphTarget {
		public Add(Morph morph, MorphTarget morphTarget) {
			super(morph, morphTarget);
			add();
		}
		
		public String getName() {
			return "add morph-target";
		}
		
		public void undo() {
			remove();
		}
		
		public void redo() {
			add();
		}
	}
	
	public static class Remove extends AtomicMorphTarget {
		public Remove(Morph morph, MorphTarget morphTarget) {
			super(morph, morphTarget);
			add();
		}
		
		public String getName() {
			return "remove morph-target";
		}
		
		public void undo() {
			add();
		}
		
		public void redo() {
			remove();
		}
	}
}
