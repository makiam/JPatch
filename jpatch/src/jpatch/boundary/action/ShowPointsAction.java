package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ShowPointsAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ShowPointsAction() {
		super("points");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
//		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
//		for (int i = 0; i < ste.length; i++) {
//			System.out.println(ste[i].getClassName() + ste[i].getMethodName() + ste[i].getLineNumber());
//		}
//		System.out.println("show points action");
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition();
		viewDef.renderPoints(!viewDef.renderPoints());
		viewDef.repaint();
	}
}

