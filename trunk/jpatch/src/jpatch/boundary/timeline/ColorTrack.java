package jpatch.boundary.timeline;

import java.awt.*;
import javax.swing.*;

import jpatch.entity.*;

public class ColorTrack extends Track<MotionCurve.Color3f> {

	public ColorTrack(TimelineEditor timelineEditor, MotionCurve.Color3f motionCurve) {
		super(timelineEditor, motionCurve);
	}

	public void paint(Graphics g, int y) {	
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		
		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
		g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
		
		frame = start / fw - 1;
		for (int x = -fw ; x <= clip.width + fw; x ++) {
			float f = (float) (start + x - fw / 2) / fw;
			g.setColor(motionCurve.getColor3fAt(f).get());
			g.drawLine(x + start, y + 5, x + start, y + 9);
		}
		
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			MotionKey.Color3f colorKey = (MotionKey.Color3f) motionCurve.getKeyAt(frame);
			if (colorKey != null) {
				//g.fill3DRect(x + start - iFrameWidth / 2, y + 2, iFrameWidth, 11, true);
				g.setColor(colorKey.getColor3f().get());
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
