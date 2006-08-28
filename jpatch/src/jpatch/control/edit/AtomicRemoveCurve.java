package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;

public class AtomicRemoveCurve extends JPatchAtomicEdit {
	
	private OLDControlPoint cpStart;
	
	public AtomicRemoveCurve(OLDControlPoint start) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + start + ")");
		cpStart = start;
		redo();
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().addCurve(cpStart);
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().removeCurve(cpStart);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
