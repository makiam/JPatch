/*
 * $Id: Ruler.java,v 1.10 2006/01/30 19:42:01 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary.timeline;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JComponent;
import javax.swing.JScrollBar;

class Ruler extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener {
	/**
	 * 
	 */
	private static enum State { IDLE, RESIZE, MOVE, FRAME }
	
	private final TimelineEditor timelineEditor;
	
	private int mx, my;
	private int iStart, iEnd;
	private boolean bSelection;
	
	private State state = State.IDLE;
	/**
	 * @param editor
	 */
	Ruler(TimelineEditor timelineEditor) {
		this.timelineEditor = timelineEditor;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	private Dimension dim = new Dimension();
	private Font font = new Font("Monospaced", Font.PLAIN, 10);
	private int mouseX;
	
	public Dimension getPreferredSize() {
		dim.setSize(timelineEditor.getFrameWidth() * 24 * 60, 16); // FIXME: use animation length
		return dim;
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g);
		g.setFont(font);
		Rectangle clip = g.getClipBounds();
		
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		//((Graphics2D) g).setPaint(new GradientPaint(0, 0, new Color(255, 255, 128), 0, 16, getBackground().brighter()));
		g.setColor(Color.WHITE);
		g.fillRect(clip.x, clip.y, clip.width, clip.height);
		g.setColor(TimelineEditor.SELECTED_BACKGROUND);
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			if (frame >= iStart && frame <= iEnd)
				g.fillRect(x + start - fw/2, 0, fw, getHeight() - 1);
			frame++;
		}
		g.setColor(Color.BLACK);
		g.drawLine(clip.x, 15, clip.x + clip.width, 15);
		frame = start / fw - 1;
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			if (frame % 6 == 0)
				g.drawLine(x + start, getHeight() - 6, x + start, getHeight() - 1);
			else
				g.drawLine(x + start, getHeight() - 4, x + start, getHeight() - 3);
			if (frame % 12 == 0) {
				if (fw > 2 || frame % 24 == 0) {
					String num = String.valueOf(frame);
					g.drawString(num, x + start - num.length() * 3 + 1, 9);
				}
			}
			if (frame == timelineEditor.getCurrentFrame()) {
//				g.setColor(Color.BLACK);
//				g.fillPolygon(new int[] { x + start - 4, x + start + 7, x + start + 1}, new int[] { clip.y + clip.height - 6, clip.y + clip.height - 6, clip.y + clip.height - 0}, 3);
//				g.setColor(Color.RED);
				g.fillPolygon(new int[] { x + start - 5, x + start + 6, x + start }, new int[] { getHeight() - 6, getHeight() - 6, getHeight() - 0}, 3);
//				g.setColor(Color.BLACK);
			}
			frame++;
		}
		int x = timelineEditor.getColumnHeader().getViewPosition().x;
		g.setColor(new Color(1, 0.0f, 0, 0.25f));
		g.fillRect(x, 0, 10 * fw - 2, 6);
		g.setColor(new Color(1, 0.0f, 0));
		g.fill3DRect(x + 10 * fw - 2, 0, 4, 6, true);
//		g.setColor(Color.WHITE);
//		g.draw3DRect(clip.x, clip.y, clip.width - 1, clip.height - 1, false);
//		g.draw3DRect(clip.x + 1, clip.y + 1, clip.width - 3, clip.height - 3, true);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
//		setFrameWidth(timelineEditor.getFrameWidth() - e.getWheelRotation());
	}
	
	private void setFrameWidth(int newWidth) {
		if (newWidth < 6)
			newWidth = 6;
		if (newWidth > 32)
			newWidth = 32;
		Rectangle r = timelineEditor.getViewport().getViewRect();
		r.x = r.x * newWidth / timelineEditor.getFrameWidth();
		timelineEditor.setFrameWidth(newWidth);
		timelineEditor.getHorizontalScrollBar().setValue(r.x);
		revalidate();
		((JComponent) timelineEditor.getViewport().getView()).revalidate();
		((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
	}
	
	public void mousePressed(MouseEvent e) {
		int fw = timelineEditor.getFrameWidth();
		int x = e.getX() - timelineEditor.getViewport().getViewPosition().x;
		if (e.getButton() == MouseEvent.BUTTON1) {
			
			/*
			 * Left Mousebutton pressed
			 */
			
			if (x > 10 * fw - 4 && x < 10 * fw + 4 && e.getY() < 8) {
				state = State.RESIZE;
			} else if (x > timelineEditor.getCurrentFrame() * fw - 5 && x < timelineEditor.getCurrentFrame() * fw + 11) {
				state = State.FRAME;
			}
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			
			/*
			 * Middle Mousebutton pressed
			 */
			
			mx = e.getX();
			state = State.MOVE;
		}
	}
	
	public void mouseDragged(MouseEvent e) {
		switch (state) {
		case RESIZE:
			int x = e.getX() - timelineEditor.getViewport().getViewPosition().x;
			int fw = x / 10;
			if (fw != timelineEditor.getFrameWidth())
				setFrameWidth(fw);
			break;
		case MOVE:
			JScrollBar hsb = timelineEditor.getHorizontalScrollBar();
//			JScrollBar vsb = timelineEditor.getVerticalScrollBar();
			int sbx = hsb.getValue();
//			int sby = vsb.getValue();
			int dx = e.getX() - mx;
//			int dy = e.getY() - my;
			mx = (sbx == 0 || sbx == hsb.getMaximum() - hsb.getVisibleAmount()) ? e.getX() : e.getX() - dx;
//			my = (sby == 0 || sby == vsb.getMaximum() - vsb.getVisibleAmount()) ? e.getY() : e.getY() - dy;
//			System.out.println("y=" + e.getY() + "\tmy=" + my + " \tdy=" + dy + " \tsby=" + sby + " max=" + (vsb.getMaximum() - vsb.getVisibleAmount()));
			timelineEditor.getHorizontalScrollBar().setValue(sbx - dx);
//			timelineEditor.getVerticalScrollBar().setValue(sby - dy);
			break;
		case FRAME:
			int frame = e.getX() / timelineEditor.getFrameWidth();
			timelineEditor.setCornerText("Frame " + frame);
			timelineEditor.setCurrentFrame(frame);
		}
	}
	
	public void mouseClicked(MouseEvent e) { }
	
	public void mouseReleased(MouseEvent e) {
		state = State.IDLE;
	}
	
	public void mouseEntered(MouseEvent e) { }
	
	public void mouseExited(MouseEvent e) { }
	
	public void mouseMoved(MouseEvent e) {
		int fw = timelineEditor.getFrameWidth();
		int x = e.getX() - timelineEditor.getViewport().getViewPosition().x;
		if (x > 10 * fw - 4 && x < 10 * fw + 4 && e.getY() < 8) {
			setCursor(TimelineEditor.horizontalResizeCursor);
			setToolTipText("drag to zoom");
		} else {
			setCursor(TimelineEditor.defaultCursor);
			setToolTipText("");
		}
		int frame = e.getX() / fw;
		timelineEditor.setCornerText("Frame " + frame);
	}
	
	
}