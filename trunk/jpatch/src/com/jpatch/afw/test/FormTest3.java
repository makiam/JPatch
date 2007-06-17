package com.jpatch.afw.test;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class FormTest3 {
	static int n;
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(10, 10));
		for (int y = 0; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				panel.add(new JTextField(x + "/" + y));
			}
		}
		frame.add(panel, BorderLayout.CENTER);
		JButton button = new JButton("x");
		frame.add(button, BorderLayout.SOUTH);
		frame.setSize(800, 600);
		frame.setVisible(true);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int y = 0; y < 10; y++) {
					for (int x = 0; x < 10; x++) {
						JTextField textField = (JTextField) panel.getComponent(y * 10 + x);
						textField.setText(n++ + "/" + x + "/" + y + "alksjdalksjd");
					}
				}
			}
		});
	}
}
