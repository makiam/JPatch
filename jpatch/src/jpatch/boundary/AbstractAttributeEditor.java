/*
 * $Id:$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.*;

import jpatch.boundary.ui.*;
import jpatch.entity.*;
import jpatch.entity.attributes2.*;


/**
 * @author sascha
 *
 */
public class AbstractAttributeEditor extends ExpandableFormContainer {
	
	public static enum Type { ATTRIBUTE, LIMIT }
	
	public AbstractAttributeEditor(Object object, Item[][] items) {
		System.out.println("new AbstractAttributeEditor(" + object + ", " + items);
		System.out.println("items:");
		for (int i = 0; i < items.length; i++) {
			for (int j = 0; j < items[i].length; j++) {
				System.out.println(items[i][j]);
			}
			System.out.println();
		}
		for (int section = 0; section < items.length; section++) {
			beginSection();
			for (int item = 0; item < items[section].length; item++) {
				try {
					switch (items[section][item].type) {
					case ATTRIBUTE:
//						if (items[section][item].widget != null && items[section][item].widget.equals("slider")) {
//							addSlider(container, items[section][item].name, (Attribute.Double) items[section][item].field.get(object), items[section][item].min, items[section][item].max);
//						}
						addAttribute(items[section][item].name, items[section][item].method.invoke(object, (Object[]) null));
						break;
					case LIMIT:
//						addLimit(container, (Attribute.Tuple3) items[section][item].field.get(object));
						break;
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			endSection();
		}
	}
	
	private Container container;
	
	public void beginSection() {
		assert container == null : "beginSection called within a section.";
		container = new ExpandableForm();
	}
	
	public void endSection() {
		assert container != null : "endSection called outside a section.";
		add(container);
		container = null;
	}
	
	public void addAttribute(String name, Object object) {
		assert container != null : "addAttribute called outside a section.";
		if (object instanceof Attribute) {
			addScalar(container, name, (Attribute) object);
		} else {
			container.add(new JLabel(name));
			JComponent box = new ExpandableFormRow();
			box.setOpaque(false);
			try {
				for (Method method : object.getClass().getMethods()) {
					if (Attribute.class.isAssignableFrom(method.getReturnType())) {
						box.add(AttributeUiHelper.createTextFieldFor(name, (Attribute) method.invoke(object, (Object[]) null)));
					}
				}
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
			container.add(box);
		}
	}
	
	protected void addSlider(Container c, String name, Attribute.Double a, double min, double max) {
		c.add(new JLabel(name));
		Box box = Box.createHorizontalBox();
		box.add(AttributeUiHelper.createSliderFor(a, min, max));
		c.add(box);
	}
	
	protected void addScalar(Container c, String name, Attribute a) {
		c.add(new JLabel(name));
		Box box = Box.createHorizontalBox();
//		if (a instanceof Attribute.KeyedBoolean) {
//			box.add(AttributeUiHelper.createBooleanComboFor(a));
		if (a instanceof BooleanAttr) {
			box.add(AttributeUiHelper.createCheckBoxFor(name, (BooleanAttr) a));
//		} else if (a instanceof Attribute.Enum) {
//			box.add(AttributeUiHelper.createComboBoxFor((Attribute.Enum) a));
		} else if (a instanceof ArrayAttr) {
			box.add(AttributeUiHelper.createComboBoxFor((ArrayAttr) a));
//		} else {
//			box.add(AttributeUiHelper.createTextFieldFor(a));
		} else {
			box.add(AttributeUiHelper.createTextFieldFor(name, a));
		}
		c.add(box);
	}
	
	protected void addFileSelector(Container c, String name, final Attribute attribute, int fileSelectionMode, final String approveButtonText) {
		c.add(new JLabel(name));
		final JButton button = new JButton("Browse...");
		final Box box = Box.createHorizontalBox();
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(fileSelectionMode);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component parent;
				for (parent = box; parent.getParent() != null; parent = parent.getParent());
				if (fileChooser.showDialog(parent, approveButtonText) == JFileChooser.APPROVE_OPTION) {
					try {
						((Attribute.String) attribute).set(fileChooser.getSelectedFile().getCanonicalPath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		box.add(AttributeUiHelper.createTextFieldFor(attribute, 200));
		box.add(Box.createHorizontalStrut(4));
		box.add(button);
		c.add(box);
	}
	
	protected void addTuple(Container c, String name, Tuple2 a) {
		c.add(new JLabel(name));
		JComponent box = new ExpandableFormRow();
		box.setOpaque(false);
		
		box.add(AttributeUiHelper.createTextFieldFor(a.x));
		box.add(AttributeUiHelper.createTextFieldFor(a.y));
		c.add(box);

//		if (!a.isKeyable())
//			return;
//		
//		c.add(new JLabel("keyed"));
//		box = new ExpandableFormRow();
//		box.setOpaque(false);
//		if (a.isKeyable()) {
//			JCheckBox keyX = AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.x).keyed);
//			JCheckBox keyY = AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.y).keyed);
//			keyX.setEnabled(a.isKeyable());
//			keyY.setEnabled(a.isKeyable());
//			box.add(keyX);
//			box.add(keyY);
//		}
//		c.add(box);
//		
//		c.add(new JLabel("locked"));
//		box = new ExpandableFormRow();
//		box.setOpaque(false);
//		JCheckBox lockX = AttributeUiHelper.createCheckBoxFor(a.x.locked);
//		JCheckBox lockY = AttributeUiHelper.createCheckBoxFor(a.y.locked);
//		box.add(lockX);
//		box.add(lockY);
//		c.add(box);
	}
	
	protected void addTuple(Container c, String name, Attribute.Tuple3 a) {
		System.out.println("addTuple(" + c + ", " + name + ", " + a);
		c.add(new JLabel(name));
		JComponent box = new ExpandableFormRow();
		box.setOpaque(false);
		
		box.add(AttributeUiHelper.createTextFieldFor(a.x));
		box.add(AttributeUiHelper.createTextFieldFor(a.y));
		box.add(AttributeUiHelper.createTextFieldFor(a.z));
		c.add(box);

		if (!a.isKeyable())
			return;
		
		c.add(new JLabel("keyed"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		if (a.isKeyable()) {
			JCheckBox keyX = AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.x).keyed);
			JCheckBox keyY = AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.y).keyed);
			JCheckBox keyZ = AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.z).keyed);
			keyX.setEnabled(a.isKeyable());
			keyY.setEnabled(a.isKeyable());
			keyZ.setEnabled(a.isKeyable());
			box.add(keyX);
			box.add(keyY);
			box.add(keyZ);
		}
		c.add(box);
		
		c.add(new JLabel("locked"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		JCheckBox lockX = AttributeUiHelper.createCheckBoxFor(a.x.locked);
		JCheckBox lockY = AttributeUiHelper.createCheckBoxFor(a.y.locked);
		JCheckBox lockZ = AttributeUiHelper.createCheckBoxFor(a.z.locked);
		box.add(lockX);
		box.add(lockY);
		box.add(lockZ);
		c.add(box);
	}
	
	protected void addLimit(Container c, final Attribute.Tuple3 a) {
		c.add(new JLabel("Minimum"));
		JComponent box = new ExpandableFormRow();
		box.setOpaque(false);
		
		box.add(AttributeUiHelper.createTextFieldFor(((Attribute.BoundedDouble) a.x).min));
		box.add(AttributeUiHelper.createTextFieldFor(((Attribute.BoundedDouble) a.y).min));
		box.add(AttributeUiHelper.createTextFieldFor(((Attribute.BoundedDouble) a.z).min));
		c.add(box);
		
		c.add(new JLabel("Enable min"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.x).min.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.y).min.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.z).min.enabled));
		c.add(box);
		
		c.add(new JLabel("Set min"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		JButton button;
		
		Insets insets = new Insets(0, 4, 0, 4);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set x.min to current x value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Attribute.BoundedDouble) a.x).min.set(a.x.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set y.min to current y value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Attribute.BoundedDouble) a.y).min.set(a.y.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set z.min to current z value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Attribute.BoundedDouble) a.z).min.set(a.z.get());
			}
		});
		box.add(button);
		c.add(box);
		
		c.add(new JLabel("Maximum"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(AttributeUiHelper.createTextFieldFor(((Attribute.BoundedDouble) a.x).max));
		box.add(AttributeUiHelper.createTextFieldFor(((Attribute.BoundedDouble) a.y).max));
		box.add(AttributeUiHelper.createTextFieldFor(((Attribute.BoundedDouble) a.z).max));
		c.add(box);
		
		c.add(new JLabel("Enable max"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.x).max.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.y).max.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(((Attribute.BoundedDouble) a.z).max.enabled));
		c.add(box);
		
		c.add(new JLabel("Set max"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set x.min to current x value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Attribute.BoundedDouble) a.x).max.set(a.x.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set y.min to current y value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Attribute.BoundedDouble) a.y).max.set(a.y.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set z.min to current z value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Attribute.BoundedDouble) a.z).max.set(a.z.get());
			}
		});
		box.add(button);
		c.add(box);
		
	}
	
	public final static class Item {
		public final Type type;
		public final String name;
		public final Method method;
		
		public Item(Type type, String name, Method method) {
			this.type = type;
			this.name = name;
			this.method = method;
		}
		
		public String toString() {
			return type + " " + name + " " + method;
		}
	}
}
