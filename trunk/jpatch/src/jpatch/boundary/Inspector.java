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
import javax.swing.*;

import jpatch.entity.*;
import sds.HalfEdge;
import sun.management.snmp.util.JvmContextFactory;

/**
 * @author sascha
 *
 */
public class Inspector {
	private static Font FONT = new Font("sans-serif", Font.BOLD, 12);
	private static Color COLOR = new Color(0x77aaff);
	private final JPanel panel = new JPanel(new BorderLayout());
	private final JLabel label = new JLabel();
	private Component component;
	private Object object;
//	private AttributeListener attributeListener = new AttributeListener() {
//		public void attributeChanged(Attribute attribute) {
//			setLabelText((Attribute.Name) attribute);
//		}
//	};
	
	public Inspector() {
		if (COLOR == null) {
			COLOR = COLOR.black;
		}
		label.setFont(FONT);
		label.setBackground(COLOR);
		label.setOpaque(true);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(label, BorderLayout.NORTH);
	}
	
	public void setObject(Object object) {
		if (component != null) {
			panel.remove(component);
		}
//		if (object != null) {
//			object.name.removeAttributeListener(attributeListener);
//		}
		this.object = object;
		if (object != null) {
			if (object instanceof HalfEdge) {
				HalfEdge edge = (HalfEdge) object;
				System.out.println("EDGE: master=" + edge.isPrimary() + " sharpness=" + edge.sharpness);
			}
//			setLabelText(object.name);
//			object.name.addAttributeListener(attributeListener);
			label.setText(object.toString() + object.hashCode());
			component = AttributeEditorFactory.INSTANCE.createEditorFor(object);
			panel.add(component, BorderLayout.CENTER);
			
		} else {
			label.setText("");
			component = null;
		}
	}
	
	public JComponent getComponent() {
		return panel;
	}
	
	public Object getObject() {
		return object;
	}
	
	private void setLabelText(Attribute.Name attribute) {
		label.setText(attribute.get());
	}
}
