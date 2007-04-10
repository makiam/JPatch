package jpatch.control.edit2;

import jpatch.entity.Selection;

public final class ModifySelection {
	/**
	 * Factory method that creates a new JPatchUndoableEdit that adds the specified objects to the specified selection.
	 * @param selection the selection the objects should be added to
	 * @param objects an array containing the objects to add
	 * @param weights an array containing the objects weights. It may be null, and must be of the same length as objects otherwise.
	 * @param applyNow true to immediately add the objects to the selection, false otherwise
	 * @return a new JPatchUndoableEdit
	 * @throws IllegalArgumentException if one of the objects is not part of the selection or, if weights.lenght != objects.length (if weights != null)
	 */
	public static JPatchUndoableEdit addObjectsToSelection(Selection selection, Object[] objects, double[] weights, boolean applyNow) {
		double[] w = null;
		if (weights != null) {
			if (weights.length != objects.length) {
				throw new IllegalArgumentException("weights.length (" + weights.length + ") != objects.length (" + objects.length + ")");
			}
			w = weights.clone();
		}
		for (int i = 0; i < objects.length; i++) {
			if (!selection.contains(objects[i])) {
				throw new IllegalArgumentException(selection + " does not contain the object " + objects[i]);
			}
		}
		return new AddObjectsToSelectionEdit(selection, objects.clone(), w, applyNow);
	}
	
	/**
	Factory method that creates a new JPatchUndoableEdit that removes the specified objects from the specified selection.
	 * @param selection the selection the objects should be removed from
	 * @param objects an array containing the objects to remove
	 * @param applyNow true to immediately remove the objects from the selection, false otherwise
	 * @return a new JPatchUndoableEdit
	 * @throws IllegalArgumentException if one of the objects is not part of the selection
	 */
	public static JPatchUndoableEdit removeObjectsFromSelection(Selection selection, Object[] objects, boolean applyNow) {
		double[] weights = new double[objects.length];
		for (int i = 0; i < objects.length; i++) {
			if (!selection.contains(objects[i])) {
				throw new IllegalArgumentException(selection + " does not contain the object " + objects[i]);
			}
			weights[i] = selection.getWeight(objects[i]);
		}
		return new RemoveObjectsFromSelectionEdit(selection, objects.clone(), weights, applyNow);
	}
	
	private static class ModifySelectionEdit extends AbstractUndoableEdit {
		final Selection selection;
		final Object[] objects;
		final double[] weights;
		
		private ModifySelectionEdit(Selection selection, Object[] objects, double[] weights) {
			this.selection = selection;
			this.objects = objects;
			this.weights = weights;
		}
		
		/**
		 * adds the objects to the selection
		 */
		void add() {
			if (weights == null) {
				for (int i = 0; i < objects.length; i++) {
					selection.add(objects[i]);
				}
			} else {
				for (int i = 0; i < objects.length; i++) {
					selection.add(objects[i], weights[i]);
				}
			}
		}
		
		/**
		 * removes the objects from the selection
		 */
		void remove() {
			for (int i = 0; i < objects.length; i++) {
				selection.remove(objects[i]);
			}
		}
	}
	
	private static final class AddObjectsToSelectionEdit extends ModifySelectionEdit {
		private AddObjectsToSelectionEdit(Selection selection, Object[] objects, double[] weights, boolean applyNow) {
			super(selection, objects, weights);
			if (applyNow) {
				redo();
			}
		}

		@Override
		public void redo() {
			super.redo();
			add();
		}

		@Override
		public void undo() {
			super.undo();
			remove();
		}
	}
	
	private static final class RemoveObjectsFromSelectionEdit extends ModifySelectionEdit {
		private RemoveObjectsFromSelectionEdit(Selection selection, Object[] objects, double[] weights, boolean applyNow) {
			super(selection, objects, weights);
			if (applyNow) {
				redo();
			}
		}

		@Override
		public void redo() {
			super.redo();
			remove();
		}

		@Override
		public void undo() {
			super.undo();
			add();
		}
	}
}
