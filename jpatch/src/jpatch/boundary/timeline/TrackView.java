/*
 * $Id: TrackView.java,v 1.18 2006/02/06 19:44:06 sascha_l Exp $
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
import java.util.Set;

import javax.swing.*;

import jpatch.control.edit.*;
import jpatch.entity.Animation;
import jpatch.entity.MotionKey;
import jpatch.boundary.*;

@SuppressWarnings("serial")
class TrackView extends JComponent implements Scrollable, MouseListener, MouseMotionListener, MouseWheelListener {
	/**
	 * 
	 */
	private static enum State { IDLE, RESIZE, SCROLL, MOVE_KEY };
	private State state = State.IDLE;
	private final TimelineEditor timelineEditor;
	private Dimension dim = new Dimension();
	private int iVerticalResize = -1;
	private int mx, my, trackTop;
	private Object selectedKey;
	private Track selectedTrack;
	
	public TrackView(TimelineEditor tle) {
		timelineEditor = tle;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	public Dimension getPreferredSize() {
		dim.setSize(timelineEditor.getFrameWidth() * MainFrame.getInstance().getAnimation().getEnd(), timelineEditor.getTracksHeight() + 7); // FIXME: use animation length
		return dim;
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	public void paintComponent(Graphics g) {
//		long t = System.currentTimeMillis();
		super.paintComponent(g);
		Rectangle clip = g.getClipBounds();
//		System.out.println(clip);
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		//int frame = start / TimelineEditor.this.iFrameWidth - 1;
//		g.setColor(Color.WHITE);
//		for (int x = -TimelineEditor.this.iFrameWidth ; x <= clip.width + TimelineEditor.this.iFrameWidth; x += TimelineEditor.this.iFrameWidth) {
//		g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//		}
		g.setColor(TimelineEditor.BACKGROUND);
		g.fillRect(clip.x, clip.y, clip.width, clip.height);
		
		int y = 0;
		Set<Track> selectedTracks = timelineEditor.getHeader().getSelectedTracks();
		g.setColor(TimelineEditor.SELECTED_BACKGROUND);
		for (Track track : timelineEditor.getTracks()) {
			if (track.isHidden())
				continue;
			if (y + track.getHeight() > clip.y && y < clip.y + clip.height)
				if (selectedTracks.contains(track))
					g.fillRect(clip.x, y, clip.width, track.getHeight());
			y += track.getHeight();
		}
		
		g.setColor(TimelineEditor.TICK);
		int frame = start / fw - 1;
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			if (frame % 6 == 0) {
//				g.setColor(TimelineEditor.TICK);
				g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
//				g.setColor(TimelineEditor.HIGHLIGHT);
//				g.drawLine(x + start + 1, clip.y, x + start + 1, clip.y + clip.height);
			} else {
//				g.drawLine(x + start, y + 3, x + start, y + iExpandedHeight - 3);
			}
			frame++;
		}
		
		y = 0;
		for (Track track : timelineEditor.getTracks()) {
			if (track.isHidden())
				continue;
			if (y + track.getHeight() > clip.y && y < clip.y + clip.height)
				track.paint(g, y, selectedKey);
			y += track.getHeight();
		}
//		if (timelineEditor.getTracks().size() > 0 && timelineEditor.getTracks().get(timelineEditor.getTracks().size() - 1).isExpanded())
//		y -= 1;
//		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
//		g.drawLine(clip.x, y - 1, clip.x + clip.width - 1, y - 1);
//		g.setColor(UIManager.getColor("ScrollBar.shadow"));
//		g.drawLine(clip.x, y, clip.x + clip.width - 1, y);
		//timelineEditor.getHeight();
//		if (timelineEditor.getViewport().getHeight() > clip.height)
//		g.setClip(clip.x, clip.y, clip.width, timelineEditor.getViewport().getHeight());
//		g.fillRect(clip.x, y, clip.x + clip.width - 1, timelineEditor.getHeight() - y);
		
		int x = timelineEditor.getCurrentFrame() * fw + fw / 2;
		g.setColor(Color.BLACK);
		g.drawLine(x, clip.y, x, clip.y + clip.height - 1);
		g.fillPolygon(new int[] { x - 6, x + 6, x }, new int[] { getHeight() - 0, getHeight() - 0, getHeight() - 7}, 3);
		
		g.setColor(Color.BLACK);
		g.drawLine(clip.x, y + 6, clip.x + clip.width - 1, y + 6);
//		
//		g.setColor(Color.BLACK);
//		g.drawLine(x + 1, clip.y, x + 1, clip.height);
//		System.out.println((System.currentTimeMillis() - t) + " ms");
	}
	
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(600, 200);
	}
	
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		switch (orientation) {
		case SwingConstants.HORIZONTAL:
			int x = visibleRect.x % timelineEditor.getFrameWidth();
			if (direction > 0)
				return timelineEditor.getFrameWidth() - x;
			else
				return x == 0 ? timelineEditor.getFrameWidth() : x;
		case SwingConstants.VERTICAL:
			int y= visibleRect.y % 16;
			if (direction > 0)
				return 16 - y;
			else
				return y == 0 ? 16 : y;
		}
		throw new IllegalArgumentException();
	}
	
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		switch (orientation) {
		case SwingConstants.HORIZONTAL:
			return getScrollableUnitIncrement(visibleRect, orientation, direction) + timelineEditor.getFrameWidth() * 9;
		case SwingConstants.VERTICAL:
			return getScrollableUnitIncrement(visibleRect, orientation, direction) + 16 * 4;
		}
		throw new IllegalArgumentException();
	}
	
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}
	
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
	public void mousePressed(MouseEvent e) {
		int my = e.getY();
		int mx = e.getX();
		if (e.getButton() == MouseEvent.BUTTON1) {
			int y = 0;
			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
				Track track = timelineEditor.getTracks().get(i);
				if (track.isHidden())
					continue;
//				if (e.getClickCount() == 2 && e.getY() > y && e.getY() < y + track.getHeight() - 6) {
//				timelineEditor.expandTrack(track, !track.isExpanded());
//				return;
				if (track.isExpanded()) {
					if (my > y && my <= y + track.getHeight()) {
						// check if resize-bar was hit
						if (e.getY() > y + track.getHeight() - 6) {
							if (e.getClickCount() == 2) {
								timelineEditor.setTrackHeight(i, Track.EXPANDED_HEIGHT);
								timelineEditor.revalidate();
								revalidate();
								((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
								timelineEditor.repaint();
							} else {
								iVerticalResize = i;
								state = State.RESIZE;
							}
						} else {
							// no - so the track area was hit - let's see if a key was hit...
							
							selectedKey = track.getKeyAt(mx, my - y);
							if (selectedKey != null) {
								state = State.MOVE_KEY;
								selectedTrack = track;
								trackTop = y;
							}
							repaint();
						}
					}
				} else {
					if (my > y && my <= y + track.getHeight()) {
						selectedKey = track.getKeyAt(mx, my - y);
						if (selectedKey != null) {
							state = State.MOVE_KEY;
							selectedTrack = track;
						}
						repaint();
					}
				}
				y += track.getHeight();
			}
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			mx = e.getX();
			my = e.getY();
			state = State.SCROLL;
//			System.out.println(mx);
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			int y = 0;
			for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
				Track track = timelineEditor.getTracks().get(i);
				if (track.isHidden())
					continue;
				int h = track.isExpanded() ? track.getHeight() - 6 : track.getHeight();
				if (e.getY() > y && e.getY() <= y + h)
					showPopup(e, track);
				y += track.getHeight();
			}
		}
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseReleased(MouseEvent e) {
		state = State.IDLE;
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
		if (iVerticalResize < 0)
			timelineEditor.setCursor(TimelineEditor.defaultCursor);
	}
	
	public void mouseDragged(MouseEvent e) {
//		System.out.println(e.getButton());
		switch (state) {
		
		case RESIZE:
			int y = 0;
			for (int i = 0; i < iVerticalResize; i++)
				y += timelineEditor.getTracks().get(i).getHeight();
			int h = e.getY() - y + 3;
			if (h < 32)
				h = 32;
			if (h > 256)
				h = 256;
			timelineEditor.setTrackHeight(iVerticalResize, h);
			timelineEditor.revalidate();
			revalidate();
			((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
			timelineEditor.repaint();
			break;
		case SCROLL:
//			int x = timelineEditor.getHorizontalScrollBar().getValue();
//			int y = timelineEditor.getVerticalScrollBar().getValue();
			JScrollBar hsb = timelineEditor.getHorizontalScrollBar();
			JScrollBar vsb = timelineEditor.getVerticalScrollBar();
			int sbx = hsb.getValue();
			int sby = vsb.getValue();
			int dx = e.getX() - mx;
			int dy = e.getY() - my;
			mx = (sbx == 0 || sbx == hsb.getMaximum() - hsb.getVisibleAmount()) ? e.getX() : e.getX() - dx;
			my = (sby == 0 || sby == vsb.getMaximum() - vsb.getVisibleAmount()) ? e.getY() : e.getY() - dy;
//			System.out.println("y=" + e.getY() + "\tmy=" + my + " \tdy=" + dy + " \tsby=" + sby + " max=" + (vsb.getMaximum() - vsb.getVisibleAmount()));
			timelineEditor.getHorizontalScrollBar().setValue(sbx - dx);
			timelineEditor.getVerticalScrollBar().setValue(sby - dy);
//			dx = e.getX() - sbx;
//			dy = e.getY() - sby;
//			revalidate();
//			timelineEditor.revalidate();
//			((JComponent) timelineEditor.getRowHeader().getView()).revalidate();
			break;
		case MOVE_KEY:
			int frame = e.getX() / timelineEditor.getFrameWidth();
			if (selectedTrack.isExpanded())
				selectedTrack.moveKey(selectedKey, e.getY() - trackTop);
			selectedTrack.shiftKey(selectedKey, frame);
			repaint();
			timelineEditor.setCornerText("Frame " + frame);
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		boolean vResize = false;
		int y = 0;
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
			if (track.isExpanded() && e.getY() > y + track.getHeight() - 6 && e.getY() <= y + track.getHeight()) {
				vResize = true;
				break;
			}
			y += track.getHeight();
		}
		if (vResize)
			timelineEditor.setCursor(TimelineEditor.verticalResizeCursor);
		else 
			timelineEditor.setCursor(TimelineEditor.defaultCursor);
		int frame = e.getX() / timelineEditor.getFrameWidth();
		timelineEditor.setCornerText("Frame " + frame);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
//		timelineEditor.setCurrentFrame(timelineEditor.getCurrentFrame() + e.getWheelRotation());
		Animation anim = MainFrame.getInstance().getAnimation();
		anim.setPosition(anim.getPosition() + e.getWheelRotation());
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
	
	private void showPopup(MouseEvent e, final Track track) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem mi;
		
		final int frame = e.getX() / timelineEditor.getFrameWidth();
//		popup.add(new JLabel(track.getName()));
		
		/*
		 * insert key (on this track)
		 */
		mi = new JMenuItem("insert key (on this track)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JPatchUndoableEdit edit = track.insertKeyAt(frame);
				if (edit == null)
					return;
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				addKeysEdit.addEdit(edit);
				MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
				TrackView.this.repaint();
			}
		});
		popup.add(mi);
		
		popup.add(new JSeparator());
		
		/*
		 * insert key (on selected tracks)
		 */
		mi = new JMenuItem("insert keys (on selected tracks)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				for (Track track : timelineEditor.getHeader().getSelectedTracks()) {
					JPatchUndoableEdit edit = track.insertKeyAt(frame);
					if (edit != null)
						addKeysEdit.addEdit(edit);
				}
				if (addKeysEdit.isValid()) {
					MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
					TrackView.this.repaint();
				}
			}
		});
		popup.add(mi);
		
		/*
		 * insert key (on all not hidden tracks)
		 */
		mi = new JMenuItem("insert keys (on all not hidden tracks)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				for (Track track : timelineEditor.getTracks()) {
					if (track.isHidden())
						continue;
					JPatchUndoableEdit edit = track.insertKeyAt(frame);
					if (edit != null)
						addKeysEdit.addEdit(edit);
				}
				if (addKeysEdit.isValid()) {
					MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
					TrackView.this.repaint();
				}
			}
		});
		popup.add(mi);
		
//		/*
//		 * insert key (on all tracks)
//		 */
//		mi = new JMenuItem("insert key (on all tracks)");
//		mi.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
//				for (Track track : timelineEditor.getTracks()) {
//					JPatchUndoableEdit edit = track.insertKeyAt(frame);
//					if (edit != null)
//						addKeysEdit.addEdit(edit);
//				}
//				if (addKeysEdit.isValid()) {
//					MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
//					TrackView.this.repaint();
//				}
//			}
//		});
//		popup.add(mi);
		
		popup.add(new JSeparator());
		
		popup.show((Component) e.getSource(), e.getX(), e.getY());
	}
}