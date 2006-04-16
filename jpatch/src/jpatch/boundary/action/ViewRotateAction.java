package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

public final class ViewRotateAction extends AbstractAction {
	public void actionPerformed(ActionEvent actionEvent) {
//		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.VIEW_ROTATE) {
//			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().setMouseListener(new ChangeViewMouseListener(MouseEvent.BUTTON1,ChangeViewMouseListener.ROTATE));
			MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
//			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.VIEW_ROTATE);
//		} else {
//			MainFrame.getInstance().getMeshToolBar().reset();
//		}
	}	
}

