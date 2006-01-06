package jpatch.boundary.action;

import java.awt.event.*;

import javax.swing.*;
import jpatch.auxilary.JPatchUtils;
import jpatch.boundary.MainFrame;

public class NewAnimAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	public NewAnimAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/new.png")));
		putValue(Action.SHORT_DESCRIPTION,"New Animation");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("new anim");
		if (MainFrame.getInstance().getUndoManager().hasChanged()) {
			int option = JPatchUtils.showSaveDialog();
			switch (option) {
				
				case JOptionPane.YES_OPTION:
					SaveAsAction saveAsAction = new SaveAsAction(false);
					if (saveAsAction.save()) {
						MainFrame.getInstance().newAnimation();
					}
					break;
				
				case JOptionPane.NO_OPTION:
					MainFrame.getInstance().newAnimation();
			}
		} else {
			MainFrame.getInstance().newAnimation();
		}
	}
}
