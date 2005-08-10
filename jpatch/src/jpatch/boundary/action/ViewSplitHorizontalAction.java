package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ViewSplitHorizontalAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ViewSplitHorizontalAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/horizontal.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("horizontal split view"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.HORIZONTAL_SPLIT);
	}
}

