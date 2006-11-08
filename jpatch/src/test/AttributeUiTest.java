package test;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import jpatch.boundary.AbstractAttributeEditor;
import jpatch.boundary.AttributeEditorFactory;
import jpatch.boundary.AttributeUiHelper;
import jpatch.boundary.BoneAttributeEditor;
import jpatch.boundary.TransformNodeAttributeEditor;
import jpatch.entity.*;

public class AttributeUiTest {
	public static void main(String[] args) {
		new AttributeUiTest();
	}
	
	
	public AttributeUiTest() {
		System.setProperty("swing.boldMetal", "false");
		System.setProperty("swing.aatext", "true");
//		UIManager.put("swing.aatext", true);
//		UIManager.put("swing.boldMetal", false);
		
//		try {
//			UIManager.setLookAndFeel(laf);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		final JFrame frame1 = new JFrame(getClass().getCanonicalName() + " 1");
		final JFrame frame2 = new JFrame(getClass().getCanonicalName() + " 2");
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TransformNode tn1 = new TransformNode(null);
		tn1.name.set("Transformnode 1");
		Bone bone = new Bone();
		bone.name.set("Bone 1");
		//TransformNodeAttributeEditor tnae1 = new TransformNodeAttributeEditor(tn1);
		
		AbstractAttributeEditor aae = null;
//		long t = System.currentTimeMillis();
//		for (int i = 1; i < 100; i++) {
			aae = AttributeEditorFactory.INSTANCE.createEditorFor(new TransformNode(new ObjectRegistry()));
//			long d = System.currentTimeMillis();;
//			System.out.println(d - t);
//			t = d;
//		}
		
//		BoneAttributeEditor bae = new BoneAttributeEditor(bone);
//		tn1.addChild(tn2);
		
//		scrollPane.setBorder(BorderFactory.createTitledBorder("Attribute Editor"));
		frame1.add(new JScrollPane(aae, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
//		frame2.add(new JScrollPane(bae, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		frame1.pack();
//		frame2.pack();
		frame1.setVisible(true);
//		frame2.setVisible(true);
	}
}
