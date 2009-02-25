package com.jpatch.afw;

import java.awt.image.*;

import javax.swing.*;

public class ComonentImageTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		panel.add(new JLabel("test"));
		panel.add(new JTextField("test"));
		panel.add(new JList(new String[] { "test", "abc", "123" } ));
		BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
		panel.setSize(300, 80);
		panel.doLayout();
		panel.paint(img.createGraphics());
		//frame.add(panel);
		frame.add(new JLabel(new ImageIcon(img)));
		frame.pack();
		frame.setVisible(true);
	}

}
