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
package jpatch.boundary.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.border.Border;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyledEditorKit;

import test.IconComponent;

/**
 * @author sascha
 *
 */
public class JPatchDialog extends JDialog {
	public final static Icon ERROR = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_64x64/dialog-error.png"));
	public final static Icon INFORMATION = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_64x64/dialog-information.png"));
	public final static Icon WARNING = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_64x64/dialog-warning.png"));
	public final static Icon QUESTION = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/icons_64x64/dialog-question.png"));
	
	private int selectedOption = -1;
	private Font font = new Font("SansSerif", Font.PLAIN, 12);
	
	private JPatchDialog(Frame owner, String title, boolean modal, Icon icon, String message, Component component, String[] options, int focus, String width) {
		super(owner, title, modal);
		setLayout(new BorderLayout());
		Box buttonBox = Box.createHorizontalBox();
		if (icon != null) {
			IconComponent iconComponent = new IconComponent(icon);
			iconComponent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 0));
			add(iconComponent, BorderLayout.WEST);
		}
		Box textBox = Box.createVerticalBox();
		
		final JEditorPane text = new JEditorPane("text/html", "<div width='" + width + "'>" + message + "</div>");
		text.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		text.getDocument().putProperty(PlainDocument.lineLimitAttribute, 20);
		text.setFont(font);
		text.setEditable(false);
		text.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		text.setOpaque(false);
		textBox.add(text);
		if (component != null) {
//			Box box = Box.createVerticalBox();
//			box.add(component);
//			box.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(component, BorderLayout.CENTER);
			Border b1 = BorderFactory.createEmptyBorder(8, 8, 8, 8);
			Border b2 = BorderFactory.createEtchedBorder();
			Border b3 = BorderFactory.createEmptyBorder(0, 16, 16, 16);
//			panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(b3, b2), b1));
			panel.setBorder(b3);
			
//			panel.s
			textBox.add(panel);
		}
		textBox.add(buttonBox);
		
		add(textBox, BorderLayout.CENTER);
		
		Component focusComponent = null;
		int o = 0;
		for (String option : options) {
			if (option != null) {
				JButton button = new JButton(option);
				buttonBox.add(Box.createHorizontalStrut(8));
				buttonBox.add(button);
				buttonBox.add(Box.createHorizontalStrut(8));
				if (o == focus)
					focusComponent = button;
				final int opt = o;
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectedOption = opt;
						dispose();
					}
				});
				o++;
			} else {
				buttonBox.add(Box.createHorizontalGlue());
			}
		}
		buttonBox.setBorder(BorderFactory.createEmptyBorder(0, 8, 16, 8));
		pack();
		setResizable(false);
		if (focusComponent != null) {
			focusComponent.requestFocus();
		}
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public static int showDialog(Frame owner, String title, Icon icon, String message, Component component, String[] options, int focus, String width) {
		
		JPatchDialog dialog = new JPatchDialog(owner, title, true, icon, message, component, options, focus, width);
		return dialog.selectedOption;
	}
	
	
}
