package test;



import javax.swing.*;
import javax.swing.tree.*;

import jpatch.boundary.tree.*;
import jpatch.entity.*;

public class NewTreeTest2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NewTreeTest2();
	}

	public NewTreeTest2() {
		JFrame frame = new JFrame("tree test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTree tree = new JTree(new JPatchTreeModel());
//		tree.setRootVisible(false);
		tree.setCellRenderer(new JPatchTreeCellRenderer());
		
		TransformNode n1 = new TransformNode();
		TransformNode n2 = new TransformNode();
		TransformNode n3 = new TransformNode();
		TransformNode n4 = new TransformNode();
		TransformNode n5 = new TransformNode();
		
		n1.getAttribute("Name").setValue("a");
		n2.getAttribute("Name").setValue("b");
		n3.getAttribute("Name").setValue("c");
		n4.getAttribute("Name").setValue("d");
		n5.getAttribute("Name").setValue("e");
		
		JPatchTreeNode tn1 = new JPatchTreeNode(n1);
		JPatchTreeNode tn2 = new JPatchTreeNode(n2);
		JPatchTreeNode tn3 = new JPatchTreeNode(n3);
		JPatchTreeNode tn4 = new JPatchTreeNode(n4);
		JPatchTreeNode tn5 = new JPatchTreeNode(n5);
		
		JPatchTreeNode root = (JPatchTreeNode) tree.getModel().getRoot();
		root.add(tn5);
		root.add(tn3);
		tn5.add(tn2);
		tn5.add(tn4);
		tn5.add(tn1);
		
		frame.add(new JScrollPane(tree));
		frame.pack();
		frame.setVisible(true);
	}
}
