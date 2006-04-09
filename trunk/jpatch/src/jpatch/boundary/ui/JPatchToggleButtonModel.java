package jpatch.boundary.ui;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class JPatchToggleButtonModel extends JToggleButton.ToggleButtonModel implements ChangeListener, ItemListener {

	private UnderlyingToggleButtonModel underlyingButtonModel;
	
	public JPatchToggleButtonModel(UnderlyingToggleButtonModel underlyingButtonModel) {
		this.underlyingButtonModel = underlyingButtonModel;
		underlyingButtonModel.addChangeListener(this);
		underlyingButtonModel.addItemListener(this);
	}
	
	@Override
	public ButtonGroup getGroup() {
		return underlyingButtonModel.getGroup();
	}

	@Override
	public void setGroup(ButtonGroup group) {
		underlyingButtonModel.setGroup(group);
	}

	@Override
	public boolean isSelected() {
		return underlyingButtonModel.isSelected();
	}

	@Override
	public void setSelected(boolean b) {
		underlyingButtonModel.setSelected(b);
	}

	@Override
	protected void fireActionPerformed(ActionEvent e) {
		underlyingButtonModel.fireActionPerformed(e);
	}

	public void stateChanged(ChangeEvent e) {
		fireStateChanged();
	}

	public void itemStateChanged(ItemEvent e) {
		fireItemStateChanged(new ItemEvent(this, e.getID(), this, e.getStateChange()));
	}
	
}
