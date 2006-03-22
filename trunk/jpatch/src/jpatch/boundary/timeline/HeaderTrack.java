package jpatch.boundary.timeline;

import java.awt.*;

import javax.swing.UIManager;

import jpatch.control.edit.AtomicModifyMotionCurve;
import jpatch.control.edit.JPatchUndoableEdit;
import jpatch.entity.MotionCurve;
import jpatch.entity.MotionKey;

public class HeaderTrack extends Track {
	private String name;
	private int iIndent;
	private boolean bDontHide = false;
	
	public HeaderTrack(TimelineEditor timelineEditor, String name, int indent, boolean showAlways) {
		super(timelineEditor, null);
		this.name = name;
		iIndent = indent;
		bDontHide = showAlways;
	}

	public boolean isHidden() {
		return bDontHide ? false : super.isHidden();
	}
	
	public String getName() {
		return name;
	}
	
	public int getIndent() {
		return iIndent;
	}
	
	public MotionKey getKeyAt(int mx, int my) {
		return null;
	}
	
	public void moveKey(Object key, int y) { }
	
	public void shiftKey(Object key, int frame) { }
	
	public JPatchUndoableEdit insertKeyAt(int frame) {
		return null;
	}
	
	public void paint(Graphics g, int y, Object selectedKey) {	
//		Rectangle clip = g.getClipBounds();
//		int fw = timelineEditor.getFrameWidth();
//		int start = clip.x - clip.x % fw + fw / 2;
//		int frame = start / fw - 1;
//		
//		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
//		g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
//		g.setColor(UIManager.getColor("ScrollBar.shadow"));
//		g.fillRect(clip.x, y, clip.width, getHeight() - 1);
	}
}
