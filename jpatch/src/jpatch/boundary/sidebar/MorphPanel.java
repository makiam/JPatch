package jpatch.boundary.sidebar;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.boundary.action.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public class MorphPanel extends SidePanel
implements ChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1319926408036866674L;
	JPatchInput inputName;
	JPanel panelMinMax = new JPanel();
	JPatchInput inputMin;
	JPatchInput inputMax;
	JSlider slider;
	MorphTarget morph;
	AbstractButton editButton;
	AbstractButton deleteButton;
	
	public MorphPanel(MorphTarget morph) {
		this.morph = morph;
		MorphTarget editedMorph = MainFrame.getInstance().getEditedMorph();
		deleteButton = new JPatchButton(new DeleteMorphAction(morph));
		editButton = new JPatchToggleButton(new EditMorphAction(morph, this));
		add(editButton);
		add(deleteButton);
		
		//JPatchSlider.setDimensions(0,150,50,20);
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",morph.getName());
		panelMinMax.setLayout(new BoxLayout(panelMinMax,BoxLayout.X_AXIS));
		JPatchInput.setDimensions(50,50,20);
		inputMin = new JPatchInput("Min:",morph.getMin());
		inputMax = new JPatchInput("Max:",morph.getMax());
		panelMinMax.add(inputMin);
		panelMinMax.add(inputMax);
		detailPanel.removeAll();
		detailPanel.add(inputName);
		detailPanel.add(panelMinMax);
		slider = new JSlider(JSlider.HORIZONTAL,0,100,morph.getSliderValue());
		slider.setFocusable(false);
		detailPanel.add(slider);
		detailPanel.repaint();
		inputName.addChangeListener(this);
		inputMin.addChangeListener(this);
		inputMax.addChangeListener(this);
		slider.addChangeListener(this);
		
		if (morph == editedMorph) {
			editButton.setSelected(true);
			deleteButton.setEnabled(false);
			slider.setEnabled(false);
		} else if (editedMorph != null) {
			editButton.setEnabled(false);
		}
	}
	
	public void edit() {
		editButton.doClick();
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		if (changeEvent.getSource() == inputName) {
			morph.setName(inputName.getStringValue());
			((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(morph);
		} else if (changeEvent.getSource() == inputMin) {
			morph.setMin(inputMin.getFloatValue());
			if (inputMin.getFloatValue() > morph.getValue()) {
				morph.unapply();
				morph.setValue(inputMin.getFloatValue());
				morph.apply();
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
			slider.setValue(morph.getSliderValue());
		} else if (changeEvent.getSource() == inputMax) {
			morph.setMax(inputMax.getFloatValue());
			if (inputMax.getFloatValue() < morph.getValue()) {
				morph.unapply();
				morph.setValue(inputMax.getFloatValue());
				morph.apply();
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
			slider.setValue(morph.getSliderValue());
		} else if (changeEvent.getSource() == slider) {
			if (slider.getValueIsAdjusting()) {
				morph.unapply();
				morph.setSliderValue(slider.getValue());
				morph.apply();
				MainFrame.getInstance().getJPatchScreen().update_all();
			} else {
			}
		}
	}
	
	public void editMorph() {
		slider.setValue(morph.getSliderValue());
		slider.setEnabled(false);
		deleteButton.setEnabled(false);
	}
	
	public void editMorphDone() {
		slider.setEnabled(true);
		deleteButton.setEnabled(true);
	}
}

