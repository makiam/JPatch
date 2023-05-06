package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.icons.IconSet;
import com.jpatch.boundary.*;
import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class AttributeEditor {
	
	private static final Icon LOWER_LIMIT = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/SET_LOWER_LIMIT.png"));
	private static final Icon UPPER_LIMIT = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/SET_UPPER_LIMIT.png"));
	private static final Icon CLEAR_LIMIT = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/CLEAR_LIMIT.png"));
	
	private final Class entityClass;
	private Object entity;
	private Object currentObject;
	private final Stack<Method> objectStack = new Stack<Method>();
	private final Stack<JPatchFormContainer> containerStack = new Stack<JPatchFormContainer>();
	private JPatchForm form = new JPatchForm();
	private final List<ComponentBinding> bindings = new ArrayList<ComponentBinding>();
	private final List<SpecialBinding> specialBindings = new ArrayList<SpecialBinding>();
	private String falseString = null;
	private String trueString = null;
	private Mapping mapping = IdentityMapping.getInstance();
	
	public AttributeEditor(Class entityClass, String name, BooleanAttr expansionControl, Object entity, Color borderColor) {
		this.entityClass = entityClass;
		this.entity = this.currentObject = entity;
		startContainer(name, expansionControl);
		containerStack.peek().setRootBorderColor(borderColor);
	}
	
	private Method getAttributeMethod(String name) {
		try {
			return entityClass.getMethod("get" + name + "Attribute", (Class[]) null);
		} catch (Exception e) {
			throw new RuntimeException("class:" + entityClass + " method:" + "get" + name + "Attribute", e);
		}
	}
	
	private Attribute getAttribute(String name) {
		try {
			return (Attribute) getAttributeMethod(name).invoke(currentObject, (Object[]) null);
		} catch (Exception e) {
			throw new RuntimeException("object:" + currentObject + " method:" + getAttributeMethod(name), e);
		}
	}
	
	public JPatchFormContainer getRootContainer() {
		return containerStack.firstElement();
	}
	
	public void setBooleanValues(String falseString, String trueString) {
		this.falseString = falseString;
		this.trueString = trueString;
	}
	
	
	public void startContainer(String name, BooleanAttr expansionControl) {
		JPatchFormContainer formContainer = new JPatchFormContainer(name, expansionControl);
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
	
	public void addField(JLabel label, String attributeName) {
		Attribute attribute = getAttribute(attributeName);
		if (attribute instanceof Tuple3Attr) {
			JTextField x = new JTextField();
			JTextField y = new JTextField();
			JTextField z = new JTextField();
			form.addRow(label, x, y, z);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), x, y, z));
		} else if (attribute instanceof Tuple2Attr) {
			JTextField x = new JTextField();
			JTextField y = new JTextField();
			form.addRow(label, x, y);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), x, y));
		} else if (attribute instanceof BooleanAttr) {
			if (falseString == null || trueString == null) {
				JCheckBox checkBox = new JCheckBox();
				form.addRow(label, checkBox);
				addBinding(new ComponentBinding(getAttributeMethod(attributeName), checkBox));
			} else {
				Switcher switcher = new Switcher(falseString, trueString);
				form.addRow(label, switcher);
				addBinding(new ComponentBinding(getAttributeMethod(attributeName), switcher.asAbstractButton()));
			}
		} else if (attribute instanceof StateMachine) {
			JComboBox comboBox = new JComboBox();
			comboBox.setOpaque(false);
			form.addRow(label, comboBox);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), comboBox));
		} else {
			JTextField textField = new JTextField();
			form.addRow(label, textField);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), textField));
		}
	}
	
	public void addSlider(JLabel label, String attributeName) {
		
			JTextField textField = new JTextField();
			JSlider slider = new JSlider();
			form.addRow(label, textField, slider);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), textField, slider));
		
	}
	
	public void addCombo(JLabel label, String attributeName) {
			JComboBox comboBox = new JComboBox();
			form.addRow(label, comboBox);
			addBinding(new ComponentBinding(getAttributeMethod(attributeName), comboBox));
	}
	
	public void addLimits(String attributeName) {
		JButton[] setButtons = new JButton[6];
		JButton[] clrButtons = new JButton[6];
		JTextField[] textFields = new JTextField[6];
		JComponent[] boxes = new JComponent[6];
		ButtonUtils buttonUtils = new ButtonUtils();
		for (int i = 0; i < 6; i++) {
			setButtons[i] = new JButton();
			setButtons[i].setToolTipText(i % 2 == 0 ? "set maximum value" : "set minimum value");
			clrButtons[i] = new JButton();
			clrButtons[i].setToolTipText(i % 2 == 0 ? "clear maximum value" : "clear minimum value");
			textFields[i] = new JTextField();
			boxes[i] = Box.createHorizontalBox();
			boxes[i].add(setButtons[i]);
			boxes[i].add(clrButtons[i]);
			buttonUtils.configureButton(setButtons[i], IconSet.Style.TINY, IconSet.Type.LEFT, i % 2 == 0 ? UPPER_LIMIT : LOWER_LIMIT);
			buttonUtils.configureButton(clrButtons[i], IconSet.Style.TINY, IconSet.Type.RIGHT, CLEAR_LIMIT);
		}
		
		
		JComponent[] components = new JComponent[18];
		for (int i = 0; i < components.length; i++) {
			if (i < 6) components[i] = textFields[i];
			else if (i < 12) components[i] = setButtons[i - 6];
			else components[i] = clrButtons[i - 12];
		}
		form.addRow(new JLabel("MAXIMUM"), textFields[0], textFields[2], textFields[4]);
		form.addRow(new JLabel("SET/CLR"), boxes[0], boxes[2], boxes[4]);
		addField(new JLabel("Current"), attributeName);
		form.addRow(new JLabel("SET/CLR"), boxes[1], boxes[3], boxes[5]);
		form.addRow(new JLabel("MINIMUM"), textFields[1], textFields[3], textFields[5]);
		addBinding(new ComponentBinding(getAttributeMethod(attributeName), components));
	}
	
	private void addBinding(ComponentBinding binding) {
		bindings.add(binding);
		bind(binding);
	}
	
	public void addSpecialBinding(SpecialBinding specialBinding) {
		specialBindings.add(specialBinding);
		if (specialBinding instanceof SpecialBinding.CustomComponent) {
			containerStack.peek().add(((SpecialBinding.CustomComponent)specialBinding).getComponent());
		} else if (specialBinding instanceof SpecialBinding.FormContainer) {
			JPatchFormContainer formContainer = ((SpecialBinding.FormContainer) specialBinding).getFormContainer();
			if (!containerStack.isEmpty()) {
				containerStack.peek().add(formContainer);
			}
		} else {
			throw new AssertionError("should never get here");
		}
	}
	
	public void setEntity(Object entity) {
		System.out.println("*** setEntity called, entity=" + entity);
		this.entity = entity;
		getRootContainer().getComponent().setVisible(false);	// otherwise Swing would repaint each component individually
		for (ComponentBinding binding : bindings) {
			bind(binding);
		}
		for (SpecialBinding specialBinding : specialBindings) {
			specialBinding.bindTo(entity);
		}
		getRootContainer().getComponent().setVisible(true);
	}
	
	private void bind(ComponentBinding binding) {
		Attribute attribute;
		try {
			attribute = binding.getAttribute();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		/* unbind all components */
		for (JComponent component : binding.components) {
			AttributeManager.getInstance().unbind(component);
		}
		
		/* bind components */
		if (binding.components.length == 18) {
			/* limit */
			Tuple3Attr attr = (Tuple3Attr) attribute;
			JComponent[] components = binding.components;
			AttributeManager.getInstance().bindLimit(entity, attr.getXAttr(), DoubleMaximum.class, (JButton) components[6], (JButton) components[12], (JTextField) components[0]);
			AttributeManager.getInstance().bindLimit(entity, attr.getXAttr(), DoubleMinimum.class, (JButton) components[7], (JButton) components[13], (JTextField) components[1]);
			AttributeManager.getInstance().bindLimit(entity, attr.getYAttr(), DoubleMaximum.class, (JButton) components[8], (JButton) components[14], (JTextField) components[2]);
			AttributeManager.getInstance().bindLimit(entity, attr.getYAttr(), DoubleMinimum.class, (JButton) components[9],(JButton) components[15], (JTextField) components[3]);
			AttributeManager.getInstance().bindLimit(entity, attr.getZAttr(), DoubleMaximum.class, (JButton) components[10], (JButton) components[16], (JTextField) components[4]);
			AttributeManager.getInstance().bindLimit(entity, attr.getZAttr(), DoubleMinimum.class, (JButton) components[11], (JButton) components[17], (JTextField) components[5]);
		} else if (attribute instanceof Tuple3Attr) {
			AttributeManager.getInstance().bindTextFieldToAttribute(entity, (JTextField) binding.components[0], ((Tuple3Attr) attribute).getXAttr());
			AttributeManager.getInstance().bindTextFieldToAttribute(entity, (JTextField) binding.components[1], ((Tuple3Attr) attribute).getYAttr());
			AttributeManager.getInstance().bindTextFieldToAttribute(entity, (JTextField) binding.components[2], ((Tuple3Attr) attribute).getZAttr());
		} else if (attribute instanceof Tuple2Attr) {
			AttributeManager.getInstance().bindTextFieldToAttribute(entity, (JTextField) binding.components[0], ((Tuple2Attr) attribute).getXAttr());
			AttributeManager.getInstance().bindTextFieldToAttribute(entity, (JTextField) binding.components[1], ((Tuple2Attr) attribute).getYAttr());
		} else if (attribute instanceof BooleanAttr) {
			AttributeManager.getInstance().bindButtonToAttribute(entity, (AbstractButton) binding.components[0], (BooleanAttr) attribute);
		} else if (attribute instanceof StateMachine) {
			AttributeManager.getInstance().bindComboBoxToAttribute(entity, (JComboBox) binding.components[0], (StateMachine<?>) attribute);
		} else if (attribute instanceof ScalarAttribute){
			if (binding.components[0] instanceof JComboBox) {
				AttributeManager.getInstance().bindComboBoxToAttribute(entity, (JComboBox) binding.components[0], (IntAttr) attribute);
			} else {
				AttributeManager.getInstance().bindTextFieldToAttribute(entity, (JTextField) binding.components[0], (ScalarAttribute) attribute);
				if (binding.components.length == 2) {
					if (attribute instanceof IntAttr) {
						AttributeManager.getInstance().bindSliderToAttribute(entity, (JSlider) binding.components[1], (IntAttr) attribute);
					} else if (attribute instanceof DoubleAttr) {
						AttributeManager.getInstance().bindSliderToAttribute(entity, (JSlider) binding.components[1], (DoubleAttr) attribute, binding.mapping);
					} else {
						throw new IllegalStateException();
					}
				}
			}
		} else {
			throw new IllegalStateException();
		}
	}
	
	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}
	
	private class ComponentBinding {
		final Method[] getObjectMethod;
		final Method getAttributeMethod;
		final JComponent[] components;
		final Mapping mapping = AttributeEditor.this.mapping;
		ComponentBinding(Method getAttributeMethod, JComponent... components) {
			this.getObjectMethod = objectStack.toArray(new Method[objectStack.size()]);
			this.getAttributeMethod = getAttributeMethod;
			this.components = components.clone();
		}
		
		Attribute getAttribute() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			Object object = entity;
			for (Method method : getObjectMethod) {
				object = method.invoke(object, (Object[]) null);
			}
			return (Attribute) getAttributeMethod.invoke(object, (Object[]) null);
		}
	}
}
