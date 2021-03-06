/*
 * $Id$
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
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import jpatch.control.edit.*;
import jpatch.entity.*;
import jpatch.entity.MotionCurve.Float;
import jpatch.boundary.*;
import jpatch.boundary.settings.Settings;

@SuppressWarnings("serial")
class TrackView extends JComponent implements Scrollable, MouseListener, MouseMotionListener, MouseWheelListener {
	/**
	 * 
	 */
	private static enum State { IDLE, RESIZE, SCROLL, MOVE_KEY, LASSO, RETIME_LEFT, RETIME_RIGHT, TANGENT };
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
	private final Map<MotionKey, KeyData> selection = new HashMap<MotionKey, KeyData>();
	private Range range;
	private Rectangle rect;
	private Range retimeRange;
	private boolean moveSelection;
//	private boolean[] trackHasKeys;
	private Set<MotionCurve> curveWithKeys = new HashSet<MotionCurve>();
	private Map<MotionKey, MotionCurve> suspendedKeys = new HashMap<MotionKey, MotionCurve>();
	private Map<MotionKey, MotionCurve> clipboard = new HashMap<MotionKey, MotionCurve>();
	private Range clipRange;
	private Set<MotionCurve> clipCurveWithKeys = new HashSet<MotionCurve>();
//	private boolean[] clipTrackHasKeys;
	private TangentHandle tangentHandle;
	private JPatchActionEdit edit;
	
	private Action deleteAction = new AbstractAction() {
		public void actionPerformed(ActionEvent event) {
			delete("delete keys");
			TrackView.this.repaint();
			MainFrame.getInstance().getAnimation().rethink();
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	};
	
	private Action cutAction = new AbstractAction() {
		public void actionPerformed(ActionEvent event) {
			copy();
			delete("cut");
			TrackView.this.repaint();
			MainFrame.getInstance().getAnimation().rethink();
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	};
	
	private Action copyAction = new AbstractAction() {
		public void actionPerformed(ActionEvent event) {
			copy();
		}
	};
	
	private Action pasteAction = new AbstractAction() {
		public void actionPerformed(ActionEvent event) {
			paste(timelineEditor.getCurrentFrame());
			TrackView.this.repaint();
			MainFrame.getInstance().getAnimation().rethink();
			MainFrame.getInstance().getJPatchScreen().update_all();
		}
	};
	
	public TrackView(TimelineEditor tle) {
		timelineEditor = tle;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "delete");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control X"), "cut");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control C"), "copy");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control V"), "paste");
		getActionMap().put("delete", deleteAction);
		getActionMap().put("cut", cutAction);
		getActionMap().put("copy", copyAction);
		getActionMap().put("paste", pasteAction);
	}
	
	public void reset() {
		clipboard.clear();
	}
	
	public Dimension getPreferredSize() {
		Animation anim = MainFrame.getInstance().getAnimation();
		int x = (int) (timelineEditor.getFrameWidth() * (anim.getEnd() - anim.getStart()));
		int y = timelineEditor.getTracksHeight() + 7;
//		y = (int) timelineEditor.getViewport().getViewRect().getHeight();
		dim.setSize(x, y);
		return dim;
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	
	public void paintComponent(Graphics g) {
		paintMultiTrack(g);
	}
	
	private void paintCombinedTrack(Graphics g) {
		/*
		 * paint individual tracks
		 */
		Rectangle clip = g.getClipBounds();
		for (Track track : timelineEditor.getTracks()) {
			if (track.isHidden())
				continue;
			if (track instanceof AvarTrack) {
				AvarTrack avarTrack = (AvarTrack) track;
//				track.setExpandedHeight((int) timelineEditor.getViewport().getViewRect().getHeight());
				avarTrack.setOffset((int) timelineEditor.getViewport().getViewRect().getHeight() / 2);
				avarTrack.setScale(1);
				avarTrack.drawCurve(g, false, 0, Color.BLACK, Color.GRAY, (Float) track.getMotionCurves()[0], selection, hitKeys);
			}
		}
	}
	
	
	/*
	 * Paint multi-track view
	 */
	private void paintMultiTrack(Graphics g) {
		/*
		 * get clip bounds, calculate start frame, fill background
		 */
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		g.setColor(TimelineEditor.BACKGROUND);
		g.fillRect(clip.x, clip.y, clip.width, clip.height);
		
		/*
		 * loop over tracks, paint track background, set up selection rectangle
		 */
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
		
		/*
		 * paint ticks
		 */
		g.setColor(TimelineEditor.TICK);
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		int minor;
		if (MainFrame.getInstance().getAnimation().getFramerate() == 24) {
			minor = 6;
		} else {
			minor = 5;
		}
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			if (frame % minor == 0)
				g.drawLine(x + start, clip.y, x + start, clip.y + clip.height);
			frame++;
		}
		
		/*
		 * paint individual tracks
		 */
		y = 0;
		for (Track track : timelineEditor.getTracks()) {
			if (track.isHidden())
				continue;
			if (y + track.getHeight() > clip.y && y < clip.y + clip.height)
				track.paint(g, y, selection, hitKeys);
			y += track.getHeight();
		}
		
		/*
		 * draw frame marker and bottom
		 */
		int x = (timelineEditor.getCurrentFrame() - (int) MainFrame.getInstance().getAnimation().getStart()) * fw + fw / 2;
		g.setColor(Color.BLACK);
		g.drawLine(x, clip.y, x, clip.y + clip.height - 1);
		
		g.setColor(Color.BLACK);
		g.drawLine(clip.x, y + 6, clip.x + clip.width - 1, y + 6);

		/*
		 * draw selection rectangle
		 */
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
		requestFocusInWindow();
		my = e.getY();
		mx = e.getX();
		frame = mx / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
		int trackNumber = -1;
		int y = 0;
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
			if (track.isHidden())
				continue;
			if (my >= y && my < y + track.getHeight()) {
				trackNumber = i;
				break;
			}
			y += track.getHeight();
		}
		
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (range != null && range.lastFrame > range.firstFrame) {
				if (e.getX() < rect.x + 4 && e.getX() > rect.x - 4) {
					state = State.RETIME_LEFT;
					retimeRange = new Range(range);
					return;
				} else if (e.getX() < rect.x + rect.width + 4 && e.getX() > rect.x + rect.width - 4) {
					state = State.RETIME_RIGHT;
					retimeRange = new Range(range);
					return;
				}
			}
			
			if (range != null && trackNumber >= range.firstTrack && trackNumber <=range.lastTrack && frame >= range.firstFrame && frame <= range.lastFrame) {
				moveSelection = true;
				suspendedKeys.clear();
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
							if (track instanceof AvarTrack)
								tangentHandle = ((AvarTrack) track).getTangentHandleAt(mx, my - y);
							else
								tangentHandle = null;
							System.out.println("tangentHandle = " + tangentHandle);
							if (hitKeys != null) {
								hitTrack = track;
								trackTop = y;
								state = State.MOVE_KEY;
								delta = 0;
								MainFrame.getInstance().getAnimation().setPosition(frame);
								MainFrame.getInstance().getJPatchScreen().update_all();
								timelineEditor.setCurrentFrame((int) frame);
								if (hitKeys[0] instanceof MotionKey.Float)
									value = ((MotionKey.Float) hitKeys[0]).getFloat();
//								System.out.println("key selected: " + selectedKey + " position=" + position);
							} else if (tangentHandle != null) {
								state = State.TANGENT;
								hitTrack = track;
								trackTop = y;
								tangentHandle.prepare();
								edit = new JPatchActionEdit("modify tangent");
							} else {
								if (moveSelection) {
									state = State.MOVE_KEY;
									delta = 0;
									hitTrack = null;
									hitKeys = null;
								} else {
									/* enter lasso select mode */
									range = null;
									rect = new Rectangle(mx, my, 0, 0);
									state = State.LASSO;
								}
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
							delta = 0;
//							position = getSelectedKey().getPosition();
//							MainFrame.getInstance().getAnimation().setPosition(position);
//							timelineEditor.setCurrentFrame((int) position);
						} else {
							if (moveSelection) {
								state = State.MOVE_KEY;
								delta = 0;
								hitTrack = null;
								hitKeys = null;
							} else {
								/* enter lasso select mode */
								range = null;
								rect = new Rectangle(mx, my, 0, 0);
								state = State.LASSO;
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
		case TANGENT:
			edit.addEdit(tangentHandle.end());
			MainFrame.getInstance().getUndoManager().addEdit(edit);
			/*
			 * Repaint viewports
			 */
			MainFrame.getInstance().getAnimation().rethink();
			MainFrame.getInstance().getJPatchScreen().update_all();
			break;
		case MOVE_KEY:
			if (delta != 0) {
				edit = new JPatchActionEdit("move key");
				if (!moveSelection) {
					for (MotionKey selectedKey : hitKeys) {
						edit.addEdit(new AtomicMoveMotionKey(selectedKey, (int) selectedKey.getPosition() - delta));
						if (selectedKey instanceof MotionKey.Float) {
							MotionKey.Float key = (MotionKey.Float) selectedKey;
							float newValue = key.getFloat();
							key.setFloat(value);
							if (newValue != value)
								edit.addEdit(new AtomicChangeMotionKeyValue(key, newValue));
						}
					}
				} else {
					/*
					 * move all selected keys
					 */
					for (MotionKey selectedKey : selection.keySet()) {
						KeyData keyData = selection.get(selectedKey);
						edit.addEdit(new AtomicMoveMotionKey(selectedKey, (int) keyData.position));
					}
				}
				/*
				 * delete all suspended keys
				 */
				for (MotionKey key : suspendedKeys.keySet()) {
					suspendedKeys.get(key).addKey(key);
					edit.addEdit(new AtomicDeleteMotionKey(key));
				}
				suspendedKeys.clear();
				if (edit.isValid())
					MainFrame.getInstance().getUndoManager().addEdit(edit);
			}
			if (moveSelection) {
				/*
				 * Update selection
				 */
				for (MotionKey key : selection.keySet()) {
					selection.get(key).position = key.getPosition();
				}
			}
			/*
			 * Repaint viewports
			 */
			MainFrame.getInstance().getAnimation().rethink();
			MainFrame.getInstance().getJPatchScreen().update_all();
			break;
		case LASSO:
			if (range == null)
				range = new Range();
			selection.clear();
			Range newRange = new Range();
			boolean selectionValid = false;
			boolean[] hasKeys = new boolean[range.lastTrack - range.firstTrack + 1];
			int first = range.firstTrack;
			curveWithKeys.clear();
			for (int i = range.firstTrack; i <= range.lastTrack;i++) {
				Track track = timelineEditor.getTracks().get(i);
				if (track.isHidden())
					continue;
				
				for (MotionCurve motionCurve : track.getMotionCurves()) {
					int startIndex = motionCurve.getIndexAt(range.firstFrame - 1);
					int endIndex = motionCurve.getIndexAt(range.lastFrame - 1);
//					System.out.println(startIndex + " " + endIndex);
					if (startIndex != endIndex){
						if (i < newRange.firstTrack)
							newRange.firstTrack = i;
						if (i > newRange.lastTrack)
							newRange.lastTrack = i;
					}
//					System.out.println("index " + startIndex + " to " + endIndex);
					for (int j = startIndex; j < endIndex; j++) {
						curveWithKeys.add(motionCurve);
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
			if (selectionValid) {
				range = newRange;
				System.out.println(range);
			} else {
				range = null;
				rect = null;
				curveWithKeys.clear();
			}
			repaint();
			break;
		case RETIME_LEFT:	// fallthrough is intended!!!
		case RETIME_RIGHT:	// fallthrough is intended!!!
			edit = new JPatchActionEdit("move key");
			/*
			 * move all selected keys
			 */
			for (MotionKey selectedKey : selection.keySet()) {
				KeyData keyData = selection.get(selectedKey);
				edit.addEdit(new AtomicMoveMotionKey(selectedKey, (int) keyData.position));
			}
			/*
			 * delete all suspended keys
			 */
			for (MotionKey key : suspendedKeys.keySet()) {
				suspendedKeys.get(key).addKey(key);
				edit.addEdit(new AtomicDeleteMotionKey(key));
			}
			suspendedKeys.clear();
			/*
			 * Add edit
			 */
			if (edit.isValid())
				MainFrame.getInstance().getUndoManager().addEdit(edit);
			
			/*
			 * Update selection
			 */
			for (MotionKey key : selection.keySet()) {
				selection.get(key).position = key.getPosition();
			}
			
			/*
			 * repaint viewports
			 */
			MainFrame.getInstance().getAnimation().rethink();
			MainFrame.getInstance().getJPatchScreen().update_all();
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
//		System.out.println("frame " + frame + " delta " + delta + " state " + state);
//		System.out.println(Arrays.toString(hitKeys));
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
		case TANGENT:
			if (tangentHandle.getMotionKey().getTangentMode() != MotionKey.TangentMode.MANUAL) {
				edit.addEdit(new CompoundChangeTangentMode(tangentHandle.getMotionKey(), MotionKey.TangentMode.MANUAL));
			}
			hitTrack.moveKey(tangentHandle, e.getY() - trackTop);
			timelineEditor.repaint();
			break;
		case MOVE_KEY:
			int d = e.getX() / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart() - frame;
			boolean shift = d != delta;
			delta = d;
			
//			if (selectedTrack.isExpanded()) {
//				selectedTrack.moveKey(selectedKey, e.getY() - trackTop);
//			}
//			if (selectedTrack.getMotionCurve(selectedKey) != null && !selectedTrack.getMotionCurve(selectedKey).hasKeyAt(frame))
//				selectedTrack.shiftKey(selectedKey, frame);
			if (moveSelection) {
				if (!shift)
					return;
				for (MotionKey key: selection.keySet()) {
					KeyData keyData = selection.get(key);
					keyData.track.shiftKey(key, (int) keyData.position + delta);
				}
				range.firstFrame = firstFrame + delta;
				range.lastFrame = lastFrame + delta;
				/*
				 * unsuspend keys
				 */
				for (MotionKey key : new HashSet<MotionKey>(suspendedKeys.keySet())) {
					if (key.getPosition() < range.firstFrame || key.getPosition() > range.lastFrame) {
						suspendedKeys.get(key).addKey(key);
						suspendedKeys.remove(key);
					}
				}
				/*
				 * suspend old keys
				 */
//				for (int i = range.firstTrack; i <= range.lastTrack; i++) {
//					Track track = timelineEditor.getTracks().get(i);
//					if (track.isHidden())
//						continue;
//					if (!trackHasKeys[i - range.firstTrack])
//						continue;
//					for (MotionCurve motionCurve : track.getMotionCurves()) {
				System.out.println(curveWithKeys);	
				for (MotionCurve motionCurve : curveWithKeys) {
						int startIndex = motionCurve.getIndexAt(range.firstFrame - 1);
						int endIndex = motionCurve.getIndexAt(range.lastFrame);
						System.out.print("start\t" + range.firstFrame + "\tend\t" + range.lastFrame + "\tindex\t" + startIndex + "\tindex\t" + endIndex);
						Set<MotionKey> keysToRemove = new HashSet<MotionKey>();
						for (int j = startIndex; j < endIndex; j++) {
							MotionKey key = motionCurve.getKey(j);
							if (!selection.containsKey(key)) {
								suspendedKeys.put(key, motionCurve);
								keysToRemove.add(key);
								System.out.print("\tpos\t" + key.getPosition());
							}
						}
						for (MotionKey key : keysToRemove)
							motionCurve.removeKey(key);
						System.out.println();
//					}
				}
			} else {
				
				if (hitTrack.isExpanded())
					for (MotionKey key : hitKeys)
						hitTrack.moveKey(key, e.getY() - trackTop);
				if (shift) {
					
					for (MotionKey key : hitKeys) {
						MotionCurve mc = key.getMotionCurve();
						mc.forceRemoveKey(key);
						
					
					
					/*
					 * suspend old keys
					 */
					
						MotionKey sKey = mc.getKeyAt(frame + delta);
						System.out.println("sKey=" + sKey);
						if (sKey != null) {
//							boolean selected = false;
//							for (MotionKey test : hitKeys) {
//								if (test == sKey) {
//									selected = true;
//									break;
//								}
//							}
//							System.out.println(selected);
//							if (!selected) {
								suspendedKeys.put(sKey, mc);
								mc.forceRemoveKey(sKey);
//							}
						}
					
					
						key.setPosition(frame + delta);
						mc.addKey(key);
					}
//						hitTrack.shiftKey(key, frame + delta);
					
				}
				/*
				 * unsuspend keys
				 */
				for (MotionKey sKey : new HashSet<MotionKey>(suspendedKeys.keySet())) {
					if (sKey.getPosition() != frame + delta) {
						suspendedKeys.get(sKey).addKey(sKey);
						suspendedKeys.remove(sKey);
					}
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
			
//			if (!moveSelection) {
//				MainFrame.getInstance().getAnimation().setPosition(frame + delta);
//				timelineEditor.setCurrentFrame(frame + delta);
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			}
			
			break;
		case LASSO:
			if (range == null)
				range = new Range();
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
				if (rect.y >= top && rect.y < bottom) {
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
//			System.out.println(range);
			drawSelectionRectangle(g2);
			break;
		case RETIME_RIGHT:
			int f = (e.getX() - timelineEditor.getFrameWidth() / 2) / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
			int lastFrame = range.lastFrame;
			if (f == lastFrame)
				return;
			range.lastFrame = f;
			if (range.lastFrame < range.firstFrame)
				range.lastFrame = range.firstFrame;
			
			
			/*
			 * suspend keys
			 */
			if (range.lastFrame > retimeRange.lastFrame) {
//				for (int i = range.firstTrack; i <= range.lastTrack; i++) {
//					Track track = timelineEditor.getTracks().get(i);
//					if (!trackHasKeys[i - range.firstTrack])
//						continue;
//					for (MotionCurve motionCurve : track.getMotionCurves()) {
				for (MotionCurve motionCurve : curveWithKeys) {
						int startIndex = motionCurve.getIndexAt(retimeRange.lastFrame);
						int endIndex = motionCurve.getIndexAt(range.lastFrame);
						Set<MotionKey> keysToRemove = new HashSet<MotionKey>();
						for (int j = startIndex; j < endIndex; j++) {
							MotionKey key = motionCurve.getKey(j);
							if (!selection.containsKey(key)) {
								suspendedKeys.put(key, motionCurve);
								keysToRemove.add(key);
							}
						}
						for (MotionKey key : keysToRemove)
							motionCurve.removeKey(key);
//					}
				}
			}
			
			/*
			 * retime
			 */
//			for (int i = range.firstTrack; i <= range.lastTrack; i++) {
//				Track track = timelineEditor.getTracks().get(i);
//				if (!trackHasKeys[i - range.firstTrack])
//					continue;
//				for (MotionCurve motionCurve : track.getMotionCurves()) {
			for (MotionCurve motionCurve : curveWithKeys) {
					int startIndex = motionCurve.getIndexAt(range.firstFrame - 1);
					int endIndex = motionCurve.getIndexAt(lastFrame);
					Set<Integer> frames = new HashSet<Integer>();
					for (int j = startIndex; j < endIndex; j++) {
						MotionKey key = motionCurve.getKey(j);
						if (selection.containsKey(key)) {
							int dNew = range.lastFrame - range.firstFrame;
							int dOld = retimeRange.lastFrame - retimeRange.firstFrame;
							int newPos = Math.round(range.firstFrame + ((int) selection.get(key).position - range.firstFrame) * dNew / dOld);
							while (frames.contains(newPos))
								newPos++;
							frames.add(newPos);
							key.setPosition(newPos);
						}
					}
//				}
			}
			
			/*
			 * unsuspend keys
			 */
			for (MotionKey key : new HashSet<MotionKey>(suspendedKeys.keySet())) {
				if (key.getPosition() > range.lastFrame) {
					suspendedKeys.get(key).addKey(key);
					suspendedKeys.remove(key);
				}
			}
			
			repaint();
			break;
		case RETIME_LEFT:
			f = (e.getX() - timelineEditor.getFrameWidth() / 2) / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
			int firstFrame = range.firstFrame;
			if (f == firstFrame)
				return;
			range.firstFrame = f;
			if (range.firstFrame > range.lastFrame)
				range.firstFrame = range.lastFrame;
			
			
			/*
			 * suspend keys
			 */
			if (range.firstFrame < retimeRange.firstFrame) {
//				for (int i = range.firstTrack; i <= range.lastTrack; i++) {
//					Track track = timelineEditor.getTracks().get(i);
//					if (!trackHasKeys[i - range.firstTrack])
//						continue;
//					for (MotionCurve motionCurve : track.getMotionCurves()) {
				for (MotionCurve motionCurve : curveWithKeys) {
						int startIndex = motionCurve.getIndexAt(range.firstFrame - 1);
						int endIndex = motionCurve.getIndexAt(retimeRange.firstFrame);
						Set<MotionKey> keysToRemove = new HashSet<MotionKey>();
						for (int j = startIndex; j < endIndex; j++) {
							MotionKey key = motionCurve.getKey(j);
							if (!selection.containsKey(key)) {
								suspendedKeys.put(key, motionCurve);
								keysToRemove.add(key);
							}
						}
						for (MotionKey key : keysToRemove)
							motionCurve.removeKey(key);
					}
//				}
			}
			
			/*
			 * retime
			 */
//			for (int i = range.firstTrack; i <= range.lastTrack; i++) {
//				Track track = timelineEditor.getTracks().get(i);
//				if (!trackHasKeys[i - range.firstTrack])
//					continue;
//				for (MotionCurve motionCurve : track.getMotionCurves()) {
			for (MotionCurve motionCurve : curveWithKeys) {
					int startIndex = motionCurve.getIndexAt(firstFrame - 1);
					int endIndex = motionCurve.getIndexAt(range.lastFrame);
					Set<Integer> frames = new HashSet<Integer>();
					for (int j = endIndex - 1; j >= startIndex; j--) {
						MotionKey key = motionCurve.getKey(j);
						if (selection.containsKey(key)) {
							int dNew = range.lastFrame - range.firstFrame;
							int dOld = retimeRange.lastFrame - retimeRange.firstFrame;
							int newPos = Math.round(range.lastFrame - (range.lastFrame - (int) selection.get(key).position) * dNew / dOld);
							while (frames.contains(newPos))
								newPos--;
							frames.add(newPos);
							key.setPosition(newPos);
						}
					}
//				}
			}
			
			/*
			 * unsuspend keys
			 */
			for (MotionKey key : new HashSet<MotionKey>(suspendedKeys.keySet())) {
				if (key.getPosition() < range.firstFrame) {
					suspendedKeys.get(key).addKey(key);
					suspendedKeys.remove(key);
				}
			}
			
			repaint();
			break;	
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		boolean vResize = false;
		boolean retimeLeft = false;
		boolean retimeRight = false;
		int y = 0;
		for (int i = 0; i < timelineEditor.getTracks().size(); i++) {
			Track track = timelineEditor.getTracks().get(i);
			if (track.isExpanded() && e.getY() > y + track.getHeight() - 6 && e.getY() <= y + track.getHeight()) {
				vResize = true;
				break;
			}
			y += track.getHeight();
		}
		if (range != null && range.lastFrame > range.firstFrame) {
			if (e.getX() < rect.x + 4 && e.getX() > rect.x - 4)
				retimeLeft = true;
			else if (e.getX() < rect.x + rect.width + 4 && e.getX() > rect.x + rect.width - 4)
				retimeRight = true;
		}
		
		if (retimeLeft)
			timelineEditor.setCursor(TimelineEditor.westResizeCursor);
		else if (retimeRight)
			timelineEditor.setCursor(TimelineEditor.eastResizeCursor);
		else if (vResize)
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
		mi = new JMenuItem("delete selected keys");
//		final MotionCurve motionCurve = selectedTrack == null ? null : selectedTrack.getMotionCurve(selectedKey);
		mi.addActionListener(deleteAction);
		mi.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
		mi.setEnabled(hitKeys != null || !selection.isEmpty());
		popup.add(mi);
		
		/*
		 * cut
		 */
		mi = new JMenuItem("cut");
		mi.addActionListener(cutAction);
		mi.setAccelerator(KeyStroke.getKeyStroke("control X"));
		mi.setEnabled(!selection.isEmpty());
		popup.add(mi);
		
		/*
		 * copy
		 */
		mi = new JMenuItem("copy");
		mi.addActionListener(copyAction);
		mi.setAccelerator(KeyStroke.getKeyStroke("control C"));
		mi.setEnabled(!selection.isEmpty());
		popup.add(mi);
		
		/*
		 * paste
		 */
		mi = new JMenuItem("paste at current frame (" + currentframe + ")");
		mi.addActionListener(pasteAction);
		mi.setAccelerator(KeyStroke.getKeyStroke("control V"));
		mi.setEnabled(!clipboard.isEmpty());
		popup.add(mi);
		
		/*
		 * paste
		 */
		mi = new JMenuItem("paste at this frame (" + frame + ")");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				paste(frame);
				TrackView.this.repaint();
				MainFrame.getInstance().getAnimation().rethink();
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		mi.setEnabled(!clipboard.isEmpty());
		popup.add(mi);
		
		popup.add(new JSeparator());
		
		menu = new JMenu("modify selected keys");
		JMenu subMenu;
		
		subMenu = new JMenu("set interpolation method to");
		for (final MotionKey.Interpolation interpolation : MotionKey.Interpolation.values()) {
			mi = new JMenuItem(interpolation.toString().toLowerCase());
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					setInterpolation(interpolation);
					TrackView.this.repaint();
				}
			});
			subMenu.add(mi);
		}
		menu.add(subMenu);
		
		subMenu = new JMenu("set tangent mode to");
		for (final MotionKey.TangentMode tangentMode : MotionKey.TangentMode.values()) {
			mi = new JMenuItem(tangentMode.toString().toLowerCase());
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					setTangentMode(tangentMode);
					TrackView.this.repaint();
				}
			});
			subMenu.add(mi);
		}
		menu.add(subMenu);
		
		subMenu = new JMenu("set tangent continuity to");
		mi = new JMenuItem("smooth");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setTangentSmooth(true);
				TrackView.this.repaint();
			}
		});
		subMenu.add(mi);
		mi = new JMenuItem("independend");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setTangentSmooth(false);
				TrackView.this.repaint();
			}
		});
		subMenu.add(mi);
		menu.add(subMenu);
		
		menu.setEnabled(!selection.isEmpty() || hitKeys != null);
		popup.add(menu);
		
		
		
		mi = new JMenuItem("revalidate anchor positions");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				AnimModel animModel = (AnimModel) timelineEditor.getAnimObject();
				MotionCurveSet.Model mcs = (MotionCurveSet.Model) MainFrame.getInstance().getAnimation().getCurvesetFor(animModel);
				JPatchActionEdit edit = new JPatchActionEdit("revalidate anchor position");
				MotionKey.Object[] anchorKeys = new MotionKey.Object[mcs.anchor.getKeyCount()];
				for (int i = 0, n = mcs.anchor.getKeyCount(); i < n; i++) {
					anchorKeys[i] = (MotionKey.Object) mcs.anchor.getKey(i);
					if (i == 0)
						continue;
					MotionKey positionKey = mcs.position.getKeyAt(anchorKeys[i].getPosition());
					if (positionKey != null)
						edit.addEdit(new AtomicDeleteMotionKey(positionKey));
				}
				for (int i = 1; i < anchorKeys.length; i++) {
					edit.addEdit(new AtomicDeleteMotionKey(anchorKeys[i]));
				}
				for (int i = 1; i < anchorKeys.length; i++) {
					float frame = anchorKeys[i].getPosition();
					Transformable oldAnchor = (Transformable) anchorKeys[i - 1].getObject();
					Transformable newAnchor = (Transformable) anchorKeys[i].getObject();
					MainFrame.getInstance().getAnimation().setPosition(frame);
					
					Point3f p0 = oldAnchor == null ? new Point3f() : oldAnchor.getPosition();
					Point3f p1 = (newAnchor == null) ? new Point3f() : newAnchor.getPosition();
					Vector3d v = new Vector3d(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
					
					animModel.getTransform().transform(v);
					Point3d p = animModel.getPositionDouble();
					p.add(v);
					edit.addEdit(new AtomicModifyMotionCurve.Object(mcs.anchor, frame, newAnchor));
					edit.addEdit(new AtomicModifyMotionCurve.Point3d(mcs.position, frame, p));
				}
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				MainFrame.getInstance().getAnimation().setPosition(MainFrame.getInstance().getAnimation().getStart());
				MainFrame.getInstance().getJPatchScreen().update_all();
				TrackView.this.repaint();
			}
		});
		
		mi.setEnabled(timelineEditor.getAnimObject() instanceof AnimModel);
		popup.add(new JSeparator());
		popup.add(mi);
		
		popup.add(new JSeparator());
		mi = new JMenuItem("dump track curves");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (MotionCurve mc : track.getMotionCurves()) {
					System.out.println("=== " + mc.getName() + " ===");
					mc.dump();
				}
			}
		});
		popup.add(mi);
		
		popup.show((Component) e.getSource(), e.getX(), e.getY());
	}
	
	private void setInterpolation(MotionKey.Interpolation interpolation) {
		JPatchActionEdit edit = new JPatchActionEdit("change interpolation");
		for (MotionKey key : selection.keySet()) {
			if (key.getInterpolation() != interpolation)
				edit.addEdit(new AtomicChangeMotionKey.Interpolation(key, interpolation));
		}
		if (hitKeys != null) {
			for (MotionKey key : hitKeys) {
				if (key.getInterpolation() != interpolation)
					edit.addEdit(new AtomicChangeMotionKey.Interpolation(key, interpolation));
			}
		}
		if (edit.isValid())
			MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
	
	private void setTangentMode(MotionKey.TangentMode tangentMode) {
		JPatchActionEdit edit = new JPatchActionEdit("change tangent mode");
		for (MotionKey key : selection.keySet()) {
			if (key.getTangentMode() != tangentMode)
				edit.addEdit(new CompoundChangeTangentMode(key, tangentMode));
		}
		if (hitKeys != null) {
			for (MotionKey key : hitKeys) {
				if (key.getTangentMode() != tangentMode)
					edit.addEdit(new CompoundChangeTangentMode(key, tangentMode));
			}
		}
		if (edit.isValid())
			MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
	
	private void setTangentSmooth(boolean smooth) {
		JPatchActionEdit edit = new JPatchActionEdit("change tangent mode");
		for (MotionKey key : selection.keySet()) {
			if (key.isSmooth() != smooth)
				edit.addEdit(new AtomicChangeMotionKey.Smooth(key, smooth));
		}
		if (hitKeys != null) {
			for (MotionKey key : hitKeys) {
				if (key.isSmooth() != smooth)
					edit.addEdit(new AtomicChangeMotionKey.Smooth(key, smooth));
			}
		}
		if (edit.isValid())
			MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
	private void drawSelectionRectangle(Graphics g) {
		System.out.println("rect = " + rect.x + " " + rect.y + " " + rect.width + " " + rect.height);
		g.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y);
		g.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
		g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height);
		g.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y + rect.height);
	}
	
	private void delete(String editName) {
		JPatchActionEdit edit = new JPatchActionEdit(editName);
		if (hitKeys != null) {
			for (MotionKey key : hitKeys)
				edit.addEdit(new AtomicDeleteMotionKey(key));
		} else {
			for (MotionKey key : selection.keySet()) {
				edit.addEdit(new AtomicDeleteMotionKey(key));
			}
		}
		MainFrame.getInstance().getUndoManager().addEdit(edit);
		hitKeys = null;
		selection.clear();
		range = null;
		rect = null;
	}
	
	private void copy() {
		clipboard.clear();
//		if (hitKeys != null) {
//			for (MotionKey key : hitKeys) {
//				MotionKey copy = key.copy();
//				copy.setPosition(0);
//				clipboard.put(copy, hitTrack.getMotionCurve(key));
//			}
//		} else {
			for (MotionKey key : selection.keySet()) {
				MotionKey copy = (MotionKey) key.copy();
				copy.setPosition(copy.getPosition() - range.firstFrame);
				clipboard.put(copy, key.getMotionCurve());
			}
			clipRange = new Range(range);
//			clipTrackHasKeys = new boolean[trackHasKeys.length];
//			System.arraycopy(trackHasKeys, 0, clipTrackHasKeys, 0, trackHasKeys.length);
			clipCurveWithKeys.clear();
			clipCurveWithKeys.addAll(curveWithKeys);
//		}
	}
	
	private void paste(int frame) {
		JPatchActionEdit edit = new JPatchActionEdit("paste");
		
		/*
		 * delete keys
		 */
//		for (int i = clipRange.firstTrack; i <= clipRange.lastTrack; i++) {
//			Track track = timelineEditor.getTracks().get(i);
////			System.out.println(track.getName() + " " + trackHasKeys[i - range.firstTrack]);
//			if (!clipTrackHasKeys[i - clipRange.firstTrack])
//				continue;
//			for (MotionCurve motionCurve : track.getMotionCurves()) {
		for (MotionCurve motionCurve : clipCurveWithKeys) {
				int startIndex = motionCurve.getIndexAt(frame - 1);
				int endIndex = motionCurve.getIndexAt(frame - clipRange.firstFrame + clipRange.lastFrame);
				Set<MotionKey> keysToRemove = new HashSet<MotionKey>();
				for (int j = startIndex; j < endIndex; j++) {
					keysToRemove.add(motionCurve.getKey(j));
				}
				for (MotionKey key : keysToRemove)
					edit.addEdit(new AtomicDeleteMotionKey(key));
//			}
		}
		
		/*
		 * paste
		 */
		for (MotionKey key : clipboard.keySet()) {
			MotionKey copy = key.copy();
			copy.setPosition(copy.getPosition() + frame);
			edit.addEdit(new AtomicAddMotionKey(clipboard.get(key), copy));
		}
		MainFrame.getInstance().getUndoManager().addEdit(edit);
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