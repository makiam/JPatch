package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;
import jpatch.boundary.tools.*;

public final class RotoscopeAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public RotoscopeAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/roto.png")));
		putValue(Action.SHORT_DESCRIPTION,"Rotoscope tool");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
//		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.ROTOSCOPE) {
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(Tools.rotoscopeTool));
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.ROTOSCOPE);
//		} else {
//			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(new DefaultTool()));
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.DEFAULT);
//		}
	}
}

