package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.auxilary.*;
import jpatch.boundary.*;

public final class QuitAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QuitAction() {
		super("Quit");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getUndoManager().hasChanged()) {
			int option = JPatchUtils.showSaveDialog();
			switch (option) {
				
				case JOptionPane.YES_OPTION:
					SaveAsAction saveAsAction = new SaveAsAction(false);
					if (saveAsAction.save()) {
						close();
					}
					break;
				
				case JOptionPane.NO_OPTION:
					close();
			}
		} else {
			close();
		}
	}
	
	private void close() {
		JPatchWindowAdapter jpatchWindowAdapter = new JPatchWindowAdapter();
		jpatchWindowAdapter.quit(MainFrame.getInstance());
	}
}

