package jpatch.boundary.timeline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Map;

import javax.swing.UIManager;

import jpatch.entity.Bone;
import jpatch.entity.MotionCurve;
import jpatch.entity.MotionKey;
import jpatch.entity.RotationDof;

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
	
	public int getIndent() {
		return level * 4;
	}
	
	public void paint(Graphics g, int y) {	
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		
		if (bExpanded) {
			float min = 0, max = 0;
			for (MotionCurve.Float motionCurve : motionCurves) {
				if (motionCurve.getMin() < min)
					min = motionCurve.getMin();
				if (motionCurve.getMax() > max)
					max = motionCurve.getMax();
			}
			
			g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
			g.drawLine(clip.x, y + getHeight() - 2, clip.x + clip.width, y + getHeight() - 2);
			g.drawLine(clip.x, y + getHeight() - 6, clip.x + clip.width, y + getHeight() - 6);
			g.setColor(UIManager.getColor("ScrollBar.shadow"));
			g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
			float scale = max - min;
			int size = iExpandedHeight - 17;
			int off = iExpandedHeight - 12 + (int) Math.round(size * min / scale);
			g.setColor(TRACK);
			g.fillRect(clip.x, y + 5, clip.width, size + 1);
//			g.setColor(TRACK.darker());
//			g.drawLine(clip.x, y + 1, clip.x + clip.width, y + 1);
//			g.setColor(TRACK.brighter());
//			g.drawLine(clip.x, y + 61, clip.x + clip.width, y + 61);
			frame = start / fw - 1;
			g.setColor(TICK);
			for (int x = -fw ; x <= clip.width + fw; x += fw) {
				if (frame % 6 == 0) {
					g.setColor(ZERO);
					g.drawLine(x + start, y + 5, x + start, y + iExpandedHeight - 12);
					g.setColor(TICK);
				} else {
					g.drawLine(x + start, y + 5, x + start, y + iExpandedHeight - 12);
				}
				frame++;
			}
			g.setColor(ZERO);
			g.drawLine(clip.x, y + off, clip.x + clip.width, y + off);
			g.setClip(clip.intersection(new Rectangle(clip.x, y + 2, clip.width, size + 7)));
			
			frame = start / fw - 1;
			for (int i = 0; i < motionCurves.length; i++) {
				MotionCurve.Float motionCurve = motionCurves[i];
				int vPrev = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
				g.setColor(col[i]);
				
				for (int x = -fw ; x <= clip.width + fw; x ++) {
					float f = (float) (start + x - fw / 2) / fw;
					int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(f));
//					g.setColor(Color.BLACK);
					g.drawLine(x + start - 1, y + vPrev, x + start, y + vThis);
					frame++;
					vPrev = vThis;
				}
				g.setColor(col[i]);
				frame = start / fw - 1;
				for (int x = -fw ; x <= clip.width + fw; x += fw) {
					int vThis = off - (int) Math.round(size / scale * motionCurve.getFloatAt(frame));
					if (motionCurve.hasKeyAt(frame)) {
						g.fillOval(x + start - 3, y + vThis - 3, 6, 6);
						g.setColor(Color.BLACK);
						g.drawOval(x + start - 3, y + vThis - 3, 6, 6);
						g.setColor(col[i]);
					} else {
//						g.fillRect(x + start - 1, y + vThis - 1, 3, 3);
					}
					frame++;
				}
			}
			g.setClip(clip);
			return;
		}
		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
		g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
		g.setColor(TRACK);
		g.fillRect(clip.x, y + 5, clip.width, 5);
		
		g.setColor(KEY);
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			boolean key = false;
			for (MotionCurve m : motionCurves)
				if (m.hasKeyAt(frame)) {
					key = true;
					break;
				}
			if (key) {
				//g.fill3DRect(x + start - iFrameWidth / 2, y + 2, iFrameWidth, 11, true);
				g.setColor(KEY);
				g.fillOval(x + start - 3, y + 4, 6, 6);
				g.setColor(Color.BLACK);
				g.drawOval(x + start - 3, y + 4, 6, 6);
				
			} else {
				if (frame % 6 == 0) {
					g.setColor(ZERO);
					g.drawLine(x + start, y + 5, x + start, y + 9);
				} else {
					g.setColor(TICK);
					g.drawLine(x + start, y + 5, x + start, y + 9);
				}
			}
			frame++;
		}
	}
}
