package jpatch.boundary;

import javax.swing.tree.*;
import java.awt.*;
import javax.swing.*;

public class JPatchTreeCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ImageIcon[] aimageicon = new ImageIcon[] {
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/root.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/model.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/materials.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/expressions.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bones.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/group.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bone.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rot_dof.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/trans_dof.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/muscle.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/material.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/expression.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selections.png")),
		new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selection.png"))
	};
	
	public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
		super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
		setIcon(aimageicon[((JPatchTreeLeaf)value).getNodeType()]);
		return this;
	}
}

