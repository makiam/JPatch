package jpatch.boundary.action;

import javax.swing.*;
import javax.swing.plaf.metal.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.boundary.laf.*;
import jpatch.boundary.settings.Settings;

public class SwitchLookAndFeelAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8098202792221617869L;
	Settings.Plaf plaf;
	
	public SwitchLookAndFeelAction(Settings.Plaf plaf) {
		this.plaf = plaf;
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		try {
			switch (plaf) {
			case CROSS_PLATFORM:
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				break;
			case SYSTEM:
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				break;
			case JPATCH:
				UIManager.setLookAndFeel(new SmoothLookAndFeel());
				break;
			}
			Settings.getInstance().lookAndFeel = plaf;
			SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

