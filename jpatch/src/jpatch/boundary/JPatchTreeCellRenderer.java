package jpatch.boundary;

import javax.swing.tree.*;
import java.awt.*;
import java.awt.geom.*;

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
//		System.out.println(value + " " + ((TreeNode) value).getParent());
		TreeNode parent = ((TreeNode) value).getParent();
		Model model = MainFrame.getInstance().getModel();
		if (MainFrame.getInstance().getAnimation() != null)
			if (parent instanceof Model)
				model = (Model) parent;
//		System.out.println(value + " " + model + " " + model.getTreenodeBones());
		if (value instanceof Model)
			setIcon(iconModel);
		else if (value instanceof Animation)
			setIcon(iconAnimation);
		else if (model != null && value == model.getTreenodeSelections())
			setIcon(iconSelections);
		else if (model != null && value == model.getTreenodeMaterials())
			setIcon(iconMaterials);
		else if (model != null && value == model.getTreenodeExpressions())
			setIcon(iconExpressions);
		else if (model != null && value == model.getTreenodeBones())
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
						return new MorphSlider(iconRdofRed1, 1, 0, 0, tree,value,sel,expanded,leaf,row,hasFocus);
					else if (rdof.getType() == 2)
						return new MorphSlider(iconRdofRed2, 1, 0, 0, tree,value,sel,expanded,leaf,row,hasFocus);
					else if (rdof.getType() == 4)
						return new MorphSlider(iconRdofRed3, 1, 0, 0, tree,value,sel,expanded,leaf,row,hasFocus);
				} else if (rdof.getBone().getIndex(rdof) == 1) {
					if (rdof.getType() == 1)
						return new MorphSlider(iconRdofGreen1, 0, 0.75f, 0, tree,value,sel,expanded,leaf,row,hasFocus);
					else if (rdof.getType() == 2)
						return new MorphSlider(iconRdofGreen2, 0, 0.75f, 0, tree,value,sel,expanded,leaf,row,hasFocus);
					else if (rdof.getType() == 4)
						return new MorphSlider(iconRdofGreen3, 0, 0.75f, 0, tree,value,sel,expanded,leaf,row,hasFocus);
				} else if (rdof.getBone().getIndex(rdof) == 2) {
					if (rdof.getType() == 1)
						return new MorphSlider(iconRdofBlue1, 0, 0, 1, tree,value,sel,expanded,leaf,row,hasFocus);
					else if (rdof.getType() == 2)
						return new MorphSlider(iconRdofBlue2, 0, 0, 1, tree,value,sel,expanded,leaf,row,hasFocus);
					else if (rdof.getType() == 4)
						return new MorphSlider(iconRdofBlue3, 0, 0, 1, tree,value,sel,expanded,leaf,row,hasFocus);
				}
			} else {
//				setIcon(iconMorph);
				return new MorphSlider(iconMorph, 0.25f, 0.25f, 0.25f, tree,value,sel,expanded,leaf,row,hasFocus);
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
		private boolean selected;
		private float r, gr, b;
		
		public MorphSlider(Icon icon, float r, float g, float b, JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus) {
			super.getTreeCellRendererComponent(tree,value,false,expanded,leaf,row,hasFocus);
			this.selected = sel;
			this.morph = (Morph) value;
			this.tree = tree;
			this.r = r;
			this.gr = g;
			this.b = b;
			setIcon(icon);
			Dimension dim = getPreferredSize();
			if (dim.width < 120)
				dim.width = 120;
			setMinimumSize(dim);
			setPreferredSize(dim);
//			setMaximumSize(dim);
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			float min = morph.getMin();
			float max = morph.getMax();
			float absMax = Math.max(Math.abs(min), Math.abs(max));
			float hmin = min / absMax * 6;
			float hmax = max / absMax * 6;
			float v = morph.getValue();
			float w1 = morph.getSliderValue() - 2;
			float w2 = morph.getSliderValue() + 2;
			float h1 = w1 / 10;
			float h2 = w2 / 10;
			float left = 20;
			float right = left + 100;
			float bottom = getHeight() - 8;
			g.setColor(new Color(r, gr, b, 0.12f));
			GeneralPath path = new GeneralPath();
			path.moveTo(left, bottom - hmin);
			path.lineTo(left, bottom);
			path.lineTo(right + 1, bottom);
			path.lineTo(right + 1, bottom - hmax);
			path.closePath();
			((Graphics2D) g).fill(path);
//			((Graphics2D) g).draw(path);
			((Graphics2D) g).draw(new Line2D.Float(left, bottom, right, bottom));
//			if (w2 > 3) {
//				g.setColor(new Color(r, gr, b, 0.24f));
//				GeneralPath path = new GeneralPath();
//				path.moveTo(left, bottom);
//				path.lineTo(left + w1, bottom);
//				path.lineTo(left + w1, bottom - h1);
//				path.closePath();
//				((Graphics2D) g).fill(path);
//			}
//			if (w1 < 97) {
//				g.setColor(new Color(r, gr, b, 0.08f));
//				GeneralPath path = new GeneralPath();
//				path.moveTo(right, bottom);
//				path.lineTo(left + w2, bottom);
//				path.lineTo(left + w2, bottom - h2);
//				path.lineTo(right, bottom - 10);
//				path.closePath();
//				((Graphics2D) g).fill(path);
//			}
			g.setColor(new Color(r, gr, b, 0.24f));
//			((Graphics2D) g).draw(new Line2D.Float(left, bottom, right, bottom));
//			g.fill3DRect((int) (left + w - 2), 1, 4, 14, true);
			((Graphics2D) g).fill(new Rectangle2D.Float(left + w1, 1, 4, 13));
			((Graphics2D) g).draw(new Rectangle2D.Float(left + w1, 1, 3, 12));
			if (selected) {
				g.setColor(UIManager.getColor("Tree.selectionBackground"));
				g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			}
		}
	}
}

