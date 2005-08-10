package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Changes the bLoop flag of a ControlPoint
 */
public class ChangePatchMaterialEdit extends JPatchAbstractUndoableEdit {

	private Patch patch;
	private JPatchMaterial material;
	
	/**
	* @param cp The ControlPoint to change
	* @param loop The new value for bLoop
	**/
	public ChangePatchMaterialEdit(Patch patch, JPatchMaterial material) {
		this.patch = patch;
		this.material = material;
		swap();
	}


	/**
	 *  redoes the operation
	 */
	public void redo() {
		swap();
	}


	/**
	 *  undoes the operation
	 */
	public void undo() {
		swap();
	}
	
	private void swap() {
		JPatchMaterial dummy = material;
		material = patch.getMaterial();
		patch.setMaterial(dummy);
	}
}


