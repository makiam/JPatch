package jpatch.boundary.tree;


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.undo.UndoManager;

import jpatch.boundary.*;
import jpatch.control.edit.*;
import jpatch.entity.*;

public class JPatchTree extends JTree {
	private static final DataFlavor[] DATA_FLAVORS = new DataFlavor[] { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreeNode") };
	
	private DragGestureListener dragGestureListener = new DragGestureListener() {
		public void dragGestureRecognized(DragGestureEvent dge) {
			JPatchTreeNode[] selectedNodes = getSelectedNodes();
//			System.out.println("drag " + dge);
//			for (JPatchTreeNode node : selectedNodes) {
//				System.out.println(node);
//			}
//			System.out.println(dge.getTriggerEvent());
			Class userObjectClass = commonUserObjectClass(selectedNodes);
			JPatchTreeNode parent = commonParent(selectedNodes);
//			System.out.println(userObjectClass + " " + parent);
			if (userObjectClass != null && parent != null) {
				if (userObjectClass == TransformNode.class || userObjectClass == Model.class) {
//					System.out.println("start drag");
					dge.getDragSource().startDrag(dge, DragSource.DefaultMoveNoDrop, createTransferable(), dragSourceListener);
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
			System.out.println("dragOver path=" + path);
			if (path != null) {
				JPatchTreeNode targetNode = (JPatchTreeNode) path.getLastPathComponent();
				JPatchTreeNode[] sourceNodes = null;
				try {
					TreePath[] sourcePaths = (TreePath[]) dsde.getDragSourceContext().getTransferable().getTransferData(DATA_FLAVORS[0]);
					sourceNodes = getNodesForPaths(sourcePaths);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (isDragSupported(sourceNodes, targetNode)) {
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
			Point point = new Point(dsde.getX(), dsde.getY());
			SwingUtilities.convertPointFromScreen(point, JPatchTree.this);
			TreePath path = JPatchTree.this.getPathForLocation(point.x, point.y);
			System.out.println("dragOver path=" + path);
			if (path != null) {
				JPatchTreeNode targetNode = (JPatchTreeNode) path.getLastPathComponent();
				JPatchTreeNode[] sourceNodes = null;
				try {
					TreePath[] sourcePaths = (TreePath[]) dsde.getDragSourceContext().getTransferable().getTransferData(DATA_FLAVORS[0]);
					sourceNodes = getNodesForPaths(sourcePaths);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (isDragSupported(sourceNodes, targetNode)) {
					performDrop(sourceNodes, targetNode);
				} else {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			} else {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
			}
		}
	};
	
	private DropTargetListener dropTargetListener = new DropTargetListener() {
		public void dragEnter(DropTargetDragEvent dtde) {
//			System.out.println("dragEnter(" + dtde + ")");
		}
		public void dragOver(DropTargetDragEvent dtde) {
//			System.out.println("dragOver(" + dtde + ")");
		}
		public void dropActionChanged(DropTargetDragEvent dtde) {
//			System.out.println("dropActionChanged(" + dtde + ")");
		}
		public void dragExit(DropTargetEvent dte) {
//			System.out.println("dragExit(" + dte + ")");
		}
		public void drop(DropTargetDropEvent dtde) {
			System.out.println("drop(" + dtde + ")");
		}		
	};
	
	private static class TreeDragGestureRecognizer extends MouseDragGestureRecognizer {
		private Point point;
		TreeDragGestureRecognizer(JPatchTree tree, DragGestureListener dgl) {
			super(new DragSource(), tree, DnDConstants.ACTION_MOVE, dgl);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			point = e.getPoint();
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (point != null && point.distance(e.getPoint()) > DragSource.getDragThreshold()) {
				events.add(e);
				fireDragGestureRecognized(DnDConstants.ACTION_MOVE, point);
				point = null;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			point = null;
		}
	};
	
	public JPatchTree() {
		super(new JPatchTreeModel());
		setCellRenderer(new JPatchTreeCellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		DropTarget dropTarget = new DropTarget();
		try {
			dropTarget.addDropTargetListener(dropTargetListener);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		}
		setDropTarget(dropTarget);
		new TreeDragGestureRecognizer(this, dragGestureListener);
//		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dragGestureListener);
	}
	
	private Transferable createTransferable() {
		System.out.println("createTransferable");
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
				return JPatchTree.this.getSelectionPaths();
			}
		};
	}
	
	private boolean isDragSupported(JPatchTreeNode[] sources, JPatchTreeNode target) {
		System.out.println("isDragSupported");
		if (sources == null || sources.length == 0) {
			return false;
		}
		if (sources[0].getUserObject() instanceof Model) {
			if (target.getUserObject() instanceof TransformNode) {
				if (sources[0].getParent() == target) {
					return false;
				}
				return true;
			}
		} else if (sources[0].getUserObject() instanceof TransformNode) {
			if (target.getUserObject() == null) {
				System.out.println("here");
				for (JPatchTreeNode source : sources) {
					if (target == source || source.getParent() == target) {
						return false;
					}
					return true;
				}
			} else if (target.getUserObject() instanceof TransformNode) {
				for (JPatchTreeNode source : sources) {
					if (target == source || target.isNodeAncestor(source) || source.getParent() == target) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	private void performDrop(JPatchTreeNode[] sources, JPatchTreeNode target) {
//		MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeTreenodeParent(sources, target));
		for (JPatchTreeNode source : sources) {
			setExpandedState(new TreePath(target.getPath()), true);
		}
		dump();
	}
	
	private JPatchTreeNode[] getNodesForPaths(TreePath[] paths) {
		if (paths != null) {
			JPatchTreeNode[] draggedNodes = new JPatchTreeNode[paths.length];
			for (int i = 0; i < paths.length; i++) {
				draggedNodes[i] = (JPatchTreeNode) paths[i].getLastPathComponent();
			}
			return draggedNodes;
		}
		return new JPatchTreeNode[0];
	}
	
	/**
	 * Gets the selected JPatchTreeNodes
	 * @return an array containing the selected JPatchTreeNodes, or a zero-length array if nothis is selected.
	 */
	private JPatchTreeNode[] getSelectedNodes() {
		return getNodesForPaths(getSelectionPaths());
	}
	
	/**
	 * Gets the common class of the userObjects of the passed treeNodes
	 * @param nodes The JPatchTreeNodes
	 * @return The common class of all userObjects, null if there is no common class or if the passed array was empty.
	 */
	private Class commonUserObjectClass(JPatchTreeNode[] nodes) {
		if (nodes.length > 0 && nodes[0] != null && nodes[0].getUserObject() != null) {
			Class userObjectClass = nodes[0].getUserObject().getClass();
			for (int i = 1; i < nodes.length; i++) {
				if (!nodes[i].getUserObject().getClass().equals(userObjectClass)) {
					return null;
				}
			}
			return userObjectClass;
		}
		return null;
	}
	
	/**
	 * Gets the common parent of the passed treeNodes
	 * @param nodes The JPatchTreeNodes
	 * @return The common parent of all userObjects, null if there is no common parent or if the passed array was empty.
	 */
	private JPatchTreeNode commonParent(JPatchTreeNode[] nodes) {
		if (nodes.length > 0 && nodes[0] != null) {
			JPatchTreeNode parent = (JPatchTreeNode) nodes[0].getParent();
			for (int i = 1; i < nodes.length; i++) {
				if (nodes[i].getParent() != parent) {
					return null;
				}
			}
			return parent;
		}
		return null;
	}
	
	public void dump() {
		((JPatchTreeNode) treeModel.getRoot()).dump("");
	}
}
