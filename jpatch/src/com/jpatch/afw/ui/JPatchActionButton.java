package com.jpatch.afw.ui;

import com.jpatch.afw.control.JPatchAction;
import javax.swing.JButton;

public class JPatchActionButton extends JButton implements JPatchButton {
	private JPatchAction jpatchAction;
	
	public JPatchAction getJPatchAction() {
		return jpatchAction;
	}
}
