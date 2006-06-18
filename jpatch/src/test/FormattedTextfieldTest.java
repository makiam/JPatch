package test;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.*;

public class FormattedTextfieldTest {
	public static void main(String[] args) {
		new FormattedTextfieldTest();
	}
	
	public FormattedTextfieldTest() {
		JFrame frame = new JFrame(getClass().getCanonicalName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DecimalFormat format = new DecimalFormat("#####0.0000");
		JFormattedTextField ftf = new JFormattedTextField(new NumberFormatter(format));
		ftf.setColumns(10);
		ftf.setHorizontalAlignment(JTextField.RIGHT);
		ftf.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				JFormattedTextField ftf = (JFormattedTextField)input;
	             AbstractFormatter formatter = ftf.getFormatter();
	             if (formatter != null) {
	                 String text = ftf.getText();
	                 try {
	                      formatter.stringToValue(text);
	                      ftf.setBackground(Color.WHITE);
	                      return true;
	                  } catch (ParseException pe) {
	                	  ftf.setBackground(Color.YELLOW);
	                      return false;
	                  }
	              }
				return true;
			}
		});
		ftf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFormattedTextField ftf = (JFormattedTextField) e.getSource();
				if (!ftf.getInputVerifier().verify(ftf))
					ftf.setValue(ftf.getValue());
				((JComponent) e.getSource()).transferFocus();
			}
		});
		JTextField tf = new JTextField(10);
		JPanel panel = new JPanel();
		panel.add(ftf);
		panel.add(tf);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
}
