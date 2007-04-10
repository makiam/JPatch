package jpatch.control;

import java.util.List;

import jpatch.control.edit2.JPatchUndoableEdit;
import jpatch.entity.ControlPoint;

/**
 * This class provides factory methods to manipulate ControlPoints.
 * It can not be instantiated.
 * 
 * All factory methods that create new edits provide a boolean changeNow flag.
 * If set to true, the change on the controlpoint is performed immediately.
 * Setting changeNow to false makes sense if the change has already been
 * applied and the edit is used to make the change undoable.
 * 
 * @author sascha
 * @see jpatch.entity.Attribute
 */

public class EditControlPoint {
	
	private EditControlPoint() { } 	// private default constructor makes sure this class can not be instantiated.
	
	/* * * * * * * * * * * * *
	 * static factory methods
	 * * * * * * * * * * * * */

	/**
	 * Facotory method that creates a new JPatchUndoableEdit which reverses a curve.
	 * Must be calles on the start ControlPoint of the curve.
	 * @param cp start ControlPoint of the curve to reverse
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit reverse(ControlPoint cp) {
		return new Reverse(cp);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates the nextCp of a ControlPoint.
	 * @param cp the ControlPoint to edit
	 * @param next the new nextCp for the ControlPoint
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeNext(ControlPoint cp, ControlPoint next) {
		return new Next(cp, next);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates the prevCp of a ControlPoint.
	 * @param cp the ControlPoint to edit
	 * @param prev the new prevCp for the ControlPoint
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changePrev(ControlPoint cp, ControlPoint prev) {
		return new Prev(cp, prev);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates the nextAttachedCp of a ControlPoint.
	 * @param cp the ControlPoint to edit
	 * @param nextAttached the new nextAttachedCp for the ControlPoint
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeNextAttached(ControlPoint cp, ControlPoint nextAttached) {
		return new NextAttached(cp, nextAttached);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates the prevAttachedCp of a ControlPoint.
	 * @param cp the ControlPoint to edit
	 * @param prevAttached the new prevAttachedCp for the ControlPoint
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changePrevAttached(ControlPoint cp, ControlPoint prevAttached) {
		return new PrevAttached(cp, prevAttached);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which manipulates the loop flag of a ControlPoint.
	 * @param cp the ControlPoint to edit
	 * @param loop the new loop flag for the ControlPoint
	 * @param changeNow set to true to apply the change now
	 * @return a new JPatchUndoableEdit object that encapsulates the specified modification
	 */
	public static JPatchUndoableEdit changeLoop(ControlPoint cp, boolean loop) {
		return new Loop(cp, loop);
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * private inner classes that implement the required JPatchUndoableEdits
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/*
	 * all of the following edits are "swapper" edits. They store a reference
	 * to the controlpoint to change and the changed value itself. On undo() or redo()
	 * they simply swap the current value of the controlpoint with
	 * the value stored in the edit (this is actually implemented in
	 * SwapperEdit, which is the superclass of all of the following edits).
	 * 
	 * All these edits provide a boolean changeNow flag in their constructor.
	 * If set to true, the change on the attribute is performed immediately.
	 * Setting changeNow to false makes sense if the change has already been
	 * applied and the edit is used to make the change undoable.
	 */
	
	
	
	/**
	 * JPatchUndoableEdit that modifies the nextCp of a ControlPoint
	 */
	private static final class Next extends SwapperEdit {
		ControlPoint cp;
		ControlPoint next;
		private Next(ControlPoint cp, ControlPoint next) {
			this.cp = cp;
			this.next = next;
			swap();
		}
		
		@Override
		protected void swap() {
			ControlPoint tmp = cp.getNext();
			cp.setNext(next);
			next = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies the prevCp of a ControlPoint
	 */
	private static final class Prev extends SwapperEdit {
		ControlPoint cp;
		ControlPoint prev;
		private Prev(ControlPoint cp, ControlPoint prev) {
			this.cp = cp;
			this.prev = prev;
			swap();
		}
		
		@Override
		protected void swap() {
			ControlPoint tmp = cp.getPrev();
			cp.setPrev(prev);
			prev = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies the nextAttachedCp of a ControlPoint
	 */
	private static final class NextAttached extends SwapperEdit {
		ControlPoint cp;
		ControlPoint nextAttached;
		private NextAttached(ControlPoint cp, ControlPoint nextAttached) {
			this.cp = cp;
			this.nextAttached = nextAttached;
			swap();
		}
		
		@Override
		protected void swap() {
			ControlPoint tmp = cp.getNextAttached();
			cp.setNextAttached(nextAttached);
			nextAttached = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies the prevAttachedCp of a ControlPoint
	 */
	private static final class PrevAttached extends SwapperEdit {
		ControlPoint cp;
		ControlPoint prevAttached;
		private PrevAttached(ControlPoint cp, ControlPoint prevAttached) {
			this.cp = cp;
			this.prevAttached = prevAttached;
			swap();
		}
		
		@Override
		protected void swap() {
			ControlPoint tmp = cp.getPrevAttached();
			cp.setPrevAttached(prevAttached);
			prevAttached = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies the loop flag of a ControlPoint
	 */
	private static final class Loop extends SwapperEdit {
		ControlPoint cp;
		boolean loop;
		private Loop(ControlPoint cp, boolean loop) {
			this.cp = cp;
			this.loop = loop;
			swap();
		}
		
		@Override
		protected void swap() {
			boolean tmp = cp.isLoop();
			cp.setLoop(loop);
			loop = tmp;
		}
	}
	
	/**
	 * JPatchUndoableEdit that modifies the loop flag of a ControlPoint
	 */
	private static final class Reverse extends SwapperEdit {
		ControlPoint cp;
		private Reverse(ControlPoint cp) {
			this.cp = cp;
			swap();
		}
		
		@Override
		protected void swap() {
			cp = cp.reverseCurve();
		}
	}
}
