package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

public class ChangeControlPointPositionEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private Point3f p3Position;
	private static Point3f p3Cache = new Point3f();
	
	public ChangeControlPointPositionEdit(ControlPoint cp, Point3f oldPosition) {
		this.cp = cp;
		this.p3Position = new Point3f(oldPosition);
	}
	
	public String name() {
		return ("change controlpoint position");
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		p3Cache.set(p3Position);
		p3Position.set(cp.getPosition());
		cp.setPosition(p3Cache);
	}
}
