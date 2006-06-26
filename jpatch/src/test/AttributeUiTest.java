package test;

import java.awt.*;
import java.awt.event.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import jpatch.boundary.AttributeUiHelper;
import jpatch.boundary.TransformNodeAttributeEditor;
import jpatch.entity.*;

public class AttributeUiTest {
	public static void main(String[] args) {
		new AttributeUiTest();
	}
	
	public AttributeUiTest() {
		UIManager.put("swing.boldMetal", false);
		final JFrame frame1 = new JFrame(getClass().getCanonicalName() + " 1");
		final JFrame frame2 = new JFrame(getClass().getCanonicalName() + " 2");
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TransformNode tn1 = new TransformNode();
		TransformNode tn2 = new TransformNode();
		TransformNodeAttributeEditor tnae1 = new TransformNodeAttributeEditor(tn1);
		TransformNodeAttributeEditor tnae2 = new TransformNodeAttributeEditor(tn2);
		tn1.addChild(tn2);
		frame1.add(new JScrollPane(tnae1));
		frame2.add(tnae2);
		frame1.pack();
		frame2.pack();
		frame1.setVisible(true);
		frame2.setVisible(true);
	}
}
