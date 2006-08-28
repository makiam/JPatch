package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public final class NewMorphTargetAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private OLDMorph morph;
	
	public NewMorphTargetAction(OLDMorph morph) {
		super("Add target");
		this.morph = morph;
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MorphTarget target = new MorphTarget(morph.getValue());
		Map map = new HashMap();
		for (Iterator it = morph.getMorphMap().keySet().iterator(); it.hasNext(); ) {
			OLDControlPoint cp = (OLDControlPoint) it.next();
			Vector3f v = (Vector3f) morph.getMorphMap().get(cp);
			map.put(cp, new Vector3f(v));
		}
		target.setMorphMap(map);
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicMorphTarget.Add(morph, target));
	}
}

