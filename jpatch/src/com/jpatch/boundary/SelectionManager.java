package com.jpatch.boundary;

import java.util.*;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.undo.*;

import com.jpatch.afw.Utils;
import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.afw.attributes.GenericAttr;
import com.jpatch.afw.control.*;
import com.jpatch.entity.*;

public class SelectionManager {
	/**
	 * Attribute to hold the selected object
	 */
//	private final GenericAttr<Object> selectedObjectAttr = new GenericAttr<Object>();
	
	/**
	 * Flag to prevent update loops
	 */
	private boolean ignoreChange;
	
	public SelectionManager(final JTree tree, final TreeManager treeManager, final Selection selection) {
		/*
		 * listen for changes of the selected object to update the tree-selection
		 * accordingly
		 */
		selection.getNodeAttribute().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				if (!ignoreChange) {
					ignoreChange = true;
//					Object selectedObject = selectedObjectAttr.getValue();
//					SceneGraphNode sceneGraphNode = null;
//					if (selectedObject instanceof SceneGraphNode) {
//						sceneGraphNode = (SceneGraphNode) selectedObject;
//					}
					TreeNode treeNode = treeManager.getTreeNodeFor(selection.getNode());
					if (treeNode != null) {
						tree.setSelectionPath(Utils.createTreePath(treeNode));
					} else {
						tree.clearSelection();
					}
					ignoreChange = false;
				}
			}
		});
		
		/*
		 * listen for tree-selection changes to update the selectedObject attribute
		 * accordingly
		 */
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (!ignoreChange) {
					ignoreChange = true;
					Object selectedTreeNode = null;
					if (e.getNewLeadSelectionPath() != null) {
						selectedTreeNode = e.getNewLeadSelectionPath().getLastPathComponent();
					}
					if (selectedTreeNode instanceof XFormNode) {
						List<JPatchUndoableEdit> editList = new ArrayList<JPatchUndoableEdit>(1);
						selection.setNode((XFormNode) selectedTreeNode, editList);
						Main.getInstance().getUndoManager().addEdit("Change selected node", editList);
//						Object selection = ((SceneGraphTreeNode) selectedTreeNode).getSceneGraphNode();
//						selectedObjectAttr.setValue(selection);
					}
					ignoreChange = false;
				}
			}
		});
	}
	
//	/**
//	 * Returns the Attribute that holds the selected object
//	 * @return the Attribute that holds the selected object
//	 */
//	public GenericAttr<Object> getSelectedObjectAttribute() {
//		return selectedObjectAttr;
//	}
}
