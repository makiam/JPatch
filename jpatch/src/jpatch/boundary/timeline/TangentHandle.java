package jpatch.boundary.timeline;

import jpatch.control.edit.*;
import jpatch.entity.*;

/**
 * Encapsulates data needed to present and modify (MotionCurve) tangents
 * in the TrackView.
 */
public abstract class TangentHandle {
	/** incoming or outgoing tangent */
	public static enum Side { IN, OUT };
	/** the MotionKey */
	final MotionKey motionKey;
	/** the side (incoming/left or outgoing/right) */
	final Side side;

	/**
	 * Abstract superclass constructor 
	 */
	private TangentHandle(MotionKey motionKey, Side side) {
		this.motionKey = motionKey;
		this.side = side;
	}
	
	/**
	 * gets motionkey
	 * @return the MotionKey
	 */
	public MotionKey getMotionKey() {
		return motionKey;
	}

	/**
	 * gets side
	 * @return the Side
	 */
	public Side getSide() {
		return side;
	}
	
	/**
	 * Stores information for undoable edit at the begin of
	 * the modification (before the handle was moved).
	 */
	public abstract void prepare();
	
	/**
	 * Returns an UndoableEdit that encapsulates the tangent
	 * modification.
	 * @return a new AtomicChangeMotionKey object
	 */
	public abstract AtomicChangeMotionKey end();
	
	/**
	 * Returns the tangent handles "position" value
	 * @return the tangent handles "position" value
	 */
	public abstract float getValue();
	
	/**
	 * Sets the tangent handles "position" value
	 * @param the new "position" value
	 */
	public abstract void setValue(float value);
	
	/**
	 * Implementation of TangentHandle for float curves (MotionCurve.Float)
	 */
	public static class Float extends TangentHandle {
		/** to store the initial value (for undo/redo support) */
		private float dfStart;
		
		public Float(MotionKey motionKey, Side side) {
			super(motionKey, side);
		}
		
		@Override
		public float getValue() {
			
			/* treat IN and OUT tangents differently */
			switch (side) {
			case IN:
				return ((MotionKey.Float) motionKey).getFloat() - ((MotionKey.Float) motionKey).getDfIn();
			case OUT:
				
				/* if continuity is smooth, dfIn is used for IN and OUT tangents */
				if (motionKey.isSmooth())
					return ((MotionKey.Float) motionKey).getFloat() + ((MotionKey.Float) motionKey).getDfIn();
				else
					return ((MotionKey.Float) motionKey).getFloat() + ((MotionKey.Float) motionKey).getDfOut();
			}
			throw new IllegalStateException();
		}
		
		@Override
		public void setValue(float df) {
			
			/* treat IN and OUT tangents differently */
			switch (side) {
			case IN:
				((MotionKey.Float) motionKey).setDfIn(-df + ((MotionKey.Float) motionKey).getFloat());
				return;
			case OUT:
				
				/* if continuity is smooth, dfIn is used for IN and OUT tangents */
				if (motionKey.isSmooth())
					((MotionKey.Float) motionKey).setDfIn(df - ((MotionKey.Float) motionKey).getFloat());
				else
					((MotionKey.Float) motionKey).setDfOut(df - ((MotionKey.Float) motionKey).getFloat());
				return;
			}
			throw new IllegalStateException();
		}
		
		@Override
		public void prepare() {
			
			/* treat IN and OUT tangents differently */
			switch (side) {
			case IN:
				dfStart = ((MotionKey.Float) motionKey).getDfIn();
				return;
			case OUT:
				
				/* if continuity is smooth, dfIn is used for IN and OUT tangents */
				if (motionKey.isSmooth())
					dfStart = ((MotionKey.Float) motionKey).getDfIn();
				else
					dfStart = ((MotionKey.Float) motionKey).getDfOut();
				return;
			}
			throw new IllegalStateException();
		}
		
		@Override
		public AtomicChangeMotionKey end() {
			float dummy;	// used to store current value
			
			/* treat IN and OUT tangents differently */
			switch (side) {
			case IN:
				
				/* store current value */
				dummy = ((MotionKey.Float) motionKey).getDfIn();
				
				/* reset tangent to start value before applying the edit */
				((MotionKey.Float) motionKey).setDfIn(dfStart);
				
				/* apply edit */
				return new AtomicChangeMotionKey.DfIn(motionKey, dummy);
			case OUT:
				
				/* if continuity is smooth, dfIn is used for IN and OUT tangents */
				if (motionKey.isSmooth()) {
					
					/* store current value */
					dummy = ((MotionKey.Float) motionKey).getDfIn();
					
					/* reset tangent to start value before applying the edit */
					((MotionKey.Float) motionKey).setDfIn(dfStart);
					
					/* apply edit */
					return new AtomicChangeMotionKey.DfIn(motionKey, dummy);
				} else {
					
					/* store current value */
					dummy = ((MotionKey.Float) motionKey).getDfOut();
					
					/* reset tangent to start value before applying the edit */
					((MotionKey.Float) motionKey).setDfOut(dfStart - ((MotionKey.Float) motionKey).getFloat());
					
					/* apply edit */
					return new AtomicChangeMotionKey.DfOut(motionKey, dummy);
				}
			}
			throw new IllegalStateException();
		}
	}
}
