package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class XLockAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public XLockAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/xunlocked.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("lock x"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getConstraints().toggleXLock();
	}
}

