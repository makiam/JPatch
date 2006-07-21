package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public final class ResetPoseAction extends AbstractAction {
	/**
	 * 
	 */
	OLDModel model;
	
	private static final long serialVersionUID = -2818638231423442181L;
	public ResetPoseAction(OLDModel model) {
		super("reset pose");
		this.model = model;
		//putValue(Action.SHORT_DESCRIPTION,"Add Bone");
		//MainFrame.getInstance().getKeyEventDispatcher().setKeyActionListener(this,KeyEvent.VK_A);
	}
	public void actionPerformed(ActionEvent actionEvent) {
		for (Iterator itB = model.getBoneSet().iterator(); itB.hasNext(); ) {
			OLDBone bone = (OLDBone) itB.next();
			for (Iterator itD = bone.getDofs().iterator(); itD.hasNext(); ) {
				((RotationDof) itD.next()).setValue(0);
			}
		}
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}

