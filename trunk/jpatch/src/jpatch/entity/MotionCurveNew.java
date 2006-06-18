/*
 * $Id:$
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
package jpatch.entity;

import java.io.PrintStream;
import java.util.*;

import jpatch.entity.MotionKey.TangentMode;

public class MotionCurveNew {
	private List<MotionKeyNew> list = new ArrayList<MotionKeyNew>();
	private Attribute target;
	private int channel;
	private MotionKeyNew searchKey = new MotionKeyNew();
	
	public MotionCurveNew(Attribute target, int channel) {
		this.target = target;
		this.channel = channel;
		searchKey.setMotionCurve(this);
	}
	
	public int getIndexOf(MotionKeyNew motionKey) {
		return Collections.<MotionKeyNew>binarySearch(list, motionKey);
	}
	
	public void removeKey(MotionKeyNew motionKey) {
		int index = getIndexOf(motionKey);
		if (index < 0)
			throw new IllegalArgumentException("Key " + motionKey + " is not on curve " + this);
		list.remove(index);
	}
	
	public void addKey(MotionKeyNew motionKey) {
		int index = getIndexOf(motionKey);
		if (index >= 0)
			throw new IllegalArgumentException("Key " + motionKey + " is already on curve " + this);
		list.add(1 - index, motionKey);
		motionKey.setMotionCurve(this);
	}
	
	public int getIndexAt(double position) {
		searchKey.setPosition(position);
		return 1 - getIndexOf(searchKey);
	}
	
	public double getStart() {
		return ((MotionKeyNew) list.get(0)).getPosition();
	}
	
	public double getEnd() {
		return ((MotionKeyNew) list.get(list.size() - 1)).getPosition();
	}
	
	public double getValueAt(double position) {
		
		/* if outside of range, return value of first or last key, respectively */
		if (position <= getStart()) return ((MotionKeyNew) list.get(0)).getValue();
		if (position >= getEnd()) return ((MotionKeyNew) list.get(list.size() - 1)).getValue();
		
		/* binary search for keys */
		int index = getIndexAt(position);
		MotionKeyNew k0 = (MotionKeyNew) list.get(index);		// the key before position
		MotionKeyNew k1 = (MotionKeyNew) list.get(index + 1);	// the key after position
		
		/* compute some values needed for interpolation */
		double l = k1.getPosition() - k0.getPosition();		// the distance (in frames) between the two keys
		double t = (position - k0.getPosition()) / l;		// relative position: 0 = at k0, 1 = at k1, 0..1 inbetween
		double p0 = k0.getValue();							// value at k0
		double p1 = k1.getValue();							// value at k1
		
		/* switch by interpolation method */
		switch (k0.getInterpolation()) {
		case DISCRETE:
			return p0;										// return k0's value
		case LINEAR:
			return (p0 * (1 - t) + p1 * t);					// return linear interpolation
		case CUBIC:
			double m0 = k0.getOutTangent() * l;				// get derivative at k0
			double m1 = k1.getInTangent() * l;				// get derivative at k1
			double t2 = t * t;								// precompute t² and t³ to
			double t3 = t2 * t;								// save a few multiplications
			return (2 * t3 - 3 * t2 + 1) * p0 +
				   (t3 - 2 * t2 + t) * m0 +
				   (-2 * t3 + 3 * t2) * p1 +
				   (t3 - t2) * m1;							// return cubic interpolation
		}
		throw new IllegalStateException();
	}
	
	public void computeDerivatives(int keyIndex) {
		MotionKeyNew key = list.get(keyIndex);
		
		/* if tangent mode is manual, do nothing and return */
		if (key.getTangentMode() == MotionKeyNew.TangentMode.MANUAL)
			return;
		
		/* for the first and last key, return 0 tangent */
		if ((keyIndex == 0) || (keyIndex == list.size() - 1)) {
			key.setInTangent(0);
			key.setOutTangent(0);
			return;
		}
		
		MotionKeyNew prevKey = (MotionKeyNew) list.get(keyIndex - 1);
		MotionKeyNew nextKey = (MotionKeyNew) list.get(keyIndex + 1);
		double vp = prevKey.getValue();
		double v = key.getValue();
		double vn = nextKey.getValue();
		
		/* compute tangent */
		double d = 0;
		double p = key.getPosition();
		double pn = nextKey.getPosition();
		double pp = prevKey.getPosition();
		if (prevKey.interpolation == MotionKeyNew.Interpolation.DISCRETE)
			d = 0;
		else if (prevKey.interpolation == MotionKeyNew.Interpolation.LINEAR)
			d = (v - vp) / (key.getPosition() - prevKey.getPosition());
		else if (key.interpolation == MotionKeyNew.Interpolation.DISCRETE)
			d = 0;
		else if (key.interpolation == MotionKeyNew.Interpolation.LINEAR)
			d = (vn - v) / (pn - p);
		else
			d = (vn - vp) / (pn - pp);
		
		/* apply overshoot limitation */
		if (key.getTangentMode() == MotionKeyNew.TangentMode.OVERSHOOT) {
			if (( v < vp && v < vn) || (v > vp && v > vn)) {
				d = 0;
			} else if (d > 0) {
				if (d * (pn - p) / 3 > (vn - v))
					d = (vn - v) / (pn - p) * 3;
				if (d * (p - pp) / 3 > (v - vp))
					d = (v - vp) / (p - pp) * 3;
			} else if (d < 0) {
				if (d * (pn - p) / 3 < (vn - v))
					d = (vn - v) / (pn - p) * 3;
				if (d * (p - pp) / 3 < (v - vp))
					d = (v - vp) / (p - pp) * 3;
			}
		}
		key.setInTangent(d);
		key.setOutTangent(d);
		return;
	}
	
	public void xml(PrintStream out, String prefix, String type) {
		out.append(prefix).append("<motioncurve>\n");
		for (MotionKeyNew key : list) {
			out.append(prefix).append("\t");
			key.xml(out);
		}
		out.append(prefix).append("</motioncurve>").append("\n");
	}
	
	static private double cubicInterpolate(double p0, double m0, double p1, double m1, double t) {
		double t2 = t * t;
		double t3 = t * t2;
		double H0 = 2 * t3 - 3 * t2 + 1;
		double H1 = t3 - 2 * t2 + t;
		double H2 = -2 * t3 + 3 * t2;
		double H3 = t3 - t2;
		return H0 * p0 + H1 * m0 + H2 * p1 + H3 * m1;
	}
}
