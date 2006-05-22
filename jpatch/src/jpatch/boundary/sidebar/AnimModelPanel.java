package jpatch.boundary.sidebar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.action.*;
import jpatch.boundary.ui.*;

public class AnimModelPanel extends SidePanel
implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6771633567920818871L;
	JPatchInput inputName;
	AnimModel model;
	public AnimModelPanel(AnimModel model) {
		this.model = model;
		add(new JButton(new EditAnimObjectAction(model)));
		add(new JButton(new DeleteAnimObjectAction(model)));
		add(new JButton(new CopyPoseAction(model.getModel())));
		add(new JButton(new PastePoseAction(model.getModel())));
		
		JPatchInput.setDimensions(50,150,20);
		inputName = new JPatchInput("Name:",model.getName());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		inputName.addChangeListener(this);
//		JList list = new JList(new DefaultListModel());
//		for (Iterator it = model.getModel().getMorphList().iterator(); it.hasNext(); ) {
//			Morph morph = (Morph) it.next();
//			((DefaultListModel) list.getModel()).addElement(new MorphSlider(morph));
//		}
//		JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		detailPanel.add(scrollPane);
		detailPanel.repaint();
	}
	
	public void stateChanged(ChangeEvent changeEvent) {
		model.setName(inputName.getStringValue());
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(model);
		MainFrame.getInstance().requestFocus();
	}
	
//	public static class MorphSlider extends DefaultListCellRenderer implements Morph.MorphListener {
//		private Morph morph;
//		public MorphSlider(Morph morph) {
//			this.morph = morph;
//			setText(morph.getName());
//			Dimension dim = getPreferredSize();
//			if (dim.width < 100)
//				dim.width = 100;
//			System.out.println(dim);
//			setMinimumSize(dim);
//			setPreferredSize(dim);
////			setMaximumSize(dim);
////			morph.setMorphListener(this);
//		}
//		
//		public void paint(Graphics g) {
//			super.paint(g);
//			g.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
//			float w = morph.getSliderValue();
//			float h = w / 10;
//			GeneralPath path = new GeneralPath();
//			float left = getWidth() - 100;
//			float bottom = getHeight();
//			path.moveTo(left, bottom);
//			path.lineTo(left + w, bottom);
//			path.lineTo(left + w, bottom - h);
//			path.closePath();
//			((Graphics2D) g).fill(path);
//		}
//
//		public void valueChanged(int value) {
////			repaint();
//		}
//	}
}