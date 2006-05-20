package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.boundary.action.*;
import jpatch.boundary.*;
import jpatch.entity.*;
import jpatch.boundary.ui.*;

public class MorphTargetPanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1319926408036866674L;
	JPatchInput inputPosition;
	MorphTarget morphTarget;
	JToggleButton editButton;
	AbstractButton deleteButton;
	
	public MorphTargetPanel(MorphTarget morphTarget) {
		this.morphTarget = morphTarget;
		MorphTarget editedMorph = MainFrame.getInstance().getEditedMorph();
		deleteButton = new JButton(new DeleteMorphTargetAction(morphTarget));
		editButton = new JToggleButton(new EditMorphTargetAction(morphTarget));
		add(editButton);
		add(deleteButton);
		
		//JPatchSlider.setDimensions(0,150,50,20);
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		JPatchInput.setDimensions(50,150,20);
		
		inputPosition = new JPatchInput("Position:", morphTarget.getPosition());
		
		inputPosition.setEnabled(morphTarget.getPosition() != 0);
		editButton.setEnabled(morphTarget.getPosition() != 0);
		deleteButton.setEnabled(morphTarget.getMorph().getChildCount() > 1 && morphTarget.getPosition() != 0);
		detailPanel.removeAll();
		detailPanel.add(inputPosition);
		
		detailPanel.repaint();
		inputPosition.addChangeListener(this);
		
		
//		if (morph == editedMorph) {
//			editButton.setSelected(true);
//			deleteButton.setEnabled(false);
//			slider.setEnabled(false);
//		} else if (editedMorph != null) {
//			editButton.setEnabled(false);
//		}
	}
	
//	public void edit() {
//		editButton.doClick();
//	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		System.out.println(changeEvent);
		if (changeEvent.getSource() == inputPosition) {
			morphTarget.getMorph().changeTargetPosition(morphTarget, inputPosition.getFloatValue());
			MainFrame.getInstance().selectTreeNode(morphTarget);
		}
	}
	
	public JToggleButton getEditButton() {
		return editButton;
	}
	
//	public void editMorph() {
//		slider.setValue(morph.getSliderValue());
//		slider.setEnabled(false);
//		deleteButton.setEnabled(false);
//	}
//	
//	public void editMorphDone() {
//		slider.setEnabled(true);
//		deleteButton.setEnabled(true);
//	}
}

