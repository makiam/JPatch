package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.dialog.*;

public final class ExportPovrayAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ExportPovrayAction() {
		super("POV-Ray [.inc]");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		ExportPovrayDialog dialog = new ExportPovrayDialog();
		((JDialog) dialog.getComponent()).setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);
	}
}

