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
		final JFrame frame = new JFrame(getClass().getCanonicalName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		TransformNode tn = new TransformNode();
		TransformNodeAttributeEditor tnae = new TransformNodeAttributeEditor(tn);
		frame.add(tnae);
		frame.pack();
		frame.setVisible(true);
	}
}
