package com.jpatch.boundary.actions;

import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.boundary.Main;
import com.jpatch.boundary.tools.*;
import com.jpatch.entity.sds.*;

public class Actions {
//	public static enum Tool { MOVE_VIEW, ZOOM_VIEW, ROTATE_VIEW, DEFAULT_TOOL, MOVE_TOOL, SCALE_TOOL, ROTATE_TOOL, EXTRUDE_TOOL, LATHE_TOOL }
	public static enum SdsMode { VERTEX_MODE, EDGE_MODE, FACE_MODE, OBJECT_MODE }
	public static enum ViewportMode { VIEWPORT_1, VIEWPORT_2, VIEWPORT_3, VIEWPORT_4, SPLIT_1_2, SPLIT_3_4, SPLIT_1_3, SPLIT_2_4, QUAD } 
	
	public final JPatchTool[] tools = new JPatchTool[] {
			ChangeViewTool.createMoveViewTool(),
			ChangeViewTool.createZoomViewTool(),
			ChangeViewTool.createRotateViewTool(),
			new MoveVertexTool(),
			new RotateTool(),
			new TranslateTool(),
			null
	};
	
	public final JPatchUndoManager undoManager = new JPatchUndoManager();
	public final StateMachine<JPatchTool> toolSM = new StateMachine<JPatchTool>(tools, tools[3]);
	public final StateMachine<SdsMode> sdsModeSM = new StateMachine<SdsMode>(SdsMode.class, SdsMode.VERTEX_MODE);
	public final StateMachine<ViewportMode> viewportModeSM = new StateMachine<ViewportMode>(ViewportMode.class, ViewportMode.VIEWPORT_1);
	public final Toggle snapToGridToggle = new Toggle();
	
	public final SwitchStateAction moveView = new SwitchStateAction(toolSM, tools[0], undoManager, "MOVE_VIEW");
	public final SwitchStateAction zoomView = new SwitchStateAction(toolSM, tools[1], undoManager, "ZOOM_VIEW");
	public final SwitchStateAction rotateView = new SwitchStateAction(toolSM, tools[2], undoManager, "ROTATE_VIEW");
	
	public final SwitchStateAction defaultTool = new SwitchStateAction(toolSM, tools[3], undoManager, "DEFAULT_TOOL");
	public final SwitchStateAction moveTool = new SwitchStateAction(toolSM, tools[5], undoManager, "MOVE_TOOL");
	public final SwitchStateAction scaleTool = new SwitchStateAction(toolSM, tools[6], undoManager, "SCALE_TOOL");
	public final SwitchStateAction rotateTool = new SwitchStateAction(toolSM, tools[4], undoManager, "ROTATE_TOOL");
	public final SwitchStateAction extrudeTool = new SwitchStateAction(toolSM, tools[6], undoManager, "EXTRUDE_TOOL");
	public final SwitchStateAction latheTool = new SwitchStateAction(toolSM, tools[6], undoManager, "LATHE_TOOL");
	
	public final SwitchStateAction vertexMode = new SwitchStateAction(sdsModeSM, SdsMode.VERTEX_MODE, undoManager, "VERTEX_MODE");
	public final SwitchStateAction edgeMode = new SwitchStateAction(sdsModeSM, SdsMode.EDGE_MODE, undoManager, "EDGE_MODE");
	public final SwitchStateAction faceMode = new SwitchStateAction(sdsModeSM, SdsMode.FACE_MODE, undoManager, "FACE_MODE");
	public final SwitchStateAction objectMode = new SwitchStateAction(sdsModeSM, SdsMode.OBJECT_MODE, undoManager, "OBJECT_MODE");
	
	public final ToggleAction snapToGrid = new ToggleAction(snapToGridToggle, undoManager, "SNAP_TO_GRID");
	
	public final JPatchAction undo = new JPatchAction(undoManager, "UNDO") {
		public void actionPerformed(ActionEvent e) {
			if (undoManager.canUndo()) {
				undoManager.undo();
			}
		}
	};
	public final JPatchAction redo = new JPatchAction(undoManager, "REDO") {
		public void actionPerformed(ActionEvent e) {
			if (undoManager.canRedo()) {
				undoManager.redo();
			}
		}
	};
	
	public final JPatchAction open = new JPatchAction(undoManager, "OPEN_FILE") {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showOpenDialog(Main.getInstance().getFrame()) == JFileChooser.APPROVE_OPTION) {
				try {
					Sds sds = new JptLoader().importModel(new FileInputStream(fileChooser.getSelectedFile()));
					Main.getInstance().setModel(sds);
				} catch (IOException ex) {
					ex.printStackTrace();
				} 
			}
			
		}
	};
	
	public final JPatchAction save = new JPatchAction(undoManager, "SAVE_FILE") {
		public void actionPerformed(ActionEvent e) {
			System.out.println("save");
		}
	};
	
	public Actions() {
		/*
		 * configure tool statemachine
		 */
		toolSM.setDefaultState(tools[3]);
		
		extrudeTool.getEnabled().setBoolean(false);
		latheTool.getEnabled().setBoolean(false);
		scaleTool.getEnabled().setBoolean(false);
		save.getEnabled().setBoolean(false);
		
		snapToGrid.getEnabled().setBoolean(false);
		edgeMode.getEnabled().setBoolean(false);
		faceMode.getEnabled().setBoolean(false);
		
		/*
		 * configure undo and redo actions
		 */
		undo.getEnabled().setBoolean(undoManager.canUndo());
		redo.getEnabled().setBoolean(undoManager.canRedo());
		undoManager.addUndoListener(new JPatchUndoListener() {
			public void editAdded(JPatchUndoManager undoManager) {
				undo.getEnabled().setBoolean(undoManager.canUndo());
				redo.getEnabled().setBoolean(undoManager.canRedo());
			}

			public void redoPerformed(JPatchUndoManager undoManager) {
				undo.getEnabled().setBoolean(undoManager.canUndo());
				redo.getEnabled().setBoolean(undoManager.canRedo());
			}

			public void undoPerformed(JPatchUndoManager undoManager) {
				undo.getEnabled().setBoolean(undoManager.canUndo());
				redo.getEnabled().setBoolean(undoManager.canRedo());
			}
		});
	}
}