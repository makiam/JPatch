package jpatch.boundary;
import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.*;

import sun.security.action.GetBooleanAction;

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
	private Icon defaultSelectedIcon;
	private Icon defaultRolloverSelectedIcon;
	
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
		// Create the model
		setModel(new LockingToggleButtonModel());
		
		model.setSelected(selected);
		
		// initialize
		init(text, icon);
	}
	
	public boolean isLocked() {
		return ((LockingToggleButtonModel) model).isLocked();
	}
	
	public void setLocked(boolean b) {
		((LockingToggleButtonModel) model).setLocked(b);
	}
	
	public void setLockedIcon(final Icon lockedIcon) {
		this.lockedIcon = lockedIcon;
//		System.out.println("setLockedIcon");
//		JFrame frame = new JFrame();
//		frame.add(new JPanel() {
//			public void paint(java.awt.Graphics g) {
//				lockedIcon.paintIcon(this, g, 0, 0);
//			}
//		});
//		frame.setSize(200, 200);
//		frame.setVisible(true);
//		setIcon(lockedIcon);
	}
	
	public void setRolloverLockedIcon(Icon rolloverLockedIcon) {
		this.rolloverLockedIcon = rolloverLockedIcon;
	}
	
	public void createLockedIcons(ImageIconFactory.Position position) {
		if (getSelectedIcon() == null)
			setSelectedIcon(getIcon());
		if (getSelectedIcon() != null)
			setLockedIcon(ImageIconFactory.createLockedIcon(getSelectedIcon(), position));
		if (getRolloverSelectedIcon() != null)
			setRolloverLockedIcon(ImageIconFactory.createLockedIcon(getRolloverSelectedIcon(), position));
	}
	
	protected void fireStateChanged() {
		boolean locked = isLocked();
		if (oldLocked != locked) {
			oldLocked = locked;
			if (locked) {
				defaultSelectedIcon = getSelectedIcon();
				defaultRolloverSelectedIcon = getRolloverSelectedIcon();
				if (lockedIcon != null) {
					setSelectedIcon(lockedIcon);
				}
				if (rolloverLockedIcon != null)
					setRolloverSelectedIcon(rolloverLockedIcon);
			} else {
				System.out.println("not locked");
				if (lockedIcon != null) {
					setSelectedIcon(defaultSelectedIcon);
				}
				if (rolloverLockedIcon != null)
					setRolloverSelectedIcon(defaultRolloverSelectedIcon);
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
			System.out.println(this + ".setPressed(" + b + ")");
			
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
			System.out.println(this + ".setSelected(" + b + ")");
			super.setSelected(b);
			setLocked(false);
		}
	}
}
