package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.tools.*;

public final class ScaleAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ScaleAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/scale.png")));
		putValue(Action.SHORT_DESCRIPTION,"Advanced Scale Tool[S]");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		/*
		MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
		ScaleDialog scaleDialog = new ScaleDialog(compoundEdit);
		MainFrame.getInstance().setDialog(scaleDialog);
		MainFrame.getInstance().getJPatchScreen().addMouseListeners(new ScaleMouseAdapter(scaleDialog, compoundEdit));
		*/
		MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
	}
}

