/*
 * $Id: TrackView.java,v 1.23 2006/05/06 09:52:15 sascha_l Exp $
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
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;

@SuppressWarnings("serial")
class TrackView extends JComponent implements Scrollable, MouseListener, MouseMotionListener, MouseWheelListener {
	/**
	 * 
	 */
	private static enum State { IDLE, RESIZE, SCROLL, MOVE_KEY, LASSO };
	private State state = State.IDLE;
	private final TimelineEditor timelineEditor;
	private Dimension dim = new Dimension();
	private int iVerticalResize = -1;
	private int mx, my, trackTop;
	private int cornerX, cornerY, deltaX, deltaY;
	
	private MotionKey selectedKey;
	private Track selectedTrack;
	private float position, value;
	
	public TrackView(TimelineEditor tle) {
		timelineEditor = tle;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	public Dimension getPreferredSize() {
		Animation anim = MainFrame.getInstance().getAnimation();
		dim.setSize(timelineEditor.getFrameWidth() * (anim.getEnd() - anim.getStart()), timelineEditor.getTracksHeight() + 7);
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
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		int minor;
		if (MainFrame.getInstance().getAnimation().getFramerate() == 24) {
			minor = 6;
		} else {
			minor = 5;
		}
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			if (frame % minor == 0) {
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
		
		int x = (timelineEditor.getCurrentFrame() - (int) MainFrame.getInstance().getAnimation().getStart()) * fw + fw / 2;
		g.setColor(Color.BLACK);
		g.drawLine(x, clip.y, x, clip.y + clip.height - 1);
//		g.fillPolygon(new int[] { x - 6, x + 6, x }, new int[] { getHeight() - 0, getHeight() - 0, getHeight() - 7}, 3);
		
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
		my = e.getY();
		mx = e.getX();
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
								position = selectedKey.getPosition();
								MainFrame.getInstance().getAnimation().setPosition(position);
								MainFrame.getInstance().getJPatchScreen().update_all();
								timelineEditor.setCurrentFrame((int) position);
								if (selectedKey instanceof MotionKey.Float)
									value = ((MotionKey.Float) selectedKey).getFloat();
//								System.out.println("key selected: " + selectedKey + " position=" + position);
							} else {
								/* enter lasso select mode */
								state = State.LASSO;
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
							position = selectedKey.getPosition();
//							MainFrame.getInstance().getAnimation().setPosition(position);
//							timelineEditor.setCurrentFrame((int) position);
						} else {
							/* enter lasso select mode */
							state = State.LASSO;
						}
						repaint();
					}
				}
				y += track.getHeight();
			}
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			this.mx = e.getX();
			this.my = e.getY();
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
		switch (state) {
		case MOVE_KEY:
			JPatchActionEdit edit = new JPatchActionEdit("move key");
			System.out.println("moving key " + selectedKey + " position=" + position);
			float newPosition = selectedKey.getPosition();
			selectedKey.setPosition(position);
			if (newPosition != position)
				edit.addEdit(new AtomicMoveMotionKey(selectedTrack.getMotionCurve(selectedKey), selectedKey, newPosition));
			if (selectedKey instanceof MotionKey.Float) {
				MotionKey.Float key = (MotionKey.Float) selectedKey;
				float newValue = key.getFloat();
				key.setFloat(value);
				if (newValue != value)
//					edit.addEdit(new AtomicModifyMotionCurve.Float((MotionCurve.Float) selectedTrack.getMotionCurve(selectedKey), newPosition, newValue));
//					edit.addEdit(new AtomicMoveMotionKey(selectedTrack.getMotionCurve(selectedKey), selectedKey, newPosition));
					edit.addEdit(new AtomicChangeMotionKeyValue(key, newValue));
			}
			if (edit.isValid())
				MainFrame.getInstance().getUndoManager().addEdit(edit);
			break;
		case LASSO:
			int y = 0;
			Set<Track> selectedTracks = timelineEditor.getHeader().getSelectedTracks();
			for (Track track : timelineEditor.getTracks()) {
				if (track.isHidden())
					continue;
				int top = y;
				int bottom = y + track.getHeight();
				
				
				y = bottom;
			}
			
			repaint();
			cornerX = cornerY = -1;
			deltaX = deltaY = 0;
			break;
		}
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
			int frame = e.getX() / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
			
			if (selectedTrack.isExpanded()) {
				selectedTrack.moveKey(selectedKey, e.getY() - trackTop);
			}
			if (selectedTrack.getMotionCurve(selectedKey) != null && !selectedTrack.getMotionCurve(selectedKey).hasKeyAt(frame))
				selectedTrack.shiftKey(selectedKey, frame);
			repaint();
