package jpatch.boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.*;
import javax.swing.event.*;

import jpatch.entity.*;
import jpatch.entity.attributes2.Attribute;

public class AttributeUiHelper {
	private static final DecimalFormat INT_FORMAT = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final int COLUMNS = 6;
	private static final Font staticFont = new JTextField().getFont();
	private static final Font keyedFont = staticFont.deriveFont(Font.BOLD);
	private static final String[] OFF_ON = new String[] { "no", "yes" };
	private static final JLabel[] LABLES = new JLabel[] { new JLabel(OFF_ON[0]), new JLabel(OFF_ON[1]) };
	
	static {
		Dimension dim = new Dimension(30, 16);
		LABLES[0].setPreferredSize(dim);
		LABLES[0].setOpaque(true);
		LABLES[0].setBackground(Color.WHITE);
		LABLES[1].setPreferredSize(dim);
		LABLES[1].setOpaque(true);
		LABLES[1].setBackground(Color.WHITE);
	}
	static JLabel getLabelFor(Attribute attribute) {
		return new JLabel(attribute.getName());
	}
	
//	public static JLabel createLabelFor(final Attribute.Name name) {
//		final JLabel label = new JLabel();
//		label.setText(name.get());
//		name.addAttributeListener(new AttributeListener() {
//			public void attributeChanged(Attribute attribute) {
//				label.setText(name.get());
//			}
//		});
//		return label;
//	}
	
	public static JSlider createSliderFor(final Attribute.Double attribute, final double min, final double max) {
		final JSlider slider;
		slider = new JSlider(0, 0x10000);
		final int intMax = 0x10000 * 0x10000;
		
		slider.setValue((int) (Math.sqrt((attribute.get() - min) / (max - min)) * 0x10000));
//		slider.setMinorTickSpacing(1);
//		slider.setSnapToTicks(true);
		slider.setToolTipText(attribute.getName());
		
		/* create a ChangeListener to update the attribute if the slider was changed */
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double t = ((double) slider.getValue()) / 0x10000;
				attribute.set(t * t * (max - min) + min);
				Main.getInstance().getActiveSds().rethinkSlates();
				Main.getInstance().repaintViewports();
			}
		});
		
		/* create a ChangeListener to update the slider if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				slider.setValue((int) (Math.sqrt((attribute.get() - min) / (max - min)) * 0x10000));
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
	
	public static JSlider createSliderFor(final Attribute attribute) {
		final JSlider slider;
		if (attribute instanceof Attribute.BoundedInteger) {
			slider = new JSlider(((Attribute.BoundedInteger) attribute).getMin(), ((Attribute.BoundedInteger) attribute).getMax());
			slider.setMinorTickSpacing(1);
			slider.setSnapToTicks(true);
			slider.setToolTipText(attribute.getName());
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
					double min = bdAttr.min.get();
					double max = bdAttr.max.get();
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
	
	public static JComponent createBooleanComboFor(final Attribute attribute) {
		
		
		final Attribute.KeyedBoolean attrkb = (Attribute.KeyedBoolean) attribute;
		Box box = Box.createHorizontalBox();
		
		final JSpinner comboBox = new JSpinner(new SpinnerListModel(OFF_ON));
		comboBox.setEditor(attrkb.get() ? LABLES[1] : LABLES[0]);
		
		comboBox.setValue(attrkb.get() ? OFF_ON[1] : OFF_ON[0]);
		
		/* create a ChangeListener to update the attribute if the slider was changed */
		comboBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				attrkb.set(comboBox.getValue() == OFF_ON[1]);
			}
		});
		
		/* create a AttributeListener to update the CheckBox if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				comboBox.setValue(attrkb.get() ? OFF_ON[1] : OFF_ON[0]);
				comboBox.setEditor(attrkb.get() ? LABLES[1] : LABLES[0]);
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
		
		box.add(comboBox);
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("keyed:"));
		box.add(createCheckBoxFor(attrkb.keyed));
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("locked:"));
		box.add(createCheckBoxFor(attrkb.locked));
		return box;
	}
	
	public static JCheckBox createCheckBoxFor(final Attribute attribute) {
		final Attribute.Boolean attrBool = (Attribute.Boolean) attribute;
		
		/* create checkBox and set selection state */
		final JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(attrBool.get());
		checkBox.setToolTipText(attribute.getName());
		checkBox.setOpaque(false);
		
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
		return createTextFieldFor(attribute, 70);
	}
	
	public static JTextField createTextFieldFor(final Attribute attribute, int width) {
		final JTextField textField = new JTextField();
		Dimension dim = textField.getPreferredSize();
		dim.width = width;
		textField.setPreferredSize(dim);
		textField.setToolTipText(attribute.getName());
		
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
		
		if (attribute instanceof Attribute.Limit) {
			textField.setEditable(((Attribute.Limit) attribute).enabled.get());
			((Attribute.Limit) attribute).enabled.addAttributeListener(new AttributeListener() {
				public void attributeChanged(Attribute attribute) {
					textField.setEditable(((Attribute.Boolean) attribute).get());
				}
			});
		} else if (attribute instanceof Attribute.BoundedDouble) {
			textField.setEditable(!((Attribute.BoundedDouble) attribute).locked.get());
			((Attribute.BoundedDouble) attribute).locked.addAttributeListener(new AttributeListener() {
				public void attributeChanged(Attribute attribute) {
					textField.setEditable(!((Attribute.Boolean) attribute).get());
				}
			});
			textField.setFont(((Attribute.BoundedDouble) attribute).keyed.get() ? keyedFont : staticFont);
			((Attribute.BoundedDouble) attribute).keyed.addAttributeListener(new AttributeListener() {
				public void attributeChanged(Attribute attribute) {
					textField.setFont(((Attribute.Boolean) attribute).get() ? keyedFont : staticFont);
				}
			});
		}
		
		/* add a FocusListener to verify the text and update the attribute or revert back to the old value */
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				verifyTextField(textField, attribute);
				setTextFieldValue(textField, attribute);
//				}
//				textField.setBackground(Color.WHITE);
//				attribute.commitModification();
			}
		});
		
		/* add an ActionListener verify the text and transfer focus, or paint a yellow background */
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("action");
				if (!verifyTextField(textField, attribute))
					;
