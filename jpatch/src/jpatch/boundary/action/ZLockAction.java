package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ZLockAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7976600607292109628L;
	public ZLockAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/zunlocked.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("lock z"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getConstraints().toggleZLock();
	}
}

