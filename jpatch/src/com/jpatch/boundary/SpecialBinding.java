package com.jpatch.boundary;

import com.jpatch.afw.ui.*;

import javax.swing.*;

public abstract interface SpecialBinding {
	public void bindTo(Object binding);
	
	public interface CustomComponent extends SpecialBinding {
		public JComponent getComponent();
	}
	
	public interface FormContainer extends SpecialBinding {
		public JPatchFormContainer getFormContainer();
	}
}

