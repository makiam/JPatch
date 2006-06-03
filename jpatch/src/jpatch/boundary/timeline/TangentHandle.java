package jpatch.boundary.timeline;

import jpatch.control.edit.*;
import jpatch.entity.*;

public abstract class TangentHandle {
	public static enum Side { IN, OUT };
	MotionKey motionKey;
	Side side;

	private TangentHandle(MotionKey motionKey, Side side) {
		this.motionKey = motionKey;
		this.side = side;
	}
	
	public abstract void prepare();
	public abstract AtomicChangeMotionKey end();
	
	public static class Float extends TangentHandle {
		private float dfStart;
		
		public Float(MotionKey motionKey, Side side) {
			super(motionKey, side);
		}
		
		public float getValue() {
			switch (side) {
			case IN:
				return ((MotionKey.Float) motionKey).getFloat() - ((MotionKey.Float) motionKey).getDfIn();
			case OUT:
				if (motionKey.isSmooth())
					return ((MotionKey.Float) motionKey).getFloat() + ((MotionKey.Float) motionKey).getDfIn();
				else
					return ((MotionKey.Float) motionKey).getFloat() + ((MotionKey.Float) motionKey).getDfOut();
			}
			throw new IllegalStateException();
		}
		
		public void setValue(float df) {
			switch (side) {
			case IN:
				((MotionKey.Float) motionKey).setDfIn(-df + ((MotionKey.Float) motionKey).getFloat());
				return;
			case OUT:
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
			switch (side) {
			case IN:
				dfStart = ((MotionKey.Float) motionKey).getDfIn();
				return;
			case OUT:
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
			float dummy;
			switch (side) {
			case IN:
				dummy = ((MotionKey.Float) motionKey).getDfIn();
				((MotionKey.Float) motionKey).setDfIn(dfStart - ((MotionKey.Float) motionKey).getFloat());
				return new AtomicChangeMotionKey.DfIn(motionKey, dummy);
			case OUT:
				if (motionKey.isSmooth()) {
					dummy = ((MotionKey.Float) motionKey).getDfIn();
					((MotionKey.Float) motionKey).setDfIn(dfStart - ((MotionKey.Float) motionKey).getFloat());
					return new AtomicChangeMotionKey.DfIn(motionKey, dummy);
				} else {
					dummy = ((MotionKey.Float) motionKey).getDfOut();
					((MotionKey.Float) motionKey).setDfOut(dfStart - ((MotionKey.Float) motionKey).getFloat());
					return new AtomicChangeMotionKey.DfOut(motionKey, dummy);
				}
			}
			throw new IllegalStateException();
		}
	}
}
