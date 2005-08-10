package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

public final class GridSnapAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GridSnapAction() {
		super("",new ImageIcon(ClassLoader.getSystemResource("jpatch/images/grid_no_snap.png")));
		putValue(Action.SHORT_DESCRIPTION,KeyMapping.getDescription("grid"));
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().snapToGrid(!MainFrame.getInstance().getJPatchScreen().snapToGrid());
	}
}
