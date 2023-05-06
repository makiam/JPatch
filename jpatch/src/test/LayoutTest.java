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
package test;


import javax.swing.*;
import jpatch.boundary.ui.ExpandableForm;
import jpatch.boundary.ui.ExpandableFormContainer;

public class LayoutTest {

        @SuppressWarnings("ResultOfObjectAllocationIgnored")
	public static void main(String[] args) {
		new LayoutTest();
	}

	public LayoutTest() {
		Row[][] panels = new Row[][] {
				{ new Row(new JLabel("Blablabla Name"), new JTextField(10)), new Row(new JLabel("Wert 1"), new JTextField(10)), new Row(new JLabel("Wert 2"), new JTextField(10)) },
				{ new Row(new JLabel("Name"), new JTextField(10)) },
				{ new Row(new JLabel("Name"), new JTextField(0)), new Row(new JLabel("Wert 1"), new JTextField(10)) },
		};
		
		JFrame frame = new JFrame("Test");
		ExpandableFormContainer box = new ExpandableFormContainer();
		
		
		for (Row[] panel : panels) {
			JComponent container = new ExpandableForm();
			for (Row row : panel) {
				container.add(row.label);
				container.add(row.field);
			}
			box.add(container);
		}
		
		frame.add(box);
		frame.pack();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.add(container);
		frame.setVisible(true);
	}
	
	
	
	static class Row {
		public JComponent label;
		public JComponent field;
		public Row(JComponent label, JComponent field) {
			this.label = label;
			this.field = field;
		}
	}
	
	
}
