package jpatch.boundary;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.event.*;

import jpatch.entity.*;

public class AttributeUiHelper {
	private static final DecimalFormat INT_FORMAT = new DecimalFormat("0");
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.000");
	private static final int COLUMNS = 7;
	
	static JLabel getLabelFor(Attribute attribute) {
		return new JLabel(attribute.getName());
	}
	
	public static JSlider createSliderFor(final Attribute attribute) {
		final JSlider slider;
		if (attribute instanceof Attribute.BoundedInteger) {
			slider = new JSlider(((Attribute.BoundedInteger) attribute).getMin(), ((Attribute.BoundedInteger) attribute).getMax());
			slider.setMinorTickSpacing(1);
			slider.setSnapToTicks(true);
		} else if (attribute instanceof Attribute.BoundedDouble) {
			slider = new JSlider(0, 100000);
		} else {
			throw new IllegalStateException();
		}
		
		setSliderValue(slider, attribute);
		
		/* create a ChangeListener to update the attribute if the slider was changed */
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (attribute instanceof Attribute.BoundedInteger) {
					((Attribute.BoundedInteger) attribute).set(slider.getValue());
				} else if (attribute instanceof Attribute.BoundedDouble) {
					Attribute.BoundedDouble bdAttr = ((Attribute.BoundedDouble) attribute);
					double min = bdAttr.getMin().get();
					double max = bdAttr.getMax().get();
					bdAttr.set(min + slider.getValue() * (max - min) / (slider.getMaximum() - slider.getMinimum()));
				} else {
					throw new IllegalStateException();
				}
//				if (!slider.getModel().getValueIsAdjusting())
//					attribute.commitModification();
			}
		});
		
		/* create a ChangeListener to update the slider if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				setSliderValue(slider, attribute);
			}
		};
		
		/* add a HierarchyListener to add/remove the changelistener if the component becomes showing */
		slider.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (slider.isShowing())
						attribute.addAttributeListener(attributeListener);
					else
						attribute.removeAttributeListener(attributeListener);
				}
			}
		});

		return slider;
	}
	
	public static JCheckBox createCheckBoxFor(final Attribute attribute) {
		final Attribute.Boolean attrBool = (Attribute.Boolean) attribute;
		
		/* create checkBox and set selection state */
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(attrBool.get());
		
		/* create a ChangeListener to update the attribute if the slider was changed */
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attrBool.set(checkBox.isSelected());
			}
		});
		
		/* create a AttributeListener to update the CheckBox if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				checkBox.setSelected(attrBool.get());
			}
		};
		
		/* add a HierarchyListener to add/remove the attributelistener if the component becomes showing */
		checkBox.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (checkBox.isShowing())
						attribute.addAttributeListener(attributeListener);
					else
						attribute.removeAttributeListener(attributeListener);
				}
			}
		});
		
		return checkBox;
	}
	
	public static JTextField createTextFieldFor(final Attribute attribute) {
		final JTextField textField = new JTextField(COLUMNS);
		
		if (attribute instanceof Attribute.Integer) {
			textField.setText(INT_FORMAT.format(((Attribute.Integer) attribute).get()));
			textField.setHorizontalAlignment(SwingConstants.RIGHT);
		} else if (attribute instanceof Attribute.Double) {
			textField.setText(DOUBLE_FORMAT.format(((Attribute.Double) attribute).get()));
			textField.setHorizontalAlignment(SwingConstants.RIGHT);
		} else if (attribute instanceof Attribute.String) {
			textField.setText(((Attribute.String) attribute).get());
		} else {
			throw new IllegalStateException();
		}
		
		/* add a FocusListener to verify the text and update the attribute or revert back to the old value */
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!verifyTextField(textField, attribute)) {
					setTextFieldValue(textField, attribute);
				}
				textField.setBackground(Color.WHITE);
