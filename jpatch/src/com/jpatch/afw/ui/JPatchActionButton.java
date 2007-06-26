package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.JPatchAction;
import javax.swing.JButton;

public class JPatchActionButton extends JButton implements JPatchButton {
	private final JPatchAction jpatchAction;
	
	public JPatchActionButton(JPatchAction action) {
		this.jpatchAction = action;
		action.getEnabled().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				setEnabled(((BooleanAttr) source).getBoolean());
			}
		});
		addActionListener(jpatchAction);
	}
	
	public JPatchAction getJPatchAction() {
		return jpatchAction;
	}
}
