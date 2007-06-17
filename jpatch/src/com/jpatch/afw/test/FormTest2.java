package com.jpatch.afw.test;

import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.Tuple2Attr;
import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.ui.AttributeEditor;
import com.jpatch.afw.ui.AttributeManager;
import com.jpatch.afw.ui.PlatformUtils;
import com.jpatch.afw.vecmath.Rotation3d;
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
		
		final AttributeEditor ae = new AttributeEditor(TestNode.class, "Transform");
		final TestNode tn1 = new TestNode();
		TestNode tn2 = new TestNode();
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
		ae.addField("VALUE", "Slider");
		ae.addSlider("SLIDER", "Slider");
		ae.addField("LIMITS", "Limits");
		ae.addLimits("Translation");
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
		tn1.getRotationOrderAttribute().removeState(Rotation3d.Order.ZYX);
		frame.add(combo, BorderLayout.SOUTH);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tn1.getRotationOrderAttribute().addState(Rotation3d.Order.ZYX);
		System.out.println("*");
	}
	
	public static class TestNode extends TransformNode {
		private DoubleAttr min = new DoubleAttr(-10);
		private DoubleAttr max = new DoubleAttr(10);
		private Tuple2Attr limits = new Tuple2Attr(min, max);
		private DoubleAttr slider = AttributeManager.getInstance().createBoundedDoubleAttr(min, max);
		
		public DoubleAttr getSliderAttribute() {
			return slider;
		}
		
		public Tuple2Attr getLimitsAttribute() {
			return limits;
		}
	}
}
