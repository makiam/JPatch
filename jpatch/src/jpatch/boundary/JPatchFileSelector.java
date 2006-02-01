/*
 * $Id: JPatchFileSelector.java,v 1.2 2006/02/01 21:11:28 sascha_l Exp $
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

import javax.swing.*;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;

/**
 * A fileselector component containing a textfiled and a browse button.
 * @author sascha
 * @version $Revision: 1.2 $
 */
public class JPatchFileSelector extends JPanel {

	private static final long serialVersionUID = -8371423060126511816L;
	/** the textfiled for the filename*/
	private JTextField textField = new JTextField(20);
	/** the browse button */
	private JButton button = new JButton("...");
	/**
	 * private dummy constructor to prohibit public constructors.
	 */
	private JPatchFileSelector() { }
	
	/**
	 * private constructor
	 * @param path The initial path (selected file)
	 * @param dir The initial directory
	 * @param bDirectories true if this is a directory-chooser, false if this is a file-chooser
	 */
	private JPatchFileSelector(final String path, final String dir, final boolean bDirectories) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		textField.setText(path);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Container parent;
				for (parent = JPatchFileSelector.this; parent.getParent() != null; parent = parent.getParent());
				JFileChooser fileChooser = new JFileChooser(dir);
				if (bDirectories)
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				else
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (!textField.getText().equals(dir + File.separator))
					fileChooser.setSelectedFile(new File(textField.getText()));
				fileChooser.setDialogTitle("Select a " + (bDirectories ? "directory" : "file"));
				if (fileChooser.showDialog(parent, "select this " + (bDirectories ? "directory" : "file")) == JFileChooser.APPROVE_OPTION) {
					textField.setText(fileChooser.getSelectedFile().getPath());
				}				
			}
		});
		button.setPreferredSize(new Dimension(20,19));
		button.setToolTipText("browse...");
		add(textField);
		add(button);
	}
	
	/**
	 * Factory method to create a new file selector
	 * @param path The selected file
	 * @param dir The initial directory (if no file is given)
	 * @return a new JPatchFileSelector object
	 */
	public static JPatchFileSelector createFileSelector(String path, String dir) {
		return new JPatchFileSelector(path, dir, false);
	}
	
	/**
	 * Factory method to create a new directory selector
	 * @param path The selected file
	 * @param dir The initial directory (if no file is given)
	 * @return a new JPatchFileSelector object
	 */
	public static JPatchFileSelector createDirectorySelector(String path, String dir) {
		return new JPatchFileSelector(path, dir, true);
	}
	
	/**
	 * @return The selected file or directory
	 */
	public String getPath() {
		return textField.getText();
	}
}
