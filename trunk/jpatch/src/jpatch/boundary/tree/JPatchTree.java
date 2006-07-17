package jpatch.boundary.tree;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import jpatch.entity.*;

public class JPatchTree extends JTree {
	private static final DataFlavor[] DATA_FLAVORS = new DataFlavor[] { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreeNode") };
	
	private DragGestureListener dragGestureListener = new DragGestureListener() {
		public void dragGestureRecognized(DragGestureEvent dge) {
			TreePath path = JPatchTree.this.getSelectionPath(); 
			if (path != null) { 
				JPatchTreeNode draggedNode = (JPatchTreeNode) path.getLastPathComponent();
				Object userObject = draggedNode.getUserObject();
				if (userObject instanceof TransformNode || userObject instanceof Model) {
					dge.getDragSource().startDrag(dge, DragSource.DefaultMoveNoDrop, null, dge.getDragOrigin(), createTransferable(), dragSourceListener);
				}
			}
		}
	};
	
	private DragSourceListener dragSourceListener = new DragSourceListener() {
		public void dragEnter(DragSourceDragEvent dsde) {
			System.out.println("dragEnter(" + dsde + ")");
		}
		public void dragOver(DragSourceDragEvent dsde) {
			Point point = new Point(dsde.getX(), dsde.getY());
			SwingUtilities.convertPointFromScreen(point, JPatchTree.this);
			TreePath path = JPatchTree.this.getPathForLocation(point.x, point.y);
			if (path != null) {
				JPatchTreeNode targetNode = (JPatchTreeNode) path.getLastPathComponent();
				JPatchTreeNode sourceNode = null;
				try {
					TreePath sourcePath = (TreePath) dsde.getDragSourceContext().getTransferable().getTransferData(DATA_FLAVORS[0]);
					sourceNode = (JPatchTreeNode) sourcePath.getLastPathComponent();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (isDragSupported(sourceNode, targetNode)) {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} else {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			} else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
		public void dropActionChanged(DragSourceDragEvent dsde) {
			System.out.println("dropActionChanged(" + dsde + ")");
		}
		public void dragExit(DragSourceEvent dse) {
			System.out.println("dragExit(" + dse + ")");
		}
		public void dragDropEnd(DragSourceDropEvent dsde) {
			System.out.println("dragDropEnd(" + dsde + ")");
		}
	};
	
	private DropTargetListener dropTargetListener = new DropTargetListener() {
		public void dragEnter(DropTargetDragEvent dtde) {
			System.out.println("dragEnter(" + dtde + ")");
		}
		public void dragOver(DropTargetDragEvent dtde) {
			System.out.println("dragOver(" + dtde + ")");
		}
		public void dropActionChanged(DropTargetDragEvent dtde) {
			System.out.println("dropActionChanged(" + dtde + ")");
		}
		public void dragExit(DropTargetEvent dte) {
			System.out.println("dragExit(" + dte + ")");
		}
		public void drop(DropTargetDropEvent dtde) {
			System.out.println("drop(" + dtde + ")");
		}		
	};
	
	public JPatchTree() {
		super(new JPatchTreeModel());
		setCellRenderer(new JPatchTreeCellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		DropTarget dropTarget = new DropTarget();
		try {
			dropTarget.addDropTargetListener(dropTargetListener);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		}
		setDropTarget(dropTarget);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dragGestureListener);
	}
	
	private Transferable createTransferable() {
		return new Transferable() {
			public DataFlavor[] getTransferDataFlavors() {
				return DATA_FLAVORS;
			}
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return flavor.equals(DATA_FLAVORS[0]);
			}
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (!flavor.equals(DATA_FLAVORS[0]))
					throw new UnsupportedFlavorException(flavor);
				return JPatchTree.this.getSelectionPath();
			}
		};
	}
	
	private boolean isDragSupported(JPatchTreeNode source, JPatchTreeNode target) {
		if (source == null || target == null) {
			return false;
		}
		if (target.getUserObject() instanceof TransformNode) {
			if (source.getUserObject() instanceof Model) {
				if (source.getParent() == target) {
					return false;
				}
				return true;
			} else if (source.getUserObject() instanceof TransformNode) {
				if (target == source || target.isNodeAncestor(source) || source.getParent() == target) {
					return false;
				}
				return true;
			}
		}
		return false;
	}
}
