package com.jpatch.afw.ui;

import java.awt.Color;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.jpatch.afw.attributes.*;

public class AttributeManager {
	/**
	 * The sole AttributeManager instance (singleton pattern)
	 */
	private static AttributeManager INSTANCE = new AttributeManager();
	
	/**
	 * DecimalFormat used to print integers in TextFields
	 */
	private static final DecimalFormat INT_FORMAT = new DecimalFormat("0", new DecimalFormatSymbols(Locale.ENGLISH));
	
	/**
	 * DecimalFormat used to print doubles in TextFields
	 */
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	private static final DecimalFormat SCI_FORMAT = new DecimalFormat("0.0E0", new DecimalFormatSymbols(Locale.ENGLISH));
	
	/**
	 * Columns (width of the TextFields)
	 */
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
	
	/**
	 * A set containing all locked attributes
	 */
	private final Set<ScalarAttribute> locks = new HashSet<ScalarAttribute>();
	
	/**
	 * Maps DoubleAttr to their upper limits
	 */
	private final Map<DoubleAttr, DoubleMaximum> upperLimitsDouble = new HashMap<DoubleAttr, DoubleMaximum>();
	
	/**
	 * Maps DoubleAttr to their lower limits
	 */
	private final Map<DoubleAttr, DoubleMinimum> lowerLimitsDouble = new HashMap<DoubleAttr, DoubleMinimum>();
	
	/**
	 * Maps IntAttr to their upper limits
	 */
	private final Map<IntAttr, IntMaximum> upperLimitsInt = new HashMap<IntAttr, IntMaximum>();
	
	/**
	 * Maps IntAttr to their lower limits
	 */
	private final Map<IntAttr, IntMinimum> lowerLimitsInt = new HashMap<IntAttr, IntMinimum>();
	
	/**
	 * Stores the managed listeners for managed JComponents
	 */
	private final Map<JComponent, Collection<Object>> componentListeners = new HashMap<JComponent, Collection<Object>>();
	
	/**
	 * Stores the AttributeBindings for managed JComponents
	 */
	private final Map<JComponent, AttributeBinding[]> componentBindings = new HashMap<JComponent, AttributeBinding[]>();
	
	/**
	 * A list of ActionListeners
	 */
	private final List<UserInputListener> listenerList = new ArrayList<UserInputListener>();
	
	/**
	 * Returns the single AttributeManager instance (singleton pattern)
	 * @return the single AttributeManager instance
	 */
	public static AttributeManager getInstance() {
		return INSTANCE;
	}
	
	static {
		DOUBLE_FORMAT.setMaximumFractionDigits(5);
		DOUBLE_FORMAT.setMinimumFractionDigits(1);
//		DOUBLE_FORMAT.set
	}
	/**
	 * Private constructor (singleton pattern)
	 */
	private AttributeManager() { }
	
	/**
	 * Adds the specified ActionListener. The listeners actionPerformed method will be
	 * called on every user input.
	 * @param listener the ActionListener to add
	 */
	public void addUserInputListener(UserInputListener listener) {
		listenerList.add(listener);
	}
	
	/**
	 * Removes the specified ActionListener.
	 * @param listener the ActionListener to remove
	 */
	public void removeUserInputListener(UserInputListener listener) {
		listenerList.remove(listener);
	}
	
	/**
	 * Locks the specified attribute
	 * @param attr the attribute to be locked
	 * @throws IllegalArgumentException if the specified attribute has already been locked
	 */
	public void lock(ScalarAttribute attr) {
		attr.addAttributePreChangeListener(Lock.getInstance());		// throws IllegalArgumentException if already locked
		locks.add(attr);
	}
	
	/**
	 * Unlocks the specified attribute
	 * @param attr the attribute to be unlocked
	 * @throws IllegalArgumentException if the specified attribute has not been locked
	 */
	public void unlock(ScalarAttribute attr) {
		attr.removeAttributePreChangeListener(Lock.getInstance());	// throws IllegalArgumentException if already locked
		locks.remove(attr);
	}
	
	/**
	 * Returns true if the specified attribute is locked, false otherwise
	 * @param attr the attribute to check for a lock
	 * @return true if the specified attribute is locked, false otherwise
	 */
	public boolean isLocked(ScalarAttribute attr) {
		return locks.contains(attr);
	}
	
