package jpatch.boundary.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jpatch.renderer.AnimationRenderer;

public class RenderAnimationAction extends AbstractAction {

	public void actionPerformed(ActionEvent e) {
		new AnimationRenderer().testShowDisplay();
	}

}
