package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.tools.*;
import jpatch.control.edit.*;

public final class AddMultiControlPointAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6683091545961721079L;
	public AddMultiControlPointAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/addmultiple.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("add multiple points"));
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
//		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.ADD_LOCK) {
			if (MainFrame.getInstance().getSelection() != null) {
				JPatchActionEdit edit = new JPatchActionEdit("Reset selection, reset tool");
				edit.addEdit(new AtomicChangeSelection(null));
				edit.addEdit(new AtomicChangeTool(null));
				MainFrame.getInstance().getUndoManager().addEdit(edit, true);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
			MainFrame.getInstance().getJPatchScreen().setTool(null);
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter(true));
			MainFrame.getInstance().clearDialog();
			//if (MainFrame.getInstance().getSelection() != null) {
			//	MainFrame.getInstance().getUndoManager().addEdit(new ChangeSelectionEdit(null));
			//	MainFrame.getInstance().getJPatchScreen().update_all();
			//}
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.ADD_LOCK);
//		} else {
//			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(new DefaultTool()));
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.DEFAULT);
//		}
	}
}

