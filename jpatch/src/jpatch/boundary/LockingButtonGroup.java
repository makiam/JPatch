package jpatch.boundary;
import javax.swing.*;

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
	private AbstractButton defaultButton;

	/*
	 * sets the Default Button 
	 */
	public void setDefaultButton(AbstractButton defaultButton) {
		this.defaultButton = defaultButton;
	}
	
	/*
	 * gets the Default Button 
	 */
	public AbstractButton getDefaultButton() {
		return defaultButton;
	}
	
	/*
	 * if the defaultbutton is not the currently selected button, a doClick() is performed on it
	 */
	public void actionDone() {
		ButtonModel selectedButtonModel = getSelection();
		if (defaultButton != null && defaultButton.getModel() != selectedButtonModel) {
			defaultButton.doClick();
		}
	}
}
