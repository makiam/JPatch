package jpatch.boundary.ui;

import java.awt.Rectangle;
import javax.swing.*;
import jpatch.boundary.ui.*;

/**
 * 
 */

/**
 * 
 * @author sascha
 *
 */
public class LockingButtonGroup extends ButtonGroup {
	/*
	 * The default button of the LockingButtonGroup
	 */
	private ButtonModel defaultButtonModel;
	private ButtonModel temporaryButtonModel;
	private boolean switchback;
	
	/*
	 * sets the Default Button 
	 */
	public void setDefaultButtonModel(ButtonModel defaultButtonModel) {
		this.defaultButtonModel = defaultButtonModel;
	}
	
	/*
	 * gets the Default Button 
	 */
	public ButtonModel getDefaultButtonmodel() {
		return defaultButtonModel;
	}
	
	/*
	 * begins a new temporary action
	 * (sets temportayButtonModel to the currently selected one)
	 */
	public void beginTemporaryAction() {
		temporaryButtonModel = getSelection();	
	}
	
	public void switchBack() {
		if (temporaryButtonModel == getSelection())
			switchback = true;
	}
	
	/*
	 * select the default button
	 */
	public void actionDone(boolean abort) {
		ButtonModel selectedButtonModel = getSelection();
		if (!abort && selectedButtonModel instanceof JPatchLockingToggleButtonModel.UnderlyingModel) {
			if (((JPatchLockingToggleButtonModel.UnderlyingModel) selectedButtonModel).isLocked())
				return;
		}
		if (switchback && temporaryButtonModel != null && temporaryButtonModel != selectedButtonModel) {
			doClick(temporaryButtonModel);
			if (temporaryButtonModel instanceof JPatchLockingToggleButtonModel.UnderlyingModel)
				((JPatchLockingToggleButtonModel.UnderlyingModel) temporaryButtonModel).setLocked(true);
			temporaryButtonModel = null;
			switchback = false;
		} else if (defaultButtonModel != null && defaultButtonModel != selectedButtonModel) {
			doClick(defaultButtonModel);
		}
	}
	
	private void doClick(ButtonModel buttonModel) {
		buttonModel.setArmed(true);
		buttonModel.setPressed(true);
		buttonModel.setPressed(false);
		buttonModel.setArmed(false);
	}
}
