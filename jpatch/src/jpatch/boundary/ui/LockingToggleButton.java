package jpatch.boundary.ui;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
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
public class LockingToggleButton extends JPatchToggleButton {
	
	public final static int LOCKED = 1 << 5;
	
	private boolean oldLocked;
	private Icon lockedIcon;
	private Icon rolloverLockedIcon;
	private Icon disabledLockedIcon;
	private Icon defaultSelectedIcon;
	private Icon defaultRolloverSelectedIcon;
	private Icon defaultDisabledSelectedIcon;
	
	public LockingToggleButton () {
		this(null, null, false);
	}
	
	/**
	 * Creates an initially unselected toggle button
	 * with the specified image but no text.
	 *
	 * @param icon  the image that the button should display
	 */
	public LockingToggleButton(Icon icon) {
		this(null, icon, false);
	}
	
	/**
	 * Creates a toggle button with the specified image 
	 * and selection state, but no text.
	 *
	 * @param icon  the image that the button should display
	 * @param selected  if true, the button is initially selected;
	 *                  otherwise, the button is initially unselected
	 */
	public LockingToggleButton(Icon icon, boolean selected) {
		this(null, icon, selected);
	}
	
	/**
	 * Creates an unselected toggle button with the specified text.
	 *
	 * @param text  the string displayed on the toggle button
	 */
	public LockingToggleButton (String text) {
		this(text, null, false);
	}
	
	/**
	 * Creates a toggle button with the specified text
	 * and selection state.
	 *
	 * @param text  the string displayed on the toggle button
	 * @param selected  if true, the button is initially selected;
	 *                  otherwise, the button is initially unselected
	 */
	public LockingToggleButton (String text, boolean selected) {
		this(text, null, selected);
	}
	
	/**
	 * Creates a toggle button where properties are taken from the 
	 * Action supplied.
	 *
	 * @since 1.3
	 */
	public LockingToggleButton(Action a) {
		this();
		setAction(a);
	}
	
	/**
	 * Creates a toggle button that has the specified text and image,
	 * and that is initially unselected.
	 *
	 * @param text the string displayed on the button
	 * @param icon  the image that the button should display
	 */
	public LockingToggleButton(String text, Icon icon) {
		this(text, icon, false);
	}
	
	/**
	 * Creates a toggle button with the specified text, image, and
	 * selection state.
	 *
	 * @param text the text of the toggle button
	 * @param icon  the image that the button should display
	 * @param selected  if true, the button is initially selected;
	 *                  otherwise, the button is initially unselected
	 */
	public LockingToggleButton (String text, Icon icon, boolean selected) {
		System.out.println("*");
		// Create the model
		setModel(new LockingToggleButtonModel());
		
		model.setSelected(selected);
		
		// initialize
		init(text, icon);
	}
	
	protected void configurePropertiesFromAction(Action a) {
		System.out.println("hello hello");
		super.configurePropertiesFromAction(a);
		if (a == null)
			return;
		String lockedIcon = (String) a.getValue("LockedIcon");
		String rolloverLockedIcon = (String) a.getValue("RolloverLockedIcon");
		String disabledLockedIcon = (String) a.getValue("DisabledLockedIcon");
		if (lockedIcon != null)
			setLockedIcon(new ImageIcon(ClassLoader.getSystemResource(lockedIcon)));
		if (rolloverLockedIcon != null)
			setRolloverLockedIcon(new ImageIcon(ClassLoader.getSystemResource(rolloverLockedIcon)));
		if (disabledLockedIcon != null)
			setDisabledLockedIcon(new ImageIcon(ClassLoader.getSystemResource(disabledLockedIcon)));
		System.out.println(a.getValue("ToolTipText"));
	}

	public boolean isLocked() {
		return ((LockingToggleButtonModel) model).isLocked();
	}
	
	public void setSelectedIcon(Icon selectedIcon) {
		lockedIcon = null;
		super.setSelectedIcon(selectedIcon);
	}
	
	public void setRolloverIcon(Icon rolloverIcon) {
		rolloverLockedIcon = null;
		super.setRolloverIcon(rolloverIcon);
	}
	
	public void setLockedIcon(Icon lockedIcon) {
		this.lockedIcon = lockedIcon;
	}
	
	public void setRolloverLockedIcon(Icon rolloverLockedIcon) {
		this.rolloverLockedIcon = rolloverLockedIcon;
	}
	
	public void setDisabledLockedIcon(Icon disabledLockedIcon) {
		this.disabledLockedIcon = disabledLockedIcon;
	}
	
