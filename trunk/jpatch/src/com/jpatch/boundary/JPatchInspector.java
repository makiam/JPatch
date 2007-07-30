package com.jpatch.boundary;

import java.awt.Color;

import javax.swing.JComponent;

import com.jpatch.afw.attributes.*;
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
			setViewport(((GenericAttr<Viewport>) source).getValue());
		}
	};
	
	private AttributePostChangeListener selectionChangeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			panel.remove(2);
			System.out.println("selection=" + ((GenericAttr<Object>) source).getValue());
			Object selectedObject = ((GenericAttr<Object>) source).getValue();
			if (selectedObject != null) {
				panel.add(AttributeEditorFactory.getInstance().getEditorFor(((GenericAttr<Object>) source).getValue(), SELECTION_COLOR), 2);
			} else {
				panel.add(selectionEditor, 2);
			}
			panel.getComponent().validate();
			panel.getComponent().repaint();
		}
	};
	
	public AttributePostChangeListener getViewportChangeListener() {
		return viewportChangeListener;
	}
	
	public AttributePostChangeListener getSelectionChangeListener() {
		return selectionChangeListener;
	}
	
	public void setViewport(Viewport viewport) {
		panel.remove(0);
		System.out.println("viewport=" + viewport);
		panel.add(AttributeEditorFactory.getInstance().getEditorFor(viewport.viewDef, VIEW_COLOR), 0);
		panel.getComponent().validate();
		panel.getComponent().repaint();
	}
	
	public JPatchInspector() {
		panel.add(NULL_VIEW, 0);
		panel.add(toolEditor, 1);
		panel.add(selectionEditor, 2);
//		panel.add(AttributeEditorFactory.getInstance().getEditorFor(new TransformNode(), SELECTION_COLOR), 2);
	}
	
	public JComponent getComponent() {
		return panel.getComponent();
	}
}
