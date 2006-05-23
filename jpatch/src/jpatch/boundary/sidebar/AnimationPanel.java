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
import jpatch.boundary.timeline.TimelineEditor;
import jpatch.boundary.ui.*;

public class AnimationPanel extends SidePanel
implements ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6771633567920818871L;
	JPatchInput inputName;
	JPatchInput inputStart;
	JPatchInput inputEnd;
	JPatchInput inputRate;
	Animation anim;
	public AnimationPanel(Animation animation) {
		this.anim = animation;
//		add(new JButton(new EditAnimObjectAction(model)));
//		add(new JButton(new DeleteModelAction(model)));
//		add(new JButton(new CopyPoseAction(model.getModel())));
//		add(new JButton(new PastePoseAction(model.getModel())));
		
		JPatchInput.setDimensions(100,100,20);
		inputName = new JPatchInput("Name:",anim.getName());
		inputStart = new JPatchInput("First frame:",(int) anim.getStart());
		inputEnd = new JPatchInput("Last frame:",(int) anim.getEnd());
		inputRate = new JPatchInput("Framerate:",(int) anim.getFramerate());
		JPanel detailPanel = MainFrame.getInstance().getSideBar().getDetailPanel();
		detailPanel.removeAll();
		detailPanel.add(inputName);
		detailPanel.add(inputStart);
		detailPanel.add(inputEnd);
		detailPanel.add(inputRate);
		inputName.addChangeListener(this);
		inputStart.addChangeListener(this);
		inputEnd.addChangeListener(this);
		inputRate.addChangeListener(this);
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
		if (changeEvent.getSource() == inputName) {
			anim.setName(inputName.getStringValue());
			((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).nodeChanged(anim);
		} else if (changeEvent.getSource() == inputStart) {
			anim.setStart(inputStart.getIntValue());
			if (anim.getPosition() < anim.getStart())
				anim.setPosition(anim.getStart());
			else {
				MainFrame.getInstance().getTimelineEditor().repaint();
			}
		} else if (changeEvent.getSource() == inputEnd) {
			anim.setEnd(inputEnd.getIntValue());
			if (anim.getPosition() > anim.getEnd())
				anim.setPosition(anim.getEnd());
			else {
				MainFrame.getInstance().getTimelineEditor().repaint();
			}
		} else if (changeEvent.getSource() == inputRate) {
			System.out.println("rate changed");
			anim.setFramerate(inputRate.getIntValue());
			MainFrame.getInstance().getTimelineEditor().repaint();
		}
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