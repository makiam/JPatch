package jpatch.boundary.ui;

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

public class JPatchToggleButton extends JToggleButton {

	private static final Insets INSETS = new Insets(2, 2, 2, 2);
	
	public JPatchToggleButton() {
		super();
	}

	public JPatchToggleButton(Icon icon) {
		super(icon);
	}

	public JPatchToggleButton(Icon icon, boolean selected) {
		super(icon, selected);
	}

	public JPatchToggleButton(String text) {
		super(text);
	}

	public JPatchToggleButton(String text, boolean selected) {
		super(text, selected);
	}

	public JPatchToggleButton(Action a) {
		super(a);
	}

	public JPatchToggleButton(String text, Icon icon) {
		super(text, icon);
	}

	public JPatchToggleButton(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
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
		System.out.println(a.getClass().getName() + " ToolTipText = " + toolTipText);
	}
}
