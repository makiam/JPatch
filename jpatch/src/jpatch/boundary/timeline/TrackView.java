/*
 * $Id: TrackView.java,v 1.25 2006/05/10 11:31:59 sascha_l Exp $
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
import java.util.*;
import java.util.List;

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
	private int mx, my;
//	private int cornerX, cornerY, deltaX, deltaY;
//	private int frame, delta, frameA, frameB;
//	private int trackA, trackB;
	private float value;
	private int trackTop;
	private int frame, delta, firstFrame, lastFrame;
	private Track hitTrack;
	private MotionKey[] hitKeys;
	private Map<MotionKey, KeyData> selection = new HashMap<MotionKey, KeyData>();
	private Range range;
	private Rectangle rect;
	private boolean moveSelection;
	
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
		for (int i = 0, n = timelineEditor.getTracks().size(); i < n; i++) {
			Track track = timelineEditor.getTracks().get(i);
			if (track.isHidden()) 
				continue;
			if (y + track.getHeight() > clip.y && y < clip.y + clip.height)
				if (selectedTracks.contains(track))
					g.fillRect(clip.x, y, clip.width, track.getHeight());
			if (range != null) {
				if (i == range.firstTrack)
					rect.y = y;
				if (i == range.lastTrack)
					rect.height = y + track.getHeight() - rect.y;
			}
			y += track.getHeight();
		}
		
		if (range != null) {
			rect.x = (range.firstFrame - (int) MainFrame.getInstance().getAnimation().getStart()) * fw;
			rect.width = (range.lastFrame - range.firstFrame + 1) * fw;
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
				track.paint(g, y, selection, hitKeys);
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
		if (state != State.LASSO && range != null && rect != null) {
			g.setColor(Color.BLACK);
			g.drawRect(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2);
			g.drawRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
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
		frame = mx / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
		int trackNumber = -1;
		int y = 0;
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
			if (track.isHidden())
				continue;
			if (my > y && my < y + track.getHeight()) {
				trackNumber = i;
				break;
			}
			y += track.getHeight();
		}
		
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (range != null && trackNumber >= range.firstTrack && trackNumber <=range.lastTrack && frame >= range.firstFrame && frame <= range.lastFrame) {
				moveSelection = true;
				firstFrame = range.firstFrame;
				lastFrame = range.lastFrame;
			} else {
				moveSelection = false;
				selection.clear();
				range = null;
				rect = null;
				repaint();
			}
			y = 0;
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
							
							hitKeys = track.getKeysAt(mx, my - y);
							if (hitKeys != null) {
								hitTrack = track;
								trackTop = y;
								state = State.MOVE_KEY;
								MainFrame.getInstance().getAnimation().setPosition(frame);
								MainFrame.getInstance().getJPatchScreen().update_all();
								timelineEditor.setCurrentFrame((int) frame);
								if (hitKeys[0] instanceof MotionKey.Float)
									value = ((MotionKey.Float) hitKeys[0]).getFloat();
//								System.out.println("key selected: " + selectedKey + " position=" + position);
							} else {
								/* enter lasso select mode */
								state = State.LASSO;
								range = new Range();
								rect = new Rectangle(mx, my, 0, 0);
							}
							repaint();
						}
					}
				} else {
					if (my > y && my <= y + track.getHeight()) {
						hitKeys =  track.getKeysAt(mx, my - y);
						if (hitKeys != null) {
							hitTrack = track;
							state = State.MOVE_KEY;
//							position = getSelectedKey().getPosition();
//							MainFrame.getInstance().getAnimation().setPosition(position);
//							timelineEditor.setCurrentFrame((int) position);
						} else {
							if (moveSelection) {
								state = State.MOVE_KEY;
								hitTrack = null;
								hitKeys = null;
							} else {
								/* enter lasso select mode */
								state = State.LASSO;
								range = new Range();
								rect = new Rectangle(mx, my, 0, 0);
							}
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
			y = 0;
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
	
//	public MotionKey getSelectedKey() {
//		for (MotionKey key : selection.keySet())
//			return key;
//		return null;
//	}
//	
//	public Track getSelectedTrack() {
//		return selection.get(getSelectedKey());
//	}
	
	public void mouseReleased(MouseEvent e) {
		switch (state) {
		case MOVE_KEY:
			if (delta != 0) {
				JPatchActionEdit edit = new JPatchActionEdit("move key");
	//			System.out.println("moving key " + selectedKey + " position=" + position);
	//			float newPosition = getSelectedKey().getPosition();
				for (MotionKey selectedKey : hitKeys) {
					selectedKey.setPosition((int) selectedKey.getPosition() - delta);
					edit.addEdit(new AtomicMoveMotionKey(hitTrack.getMotionCurve(selectedKey), selectedKey, (int) selectedKey.getPosition() + delta));
					if (selectedKey instanceof MotionKey.Float) {
						MotionKey.Float key = (MotionKey.Float) selectedKey;
						float newValue = key.getFloat();
						key.setFloat(value);
						if (newValue != value)
		//					edit.addEdit(new AtomicModifyMotionCurve.Float((MotionCurve.Float) selectedTrack.getMotionCurve(selectedKey), newPosition, newValue));
		//					edit.addEdit(new AtomicMoveMotionKey(selectedTrack.getMotionCurve(selectedKey), selectedKey, newPosition));
							edit.addEdit(new AtomicChangeMotionKeyValue(key, newValue));
					}
				}
				if (edit.isValid())
					MainFrame.getInstance().getUndoManager().addEdit(edit);
			}
			break;
		case LASSO:
			selection.clear();
			Range newRange = new Range();
			boolean selectionValid = false;
			for (int i = range.firstTrack; i <= range.lastTrack;i++) {
				Track track = timelineEditor.getTracks().get(i);
				for (MotionCurve motionCurve : track.getMotionCurves()) {
					int startIndex = motionCurve.getIndexAt(range.firstFrame);
					int endIndex = motionCurve.getIndexAt(range.lastFrame);
					if (startIndex != endIndex){
						if (i < newRange.firstTrack)
							newRange.firstTrack = i;
						if (i > newRange.lastTrack)
							newRange.lastTrack = i;
					}
					System.out.println("index " + startIndex + " to " + endIndex);
					for (int j = startIndex; j < endIndex; j++) {
						MotionKey key = motionCurve.getKey(j);
						if (key instanceof MotionKey.Float)
							selection.put(key, new KeyData(track, key.getPosition(), ((MotionKey.Float) key).getFloat()));
						else
							selection.put(key, new KeyData(track, key.getPosition()));
						int p = (int) key.getPosition();
						if (p < newRange.firstFrame)
							newRange.firstFrame = p;
						if (p > newRange.lastFrame)
							newRange.lastFrame = p;
						selectionValid = true;
					}
				}
			}
			if (selectionValid)
				range = newRange;
			else {
				range = null;
				rect = null;
			}
			repaint();
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
		System.out.println("frame " + frame + " delta " + delta + " state " + state);
		System.out.println(Arrays.toString(hitKeys));
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
			delta = e.getX() / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart() - frame;
			
//			if (selectedTrack.isExpanded()) {
//				selectedTrack.moveKey(selectedKey, e.getY() - trackTop);
//			}
//			if (selectedTrack.getMotionCurve(selectedKey) != null && !selectedTrack.getMotionCurve(selectedKey).hasKeyAt(frame))
//				selectedTrack.shiftKey(selectedKey, frame);
			if (moveSelection) {
				for (MotionKey key: selection.keySet()) {
					KeyData keyData = selection.get(key);
					keyData.track.shiftKey(key, (int) keyData.position + delta);
				}
				range.firstFrame = firstFrame + delta;
				range.lastFrame = lastFrame + delta;
			} else {
				for (MotionKey key : hitKeys) {
					if (hitTrack.isExpanded())
						hitTrack.moveKey(key, e.getY() - trackTop);
					hitTrack.shiftKey(key, frame + delta);
				}
			}
//				if (!(object instanceof MotionKey))
//				return false;
//			return fPosition == ((MotionKey) object).fPosition;
//		}public boolean equals(Object object) {
//				if (!(object instanceof MotionKey))
//				return false;
//			return fPosition == ((MotionKey) object).fPosition;
//		}
//		
//		public int compareTo(Object object) {
//			MotionKey other = (MotionKey) object;
//			return java.lang.Float.compare(fPosition, other.fPosition);
//		}
//		
//		public int compareTo(Object object) {
//			MotionKey other = (MotionKey) object;
//			return java.lang.Float.compare(fPosition, other.fPosition);
//		}
			repaint();
//			timelineEditor.setCornerText("Frame " + frame);
//			if (timelineEditor.getAnimObject() instanceof AnimModel) {
//			MainFrame.getInstance().getAnimation().getCurvesetFor(timelineEditor.getAnimObject()).setPosition(position);
			MainFrame.getInstance().getAnimation().setPosition(frame + delta);
			timelineEditor.setCurrentFrame(frame + delta);
			MainFrame.getInstance().getJPatchScreen().update_all();
			break;
		case LASSO:
			Graphics2D g2 = (Graphics2D) ((Component) e.getSource()).getGraphics();
			g2.setXORMode(new Color(((Component) e.getSource()).getBackground().getRGB() ^ 0x00000000));
			g2.setStroke(new BasicStroke(2.0f,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_BEVEL,0.0f,new float[] { 4.0f, 4.0f }, 0.0f));
			int eventX = e.getX();
			int eventY = e.getY();
			drawSelectionRectangle(g2);
			rect.width = Math.abs(mx - eventX);
			rect.height = Math.abs(my - eventY);
			rect.x = (mx < eventX) ? mx : eventX;
			rect.y = (my < eventY) ? my : eventY;
			y = 0;
			List<Track> list = timelineEditor.getTracks();
			for (int i = 0, n = list.size(); i < n; i++) {
				Track track = list.get(i);
				if (track.isHidden())
					continue;
				int top = y;
				int bottom = y + track.getHeight();
				if (rect.y > top && rect.y < bottom) {
					rect.height += (rect.y - top);
					rect.y = top;
					range.firstTrack = i;
				} if ((rect.y + rect.height) > top && (rect.y + rect.height) < bottom) {
					rect.height = bottom - rect.y;
					range.lastTrack = i;
				}
				y = bottom;
			}
			if ((rect.y + rect.height) >= y)
				rect.height = y - rect.y;
			if (rect.y < 0) {
				rect.height += rect.y;
				rect.y = 0;
			}
			range.firstFrame = rect.x / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
			range.lastFrame = (rect.x + rect.width) / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
			rect.x = (range.firstFrame - (int) MainFrame.getInstance().getAnimation().getStart()) * timelineEditor.getFrameWidth();
			rect.width = (range.lastFrame - (int) MainFrame.getInstance().getAnimation().getStart()) * timelineEditor.getFrameWidth() - rect.x;
			drawSelectionRectangle(g2);
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
//		final MotionCurve motionCurve = selectedTrack == null ? null : selectedTrack.getMotionCurve(selectedKey);
//		mi.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				JPatchRootEdit edit = new AtomicDeleteMotionKey(motionCurve, selectedKey);
//				MainFrame.getInstance().getUndoManager().addEdit(edit);
//				selectedKey = null;
//				selectedTrack = null;
//				TrackView.this.repaint();
//				MainFrame.getInstance().getAnimation().rethink();
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			}
//		});
//		mi.setEnabled(selectedKey != null && motionCurve != null && motionCurve.getKeyCount() > 1);
		popup.add(mi);
		
		popup.show((Component) e.getSource(), e.getX(), e.getY());
	}
	
	private void drawSelectionRectangle(Graphics g) {
		g.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);
		g.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
		g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
		g.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
	}
	
	static class KeyData {
		Track track;
		float position;
		float value;
		
		KeyData(Track track, float position, float value) {
			this.track = track;
			this.position = position;
			this.value = value;
		}
		
		KeyData(Track track, float position) {
			this(track, position, 0);
		}
	}
}