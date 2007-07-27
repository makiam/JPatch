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
	private final Map<DoubleAttr, DoubleMaximum> upperLimits = new HashMap<DoubleAttr, DoubleMaximum>();
	
	/**
	 * Maps DoubleAttr to their lower limits
	 */
	private final Map<DoubleAttr, DoubleMinimum> lowerLimits = new HashMap<DoubleAttr, DoubleMinimum>();
	
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
		if (upperLimits.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set an upper limit: " + upperLimits.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		limit.getAttr().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				attr.setDouble(limit.attributeWillChange(attr, attr.getDouble()));
			}
		});
		upperLimits.put(attr, limit);
		return limit;
	}
	
	/**
	 * Clears the upper limit for the specified attribute
	 * @param attr the attribute whose upper limit should be cleared
	 */
	public void clearUpperLimit(DoubleAttr attr) {
		DoubleLimit limit = upperLimits.get(attr);
		if (limit != null) {
			attr.removeAttributePreChangeListener(limit);
			upperLimits.remove(attr);
		}
	}
	
	/**
	 * Returns the upper limit of the specified attribute
	 * @param attr the attribute whose upper limit should be returned
	 * @return the upper limit of the specified attribute, or null if no upper limit is set
	 */
	public DoubleAttr getUpperLimit(DoubleAttr attr) {
		DoubleMaximum limit = upperLimits.get(attr);
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
		if (lowerLimits.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set a lower limit: " + lowerLimits.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		limit.getAttr().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				attr.setDouble(limit.attributeWillChange(attr, attr.getDouble()));
			}
		});
		lowerLimits.put(attr, limit);
		return limit;
	}
	
	/**
	 * Clears the lower limit for the specified attribute
	 * @param attr the attribute whose lower limit should be cleared
	 */
	public void clearLowerLimit(DoubleAttr attr) {
		DoubleLimit limit = lowerLimits.get(attr);
		if (limit != null) {
			attr.removeAttributePreChangeListener(limit);
		}
		lowerLimits.remove(attr);
	}
	
	/**
	 * Returns the lower limit of the specified attribute
	 * @param attr the attribute whose lower limit should be returned
	 * @return the lower limit of the specified attribute, or null if no lower limit is set
	 */
	public DoubleAttr getLowerLimit(DoubleAttr attr) {
		DoubleMinimum limit = lowerLimits.get(attr);
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
	 * Binds the specified JCheckBox to the specified Attribute.
	 * @param checkBox
	 * @param booleanAttr
	 * @return the specified JCheckBox
	 * @throws NullPointerException if any of the specified parameters is null
	 * @throws IllegalStateException if the specified JCheckBox is already bound (to <i>any</i> Attribute)
	 */
	public JCheckBox bindCheckBoxToAttribute(final Object entity, final JCheckBox checkBox, final BooleanAttr booleanAttr) {
//		checkBox.setSelected(booleanAttr.getBoolean());
		
		/* create an AttributePostChangeListener to listen for attribute changes und update the checkbox */
		AttributePostChangeListener attrListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				checkBox.setSelected(booleanAttr.getBoolean());
			}
		};
		
		/* bind attribute and attrListener to component */
		bind(checkBox, new AttributeBinding(booleanAttr, attrListener));						// throws IllegalStateException if already bound
		
		/* create and add an ActionListener */
		addListener(checkBox, new ActionListener() {					// throws IllegalStateException if already bound
			public void actionPerformed(ActionEvent e) {
				booleanAttr.setBoolean(checkBox.isSelected());
				fireActionPerformed(entity, booleanAttr);
			}
		});
		
		return checkBox;
	}
	
	public JTextField bindTextFieldToAttribute(Object entity, final JTextField textField, ScalarAttribute attribute) {
		if (attribute instanceof DoubleAttr) {
			return bindTextFieldToAttribute(entity, textField, (DoubleAttr) attribute);
		} else if (attribute instanceof GenericAttr) {
			return bindTextFieldToAttribute(entity, textField, (GenericAttr<String>) attribute);
		} else {
			throw new IllegalArgumentException("can't bind " + attribute + " to " + textField);
		}
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
		
		
		
		class SuperListener extends FocusAdapter implements FocusListener, AttributePostChangeListener {
			private boolean suppressAction = false;

			public void focusLost(FocusEvent e) {
				if (!suppressAction) {
					suppressAction = true;
					try {
						double value = Double.parseDouble(textField.getText());
						if (value != doubleAttr.getDouble()) {
							doubleAttr.setDouble(Double.parseDouble(textField.getText()));
							textField.setBackground(textField.isEnabled() ? UIManager.getColor("TextField.background") : UIManager.getColor("TextField.inactiveBackground"));
							textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
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
					textField.setText(DOUBLE_FORMAT.format(doubleAttr.getDouble()));
					suppressAction = false;
				}
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
//			System.out.println("adding " + listener + " to " + component.getClass().getName() + "@" + System.identityHashCode(component));
////			Thread.dumpStack();
//		}
		Collection<Object> listeners = componentListeners.get(component);
		if (listeners == null) {
			listeners = new HashSet<Object>(4);
			componentListeners.put(component, listeners);
		}
		if (listeners.contains(listener)) {
			throw new IllegalStateException(listener + " has already beed added to " + component);
		}
		listeners.add(listener);
		
		if (listener instanceof ActionListener) {
			if (component instanceof AbstractButton) {
				((AbstractButton) component).addActionListener((ActionListener) listener);
				return;
			} else if (component instanceof JTextField) {
				((JTextField) component).addActionListener((ActionListener) listener);
				return;
			} else if (component instanceof JComboBox) {
				((JComboBox) component).addActionListener((ActionListener) listener);
				return;
			}
		} else if (listener instanceof ChangeListener) {
			if (component instanceof JSlider) {
				((JSlider) component).addChangeListener((ChangeListener) listener);
				return;
			}
		} else if (listener instanceof FocusListener) {
			component.addFocusListener((FocusListener) listener);
			return;
		} else if (listener instanceof HierarchyListener) {
			component.addHierarchyListener((HierarchyListener) listener);
			return;
		}
		throw new IllegalArgumentException("Can't add " + listener + " to " + component);
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
		for (Object listener : listeners) {
//			if (component instanceof AbstractButton && listener instanceof ActionListener) System.out.println("removing " + listener + " from " + component.getClass().getName() + "@" + System.identityHashCode(component));
			if (listener instanceof ActionListener) {
				if (component instanceof AbstractButton) {
					((AbstractButton) component).removeActionListener((ActionListener) listener);
					continue;
				} else if (component instanceof JTextField) {
					((JTextField) component).removeActionListener((ActionListener) listener);
					continue;
				} else if (component instanceof JComboBox) {
					((JComboBox) component).removeActionListener((ActionListener) listener);
					continue;
				}
			} else if (listener instanceof ChangeListener) {
				if (component instanceof JSlider) {
					((JSlider) component).removeChangeListener((ChangeListener) listener);
					continue;
				}
			} else if (listener instanceof FocusListener) {
				component.removeFocusListener((FocusListener) listener);
				continue;
			} else if (listener instanceof HierarchyListener) {
				component.removeHierarchyListener((HierarchyListener) listener);
				continue;
			}
			throw new IllegalArgumentException("Can't remove " + listener + " from " + component);
		}
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
		 * (otherwise this has will done by the HierarchyListener below)
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
						for (AttributeBinding binding : bindings) {
							binding.bind();
						}
						if (component instanceof JTextField) {
							component.setBackground(Color.WHITE);
						}
					} else {
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
}
