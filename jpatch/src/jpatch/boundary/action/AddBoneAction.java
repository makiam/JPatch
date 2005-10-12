package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.mouse.*;

public final class AddBoneAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2818638231423442181L;
	public AddBoneAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/addbone.png")));
		putValue(Action.SHORT_DESCRIPTION,"Add Bone");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().removeAllMouseListeners();
		MainFrame.getInstance().getJPatchScreen().addMouseListeners(new AddBoneMouseAdapter());
		MainFrame.getInstance().getJPatchScreen().enablePopupMenu(false);
		MainFrame.getInstance().clearDialog();
	}
}

