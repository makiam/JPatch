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
		System.out.println(MainFrame.getInstance().getModel().xml(""));
	}
}

