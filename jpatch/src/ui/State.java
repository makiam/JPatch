package ui;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import jpatch.entity.attributes2.AbstractAttribute;

public class State<T extends Enum> extends AbstractAttribute {
	private T state;
	private Map<AbstractButton, T> buttonMap = new HashMap<AbstractButton, T>();
	private Map<T, AbstractButton> stateMap = new HashMap<T, AbstractButton>();
	private ButtonGroup buttonGroup = new ButtonGroup();
	private ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			T newState = buttonMap.get(e.getSource());
			if (newState != state) {
				state = newState;
				fireAttributeChanged();
			}
		}
	};
	
	public State(T initialState) {
		state = initialState;
	}
	
	public T getState() {
		return state;		
	}
	
	public void enableState(T state, boolean enable) {
		stateMap.get(state).getAction().setEnabled(enable);
	}
	
	public boolean isStateEnabled(T state) {
		return stateMap.get(state).getAction().isEnabled();
	}
	
	public void setState(T state) {
		buttonGroup.setSelected(stateMap.get(state).getModel(), true);
		if (state != this.state) {
			this.state = state;
			fireAttributeChanged();
		}
	}
	
	public void addButton(AbstractButton button, T state) {
		buttonMap.put(button, state);
		stateMap.put(state, button);
		button.addActionListener(actionListener);
		buttonGroup.add(button);
	}
	
	public void removeButton(AbstractButton button) {
		if (!buttonMap.containsKey(button)) {
			throw new IllegalArgumentException(button + " is not registered to " + this);
		}
		stateMap.remove(buttonMap.get(button));
		buttonMap.remove(button);
		buttonGroup.remove(button);
		button.removeActionListener(actionListener);
	}
}
