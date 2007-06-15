package com.jpatch.afw.ui;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.jpatch.afw.attributes.*;

public class AttributeManager {
	private final Set<Attribute> locks = new HashSet<Attribute>();
	private final Map<DoubleAttr, DoubleMaximum> upperLimits = new HashMap<DoubleAttr, DoubleMaximum>();
	private final Map<DoubleAttr, DoubleMaximum> lowerLimits = new HashMap<DoubleAttr, DoubleMaximum>();
	private final Map<JComponent, Collection<Object>> componentListeners = new HashMap<JComponent, Collection<Object>>();
	private final Map<JComponent, AttributeBinding> componentBindings = new HashMap<JComponent, AttributeBinding>();
	
	/**
	 * Locks the specified attribute
	 * @param attr the attribute to be locked
	 * @throws IllegalArgumentException if the specified attribute has already been locked
	 */
	public void lock(Attribute attr) {
		attr.addAttributePreChangeListener(Lock.getInstance());		// throws IllegalArgumentException if already locked
		locks.add(attr);
	}
	
	/**
	 * Unlocks the specified attribute
	 * @param attr the attribute to be unlocked
	 * @throws IllegalArgumentException if the specified attribute has not been locked
	 */
	public void unlock(Attribute attr) {
		attr.removeAttributePreChangeListener(Lock.getInstance());	// throws IllegalArgumentException if already locked
		locks.remove(attr);
	}
	
	/**
	 * Returns true if the specified attribute is locked, false otherwise
	 * @param attr the attribute to check for a lock
	 * @return true if the specified attribute is locked, false otherwise
	 */
	public boolean isLocked(Attribute attr) {
		return locks.contains(attr);
	}
	
	/**
	 * Sets the upper limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the upper limit for the specified attribute
	 * @return the upper limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an upper limit set
	 */
	public DoubleMaximum setUpperLimit(DoubleAttr attr, DoubleMaximum limit) {
		if (upperLimits.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set an upper limit: " + upperLimits.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		return limit;
	}
	
	/**
	 * Clears the upper limit for the specified attribute
	 * @param attr the attribute whose upper limit should be cleared
	 * * @throws IllegalArgumentException if the specified attribute does not have an upper limit set
	 */
	public void clearUpperLimit(DoubleAttr attr) {
		DoubleLimit limit = upperLimits.get(attr);
		if (limit == null) {
			throw new IllegalStateException(attr + " has not set an upper limit");
		}
		attr.removeAttributePreChangeListener(limit);
	}
	
	/**
	 * Sets the lower limit for the specified attribute
	 * @param attr the attribute to be bounded
	 * @param limit the lower limit for the specified attribute
	 * @return the lower limit for the specified attribute
	 * @throws IllegalArgumentException if the specified attribute already has an lower limit set
	 */
	public DoubleMaximum setLowerLimit(DoubleAttr attr, DoubleMaximum limit) {
		if (lowerLimits.containsKey(attr)) {
			throw new IllegalStateException(attr + " has already set an lower limit: " + lowerLimits.get(attr));
		}
		attr.addAttributePreChangeListener(limit);
		return limit;
	}
	
	/**
	 * Clears the lower limit for the specified attribute
	 * @param attr the attribute whose lower limit should be cleared
	 * * @throws IllegalArgumentException if the specified attribute does not have an lower limit set
	 */
	public void clearLowerLimit(DoubleAttr attr) {
		DoubleLimit limit = lowerLimits.get(attr);
		if (limit == null) {
			throw new IllegalStateException(attr + " has not set an lower limit");
		}
		attr.removeAttributePreChangeListener(limit);
	}
	
	/**
	 * Adds the specified listener to the specified component and stores the listener
	 * in a collection in the componentListeners map.
	 * @param component the component to add the listener to
	 * @param listener the listener to add
	 * @throws IllegalStateException if the specified listener has already been added to the specified component
	 * @throws IllegalArgumentException if the specified listener can't be added to the specified component
	 */
	private void addListener(JComponent component, Object listener) {
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
	 * @throws IllegalStateException if there is no entry for the specified component in the componentListeners map
	 * @throws IllegalArgumentException if one of the listeners can't be removed from the specified component
	 */
	private void removeListeners(JComponent component) {
		Collection<Object> listeners = componentListeners.get(component);
		if (listeners == null) {
			throw new IllegalStateException(component + " doesn't have managed listeners attached");
		}
		for (Object listener : listeners) {
			if (listener instanceof ActionListener) {
				if (component instanceof AbstractButton) {
					((AbstractButton) component).removeActionListener((ActionListener) listener);
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
	private void bind(JComponent component, Attribute attribute, AttributePostChangeListener listener) {
		if (componentBindings.containsKey(component)) {
			throw new IllegalStateException(component + " is already bound");
		}

		/* put a component to attribute/listener mapping into the componentBindings map*/
		componentBindings.put(component, new AttributeBinding(attribute, listener));

		/*
		 * add the AttributePostChangeListener if the component is currently showing
		 * (otherwise this has will done by the HierarchyListener below)
		 */
		if (component.isShowing()) {
			attribute.addAttributePostChangeListener(listener);
		}
		
		/* Create the HierarchyListener and add it */
		addListener(component, new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					JComponent component = (JComponent) e.getSource();
					AttributeBinding binding = componentBindings.get(e.getSource());
					if (component.isShowing()) {
						binding.attribute.addAttributePostChangeListener(binding.listener);
					} else {
						binding.attribute.removeAttributePostChangeListener(binding.listener);
					}
				}
			}
		});
	}
	
	/**
	 * Unbinds the specified Component from the Attribute its bound to.
	 * @param component the Component to unbind
	 * @throws IllegalArgumentException if the specified Component is not bound to an Attribute.
	 */
	public void unbind(JComponent component) {
		AttributeBinding binding = componentBindings.get(component);
		if (binding == null) {
			throw new IllegalArgumentException(component + " is not bound");
		}

		/*
		 * remove the AttributePostChangeListener if the component is currently showing
		 * (otherwise this has been done by the HierarchyListener already)
		 */
		if (component.isShowing()) {
			binding.attribute.removeAttributePostChangeListener(binding.listener);
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
	 */
	public JCheckBox bindCheckBoxToAttribute(final JCheckBox checkBox, final BooleanAttr booleanAttr) {
		checkBox.setSelected(booleanAttr.getBoolean());
		
		/* create an AttributePostChangeListener to listen for attribute changes und update the checkbox */
		AttributePostChangeListener attrListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				checkBox.setSelected(booleanAttr.getBoolean());
			}
		};
		
		/* bind attribute and attrListener to component */
		bind(checkBox, booleanAttr, attrListener);
		
		/* create and add an ActionListener */
		addListener(checkBox, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				booleanAttr.setBoolean(checkBox.isSelected());
			}
		});
		
		return checkBox;
	}
	
	/**
	 * A simple structure that stores Attributes and AttributePostChangeListener.
	 * It's only used in the <code>componentBindings</code> map.
	 */
	private static class AttributeBinding {
		Attribute attribute;
		AttributePostChangeListener listener;
		AttributeBinding(Attribute attribute, AttributePostChangeListener listener) {
			this.attribute = attribute;
			this.listener = listener;
		}
	}
}
