package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.auxilary.*;
import jpatch.boundary.*;

public final class NewModelAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public NewModelAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/new.png")));
		putValue(Action.SHORT_DESCRIPTION,"New Model");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("new");
		if (MainFrame.getInstance().getUndoManager().hasChanged()) {
			int option = JPatchUtils.showSaveDialog();
			switch (option) {
				
				case JOptionPane.YES_OPTION:
					SaveAsAction saveAsAction = new SaveAsAction(false);
					if (saveAsAction.save()) {
						MainFrame.getInstance().newModel();
					}
					break;
				
				case JOptionPane.NO_OPTION:
					MainFrame.getInstance().newModel();
			}
		} else {
			MainFrame.getInstance().newModel();
		}
	}
}

