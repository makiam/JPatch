package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.tools.*;


public final class RotateAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RotateAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/rot.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("rotate tool"));
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.ROTATE) {
			/*
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
			RotateDialog rotateDialog = new RotateDialog(compoundEdit);
			MainFrame.getInstance().setDialog(rotateDialog);
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new RotateMouseAdapter(rotateDialog, compoundEdit));
			*/
			Selection selection = MainFrame.getInstance().getSelection();
			if (selection != null && selection.getMap().size() > 1) {
				//MainFrame.getInstance().getJPatchScreen().setTool(new RotateTool());
				MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(new RotateTool()));
				MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.ROTATE);
			} else {
				MainFrame.getInstance().getMeshToolBar().reset();
			}
		} else {
			MainFrame.getInstance().getMeshToolBar().reset();
		}
	}
}

