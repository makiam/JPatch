package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import jpatch.boundary.*;

import jpatch.control.edit.*;
import jpatch.entity.*;

public class RemoveStubsAction extends AbstractAction {
	
	private static final long serialVersionUID = 1L;

	public RemoveStubsAction() {
		super("remove stubs");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		ControlPoint[] acp = selection.getControlPointArray();
		JPatchActionEdit edit = new JPatchActionEdit("remove curve stubs");
		ArrayList list = new ArrayList();
		for (int i = 0; i < acp.length; i++) {
			for (ControlPoint cp = acp[i].getHead(); cp != null; cp = cp.getPrevAttached()) {
				if (!cp.isHook() && cp.isSingle())
					if (cp.getNext() == null || cp.getPrev() == null)
						list.add(cp);
			}
		}
					
		if (list.size() > 0) {
			for (Iterator it = list.iterator(); it.hasNext(); )
				edit.addEdit(new CompoundDeleteControlPoint((ControlPoint) it.next()));
//			edit.addEdit(new RemoveControlPointsFromSelectionEdit(ps, list));
//			acp = ps.getControlPointArray();
//			for (int i = 0; i < acp.length; i++) {
//				if (acp[i].getCurve() == null)
//					edit.addEdit(new RemoveControlPointFromSelectionEdit(acp[i], ps));
//			}
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
}