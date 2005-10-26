package jpatch.boundary;

import javax.swing.tree.*;
import java.awt.*;
import javax.swing.*;

import jpatch.entity.*;

public class JPatchTreeCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private ImageIcon iconRoot = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/root.png"));
	private ImageIcon iconModel = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/model.png"));
	private ImageIcon iconMaterials = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/materials.png"));
	private ImageIcon iconExpressions = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/expressions.png"));
	private ImageIcon iconBones = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bones.png"));
//	private ImageIcon iconGroup = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/group.png"));
	private ImageIcon iconBone = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bone.png"));
	private ImageIcon iconRotDof = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rot_dof.png"));
//	private ImageIcon iconTransDof = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/trans_dof.png"));
//	private ImageIcon iconMuscle = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/muscle.png"));
	private ImageIcon iconMaterial = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/material.png"));
	private ImageIcon iconMorph = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/expression.png"));
	private ImageIcon iconSelections = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selections.png"));
	private ImageIcon iconSelection = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selection.png"));
	
	public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
		super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
		if (value instanceof Model)
			setIcon(iconModel);
		else if (value == MainFrame.getInstance().getModel().getTreenodeSelections())
			setIcon(iconSelections);
		else if (value == MainFrame.getInstance().getModel().getTreenodeMaterials())
			setIcon(iconMaterials);
		else if (value == MainFrame.getInstance().getModel().getTreenodeExpressions())
			setIcon(iconExpressions);
		else if (value == MainFrame.getInstance().getModel().getTreenodeBones())
			setIcon(iconBones);
		else if (value instanceof Selection)
			setIcon(iconSelection);
		else if (value instanceof JPatchMaterial)
			setIcon(iconMaterial);
		else if (value instanceof Morph)
			setIcon(iconMorph);
		else if (value instanceof Bone)
			setIcon(iconBone);
		return this;
	}
}