//			timelineEditor.setCornerText("Frame " + frame);
//			if (timelineEditor.getAnimObject() instanceof AnimModel) {
//			MainFrame.getInstance().getAnimation().getCurvesetFor(timelineEditor.getAnimObject()).setPosition(position);
			MainFrame.getInstance().getAnimation().setPosition(frame);
			timelineEditor.setCurrentFrame(frame);
			MainFrame.getInstance().getJPatchScreen().update_all();
			break;
		case LASSO:
			Graphics2D g2 = (Graphics2D) ((Component) e.getSource()).getGraphics();
			g2.setXORMode(new Color(((Component) e.getSource()).getBackground().getRGB() ^ 0x00000000));
			g2.setStroke(new BasicStroke(2.0f,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_BEVEL,0.0f,new float[] { 5.0f, 5.0f }, 0.0f));
			int eventX = e.getX();
			int eventY = e.getY();
			drawSelectionRectangle(g2, cornerX, cornerY, deltaX, deltaY);
			deltaX = Math.abs(mx - eventX);
			deltaY = Math.abs(my - eventY);
			cornerX = (mx < eventX) ? mx : eventX;
			cornerY = (my < eventY) ? my : eventY;
			y = 0;
			Set<Track> selectedTracks = timelineEditor.getHeader().getSelectedTracks();
			for (Track track : timelineEditor.getTracks()) {
				if (track.isHidden())
					continue;
				int top = y;
				int bottom = y + track.getHeight();
				if (cornerY > top && cornerY < bottom) {
					deltaY += (cornerY - top);
					cornerY = top;
				} if ((cornerY + deltaY) > top && (cornerY + deltaY) < bottom)
					deltaY = bottom - cornerY;
				y = bottom;
			}
			if ((cornerY + deltaY) >= y)
				deltaY = y - cornerY;
			if (cornerY < 0) {
				deltaY += cornerY;
				cornerY = 0;
			}
			int frameA = cornerX / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
			int frameB = (cornerX + deltaX) / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
			cornerX = (frameA - (int) MainFrame.getInstance().getAnimation().getStart()) * timelineEditor.getFrameWidth();
			deltaX = (frameB - (int) MainFrame.getInstance().getAnimation().getStart()) * timelineEditor.getFrameWidth() - cornerX;
			drawSelectionRectangle(g2, cornerX, cornerY, deltaX, deltaY);
			break;
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
		int frame = e.getX() / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
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
		
		boolean selected = timelineEditor.getHeader().getSelectedTracks().size() == 1;
		boolean multiSelection = timelineEditor.getHeader().getSelectedTracks().size() > 1;
		String selectedTrackName = null;
		if (selected && !multiSelection)
			for (Track t : timelineEditor.getHeader().getSelectedTracks())
				selectedTrackName = t.getName();
		boolean hidden = false;
		for (Track t : timelineEditor.getTracks()) {
			if (t.isHidden()) {
				hidden = true;
				break;
			}
		}
		
		final int frame = e.getX() / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
