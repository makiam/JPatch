package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;

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
			UIManager.setLookAndFeel(lookAndFeel);
			SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
			JPatchSettings.getInstance().strPlafClassName = lookAndFeel.getClass().getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

