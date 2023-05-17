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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
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
		frame.setLayout(new FlowLayout());
		frame.add(new JLabel("Test"));
		JButton button = new JButton("test");
		final JTextField textField = new JTextField("test");
		
		final WorkspaceChooser wsc = new WorkspaceChooser();
		button.addActionListener(new ActionListener() {

                        @Override
			public void actionPerformed(ActionEvent e) {
				int i = JPatchDialog.showDialog(frame, "abc.txt", JPatchDialog.WARNING, "<b>Do you want to save changes to this file before closing it?</b><p>If you don't save, your changes will be lost.", null, new String[] { "Don't Save", null, "Cancel", "Save" }, 2, "320");
				System.out.println(i);
				
				i = JPatchDialog.showDialog(frame, "JPatch - Select workspace", null, "<b>Please select a workspace folder.</b><p>JPatch stores your projects in a folder called a workspace. Choose a workspace folder to use for this session. If the folder does not exist, it will be created.", wsc, new String[] { "Quit", null, "Proceed" }, 1, "380");
				System.out.println(i);
			}
			
		});
		
//		Attribute.String filename = new Attribute.String();
//		Attribute.Boolean show = new Attribute.Boolean();
//		
//		Box box = Box.createVerticalBox();
//		box.
		frame.add(button);
		
		JButton b2 = new JButton("size");
		b2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				wsc.showDim();
			}
			
		});
		frame.add(b2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(640, 480);
		frame.setVisible(true);
	}

//	static class WorkspaceChooser extends AbstractAttributeEditor {
//		ExpandableForm defaultForm = new ExpandableForm(true);
//		WorkspaceChooser() {
//			Attribute.String dir = new Attribute.String();
//			try {
//				dir.set(Settings.getInstance().workspace.getCanonicalPath());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Attribute.Boolean show = new Attribute.Boolean();
//			show.set(Settings.getInstance().promptForWorkspace);
//			addFileSelector(defaultForm, "Workspace", dir, JFileChooser.DIRECTORIES_ONLY, "Select");
//			addScalar(defaultForm, "Ask on startup", show);
//			add(defaultForm);
//		}
//		
//		void showDim() {
//			System.out.println(defaultForm.getSize());
//		}
//	}
}
