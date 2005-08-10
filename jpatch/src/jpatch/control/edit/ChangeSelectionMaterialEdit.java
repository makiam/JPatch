package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;
import jpatch.boundary.selection.*;

/**
 *  Changes the bLoop flag of a ControlPoint
 */
public class ChangeSelectionMaterialEdit extends JPatchCompoundEdit {

	/**
	* @param cp The ControlPoint to change
	* @param loop The new value for bLoop
	**/
	public ChangeSelectionMaterialEdit(PointSelection pointSelection, JPatchMaterial material) {
		for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
			if (patch.isSelected(pointSelection)) {
				addEdit(new ChangePatchMaterialEdit(patch,material));
			}
		}
	}
}