//		popup.add(new JLabel(track.getName()));
		
		/*
		 * go to this frame
		 */
		mi = new JMenuItem("go to this frame (" + frame + ")");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				timelineEditor.setCurrentFrame(frame);
				MainFrame.getInstance().getAnimation().setPosition(frame);
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		popup.add(mi);
		mi.setEnabled(frame != MainFrame.getInstance().getAnimation().getPosition());
		
		popup.add(new JSeparator());
		
		/*
		 * quick insert (insert key on this track)
		 */
		if (!(track instanceof HeaderTrack)) {
			mi = new JMenuItem("quick insert key");
		} else {
			mi = new JMenuItem("quick insert key");
			mi.setEnabled(false);
		}
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JPatchUndoableEdit edit = track.insertKeyAt(frame);
				if (edit == null)
					return;
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				addKeysEdit.addEdit(edit);
				MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
				TrackView.this.repaint();
				MainFrame.getInstance().getAnimation().rethink();
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		popup.add(mi);
		
		JMenu menu = new JMenu("Insert key on this frame (" + frame + ")");
		popup.add(menu);
		/*
		 * insert key (on this track)
		 */
		if (!(track instanceof HeaderTrack)) {
			mi = new JMenuItem("on this track (" + track.getName() + ")");
		} else {
			mi = new JMenuItem("on this track");
			mi.setEnabled(false);
		}
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JPatchUndoableEdit edit = track.insertKeyAt(frame);
				if (edit == null)
					return;
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				addKeysEdit.addEdit(edit);
				MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
				TrackView.this.repaint();
				MainFrame.getInstance().getAnimation().rethink();
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		menu.add(mi);
		
		/*
		 * insert key (on selected tracks)
		 */
		if (selected) {
			mi = new JMenuItem("on selected track (" + selectedTrackName + ")");
		} else {
			mi = new JMenuItem("on selected tracks");
			if (!multiSelection)
				mi.setEnabled(false);
		}
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
					MainFrame.getInstance().getAnimation().rethink();
					MainFrame.getInstance().getJPatchScreen().update_all();
				}
			}
		});
		menu.add(mi);
		
		/*
		 * insert key (on all not hidden tracks)
		 */
		if (hidden)
			mi = new JMenuItem("on all not hidden tracks");
		else
			mi = new JMenuItem("on all tracks");
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
					MainFrame.getInstance().getAnimation().rethink();
					MainFrame.getInstance().getJPatchScreen().update_all();
				}
			}
		});
		menu.add(mi);
		
		final int currentframe = timelineEditor.getCurrentFrame();
		
		menu = new JMenu("Insert key on current frame (" + currentframe + ")");
		popup.add(menu);
		/*
		 * insert key (on this track)
		 */
		if (!(track instanceof HeaderTrack)) {
			mi = new JMenuItem("on this track (" + track.getName() + ")");
		} else {
			mi = new JMenuItem("on this track");
			mi.setEnabled(false);
		}
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JPatchUndoableEdit edit = track.insertKeyAt(currentframe);
				if (edit == null)
					return;
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				addKeysEdit.addEdit(edit);
				MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
				TrackView.this.repaint();
				MainFrame.getInstance().getAnimation().rethink();
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		menu.add(mi);
		
		/*
		 * insert key (on selected tracks)
		 */
		if (selected) {
			mi = new JMenuItem("on selected track (" + selectedTrackName + ")");
		} else {
			mi = new JMenuItem("on selected tracks");
			if (!multiSelection)
				mi.setEnabled(false);
		}
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				for (Track track : timelineEditor.getHeader().getSelectedTracks()) {
					JPatchUndoableEdit edit = track.insertKeyAt(currentframe);
					if (edit != null)
						addKeysEdit.addEdit(edit);
				}
				if (addKeysEdit.isValid()) {
					MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
					TrackView.this.repaint();
					MainFrame.getInstance().getAnimation().rethink();
					MainFrame.getInstance().getJPatchScreen().update_all();
				}
			}
		});
		menu.add(mi);
		
		/*
		 * insert key (on all not hidden tracks)
		 */
		if (hidden)
			mi = new JMenuItem("on all not hidden tracks");
		else
			mi = new JMenuItem("on all tracks");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				CompoundAddKeys addKeysEdit = new CompoundAddKeys();
				for (Track track : timelineEditor.getTracks()) {
					if (track.isHidden())
						continue;
					JPatchUndoableEdit edit = track.insertKeyAt(currentframe);
					if (edit != null)
						addKeysEdit.addEdit(edit);
				}
				if (addKeysEdit.isValid()) {
					MainFrame.getInstance().getUndoManager().addEdit(addKeysEdit);
					TrackView.this.repaint();
					MainFrame.getInstance().getAnimation().rethink();
					MainFrame.getInstance().getJPatchScreen().update_all();
				}
			}
		});
		menu.add(mi);
		
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
		
		/*
		 * delete key
		 */
		mi = new JMenuItem("delete selected key");
		final MotionCurve motionCurve = selectedTrack == null ? null : selectedTrack.getMotionCurve(selectedKey);
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JPatchRootEdit edit = new AtomicDeleteMotionKey(motionCurve, selectedKey);
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				selectedKey = null;
				selectedTrack = null;
				TrackView.this.repaint();
				MainFrame.getInstance().getAnimation().rethink();
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		mi.setEnabled(selectedKey != null && motionCurve != null && motionCurve.getKeyCount() > 1);
		popup.add(mi);
		
		popup.show((Component) e.getSource(), e.getX(), e.getY());
	}
	
	private static void drawSelectionRectangle(Graphics g, int iCornerX, int iCornerY, int iDeltaX, int iDeltaY) {
		g.drawLine(iCornerX,iCornerY,iCornerX + iDeltaX,iCornerY);
		g.drawLine(iCornerX + iDeltaX,iCornerY,iCornerX + iDeltaX,iCornerY + iDeltaY);
		g.drawLine(iCornerX,iCornerY + iDeltaY,iCornerX + iDeltaX,iCornerY + iDeltaY);
		g.drawLine(iCornerX,iCornerY,iCornerX,iCornerY + iDeltaY);
	}
}