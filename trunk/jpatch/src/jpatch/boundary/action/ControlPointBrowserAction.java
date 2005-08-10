package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public class ControlPointBrowserAction extends AbstractAction{
	private static final long serialVersionUID = 1L;
	public ControlPointBrowserAction() {
		super("Controlpoint Browser");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		PointSelection ps = MainFrame.getInstance().getPointSelection();
		if (ps != null && ps.isSingle()) {
			new ControlPointBrowser(ps.getControlPoint());
		}
	}
}
