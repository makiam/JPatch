package jpatch.control.edit;

import java.util.*;
import javax.vecmath.*;
import jpatch.entity.*;

/**
 * This class is inteded to be used if a ControlPoint has to be removed from all
 * morphs because the ControlPoint has been deleted.
 */
 
public class RemoveControlPointFromMorphsEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private ArrayList listMorphs = new ArrayList();
	private ArrayList listVectors = new ArrayList();
	
	public RemoveControlPointFromMorphsEdit(ControlPoint cp, Model model) {
		this.cp = cp;
		for (Iterator it = model.getMorphIterator(); it.hasNext(); ) {
			Morph morph = (Morph) it.next();
			Vector3f vector = morph.removePoint(cp);
			if (vector != null) {
				listMorphs.add(morph);
				listVectors.add(vector);
			}
		}
	}
	
	public String getName() {
		return "remove cp from morphs";
	}
	
	public void undo() {
		for (int i = 0, n = listMorphs.size(); i < n; i++) {
			Morph morph = (Morph) listMorphs.get(i);
			Vector3f vector = (Vector3f)  listVectors.get(i);
			morph.add(cp, vector);
		}
	}
	
	public void redo() {
		for (int i = 0, n = listMorphs.size(); i < n; i++) {
			Morph morph = (Morph) listMorphs.get(i);
			morph.removePoint(cp);
		}
	}
}