	public Icon getLockedIcon() {
		if (lockedIcon == null) {
			if (getSelectedIcon() != null)
				lockedIcon = ImageIconFactory.createLockedIcon(getSelectedIcon(), ImageIconFactory.Position.TOP_LEFT);
			else if (getIcon() != null)
				lockedIcon = ImageIconFactory.createLockedIcon(getIcon(), ImageIconFactory.Position.TOP_LEFT);
		}
		return lockedIcon;
	}
	
	public Icon getRolloverLockedIcon() {
		if (rolloverLockedIcon == null && getRolloverIcon() != null)
			rolloverLockedIcon = ImageIconFactory.createLockedIcon(getRolloverIcon(), ImageIconFactory.Position.TOP_LEFT);
		return rolloverLockedIcon;
	}
	
	public Icon getDisabledLockedIcon() {
		if (disabledLockedIcon == null)
			disabledLockedIcon = UIManager.getLookAndFeel().getDisabledIcon(this, getLockedIcon());
		return disabledLockedIcon;
	}
	
	protected void fireStateChanged() {
		boolean locked = isLocked();
		if (oldLocked != locked) {
			oldLocked = locked;
			if (locked) {
				defaultSelectedIcon = getSelectedIcon();
				defaultRolloverSelectedIcon = getRolloverSelectedIcon();
				defaultDisabledSelectedIcon = getDisabledSelectedIcon();
				if (getLockedIcon() != null)
					setSelectedIcon(lockedIcon);
				if (getRolloverLockedIcon() != null)
					setRolloverSelectedIcon(rolloverLockedIcon);
				if (getDisabledLockedIcon() != null)
					setDisabledSelectedIcon(disabledLockedIcon);
			} else {
				setSelectedIcon(defaultSelectedIcon);
				setRolloverSelectedIcon(defaultRolloverSelectedIcon);
				setDisabledSelectedIcon(defaultDisabledSelectedIcon);
			}
		}
		super.fireStateChanged();
	}
	
	public static class LockingToggleButtonModel extends JToggleButton.ToggleButtonModel {
		public static final int DOUBLECLICK_THRESHOLD = 650;
		private long lastClick;
		
		public boolean isLocked() {
	        return (stateMask & LOCKED) != 0;
	    }
		
		public void setLocked(boolean b) {
			if((isLocked() == b) || !isEnabled()) {
	            return;
	        }
	            
	        if (b) {
	            stateMask |= LOCKED;
	        } else {
	            stateMask &= ~LOCKED;
	        }
	            
	        fireStateChanged();
	    }
		
		
		public void setArmed(boolean b) {
//			System.out.println(this + ".setArmed(" + b + ")");
			super.setArmed(b);
		}
		
		public void setPressed(boolean b) {
//			System.out.println(this + ".setPressed(" + b + ")");
			
			boolean performAction = false;
			
			if ((isPressed() == b) || !isEnabled()) {
                return;
            }

            if (b == false && isArmed()) {
            	long time = System.currentTimeMillis();
            	if (!isSelected()) {
            		setSelected(!this.isSelected());
                	performAction = true;
 //               	setLocked(false);
            	} else {
            		System.out.println("1");
            		if (time < lastClick + DOUBLECLICK_THRESHOLD) {
            			System.out.println("2");
            			setLocked(true);
            		} else {
            			System.out.println("3");
            			setLocked(false);
            			if (getGroup() != null && getGroup() instanceof LockingButtonGroup)
            				((LockingButtonGroup) getGroup()).actionDone();
            		}
            	}
            	lastClick = time;
            } 

            if (b) {
                stateMask |= PRESSED;
            } else {
                stateMask &= ~PRESSED;
            }

            fireStateChanged();

            if(!isPressed() && isArmed() && performAction) {
                int modifiers = 0;
                AWTEvent currentEvent = EventQueue.getCurrentEvent();
                if (currentEvent instanceof InputEvent) {
                    modifiers = ((InputEvent)currentEvent).getModifiers();
                } else if (currentEvent instanceof ActionEvent) {
                    modifiers = ((ActionEvent)currentEvent).getModifiers();
                }
                fireActionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                                    getActionCommand(),
                                    EventQueue.getMostRecentEventTime(),
                                    modifiers));
            }
		}
		
		public void setSelected(boolean b) {
//			System.out.println(this + ".setSelected(" + b + ")");
			super.setSelected(b);
			setLocked(false);
		}
	}
}
