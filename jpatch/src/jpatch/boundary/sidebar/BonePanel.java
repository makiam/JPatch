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
	Bone bone;
	
	public BonePanel(Bone bone) {
		this.bone = bone;
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",bone.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		detailPanel.repaint();
		inputName.addChangeListener(this);
		
		add(new JButton(new AddDofAction(bone)));
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		bone.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(bone);
		MainFrame.getInstance().requestFocus();
	}
}

