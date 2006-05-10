package jpatch.boundary.timeline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.UIManager;

import jpatch.boundary.MainFrame;
import jpatch.control.edit.*;
import jpatch.entity.*;

public class BoneTrack extends Track {
	
	private MotionCurve.Float[] motionCurves;
	private Bone bone;
	private int level;
	private Color[] col = new Color[] {
			new Color(255, 0, 0),
			new Color(0, 128, 0),
			new Color(0, 0, 255)
	};
	public BoneTrack(TimelineEditor timelineEditor, MotionCurve.Float[] motionCurves, Bone bone, int level) {
		super(timelineEditor, null);
		this.motionCurves = motionCurves;
		this.bone = bone;
		this.level = level;
		bExpandable = true;
	}
	
	public String getName() {
		return bone.getName();
	}
	
	public Bone getBone() {
		return bone;
	}
	
	public int getIndent() {
		return level * 4;
	}
	

	public void paint(Graphics g, int y, Map<MotionKey, TrackView.KeyData> selection, MotionKey[] hitKeys) {
		int bottom = getHeight() - 4;
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		
		if (bExpanded) {
			float min = 0, max = 0;
			for (MotionCurve.Float motionCurve : motionCurves) {
				if (motionCurve.getMin() < min)
					min = motionCurve.getMin();
				if (motionCurve.getMax() > max)
					max = motionCurve.getMax();
			}
			
			float scale = max - min;
			int size = iExpandedHeight - 4;
			int off = iExpandedHeight - 4 + (int) Math.round(size * min / scale);
			if (timelineEditor.getHeader().getSelectedTracks().contains(this)) 
				g.setColor(TimelineEditor.SELECTED_BACKGROUND);
			else
				g.setColor(TimelineEditor.TRACK);
			g.fillRect(clip.x, y + 1, clip.width, size);

			frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
			g.setColor(TimelineEditor.BACKGROUND);
//			g.fillRect(clip.x, y - 3, clip.width, 3);
			g.fillRect(clip.x, y + bottom + 1, clip.width, 3);
			g.setColor(TimelineEditor.SHADOW);
			g.drawLine(clip.x, y, clip.x + clip.width, y);
			g.drawLine(clip.x, y + bottom, clip.x + clip.width, y + bottom);
			g.setColor(TimelineEditor.LIGHT_SHADOW);
			g.drawLine(clip.x, y + 1, clip.x + clip.width, y + 1);
			g.setClip(clip.intersection(new Rectangle(clip.x, y + 1, clip.width, bottom - 1)));
			g.setColor(TimelineEditor.DARK_TICK);
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				if (frame % 6 == 0)
					g.drawLine(x + start, y + 2, x + start, y + size - 1);
				else
					g.drawLine(x + start, y + off - 5, x + start, y + off + 5);
				frame++;
			}
			g.drawLine(clip.x, y + off, clip.x + clip.width, y + off);

			frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
			for (int i = motionCurves.length - 1; i >= 0; i--) {
				MotionCurve.Float motionCurve = motionCurves[i];
				int vPrev = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
				g.setColor(col[i]);
				for (int x = -fw ; x <= clip.width + fw; x++) {
					float f = (float) (start + x - fw / 2) / fw;
					int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(f));
					g.drawLine(x + start - 1, y + vPrev, x + start, y + vThis);
					frame++;
					vPrev = vThis;
				}
				frame = start / fw - 1;
				for (int x = -fw ; x <= clip.width + fw; x += fw) {
					int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
					MotionKey key = motionCurve.getKeyAt(frame);
					if (key != null) {
						if (keyHit(key, hitKeys))
							g.setColor(TimelineEditor.HIT_KEY);
						else if (selection.containsKey(key))
							g.setColor(TimelineEditor.SELECTED_KEY);
						else
							g.setColor(col[i]);
						g.fillOval(x + start - 3, y + vThis - 3, 6, 6);
						g.setColor(Color.BLACK);
						g.drawOval(x + start - 3, y + vThis - 3, 6, 6);
					}
					frame++;
				}
			}
			g.setClip(clip);
			return;
		}
		Color background, track;
		if (timelineEditor.getHeader().getSelectedTracks().contains(this)) {
			background = TimelineEditor.SELECTED_BACKGROUND;
			track = TimelineEditor.SHADOW;
		} else {
			background = TimelineEditor.BACKGROUND;
			track = TimelineEditor.LIGHT_SHADOW;
		}
		g.setColor(background);
		g.fillRect(clip.x, y + TOP - 2, clip.width, 3);
		g.fillRect(clip.x, y + TOP + 4, clip.width, 3);
		g.setColor(track);
		g.fillRect(clip.x, y + TOP + 1, clip.width, 3);
	
		
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			MotionKey key;
			boolean selected = false;
			boolean hit = false;
			boolean draw = false;
			for (MotionCurve m : motionCurves) {
				key = m.getKeyAt(frame);
				if (key != null) {
					draw = true;
					if (keyHit(key, hitKeys)) {
						hit = true;
						break;
					}
					if (selection.containsKey(key))
						selected = true;
				}
			}
			if (draw) {
				if (hit)
					g.setColor(TimelineEditor.HIT_KEY);
				else if (selected)
					g.setColor(TimelineEditor.SELECTED_KEY);
				else
					g.setColor(Color.GRAY);
				g.fillOval(x + start - 3, y + TOP - 1, 6, 6);
				g.setColor(Color.BLACK);
				g.drawOval(x + start - 3, y + TOP - 1, 6, 6);
			} 
			frame++;
		}
	}
	
	public MotionKey[] getKeysAt(int mx, int my) {
		int frame = mx / timelineEditor.getFrameWidth() + (int) MainFrame.getInstance().getAnimation().getStart();
		float min = 0, max = 0;
		MotionKey[] result = new MotionKey[motionCurves.length];
		for (MotionCurve.Float motionCurve : motionCurves) {
			if (motionCurve.getMin() < min)
				min = motionCurve.getMin();
			if (motionCurve.getMax() > max)
				max = motionCurve.getMax();
		}
		int i = 0;
		for (MotionCurve.Float motionCurve : motionCurves) {
			MotionKey.Float key = (MotionKey.Float) motionCurve.getKeyAt(frame);
			if (key == null)
				continue;
			if (isExpanded()) {
				float scale = max - min;
				int size = iExpandedHeight - 4;
				int off = iExpandedHeight - 4 + (int) Math.round(size * min / scale);
				int ky = off - (int) Math.round(size / scale * key.getFloat());
				if (my > ky - 5 && my < ky + 5) {
					reorder(motionCurve);
					return new MotionKey[] { key };
				}
			} else {
				result[i++] = key;
			}
		}
		if (i == 0)
			return null;
		MotionKey[] ret = new MotionKey[i];
		for (int j = 0; j < i; j++)
			ret[j] = result[j];
		return ret;
	}
	
	public MotionCurve[] getMotionCurves() {
		return motionCurves;
	}
	
	public MotionCurve getMotionCurve(MotionKey key) {
		if (key == null)
			return null;
		for (MotionCurve.Float motionCurve : motionCurves) {
			if (motionCurve.getKeyAt(key.getPosition()) == key)
				return motionCurve;
		}
		return null;
	}
	
	public void moveKey(Object object, int y) {
		MotionKey.Float key = (MotionKey.Float) object;
		float min = 0, max = 0;
		for (MotionCurve.Float motionCurve : motionCurves) {
			if (motionCurve.getMin() < min)
				min = motionCurve.getMin();
			if (motionCurve.getMax() > max)
				max = motionCurve.getMax();
		}
		float scale = max - min;
		int size = iExpandedHeight - 4;
		int off = iExpandedHeight - 4 + (int) Math.round(size * min / scale);
		float f = (off - y) * scale / size;
		if (f < min)
			f = min;
		if (f > max)
			f = max;
		key.setFloat(f);
	}
	
	public void shiftKey(Object object, int frame) {
		MotionKey.Float key = (MotionKey.Float) object;
		for (MotionCurve.Float motionCurve : motionCurves) {
			if (motionCurve.getKeyAt(key.getPosition()) == key) {
				motionCurve.moveKey(key, frame);
				return;
			}
		}
	}
	
	public JPatchUndoableEdit insertKeyAt(int frame) {
		JPatchActionEdit edit = new JPatchActionEdit("insert keys");
		for (MotionCurve motionCurve : motionCurves)
			edit.addEdit(new AtomicModifyMotionCurve.Float((MotionCurve.Float) motionCurve, frame, ((MotionCurve.Float) motionCurve).getFloatAt(frame)));
		return edit;
	}	
		
	private void reorder(MotionCurve.Float motionCurve) {
		MotionCurve.Float[] newCurves = new MotionCurve.Float[motionCurves.length];
		Color[] newColors = new Color[motionCurves.length];
		newCurves[0] = motionCurve;
		int j = 0;
		for (int i = 1; i < newCurves.length; i++) {
			if (motionCurves[j] == motionCurve) {
				newColors[0] = col[j];
				j++;
			}
			newCurves[i] = motionCurves[j];
			newColors[i] = col[j++];
		}
		if (newColors[0] == null)
			newColors[0] = col[j--];
		motionCurves = newCurves;
		col = newColors;
	}
//	private class KeyCurve {
//		private MotionKey.Float key;
//		private MotionCurve.Float curve;
//		private KeyCurve(MotionKey.Float key, MotionCurve.Float curve) {
//			this.key = key;
//			this.curve = curve;
//		}
//	}
}
