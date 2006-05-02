package jpatch.boundary.sidebar;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import jpatch.boundary.JPatchTree;
import jpatch.boundary.MainFrame;
import jpatch.boundary.Selection;
import jpatch.boundary.sidebar.*;
import jpatch.entity.*;
import jpatch.control.edit.*;

public class SideBar extends JSplitPane
implements TreeSelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6876513942065351367L;
	private JScrollPane scrollPane;
	private SidePanel sidePanel;
	private JPanel detailPanel = new JPanel();
	private boolean bListener = true;
	private JPanel box;
	private JPanel box2;
	//private TransformPanel transformPanel = new TransformPanel();
	
	public SideBar() {
		super(VERTICAL_SPLIT);
		//setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		//setWidth(200);
		//setPreferredSize
		//scrollPane = new JScrollPane(tree,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		setDividerSize(4);
		setDividerLocation(MainFrame.getInstance().getHeight() - 300);
		setContinuousLayout(true);
		scrollPane = new JScrollPane();
		//Dimension dim = new Dimension(248,400);
		//scrollPane.setPreferredSize(dim);
		//scrollPane.setMinimumSize(dim);
		//scrollPane.setMaximumSize(dim);
		//add(new JLabel("Tree",SwingConstants.LEFT));
		sidePanel = new SidePanel();
		//dim = new Dimension(200,100);
		add(scrollPane);
		//add(new JSeparator(SwingConstants.HORIZONTAL));
		box = new JPanel();
		box.setLayout(new BorderLayout());
		box.add(sidePanel, BorderLayout.NORTH);
		box2 = new JPanel();
		box2.setLayout(new BorderLayout());
		box2.add(detailPanel, BorderLayout.NORTH);
		sidePanel.setBorder(BorderFactory.createEtchedBorder());
		//detailPanel.add(new JLabel("test"));
		box.add(box2, BorderLayout.CENTER);
//		tree.setFocusable(false);
//		tree.addTreeSelectionListener(this);
		//detailPanel.setLayout(new GridLayout(10,1));
//		dim = new Dimension(248,1000);
//		detailPanel.setPreferredSize(dim);
//		detailPanel.setMinimumSize(dim);
//		detailPanel.setMaximumSize(dim);
		scrollPane.setEnabled(false);
		add(box);
		detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
	}
	
	
	public void replacePanel(SidePanel newSidePanel) {
		box.remove(sidePanel);
		box2.remove(detailPanel);
		sidePanel = newSidePanel;
		box.add(sidePanel, BorderLayout.NORTH);
		box2.add(detailPanel, BorderLayout.NORTH);
		
		sidePanel.setBorder(BorderFactory.createEtchedBorder()); //(new Color(0,0,0)));
		sidePanel.repaint();
	}
	
	public void setTree(JPatchTree tree) {
//		scrollPane.remove(scrollPane.getComponent(0));
//		scrollPane.add(tree);
		remove(scrollPane);
		scrollPane.setViewportView(tree);
//		Dimension dim = new Dimension(198,300);
//		scrollPane.setPreferredSize(dim);
//		scrollPane.setMinimumSize(dim);
//		scrollPane.setMaximumSize(dim);
//		tree.setFocusable(false);
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
//		Object[] aobjectPath = treeSelectionEvent.getPath().getPath();
//		JPatchTreeLeaf selectedLeaf = (JPatchTreeLeaf)aobjectPath[aobjectPath.length - 1];
//		switch(selectedLeaf.getNodeType()) {
//			case JPatchTreeNode.MODEL:
//				replacePanel(new ModelPanel((Model)selectedLeaf));
//				break;
//			case JPatchTreeNode.MATERIALS:
//				replacePanel(new MaterialsPanel((Model)selectedLeaf.getParent()));
//				break;
//			case JPatchTreeNode.MATERIAL:
//				replacePanel(new MaterialPanel((JPatchMaterial)selectedLeaf));
//				break;
//			case JPatchTreeNode.MORPHS:
//				replacePanel(new MorphsPanel((JPatchTreeNode)selectedLeaf));
//				//MainFrame.getInstance().switchMode(MainFrame.MORPH);
//				break;
//			case JPatchTreeNode.MORPH:
//				replacePanel(new MorphPanel((Morph)selectedLeaf));
//				//MainFrame.getInstance().switchMode(MainFrame.MORPH);
//				break;
//			case JPatchTreeNode.MORPHGROUP:
//				replacePanel(new MorphGroupPanel((JPatchTreeNode)selectedLeaf));
//				break;
//			case JPatchTreeNode.SELECTIONS:
//				replacePanel(new SelectionsPanel((Model)selectedLeaf.getParent()));
//				break;
//			case JPatchTreeNode.SELECTION:
//				replacePanel(new SelectionPanel((Selection) selectedLeaf));
//				if (!selectedLeaf.equals(MainFrame.getInstance().getSelection())) {
//					MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(((Selection) selectedLeaf).cloneSelection()));
//					MainFrame.getInstance().getJPatchScreen().update_all();
//				}break;
//			case JPatchTreeNode.BONES:
//				replacePanel(new SidePanel());
//				MainFrame.getInstance().getSideBar().clearDetailPanel();
//				break;
//			case JPatchTreeNode.BONE:
//				replacePanel(new BonePanel((Bone) selectedLeaf));
//				//MainFrame.getInstance().switchMode(MainFrame.MORPH);
//				break;
//			case JPatchTreeNode.RDOF:
//				replacePanel(new DofPanel((RotationDof) selectedLeaf));
//				//MainFrame.getInstance().switchMode(MainFrame.MORPH);
//				break;
//			default:
//				replacePanel(new SidePanel());
//				break;
//		}
//		validate();
		//System.out.println(selectedLeaf);
		boolean anim = MainFrame.getInstance().getAnimation() != null;
		MutableTreeNode selectedNode = (MutableTreeNode) treeSelectionEvent.getPath().getLastPathComponent();
//		System.out.println("tree hit: selected node = " + selectedNode);
		if (!anim && selectedNode == MainFrame.getInstance().getModel().getTreenodeMaterials()) {
			replacePanel(new MaterialsPanel((Model) selectedNode.getParent()));
		} else if (selectedNode instanceof JPatchMaterial) {
			replacePanel(new MaterialPanel((JPatchMaterial) selectedNode));
		} else if (!anim && selectedNode == MainFrame.getInstance().getModel().getTreenodeExpressions()) {
			replacePanel(new MorphsPanel());
		} else if (selectedNode instanceof Morph) {
			if (selectedNode instanceof RotationDof) {
				RotationDof dof = (RotationDof) selectedNode;
				replacePanel(new DofPanel(dof));
				Map map = new HashMap();
				Bone bone = dof.getBone();
				map.put(bone.getBoneEnd(), new Float(1));
				map.put(bone.getParentBone() == null ? bone.getBoneStart() : bone.getParentBone().getBoneEnd(), new Float(1));
				MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(new Selection(map)));
				MainFrame.getInstance().getJPatchScreen().update_all();
			} else {	
				replacePanel(new MorphPanel((Morph) selectedNode));
			}
		} else if (selectedNode instanceof MorphTarget) {
			MorphTarget target = (MorphTarget) selectedNode;
//			if (target.getPosition() != 0) {
				replacePanel(new MorphTargetPanel(target));
//			} else {
//				replacePanel(new SidePanel());
//				MainFrame.getInstance().getSideBar().clearDetailPanel();
//			}
			target.getMorph().setValue(target.getPosition());
			MainFrame.getInstance().getJPatchScreen().update_all();
		} else if (!anim && selectedNode == MainFrame.getInstance().getModel().getTreenodeSelections()) {
			replacePanel(new SelectionsPanel((Model) selectedNode.getParent()));
		} else if (selectedNode instanceof Selection) {
			replacePanel(new SelectionPanel((Selection) selectedNode));
			if (!selectedNode.equals(MainFrame.getInstance().getSelection())) {
				MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(((Selection) selectedNode).cloneSelection()));
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		} else if (!anim && selectedNode == MainFrame.getInstance().getModel().getTreenodeBones()) {
			replacePanel(new BonesPanel(MainFrame.getInstance().getModel()));
			MainFrame.getInstance().getSideBar().clearDetailPanel();
		} else if (selectedNode instanceof Bone) {
			Bone bone = (Bone) selectedNode;
			replacePanel(new BonePanel(bone));
			Map map = new HashMap();
			map.put(bone.getBoneEnd(), new Float(1));
			map.put(bone.getParentBone() == null ? bone.getBoneStart() : bone.getParentBone().getBoneEnd(), new Float(1));
			if (bone.getDofs().size() > 0)
				MainFrame.getInstance().getTree().expandPath(treeSelectionEvent.getPath());
			MainFrame.getInstance().getUndoManager().addEdit(new AtomicChangeSelection(new Selection(map)));
			MainFrame.getInstance().getJPatchScreen().update_all();
		} else if (selectedNode instanceof Model) {
			replacePanel(new ModelPanel((Model) selectedNode));
		} else if (anim && selectedNode == MainFrame.getInstance().getAnimation().getTreenodeCameras()) {
			replacePanel(new CamerasPanel());
		} else if (anim && selectedNode == MainFrame.getInstance().getAnimation().getTreenodeLights()) {
			replacePanel(new LightsPanel());
		} else if (anim && selectedNode == MainFrame.getInstance().getAnimation().getTreenodeModels()) {
			replacePanel(new AnimModelsPanel());
		} else if (anim && selectedNode instanceof Camera) {
			replacePanel(new CameraPanel((Camera) selectedNode));
		} else if (anim && selectedNode instanceof AnimLight) {
			replacePanel(new LightPanel((AnimLight) selectedNode));
		} else if (anim && selectedNode instanceof AnimModel) {
			replacePanel(new AnimModelPanel((AnimModel) selectedNode));
		} else if (anim && selectedNode instanceof Animation) {
			replacePanel(new AnimationPanel(MainFrame.getInstance().getAnimation()));
		} else {
			replacePanel(new SidePanel());
			detailPanel.removeAll();
		}
		validate();
	}
}

