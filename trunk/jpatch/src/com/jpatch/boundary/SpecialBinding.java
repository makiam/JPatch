package com.jpatch.boundary;

import javax.swing.*;

public interface SpecialBinding {
	public JComponent getComponent();
	public void bindTo(Object binding);
}
