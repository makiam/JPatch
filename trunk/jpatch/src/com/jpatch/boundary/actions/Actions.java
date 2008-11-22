package com.jpatch.boundary.actions;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;

import javax.swing.JFileChooser;

import com.jpatch.afw.*;
import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.*;
import com.jpatch.boundary.*;
import com.jpatch.boundary.tools.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class Actions {
//	public static enum Tool { MOVE_VIEW, ZOOM_VIEW, ROTATE_VIEW, DEFAULT_TOOL, MOVE_TOOL, SCALE_TOOL, ROTATE_TOOL, EXTRUDE_TOOL, LATHE_TOOL }
	public static enum SdsMode { VERTEX_MODE, EDGE_MODE, FACE_MODE, OBJECT_MODE }
	public static enum ViewportMode { VIEWPORT_1, VIEWPORT_2, VIEWPORT_3, VIEWPORT_4, SPLIT_1_2, SPLIT_3_4, SPLIT_1_3, SPLIT_2_4, QUAD } 
	
	public final JPatchTool[] tools = new JPatchTool[] {
			ChangeViewTool.createMoveViewTool(),
			ChangeViewTool.createZoomViewTool(),
			ChangeViewTool.createRotateViewTool(),
			new SelectTool(),
			new RotateTool(),
			new TranslateTool(),
		    new LatheTool(),
			new TweakTool(),
			new ExtrudeTool(),
			new AddEdgeTool(),
			null, // new FlipTool()
			new CornerTool()
	};
	
	public final JPatchUndoManager undoManager = new JPatchUndoManager();
	public final StateMachine<JPatchTool> toolSM = new StateMachine<JPatchTool>(tools, tools[0]);
	public final StateMachine<SdsMode> sdsModeSM = new StateMachine<SdsMode>(SdsMode.class, SdsMode.VERTEX_MODE);
	public final StateMachine<ViewportMode> viewportModeSM = new StateMachine<ViewportMode>(ViewportMode.class, ViewportMode.VIEWPORT_1);
	public final Toggle snapToGridToggle = new Toggle();
	
	public final Toggle selectVerticesToggle = new Toggle();
	public final Toggle selectEdgesToggle = new Toggle();
	public final Toggle selectFacesToggle = new Toggle();
	
	public final SwitchStateAction moveView = new SwitchStateAction(toolSM, tools[0], undoManager, "MOVE_VIEW");
	public final SwitchStateAction zoomView = new SwitchStateAction(toolSM, tools[1], undoManager, "ZOOM_VIEW");
	public final SwitchStateAction rotateView = new SwitchStateAction(toolSM, tools[2], undoManager, "ROTATE_VIEW");
	
	public final SwitchStateAction selectTool = new SwitchStateAction(toolSM, tools[3], undoManager, "SELECT_TOOL");
	public final SwitchStateAction moveTool = new SwitchStateAction(toolSM, tools[5], undoManager, "MOVE_TOOL");
	public final SwitchStateAction scaleTool = new SwitchStateAction(toolSM, tools[8], undoManager, "SCALE_TOOL");
	public final SwitchStateAction rotateTool = new SwitchStateAction(toolSM, tools[4], undoManager, "ROTATE_TOOL");
	public final SwitchStateAction extrudeTool = new SwitchStateAction(toolSM, tools[8], undoManager, "EXTRUDE_TOOL");
	public final SwitchStateAction latheTool = new SwitchStateAction(toolSM, tools[6], undoManager, "LATHE_TOOL");
	public final SwitchStateAction tweakTool = new SwitchStateAction(toolSM, tools[7], undoManager, "TWEAK_TOOL");
	public final SwitchStateAction insetTool = new SwitchStateAction(toolSM, tools[8], undoManager, "INSET_TOOL");
	public final SwitchStateAction addEdgeTool = new SwitchStateAction(toolSM, tools[9], undoManager, "ADD_EDGE_TOOL");
	public final SwitchStateAction flipTool = new SwitchStateAction(toolSM, tools[10], undoManager, "FLIP_TOOL");
	public final SwitchStateAction cornerTool = new SwitchStateAction(toolSM, tools[11], undoManager, "FLIP_TOOL");
	
	public final SwitchStateAction vertexMode = new SwitchStateAction(sdsModeSM, SdsMode.VERTEX_MODE, undoManager, "VERTEX_MODE");
	public final SwitchStateAction edgeMode = new SwitchStateAction(sdsModeSM, SdsMode.EDGE_MODE, undoManager, "EDGE_MODE");
	public final SwitchStateAction faceMode = new SwitchStateAction(sdsModeSM, SdsMode.FACE_MODE, undoManager, "FACE_MODE");
	public final SwitchStateAction objectMode = new SwitchStateAction(sdsModeSM, SdsMode.OBJECT_MODE, undoManager, "OBJECT_MODE");
	
	public final ToggleAction snapToGrid = new ToggleAction(snapToGridToggle, undoManager, "SNAP_TO_GRID");
	
	public final ToggleAction selectVertices = new ToggleAction(selectVerticesToggle, undoManager, "VERTEX_MODE");
	public final ToggleAction selectEdges = new ToggleAction(selectEdgesToggle, undoManager, "EDGE_MODE");
	public final ToggleAction selectFaces = new ToggleAction(selectFacesToggle, undoManager, "FACE_MODE");
	
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
					SdsModel sdsModel = new JptLoader().importModel(new FileInputStream(fileChooser.getSelectedFile()));
					Main.getInstance().setModel(sdsModel);
				} catch (IOException ex) {
					ex.printStackTrace();
				} 
			}
			
		}
	};
	
	public final JPatchAction dump = new JPatchAction(undoManager, "DUMP") {
		public void actionPerformed(ActionEvent e) {
			Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
			sds.dumpFaces(0);
			try {
				Writer writer = new OutputStreamWriter(System.out);
				XmlWriter xmlWriter = new XmlWriter(writer);
				xmlWriter.startDocument();
				xmlWriter.startElement("subdivisionsurface");
				xmlWriter.attribute("scheme", "catmull-clark");
				for (int level = 0; level <= sds.getMinLevelAttribute().getInt(); level++) {
					for (AbstractVertex v : sds.getVertices(level, false)) {
//						v.writeXml(xmlWriter);
					}
				}
				xmlWriter.endElement();
				xmlWriter.endDocument();
				writer.flush();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
//			for (AbstractVertex v : sds.getVertices(2, false)) {
//				int[] hierarchyId = ((DerivedVertex) v).generateId();
//				System.out.println("* searching for " + Arrays.toString(hierarchyId));
//				DerivedVertex d = sds.locateVertex(hierarchyId);
//				System.out.println("*  found " + Arrays.toString(d.generateId()));
//				System.out.println(d == v);
//			}
		}
	};
	
	public final JPatchAction flip = new JPatchAction(undoManager, "FLIP_FACES") {
		public void actionPerformed(ActionEvent e) {
			Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
			Selection selection = Main.getInstance().getSelection();
			if (selection != null) {
				Collection<Face> faces = selection.getFaces();
				if (faces.size() > 0) {
					List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
					sds.flipFaces(editList, faces);
					selection.clear(editList);
					undoManager.addEdit("Extrude", editList);
					Main.getInstance().repaintViewports();
				}
			}
		}
	};
	
	public final JPatchAction extrudeTest = new JPatchAction(undoManager, "EXTRUDE_TOOL") {
		public void actionPerformed(ActionEvent e) {
			System.out.println("extrude");
			SdsModel sdsModel = Main.getInstance().getSelection().getSdsModel();
//			Set<AbstractVertex> selectedVertices = new HashSet<AbstractVertex>(Main.getInstance().getSelection().getVertices());
//			Collection<Face> facesToExtrude = new HashSet<Face>();
//			faceLoop:
//			for (Face face : sds.getFaces(0)) {
//				for (HalfEdge edge : face.getEdges()) {
//					if (!selectedVertices.contains(edge.getVertex())) {
//						continue faceLoop;
//					}
//				}
//				facesToExtrude.add(face);
//			}
////			for (Face face : facesToExtrude) {
//////				sds.dumpFaces(0);
////				sds.removeFace(0, face);
//////				sds.dumpFaces(0);
////			}
//			Collection<Face> facesToExtrude = Main.getInstance().getSelection().getFaces();
			List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>();
			Operations.extrude(Main.getInstance().getSelection(), editList);
			undoManager.addEdit("Extrude", editList);
			toolSM.setValue(tools[7]);
			Main.getInstance().repaintViewports();
		}
	};
	
	public final JPatchAction save = new JPatchAction(undoManager, "SAVE_FILE") {
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showOpenDialog(Main.getInstance().getFrame()) == JFileChooser.APPROVE_OPTION) {
				try {
					Sds sds = Main.getInstance().getSelection().getSdsModel().getSds();
					PrintStream out = new PrintStream(new FileOutputStream(fileChooser.getSelectedFile()));
					new RibExporter().export(Main.getInstance().getSelection().getSdsModel(), out);
//					AmbientOcclusion2 ao = new AmbientOcclusion2();
//					ao.compute(Main.getInstance().getSelection().getSdsModel());
//					ao.dumpSpheres(out);
				} catch (IOException ex) {
					ex.printStackTrace();
				} 
			}
		}
	};
	
	public Actions() {
		/*
		 * configure tool statemachine
		 */
		toolSM.setDefaultState(tools[7]);
		
//		extrudeTool.getEnabled().setBoolean(false);
//		latheTool.getEnabled().setBoolean(false);
		scaleTool.getEnabled().setBoolean(false);
//		save.getEnabled().setBoolean(false);
		rotateTool.getEnabled().setBoolean(false);
		moveTool.getEnabled().setBoolean(false);
		selectTool.getEnabled().setBoolean(false);
		
		snapToGrid.getEnabled().setBoolean(false);
//		edgeMode.getEnabled().setBoolean(false);
//		faceMode.getEnabled().setBoolean(false);
		
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
