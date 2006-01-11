package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.boundary.action.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public class MorphPanel extends SidePanel
implements ChangeListener, Morph.MorphListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1319926408036866674L;
	JPatchInput inputName;
//	JPanel panelMinMax = new JPanel();
	JPatchInput inputMin;
	JPatchInput inputMax;
	JPatchInput inputCurrent;
	JSlider slider;
	Morph morph;
	AbstractButton addTargetButton;
	AbstractButton deleteButton;
	
	public MorphPanel(Morph morph) {
		this.morph = morph;
		MorphTarget editedMorph = MainFrame.getInstance().getEditedMorph();
		deleteButton = new JPatchButton(new DeleteMorphAction(morph));
		addTargetButton = new JPatchButton(new NewMorphTargetAction(morph));
		
		
		
		addTargetButton.setEnabled(!morph.isTarget());
		//JPatchSlider.setDimensions(0,150,50,20);
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",morph.getName());
//		panelMinMax.setLayout(new BoxLayout(panelMinMax,BoxLayout.X_AXIS));
//		JPatchInput.setDimensions(100,100,20);
		inputMin = new JPatchInput("Min:",morph.getMin());
		inputMax = new JPatchInput("Max:",morph.getMax());
		inputCurrent = new JPatchInput("Value:",morph.getValue());
//		panelMinMax.add(inputMin);
//		panelMinMax.add(inputMax);
		detailPanel.removeAll();
		
//		detailPanel.add(panelMinMax);
		slider = new JSlider(JSlider.HORIZONTAL,0,100,morph.getSliderValue());
		slider.setFocusable(false);
//		detailPanel.add(slider);
		detailPanel.repaint();
		inputName.addChangeListener(this);
		inputMin.addChangeListener(this);
		inputMax.addChangeListener(this);
		inputCurrent.addChangeListener(this);
		slider.addChangeListener(this);
		morph.addMorphListener(this);
//		if (morph == editedMorph) {
//			editButton.setSelected(true);
//			deleteButton.setEnabled(false);
//			slider.setEnabled(false);
//		} else if (editedMorph != null) {
//			editButton.setEnabled(false);
//		}
		
		
		if (MainFrame.getInstance().getAnimation() != null) {
			inputName.setEnabled(false);
			inputMin.setEnabled(false);
			inputMax.setEnabled(false);
		} else {
			add(deleteButton);
			add(addTargetButton);
			detailPanel.add(inputName);
		}
		detailPanel.add(inputMin);
		detailPanel.add(inputMax);
		detailPanel.add(inputCurrent);
		
	}
	
//	public void edit() {
//		editButton.doClick();
//	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		if (changeEvent.getSource() == inputName) {
			morph.setName(inputName.getStringValue());
			((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(morph);
		} else if (changeEvent.getSource() == inputMin) {
			morph.setMin(inputMin.getFloatValue());
			if (inputMin.getFloatValue() > morph.getValue()) {
//				morph.unapply();
				morph.setValue(inputMin.getFloatValue());
//				morph.apply();
				MainFrame.getInstance().getJPatchScreen().update_all();
				MainFrame.getInstance().getTree().repaint();
			}
			slider.setValue(morph.getSliderValue());
		} else if (changeEvent.getSource() == inputMax) {
			morph.setMax(inputMax.getFloatValue());
			if (inputMax.getFloatValue() < morph.getValue()) {
//				morph.unapply();
				morph.setValue(inputMax.getFloatValue());
//				morph.apply();
				MainFrame.getInstance().getJPatchScreen().update_all();
				MainFrame.getInstance().getTree().repaint();
			}
			slider.setValue(morph.getSliderValue());
		} else if (changeEvent.getSource() == inputCurrent) {
			float v = inputCurrent.getFloatValue();
//			if (v < morph.getMin())
//				v = morph.getMin();
//			if (v > morph.getMax())
//				v = morph.getMax();
//			inputCurrent.setValue(v);
			if (v != morph.getValue()) {
				morph.setValue(v);
				MainFrame.getInstance().getJPatchScreen().update_all();
				slider.setValue(morph.getSliderValue());
				addTargetButton.setEnabled(!morph.isTarget());
				MainFrame.getInstance().getTree().repaint();
				if (MainFrame.getInstance().getAnimation() != null)
					morph.updateCurve();
			}
		} else if (changeEvent.getSource() == slider) {
			if (slider.getValueIsAdjusting()) {
//				morph.unapply();
				morph.setSliderValue(slider.getValue());
				inputCurrent.setValue(morph.getValue());
//				morph.apply();
				MainFrame.getInstance().getJPatchScreen().update_all();
				addTargetButton.setEnabled(!morph.isTarget());
			} else {
			}
		}
	}

	public void removeNotify() {
		super.removeNotify();
		morph.removeMorphListener(this);
	}
	
	public void valueChanged(Morph morph) {
		inputCurrent.setValue(morph.getValue());
		addTargetButton.setEnabled(!morph.isTarget());
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

