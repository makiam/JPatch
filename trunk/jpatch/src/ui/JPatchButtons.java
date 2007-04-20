package ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jpatch.entity.attributes2.Attribute;
import jpatch.entity.attributes2.AttributeListener;
import jpatch.entity.attributes2.StateMachine;
import jpatch.entity.attributes2.Toggle;


public class JPatchButtons {
	public static void configureRadioButton(final AbstractButton button, final StateMachine stateMachine, final Object state) {
		button.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				button.setSelected(stateMachine.getState() == state);
			}
		});
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (stateMachine.getState() == state) {
					stateMachine.revertToDefault();
				} else {
					stateMachine.setState(state);
				}
			}
		});
		stateMachine.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute source) {
				button.setSelected(stateMachine.getState() == state);
			}
		});
		button.setSelected(stateMachine.getState() == state);
	}
	
	public static void configureToggleButton(final AbstractButton button, final Toggle toggle) {
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggle.setState(!toggle.getState());
			}
		});
		toggle.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute source) {
				button.setSelected(toggle.getState());
			}
		});
		button.setSelected(toggle.getState());
	}
	
}
