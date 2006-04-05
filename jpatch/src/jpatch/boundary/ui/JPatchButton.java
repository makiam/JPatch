package jpatch.boundary.ui;

import java.awt.*;
import javax.swing.*;

public class JPatchButton extends JButton {
	private static final Insets INSETS = new Insets(2, 2, 2, 2);
	
	public JPatchButton() {
		super();
	}

	public JPatchButton(Icon icon) {
		super(icon);
	}

	public JPatchButton(String text) {
		super(text);
	}

	public JPatchButton(Action a) {
		super(a);
//		setAction(a);
	}

	public JPatchButton(String text, Icon icon) {
		super(text, icon);
	}

	public Insets getMargin() {
		return INSETS;
	}
	
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
		if (a == null)
			return;
		String icon = (String) a.getValue("Icon");
		String selectedIcon = (String) a.getValue("SelectedIcon");
		String rolloverIcon = (String) a.getValue("RolloverIcon");
		String rolloverSelectedIcon = (String) a.getValue("RolloverSelectedIcon");
		String disabledIcon = (String) a.getValue("DisabledIcon");
		String disabledSelectedIcon = (String) a.getValue("DisabledSelectedIconResoure");
		String toolTipText = (String) a.getValue("ToolTipText");
		if (icon != null)
			setIcon(new ImageIcon(ClassLoader.getSystemResource(icon)));
		if (selectedIcon != null)
			setSelectedIcon(new ImageIcon(ClassLoader.getSystemResource(selectedIcon)));
		if (rolloverIcon != null)
			setRolloverIcon(new ImageIcon(ClassLoader.getSystemResource(rolloverIcon)));
		if (rolloverSelectedIcon != null)
			setRolloverSelectedIcon(new ImageIcon(ClassLoader.getSystemResource(rolloverSelectedIcon)));
		if (disabledIcon != null)
			setDisabledIcon(new ImageIcon(ClassLoader.getSystemResource(disabledIcon)));
		if (disabledSelectedIcon != null)
			setDisabledSelectedIcon(new ImageIcon(ClassLoader.getSystemResource(disabledSelectedIcon)));
		setToolTipText(toolTipText);
	}
}
