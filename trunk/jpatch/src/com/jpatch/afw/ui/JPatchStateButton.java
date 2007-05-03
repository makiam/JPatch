package com.jpatch.afw.ui;

import com.jpatch.afw.control.*;
import javax.swing.JButton;

public class JPatchStateButton extends JButton implements JPatchButton {
	private SwitchStateAction jpatchAction;
	
	public SwitchStateAction getJPatchAction() {
		return jpatchAction;
	}
}
