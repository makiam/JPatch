package com.jpatch.afw.test;

import java.awt.*;
import java.util.*;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.DoubleMaximum;
import com.jpatch.afw.attributes.DoubleMinimum;
import com.jpatch.afw.attributes.IdentityMapping;
import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.icons.IconSet;
import com.jpatch.afw.icons.IconSet.*;
import com.jpatch.afw.ui.*;
import com.jpatch.afw.vecmath.Rotation3d;

import javax.sound.sampled.spi.FormatConversionProvider;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;

import jpatch.entity.attributes2.LinearMapping;

import trashcan.HardBoundedDoubleAttr;
import trashcan.SoftBoundedDoubleAttr;

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
		
		JTextField textField = new JTextField();
		JSlider slider = new JSlider();
		DoubleAttr min = new DoubleAttr(-100);
		DoubleAttr max = new DoubleAttr(100);
		DoubleAttr doubleAttr = AttributeManager.getInstance().createBoundedDoubleAttr(min, max);
		
		doubleAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				System.out.println(((DoubleAttr) source).getDouble());
			}
		});
		AttributeManager.getInstance().bindTextFieldToAttribute(textField, doubleAttr);
		AttributeManager.getInstance().bindSliderToAttribute(slider, doubleAttr, IdentityMapping.getInstance());
		
		transform.addRow(new JLabel("translation"), textField, slider);
		transform.addRow(new JLabel("rotation"), new JTextField(), new JTextField(), new JTextField());
		transform.addRow(new JLabel("scale"), new JTextField(), new JTextField(), new JTextField());
		
		JPatchForm advanced = new JPatchForm();
		advanced.addRow(new JLabel("visibility"), new JCheckBox());
		advanced.addRow(new JLabel("rotation order"), new JComboBox(Rotation3d.Order.values()));
		advanced.addRow(new JLabel("position"), new JTextField(), new JTextField(), new JTextField());
		advanced.addRow(new JLabel("orientation"), new JTextField(), new JTextField(), new JTextField());
		advanced.addRow(new JLabel("shear"), new JTextField(), new JTextField(), new JTextField());
		advanced.addRow(new JLabel("axis rotation"), new JTextField(), new JTextField(), new JTextField());
		
		JPatchForm transLimits = new JPatchForm();
		JTextField minTextField = new JTextField();
		
		Icon LOWER_LIMIT = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/SET_LOWER_LIMIT.png"));
		Icon UPPER_LIMIT = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/SET_UPPER_LIMIT.png"));
		Icon CLEAR_LIMIT = new ImageIcon(ClassLoader.getSystemResource("com/jpatch/afw/icons/CLEAR_LIMIT.png"));
		
		JButton setMin = new JButton();
		JButton clearMin = new JButton();
		JButton setMax = new JButton();
		JButton clearMax = new JButton();
//		setMin.setBorderPainted(false);
//		setMin.setBorder(null);
//		setMin.setPreferredSize(new Dimension(16, 16));
//		setMin.setContentAreaFilled(false);
//		setMax.setBorderPainted(false);
//		setMax.setBorder(null);
//		setMax.setPreferredSize(new Dimension(16, 16));
//		setMax.setContentAreaFilled(false);
//		clearMin.setBorderPainted(false);
//		clearMin.setBorder(null);
//		clearMin.setPreferredSize(new Dimension(16, 16));
//		clearMin.setContentAreaFilled(false);
//		clearMax.setBorderPainted(false);
//		clearMax.setBorder(null);
//		clearMax.setPreferredSize(new Dimension(16, 16));
//		clearMax.setContentAreaFilled(false);
		ButtonUtils buttonUtils = new ButtonUtils();
		buttonUtils.configureButton(setMin, IconSet.Style.GLOSSY, IconSet.Type.LEFT, LOWER_LIMIT);
		buttonUtils.configureButton(setMax, IconSet.Style.GLOSSY, IconSet.Type.LEFT, UPPER_LIMIT);
		buttonUtils.configureButton(clearMin, IconSet.Style.GLOSSY, IconSet.Type.RIGHT, CLEAR_LIMIT);
		buttonUtils.configureButton(clearMax, IconSet.Style.GLOSSY, IconSet.Type.RIGHT, CLEAR_LIMIT);
		
		JTextField maxTextField = new JTextField();
