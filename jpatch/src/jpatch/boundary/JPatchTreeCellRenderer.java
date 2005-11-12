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
	private ImageIcon iconExpressions = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/morphs.png"));
	private ImageIcon iconBones = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bones.png"));
//	private ImageIcon iconGroup = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/group.png"));
	private ImageIcon iconBone = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bone.png"));
	private ImageIcon iconRdofRed1 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_red_1.png"));
	private ImageIcon iconRdofRed2 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_red_2.png"));
	private ImageIcon iconRdofRed3 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_red_3.png"));
	private ImageIcon iconRdofGreen1 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_green_1.png"));
	private ImageIcon iconRdofGreen2 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_green_2.png"));
	private ImageIcon iconRdofGreen3 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_green_3.png"));
	private ImageIcon iconRdofBlue1 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_blue_1.png"));
	private ImageIcon iconRdofBlue2 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_blue_2.png"));
	private ImageIcon iconRdofBlue3 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_blue_3.png"));
//	private ImageIcon iconTransDof = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/trans_dof.png"));
//	private ImageIcon iconMuscle = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/muscle.png"));
	private ImageIcon iconMaterial = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/material.png"));
	private ImageIcon iconMorph = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/morph.png"));
	private ImageIcon iconSelections = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selections.png"));
	private ImageIcon iconSelection = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selection.png"));
	private ImageIcon iconTarget = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/target.png"));
	
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
		else if (value instanceof Morph) {
			if (value instanceof RotationDof) {
				RotationDof rdof = (RotationDof) value;
				if (rdof.getBone().getIndex(rdof) == 0) {
					if (rdof.getType() == 1)
						setIcon(iconRdofRed1);
					else if (rdof.getType() == 2)
						setIcon(iconRdofRed2);
					else if (rdof.getType() == 4)
						setIcon(iconRdofRed3);
				} else if (rdof.getBone().getIndex(rdof) == 1) {
					if (rdof.getType() == 1)
						setIcon(iconRdofGreen1);
					else if (rdof.getType() == 2)
						setIcon(iconRdofGreen2);
					else if (rdof.getType() == 4)
						setIcon(iconRdofGreen3);
				} else if (rdof.getBone().getIndex(rdof) == 2) {
					if (rdof.getType() == 1)
						setIcon(iconRdofBlue1);
					else if (rdof.getType() == 2)
						setIcon(iconRdofBlue2);
					else if (rdof.getType() == 4)
						setIcon(iconRdofBlue3);
				}
			} else {
				setIcon(iconMorph);
			}
		}
		else if (value instanceof MorphTarget) {
			setIcon(iconTarget);
			if (((MorphTarget) value).getPosition() == 0)
				setEnabled(false);
		}
		else if (value instanceof Bone)
			setIcon(iconBone);
		return this;
	}
}

