package jpatch.boundary;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import jpatch.boundary.sidebar.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public class SideBar extends JPanel
implements TreeSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6876513942065351367L;
	private JScrollPane scrollPane;
	private SidePanel sidePanel;
	private JPanel detailPanel = new JPanel();
	private boolean bListener = true;
	//private TransformPanel transformPanel = new TransformPanel();
	
	public SideBar(JPatchTree tree) {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		//setWidth(200);
		//setPreferredSize
		//scrollPane = new JScrollPane(tree,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane = new JScrollPane(tree);
		Dimension dim = new Dimension(198,300);
		scrollPane.setPreferredSize(dim);
		scrollPane.setMinimumSize(dim);
		scrollPane.setMaximumSize(dim);
		//add(new JLabel("Tree",SwingConstants.LEFT));
		sidePanel = new SidePanel();
		dim = new Dimension(200,100);
		add(scrollPane);
		//add(new JSeparator(SwingConstants.HORIZONTAL));
		add(sidePanel);
		sidePanel.setBorder(BorderFactory.createEtchedBorder());
		//detailPanel.add(new JLabel("test"));
		add(detailPanel);
		tree.setFocusable(false);
		tree.addTreeSelectionListener(this);
		//detailPanel.setLayout(new GridLayout(10,1));
		dim = new Dimension(198,1000);
		detailPanel.setPreferredSize(dim);
		detailPanel.setMinimumSize(dim);
		detailPanel.setMaximumSize(dim);
		scrollPane.setEnabled(false);
	}
	
	
	public void replacePanel(SidePanel newSidePanel) {
		remove(detailPanel);
		remove(sidePanel);
		sidePanel = newSidePanel;
		add(sidePanel);
		add(detailPanel);
		
		sidePanel.setBorder(BorderFactory.createEtchedBorder()); //(new Color(0,0,0)));
		sidePanel.repaint();
	}
	
	public void setTree(JPatchTree tree) {
//		scrollPane.remove(scrollPane.getComponent(0));
//		scrollPane.add(tree);
		remove(scrollPane);
		scrollPane = new JScrollPane(tree);
		Dimension dim = new Dimension(198,300);
		scrollPane.setPreferredSize(dim);
		scrollPane.setMinimumSize(dim);
		scrollPane.setMaximumSize(dim);
		tree.setFocusable(false);
		tree.addTreeSelectionListener(this);
		add(scrollPane,0);
		validate();
		//scrollPane.add(tree);
	}
	
	public void clearDetailPanel() {
		detailPanel.removeAll();
		detailPanel.repaint();
	}
	
	public JPanel getDetailPanel() {
		return detailPanel;
	}
	
	public SidePanel getSidePanel() {
		return sidePanel;
	}
	
	public void enableTreeSelectionListener(boolean enable) {
		bListener = enable;
	}
	
	public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
		if (!bListener)
			return;
		//System.out.println(treeSelectionEvent);
		//System.out.println(treeSelectionEvent.getPath());
		Object[] aobjectPath = treeSelectionEvent.getPath().getPath();
		JPatchTreeLeaf selectedLeaf = (JPatchTreeLeaf)aobjectPath[aobjectPath.length - 1];
		switch(selectedLeaf.getNodeType()) {
			case JPatchTreeNode.MODEL:
				replacePanel(new ModelPanel((Model)selectedLeaf));
				break;
			case JPatchTreeNode.MATERIALS:
				replacePanel(new MaterialsPanel((Model)selectedLeaf.getParent()));
				break;
			case JPatchTreeNode.MATERIAL:
				replacePanel(new MaterialPanel((JPatchMaterial)selectedLeaf));
				break;
			case JPatchTreeNode.MORPHS:
				replacePanel(new MorphsPanel((JPatchTreeNode)selectedLeaf));
				//MainFrame.getInstance().switchMode(MainFrame.MORPH);
				break;
			case JPatchTreeNode.MORPH:
				replacePanel(new MorphPanel((Morph)selectedLeaf));
				//MainFrame.getInstance().switchMode(MainFrame.MORPH);
				break;
			case JPatchTreeNode.MORPHGROUP:
				replacePanel(new MorphGroupPanel((JPatchTreeNode)selectedLeaf));
				break;
			case JPatchTreeNode.SELECTIONS:
				replacePanel(new SelectionsPanel((Model)selectedLeaf.getParent()));
				break;
			case JPatchTreeNode.SELECTION:
				replacePanel(new SelectionPanel((NewSelection) selectedLeaf));
				if (!selectedLeaf.equals(MainFrame.getInstance().getSelection())) {
					MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(((NewSelection) selectedLeaf).cloneSelection()));
					MainFrame.getInstance().getJPatchScreen().update_all();
				}break;
			default:
				replacePanel(new SidePanel());
				break;
		}
		validate();
		//System.out.println(selectedLeaf);
	}
}

