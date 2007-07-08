package com.jpatch.test;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;

import com.jpatch.boundary.TreeManager;
import com.jpatch.entity.SceneGraphTreeNode;
import com.jpatch.entity.TransformNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
public class TreeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TreeTest();
	}
	
	TreeTest() {
		JFrame frame = new JFrame("TreeTest");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final TransformNode node1 = new TransformNode();
		final TransformNode node2 = new TransformNode();
		final TransformNode node3 = new TransformNode();
		final TransformNode node4 = new TransformNode();
		
		node3.getParentAttribute().setValue(node2);
		node4.getParentAttribute().setValue(node2);
		node2.getParentAttribute().setValue(node1);
		
		final DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		final TreeManager treeManager = new TreeManager(treeModel);
		treeManager.createTreeNodeFor(node1);
		
		JTree tree = new JTree(treeModel);
		frame.setSize(600, 600);
		frame.add(tree);
		frame.setVisible(true);
		
		System.out.println("node1=" + treeManager.getTreeNodeFor(node1));
		System.out.println("node2=" + treeManager.getTreeNodeFor(node2));
		System.out.println("node3=" + treeManager.getTreeNodeFor(node3));
		System.out.println("node4=" + treeManager.getTreeNodeFor(node4));
		
		final Timer timer = new Timer(5000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						System.out.println("*");
						node4.getParentAttribute().setValue(node1);
					}
				});
			}	
		});
		timer.start();
		
	}

}
