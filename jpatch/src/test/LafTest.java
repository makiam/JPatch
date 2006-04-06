/*
 * $Id$
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

import jpatch.boundary.ui.*;
import jpatch.boundary.laf.*;

/**
 * @author sascha
 *
 */
public class LafTest {

	public static void main(String[] args) throws Exception {
		new LafTest();
	}
	
	public LafTest() throws Exception {
		UIManager.setLookAndFeel(new SmoothLookAndFeel());
		JFrame frame = new JFrame("LAF Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JToolBar toolBar = new JToolBar();
		toolBar.add(new JPatchToggleButton(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/add.png"))));
		toolBar.add(new JPatchToggleButton(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/add.png"))));
		toolBar.add(new JPatchToggleButton(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/add.png"))));
		toolBar.add(new JPatchToggleButton(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/add.png"))));
		toolBar.add(new JPatchToggleButton(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/add.png"))));
		JPanel panel = new JPanel();
		panel.add(new JPatchToggleButton("Button 1"));
		panel.add(new JPatchToggleButton("Button 2"));
		panel.add(new JPatchToggleButton("Button 3"));
		panel.add(new JComboBox(new String[] { "Item 1", "Item 2", "Item 3" }));
		frame.add(toolBar, BorderLayout.NORTH);
		frame.add(panel, BorderLayout.CENTER);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
}
