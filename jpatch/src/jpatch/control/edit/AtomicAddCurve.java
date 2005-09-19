package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;

public class AtomicAddCurve extends JPatchAtomicEdit {
	private ControlPoint cpStart;
	
	public AtomicAddCurve(ControlPoint start) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + start + ")");
		cpStart = start;
		if (cpStart == null || cpStart.getNext() == null)
			throw new IllegalArgumentException("bad curve");
		redo();
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().removeCurve(cpStart);
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().addCurve(cpStart);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}

			
