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
	private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0.000");
	private static final int COLUMNS = 8;
	
	static JLabel getLabelFor(AttributeOld attribute) {
		return new JLabel(attribute.getName());
	}
	
	public static JSlider getSliderFor(final AttributeOld attribute) {
		final JSlider slider;
		switch(attribute.getType()) {
		case BYTE:		// fallthrough intended
		case SHORT:		// fallthrough intended
		case INTEGER:	// fallthrough intended
		case LONG:
			slider = new JSlider((Integer) attribute.getMinimum(), (Integer) attribute.getMaximum());
			slider.setMinorTickSpacing(1);
			slider.setSnapToTicks(true);
			break;
		case FLOAT:		// fallthrough intended
		case DOUBLE:
			slider = new JSlider(0, 100000);
			break;
		default:
			throw new IllegalStateException();
		}
		
		setSliderValue(slider, attribute);
		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				switch(attribute.getType()) {
				case BYTE:		// fallthrough intended
				case SHORT:		// fallthrough intended
				case INTEGER:	// fallthrough intended
				case LONG:
					attribute.setValue(slider.getValue());
					break;
				case FLOAT:		// fallthrough intended
				case DOUBLE:
					double min = (Double) attribute.getMinimum(); 
					double max = (Double) attribute.getMaximum();
					double value = (Double) attribute.getValue();
					attribute.setValue(min + slider.getValue() * (max - min) / (slider.getMaximum() - slider.getMinimum()));
					break;
				default:
					throw new IllegalStateException();
				}
				if (!slider.getModel().getValueIsAdjusting())
					attribute.commitModification();
			}
		});
		
		/* create a ChangeListener to update the slider if the attribute changes */
		final ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSliderValue(slider, attribute);
			}
		};
		
		/* add a HierarchyListener to add/remove the changelistener if the component becomes showing */
		slider.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (slider.isShowing())
						attribute.addChangeListener(changeListener);
					else
						attribute.removeChangeListener(changeListener);
				}
			}
		});
		
		return slider;
	}
	public static JTextField getTextFieldFor(final AttributeOld attribute) {
		final JTextField textField;
		
		switch(attribute.getType()) {
		case BYTE:		// fallthrough intended
		case SHORT:		// fallthrough intended
		case INTEGER:	// fallthrough intended
		case LONG:
			textField = new JTextField(COLUMNS);
			textField.setText(INT_FORMAT.format(attribute.getValue()));
			textField.setHorizontalAlignment(SwingConstants.RIGHT);
			break;
		case FLOAT:		// fallthrough intended
		case DOUBLE:
			textField = new JTextField(COLUMNS);
			textField.setText(FLOAT_FORMAT.format(attribute.getValue()));
			textField.setHorizontalAlignment(SwingConstants.RIGHT);
			break;
		case OBJECT:
			textField = new JTextField(COLUMNS);
			textField.setText(attribute.getValue().toString());
			textField.setEditable(false);
			break;
		default:
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
				attribute.commitModification();
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
		
		/* create a ChangeListener to update the textField if the attribute changes */
		final ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setTextFieldValue(textField, attribute);
			}
		};
		
		/* add a HierarchyListener to add/remove the changelistener if the component becomes showing */
		textField.addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (textField.isShowing())
						attribute.addChangeListener(changeListener);
					else
						attribute.removeChangeListener(changeListener);
				}
			}
		});
		return textField;
	}
	
	private static void setSliderValue(JSlider slider, AttributeOld attribute) {
		switch (attribute.getType()) {
		case BYTE:		// fallthrough intended
		case SHORT:		// fallthrough intended
		case INTEGER:	// fallthrough intended
		case LONG:
			slider.setValue((Integer) attribute.getValue());
			break;
		case FLOAT:		// fallthrough intended
		case DOUBLE:
			double min = (Double) attribute.getMinimum(); 
			double max = (Double) attribute.getMaximum();
			double value = (Double) attribute.getValue();
			
			slider.setValue((int) ((value - min) / (max - min) * (slider.getMaximum() - slider.getMinimum())));
			break;
		default:
			throw new IllegalStateException();
		}
	}
	
	private static void setTextFieldValue(JTextField textField, AttributeOld attribute) {
		switch (attribute.getType()) {
		case BYTE:		// fallthrough intended
		case SHORT:		// fallthrough intended
		case INTEGER:	// fallthrough intended
		case LONG:
			textField.setText(INT_FORMAT.format(attribute.getValue()));
			break;
		case FLOAT:		// fallthrough intended
		case DOUBLE:
			textField.setText(FLOAT_FORMAT.format(attribute.getValue()));
			break;
		default:
			textField.setText(attribute.getValue().toString());
		}
	}
	
	private static boolean verifyTextField(JTextField textField, AttributeOld attribute) {
		Object min = attribute.getMinimum();
		Object max = attribute.getMaximum();
		try {
			switch (attribute.getType()) {
			case BYTE:
				byte b = Byte.parseByte(textField.getText());
				if (min != null && b < (Byte) min)
					return false;
				if (max != null && b > (Byte) max)
					return false;
				attribute.setValue(b);
				return true;
			case SHORT:
				short s = Short.parseShort(textField.getText());
				if (min != null && s < (Short) min)
					return false;
				if (max != null && s > (Short) max)
					return false;
				attribute.setValue(s);
				return true;
			case INTEGER:
				int i = Integer.parseInt(textField.getText());
				if (min != null && i < (Integer) min)
					return false;
				if (max != null && i > (Integer) max)
					return false;
				attribute.setValue(i);
				return true;
			case LONG:
				long l = Long.parseLong(textField.getText());
				if (min != null && l < (Long) min)
					return false;
				if (max != null && l > (Long) max)
					return false;
				attribute.setValue(l);
				return true;
			case FLOAT:
				float f = Float.parseFloat(textField.getText());
				if (min != null && f < (Float) min)
					return false;
				if (max != null && f > (Float) max)
					return false;
				attribute.setValue(f);
				return true;
			case DOUBLE:
				double d = Double.parseDouble(textField.getText());
				if (min != null && d < (Double) min)
					return false;
				if (max != null && d > (Double) max)
					return false;
				attribute.setValue(d);
				return true;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		throw new IllegalArgumentException();
	}
}
