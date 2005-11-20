package jpatch.boundary.sidebar;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.boundary.*;
import jpatch.boundary.action.AddMultiControlPointAction;
import jpatch.boundary.action.DeleteDofAction;
import jpatch.boundary.action.DeleteMaterialAction;
import jpatch.boundary.action.EditMorphTargetAction;
import jpatch.boundary.action.NewMorphTargetAction;
import jpatch.boundary.action.UseDofMorphAction;
import jpatch.entity.*;

public class DofPanel extends SidePanel
implements ChangeListener, ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1319926408036866674L;
	JPatchInput inputName;
	JPanel panelMinMax = new JPanel();
	JPatchInput inputMin;
	JPatchInput inputMax;
	JPatchInput inputCurrent;
	JSlider slider;
	JComboBox combo = new JComboBox(RotationDof.MODES);
	JCheckBox cbFlip = new JCheckBox("flip axis");
	RotationDof dof;
	
	AbstractButton useMorphButton;
	AbstractButton discardMorphButton;
	AbstractButton addTargetButton;
	
//	AbstractButton editButton;
//	AbstractButton deleteButton;
	
	public DofPanel(final RotationDof dof) {
		this.dof = dof;
		combo.setSelectedIndex(dof.getMode());
//		Morph editedMorph = MainFrame.getInstance().getEditedMorph();
//		deleteButton = new JPatchButton("delete");//new DeleteDofAction(dof));
//		editButton = new JPatchToggleButton(new EditMorphAction(morph, this));
//		add(editButton);
//		add(deleteButton);
		add(new JPatchButton(new DeleteDofAction(dof)));
		useMorphButton = new JPatchButton(new UseDofMorphAction(dof, true));
		discardMorphButton = new JPatchButton(new UseDofMorphAction(dof, false));
		addTargetButton = new JPatchButton(new NewMorphTargetAction(dof));
		
		add(useMorphButton);
		add(discardMorphButton);
		add(addTargetButton);
		
		useMorphButton.setEnabled(dof.getChildCount() == 0);
		discardMorphButton.setEnabled(dof.getChildCount() > 0);
		addTargetButton.setEnabled(dof.getChildCount() > 0 && !dof.isTarget());
		
//		add(new JPatchToggleButton(new EditMorphAction(dof.getMorph(), "edit morph")));
		//JPatchSlider.setDimensions(0,150,50,20);
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",dof.getName());
//		panelMinMax.setLayout(new BoxLayout(panelMinMax,BoxLayout.X_AXIS));
//		JPatchInput.setDimensions(100,100,20);
		inputMin = new JPatchInput("Min:",dof.getMin());
		inputMax = new JPatchInput("Max:",dof.getMax());
		inputCurrent = new JPatchInput("Value:",dof.getValue());
//		panelMinMax.add(inputMin);
//		panelMinMax.add(inputMax);
		detailPanel.removeAll();
//		detailPanel.add(inputName);
		detailPanel.add(combo);
		detailPanel.add(cbFlip);
		detailPanel.add(inputMin);
		detailPanel.add(inputMax);
		detailPanel.add(inputCurrent);
		slider = new JSlider(JSlider.HORIZONTAL,0,100,dof.getSliderValue());
		slider.setFocusable(false);
		detailPanel.add(slider);
		detailPanel.repaint();
		inputName.addChangeListener(this);
		inputMin.addChangeListener(this);
		inputMax.addChangeListener(this);
		slider.addChangeListener(this);
		combo.addActionListener(this);
		cbFlip.setSelected(dof.isFlipped());
		cbFlip.addActionListener(this);
		
		JButton buttonUp = new JButton("up");
		buttonUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = dof.getBone().getDofIndex(dof);
				if (i > 0) {
					MainFrame.getInstance().getTreeModel().removeNodeFromParent(dof);
					MainFrame.getInstance().getTreeModel().insertNodeInto(dof, dof.getBone(), i - 1);
					MainFrame.getInstance().getModel().setPose();
					MainFrame.getInstance().getJPatchScreen().update_all();
				}
			}
		});
		
		JButton buttonDown = new JButton("down");
		buttonDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = dof.getBone().getDofIndex(dof);
				if (i < 2) {
					MainFrame.getInstance().getTreeModel().removeNodeFromParent(dof);
					MainFrame.getInstance().getTreeModel().insertNodeInto(dof, dof.getBone(), i + 1);
					MainFrame.getInstance().getModel().setPose();
					MainFrame.getInstance().getJPatchScreen().update_all();
				}
			}
		});
		
		add(buttonUp);
		add(buttonDown);
		
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
			dof.setMin(inputMin.getFloatValue());
			if (dof.getMin() > dof.getValue()) {
				dof.setValue(dof.getValue());
			}
			slider.setValue(dof.getSliderValue());
		} else if (changeEvent.getSource() == inputMax) {
			dof.setMax(inputMax.getFloatValue());
			if (dof.getMax() < dof.getValue()) {
				dof.setValue(dof.getMax());
				}
			slider.setValue(dof.getSliderValue());
		} else if (changeEvent.getSource() == inputCurrent) {
			float v = inputCurrent.getFloatValue();
//			if (v < morph.getMin())
//				v = morph.getMin();
//			if (v > morph.getMax())
//				v = morph.getMax();
//			inputCurrent.setValue(v);
			if (v != dof.getValue()) {
				dof.setValue(v);
				MainFrame.getInstance().getJPatchScreen().update_all();
				addTargetButton.setEnabled(dof.getChildCount() > 0 && !dof.isTarget());
				slider.setValue(dof.getSliderValue());
			}
		} else if (changeEvent.getSource() == slider) {
			if (slider.getValueIsAdjusting()) {
				dof.setSliderValue(slider.getValue());
				inputCurrent.setValue(dof.getValue());
				addTargetButton.setEnabled(dof.getChildCount() > 0 && !dof.isTarget());
				MainFrame.getInstance().getModel().setPose();
				MainFrame.getInstance().getJPatchScreen().update_all();
			} else {
			}
		} 
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == combo) {
			dof.setMode(combo.getSelectedIndex());
		} else if (actionEvent.getSource() == cbFlip) {
			dof.setFlipped(cbFlip.isSelected());
			dof.invalidate();
			MainFrame.getInstance().getModel().setPose();
			MainFrame.getInstance().getJPatchScreen().update_all();
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

