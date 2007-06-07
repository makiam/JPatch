package com.jpatch.afw.test;

import java.awt.*;
import java.util.*;

import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.ui.*;
import com.jpatch.afw.vecmath.Rotation3d;

import javax.sound.sampled.spi.FormatConversionProvider;
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
		JPatchForm transform = new JPatchForm();
		transform.addRow(new JLabel("translation"), new JTextField(), new JTextField(), new JTextField());
		transform.addRow(new JLabel("rotation"), new JTextField(), new JTextField(), new JTextField());
		transform.addRow(new JLabel("scale"), new JTextField(), new JTextField(), new JTextField());
		
		JPatchForm advanced = new JPatchForm();
		advanced.addRow(new JLabel("visible"), new JCheckBox());
		advanced.addRow(new JLabel("rotation order"), new JComboBox(Rotation3d.Order.values()));
		advanced.addRow(new JLabel("position"), new JTextField(), new JTextField(), new JTextField());
		advanced.addRow(new JLabel("orientation"), new JTextField(), new JTextField(), new JTextField());
		advanced.addRow(new JLabel("shear"), new JTextField(), new JTextField(), new JTextField());
		advanced.addRow(new JLabel("axis rotation"), new JTextField(), new JTextField(), new JTextField());
		
		JPatchForm transLimits = new JPatchForm();
		transLimits.addRow(new JLabel("maximum"), new JTextField(), new JTextField(), new JTextField());
		transLimits.addRow(new JLabel("set max"), new JButton("set"), new JButton("set"), new JButton("set"));
		transLimits.addRow(new JLabel("enable max"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		transLimits.addRow(new JLabel("current"), new JTextField(), new JTextField(), new JTextField());
		transLimits.addRow(new JLabel("enable min"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		transLimits.addRow(new JLabel("set min"), new JButton("set"), new JButton("set"), new JButton("set"));
		transLimits.addRow(new JLabel("minumum"), new JTextField(), new JTextField(), new JTextField());
		
		JPatchForm rotLimits = new JPatchForm();
		rotLimits.addRow(new JLabel("maximum"), new JTextField(), new JTextField(), new JTextField());
		rotLimits.addRow(new JLabel("set max"), new JButton("set"), new JButton("set"), new JButton("set"));
		rotLimits.addRow(new JLabel("enable max"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		rotLimits.addRow(new JLabel("current"), new JTextField(), new JTextField(), new JTextField());
		rotLimits.addRow(new JLabel("enable min"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		rotLimits.addRow(new JLabel("set min"), new JButton("set"), new JButton("set"), new JButton("set"));
		rotLimits.addRow(new JLabel("minumum"), new JTextField(), new JTextField(), new JTextField());
		
		JPatchForm scaleLimits = new JPatchForm();
		scaleLimits.addRow(new JLabel("maximum"), new JTextField(), new JTextField(), new JTextField());
		scaleLimits.addRow(new JLabel("set max"), new JButton("set"), new JButton("set"), new JButton("set"));
		scaleLimits.addRow(new JLabel("enable max"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		scaleLimits.addRow(new JLabel("current"), new JTextField(), new JTextField(), new JTextField());
		scaleLimits.addRow(new JLabel("enable min"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		scaleLimits.addRow(new JLabel("set min"), new JButton("set"), new JButton("set"), new JButton("set"));
		scaleLimits.addRow(new JLabel("minumum"), new JTextField(), new JTextField(), new JTextField());
		
//		limits.addRow(new JLabel("translation max"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("rotation min"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("rotation max"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("scale min"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("scale max"), new JTextField(), new JTextField(), new JTextField());
		
		
		frame.setLayout(new BorderLayout());
		
		JPatchFormContainer container = new JPatchFormContainer("Transform");
		container.add(transform);
		JPatchFormContainer container2 = new JPatchFormContainer("Advanced");
		container2.add(advanced);
		JPatchFormContainer container3 = new JPatchFormContainer("Limits");
		JPatchFormContainer container4 = new JPatchFormContainer("Translation");
		JPatchFormContainer container5 = new JPatchFormContainer("Rotation");
		JPatchFormContainer container6 = new JPatchFormContainer("Scale");
		
		container4.add(transLimits);
		container5.add(rotLimits);
		container6.add(scaleLimits);
		
		container3.add(container4);
		container3.add(container5);
		container3.add(container6);
		
		container.add(container2);
		container.add(container3);
		
		
		frame.add(container.getComponent(), BorderLayout.NORTH);
		frame.setSize(300, 500);
		frame.setVisible(true);
		
	}
}
