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
	
	public int getInlay() {
		return level * 4;
	}
	
	public void paint(Graphics g, int y) {	
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		
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
