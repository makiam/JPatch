package com.jpatch.afw.test;

import com.jpatch.afw.attributes.DoubleAttr;
import com.jpatch.afw.attributes.Tuple2Attr;
import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.ui.AttributeEditor;
import com.jpatch.afw.ui.AttributeEditorFactory;
import com.jpatch.afw.ui.AttributeManager;
import com.jpatch.afw.ui.PlatformUtils;
import com.jpatch.afw.vecmath.Rotation3d;
import com.jpatch.boundary.ViewportGl;
import com.jpatch.entity.TransformNode;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public class FormTest2 {
	public static void main(String[] args) {
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/resources/Strings");
		Configuration.getInstance().put("iconDir", "com/jpatch/icons/");
		Configuration.getInstance().put("settingsUserRoot", "com/jpatch/settings/preferences");
		PlatformUtils.setupSwing();
		JFrame frame = new JFrame();
		SwingUtilities.updateComponentTreeUI(frame);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		
		
//		final AttributeEditor ae = new AttributeEditor(TestNode.class, "Transform");
		final TestNode tn1 = new TestNode();
		TestNode tn2 = new TestNode();
		
		final AttributeEditor ae1 = AttributeEditorFactory.getInstance().getEditorFor(new ViewportGl(1, 1));
		final AttributeEditor ae2 = AttributeEditorFactory.getInstance().getEditorFor(tn1);
		
//		ae.setEntity(tn1);
		
//		ae.addField("TRANSLATE", "Translation");
//		ae.addField("ROTATE", "Rotation");
//		ae.addField("SCALE", "Scale");
//		ae.startContainer("Advanced");
//		ae.addField("AXIS_ROTATION", "AxisRotation");
////		ae.addField("TRANSLATE", "Translation");
//		ae.addField("VISIBILITY", "Visibility");
//		System.out.println("<<<");
//		ae.addField("ORDER", "RotationOrder");
//		ae.addField("VALUE", "Slider");
//		ae.addSlider("SLIDER", "Slider");
//		ae.addField("LIMITS", "Limits");
//		ae.endContainer();
//		ae.startContainer("LIMITS");
//		ae.startContainer("TRANSLATION");
//		ae.addLimits("Translation");
//		ae.endContainer();
//		ae.startContainer("ROTATION");
//		ae.addLimits("Rotation");
//		ae.endContainer();
//		ae.startContainer("SCALE");
//		ae.addLimits("Scale");
//		ae.endContainer();
//		System.out.println(">>>");
		box.add(ae1.getRootContainer().getComponent());
		box.add(ae2.getRootContainer().getComponent());
		box.setOpaque(false);
//		box.setPreferredSize(new Dimension(0, 0));
		class MyPanel extends JPanel implements Scrollable {
			
			MyPanel() {
				super(new BorderLayout());
			}
			
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(0, 0);
			}
		
			public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 20;
			}
		
			public boolean getScrollableTracksViewportHeight() {
				return false;
			}
		
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		
			public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
				return 20;
			}
			
		}
//		JPanel panel = new JPanel(new BorderLayout()) {
//			@Override
//			public Dimension getPreferredSize() {
//				return new Dimension(0, getComponent(0).getPreferredSize().height);
//			}
//		};
		MyPanel panel = new MyPanel();
		panel.add(box, BorderLayout.NORTH);
//		panel.setPreferredSize(new Dimension(0, 200));
		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
//		scrollPane.setWheelScrollingEnabled(handleWheel)
//		scrollPane.getViewport().getView().setPreferredSize(new Dimension(0, 0));
		frame.add(scrollPane, BorderLayout.CENTER);
		frame.setSize(340, 500);
		frame.setVisible(true);
		
		JComboBox combo = new JComboBox(new Object[] { tn1, tn2 });
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ae2.setEntity(((JComboBox) e.getSource()).getSelectedItem());
			}
		});
		tn1.getRotationOrderAttribute().removeState(Rotation3d.Order.ZYX);
		frame.add(combo, BorderLayout.SOUTH);
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
