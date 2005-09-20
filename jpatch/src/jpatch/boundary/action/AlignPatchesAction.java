package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import jpatch.boundary.*;

import jpatch.entity.*;
import jpatch.control.edit.*;


public final class AlignPatchesAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4600303467349508386L;
	public AlignPatchesAction() {
		super("align patches");
		}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null) {
			HashSet patches = new HashSet();
			for (Patch patch = MainFrame.getInstance().getModel().getFirstPatch(); patch != null; patch = patch.getNext()) {
				if (patch.isSelected(ps)) patches.add(patch);
			}
			MainFrame.getInstance().getUndoManager().addEdit(new AlignPatchesEdit(patches));
		} else {
			MainFrame.getInstance().getUndoManager().addEdit(new AlignPatchesEdit());
		}
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
