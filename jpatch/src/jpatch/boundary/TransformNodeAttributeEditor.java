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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import jpatch.entity.*;
/**
 * @author sascha
 *
 */
public class TransformNodeAttributeEditor extends JPanel {
	private TransformNode transformNode;
	private GridBagConstraints gbc = new GridBagConstraints();
	
	public TransformNodeAttributeEditor(TransformNode transformNode) {
		this.transformNode = transformNode;
		setLayout(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 5;
		
		addScalar(transformNode.name);
		addScalar(transformNode.visibility);
		addTuple(transformNode.position);
		addTuple(transformNode.translation);
		addTuple(transformNode.orientation);
		addTuple(transformNode.rotation);
		addScalar(transformNode.rotationOrder);
		addTuple(transformNode.scale);
		addTuple(transformNode.rotatePivotPosition);
		addTuple(transformNode.rotatePivotTranslation);
		addTuple(transformNode.scalePivotPosition);
		addTuple(transformNode.scalePivotTranslation);
		addLimit(transformNode.translation.x);
		addLimit(transformNode.translation.y);
		addLimit(transformNode.translation.z);
		addLimit(transformNode.rotation.x);
		addLimit(transformNode.rotation.y);
		addLimit(transformNode.rotation.z);
		addLimit(transformNode.scale.x);
		addLimit(transformNode.scale.y);
		addLimit(transformNode.scale.z);
	}
	
	private void addScalar(Attribute a) {
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		add(new JLabel(a.getName()), gbc);
		gbc.anchor = GridBagConstraints.WEST;
		
		gbc.gridx = 1;
		Box box = Box.createHorizontalBox();
		if (a instanceof Attribute.Boolean) {
			box.add(AttributeUiHelper.createCheckBoxFor(a));
		} else if (a instanceof Attribute.Enum) {
			box.add(AttributeUiHelper.createComboBoxFor(a));
		} else {
			box.add(AttributeUiHelper.createTextFieldFor(a));
			gbc.fill = GridBagConstraints.HORIZONTAL;
		}
		add(box, gbc);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
	}
	
	private void addTuple(Attribute.Tuple a) {
		JComponent component;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		add(new JLabel(a.getName()), gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		Box box = Box.createHorizontalBox();
		JCheckBox keyX = AttributeUiHelper.createCheckBoxFor(a.x.keyed);
		JCheckBox keyY = AttributeUiHelper.createCheckBoxFor(a.y.keyed);
		JCheckBox keyZ = AttributeUiHelper.createCheckBoxFor(a.z.keyed);
		keyX.setEnabled(a.isKeyable());
		keyY.setEnabled(a.isKeyable());
		keyZ.setEnabled(a.isKeyable());
		box.add(keyX);
		box.add(AttributeUiHelper.createTextFieldFor(a.x));
		box.add(keyY);
		box.add(AttributeUiHelper.createTextFieldFor(a.y));
		box.add(keyZ);
		box.add(AttributeUiHelper.createTextFieldFor(a.z));
		add(box, gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
	}
	
	private void addLimit(final Attribute.BoundedDouble a) {
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		add(new JLabel(a.getName() + " Limits"), gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		Box box = Box.createHorizontalBox();
		JButton toMinButton = new JButton(createIcon(1, Color.BLACK));
		toMinButton.setRolloverIcon(createIcon(1, Color.LIGHT_GRAY));
		toMinButton.setPressedIcon(createIcon(1, Color.BLACK));
		toMinButton.setContentAreaFilled(false);
		toMinButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		toMinButton.setToolTipText("Copy current value to lower limit");
		JButton toMaxButton = new JButton(createIcon(0, Color.BLACK));
		toMaxButton.setRolloverIcon(createIcon(0, Color.LIGHT_GRAY));
		toMaxButton.setPressedIcon(createIcon(0, Color.BLACK));
		toMaxButton.setContentAreaFilled(false);
		toMaxButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		toMaxButton.setToolTipText("Copy current value to upper limit");
		final JTextField minTextField = AttributeUiHelper.createTextFieldFor(a.min);
		final JTextField maxTextField = AttributeUiHelper.createTextFieldFor(a.max);
		minTextField.setEnabled(a.min.enabled.get());
		maxTextField.setEnabled(a.max.enabled.get());
		toMinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.min.set(a.get());
			}
		});
		toMaxButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				a.max.set(a.get());
			}
		});
		a.min.enabled.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				minTextField.setEnabled(a.min.enabled.get());
			}
		});
		a.max.enabled.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				maxTextField.setEnabled(a.max.enabled.get());
			}
		});
		box.add(AttributeUiHelper.createCheckBoxFor(a.min.enabled));
		box.add(minTextField);
		box.add(toMinButton);
		box.add(AttributeUiHelper.createTextFieldFor(a));
		box.add(toMaxButton);
		box.add(maxTextField);
		box.add(AttributeUiHelper.createCheckBoxFor(a.max.enabled));
		add(box, gbc);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
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
