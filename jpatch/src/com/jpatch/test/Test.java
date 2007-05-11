package com.jpatch.test;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePreChangeAdapter;
import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.icons.IconSet;
import com.jpatch.afw.ui.ButtonUtils;
import com.jpatch.afw.ui.JPatchActionButton;
import com.jpatch.afw.ui.JPatchStateButton;
import com.jpatch.afw.ui.JPatchToggleButton;
import com.jpatch.afw.ui.JPatchToolBar;
import com.jpatch.boundary.actions.Actions;
import com.jpatch.ui.ViewportSwitcher;

import java.awt.BorderLayout;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Test {
	public static void main(String[] args) {
		System.setProperty("swing.boldMetal", "false");
		System.setProperty("swing.aatext", "true");
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/resources/Strings");
		Configuration.getInstance().put("iconDir", "com/jpatch/icons/");
		
		JFrame frame = new JFrame("JPatch Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		JPanel panel = new JPanel(new BorderLayout());
		JPatchToolBar toolBar = new JPatchToolBar();
		panel.add(toolBar, BorderLayout.CENTER);
		frame.add(panel, BorderLayout.NORTH);
		
		Actions actions = new Actions();
		
		ViewportSwitcher viewportSwitcher = new ViewportSwitcher(actions.viewportModeSM);
		
		ButtonUtils buttonUtils = new ButtonUtils();
		
		JPatchActionButton undoButton = new JPatchActionButton(actions.undo);
		JPatchActionButton redoButton = new JPatchActionButton(actions.redo);
		
		JPatchStateButton moveView = new JPatchStateButton(actions.moveView);
		JPatchStateButton zoomView = new JPatchStateButton(actions.zoomView);
		JPatchStateButton rotateView = new JPatchStateButton(actions.rotateView);
		
		JPatchStateButton vertexMode = new JPatchStateButton(actions.vertexMode);
		JPatchStateButton edgeMode = new JPatchStateButton(actions.edgeMode);
		JPatchStateButton faceMode = new JPatchStateButton(actions.faceMode);
		JPatchStateButton objectMode = new JPatchStateButton(actions.objectMode);
		
		JPatchToggleButton snapToGrid = new JPatchToggleButton(actions.snapToGrid);
		
		JPatchStateButton defaultTool = new JPatchStateButton(actions.defaultTool);
		JPatchStateButton moveTool = new JPatchStateButton(actions.moveTool);
		JPatchStateButton scaleTool = new JPatchStateButton(actions.scaleTool);
		JPatchStateButton rotateTool = new JPatchStateButton(actions.rotateTool);
		
		JPatchStateButton extrudeTool = new JPatchStateButton(actions.extrudeTool);
		JPatchStateButton latheTool = new JPatchStateButton(actions.latheTool);
		
		buttonUtils.configureButtons(IconSet.Style.DARK, undoButton, redoButton);
		buttonUtils.configureButtons(IconSet.Style.GLOSSY, moveView, zoomView, rotateView);
		buttonUtils.configureButtons(IconSet.Style.FROSTED, vertexMode, edgeMode, faceMode, objectMode);
		buttonUtils.configureButtons(IconSet.Style.BRUSHED, snapToGrid);
		buttonUtils.configureButtons(IconSet.Style.BRUSHED, defaultTool, moveTool, scaleTool, rotateTool);
		buttonUtils.configureButtons(IconSet.Style.BRUSHED, extrudeTool, latheTool);
		
		toolBar.add(viewportSwitcher.getComponent());
		toolBar.add(Box.createHorizontalStrut(32));
		toolBar.add(undoButton);
		toolBar.add(redoButton);
		toolBar.add(Box.createHorizontalStrut(16));
		toolBar.add(moveView);
		toolBar.add(zoomView);
		toolBar.add(rotateView);
		toolBar.add(Box.createHorizontalStrut(16));
		toolBar.add(vertexMode);
		toolBar.add(edgeMode);
		toolBar.add(faceMode);
		toolBar.add(objectMode);
		toolBar.add(Box.createHorizontalStrut(16));
		toolBar.add(snapToGrid);
		toolBar.add(Box.createHorizontalStrut(4));
		toolBar.add(defaultTool);
		toolBar.add(moveTool);
		toolBar.add(scaleTool);
		toolBar.add(rotateTool);
		toolBar.add(Box.createHorizontalStrut(4));
		toolBar.add(extrudeTool);
		toolBar.add(latheTool);
		
		frame.setSize(1024, 768);
		frame.setVisible(true);
		actions.toolSM.addAttributeListener(new AttributePreChangeAdapter() {

			@Override
			public void attributeHasChanged(Attribute source) {
				System.out.println(((StateMachine) source).getState());
			}
		});
	}
}