//					textField.setBackground(Color.YELLOW);
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
	
	public static JComboBox createComboBoxFor(final Attribute.Enum attribute) {
		final JComboBox comboBox = new JComboBox(attribute.get().getDeclaringClass().getEnumConstants());
		comboBox.setSelectedIndex(attribute.get().ordinal());
		
		/* create a ChangeListener to update the attribute if the slider was changed */
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attribute.set((Enum) attribute.get().getDeclaringClass().getEnumConstants()[comboBox.getSelectedIndex()]);
			}
		});
		
		/* create a AttributeListener to update the CheckBox if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				comboBox.setSelectedIndex(attribute.get().ordinal());
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
	
	public static JComboBox createComboBoxFor(final Attribute.Array attribute) {
		final JComboBox comboBox = new JComboBox(attribute.getArray());
		comboBox.setSelectedIndex(attribute.getIndex());
		
		/* create a ChangeListener to update the attribute if the slider was changed */
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attribute.set(comboBox.getSelectedIndex());
			}
		});
		
		/* create a AttributeListener to update the CheckBox if the attribute changes */
		final AttributeListener attributeListener = new AttributeListener() {
			public void attributeChanged(Attribute a) {
				comboBox.setSelectedIndex(attribute.getIndex());
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
	
	private static void setSliderValue(JSlider slider, Attribute attribute) {
		if (attribute instanceof Attribute.BoundedInteger) {
			slider.setValue(((Attribute.Integer) attribute).get());
		} else if (attribute instanceof Attribute.BoundedDouble) {
			Attribute.BoundedDouble bdAttr = (Attribute.BoundedDouble) attribute;
			double min = bdAttr.min.get();
			double max = bdAttr.max.get();
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
		if (attribute instanceof Attribute.String) {
			if (attribute instanceof Attribute.Name) {
				JPatchObject object = ((Attribute.Name) attribute).getJPatchObject();
				return (object.getObjectRegistry().getObject(object.getClass(), textField.getText()) == null);
			}
			return true;
		}
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
					Attribute.Limit min = ((Attribute.BoundedDouble) attribute).min;
					Attribute.Limit max = ((Attribute.BoundedDouble) attribute).max;
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
