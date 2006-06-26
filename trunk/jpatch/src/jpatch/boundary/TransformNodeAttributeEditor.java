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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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
		gbc.ipadx = 10;
		addLine("Name", transformNode.getAttribute(TransformNode.AttributeName.NAME));
		addLine("Visibility", transformNode.getAttribute(TransformNode.AttributeName.VISIBILITY));
		addXYZLine("Position",
				transformNode.getAttribute(TransformNode.AttributeName.POSITION_X),
				transformNode.getAttribute(TransformNode.AttributeName.POSITION_Y),
				transformNode.getAttribute(TransformNode.AttributeName.POSITION_Z)
		);
		addXYZLine("Translation",
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_X),
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_Y),
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_Z)
		);
		addXYZLine("Orientation",
				transformNode.getAttribute(TransformNode.AttributeName.ORIENTATION_X),
				transformNode.getAttribute(TransformNode.AttributeName.ORIENTATION_Y),
				transformNode.getAttribute(TransformNode.AttributeName.ORIENTATION_Z)
		);
		addXYZLine("Rotation",
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_X),
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_Y),
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_Z)
		);
		addXYZLine("Scale",
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_X),
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_Y),
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_Z)
		);
		addXYZLine("Pivot position",
				transformNode.getAttribute(TransformNode.AttributeName.PIVOT_POSITION_X),
				transformNode.getAttribute(TransformNode.AttributeName.PIVOT_POSITION_Y),
				transformNode.getAttribute(TransformNode.AttributeName.PIVOT_POSITION_Z)
		);
		addXYZLine("Pivot translation",
				transformNode.getAttribute(TransformNode.AttributeName.PIVOT_TRANSLATION_X),
				transformNode.getAttribute(TransformNode.AttributeName.PIVOT_TRANSLATION_Y),
				transformNode.getAttribute(TransformNode.AttributeName.PIVOT_TRANSLATION_Z)
		);
		addLimitLine("Translate min",
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_X_MIN),
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_Y_MIN),
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_Z_MIN)
		);
		addLimitLine("Translate max",
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_X_MAX),
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_Y_MAX),
				transformNode.getAttribute(TransformNode.AttributeName.TRANSLATION_Z_MAX)
		);
		addLimitLine("Rotate min",
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_X_MIN),
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_Y_MIN),
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_Z_MIN)
		);
		addLimitLine("Rotate max",
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_X_MAX),
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_Y_MAX),
				transformNode.getAttribute(TransformNode.AttributeName.ROTATION_Z_MAX)
		);
		addLimitLine("Scale min",
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_X_MIN),
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_Y_MIN),
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_Z_MIN)
		);
		addLimitLine("Scale max",
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_X_MAX),
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_Y_MAX),
				transformNode.getAttribute(TransformNode.AttributeName.SCALE_Z_MAX)
		);
	}
	
	private void addLine(String name, Attribute a) {
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		add(new JLabel(name), gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridwidth = 3;
		if (a instanceof Attribute.Boolean)
			add(AttributeUiHelper.createCheckBoxFor(a), gbc);
		else if (a instanceof Attribute.Enum)
			add(AttributeUiHelper.createComboBoxFor(a), gbc);
		else if (a instanceof Attribute.Limit)
			add(AttributeUiHelper.createLimitBox(a), gbc);
		else
			add(AttributeUiHelper.createTextFieldFor(a), gbc);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy++;
	}
	
	private void addXYZLine(String name, Attribute x, Attribute y, Attribute z) {
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		add(new JLabel(name), gbc);
//		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		add(AttributeUiHelper.createTextFieldFor(x), gbc);
		gbc.gridx = 2;
		add(AttributeUiHelper.createTextFieldFor(y), gbc);
		gbc.gridx = 3;
		add(AttributeUiHelper.createTextFieldFor(z), gbc);
		gbc.gridy++;
	}
	
	private void addLimitLine(String name, Attribute x, Attribute y, Attribute z) {
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		add(new JLabel(name), gbc);
//		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		add(AttributeUiHelper.createLimitBox(x), gbc);
		gbc.gridx = 2;
		add(AttributeUiHelper.createLimitBox(y), gbc);
		gbc.gridx = 3;
		add(AttributeUiHelper.createLimitBox(z), gbc);
		gbc.gridy++;
	}
	
	
}
