/**
 * $Id: JPatchTree.java,v 1.3 2005/09/20 16:17:54 sascha_l Exp $
 */
package jpatch.boundary;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import jpatch.entity.*;
//import jpatch.control.edit.AddSelectionEdit;
//import jpatch.control.edit.RemoveSelectionEdit;

/**
 * 
 * @author lois
 *
 */

public class JPatchTree extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1457802932737973862L;
//	private boolean dragStarted = false;
//	private PointSelection dragSelection = null;
//	private TreePath currentPath, lastPath = null;
	private NewSelection dragSelection;
	
	public JPatchTree (JPatchTreeNode jptn) {
		super(jptn);
		
		/*
		 * add MouseListener and MouseMotionListener to support dragging of selections
		 */
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				Object component = getClosestPathForLocation(mouseEvent.getX(), mouseEvent.getY()).getLastPathComponent();
				if (component instanceof NewSelection)
					dragSelection = (NewSelection) component;
				else
					dragSelection = null;
			}
			
			public void mouseReleased(MouseEvent mouseEvent) {
				dragSelection = null;
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent mouseEvent) {
				if (dragSelection == null)
					return;
				Object component = getClosestPathForLocation(mouseEvent.getX(), mouseEvent.getY()).getLastPathComponent();
				if (component instanceof NewSelection && component != dragSelection) {
					NewSelection selection = (NewSelection) component;
					Model model = MainFrame.getInstance().getModel();
					java.util.List list = model.getSelections();
					int index = list.indexOf(selection);
					int[] aiIndex = new int[] { dragSelection.getParent().getIndex(dragSelection) };
					Object[] aObject = new Object[] { dragSelection };
					MainFrame.getInstance().getUndoManager().setEnabled(false);
					model.removeSelection(dragSelection);
					((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereRemoved(dragSelection.getParent(),aiIndex,aObject);
					model.addSelection(index, dragSelection);
					aiIndex = new int[] { dragSelection.getParent().getIndex(dragSelection) };
					((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodesWereInserted(dragSelection.getParent(),aiIndex);
					MainFrame.getInstance().getTree().setSelectionPath(dragSelection.getTreePath());
					MainFrame.getInstance().getUndoManager().setEnabled(true);
				}
			}
		});
	}
}
