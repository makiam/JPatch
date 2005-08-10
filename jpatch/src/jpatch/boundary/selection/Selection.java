package jpatch.boundary.selection;

import java.util.*;

import jpatch.boundary.*;

public abstract class Selection extends JPatchTreeLeaf {
	protected Collection colListeners = new ArrayList();
	
	//public String toString() {
	//	return getClass() + "@" + hashCode();
	//}
	
	public int getNodeType() {
		return SELECTION;
	}
	
	public void addSelectionListener(SelectionListener selectionListener) {
		colListeners.add(selectionListener);
	}
	
	public void removeSelectionListener(SelectionListener selectionListener) {
		colListeners.remove(selectionListener);
	}
	
	protected void firePivotChanged() {
		for (Iterator it = colListeners.iterator(); it.hasNext();) {
			((SelectionListener)it.next()).pivotChanged(this);
		}
	}
	
	protected void fireRotationChanged() {
		for (Iterator it = colListeners.iterator(); it.hasNext();) {
			((SelectionListener)it.next()).rotationChanged(this);
		}
	}
	
	//public String toString() {
	//	return "PointSelection@" + Integer.toString(hashCode());
	//}
}
