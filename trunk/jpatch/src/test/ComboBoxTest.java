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

import java.awt.*;

import javax.swing.*;

/**
 * @author sascha
 *
 */
public class ComboBoxTest {

	private static ListCellRenderer renderer = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			// TODO Auto-generated method stub
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setText("aaa");
			setPreferredSize(new Dimension(40, 20));
			//setIcon((Icon) value);
			return this;
			
		}
		
	};
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		Icon[] icons = new Icon[] {
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_1.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_2.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_3.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_4.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_12.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_34.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_13.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_24.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_1234.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_single.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_split_h.png")),
				new ImageIcon(ClassLoader.getSystemResource("jpatch/images/viewport_split_v.png")),
		};
		JComboBox combo = new JComboBox(icons);
		combo.setMaximumSize(new Dimension(26, 26));
		//combo.setRenderer(renderer);
//		toolBar.add(new JButton(i2));
//		toolBar.add(new JButton(i2));
		toolBar.add(combo);
//		toolBar.add(new JButton(i2));
//		toolBar.add(new JButton(i2));
//		toolBar.add(new JButton(i2));
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(Box.createHorizontalGlue());
		frame.add(toolBar, BorderLayout.NORTH);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

}
