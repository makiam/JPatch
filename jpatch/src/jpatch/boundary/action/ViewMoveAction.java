package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

public final class ViewMoveAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ViewMoveAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/move.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("move view"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
//		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.VIEW_MOVE) {
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new ChangeViewMouseListener(MouseEvent.BUTTON1,ChangeViewMouseListener.MOVE));
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.VIEW_MOVE);
//		} else {
//			MainFrame.getInstance().getMeshToolBar().reset();
//		}
	}
}