//				attribute.commitModification();
			}
		});
		
		/* add an ActionListener verify the text and transfer focus, or paint a yellow background */
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("action");
				if (!verifyTextField(textField, attribute))
					textField.setBackground(Color.YELLOW);
				else
					((JComponent) e.getSource()).transferFocus();
			}
		});
		
		/* create a AttributeListener to update the textField if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				setTextFieldValue(textField, attribute);
			}
		};
		
		/* add a HierarchyListener to add/remove the changelistener if the component becomes showing */
		textField.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (textField.isShowing()) {
						attribute.addAttributeListener(attributeListener);
					} else {
						attribute.removeAttributeListener(attributeListener);
					}
				}
			}
		});
		return textField;
	}
	
	public static JComboBox createComboBoxFor(final Attribute attribute) {
		final Attribute.Enum attrEnum = (Attribute.Enum) attribute;
		final JComboBox comboBox = new JComboBox(attrEnum.get().getDeclaringClass().getEnumConstants());
		comboBox.setSelectedIndex(attrEnum.get().ordinal());
		
		/* create a ChangeListener to update the attribute if the slider was changed */
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attrEnum.set((Enum) attrEnum.get().getDeclaringClass().getEnumConstants()[comboBox.getSelectedIndex()]);
			}
		});
		
		/* create a AttributeListener to update the CheckBox if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				comboBox.setSelectedIndex(attrEnum.get().ordinal());
			}
		};
		
		/* add a HierarchyListener to add/remove the attributelistener if the component becomes showing */
		comboBox.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (comboBox.isShowing())
						attribute.addAttributeListener(attributeListener);
					else
						attribute.removeAttributeListener(attributeListener);
				}
			}
		});
		
		return comboBox;
	}
	
	public static Box createLimitBox(final Attribute attribute) {
		final Attribute.Limit attrLimit = (Attribute.Limit) attribute;
		Box box = Box.createHorizontalBox();
		JCheckBox checkBox = createCheckBoxFor(attrLimit.getEnableAttribute());
		final JTextField textField = createTextFieldFor(attrLimit);
		box.add(checkBox);
		box.add(textField);
		
		textField.setEnabled(attrLimit.getEnableAttribute().get());
		attrLimit.getEnableAttribute().addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				textField.setEnabled(attrLimit.getEnableAttribute().get());
			}
		});
		return box;
	}
	
	private static void setSliderValue(JSlider slider, Attribute attribute) {
		if (attribute instanceof Attribute.BoundedInteger) {
			slider.setValue(((Attribute.Integer) attribute).get());
		} else if (attribute instanceof Attribute.BoundedDouble) {
			Attribute.BoundedDouble bdAttr = (Attribute.BoundedDouble) attribute;
			double min = bdAttr.getMin().get();
			double max = bdAttr.getMax().get();
			double value = bdAttr.get();
			slider.setValue((int) ((value - min) / (max - min) * (slider.getMaximum() - slider.getMinimum())));
		} else {
			throw new IllegalStateException();
		}
	}
	
	private static void setTextFieldValue(JTextField textField, Attribute attribute) {
		if (attribute instanceof Attribute.Integer) {
			textField.setText(INT_FORMAT.format(((Attribute.Integer) attribute).get()));
		} else if (attribute instanceof Attribute.Double) {
			textField.setText(DOUBLE_FORMAT.format(((Attribute.Double) attribute).get()));
		} else if (attribute instanceof Attribute.String) {
			textField.setText(((Attribute.String) attribute).get());
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private static boolean verifyTextField(JTextField textField, Attribute attribute) {
		if (attribute instanceof Attribute.String)
			return true;
		try {
			if (attribute instanceof Attribute.Integer) {
				int i = Integer.parseInt(textField.getText());
				if (attribute instanceof Attribute.BoundedInteger) {
					if (i < ((Attribute.BoundedInteger) attribute).getMin())
						return false;
					if (i > ((Attribute.BoundedInteger) attribute).getMax())
						return false;
				}
				((Attribute.Integer) attribute).set(i);
				return true;
			} else if (attribute instanceof Attribute.Double) {
				double d = Double.parseDouble(textField.getText());
				if (attribute instanceof Attribute.BoundedDouble) {
					Attribute.Limit min = ((Attribute.BoundedDouble) attribute).getMin();
					Attribute.Limit max = ((Attribute.BoundedDouble) attribute).getMax();
					if (min.getEnableAttribute().get() && d < min.get())
						return false;
					if (max.getEnableAttribute().get() && d > max.get())
						return false;
				}
				((Attribute.Double) attribute).set(d);
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		throw new IllegalArgumentException();
	}
}
