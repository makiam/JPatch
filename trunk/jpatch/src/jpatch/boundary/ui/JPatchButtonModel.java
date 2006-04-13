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
//		underlyingButtonModel.addActionListener(this);
	}

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

	@Override
	protected void fireActionPerformed(ActionEvent e) {
		System.out.println(getClass().getName() + ".fireActionPerformed(" + e.hashCode() + ")");
		super.fireActionPerformed(e);
	}
	
//	public void actionPerformed(ActionEvent e) {
//		fireActionPerformed(e);
//	}
	
	public String toString() {
		return getClass().getName() + "@" + hashCode() + " underlying model=" + underlyingButtonModel;
	}
	
}
