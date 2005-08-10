package jpatch.boundary.selection;

public interface SelectionListener {
	void selectionChanged(Selection selection);
	void pivotChanged(Selection selection);
	void rotationChanged(Selection selection);
}

