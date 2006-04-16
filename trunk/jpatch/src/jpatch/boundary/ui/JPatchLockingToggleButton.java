package jpatch.boundary.ui;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.net.URL;

import javax.swing.*;

import jpatch.boundary.*;


/**
 * 
 */

/**
 * @author sascha
 *
 */
public class JPatchLockingToggleButton extends JPatchToggleButton implements KeyBindingHelper.CallBack {
	public final static int LOCKED = 1 << 5;
	
	private boolean oldLocked;
	private Icon lockedIcon;
//	private Icon rolloverLockedIcon;
//	private Icon disabledLockedIcon;
	private Icon defaultSelectedIcon;
//	private Icon defaultRolloverSelectedIcon;
//	private Icon defaultDisabledSelectedIcon;
	
	public JPatchLockingToggleButton(JPatchLockingToggleButtonModel.UnderlyingModel buttonModel) {
		super();
		setModel(new JPatchLockingToggleButtonModel(buttonModel));
	}
	
	protected void configurePropertiesFromAction(Action a) {
		super.configurePropertiesFromAction(a);
		if (a == null)
			return;
//		String lockedIcon = (String) a.getValue("LockedIcon");
//		String rolloverLockedIcon = (String) a.getValue("RolloverLockedIcon");
//		String disabledLockedIcon = (String) a.getValue("DisabledLockedIcon");
//		if (lockedIcon != null)
//			setLockedIcon(new ImageIcon(ClassLoader.getSystemResource(lockedIcon)));
//		if (rolloverLockedIcon != null)
//			setRolloverLockedIcon(new ImageIcon(ClassLoader.getSystemResource(rolloverLockedIcon)));
//		if (disabledLockedIcon != null)
//			setDisabledLockedIcon(new ImageIcon(ClassLoader.getSystemResource(disabledLockedIcon)));
		lockedIcon = getLockedIcon(false);
		setRolloverIcon(getLockedIcon(false));
//		getRolloverLockedIcon();
//		getDisabledLockedIcon();
	}

	public boolean isLocked() {
		return ((JPatchLockingToggleButtonModel) model).isLocked();
	}
	
//	public void setSelectedIcon(Icon selectedIcon) {
//		lockedIcon = null;
//		super.setSelectedIcon(selectedIcon);
//	}
	
//	public void setRolloverIcon(Icon rolloverIcon) {
//		rolloverLockedIcon = null;
//		super.setRolloverIcon(rolloverIcon);
//	}
	
	public void setLockedIcon(Icon lockedIcon) {
		this.lockedIcon = lockedIcon;
	}
	
//	public void setRolloverLockedIcon(Icon rolloverLockedIcon) {
//		this.rolloverLockedIcon = rolloverLockedIcon;
//	}
//	
//	public void setDisabledLockedIcon(Icon disabledLockedIcon) {
//		this.disabledLockedIcon = disabledLockedIcon;
//	}
	
	public Icon getLockedIcon(boolean transparent) {
		if (getSelectedIcon() != null)
			return ImageIconFactory.createLockedIcon(getSelectedIcon(), ImageIconFactory.Position.TOP_LEFT, transparent);
		else if (getIcon() != null)
			return ImageIconFactory.createLockedIcon(getIcon(), ImageIconFactory.Position.TOP_LEFT, transparent);
		throw new IllegalStateException("can't create locked icon - no icon is set!");
	}
	
//	public Icon getRolloverLockedIcon() {
//		if (rolloverLockedIcon == null && getRolloverIcon() != null)
//			rolloverLockedIcon = ImageIconFactory.createLockedIcon(getRolloverIcon(), ImageIconFactory.Position.TOP_LEFT);
//		return rolloverLockedIcon;
//	}
//	
//	public Icon getDisabledLockedIcon() {
//		if (disabledLockedIcon == null)
//			disabledLockedIcon = UIManager.getLookAndFeel().getDisabledIcon(this, getLockedIcon());
//		return disabledLockedIcon;
//	}
	
	protected void fireStateChanged() {
		boolean locked = isLocked();
		if (oldLocked != locked) {
			oldLocked = locked;
			if (locked) {
				defaultSelectedIcon = getSelectedIcon();
//				defaultRolloverSelectedIcon = getRolloverSelectedIcon();
//				defaultDisabledSelectedIcon = getDisabledSelectedIcon();
				if (lockedIcon != null)
					setSelectedIcon(lockedIcon);
//				if (getRolloverLockedIcon() != null)
//					setRolloverSelectedIcon(rolloverLockedIcon);
//				if (getDisabledLockedIcon() != null)
//					setDisabledSelectedIcon(disabledLockedIcon);
			} else {
				setSelectedIcon(defaultSelectedIcon);
//				setRolloverSelectedIcon(defaultRolloverSelectedIcon);
//				setDisabledSelectedIcon(defaultDisabledSelectedIcon);
			}
		}
		super.fireStateChanged();
	}
}
