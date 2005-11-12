package jpatch.boundary.sidebar;

//import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.action.AddDofAction;

public class BonePanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7041600166377802101L;
	JPatchInput inputName;
	JSlider slider;
	Bone bone;
	
	public BonePanel(Bone bone) {
		this.bone = bone;
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",bone.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		detailPanel.add(new JLabel("Joint rotation:"));
		inputName.addChangeListener(this);
		slider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (bone.getJointRotation() / 3.6f));
		slider.setFocusable(false);
		detailPanel.add(slider);
		detailPanel.repaint();
		slider.addChangeListener(this);
		
		add(new JButton(new AddDofAction(bone, 1, "add yaw")));
		add(new JButton(new AddDofAction(bone, 2, "add pitch")));
		add(new JButton(new AddDofAction(bone, 4, "add roll")));
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		if (changeEvent.getSource() == inputName) {
			bone.setName(inputName.getStringValue());
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

