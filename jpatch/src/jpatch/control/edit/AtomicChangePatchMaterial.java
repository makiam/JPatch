package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Changes the bLoop flag of a ControlPoint
 */
public class AtomicChangePatchMaterial extends JPatchAtomicEdit {

	private Patch patch;
	private OLDMaterial material;
	
	public AtomicChangePatchMaterial(Patch patch, OLDMaterial material) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + patch + ", " + material + ")");
		this.patch = patch;
		this.material = material;
		swap();
	}
	
	public void redo() {
		swap();
	}

	public void undo() {
		swap();
	}
	
	private void swap() {
		OLDMaterial dummy = material;
		material = patch.getMaterial();
		patch.setMaterial(dummy);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}


