package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.control.edit.*;
import jpatch.boundary.*;


public final class LatheAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public LatheAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/lathe.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("lathe"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("Lathe..." + actionEvent.hashCode() + " " + actionEvent.getSource().hashCode());
//		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
//			System.out.println(ste);
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && !selection.isSingle()) {
			if (AbstractClone.checkForHooks(selection.getControlPointArray())) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Lathe operation can not be performed bacause the selection contains hooks", "Can't lathe", JOptionPane.ERROR_MESSAGE);
			} else {
				//PointSelection newPs = MainFrame.getInstance().getModel().clone(ps.getControlPointArray());
				//MainFrame.getInstance().setSelection(newPs);
//				float epsilon = 3f / MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().getMatrix().getScale();
				//System.out.println(epsilon);
				new LatheOptions(MainFrame.getInstance(), 0.0f);
			}
		}
	}
}
