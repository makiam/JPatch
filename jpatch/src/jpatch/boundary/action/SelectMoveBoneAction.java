package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

public final class SelectMoveBoneAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SelectMoveBoneAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/selectmovebone.png")));
		putValue(Action.SHORT_DESCRIPTION,"Select/Move [ESC]");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		MainFrame.getInstance().getJPatchScreen().addMouseListeners(new SelectMoveBoneMouseAdapter());
		MainFrame.getInstance().clearDialog();
	}
}

