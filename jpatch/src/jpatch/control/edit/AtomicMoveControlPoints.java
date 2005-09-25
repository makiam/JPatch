package jpatch.control.edit;

import javax.vecmath.*;

import jpatch.entity.*;

public class AtomicMoveControlPoints extends JPatchAtomicEdit {
	private ControlPoint[] acp;
	private Point3f[] ap3Position;
	
	public AtomicMoveControlPoints(ControlPoint[] controlPoints) {
		this.acp = controlPoints;
		ap3Position = new Point3f[acp.length];
		for (int c = 0; c < acp.length; c++)
			ap3Position[c] = new Point3f(acp[c].getPosition());
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	public int sizeOf() {
		return 8 + 4 + 8 + acp.length * 4 + 4 + 8 + ap3Position.length * 4;
	}
	
	private void swap() {
		for (int c = 0; c < acp.length; c++) {
			Point3f p = acp[c].getPosition();
			float x = p.x;
			float y = p.y;
			float z = p.z;
			acp[c].setPosition(ap3Position[c]);
			ap3Position[c].set(x, y, z);
		}
	}
}

			
