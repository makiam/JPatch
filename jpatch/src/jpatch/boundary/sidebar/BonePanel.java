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
import jpatch.control.edit.*;

public class BonePanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7041600166377802101L;
	JPatchInput inputName;
	JSlider slider;
	OLDBone bone;
	
	public BonePanel(final OLDBone bone) {
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
		JButton deleteButton = new JButton("delete");
		JButton removeButton = new JButton("remove");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPatchActionEdit edit = new JPatchActionEdit("delete bone");
				edit.addEdit(new CompoundDeleteBone(bone));
				edit.addEdit(new AtomicChangeSelection(null));
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPatchActionEdit edit = new JPatchActionEdit("remove bone");
				edit.addEdit(new CompoundRemoveBone(bone));
				edit.addEdit(new AtomicChangeSelection(null));
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		JButton addYaw = new JButton(new AddDofAction(bone, 1, "add yaw"));
		JButton addPitch = new JButton(new AddDofAction(bone, 2, "add pitch"));
		JButton addRoll = new JButton(new AddDofAction(bone, 4, "add roll"));
		addYaw.setEnabled((bone.getDofMask() & 1) == 0);
		addPitch.setEnabled((bone.getDofMask() & 2) == 0);
		addRoll.setEnabled((bone.getDofMask() & 4) == 0);
		JButton colorButton = new JButton("set color");
		add(deleteButton);
		add(removeButton);
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
			MainFrame.getInstance().getModel().renameBone(bone, newName);
			inputName.setText(bone.getName());
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

