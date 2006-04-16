package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.tools.*;
import jpatch.control.edit.*;

public final class SelectMoveAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
		/*
		MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		MainFrame.getInstance().getJPatchScreen().addMouseListeners(new SelectMoveMouseAdapter());
		MainFrame.getInstance().clearDialog();
		*/
		//MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
//		if (!(MainFrame.getInstance().getJPatchScreen().getTool() instanceof DefaultTool))
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(Tools.defaultTool));
//		MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.DEFAULT);
	}
}

