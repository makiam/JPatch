package jpatch.boundary.action;

import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import jpatch.boundary.*;
import jpatch.renderer.AnimationRenderer;

public class RenderAnimationAction extends AbstractAction {

	private boolean singleFrame;
	
	public RenderAnimationAction(boolean singleFrame) {
		this.singleFrame = singleFrame;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (!singleFrame) {
			/*
			 * render range of frames
			 */
			JTextField textStart = new JTextField(Integer.toString((int) MainFrame.getInstance().getAnimation().getStart()), 10);
			JTextField textEnd = new JTextField(Integer.toString((int) MainFrame.getInstance().getAnimation().getEnd()), 10);
			
			JPatchForm form = new JPatchForm();

			form.addEntry("First frame", textStart);
			form.addEntry("Last frame", textEnd);
			form.populate();
			
			form.setBorder(new TitledBorder("Frames to render"));
			
			if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), form, "Render animation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				new AnimationRenderer().renderFrames(MainFrame.getInstance().getAnimation().getName(), Integer.parseInt(textStart.getText()), Integer.parseInt(textEnd.getText()));
			}
			
		} else {
			/*
			 * render current frame
			 */
			int frame = (int) MainFrame.getInstance().getAnimation().getPosition();
			new AnimationRenderer().renderFrames(MainFrame.getInstance().getAnimation().getName(), frame, frame);
		}
		
	}

}
