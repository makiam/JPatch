package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;

public class AtomicAddMorph extends JPatchAtomicEdit implements JPatchRootEdit {
	private Morph morph;
	
	public AtomicAddMorph(Morph morph) {
		this.morph = morph;
		redo();
	}
	
	public String getName() {
		return "add morph";
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().removeExpression(morph);
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().addExpression(morph);
	}
	
	public int sizeOf() {
		return 8 + 4;
	}
}
