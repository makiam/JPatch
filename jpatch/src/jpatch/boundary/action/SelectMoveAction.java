package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.tools.*;
import jpatch.control.edit.*;

public final class SelectMoveAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SelectMoveAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/default.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("default tool"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		/*
		MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		MainFrame.getInstance().getJPatchScreen().addMouseListeners(new SelectMoveMouseAdapter());
		MainFrame.getInstance().clearDialog();
		*/
		//MainFrame.getInstance().getJPatchScreen().setTool(new DefaultTool());
		MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(new DefaultTool()));
		MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.DEFAULT);
	}
}

