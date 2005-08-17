package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.control.edit.*;

public final class SwitchRendererAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int iRenderer;
	
	public SwitchRendererAction(int renderer, String name) {
		super(name);
		iRenderer = renderer;
	}
	public void actionPerformed(ActionEvent actionEvent) {
		MainFrame.getInstance().getJPatchScreen().switchRenderer(iRenderer);
	}
}
