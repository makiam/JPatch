package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import jpatch.boundary.*;

import jpatch.control.edit.*;
import jpatch.entity.*;

public final class DetachControlPointsAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DetachControlPointsAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/detach.png")));
		putValue(Action.SHORT_DESCRIPTION,"Detach points");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
//		PointSelection ps = MainFrame.getInstance().getPointSelection();
//		if (ps != null && ps.isSingle()) {
//			if (ps.isCurve()) {
//				//MainFrame.getInstance().getUndoManager().addEdit(new FullyDetachControlPointEdit(ps.getControlPoint()));
//				MainFrame.getInstance().getUndoManager().addEdit(CorrectSelectionsEdit.detachPoint(ps.getControlPoint()));
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			} else {
//				ControlPoint[] acp = ps.getControlPoint().getStack();
//				JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit("detach");
//				for (int i = 0; i < acp.length; i++) {
//					//compoundEdit.addEdit(new FullyDetachControlPointEdit(acp[i]));
//					compoundEdit.addEdit(CorrectSelectionsEdit.detachPoint(ps.getControlPoint()));
//				}
//				MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			}
//		}
//		Selection selection = MainFrame.getInstance().getSelection();
//		if (selection == null || !selection.isSingle())
//			return;
//		Object object = selection.getHotObject();
//		if (!(object instanceof ControlPoint))
//			return;
//		ControlPoint cp = (ControlPoint) object;
//		/* check if curve segment is selected */
//		if (selection.getDirection() != 0) {
//			MainFrame.getInstance().getUndoManager().addEdit(CorrectSelectionsEdit.detachPoint(cp));
//		} else {
//			JPatchCompoundEdit edit = new JPatchCompoundEdit("detach");
//			ControlPoint[] acp = cp.getStack();
//			for (int i = 0; i < acp.length; i++)
//				edit.addEdit(CorrectSelectionsEdit.detachPoint(acp[i]));
//			MainFrame.getInstance().getUndoManager().addEdit(edit);
//		}
//		MainFrame.getInstance().getJPatchScreen().update_all();
		JPatchActionEdit edit = null;
		OLDSelection selection = MainFrame.getInstance().getSelection();
		if (selection == null || !selection.isSingle())
			return;
		Object object = selection.getHotObject();
		if (object instanceof OLDControlPoint) {
			OLDControlPoint cp = (OLDControlPoint) object;
			if (selection.getDirection() == 0) {
				edit = new JPatchActionEdit("detach points");
				OLDControlPoint[] acp = cp.getStack();
				for (int i = 0; i < acp.length; i++) {
					edit.addEdit(new AtomicDetatchControlPoint(acp[i]));
					for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getPatchSet())).iterator(); it.hasNext(); ) {
						Patch patch = (Patch) it.next();
						if (patch.contains(acp[i]))
							edit.addEdit(new AtomicRemovePatch(patch));
					}
				}
			} else {
				edit = new JPatchActionEdit("detach point");
				edit.addEdit(new AtomicDetatchControlPoint(cp));
				for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getPatchSet())).iterator(); it.hasNext(); ) {
					Patch patch = (Patch) it.next();
					if (patch.contains(cp))
						edit.addEdit(new AtomicRemovePatch(patch));
				}
			}
		} else if (object instanceof OLDBone.BoneTransformable) {
			OLDBone.BoneTransformable bt = (OLDBone.BoneTransformable) object;
			if (bt.isEnd()) {
				List list = new ArrayList(bt.getBone().getChildBones());
				edit = new JPatchActionEdit(list.size() == 1 ? "detach bone" : "detach bones");
				for (Iterator it = list.iterator(); it.hasNext(); ) {
					OLDBone bone = (OLDBone) it.next();
					edit.addEdit(new AtomicDetachBone(bone));
				}
			}
		}
		if (edit != null && edit.isValid()) {
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}
