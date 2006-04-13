/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;


/**
 * @author sascha
 *
 */
public class JPatchLockingToggleButtonModel extends DefaultButtonModel implements ChangeListener, ItemListener, ActionListener {
	private UnderlyingModel underlyingButtonModel;
	public static final int DOUBLECLICK_THRESHOLD = 650;
	private long lastClick;
	
	public JPatchLockingToggleButtonModel(UnderlyingModel underlyingButtonModel) {
		this.underlyingButtonModel = underlyingButtonModel;
		underlyingButtonModel.addChangeListener(this);
		underlyingButtonModel.addItemListener(this);
		underlyingButtonModel.addActionListener(this);
	}
	
//	@Override
//	public ButtonGroup getGroup() {
//		return underlyingButtonModel.getGroup();
//	}
//
//	@Override
//	public void setGroup(ButtonGroup group) {
//		underlyingButtonModel.setGroup(group);
//	}

	@Override
	public boolean isSelected() {
		return underlyingButtonModel.isSelected();
	}

	@Override
	public void setSelected(boolean b) {
		underlyingButtonModel.setSelected(b);
		setLocked(false);
	}

	public void stateChanged(ChangeEvent e) {
		fireStateChanged();
	}

	public void itemStateChanged(ItemEvent e) {
		fireItemStateChanged(new ItemEvent(this, e.getID(), this, e.getStateChange()));
	}
	
	public void actionPerformed(ActionEvent e) {
		fireActionPerformed(e);
	}
	
	public boolean isLocked() {
		return underlyingButtonModel.isLocked();
	}
	
	public void setLocked(boolean b) {
		underlyingButtonModel.setLocked(b);
	}
	
	public void setPressed(boolean b) {
		boolean performAction = false;
		
		if ((isPressed() == b) || !isEnabled()) {
			return;
		}
		
		if (b == false && isArmed()) {
			long time = System.currentTimeMillis();
			if (!isSelected()) {
				setSelected(!this.isSelected());
				performAction = true;
//              setLocked(false);
			} else {
				if (time < lastClick + DOUBLECLICK_THRESHOLD) {
					setLocked(true);
				} else {
					setLocked(false);
					ButtonGroup group = underlyingButtonModel.getGroup();
					if (group != null && group instanceof LockingButtonGroup)
						((LockingButtonGroup) group).actionDone(false);
					setRollover(false);
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
	
	public static class UnderlyingModel extends JToggleButton.ToggleButtonModel {
		public final static int LOCKED = 1 << 5;
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
		
		public void setPressed(boolean b) {
			boolean performAction = false;
			
			if ((isPressed() == b) || !isEnabled()) {
				return;
			}
			
			if (b == false && isArmed()) {
				long time = System.currentTimeMillis();
				if (!isSelected()) {
					setSelected(!this.isSelected());
					performAction = true;
//	              setLocked(false);
				} else {
					if (time < lastClick + DOUBLECLICK_THRESHOLD) {
						setLocked(true);
					} else {
						setLocked(false);
						ButtonGroup group = getGroup();
						if (group != null && group instanceof LockingButtonGroup)
							((LockingButtonGroup) group).actionDone(false);
						setRollover(false);
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

		@Override
		public void setSelected(boolean b) {
			// TODO Auto-generated method stub
			super.setSelected(b);
			if (!b)
				setLocked(false);
		}
		
		
	}
}
