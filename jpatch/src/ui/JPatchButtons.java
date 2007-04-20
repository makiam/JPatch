package ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;

import jpatch.entity.attributes2.Attribute;
import jpatch.entity.attributes2.AttributeListener;
import jpatch.entity.attributes2.StateMachine;
import jpatch.entity.attributes2.Toggle;


public class JPatchButtons {
	public void configureRadioButton(final AbstractButton button, final StateMachine stateMachine, final Object state) {
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stateMachine.setState(state);
			}
		});
		stateMachine.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute source) {
				button.setSelected(stateMachine.getState() == state);
			}
		});
	}
	
	public void configureToggleButton(final AbstractButton button, final Toggle toggle) {
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
	}
	
}
