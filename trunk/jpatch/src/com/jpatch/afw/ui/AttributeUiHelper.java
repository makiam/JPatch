package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.BooleanAttr;
import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.control.Configuration;
import com.jpatch.boundary.Main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.text.*;
import java.util.Locale;

import javax.swing.*;

public class AttributeUiHelper {
	private static final DecimalFormat INT_FORMAT = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final int COLUMNS = 6;
	
	
	public static JComboBox bindComboBoxToAttribute(final JComboBox comboBox, final StateMachine stateMachine) {
		
		return comboBox;
	}
	/**
	 * Binds the specified JCheckBox to the specified Attribute.
	 * This method <b>must</b> be called <b>before</b> the JCheckBox becomes visible!
	 * @param checkBox
	 * @param booleanAttr
	 * @return the specified JCheckBox
	 * @throws NullPointerException if any of the specified parameters is null
	 */
	public static JCheckBox bindCheckBoxToAttribute(final JCheckBox checkBox, final BooleanAttr booleanAttr) {
		checkBox.setSelected(booleanAttr.getBoolean());
		
		/* create and an AttributePostChangeListener to listen for attribute changes und update the textfield */
		AttributePostChangeListener attrListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				checkBox.setSelected(booleanAttr.getBoolean());
			}
		};
		
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				booleanAttr.setBoolean(checkBox.isSelected());
			}
		});
		
		prepareHierarchyListener(checkBox, booleanAttr, attrListener);
		
		return checkBox;
	}
	
	/**
	 * Binds the specified JTextField to the specified Attribute.
	 * This method <b>must</b> be called <b>before</b> the JTextField becomes visible!
	 * @param textField
	 * @param doubleAttr
	 * @return the specified JTextField
	 * @throws NullPointerException if any of the specified parameters is null
	 */
	public static JTextField bindTextFieldToAttribute(final JTextField textField, final DoubleAttr doubleAttr) {
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
		
		/* create and an AttributePostChangeListener to listen for attribute changes und update the textfield */
		AttributePostChangeListener attrListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
			}
		};
		
		/* add a FocusListener to the textfied to verify and update the attribute value on focus loss */
		textField.addFocusListener(new FocusAdapter() {
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
		});
		
		prepareTextFieldCommonListeners(textField, doubleAttr, attrListener);
		
		return textField;
	}
	
	/**
	 * This method adds two listeners to the specified JTextField:
	 * <ol><li>A HierarchyListener that adds the specified AttributePostChangeListener to the attribute when the JTextField becomes
	 * visible and removes it when the JTextField becomes invisible.</li>
	 * <li>An ActionListener that calles transferFocus() on the specified JTextField. Event processing should be handled in a FocusListener.</li></ol>
	 * @param textField the TextField to add the listeners to
	 * @param attr the Attribute that's to be bound to this TextField
	 * @param attrListener the AttributeListener that's to be bound to this TextField
	 * @throws NullPointerException if any of the specified parameters is null
	 */
	private static void prepareTextFieldCommonListeners(final JTextField textField, final Attribute attr, final AttributePostChangeListener attrListener) {
		prepareHierarchyListener(textField, attr, attrListener);
		
		/* add an action listener to the textfield */
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.transferFocus();
			}
		});
	}
	
	/**
	 * This method adds a HierarchyListener to the component that adds the specified AttributePostChangeListener to the
	 * attribute when the component becomes visible and removes it when the JTextField becomes invisible.
	 * @param component the component to add the listeners to
	 * @param attr the Attribute that's to be bound to this component
	 * @param attrListener the AttributeListener that's to be bound to this component
	 * @throws NullPointerException if any of the specified parameters is null
	 */
	private static void prepareHierarchyListener(final JComponent component, final Attribute attr, final AttributePostChangeListener attrListener) {
		/* add a HierarchyListener to add/remove the changelistener if the component becomes showing */
		component.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (component.isShowing()) {
						attr.addAttributePostChangeListener(attrListener);
					} else {
						attr.removeAttributePostChangeListener(attrListener);
					}
				}
			}
		});
	}
}
