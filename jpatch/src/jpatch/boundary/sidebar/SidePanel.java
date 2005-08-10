package jpatch.boundary.sidebar;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3722600016758522183L;

	public SidePanel() {
		super();
		setLayout(new GridLayout(3,2));
		Dimension dim = new Dimension(196,60);
		setPreferredSize(dim);
		setMinimumSize(dim);
		setMaximumSize(dim);
		//MainFrame.getInstance().getSideBar().clearDetailPanel();
	}
}

