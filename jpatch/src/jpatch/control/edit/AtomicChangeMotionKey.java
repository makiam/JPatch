/*
 * $Id$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.control.edit;

import javax.vecmath.*;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public abstract class AtomicChangeMotionKey extends JPatchAtomicEdit {
	MotionKey motionKey;
	
	abstract void swap();
	
	public void undo() {
		swap();
	}

	public void redo() {
		swap();
	}

	public int sizeOf() {
		return 8 + 8;
	}

	public static final class Interpolation extends AtomicChangeMotionKey {
		private MotionKey.Interpolation interpolation;
		
		public Interpolation(MotionKey motionKey, MotionKey.Interpolation interpolation) {
			this.motionKey = motionKey;
			this.interpolation = interpolation;
			swap();
		}
		
		void swap() {
			MotionKey.Interpolation dummy = motionKey.getInterpolation();
			motionKey.setInterpolation(interpolation);
			interpolation = dummy;
		}
	}
	
	public static final class TangentMode extends AtomicChangeMotionKey {
		private MotionKey.TangentMode tangentMode;
		
		public TangentMode(MotionKey motionKey, MotionKey.TangentMode tangentMode) {
			this.motionKey = motionKey;
			this.tangentMode = tangentMode;
			swap();
		}
		
		void swap() {
			MotionKey.TangentMode dummy = motionKey.getTangentMode();
			motionKey.setTangentMode(tangentMode);
			tangentMode = dummy;
		}
	}
	
	public static final class Smooth extends AtomicChangeMotionKey {
		private boolean smooth;
		
		public Smooth(MotionKey motionKey, boolean smooth) {
			this.motionKey = motionKey;
			this.smooth = smooth;
			swap();
		}
		
		void swap() {
			boolean dummy = motionKey.isSmooth();
			motionKey.setSmooth(smooth);
			smooth = dummy;
		}
	}
	
	public static final class DfIn extends AtomicChangeMotionKey {
		private float dfIn;
		
		public DfIn(MotionKey motionKey, float dfIn) {
			if (!(motionKey instanceof MotionKey.Float))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Float, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dfIn = dfIn;
			swap();
		}
		
		void swap() {
			float dummy = ((MotionKey.Float) motionKey).getDfIn();
			((MotionKey.Float) motionKey).setDfIn(dfIn);
			dfIn = dummy;
		}
	}
	
	public static final class DfOut extends AtomicChangeMotionKey {
		private float dfOut;
		
		public DfOut(MotionKey motionKey, float dfOut) {
			if (!(motionKey instanceof MotionKey.Float))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Float, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dfOut = dfOut;
			swap();
		}
		
		void swap() {
			float dummy = ((MotionKey.Float) motionKey).getDfOut();
			((MotionKey.Float) motionKey).setDfOut(dfOut);
			dfOut = dummy;
		}
	}
	
	public static final class DpIn extends AtomicChangeMotionKey {
		private Point3d dpIn;
		
		public DpIn(MotionKey motionKey, Point3d dpIn) {
			if (!(motionKey instanceof MotionKey.Point3d))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Point3d, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dpIn = new Point3d(dpIn);
			swap();
		}
		
		void swap() {
			Point3d dummy = new Point3d(((MotionKey.Point3d) motionKey).getDpIn());
			((MotionKey.Point3d) motionKey).setDpIn(dpIn);
			dpIn = dummy;
		}
	}
	
	public static final class DpOut extends AtomicChangeMotionKey {
		private Point3d dpOut;
		
		public DpOut(MotionKey motionKey, Point3d dpOut) {
			if (!(motionKey instanceof MotionKey.Point3d))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Point3d, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dpOut = new Point3d(dpOut);
			swap();
		}
		
		void swap() {
			Point3d dummy = new Point3d(((MotionKey.Point3d) motionKey).getDpOut());
			((MotionKey.Point3d) motionKey).setDpOut(dpOut);
			dpOut = dummy;
		}
	}
	
	public static final class DcIn extends AtomicChangeMotionKey {
		private Color3f dcIn;
		
		public DcIn(MotionKey motionKey, Color3f dcIn) {
			if (!(motionKey instanceof MotionKey.Color3f))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Color3f, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dcIn = new Color3f(dcIn);
			swap();
		}
		
		void swap() {
			Color3f dummy = new Color3f(((MotionKey.Color3f) motionKey).getDcIn());
			((MotionKey.Color3f) motionKey).setDcIn(dcIn);
			dcIn = dummy;
		}
	}
	
	public static final class DcOut extends AtomicChangeMotionKey {
		private Color3f dcOut;
		
		public DcOut(MotionKey motionKey, Color3f dcOut) {
			if (!(motionKey instanceof MotionKey.Color3f))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Color3f, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dcOut = new Color3f(dcOut);
			swap();
		}
		
		void swap() {
			Color3f dummy = new Color3f(((MotionKey.Color3f) motionKey).getDcOut());
			((MotionKey.Color3f) motionKey).setDcOut(dcOut);
			dcOut = dummy;
		}
	}
	
	public static final class DqIn extends AtomicChangeMotionKey {
		private Quat4f dqIn;
		
		public DqIn(MotionKey motionKey, Quat4f dqIn) {
			if (!(motionKey instanceof MotionKey.Quat4f))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Quat4f, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dqIn = new Quat4f(dqIn);
			swap();
		}
		
		void swap() {
			Quat4f dummy = new Quat4f(((MotionKey.Quat4f) motionKey).getDqIn());
			((MotionKey.Quat4f) motionKey).setDqIn(dqIn);
			dqIn = dummy;
		}
	}
	
	public static final class DqOut extends AtomicChangeMotionKey {
		private Quat4f dqOut;
		
		public DqOut(MotionKey motionKey, Quat4f dqOut) {
			if (!(motionKey instanceof MotionKey.Quat4f))
				throw new IllegalArgumentException("MotionKey must be of type MotionKey.Quat4f, but is " + motionKey.getClass().getName());
			this.motionKey = motionKey;
			this.dqOut = new Quat4f(dqOut);
			swap();
		}
		
		void swap() {
			Quat4f dummy = new Quat4f(((MotionKey.Quat4f) motionKey).getDqOut());
			((MotionKey.Quat4f) motionKey).setDqOut(dqOut);
			dqOut = dummy;
		}
	}
}
