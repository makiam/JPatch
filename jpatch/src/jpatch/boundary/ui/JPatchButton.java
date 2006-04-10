package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.KeyEvent;

import javax.swing.*;

public class JPatchButton extends JButton {
	private static final Insets INSETS = new Insets(2, 2, 2, 2);
	
	public JPatchButton(DefaultButtonModel buttonModel) {
		super();
		setModel(new JPatchButtonModel(buttonModel));
	}

//	public JPatchButton(Icon icon) {
//		super(icon);
//	}
//
//	public JPatchButton(String text) {
//		super(text);
//	}
//
//	public JPatchButton(Action a) {
//		super(a);
////		setAction(a);
//	}
//
//	public JPatchButton(String text, Icon icon) {
//		super(text, icon);
//	}
//
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
		String shortDescription = (String) a.getValue("ShortDescription");
		String toolTipText = (String) a.getValue("ButtonToolTip");
		String accelerator = (String) a.getValue("Accelerator");
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
		String acceleratorText = null;
		if (accelerator != null) {
			String acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");
			if (acceleratorDelimiter == null)
				acceleratorDelimiter = "+";
			KeyStroke ks =  KeyStroke.getKeyStroke(accelerator);
			int color = UIManager.getColor("ToolTip.foregroundInactive").getRGB() & 0xffffff;
			acceleratorText = "&nbsp;&nbsp;&nbsp;<font style='font-size: 90%; color: #" + Integer.toHexString(color) + "'>";
			int modifiers = ks.getModifiers();
            if (modifiers > 0) {
                acceleratorText += KeyEvent.getKeyModifiersText(modifiers);
                acceleratorText += acceleratorDelimiter;
            }
            int keyCode = ks.getKeyCode();
            if (keyCode != 0) {
                acceleratorText += KeyEvent.getKeyText(keyCode);
            } else {
                acceleratorText += ks.getKeyChar();
            }
            acceleratorText += "&nbsp;</font>";
            System.out.println(acceleratorText);
        }
		if (toolTipText != null) {
			if (acceleratorText != null)
				setToolTipText("<html>" + toolTipText + acceleratorText + "</html>");
			else
				setToolTipText(toolTipText);
		} else if (shortDescription != null) {
			System.out.println("*");
			if (acceleratorText != null) {
				setToolTipText("<html>" + shortDescription + acceleratorText + "</html>");
				System.out.println("**" + getToolTipText());
			} else
				setToolTipText(shortDescription);
		}
	}
}
