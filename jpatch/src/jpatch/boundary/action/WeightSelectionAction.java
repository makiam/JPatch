package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;
import jpatch.boundary.tools.*;
import jpatch.control.edit.*;

public final class WeightSelectionAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public WeightSelectionAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/magnet.png")));
		//putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("set weight"));
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		//MainFrame.getInstance().getJPatchScreen().setTool(null);
		
		if (MainFrame.getInstance().getMeshToolBar().getMode() != MeshToolBar.ADD) {
			if (MainFrame.getInstance().getSelection() != null) {
				MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
				MainFrame.getInstance().getJPatchScreen().addMouseListeners(new WeightSelectionMouseAdapter());
			}
		}
	}
}

