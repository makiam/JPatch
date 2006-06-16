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

/**
 * @author sascha
 *
 */
public class MotionKeyNew implements Cloneable, Comparable {
	public static enum Interpolation { DISCRETE, LINEAR, CUBIC };
	public static enum TangentMode { AUTO, OVERSHOOT, MANUAL };
	
	Interpolation interpolation = Interpolation.CUBIC;
	TangentMode tangentMode;
	boolean smooth = true;
	double position;
	MotionCurveNew motionCurve;
	double value;
	double inTangent;
	double outTangent;
	
	public double getInTangent() {
		return inTangent;
	}
	
	public void setInTangent(double inTangent) {
		this.inTangent = inTangent;
	}
	
	public Interpolation getInterpolation() {
		return interpolation;
	}
	
	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
	}
	
	public MotionCurveNew getMotionCurve() {
		return motionCurve;
	}
	
	public void setMotionCurve(MotionCurveNew motionCurve) {
		this.motionCurve = motionCurve;
	}
	
	public double getOutTangent() {
		return outTangent;
	}
	
	public void setOutTangent(double outTangent) {
		this.outTangent = outTangent;
	}
	
	public double getPosition() {
		return position;
	}
	
	public void setPosition(double position) {
		this.position = position;
	}
	
	public boolean isSmooth() {
		return smooth;
	}
	
	public void setSmooth(boolean smooth) {
		this.smooth = smooth;
	}
	
	public TangentMode getTangentMode() {
		return tangentMode;
	}
	
	public void setTangentMode(TangentMode tangentMode) {
		this.tangentMode = tangentMode;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	/**
	 * Make a copy of this MotionKey
	 * @return a field-by-field copy of this MotionKey
	 */
	public MotionKeyNew copy() {
		try {
			return (MotionKeyNew) clone();
		} catch (CloneNotSupportedException e) {
			/* this should not happen */
			throw new RuntimeException(e);
		}
	}

	public void xml(PrintStream out) {
		out.append("<key position=\"").append(java.lang.Double.toString(position)).append("\"");
		out.append(" value=\"").append(java.lang.Double.toString(value)).append("\"");
		out.append(" interpolation=\"").append(interpolation.toString().toLowerCase()).append("\"");
		if (interpolation == Interpolation.CUBIC) {
			out.append(" tangentMode=\"").append(tangentMode.toString().toLowerCase()).append("\"");
			if (tangentMode == TangentMode.MANUAL) {
				if (smooth) {
					out.append(" tangent=\"").append(java.lang.Double.toString(inTangent)).append("\"");
				} else {
					out.append(" inTangent=\"").append(java.lang.Double.toString(inTangent)).append("\"");
					out.append(" outTangent=\"").append(java.lang.Double.toString(outTangent)).append("\"");
				}
			}
		}
		out.append("/>");
		out.println();
	}
	
	@Override
	public int hashCode() {
		return Float.floatToIntBits((float) position) | motionCurve.hashCode(); 
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MotionKeyNew))
			return false;
		MotionKeyNew motionKey = (MotionKeyNew) o;
		return position == motionKey.position && motionCurve == motionKey.motionCurve;
	}
	
	public int compareTo(Object o) {
		return Double.compare(position, ((MotionKeyNew) o).position);
	}
	
}
