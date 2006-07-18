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
			System.out.println(option);
			switch (option) {
				
				case 2:
					SaveAsAction saveAsAction = new SaveAsAction(false);
					if (saveAsAction.save()) {
						close();
					}
					break;
				
				case 0:
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

