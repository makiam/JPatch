package com.jpatch.afw.ui;

import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;

import com.jpatch.afw.attributes.*;

public class AttributeEditor {

	
	private final Class entityClass;
	private Object entity;
	private final Stack<JPatchFormContainer> containerStack = new Stack<JPatchFormContainer>();
	private JPatchForm form = new JPatchForm();
	private final List<ComponentBinding> bindings = new ArrayList<ComponentBinding>();
	
	public AttributeEditor(Class entityClass, String name) {
		this.entityClass = entityClass;
		startContainer(name);
	}
	
	private Method getAttributeMethod(String name) {
		try {
			return entityClass.getMethod("get" + name + "Attribute", (Class[]) null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Attribute getAttribute(String name) {
		try {
			return (Attribute) getAttributeMethod(name).invoke(entity, (Object[]) null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public JPatchFormContainer getRootContainer() {
		return containerStack.firstElement();
	}
	
	public void startContainer(String name) {
		JPatchFormContainer formContainer = new JPatchFormContainer(name);
		form = new JPatchForm();
		formContainer.add(form);
		if (!containerStack.isEmpty()) {
			containerStack.peek().add(formContainer);
		}
		containerStack.push(formContainer);
	}
	
	public void endContainer() {
		containerStack.pop();
	}
	
	public void addField(String label, String attributeName) {
		Attribute attribute = getAttribute(attributeName);
		if (attribute instanceof Tuple3Attr) {
			JTextField x = new JTextField();
			JTextField y = new JTextField();
			JTextField z = new JTextField();
			form.addRow(new JLabel(label), x, y, z);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), x, y, z));
		} else if (attribute instanceof Tuple2Attr) {
			JTextField x = new JTextField();
			JTextField y = new JTextField();
			form.addRow(new JLabel(label), x, y);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), x, y));
		} else if (attribute instanceof BooleanAttr) {
			JCheckBox checkBox = new JCheckBox();
			form.addRow(new JLabel(label), checkBox);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), checkBox));
		} else if (attribute instanceof StateMachine) {
			JComboBox comboBox = new JComboBox();
			form.addRow(new JLabel(label), comboBox);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), comboBox));
		} else {
			JTextField textField = new JTextField();
			form.addRow(new JLabel(label), textField);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), textField));
		}
	}
	
	private void addBinding(ComponentBinding binding) {
		bindings.add(binding);
		bind(binding);
	}
	
	public void setEntity(Object entity) {
		this.entity = entity;
		for (ComponentBinding binding : bindings) {
			bind(binding);
		}
	}
	
	private void bind(ComponentBinding binding) {
		Attribute attribute;
		try {
			attribute = (Attribute) binding.getAttributeMethod.invoke(entity, (Object[]) null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		/* unbind all components */
		for (JComponent component : binding.components) {
			AttributeManager.getInstance().unbind(component);
		}
		
		/* bind components */
		if (attribute instanceof Tuple3Attr) {
			AttributeManager.getInstance().bindTextFieldToAttribute((JTextField) binding.components[0], ((Tuple3Attr) attribute).getXAttr());
			AttributeManager.getInstance().bindTextFieldToAttribute((JTextField) binding.components[1], ((Tuple3Attr) attribute).getYAttr());
			AttributeManager.getInstance().bindTextFieldToAttribute((JTextField) binding.components[2], ((Tuple3Attr) attribute).getZAttr());
		} else if (attribute instanceof Tuple2Attr) {
			AttributeManager.getInstance().bindTextFieldToAttribute((JTextField) binding.components[0], ((Tuple3Attr) attribute).getXAttr());
			AttributeManager.getInstance().bindTextFieldToAttribute((JTextField) binding.components[1], ((Tuple3Attr) attribute).getYAttr());
		} else if (attribute instanceof BooleanAttr) {
			AttributeManager.getInstance().bindCheckBoxToAttribute((JCheckBox) binding.components[0], (BooleanAttr) attribute);
		} else if (attribute instanceof StateMachine) {
			AttributeManager.getInstance().bindComboBoxToAttribute((JComboBox) binding.components[0], (StateMachine) attribute);
		} else {
			AttributeManager.getInstance().bindTextFieldToAttribute((JTextField) binding.components[0], ((Tuple3Attr) attribute).getXAttr());
			if (binding.components.length == 2) {
				AttributeManager.getInstance().bindSliderToAttribute((JSlider) binding.components[1], (DoubleAttr) attribute, IdentityMapping.getInstance());
			}
		}
	}
	
	private static class ComponentBinding {
		final Method getAttributeMethod;
		final JComponent[] components;
		ComponentBinding(Method getAttributeMethod, JComponent... components) {
			this.getAttributeMethod = getAttributeMethod;
			this.components = components.clone();
		}
	}
}
