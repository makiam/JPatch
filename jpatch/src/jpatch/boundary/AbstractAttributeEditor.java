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

import javax.swing.*;

import jpatch.boundary.ui.*;
import jpatch.entity.*;
/**
 * @author sascha
 *
 */
public class AbstractAttributeEditor extends ExpandableFormContainer {
	
	protected void addScalar(Container c, Attribute a) {
		c.add(new JLabel(a.getName()));
		Box box = Box.createHorizontalBox();
		if (a instanceof Attribute.KeyedBoolean) {
			box.add(AttributeUiHelper.createBooleanComboFor(a));
		} else if (a instanceof Attribute.Boolean) {
			box.add(AttributeUiHelper.createCheckBoxFor(a));
		} else if (a instanceof Attribute.Enum) {
			box.add(AttributeUiHelper.createComboBoxFor(a));
		} else {
			box.add(AttributeUiHelper.createTextFieldFor(a));
		}
		c.add(box);
	}
	
	protected void addTuple(Container c, Attribute.Tuple a) {
		c.add(new JLabel(a.getName()));
		JComponent box = new ExpandableFormRow();
		box.setOpaque(false);
		JCheckBox keyX = AttributeUiHelper.createCheckBoxFor(a.x.keyed);
		JCheckBox keyY = AttributeUiHelper.createCheckBoxFor(a.y.keyed);
		JCheckBox keyZ = AttributeUiHelper.createCheckBoxFor(a.z.keyed);
		JCheckBox lockX = AttributeUiHelper.createCheckBoxFor(a.x.locked);
		JCheckBox lockY = AttributeUiHelper.createCheckBoxFor(a.y.locked);
		JCheckBox lockZ = AttributeUiHelper.createCheckBoxFor(a.z.locked);
		keyX.setEnabled(a.isKeyable());
		keyY.setEnabled(a.isKeyable());
		keyZ.setEnabled(a.isKeyable());

		box.add(AttributeUiHelper.createTextFieldFor(a.x));
		box.add(AttributeUiHelper.createTextFieldFor(a.y));
		box.add(AttributeUiHelper.createTextFieldFor(a.z));
		c.add(box);

		if (!a.isKeyable())
			return;
		
		c.add(new JLabel("keyed"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(keyX);
		box.add(keyY);
		box.add(keyZ);
		c.add(box);
		
		c.add(new JLabel("locked"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(lockX);
		box.add(lockY);
		box.add(lockZ);
		c.add(box);
	}
	
	protected void addLimit(Container c, final Attribute.Tuple a) {
		c.add(new JLabel("Minimum"));
		JComponent box = new ExpandableFormRow();
		box.setOpaque(false);
		
		box.add(AttributeUiHelper.createTextFieldFor(a.x.min));
		box.add(AttributeUiHelper.createTextFieldFor(a.y.min));
		box.add(AttributeUiHelper.createTextFieldFor(a.z.min));
		c.add(box);
		
		c.add(new JLabel("Enable min"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(AttributeUiHelper.createCheckBoxFor(a.x.min.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(a.y.min.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(a.z.min.enabled));
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
				a.x.min.set(a.x.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set y.min to current y value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.y.min.set(a.y.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set z.min to current z value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.z.min.set(a.z.get());
			}
		});
		box.add(button);
		c.add(box);
		
		c.add(new JLabel("Maximum"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(AttributeUiHelper.createTextFieldFor(a.x.max));
		box.add(AttributeUiHelper.createTextFieldFor(a.y.max));
		box.add(AttributeUiHelper.createTextFieldFor(a.z.max));
		c.add(box);
		
		c.add(new JLabel("Enable max"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		box.add(AttributeUiHelper.createCheckBoxFor(a.x.max.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(a.y.max.enabled));
		box.add(AttributeUiHelper.createCheckBoxFor(a.z.max.enabled));
		c.add(box);
		
		c.add(new JLabel("Set max"));
		box = new ExpandableFormRow();
		box.setOpaque(false);
		
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set x.min to current x value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.x.max.set(a.x.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set y.min to current y value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.y.max.set(a.y.get());
			}
		});
		box.add(button);
		button = new JButton("set");
		button.setMargin(insets);
		button.setToolTipText("Set z.min to current z value");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.z.max.set(a.z.get());
			}
		});
		box.add(button);
		c.add(box);
		
	}	
}
