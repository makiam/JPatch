package com.jpatch.afw.test;

import java.awt.*;
import java.util.*;

import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.ui.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;

public class FormTest {
	public static void main(String[] args) {
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/resources/Strings");
		Configuration.getInstance().put("iconDir", "com/jpatch/icons/");
		Configuration.getInstance().put("settingsUserRoot", "com/jpatch/settings/preferences");
		PlatformUtils.setupSwing();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPatchForm form = new JPatchForm();
		form.addRow(new JLabel("axis rotation"), new JTextField(), new JTextField(), new JTextField());
		form.addRow(new JLabel("rotation"), new JTextField(), new JTextField());
		form.addRow(new JLabel("position"), new JTextField());
		form.addRow(new JLabel("translation"), new JTextField(), new JSlider());
		form.addRow(new JLabel("position"), new JCheckBox());
		form.addRow(new JLabel("position sdfsd fasdfecfcsa end"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		form.addRow(new JLabel("position"), new JComboBox(new Object[] { "abc", "xlohif wqeoih asd" }));
		frame.setLayout(new BorderLayout());
		frame.add(form.getComponent(), BorderLayout.NORTH);
		frame.setSize(500, 500);
		frame.setVisible(true);
		
	}
}
