package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class TangentAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public TangentAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/magnitude.png")));
		putValue(Action.SHORT_DESCRIPTION,"show/hide tangents");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		/*
		MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
		RotateDialog rotateDialog = new RotateDialog(compoundEdit);
		MainFrame.getInstance().setDialog(rotateDialog);
		MainFrame.getInstance().getJPatchScreen().addMouseListeners(new RotateMouseAdapter(rotateDialog, compoundEdit));
		*/
		//PointSelection ps = MainFrame.getInstance().getPointSelection();
		//if (ps != null && ps.getSize() > 1) {
		//	//MainFrame.getInstance().getJPatchScreen().setTool(new RotateTool());
		//	MainFrame.getInstance().getUndoManager().addEdit(new ChangeToolEdit(new RotateTool()));
		//} else {
		//	MainFrame.getInstance().getMeshToolBar().reset();
		//}
		//MainFrame.getInstance().getUndoManager().addEdit(new ChangeToolEdit(new TangentTool()));
		MainFrame.getInstance().getJPatchScreen().showTangents(!MainFrame.getInstance().getJPatchScreen().showTangents());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

