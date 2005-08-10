package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class YLockAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public YLockAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/yunlocked.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("lock y"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getConstraints().toggleYLock();
	}
}

