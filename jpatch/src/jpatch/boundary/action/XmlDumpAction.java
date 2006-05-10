package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

public final class XmlDumpAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public XmlDumpAction() {
		super("XML dump");
	}
	public void actionPerformed(ActionEvent actionEvent) {
		if (MainFrame.getInstance().getModel() != null)
			System.out.println(MainFrame.getInstance().getModel().xml(""));
		else
			System.out.println(MainFrame.getInstance().getAnimation().xml(""));
	}
}

