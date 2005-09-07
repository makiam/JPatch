package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public final class SelectMaterialAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPatchMaterial material;
	
	public SelectMaterialAction(JPatchMaterial material) {
		super("Select");
		this.material = material;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		//System.out.println("select material");
		PointSelection ps = new PointSelection();
		for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
			if (material == patch.getMaterial()) {
				ControlPoint[] acp = patch.getControlPoints();
				for (int i = 0; i < acp.length; i++) {
					ps.addControlPoint(acp[i].getHead());
				}
			}
		}
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(ps));
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
