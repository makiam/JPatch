package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public class ControlPointBrowserAction extends AbstractAction{
	private static final long serialVersionUID = 1L;
	public ControlPointBrowserAction() {
		super("Controlpoint Browser");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		Selection selection = MainFrame.getInstance().getSelection();
		if (selection != null && selection.isSingle() && (selection.getHotObject() instanceof ControlPoint)) {
			new ControlPointBrowser((ControlPoint) selection.getHotObject());
		}
	}
}
