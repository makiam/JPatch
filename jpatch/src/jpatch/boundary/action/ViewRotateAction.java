package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

public final class ViewRotateAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ViewRotateAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/rotate.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("rotate view"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
//		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.VIEW_ROTATE) {
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new ChangeViewMouseListener(MouseEvent.BUTTON1,ChangeViewMouseListener.ROTATE));
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.VIEW_ROTATE);
//		} else {
//			MainFrame.getInstance().getMeshToolBar().reset();
//		}
	}	
}

