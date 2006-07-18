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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jpatch.boundary.ui.JPatchDialog;

/**
 * @author sascha
 *
 */
public class DialogTest {
	public static void main(String[] args) {
		System.out.println("aaa");
		System.setProperty("swing.boldMetal", "false");
		System.setProperty("swing.aatext", "true");
		final JFrame frame = new JFrame();
		frame.add(new JLabel("Test"));
		JButton button = new JButton("test");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int i = JPatchDialog.showDialog(frame, "abc.txt", JPatchDialog.ERROR, "<b>Do you want to save changes to this file before closing it?</b><p>If you don't save, your changes will be lost.", new String[] { "Don't Save", null, "Cancel", "Save" }, 2);
				System.out.println(i);
			}
			
		});
		frame.add(button);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		frame.setVisible(true);
	}

}
