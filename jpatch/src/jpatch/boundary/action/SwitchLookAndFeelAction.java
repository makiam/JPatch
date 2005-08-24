package jpatch.boundary.action;

import javax.swing.*;
import javax.swing.plaf.metal.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.laf.*;

public class SwitchLookAndFeelAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8098202792221617869L;
	LookAndFeel lookAndFeel;
	
	public SwitchLookAndFeelAction(String name, Object lookAndFeel) {
		super(name);
		this.lookAndFeel = (LookAndFeel) lookAndFeel;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		try {
			JPatchSettings.getInstance().strPlafClassName = lookAndFeel.getClass().getName();
			UIManager.setLookAndFeel(lookAndFeel);
			SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

