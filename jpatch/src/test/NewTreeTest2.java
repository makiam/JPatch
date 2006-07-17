package test;



import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.*;
import javax.swing.tree.*;

import jpatch.boundary.tree.*;
import jpatch.entity.*;

public class NewTreeTest2 {

	private static final DataFlavor[] DATA_FLAVORS = new DataFlavor[] { new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "TreeNode") };
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NewTreeTest2();
	}

	public NewTreeTest2() {
		JFrame frame = new JFrame("tree test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTree tree = new JPatchTree();
//		tree.setRootVisible(false);
//		tree.setCellRenderer(new JPatchTreeCellRenderer());
//		tree.setDragEnabled(true);
//		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		
		TransformNode n1 = new TransformNode();
		TransformNode n2 = new TransformNode();
		TransformNode n3 = new TransformNode();
		TransformNode n4 = new TransformNode();
		TransformNode n5 = new TransformNode();
		
		Model m1 = new Model();
		Model m2 = new Model();
		Model m3 = new Model();
		Model m4 = new Model();
		Model m5 = new Model();
		
		n1.name.set("a");
		n2.name.set("b");
		n3.name.set("c");
		n4.name.set("d");
		n5.name.set("e");
		m1.name.set("a");
		m2.name.set("b");
		m3.name.set("c");
		m4.name.set("d");
		m5.name.set("e");
		
		JPatchTreeNode tn1 = new JPatchTreeNode(n1);
		JPatchTreeNode tn2 = new JPatchTreeNode(n2);
		JPatchTreeNode tn3 = new JPatchTreeNode(n3);
		JPatchTreeNode tn4 = new JPatchTreeNode(n4);
		JPatchTreeNode tn5 = new JPatchTreeNode(n5);
		JPatchTreeNode tm1 = new JPatchTreeNode(m1);
		JPatchTreeNode tm2 = new JPatchTreeNode(m2);
		JPatchTreeNode tm3 = new JPatchTreeNode(m3);
		JPatchTreeNode tm4 = new JPatchTreeNode(m4);
		JPatchTreeNode tm5 = new JPatchTreeNode(m5);
		
		JPatchTreeNode root = (JPatchTreeNode) tree.getModel().getRoot();
		root.add(tn5);
		root.add(tn3);
		tn5.add(tn2);
		tn5.add(tn4);
		tn5.add(tn1);
		tn1.add(tm1);
		tn1.add(tm2);
		tn4.add(tm3);
		tn4.add(tm4);
		tn4.add(tm5);
		
		
		frame.add(new JScrollPane(tree));
		frame.pack();
		frame.setVisible(true);
	}
}