	/**
	 * Sets the upper limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the upper limit for the specified attribute
	 * @return the upper limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an upper limit set
	 */
	public DoubleMaximum setUpperLimit(final DoubleAttr attr, final DoubleMaximum limit) {
		if (upperLimitsDouble.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set an upper limit: " + upperLimitsDouble.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		limit.getAttr().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				attr.setDouble(limit.attributeWillChange(attr, attr.getDouble()));
			}
		});
		upperLimitsDouble.put(attr, limit);
		return limit;
	}
	
	/**
	 * Sets the upper limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the upper limit for the specified attribute
	 * @return the upper limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an upper limit set
	 */
	public IntMaximum setUpperLimit(final IntAttr attr, final IntMaximum limit) {
		if (upperLimitsInt.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set an upper limit: " + upperLimitsInt.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		limit.getAttr().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				attr.setInt(limit.attributeWillChange(attr, attr.getInt()));
			}
		});
		upperLimitsInt.put(attr, limit);
		return limit;
	}
	
	/**
	 * Clears the upper limit for the specified attribute
	 * @param attr the attribute whose upper limit should be cleared
	 */
	public void clearUpperLimit(DoubleAttr attr) {
		DoubleLimit limit = upperLimitsDouble.get(attr);
		if (limit != null) {
			attr.removeAttributePreChangeListener(limit);
			upperLimitsDouble.remove(attr);
		}
	}
	
	/**
	 * Clears the upper limit for the specified attribute
	 * @param attr the attribute whose upper limit should be cleared
	 */
	public void clearUpperLimit(IntAttr attr) {
		IntLimit limit = upperLimitsInt.get(attr);
		if (limit != null) {
			attr.removeAttributePreChangeListener(limit);
			upperLimitsInt.remove(attr);
		}
	}
	
	/**
	 * Returns the upper limit of the specified attribute
	 * @param attr the attribute whose upper limit should be returned
	 * @return the upper limit of the specified attribute, or null if no upper limit is set
	 */
	public DoubleAttr getUpperLimit(DoubleAttr attr) {
		DoubleMaximum limit = upperLimitsDouble.get(attr);
		return limit == null ? null : limit.getAttr();
	}
	
	/**
	 * Returns the upper limit of the specified attribute
	 * @param attr the attribute whose upper limit should be returned
	 * @return the upper limit of the specified attribute, or null if no upper limit is set
	 */
	public IntAttr getUpperLimit(IntAttr attr) {
		IntMaximum limit = upperLimitsInt.get(attr);
		return limit == null ? null : limit.getAttr();
	}
	
	/**
	 * Sets the lower limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the lower limit for the specified attribute
	 * @return the lower limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an lower limit set
	 */
	public DoubleMinimum setLowerLimit(final DoubleAttr attr, final DoubleMinimum limit) {
		if (lowerLimitsDouble.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set a lower limit: " + lowerLimitsDouble.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		limit.getAttr().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				attr.setDouble(limit.attributeWillChange(attr, attr.getDouble()));
			}
		});
		lowerLimitsDouble.put(attr, limit);
		return limit;
	}
	
	/**
	 * Sets the lower limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the lower limit for the specified attribute
	 * @return the lower limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an lower limit set
	 */
	public IntMinimum setLowerLimit(final IntAttr attr, final IntMinimum limit) {
		if (lowerLimitsInt.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set a lower limit: " + lowerLimitsInt.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		limit.getAttr().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				attr.setInt(limit.attributeWillChange(attr, attr.getInt()));
			}
		});
		lowerLimitsInt.put(attr, limit);
		return limit;
	}
	
	/**
	 * Clears the lower limit for the specified attribute
	 * @param attr the attribute whose lower limit should be cleared
	 */
	public void clearLowerLimit(DoubleAttr attr) {
		DoubleLimit limit = lowerLimitsDouble.get(attr);
		if (limit != null) {
			attr.removeAttributePreChangeListener(limit);
		}
		lowerLimitsDouble.remove(attr);
	}
	
	/**
	 * Clears the lower limit for the specified attribute
	 * @param attr the attribute whose lower limit should be cleared
	 */
	public void clearLowerLimit(IntAttr attr) {
		IntLimit limit = lowerLimitsInt.get(attr);
		if (limit != null) {
			attr.removeAttributePreChangeListener(limit);
		}
		lowerLimitsInt.remove(attr);
	}
	
	/**
	 * Returns the lower limit of the specified attribute
	 * @param attr the attribute whose lower limit should be returned
	 * @return the lower limit of the specified attribute, or null if no lower limit is set
	 */
	public DoubleAttr getLowerLimit(DoubleAttr attr) {
		DoubleMinimum limit = lowerLimitsDouble.get(attr);
		return limit == null ? null : limit.getAttr();
	}
	
	/**
	 * Returns the lower limit of the specified attribute
	 * @param attr the attribute whose lower limit should be returned
	 * @return the lower limit of the specified attribute, or null if no lower limit is set
	 */
	public IntAttr getLowerLimit(IntAttr attr) {
		IntMinimum limit = lowerLimitsInt.get(attr);
		return limit == null ? null : limit.getAttr();
	}
	
	/**
	 * Creates a new bounded DoubleAttr that is limited by the specified min and max attributes
	 * @param min the attribute representing the minimum value
	 * @param max the attribute representing the maximum value
	 * @return a new bounded DoubleAttr that is limited by the specified min and max attributes
	 */
	public DoubleAttr createBoundedDoubleAttr(DoubleAttr min, DoubleAttr max) {
		DoubleAttr attr = new DoubleAttr();
		setLowerLimit(attr, new DoubleMinimum(min));
		setUpperLimit(attr, new DoubleMaximum(max));
		return attr;
	}
	
	/**
	 * Creates a new bounded DoubleAttr that is limited by the specified min and max attributes
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return a new bounded DoubleAttr that is limited by the specified min and max attributes
	 */
	public DoubleAttr createBoundedDoubleAttr(double value, double min, double max) {
		DoubleAttr attr = new DoubleAttr(value);
		setLowerLimit(attr, new DoubleMinimum(min));
		setUpperLimit(attr, new DoubleMaximum(max));
		return attr;
	}
	
	/**
	 * Creates a new bounded IntAttr that is limited by the specified min and max attributes
	 * @param value the attribute value
	 * @param min the attribute representing the minimum value
	 * @param max the attribute representing the maximum value
	 * @return a new bounded IntAttr that is limited by the specified min and max attributes
	 */
	public IntAttr createBoundedIntAttr(int value, IntAttr min, IntAttr max) {
		IntAttr attr = new IntAttr(value);
		setLowerLimit(attr, new IntMinimum(min));
		setUpperLimit(attr, new IntMaximum(max));
		return attr;
	}
	
	/**
	 * Creates a new bounded IntAttr that is limited by the specified min and max attributes
	 * @param value the attribute value
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return a new bounded IntAttr that is limited by the specified min and max attributes
	 */
	public IntAttr createBoundedIntAttr(int value, int min, int max) {
		IntAttr attr = new IntAttr(value);
		setLowerLimit(attr, new IntMinimum(min));
		setUpperLimit(attr, new IntMaximum(max));
		return attr;
	}
	
	/**
	 * Unbinds the specified Component from the Attribute it is bound to.
	 * If the component is not bound, this method returns without doing anything.
	 * @param component the Component to unbind
	 */
	public void unbind(JComponent component) {
//		System.out.println("unbind " + component);
		AttributeBinding[] bindings = componentBindings.get(component);
		if (bindings != null) {	
			/*
			 * remove the AttributePostChangeListener if the component is currently showing
			 * (otherwise this has been done by the HierarchyListener already)
			 */
			if (component.isShowing()) {
				for (AttributeBinding binding : bindings) {
					binding.unbind();
				}
			}
		}
		
		/* remove all "managed" listeners*/
		removeListeners(component);
		
		/* remove this component from the componentBindings map*/
		componentBindings.remove(component);
	}
	
	/**
	 * Binds the specified AbstractButton to the specified Attribute.
	 * @param button
	 * @param booleanAttr
	 * @return the specified AbstractButton
	 * @throws NullPointerException if any of the specified parameters is null
	 * @throws IllegalStateException if the specified AbstractButton is already bound (to <i>any</i> Attribute)
	 */
	public AbstractButton bindButtonToAttribute(final Object entity, final AbstractButton button, final BooleanAttr booleanAttr) {
//		checkBox.setSelected(booleanAttr.getBoolean());
		
		/* create an AttributePostChangeListener to listen for attribute changes und update the checkbox */
		AttributePostChangeListener attrListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				button.setSelected(booleanAttr.getBoolean());
			}
		};
		
		/* bind attribute and attrListener to component */
		bind(button, new AttributeBinding(booleanAttr, attrListener));						// throws IllegalStateException if already bound
		
		/* create and add an ActionListener */
		addListener(button, new ActionListener() {					// throws IllegalStateException if already bound
			public void actionPerformed(ActionEvent e) {
				booleanAttr.setBoolean(button.isSelected());
				fireActionPerformed(entity, booleanAttr);
			}
		});
		
		return button;
	}
	
	public JTextField bindTextFieldToAttribute(Object entity, final JTextField textField, ScalarAttribute attribute) {
		if (attribute instanceof DoubleAttr) {
			return bindTextFieldToAttribute(entity, textField, (DoubleAttr) attribute);
		} else if (attribute instanceof IntAttr) {
			return bindTextFieldToAttribute(entity, textField, (IntAttr) attribute);
		} else if (attribute instanceof GenericAttr) {
			return bindTextFieldToAttribute(entity, textField, (GenericAttr<String>) attribute);
		} else {
			throw new IllegalArgumentException("can't bind " + attribute + " to " + textField);
		}
	}
	
	public void bindSliderToAttribute(final Object entity, final JSlider slider, final IntAttr intAttr) {
		final IntAttr minimum = getLowerLimit(intAttr);
		final IntAttr maximum = getUpperLimit(intAttr);
		slider.setSnapToTicks(true);
		slider.setMinorTickSpacing(1);
		slider.setOpaque(false);
		slider.setValue(intAttr.getInt());
		if (minimum == null || maximum == null) {
			throw new IllegalArgumentException(intAttr + " is not bounded!");
		}
		AttributeBinding minBinding = new AttributeBinding(minimum, new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				System.out.println("slider value = " + slider.getValue());
				System.out.println("setting minimum to " + minimum.getInt());
				slider.setMinimum(minimum.getInt());
				System.out.println("slider value = " + slider.getValue());
			}	
		});
		AttributeBinding maxBinding = new AttributeBinding(maximum, new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				System.out.println("slider value = " + slider.getValue());
				System.out.println("setting maximum to " + maximum.getInt());
				slider.setMaximum(maximum.getInt());
				System.out.println("slider value = " + slider.getValue());
			}	
		});
		class SuperListener implements ChangeListener, AttributePostChangeListener {
			private boolean suppressAction = false;
			
			public void stateChanged(ChangeEvent e) {
				if (!suppressAction) {
					intAttr.setInt(slider.getValue());
					fireActionPerformed(entity, intAttr);
				}
			}

			public void attributeHasChanged(Attribute source) {
				suppressAction = true;
				slider.setValue(intAttr.getInt());
				suppressAction = false;
			}
		}
		SuperListener listener = new SuperListener();
		AttributeBinding sliderBinding = new AttributeBinding(intAttr, listener);
		bind(slider, minBinding, maxBinding, sliderBinding);
		addListener(slider, listener);
	}
	
	public JComboBox bindComboBoxToAttribute(final Object entity, final JComboBox comboBox, final StateMachine stateMachine) {
		class SuperListener implements ActionListener, AttributePostChangeListener {
			private boolean suppressAction = false;
			
			public void actionPerformed(ActionEvent e) {
				if (!suppressAction) {
					if (comboBox.getSelectedItem() != stateMachine.getValue()) {
						Object newState = stateMachine.setValue(comboBox.getSelectedItem());
						fireActionPerformed(entity, stateMachine);
						if (newState != comboBox.getSelectedItem()) {
							comboBox.setSelectedItem(newState);
						}
					}
				}
			}

			public void attributeHasChanged(Attribute source) {
				suppressAction = true;
				if (source == stateMachine) {
					comboBox.setSelectedItem(stateMachine.getValue());
				} else if (source == stateMachine.getStatesAttribute()) {
					comboBox.removeAllItems();
					for (Object o : ((CollectionAttr) source).getElements()) {
						comboBox.addItem(o);
					}
					comboBox.setSelectedItem(stateMachine.getValue());
				}
				suppressAction = false;
			}
		}
		
		SuperListener listener = new SuperListener();
		
		bind(comboBox, new AttributeBinding(stateMachine, listener), new AttributeBinding(stateMachine.getStatesAttribute(), listener));
		
		/* stimulate stateSetListener to update state-list in comboBox */
//		stateSetListener.attributeHasChanged(stateMachine.getStateSet());
		
		addListener(comboBox, listener);
		
		return comboBox;
	}
	
	/**
	 * Binds the specified JTextField to the specified Attribute.
	 * @param textField
	 * @param doubleAttr
	 * @return the specified JTextField
	 * @throws NullPointerException if any of the specified parameters is null
	 * @throws IllegalStateException if the specified JTextField is already bound (to <i>any</i> Attribute)
	 */
	private JTextField bindTextFieldToAttribute(final Object entity, final JTextField textField, final GenericAttr<String> stringAttr) {
		textField.setColumns(COLUMNS);
		
		class SuperListener extends FocusAdapter implements FocusListener, AttributePostChangeListener {
			private boolean suppressAction = false;

			public void focusLost(FocusEvent e) {
				if (!suppressAction) {
					suppressAction = true;
					String newValue = stringAttr.setValue(textField.getText());
					if (newValue.equals(textField.getText())) {
						textField.setBackground(textField.isEnabled() ? UIManager.getColor("TextField.background") : UIManager.getColor("TextField.inactiveBackground"));
						fireActionPerformed(entity, stringAttr);
					} else {
						textField.setBackground(Color.YELLOW);
						textField.requestFocus();
					}
					suppressAction = false;
				}
			}
			
			public void attributeHasChanged(Attribute source) {
				if (!suppressAction) {
					suppressAction = true;
					textField.setText(stringAttr.getValue());
					suppressAction = false;
				}
			}
		}
		
		SuperListener listener = new SuperListener();
		
		/* bind attribute and attrListener to component */
		bind(textField, new AttributeBinding(stringAttr, listener));						// throws IllegalStateException if already bound
		
		
		/* add a FocusListener to verify and update the attribute value on focus loss */
		addListener(textField, listener);
		
		/* add the TRANSFER_FOCUS_ACTIONLISTENER (which simply transfers the focus when the user presses enter over the TextField */
		addListener(textField, TRANSFER_FOCUS_ACTIONLISTENER);
		
		return textField;
	}
	
	/**
	 * Binds the specified JTextField to the specified Attribute.
	 * @param textField
	 * @param doubleAttr
	 * @return the specified JTextField
	 * @throws NullPointerException if any of the specified parameters is null
	 * @throws IllegalStateException if the specified JTextField is already bound (to <i>any</i> Attribute)
	 */
	private JTextField bindTextFieldToAttribute(final Object entity, final JTextField textField, final DoubleAttr doubleAttr) {
		textField.setColumns(COLUMNS);
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
//		textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
		
		
		
		class SuperListener implements FocusListener, ComponentListener, AttributePostChangeListener {
			private boolean suppressAction = false;

			public void focusLost(FocusEvent e) {
				if (!suppressAction) {
					suppressAction = true;
					try {
						double value = Double.parseDouble(textField.getText());
						if (value != doubleAttr.getDouble()) {
							doubleAttr.setDouble(Double.parseDouble(textField.getText()));
							textField.setBackground(textField.isEnabled() ? UIManager.getColor("TextField.background") : UIManager.getColor("TextField.inactiveBackground"));
							textField.setText(formatDouble(textField, doubleAttr.getDouble()));
							fireActionPerformed(entity, doubleAttr);
						}
					} catch (NumberFormatException exception) {
						textField.setBackground(Color.YELLOW);
						textField.requestFocus();
					} finally {
						suppressAction = false;
					}
				}
			}
			
			public void attributeHasChanged(Attribute source) {
				if (!suppressAction) {
					suppressAction = true;
					textField.setText(formatDouble(textField, doubleAttr.getDouble()));
					suppressAction = false;
				}
			}

			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void componentResized(ComponentEvent e) {
				suppressAction = true;
				textField.setText(formatDouble(textField, doubleAttr.getDouble()));
				suppressAction = false;
			}

			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		}
		
		SuperListener listener = new SuperListener();
		
		/* bind attribute and attrListener to component */
		bind(textField, new AttributeBinding(doubleAttr, listener));						// throws IllegalStateException if already bound
		
		
		/* add a FocusListener to verify and update the attribute value on focus loss */
		addListener(textField, listener);
		
		/* add the TRANSFER_FOCUS_ACTIONLISTENER (which simply transfers the focus when the user presses enter over the TextField */
		addListener(textField, TRANSFER_FOCUS_ACTIONLISTENER);
		
		
		return textField;
	}
	
	
	private JTextField bindTextFieldToAttribute(final Object entity, final JTextField textField, final IntAttr intAttr) {
		textField.setColumns(COLUMNS);
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
//		textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
		
		
		
		class SuperListener extends FocusAdapter implements FocusListener, AttributePostChangeListener {
			private boolean suppressAction = false;

			public void focusLost(FocusEvent e) {
				if (!suppressAction) {
					suppressAction = true;
					try {
						double value = Double.parseDouble(textField.getText());
						if (value != intAttr.getInt()) {
							intAttr.setInt(Integer.parseInt(textField.getText()));
							textField.setBackground(textField.isEnabled() ? UIManager.getColor("TextField.background") : UIManager.getColor("TextField.inactiveBackground"));
							textField.setText(INT_FORMAT.format(intAttr.getInt()));
							fireActionPerformed(entity, intAttr);
						}
					} catch (NumberFormatException exception) {
						textField.setBackground(Color.YELLOW);
						textField.requestFocus();
					} finally {
						suppressAction = false;
					}
				}
			}
			
			public void attributeHasChanged(Attribute source) {
				if (!suppressAction) {
					suppressAction = true;
					textField.setText(INT_FORMAT.format(intAttr.getInt()));
					suppressAction = false;
				}
			}
		}
		
		SuperListener listener = new SuperListener();
		
		/* bind attribute and attrListener to component */
		bind(textField, new AttributeBinding(intAttr, listener));						// throws IllegalStateException if already bound
		
		
		/* add a FocusListener to verify and update the attribute value on focus loss */
		addListener(textField, listener);
		
		/* add the TRANSFER_FOCUS_ACTIONLISTENER (which simply transfers the focus when the user presses enter over the TextField */
		addListener(textField, TRANSFER_FOCUS_ACTIONLISTENER);
		
		return textField;
	}
	
	/**
	 * Binds the specified JSlider to the specified (bounded!) Attribute.
	 * @param slider
	 * @param attr
	 * @param mapping
	 * @return the specified JSlider
	 * @throws NullPointerException if any of the specified parameters is null
	 * @throws IllegalArgumentEexception if the specified attribute is not bounded
	 * @throws IllegalStateException if the specified JTextField is already bound (to <i>any</i> Attribute)
	 */
	public JSlider bindSliderToAttribute(Object entity, JSlider slider, DoubleAttr attr, Mapping mapping) {
		DoubleAttr min = getLowerLimit(attr);
		DoubleAttr max = getUpperLimit(attr);
		if (min == null || max == null) {
			throw new IllegalArgumentException(attr + " is not bounded");
		}
		return bindSliderToAttribute(entity, slider, attr, min, max, mapping);
	}
	
	/**
	 * Binds the specified JSlider to the specified Attribute. Constant minimum and maximum slider
	 * values must be provided.
	 * @param slider
	 * @param attr
	 * @param min
	 * @param max
	 * @param mapping
	 * @return the specified JSlider
	 * @throws NullPointerException if any of the specified parameters is null
	 * @throws IllegalArgumentEexception if the specified attribute is not bounded
	 * @throws IllegalStateException if the specified JTextField is already bound (to <i>any</i> Attribute)
	 */
	public JSlider bindSliderToAttribute(Object entity, JSlider slider, DoubleAttr attr, double min, double max, Mapping mapping) {
		return bindSliderToAttribute(entity, slider, attr, new DoubleAttr(min), new DoubleAttr(max), mapping);
	}
	
	/**
	 * Binds the specified JSlider to the specified Attribute.
	 * @param slider
	 * @param attr
	 * @param min
	 * @param max
	 * @param mapping
	 * @return the specified JSlider
	 * @throws NullPointerException if any of the specified parameters is null
	 * @throws IllegalStateException if the specified JTextField is already bound (to <i>any</i> Attribute)
	 */
	private JSlider bindSliderToAttribute(final Object entity, final JSlider slider, final DoubleAttr attr, final DoubleAttr min, final DoubleAttr max, final Mapping mapping) {
		slider.setMinimum(0);
		slider.setMaximum(1000);
		slider.setOpaque(false);
		class SuperListener implements ChangeListener, AttributePostChangeListener {
			private boolean suppressAction = false;

			public void stateChanged(ChangeEvent e) {
				if (!suppressAction) {
					suppressAction = true;
					getSliderValue(slider, attr, min, max, mapping);
					fireActionPerformed(entity, attr);
					suppressAction = false;
				}
			}
			
			public void attributeHasChanged(Attribute source) {
				if (!suppressAction) {
					suppressAction = true;
					setSliderPosition(slider, attr, min, max, mapping);
					suppressAction = false;
				}
			}
		}
		
		SuperListener listener = new SuperListener();
		
//		setSliderPosition(slider, attr, min, max, mapping);
		
		
		/* bind attribute and attrListener to component */
		bind(slider, new AttributeBinding(attr, listener), new AttributeBinding(min, listener), new AttributeBinding(max, listener));				// throws IllegalStateException if already bound
		
		/* add a ChangeListener to track the slider value */
		addListener(slider, listener);
		
		return slider;
	}
	
	/**
	 * Binds a textfied, a set and a clear button to an attribute that allow to specify an upper or lower
	 * limit for that attribute. The textfield is bound to the limit itself, the set button can be used to
	 * set the limit to the current value and the clear button can be used to clear the limit.
	 * @param attr the attribute to be limited
	 * @param type the type of the limit. Must be DoubleMimimum.class or DoubleMaximum.class.
	 * @param set the button used for the "set" action
	 * @param clear the button used fot the "clear" action
	 * @param textField the textfield to bind to the limit
	 */
	public void bindLimit(final Object entity, final DoubleAttr attr, final Class<? extends DoubleLimit> type, final JButton set, final JButton clear, final JTextField textField) {
		textField.setColumns(COLUMNS);
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		if (type == DoubleMinimum.class) {
			DoubleAttr limit = getLowerLimit(attr);
			clear.setEnabled(limit != null);
			textField.setEnabled(limit != null);
			if (limit != null) {
				bindTextFieldToAttribute(entity, textField, limit);
			} else {
				textField.setText("not set");
				textField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			}
		} else if (type == DoubleMaximum.class) {
			DoubleAttr limit = getUpperLimit(attr);
			clear.setEnabled(limit != null);
			textField.setEnabled(limit != null);
			if (limit != null) {
				bindTextFieldToAttribute(entity, textField, limit);
			} else {
				textField.setText("not set");
				textField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			}
		} else {
			throw new IllegalArgumentException(type + " must be DoubleMimimum.class or DoubleMaximum.class");
		}
		set.setEnabled(true);
		
		ActionListener setListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DoubleAttr limitAttr = new DoubleAttr(attr.getDouble());
				if (type == DoubleMinimum.class) {
					clearLowerLimit(attr);
					setLowerLimit(attr, new DoubleMinimum(limitAttr));
				} else if (type == DoubleMaximum.class) {
					clearUpperLimit(attr);
					setUpperLimit(attr, new DoubleMaximum(limitAttr));
				}
				textField.setEnabled(true);
				clear.setEnabled(true);
				if (componentBindings.containsKey(textField)) {
					unbind(textField);
				}
				bindTextFieldToAttribute(entity, textField, limitAttr);
			}
		};
		
		ActionListener clearListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (type == DoubleMinimum.class) {
					clearLowerLimit(attr);
				} else if (type == DoubleMaximum.class) {
					clearUpperLimit(attr);
				}
				clear.setEnabled(false);
				unbind(textField);
				textField.setText("not set");
				textField.setEnabled(false);
				textField.setBackground(UIManager.getColor("TextField.inactiveBackground"));
			}
		};
		
		addListener(set, setListener);
		addListener(clear, clearListener);
	}
	
	/**
	 * Adds the specified listener to the specified component and stores the listener
	 * in a collection in the componentListeners map. The following listeners are supported:<br />
	 * HierarchyListeners, FocusListeners, ActionListeners (for TextFields, ComboBoxes and AbstractButtons only) and
	 * ChangeListeners (for JSliders only).
	 * @param component the component to add the listener to. 
	 * @param listener the listener to add
	 * @throws IllegalStateException if the specified listener has already been added to the specified component
	 * @throws IllegalArgumentException if the specified listener can't be added to the specified component
	 */
	private void addListener(JComponent component, Object listener) {
//		if (component instanceof AbstractButton && listener instanceof ActionListener) {
			
////			Thread.dumpStack();
//		}
		System.out.println("adding " + listener + " to " + component.getClass().getName() + "@" + System.identityHashCode(component));
		
		Collection<Object> listeners = componentListeners.get(component);
		if (listeners == null) {
			listeners = new HashSet<Object>(4);
			componentListeners.put(component, listeners);
		}
		if (listeners.contains(listener)) {
			throw new IllegalStateException(listener + " has already beed added to " + component);
		}
		listeners.add(listener);
		
		boolean ok = false;
		if (listener instanceof ActionListener) {
			if (component instanceof Switcher) {
				((Switcher) component).asAbstractButton().addActionListener((ActionListener) listener);
				ok = true;
			} else if (component instanceof AbstractButton) {
				((AbstractButton) component).addActionListener((ActionListener) listener);
				ok = true;
			} else if (component instanceof JTextField) {
				((JTextField) component).addActionListener((ActionListener) listener);
				ok = true;
			} else if (component instanceof JComboBox) {
				((JComboBox) component).addActionListener((ActionListener) listener);
				ok = true;
			}
		}
		if (listener instanceof ChangeListener) {
			if (component instanceof JSlider) {
				((JSlider) component).addChangeListener((ChangeListener) listener);
				ok = true;
			}
		}
		if (listener instanceof FocusListener) {
			component.addFocusListener((FocusListener) listener);
			ok = true;
		}
		if (listener instanceof HierarchyListener) {
			component.addHierarchyListener((HierarchyListener) listener);
			ok = true;
		}
		if (listener instanceof ComponentListener) {
			component.addComponentListener((ComponentListener) listener);
			ok = true;
		}
		assert ok : "Can't add " + listener + " to " + component;
	}
	
	/**
	 * Removes all (previously added) listeners from the specified component. The listeners are stored
	 * in the componentListeners map.
	 * @param component the component to remove the listeners from
	 * @throws IllegalArgumentException if one of the listeners can't be removed from the specified component
	 */
	private void removeListeners(JComponent component) {
		Collection<Object> listeners = componentListeners.get(component);
		if (listeners == null) {
			return;
		}
		boolean ok = false;
		for (Object listener : listeners) {
//			if (component instanceof AbstractButton && listener instanceof ActionListener) System.out.println("removing " + listener + " from " + component.getClass().getName() + "@" + System.identityHashCode(component));
			if (listener instanceof ActionListener) {
				if (component instanceof AbstractButton) {
					((AbstractButton) component).removeActionListener((ActionListener) listener);
					ok = true;
				} else if (component instanceof JTextField) {
					((JTextField) component).removeActionListener((ActionListener) listener);
					ok = true;
				} else if (component instanceof JComboBox) {
					((JComboBox) component).removeActionListener((ActionListener) listener);
					ok = true;
				}
			} else if (listener instanceof ChangeListener) {
				if (component instanceof JSlider) {
					((JSlider) component).removeChangeListener((ChangeListener) listener);
					ok = true;
				}
			} else if (listener instanceof FocusListener) {
				component.removeFocusListener((FocusListener) listener);
				ok = true;
			} else if (listener instanceof HierarchyListener) {
				component.removeHierarchyListener((HierarchyListener) listener);
				ok = true;
			} else if (listener instanceof ComponentListener) {
				component.removeComponentListener((ComponentListener) listener);
				ok = true;
			}
			assert ok : "Can't remove " + listener + " from " + component;
		}
		System.out.println("removed listeners from " + component);
		System.out.println("remaining listeners " + component.getListeners(EventListener.class).length);
		componentListeners.remove(component);
	}
	
	/**
	 * <ul>
	 * <li>The component to attribute/listener mapping is added to the componentBindings map.</li>
	 * <li>A HierarchyListener that will add the specified AttributePostChangeListener to the specified
	 * Attribute whenever the specified Component gets shown and removes it whenever the specified Component
	 * gets hidden is created and added to the component by calling the <code>addListener</code> method.</li>
	 * <li>If the specified Component is showing when this method is called, the specifeid AttributePostChangeListener is
	 * added to the specified Attribute immediately.</li>
	 * </ul>
	 * @param component the component to be tracked
	 * @param attribute the Attribute to add/remove the specified AttributePostChangeListener to/from
	 * @param attributePostChangeListener the AttributePostChangeListener to add/remove to/from the specified Attribute
	 * @throws IllegalStateException if the specified Component is already bound (if <code>componentBindings</code> contains the specified Component as a key)
	 */
	private void bind(JComponent component, AttributeBinding... bindings) {
		if (componentBindings.containsKey(component)) {
			throw new IllegalStateException(component + " is already bound");
		}

		/* put a component to attribute/listener mapping into the componentBindings map*/
		componentBindings.put(component, bindings);

		/*
		 * add the AttributePostChangeListener if the component is currently showing
		 * (otherwise this will done by the HierarchyListener below)
		 */
		if (component.isShowing()) {
			for (AttributeBinding binding : bindings) {
				binding.bind();
			}
		}
		
		/* Create the HierarchyListener and add it */
		addListener(component, new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					JComponent component = (JComponent) e.getSource();
					AttributeBinding[] bindings = componentBindings.get(e.getSource());
					if (component.isShowing()) {
//						System.err.println("+showing:" + System.identityHashCode(component));
//						Thread.dumpStack();
						for (AttributeBinding binding : bindings) {
							binding.bind();
						}
						if (component instanceof JTextField) {
							component.setBackground(Color.WHITE);
						}
					} else {
//						System.err.println("-hiding :" + System.identityHashCode(component));
						for (AttributeBinding binding : bindings) {
							binding.unbind();
						}
					}
				}
			}
		});
	}
	
	private void fireActionPerformed(Object entity, Attribute attribute) {
		for (UserInputListener l : listenerList) {
			l.userInput(entity, attribute);
		}
	}
	
	/**
	 * Sets the JSlider's position to the value represented by the specified DoubleAttr
	 * @param slider the JSlider whose value to set
	 * @param value the value
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param mapping the mapping to use
	 */
	private static void setSliderPosition(JSlider slider, DoubleAttr value, DoubleAttr min, DoubleAttr max, Mapping mapping) {
		double mappedValue = mapping.getValue(value.getDouble());
		double mappedMin = mapping.getValue(min.getDouble());
		double mappedMax = mapping.getValue(max.getDouble());
		slider.setValue(slider.getMinimum() + (int) ((slider.getMaximum() - slider.getMinimum()) * (mappedValue - mappedMin) / (mappedMax - mappedMin)));
	}
	
	/**
	 * Sets the value DoubleAttr to the value represented by the JSlider's position
	 * @param slider the slider to whose value the DoubleAttr should be set
	 * @param value the value to set
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param mapping the mapping to use
	 */
	private static void getSliderValue(JSlider slider, DoubleAttr value, DoubleAttr min, DoubleAttr max, Mapping mapping) {
		double mappedMin = mapping.getValue(min.getDouble());
		double mappedMax = mapping.getValue(max.getDouble());
		double mappedValue = mappedMin + (mappedMax - mappedMin) * (slider.getValue() - slider.getMinimum()) / (slider.getMaximum() - slider.getMinimum());
		value.setDouble(mapping.getMappedValue(mappedValue));
	}
	
	/**
	 * A simple structure that stores Attributes and AttributePostChangeListener.
	 * It's only used in the <code>componentBindings</code> map.
	 */
	private static final class AttributeBinding {
		final Attribute attribute;
		final AttributePostChangeListener listener;

		AttributeBinding(Attribute attribute, AttributePostChangeListener listener) {
			this.attribute = attribute;
			this.listener = listener;
		}
		
		void bind() {
			attribute.addAttributePostChangeListener(listener);
			/*
			 * Cause the listener to receive the current value by faking
			 * an attributeHasChanged event - TODO that's a hack, is there a better way?
			 */
			listener.attributeHasChanged(attribute);
		}
		
		void unbind() {
			attribute.removeAttributePostChangeListener(listener);
		}
	}
	
	private static final String formatDouble(JTextField textField, double d) {
		int digits = Math.min(8, textField.getWidth() / textField.getFontMetrics(textField.getFont()).getWidths()['0'] - 1);
		
		int intDigits = getIntegerDigits(d);
		if (intDigits > digits) {
			int expDigits = (Math.abs(d) >= 1E10) ? 5 : 4;
			SCI_FORMAT.setMinimumFractionDigits(digits - expDigits);
			SCI_FORMAT.setMaximumFractionDigits(digits - expDigits);
			return SCI_FORMAT.format(d);
		}
		int fractionDigits = getFractionDigits(d);
		if (fractionDigits > digits) {
			int expDigits;
			if (Math.abs(d) >= 1e100) expDigits = 6;
			else if (Math.abs(d) >= 1e10) expDigits = 5;
			else if (Math.abs(d) <= 1e-10) expDigits = 6;
			else if (Math.abs(d) <= 1) expDigits = 5;
			else expDigits = 4;
			SCI_FORMAT.setMinimumFractionDigits(digits - expDigits);
			SCI_FORMAT.setMaximumFractionDigits(digits - expDigits);
			return SCI_FORMAT.format(d);
		}
		setFractionDigits(Math.min(Math.max(3, fractionDigits), digits - intDigits));
//		if (ad >= 10000) return SCI_FORMAT.format(d);
//		if (ad >= 1000) setFractionDigits(0);
//		else if (ad >= 100) setFractionDigits(1);
//		else if (ad >= 10) setFractionDigits(2);
//		else setFractionDigits(3);
		return DOUBLE_FORMAT.format(d);
	}
	
	private static int getIntegerDigits(double d) {
		double ad = Math.abs(d);
		int sign = (d < 0) ? 1 : 0;
		sign = 1;
		if (ad >= 1000000000) return 10 + sign;
		else if (ad >= 100000000) return 9 + sign;
		else if (ad >= 10000000) return 8 + sign;
		else if (ad >= 1000000) return 7 + sign;
		else if (ad >= 100000) return 6 + sign;
		else if (ad >= 10000) return 5 + sign;
		else if (ad >= 1000) return 4 + sign;
		else if (ad >= 100) return 3 + sign;
		else if (ad >= 10) return 2 + sign;
		else return 1 + sign;
	}
	
	private static int getFractionDigits(double d) {
		double ad = Math.abs(d);
		int sign = (d < 0) ? 1 : 0;
		sign = 1;
		if (ad == 0) return 1 + sign;
		else if (ad < 0.000000001) return 10 + sign;
		else if (ad < 0.00000001) return 9 + sign;
		else if (ad < 0.0000001) return 8 + sign;
		else if (ad < 0.000001) return 7 + sign;
		else if (ad < 0.00001) return 6 + sign;
		else if (ad < 0.0001) return 5 + sign;
		else if (ad < 0.001) return 4 + sign;
		else if (ad < 0.01) return 3 + sign;
		else if (ad < 0.1) return 2 + sign;
		else return 1 + sign;
	}
	private static void setFractionDigits(int digits) {
		DOUBLE_FORMAT.setMinimumFractionDigits(digits);
		DOUBLE_FORMAT.setMaximumFractionDigits(digits);
	}
}
