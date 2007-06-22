package com.jpatch.boundary;

import java.awt.Color;

import javax.swing.JComponent;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.ui.*;
import com.jpatch.entity.TransformNode;

public class JPatchInspector {
	public AttributeEditorPanel panel = new AttributeEditorPanel();
	private static final Color VIEW_COLOR = new Color(0x99aabb);
	private static final Color TOOL_COLOR = new Color(0xbbaa99);
	private static final Color SELECTION_COLOR = new Color(0x88aa88);
	
	private static final AttributeEditor NULL_VIEW = new AttributeEditor(null, "View", null, VIEW_COLOR);
	private AttributeEditor toolEditor = new AttributeEditor(null, "Tool", null, TOOL_COLOR);
	private AttributeEditor selectionEditor = new AttributeEditor(null, "Selection", null, SELECTION_COLOR);
	private AttributePostChangeListener viewportChangeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			panel.remove(0);
			System.out.println("viewport=" + ((GenericAttr<Viewport>) source).getValue());
			panel.add(AttributeEditorFactory.getInstance().getEditorFor(((GenericAttr<Viewport>) source).getValue(), VIEW_COLOR), 0);
			panel.getComponent().validate();
			panel.getComponent().repaint();
		}
	};
	
	public AttributePostChangeListener getViewportChangeListener() {
		return viewportChangeListener;
	}
	
	public JPatchInspector() {
		panel.add(NULL_VIEW, 0);
		panel.add(toolEditor, 1);
//		panel.add(selectionEditor, 2);
		panel.add(AttributeEditorFactory.getInstance().getEditorFor(new TransformNode(), SELECTION_COLOR), 2);
	}
	
	public JComponent getComponent() {
		return panel.getComponent();
	}
}
