package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ViewSplitVerticalAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ViewSplitVerticalAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/vertical.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("vertical split view"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.VERTICAL_SPLIT);
	}
}

