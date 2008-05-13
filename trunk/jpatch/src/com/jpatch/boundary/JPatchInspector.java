package com.jpatch.boundary;

import java.awt.Color;

import javax.swing.JComponent;

import trashcan.*;

import com.jpatch.afw.Utils;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;
import com.jpatch.boundary.tools.JPatchTool;

public class JPatchInspector {
	public AttributeEditorPanel panel = new AttributeEditorPanel();
//	private static final Color VIEW_COLOR = new Color(0x99aabb);
//	private static final Color TOOL_COLOR = new Color(0xbbaa99);
//	private static final Color SELECTION_COLOR = new Color(0x88aa88);
	
	private static final Color VIEW_COLOR = new Color(0xbbccee);
	private static final Color TOOL_COLOR = new Color(0xeeccbb);
	private static final Color SELECTION_COLOR = new Color(0xaaccaa);
	
	private final BooleanAttr viewExpandedAttr = new BooleanAttr();
	private final BooleanAttr toolExpandedAttr = new BooleanAttr();
	private final BooleanAttr selectionExpandedAttr = new BooleanAttr();
	
	private AttributeEditor NULL_VIEW = new AttributeEditor(null, "NULL_VIEW", viewExpandedAttr, null, VIEW_COLOR);
	private AttributeEditor toolEditor = new AttributeEditor(null, "NULL_TOOL", toolExpandedAttr, null, TOOL_COLOR);
	private AttributeEditor selectionEditor = new AttributeEditor(null, "NULL_SELECTION", selectionExpandedAttr, null, SELECTION_COLOR);

	private AttributePostChangeListener viewportChangeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			setViewport(((GenericAttr<Viewport>) source).getValue());
		}
	};
	
	private AttributePostChangeListener toolChangeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			panel.remove(1);
			JPatchTool activeTool = ((GenericAttr<JPatchTool>) source).getValue();
			if (activeTool != null) {
				AttributeEditor editor = AttributeEditorFactory.getInstance().getEditorFor(activeTool, null, TOOL_COLOR);
				if (editor != null) {
					panel.add(AttributeEditorFactory.getInstance().getEditorFor(activeTool, null, TOOL_COLOR), 1);
				} else {
					panel.add(toolEditor, 1);
				}
			} else {
				panel.add(toolEditor, 1);
			}
			System.out.println("inspector tool change");
			panel.getComponent().revalidate();
			panel.getComponent().repaint();
		}
	};
	
	private AttributePostChangeListener selectionChangeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			panel.remove(2);
			System.out.println("selection=" + ((GenericAttr<Object>) source).getValue());
			Object selectedObject = ((GenericAttr<Object>) source).getValue();
			if (selectedObject != null) {
				AttributeEditor ae = AttributeEditorFactory.getInstance().getEditorFor(((GenericAttr<Object>) source).getValue(), selectionExpandedAttr, SELECTION_COLOR);
				if (ae != null) {
					panel.add(ae, 2);
				} else {
					panel.add(selectionEditor, 2);
				} 
			} else {
				panel.add(selectionEditor, 2);
			}
			panel.getComponent().revalidate();
			panel.getComponent().repaint();
		}
	};
	
	public AttributePostChangeListener getViewportChangeListener() {
		return viewportChangeListener;
	}
	
	public AttributePostChangeListener getSelectionChangeListener() {
		return selectionChangeListener;
	}
	
	public AttributePostChangeListener getToolChangeListener() {
		return toolChangeListener;
	}
	
	public void setViewport(Viewport viewport) {
		panel.remove(0);
//		System.out.println("viewport=" + viewport);
		panel.add(AttributeEditorFactory.getInstance().getEditorFor(viewport.getViewDef(), viewExpandedAttr, VIEW_COLOR), 0);
//		if (panel.getComponent().getRootPane() != null) {
//			System.out.println("validate " + panel.getComponent().getRootPane());
		JComponent component = panel.getComponent();
		System.out.println("inspector" + " component=" + System.identityHashCode(component) + " parent=" + System.identityHashCode(component.getParent()) + " validateRoot=" + System.identityHashCode(Utils.getValidateRoot(component)));
		
		component.revalidate();
		component.repaint();
//		}
//		panel.getComponent().repaint();
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
