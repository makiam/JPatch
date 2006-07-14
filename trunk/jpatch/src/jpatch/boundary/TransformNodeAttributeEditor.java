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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import jpatch.boundary.ui.ExpandableForm;
import jpatch.boundary.ui.ExpandableFormContainer;
import jpatch.boundary.ui.ExpandableFormRow;
import jpatch.entity.*;
/**
 * @author sascha
 *
 */
public class TransformNodeAttributeEditor extends ExpandableFormContainer {
	private TransformNode transformNode;
	
	public TransformNodeAttributeEditor(TransformNode transformNode) {
		this.transformNode = transformNode;
		ExpandableForm defaultForm = new ExpandableForm(true);
		ExpandableForm translationForm = new ExpandableForm();
		ExpandableForm positionForm = new ExpandableForm();
		ExpandableForm rotationForm = new ExpandableForm();
		ExpandableForm orientationForm = new ExpandableForm();
		ExpandableForm scaleForm = new ExpandableForm();
		ExpandableForm shearForm = new ExpandableForm();
		
		addScalar(defaultForm, transformNode.name);
		addScalar(defaultForm, transformNode.visibility);
		addScalar(defaultForm, transformNode.rotationOrder);
		addTuple(translationForm, transformNode.translation);
		addLimit(translationForm, transformNode.translation);
		addTuple(positionForm, transformNode.position);
		addTuple(rotationForm, transformNode.rotation);
		addLimit(rotationForm, transformNode.rotation);
		addTuple(rotationForm, transformNode.rotatePivotTranslation);
		addTuple(rotationForm, transformNode.rotatePivotPosition);
		
		addTuple(orientationForm, transformNode.orientation);
		
//		addScalar(transformNode.rotation.order);
		addTuple(scaleForm, transformNode.scale);
//		addLimit(scaleForm, transformNode.scale);
		addTuple(scaleForm, transformNode.scalePivotTranslation);
		addTuple(scaleForm, transformNode.scalePivotPosition);
		addTuple(shearForm, transformNode.shear);
		
//		addTuple(shear, transformNode.scale);
//		addTuple(transformNode.rotatePivotPosition);
//		addTuple(transformNode.rotatePivotTranslation);
//		addTuple(transformNode.scalePivotPosition);
//		addTuple(transformNode.scalePivotTranslation);
//		addLimit(transformNode.translation.x);
//		addLimit(transformNode.translation.y);
//		addLimit(transformNode.translation.z);
//		addLimit(transformNode.rotation.x);
//		addLimit(transformNode.rotation.y);
//		addLimit(transformNode.rotation.z);
//		addLimit(transformNode.scale.x);
//		addLimit(transformNode.scale.y);
//		addLimit(transformNode.scale.z);
		
		add(defaultForm);
		add(translationForm);
		add(positionForm);
		add(rotationForm);
		add(orientationForm);
		add(scaleForm);
		add(shearForm);
	}
	
//	public void paintComponent(Graphics g) {
//		Graphics2D g2 = (Graphics2D) g;
//		g2.setPaint(new GradientPaint(0, 0, new Color(0xeeeeee), 0, getHeight(), new Color(0xcccccc)));
//		g2.fill(g.getClip());
//	};
	
	private void addScalar(Container c, Attribute a) {
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
	
	private void addTuple(Container c, Attribute.Tuple3d a) {
		JComponent component;
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
	
	private void addLimit(Container c, final Attribute.Tuple3d a) {
		c.add(new JLabel("Minimum"));
		JComponent box = new ExpandableFormRow();
		box.setOpaque(false);
		JTextField minX = AttributeUiHelper.createTextFieldFor(a.x.min);
		JTextField minY = AttributeUiHelper.createTextFieldFor(a.y.min);
		JTextField minZ = AttributeUiHelper.createTextFieldFor(a.z.min);
		JTextField maxX = AttributeUiHelper.createTextFieldFor(a.x.max);
		JTextField maxY = AttributeUiHelper.createTextFieldFor(a.y.max);
		JTextField maxZ = AttributeUiHelper.createTextFieldFor(a.z.max);
		
		box.add(minX);
		box.add(minY);
		box.add(minZ);
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
//	private void addLimitLine(String name, Attribute x, Attribute y, Attribute z) {
//		gbc.anchor = GridBagConstraints.EAST;
//		gbc.gridx = 0;
//		add(new JLabel(name), gbc);
////		gbc.anchor = GridBagConstraints.WEST;
//		gbc.gridx = 1;
//		add(AttributeUiHelper.createLimitBox(x), gbc);
//		gbc.gridx = 2;
//		add(AttributeUiHelper.createLimitBox(y), gbc);
//		gbc.gridx = 3;
//		add(AttributeUiHelper.createLimitBox(z), gbc);
//		gbc.gridy++;
//	}
	private static Icon createIcon(final int type, final Color color) {
		return new Icon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g.setColor(color);
				switch (type) {
				case 0:
					g.fillPolygon(new int[] { 3, 3, 8}, new int[] { 2, 12, 7 }, 3);
					break;
				case 1:
					g.fillPolygon(new int[] { 9, 9, 4}, new int[] { 2, 12, 7 }, 3);
					break;
				}
			}
			
			public int getIconWidth() {
				return 8;
			}
			
			public int getIconHeight() {
				return 10;
			}
		};
	}
	
}
