package jpatch.boundary;

import javax.swing.tree.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

import jpatch.entity.*;

public class JPatchTreeCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private ImageIcon iconRoot = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/root.png"));
	private static ImageIcon iconModel = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/model.png"));
	private static ImageIcon iconMaterials = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/materials.png"));
	private static ImageIcon iconExpressions = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/morphs.png"));
	private static ImageIcon iconBones = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bones.png"));
//	private ImageIcon iconGroup = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/group.png"));
	private static ImageIcon iconBone = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/bone.png"));
	private static ImageIcon iconRdofRed1 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_red_1.png"));
	private static ImageIcon iconRdofRed2 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_red_2.png"));
	private static ImageIcon iconRdofRed3 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_red_3.png"));
	private static ImageIcon iconRdofGreen1 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_green_1.png"));
	private static ImageIcon iconRdofGreen2 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_green_2.png"));
	private static ImageIcon iconRdofGreen3 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_green_3.png"));
	private static ImageIcon iconRdofBlue1 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_blue_1.png"));
	private static ImageIcon iconRdofBlue2 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_blue_2.png"));
	private static ImageIcon iconRdofBlue3 = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/rdof_blue_3.png"));
//	private ImageIcon iconTransDof = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/trans_dof.png"));
//	private ImageIcon iconMuscle = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/muscle.png"));
	private static ImageIcon iconMaterial = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/material.png"));
	private static ImageIcon iconMorph = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/morph.png"));
	private static ImageIcon iconSelections = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selections.png"));
	private static ImageIcon iconSelection = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/selection.png"));
	private static ImageIcon iconTarget = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/target.png"));
	
	private static ImageIcon iconAnimation = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/animation.png"));
	private static ImageIcon iconModels = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/models.png"));
	private static ImageIcon iconCameras = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/camera.png"));
	private static ImageIcon iconLights = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/light.png"));
	private static ImageIcon iconCamera = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/camera.png"));
	private static ImageIcon iconLight = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/tree/light.png"));
	
	public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
		super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
		
		if (value instanceof Model)
			setIcon(iconModel);
		else if (value instanceof Animation)
			setIcon(iconAnimation);
		else if (MainFrame.getInstance().getModel() != null && value == MainFrame.getInstance().getModel().getTreenodeSelections())
			setIcon(iconSelections);
		else if (MainFrame.getInstance().getModel() != null && value == MainFrame.getInstance().getModel().getTreenodeMaterials())
			setIcon(iconMaterials);
		else if (MainFrame.getInstance().getModel() != null && value == MainFrame.getInstance().getModel().getTreenodeExpressions())
			setIcon(iconExpressions);
		else if (MainFrame.getInstance().getModel() != null && value == MainFrame.getInstance().getModel().getTreenodeBones())
			setIcon(iconBones);
		else if (MainFrame.getInstance().getAnimation() != null && value == MainFrame.getInstance().getAnimation().getTreenodeModels())
			setIcon(iconModels);
		else if (MainFrame.getInstance().getAnimation() != null && value == MainFrame.getInstance().getAnimation().getTreenodeLights())
			setIcon(iconLights);
		else if (MainFrame.getInstance().getAnimation() != null && value == MainFrame.getInstance().getAnimation().getTreenodeCameras())
			setIcon(iconCameras);
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
//				setIcon(iconMorph);
				return new MorphSlider(tree,value,sel,expanded,leaf,row,hasFocus);
			}
		}
		else if (value instanceof MorphTarget) {
			setIcon(iconTarget);
			if (((MorphTarget) value).getPosition() == 0)
				setEnabled(false);
		}
		else if (value instanceof Bone)
			setIcon(iconBone);
		else if (value instanceof Camera)
			setIcon(iconCamera);
		else if (value instanceof AnimLight)
			setIcon(iconLight);
		else if (value instanceof AnimModel)
			setIcon(iconModel);
		return this;
	}
	
	public static class MorphSlider extends DefaultTreeCellRenderer {
		private Morph morph;
		private JTree tree;
		public MorphSlider(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
			super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
			this.morph = (Morph) value;
			this.tree = tree;
			setIcon(iconMorph);
			Dimension dim = getPreferredSize();
			if (dim.width < 120)
				dim.width = 120;
			System.out.println(dim);
			setMinimumSize(dim);
			setPreferredSize(dim);
//			setMaximumSize(dim);
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.24f));
			float w = morph.getSliderValue();
			float h = w / 10;
			float left = 20;
			float right = left + 100;
			float bottom = getHeight() - 2;
			GeneralPath path = new GeneralPath();
			path.moveTo(left, bottom);
			path.lineTo(left + w, bottom);
			path.lineTo(left + w, bottom - h);
			path.closePath();
			((Graphics2D) g).fill(path);
			g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.08f));
			path = new GeneralPath();
			path.moveTo(right, bottom);
			path.lineTo(left + w, bottom);
			path.lineTo(left + w, bottom - h);
			path.lineTo(right, bottom - 10);
			path.closePath();
			((Graphics2D) g).fill(path);
			g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.48f));
			((Graphics2D) g).fill(new Rectangle2D.Float(left + w - 1, 1, 2, 14));
		}
	}
}

