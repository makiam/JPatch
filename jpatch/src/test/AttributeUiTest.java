package test;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import jpatch.boundary.AttributeUiHelper;
import jpatch.entity.*;

public class AttributeUiTest {
	public static void main(String[] args) {
		new AttributeUiTest();
	}
	
	public AttributeUiTest() {
		final JFrame frame = new JFrame(getClass().getCanonicalName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final Attribute<Double> attribute = new Attribute<Double>("test", 0.0, -1.0, 1.0);
		final JPanel panel = new JPanel();
		final JTextField tf = AttributeUiHelper.getTextFieldFor(attribute);
		panel.add(tf);
		panel.add(new JTextField(10));
		JButton button = new JButton("inc");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				attribute.setValue(attribute.getValue() + 1);
			}
		});
		panel.add(AttributeUiHelper.getSliderFor(attribute));
		panel.add(button);
		JButton button2 = new JButton("show");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.isShowing())
					frame.remove(panel);
				else
					frame.add(panel, BorderLayout.CENTER);
				frame.repaint();
			}
		});
		panel.setBorder(BorderFactory.createTitledBorder("test"));
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.add(button2, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}
}
