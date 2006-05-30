package jpatch.boundary.timeline;

import java.awt.*;
import java.util.*;
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
	
	public MotionKey[] getKeysAt(int mx, int my) {
		return null;
	}
	
	public void moveKey(Object key, int y) { }
	
	public void shiftKey(Object key, int frame) { }
	
	public JPatchUndoableEdit insertKeyAt(int frame) {
		return null;
	}
	
	@Override
	public MotionCurve[] getMotionCurves() {
		return new MotionCurve[0];	// return empty array
	}
	
	public void paint(Graphics g, int y, Map<MotionKey, TrackView.KeyData> selection, MotionKey[] hitKeys) {	
		// don't paint anything
	}
}
