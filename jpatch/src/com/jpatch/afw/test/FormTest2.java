package com.jpatch.afw.test;

import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.ui.AttributeEditor;
import com.jpatch.afw.ui.PlatformUtils;
import com.jpatch.entity.TransformNode;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.*;

public class FormTest2 {
	public static void main(String[] args) {
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/resources/Strings");
		Configuration.getInstance().put("iconDir", "com/jpatch/icons/");
		Configuration.getInstance().put("settingsUserRoot", "com/jpatch/settings/preferences");
		PlatformUtils.setupSwing();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		final AttributeEditor ae = new AttributeEditor(TransformNode.class, "Transform");
		final TransformNode tn1 = new TransformNode();
		TransformNode tn2 = new TransformNode();
		ae.setEntity(tn1);
		
		ae.addField("TRANSLATE", "Translation");
		ae.addField("ROTATE", "Rotation");
		ae.addField("SCALE", "Scale");
		ae.startContainer("Advanced");
		ae.addField("AXIS_ROTATION", "AxisRotation");
//		ae.addField("TRANSLATE", "Translation");
		ae.addField("VISIBILITY", "Visibility");
		System.out.println("<<<");
		ae.addField("ORDER", "RotationOrder");
		System.out.println(">>>");
		
		frame.add(ae.getRootContainer().getComponent(), BorderLayout.NORTH);
		frame.setSize(300, 500);
		frame.setVisible(true);
		
		JComboBox combo = new JComboBox(new Object[] { tn1, tn2 });
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ae.setEntity(((JComboBox) e.getSource()).getSelectedItem());
				tn1.getTranslationAttribute().getXAttr().dumpListeners();
			}
		});
		frame.add(combo, BorderLayout.SOUTH);
	}
}
