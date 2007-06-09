package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.boundary.Main;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

public class AttributeUiHelper {
	private static final DecimalFormat INT_FORMAT = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final int COLUMNS = 6;
	
	/**
	 * An actionListener that transfers the focus of the component that fired the actionEvent
	 * (needed for textFields)
	 */
	private static final ActionListener TRANSFER_FOCUS_ACTIONLISTENER = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			((JComponent) e.getSource()).transferFocus();
		};
	};
	
	public static JComboBox bindComboBoxToAttribute(final JComboBox comboBox, final StateMachine stateMachine) {
		return comboBox;
	}
	
	/**
	 * Binds the specified JCheckBox to the specified Attribute.
	 * @param checkBox
	 * @param booleanAttr
	 * @return the specified JCheckBox
	 * @throws NullPointerException if any of the specified parameters is null
	 */
	public static JCheckBox bindCheckBoxToAttribute(final JCheckBox checkBox, final BooleanAttr booleanAttr) {
		checkBox.setSelected(booleanAttr.getBoolean());
		
		/* create and an AttributePostChangeListener to listen for attribute changes und update the checkbox */
		AttributePostChangeListener attrListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				checkBox.setSelected(booleanAttr.getBoolean());
			}
		};
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				booleanAttr.setBoolean(checkBox.isSelected());
			}
		};
		
		/* create the binding */
		createBinding(checkBox, booleanAttr, attrListener, actionListener, null);
		
		return checkBox;
	}
	
	/**
	 * Binds the specified JTextField to the specified Attribute.
	 * @param textField
	 * @param doubleAttr
	 * @return the specified JTextField
	 * @throws NullPointerException if any of the specified parameters is null
	 */
	public static JTextField bindTextFieldToAttribute(final JTextField textField, final DoubleAttr doubleAttr) {
		textField.setColumns(COLUMNS);
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
		
		/* create and an AttributePostChangeListener to listen for attribute changes und update the textfield */
		AttributePostChangeListener attrListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
			}
		};
		
		/* create a FocusListener to for textfied to verify and update the attribute value on focus loss */
		FocusListener focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					doubleAttr.setDouble(Double.parseDouble(textField.getText()));
					textField.setBackground(Color.WHITE);
					textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
				} catch (NumberFormatException exception) {
					textField.setBackground(Color.YELLOW);
					textField.requestFocus();
				}
			}
		};
		
		/* create the binding */
		createBinding(textField, doubleAttr, attrListener, TRANSFER_FOCUS_ACTIONLISTENER, focusListener);
		
		return textField;
	}
	
	/**
	 * Removes the binding between the specified component and the attribute it's bound to
	 * by removing all the listeners that have been added to the component and the attribute
	 * when they were bound together.
	 * @throws IllegalArgumentException if this component is not bound to an attribute
	 * @see all the relevant bind... methods.
	 */
	public static void unbindComponent(JComponent component) {
		Binding.unbind(component);
	}
	
	/**
	 * Establishes a new component-To-attribute binding. If the specified component is already bound, this method
	 * will remove the established binding first.
	 * All relevant listeners are registered and this binding is added to the static component-to-binding map.
	 * @param component
	 * @param attribute
	 * @param attributePostChangeListener
	 * @param actionListener
	 * @param focusListener
	 * @throws NullPointerException if component, attribute and/or attributePostChangeListener are null
	 * @throws RuntimeException if one of the listeners can't be added to the component
	 */
	private static void createBinding(JComponent component,
			Attribute attribute,
			AttributePostChangeListener attributePostChangeListener,
			ActionListener actionListener,
			FocusListener focusListener) {
		new Binding(component, attribute, attributePostChangeListener, actionListener, focusListener);
	}
	
	private static class Binding {
		static final Map<Component, Binding> componentMap = new HashMap<Component, Binding>();
		final JComponent component;
		final Attribute attribute;
		final AttributePostChangeListener attributePostChangeListener;
		final ActionListener actionListener;
		final FocusListener focusListener;
		final HierarchyListener hierarchyListener;

		/**
		 * Constructs and establishes a new component-To-attribute binding.
		 * If the component is currently bound, the current binding is removed.
		 * All relevant listeners are registered and this binding is added to the static component-to-binding map.
		 * @param component
		 * @param attribute
		 * @param attributePostChangeListener
		 * @param actionListener
		 * @param focusListener
		 * @throws NullPointerException if component, attribute and/or attributePostChangeListener are null
		 * @throws IllegalStateException if this component is already bound to an attribute
		 * @throws RuntimeException if one of the listeners can't be added to the component
		 */
		private Binding(JComponent component,
				Attribute attribute,
				AttributePostChangeListener attributePostChangeListener,
				ActionListener actionListener,
				FocusListener focusListener) {
			if (component == null) {
				throw new NullPointerException("component must not be null");
			}
			if (attribute == null) {
				throw new NullPointerException("attribute must not be null");
			}
			if (attributePostChangeListener == null) {
				throw new NullPointerException("attributePostChangeListener must not be null");
			}
			this.component = component;
			this.attribute = attribute;
			this.attributePostChangeListener = attributePostChangeListener;
			this.actionListener = actionListener;
			this.focusListener = focusListener;
			this.hierarchyListener = new HierarchyListener() {
				public void hierarchyChanged(HierarchyEvent e) {
					if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
						if (Binding.this.component.isShowing()) {
							Binding.this.attribute.addAttributePostChangeListener(Binding.this.attributePostChangeListener);
						} else {
							Binding.this.attribute.removeAttributePostChangeListener(Binding.this.attributePostChangeListener);
						}
					}
				}
			};
			bind();
		}
		
		/**
		 * Adds the various listeners to the component and the attribute, respectively.
		 * If the component is currently bound, the current binding is removed.
		 * Adds this Binding to the static Component-to-Binding map.
		 * This method is called by the constructor and must not be called from anywhere else!
		 * @throws RuntimeException if one of the listeners can't be added to the component
		 */
		private void bind() {
			/* ensure that this binding is not already mapped to a component */
			Binding currentBinding = componentMap.get(component);
			if (currentBinding != null) {
				currentBinding.unbind();
			}
			
			/*
			 * if the component is currently showing, add the attributePostChangeListener to the
			 * attribute (otherwise this is done by the hierarchyListener when the component
			 * gets shown)
			 */
			if (component.isShowing()) {
				attribute.addAttributePostChangeListener(attributePostChangeListener);
			}
			
			/* if there is an actionListener, add it to the component (if at all possible) */
			if (actionListener != null) {
				if (component instanceof AbstractButton) {
					((AbstractButton) component).addActionListener(actionListener);
				} else if (component instanceof JTextField) {
					((JTextField) component).addActionListener(actionListener);
				} else {
					throw new RuntimeException("Can't add actionListener to " + component);
				}
			}
			
			/* if there is a focusListener, add it to the component */
			if (focusListener != null) {
				component.addFocusListener(focusListener);
			}
			
			/* add the hierarchyListener */
			component.addHierarchyListener(hierarchyListener);
			
			/* add this binding to the component-to-binding map */
			componentMap.put(component, this);
		}
		
		/**
		 * Removes listeners from the component and the attribute.
		 * Removes this Binding from the static Component-to-Binding map.
		 * @throws IllegalStateException if this component is not bound to an attribute
		 * @throws RuntimeException if one of the listeners can't be removed from the component
		 */
		private void unbind() {
			/* ensure that this binding actually is mapped to a component */
			if (!componentMap.containsKey(component)) {
				throw new IllegalStateException("Component " + component + " is not bound to an attribute");
			}
			
			/*
			 * if the component is currently showing, remove the attributePostChangeListener from the
			 * attribute (otherwise this was done by the hierarchyListener when the component
			 * was hidden)
			 */
			if (component.isShowing()) {
				attribute.removeAttributePostChangeListener(attributePostChangeListener);
			}
			
			/* if there is an actionListener, remove it from the component (if at all possible) */
			if (actionListener != null) {
				if (component instanceof AbstractButton) {
					((AbstractButton) component).removeActionListener(actionListener);
				} else if (component instanceof JTextField) {
					((JTextField) component).removeActionListener(actionListener);
				} else {
					throw new RuntimeException("Can't remove actionListener from " + component);
				}
			}
			
			/* if there is a focusListener, remove it from the component */
			if (focusListener != null) {
				component.removeFocusListener(focusListener);
			}
			
			/* remove the hierarchyListener */
			component.removeHierarchyListener(hierarchyListener);
			
			/* remove this binding from the component-to-binding map */
			componentMap.remove(component);
		}
		
		/**
		 * Removes all listeners from the specified component and the attribute it is bound to.
		 * Removes this Binding from the static Component-to-Binding map.
		 * @throws IllegalArgumentException if this component is not bound to an attribute
		 * @throws RuntimeException if one of the listeners can't be removed from the component
		 */
		private static void unbind(JComponent component) {
			Binding binding = componentMap.get(component);
			if (binding == null) {
				throw new IllegalArgumentException("Component " + component + " is not bound to an attribute");
			}
			binding.unbind();
		}
	}
}
