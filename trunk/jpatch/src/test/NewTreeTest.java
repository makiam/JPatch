package test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.tree.*;

import jpatch.entity.*;

public class NewTreeTest {

	private static final DataFlavor[] DATA_FLAVORS = new DataFlavor[] { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreeNode") };
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NewTreeTest();
	}

	public NewTreeTest() {
		JFrame frame = new JFrame("tree test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPatchObject ob1 = new JPatchObject() {
			private DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("test 1");
			public MutableTreeNode getTreeNode() {
				return treeNode;
			}
		};
		
		JPatchObject ob2 = new JPatchObject() {
			private DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("test 2");
			public MutableTreeNode getTreeNode() {
				return treeNode;
			}
		};
		
		JPatchObject ob3 = new JPatchObject() {
			private DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("test 3");
			public MutableTreeNode getTreeNode() {
				return treeNode;
			}
		};
		
		TransformNode tr1 = new TransformNode(ob1);
		TransformNode tr2 = new TransformNode(ob2);
		TransformNode tr3 = new TransformNode(ob3);
		
		JTree tree = new JTree(JPatchObject.ROOT_NODE);
		tree.setDragEnabled(true);
//		tree.getTransferHandler().
		
		tree.setTransferHandler(new TransferHandler() {
			
			@Override
			public int getSourceActions(JComponent c) {
				return MOVE;
			}
			
			protected Transferable createTransferable(final JComponent c) {
				System.out.println("createTransferable " + c);
				
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
						return ((JTree) c).getSelectionPath();
					}
					
				};
			}
		});
//		tree.addMouseMotionListener(new MouseMotionAdapter() {
//			@Override
//			public void mouseDragged(MouseEvent e) {
//				TransferHandler = ((JTree) e.getSource()).getTra
//			}
//		});
		
		
		DropTarget dt = new DropTarget(tree, new DropTargetListener() {

			public void dragEnter(DropTargetDragEvent dtde) {
				System.out.println("dragEnter " + dtde);
			}

			public void dragOver(DropTargetDragEvent dtde) {
//				dtde.getDropTargetContext().getComponent().setCursor(DragSource.DefaultMoveDrop);
//				Point pt = dtde.getLocation();
//				JTree tree = (JTree) dtde.getDropTargetContext().getDropTarget().getComponent();
//				tree.getTransferHandler().getVisualRepresentation(dtde.getTransferable()).paintIcon(tree, tree.getGraphics(), pt.x, pt.y);
				dtde.getDropTargetContext().getComponent().setCursor(DragSource.DefaultMoveNoDrop);
			}

			public void dropActionChanged(DropTargetDragEvent dtde) {
				System.out.println("dropActionChanged " + dtde);
			}

			public void dragExit(DropTargetEvent dte) {
				System.out.println("dragExit " + dte);
			}

			public void drop(DropTargetDropEvent dtde) {
				System.out.println("drop " + dtde);
				Transferable t = dtde.getTransferable();
				System.out.println(t);
				try {
					TreePath sourcePath = (TreePath) t.getTransferData(DATA_FLAVORS[0]);
					JTree tree = (JTree) dtde.getDropTargetContext().getDropTarget().getComponent();
					Point pt = dtde.getLocation();
					TreePath targetPath = tree.getPathForLocation(pt.x, pt.y);
					if (sourcePath.isDescendant(targetPath)) {
						System.out.println("reject");
						dtde.rejectDrop();
					} else {
						
						MutableTreeNode dragSource = (MutableTreeNode) sourcePath.getLastPathComponent();
						MutableTreeNode dragTarget = (MutableTreeNode) targetPath.getLastPathComponent();
						MutableTreeNode dragSourceParent = (MutableTreeNode) dragSource.getParent();
//						boolean expanded = tree.isExpanded(sourcePath);
						System.out.println(dragSource);
						System.out.println(dragSourceParent);
						int index = dragSourceParent.getIndex(dragSource);
						System.out.println("index=" + index);
						dragSourceParent.remove(index);
						((DefaultTreeModel) tree.getModel()).nodesWereRemoved(dragSourceParent, new int[] { index }, new Object[] { dragSource });
						dragTarget.insert(dragSource, 1);
						((DefaultTreeModel) tree.getModel()).nodesWereInserted(dragTarget, new int[] { 1 });
//						if (expanded)
//							tree.expandPath(targetPath.pathByAddingChild(dragSource));

					}
				} catch (UnsupportedFlavorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		tree.setDropTarget(dt);
		
		JPatchObject.ROOT_NODE.insert(tr1.getTreeNode(), 0);
		JPatchObject.ROOT_NODE.insert(tr2.getTreeNode(), 1);
		tr2.getTreeNode().insert(tr3.getTreeNode(), 1);
		
		frame.add(new JScrollPane(tree));
		frame.pack();
		frame.setVisible(true);
	}
}
