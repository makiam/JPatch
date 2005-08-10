package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ViewSingleAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ViewSingleAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/single.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("single view"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.SINGLE);
	}
}

