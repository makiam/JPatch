package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import jpatch.boundary.*;

import jpatch.entity.*;
import jpatch.control.edit.*;

public final class SelectMaterialAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OLDMaterial material;
	
	public SelectMaterialAction(OLDMaterial material) {
		super("Select");
		this.material = material;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		//System.out.println("select material");
		ArrayList list = new ArrayList();
		for (Iterator it = MainFrame.getInstance().getModel().getPatchSet().iterator(); it.hasNext(); ) {
			Patch patch = (Patch) it.next();
			if (material == patch.getMaterial()) {
				OLDControlPoint[] acp = patch.getControlPoints();
				for (int i = 0; i < acp.length; i++) {
					list.add(acp[i].getHead());
				}
			}
		}
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(new OLDSelection(list)));
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
