package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class ViewQuadAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ViewQuadAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/quad.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("quad view"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().resetMode(JPatchScreen.QUAD);
	}
}

