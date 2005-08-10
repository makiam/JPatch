package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

public final class ViewZoomAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ViewZoomAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/zoom.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("zoom view"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.VIEW_ZOOM) {
			MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
			MainFrame.getInstance().getJPatchScreen().addMouseListeners(new ChangeViewMouseListener(MouseEvent.BUTTON1,ChangeViewMouseListener.ZOOM));
			MainFrame.getInstance().getMeshToolBar().setMode(MeshToolBar.VIEW_ZOOM);
		} else {
			MainFrame.getInstance().getMeshToolBar().reset();
		}
	}
}

