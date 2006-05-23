package jpatch.boundary.sidebar;

//import java.awt.event.*;
import java.awt.Color;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.vecmath.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.action.*;

public class BonePanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7041600166377802101L;
	JPatchInput inputName;
	JSlider slider;
	Bone bone;
	
	public BonePanel(final Bone bone) {
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		if (MainFrame.getInstance().getAnimation() != null)
			return;
		this.bone = bone;
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",bone.getName());
		detailPanel.add(inputName);
		detailPanel.add(new JLabel("Joint rotation:"));
		inputName.addChangeListener(this);
		slider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (bone.getJointRotation() / 3.6f));
//		slider.setFocusable(false);
		detailPanel.add(slider);
		detailPanel.repaint();
		slider.addChangeListener(this);
		JButton addYaw = new JButton(new AddDofAction(bone, 1, "add yaw"));
		JButton addPitch = new JButton(new AddDofAction(bone, 2, "add pitch"));
		JButton addRoll = new JButton(new AddDofAction(bone, 4, "add roll"));
		addYaw.setEnabled((bone.getDofMask() & 1) == 0);
		addPitch.setEnabled((bone.getDofMask() & 2) == 0);
		addRoll.setEnabled((bone.getDofMask() & 4) == 0);
		JButton colorButton = new JButton("set color");
		add(colorButton);
		add(addYaw);
		add(addPitch);
		add(addRoll);
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				bone.setColor(new Color3f(JColorChooser.showDialog(MainFrame.getInstance(), "Set bone's color", bone.getColor().get())));
			}
		});
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		if (changeEvent.getSource() == inputName) {
			String newName = inputName.getStringValue();
			for (Bone b : MainFrame.getInstance().getModel().getBoneSet()) {
				if (b != bone && b.getName().equals(newName)) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Bone names must be unique");
					inputName.setText(bone.getName());
					return;
				}
			}
			bone.setName(newName);
			((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(bone);
			MainFrame.getInstance().requestFocus();
		} else if (changeEvent.getSource() == slider) {
			if (slider.getValueIsAdjusting()) {
//				morph.unapply();
				bone.setJointRotation(slider.getValue() * 3.6f);
				MainFrame.getInstance().getModel().applyMorphs();
				MainFrame.getInstance().getModel().setPose();
				MainFrame.getInstance().getJPatchScreen().update_all();
			} else {
			}
		}
	}
}

