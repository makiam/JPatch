package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.tools.*;
import jpatch.control.edit.*;

public final class AddControlPointAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5180146108807944799L;
	public AddControlPointAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/add.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("add point"));
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		//MainFrame.getInstance().getJPatchScreen().setTool(null);
		
		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.ADD) {
			if (MainFrame.getInstance().getSelection() != null) {
				JPatchActionEdit edit = new JPatchActionEdit("Clear selection, clear tool");
				edit.addEdit(new AtomicChangeSelection(null));
				edit.addEdit(new AtomicChangeTool(null));
				MainFrame.getInstance().getUndoManager().addEdit(edit, true);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddControlPointMouseAdapter());
			MainFrame.getInstance().clearDialog();
			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.ADD);
		} else {
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTool(new DefaultTool()), true);
			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.DEFAULT);
		}
	}
}

