package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

public class SwitchLookAndFeelAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8098202792221617869L;
	String strPlafClassName;
	
	public SwitchLookAndFeelAction(String name, String className) {
		super(name);
		strPlafClassName = className;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		try {
			UIManager.setLookAndFeel(strPlafClassName);
			SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
			JPatchSettings.getInstance().strPlafClassName = strPlafClassName;
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}

