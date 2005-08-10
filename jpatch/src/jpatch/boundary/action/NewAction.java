package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.auxilary.*;
import jpatch.boundary.*;

public final class NewAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NewAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/new.png")));
		putValue(Action.SHORT_DESCRIPTION,"New");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getUndoManager().hasChanged()) {
			int option = JPatchUtils.showSaveDialog();
			switch (option) {
				
				case JOptionPane.YES_OPTION:
					SaveAsAction saveAsAction = new SaveAsAction(false);
					if (saveAsAction.save()) {
						MainFrame.getInstance().NEW();
					}
					break;
				
				case JOptionPane.NO_OPTION:
					MainFrame.getInstance().NEW();
			}
		} else {
			MainFrame.getInstance().NEW();
		}
	}
}

