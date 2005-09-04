package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.tools.*;
import jpatch.control.edit.*;

public final class MagnetAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MagnetAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/magnet.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("magnet tool"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.MAGNET) {
			if (MainFrame.getInstance().getSelection() != null) {
				JPatchCompoundEdit compoundEdit = new JPatchCompoundEdit();
				compoundEdit.addEdit(new ChangeSelectionEdit(null));
				compoundEdit.addEdit(new AtomicChangeTool(null));
				MainFrame.getInstance().getUndoManager().addEdit(compoundEdit);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
			MainFrame.getInstance().getJPatchScreen().setTool(null);
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new MagnetMouseAdapter());
			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.MAGNET);
		} else {
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(new DefaultTool()));
			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.DEFAULT);
		}
	}
}

