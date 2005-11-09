package jpatch.boundary.sidebar;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.boundary.action.DeleteDofAction;
import jpatch.boundary.action.DeleteMaterialAction;
import jpatch.boundary.action.EditMorphTargetAction;
import jpatch.entity.*;

public class DofPanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1319926408036866674L;
	JPatchInput inputName;
	JPanel panelMinMax = new JPanel();
	JPatchInput inputMin;
	JPatchInput inputMax;
	JPatchInput inputX, inputY, inputZ;
	JSlider slider;
	RotationDof dof;
//	AbstractButton editButton;
//	AbstractButton deleteButton;
	
	public DofPanel(RotationDof dof) {
		this.dof = dof;
//		Morph editedMorph = MainFrame.getInstance().getEditedMorph();
//		deleteButton = new JPatchButton("delete");//new DeleteDofAction(dof));
//		editButton = new JPatchToggleButton(new EditMorphAction(morph, this));
//		add(editButton);
//		add(deleteButton);
		add(new JPatchButton(new DeleteDofAction(dof)));
//		add(new JPatchToggleButton(new EditMorphAction(dof.getMorph(), "edit morph")));
		//JPatchSlider.setDimensions(0,150,50,20);
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",dof.getName());
		panelMinMax.setLayout(new BoxLayout(panelMinMax,BoxLayout.X_AXIS));
		JPatchInput.setDimensions(50,50,20);
		inputMin = new JPatchInput("Min:",dof.getMinAngle() * 180 / (float) Math.PI);
		inputMax = new JPatchInput("Max:",dof.getMaxAngle() * 180 / (float) Math.PI);
		panelMinMax.add(inputMin);
		panelMinMax.add(inputMax);
		inputX = new JPatchInput("X:", dof.getAxis().x);
		inputY = new JPatchInput("Y:", dof.getAxis().y);
		inputZ = new JPatchInput("Z:", dof.getAxis().z);
		detailPanel.removeAll();
		detailPanel.add(inputX);
		detailPanel.add(inputY);
		detailPanel.add(inputZ);
		detailPanel.add(inputName);
		detailPanel.add(panelMinMax);
		slider = new JSlider(JSlider.HORIZONTAL,0,100,dof.getSliderValue());
		slider.setFocusable(false);
		detailPanel.add(slider);
		detailPanel.repaint();
		inputName.addChangeListener(this);
		inputMin.addChangeListener(this);
		inputMax.addChangeListener(this);
		inputX.addChangeListener(this);
		inputY.addChangeListener(this);
		inputZ.addChangeListener(this);
		slider.addChangeListener(this);
		
		
		
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
		if (changeEvent.getSource() == inputName) {
			dof.setName(inputName.getStringValue());
			((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(dof);
		} else if (changeEvent.getSource() == inputMin) {
			dof.setMinAngle(inputMin.getFloatValue() / 180 * (float) Math.PI);
			if (dof.getMinAngle() > dof.getCurrentAngle()) {
				dof.setCurrentAngle(dof.getMinAngle());
			}
			slider.setValue(dof.getSliderValue());
		} else if (changeEvent.getSource() == inputMax) {
			dof.setMaxAngle(inputMax.getFloatValue() / 180 * (float) Math.PI);
			if (dof.getMaxAngle() < dof.getCurrentAngle()) {
				dof.setCurrentAngle(dof.getMaxAngle());
				}
			slider.setValue(dof.getSliderValue());
		} else if (changeEvent.getSource() == slider) {
			if (slider.getValueIsAdjusting()) {
//				for (Iterator it = MainFrame.getInstance().getModel().getBoneSet().iterator(); it.hasNext(); ) {
//					Bone bone = (Bone) it.next();
//					bone.setReferencePose();
//				}
				dof.setSliderValue(slider.getValue());
//				for (Iterator it = MainFrame.getInstance().getModel().getBoneSet().iterator(); it.hasNext(); ) {
//					Bone bone = (Bone) it.next();
//					bone.setPose();
//				}
				MainFrame.getInstance().getModel().setPose();
				MainFrame.getInstance().getJPatchScreen().update_all();
			} else {
			}
		} else if (changeEvent.getSource() == inputX) {
			Vector3f v = dof.getAxis();
			v.x = inputX.getFloatValue();
			dof.setAxis(v);
		} else if (changeEvent.getSource() == inputY) {
			Vector3f v = dof.getAxis();
			v.y = inputY.getFloatValue();
			dof.setAxis(v);
		} else if (changeEvent.getSource() == inputZ) {
			Vector3f v = dof.getAxis();
			v.z = inputZ.getFloatValue();
			dof.setAxis(v);
		}
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