//		AttributeManager.getInstance().bindTextFieldToAttribute(minTextField, doubleAttr.getMinAttr());
//		AttributeManager.getInstance().bindTextFieldToAttribute(maxTextField, doubleAttr.getMaxAttr());
		AttributeManager.getInstance().bindLimit(doubleAttr, DoubleMaximum.class, setMax, clearMax, maxTextField);
		AttributeManager.getInstance().bindLimit(doubleAttr, DoubleMinimum.class, setMin, clearMin, minTextField);
		
		JComponent setClr1 = Box.createHorizontalBox();
		setClr1.add(setMax);
		setClr1.add(clearMax);
		JComponent setClr2 = Box.createHorizontalBox();
		setClr2.add(new JButton("x"));
		setClr2.add(new JButton("y"));
		JComponent setClr3 = Box.createHorizontalBox();
		setClr3.add(new JButton("a"));
		setClr3.add(new JButton("b"));
		
		transLimits.addRow(new JLabel("maximum"), maxTextField, new JTextField(), new JTextField());
		transLimits.addRow(new JLabel("set/clear"), setClr1, setClr2, setClr3);
		transLimits.addRow(new JLabel("current"), new JTextField(), new JTextField(), new JTextField());
		transLimits.addRow(new JLabel("clear min"), clearMin, new JButton("clr"), new JButton("clr"));
		transLimits.addRow(new JLabel("set min"), setMin, new JButton("set"), new JButton("set"));
		transLimits.addRow(new JLabel("minumum"), minTextField, new JTextField(), new JTextField());
		
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
		
		JPatchForm locks = new JPatchForm();
		locks.addRow(new JLabel("translation"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		locks.addRow(new JLabel("rotation"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		locks.addRow(new JLabel("scale"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		locks.addRow(new JLabel("shear"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		
		JPatchForm animate = new JPatchForm();
		animate.addRow(new JLabel("translation"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		animate.addRow(new JLabel("rotation"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		animate.addRow(new JLabel("scale"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		animate.addRow(new JLabel("shear"), new JCheckBox(), new JCheckBox(), new JCheckBox());
		
//		limits.addRow(new JLabel("translation max"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("rotation min"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("rotation max"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("scale min"), new JTextField(), new JTextField(), new JTextField());
//		limits.addRow(new JLabel("scale max"), new JTextField(), new JTextField(), new JTextField());
		
		JPatchForm test = new JPatchForm();
		test.addRow(new JLabel("2D test"), new JTextField(), new JTextField());
		test.addRow(new JLabel("1D test"), new JTextField());
		test.addRow(new JLabel("Slider test"), new JTextField(), new JSlider());
		
		frame.setLayout(new BorderLayout());
		
		JPatchFormContainer container = new JPatchFormContainer("Transform");
		container.add(transform);
		JPatchFormContainer container2 = new JPatchFormContainer("Advanced");
		container2.add(advanced);
		JPatchFormContainer container3 = new JPatchFormContainer("Limits");
		JPatchFormContainer container4 = new JPatchFormContainer("Translation");
		JPatchFormContainer container5 = new JPatchFormContainer("Rotation");
		JPatchFormContainer container6 = new JPatchFormContainer("Scale");
		JPatchFormContainer container7 = new JPatchFormContainer("Lock");
		JPatchFormContainer container8 = new JPatchFormContainer("Animate");
		JPatchFormContainer container9 = new JPatchFormContainer("Test");
		
		container4.add(transLimits);
		container5.add(rotLimits);
		container6.add(scaleLimits);
		
		container3.add(container4);
		container3.add(container5);
		container3.add(container6);
		
		container.add(container2);
		container.add(container3);
		
		container2.add(container7);
		container2.add(container8);
		container7.add(locks);
		container8.add(animate);
		
		container9.add(test);
		container.add(container9);
		
		frame.add(container.getComponent(), BorderLayout.NORTH);
		frame.setSize(300, 500);
		frame.setVisible(true);
		
	}
}
