package jpatch.boundary.ui;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class JPatchButtonModel extends DefaultButtonModel implements ChangeListener, ItemListener {

	private DefaultButtonModel underlyingButtonModel;
	
	public JPatchButtonModel(DefaultButtonModel underlyingButtonModel) {
		this.underlyingButtonModel = underlyingButtonModel;
		underlyingButtonModel.addChangeListener(this);
		underlyingButtonModel.addItemListener(this);
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
	}

	public void stateChanged(ChangeEvent e) {
		fireStateChanged();
	}

	public void itemStateChanged(ItemEvent e) {
		fireItemStateChanged(new ItemEvent(this, e.getID(), this, e.getStateChange()));
	}
	
}
