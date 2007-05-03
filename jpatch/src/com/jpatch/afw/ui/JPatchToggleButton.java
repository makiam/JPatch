package com.jpatch.afw.ui;

import com.jpatch.afw.control.*;
import javax.swing.JButton;

public class JPatchToggleButton extends JButton implements JPatchButton {
	private ToggleAction jpatchAction;
	
	public ToggleAction getJPatchAction() {
		return jpatchAction;
	}
}
