package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.dialog.*;

public final class ZBufferQualityAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2278339099081500769L;

	public ZBufferQualityAction() {
		super("Realtime renderer settings...");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		ZBufferSettings dialog = new ZBufferSettings();
		((JDialog) dialog.getComponent()).setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);
	}
}

