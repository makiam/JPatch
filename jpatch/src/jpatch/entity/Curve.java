/*
 * $Id$
 *
 * Copyright (c) 2004 Sascha Ledinsky
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
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jpatch.entity;

import jpatch.auxilary.*;

/**
 * A curve is a list of controlPoints. You can get the first ControlPoint in the list by calling
 * curve.getStart(). After that call controlPoint.getNext(), controlPoint.getNextCheckLoop() or
 * controlPoint.getNextCheckNextLoop().
 * The curves themselfs are stored in a list too - use model.getFirstCurve() to get the first curve
 * of a model and use curve.getNext() and curve.getPrev() to loop through all curves.
 *
 * @author     Sascha Ledinsky
 * @version    $Revision$
 * @see		jpatch.entity.Model
 * @see		jpatch.entity.ControlPoint
 */

public final class Curve {

	private static int iSequence = 0;
	
	/** The model this curve belongs to */
	private Model model;
	/** The first controlPoint of the curve */
	private ControlPoint cpStart;
	/** The next curve in the list */
	private Curve crvNext;
	/** The previois curve in the list */
	private Curve crvPrev;

	private int iNumber;
	
	public Curve() {
		iNumber = iSequence++;
	}
	/**
	 * constructor
	 *
	 * @param start 	The first controlPoint of the curve
	 */
	public Curve(ControlPoint start) {
		this();
		cpStart = start;
		//validate();
	}
	
	/**
	 * constructor
	 *
	 * @param start 	The first controlPoint of the curve
	 * @param model	The model the curve belongs to
	 */
	public Curve(ControlPoint start, Model model) {
		this();
		this.model = model;
		cpStart = start;
		//reset();
		//validate();
	}

	/**
	 * accessor method to get the start ControlPoint
	 *
	 * @return 	The first controlPoint of the curve
	 */
	public final ControlPoint getStart() {
		return cpStart;
	}

	/**
	 * accessor method to set the start ControlPoint
	 *
	 * @param start  The first controlPoint of the curve
	 */
	public final void setStart(ControlPoint start) {
		cpStart = start;
	}

	/**
	 * accessor method to get the Model
	 *
	 * @return 	The Model the curve belongs to
	 */
	public final Model getModel() {
		return model;
	}

	/**
	 * accessor method to set the Model
	 *
	 * @param model	The Model the curve belongs to
	 */
	public final void setModel(Model model) {
		this.model = model;
	}

	/**
	 * accessor method to get the next curve
	 *
	 * @return  The next curve
	 */
	public final Curve getNext() {
		return crvNext;
	}

	/**
	 * accessor method to set the next curve
	 *
	 * @param next  The next curve
	 */
	public final void setNext(Curve next) {
		crvNext = next;
	}

	/**
	 * accessor method to get the previous curve
	 *
	 * @return  The previous curve
	 */
	public final Curve getPrev() {
		return crvPrev;
	}

	/**
	 * accessor method to set the previous curve
	 *
	 * @param prev  The previous curve
	 */
	public final void setPrev(Curve prev) {
		crvPrev = prev;
	}

	/**
	 * removes this curve from the list of curves
	 */
	public final void remove() {
		if (crvPrev != null) {
			crvPrev.crvNext = crvNext;
		}
		if (crvNext != null) {
			crvNext.crvPrev = crvPrev;
		}
		if (model != null && model.getLastCurve() == this) {
			model.setLastCurve(crvPrev);
		}
		if (model != null && model.getFirstCurve() == this) {
			model.setFirstCurve(crvNext);
		}
		model = null;
	}

	/**
	 * returns the lenght of the curve
	 *
	 * @return 	the lenght of this curve
	 */
	public final int getLength() {
		int iCount = 0;
		for (ControlPoint cp = cpStart; cp != null; cp = cp.getNextCheckNextLoop()) {
			iCount++;
		}
		return iCount;
	}

	/**
	 * returns true if the curve is closed
	 *
	 * @return 	true if this curve is closed
	 */
	public final boolean isClosed() {
		return cpStart.getLoop();
	}
	
	/**
	 * returns an array containing all ControlPoints of this curve
	 */
	public final ControlPoint[] getControlPointArray() {
		int points = getLength();
		ControlPoint[] acp = new ControlPoint[points];
		int n = 0;
		for (ControlPoint cp = cpStart; cp != null; cp = cp.getNextCheckNextLoop()) {
			acp[n++] = cp;
		}
		return acp;
	}

//	/**
//	 * reverses the controlPoint order
//	 */
//	public final void reverse() {
//		ControlPoint cp;
//		ControlPoint cpDummy;
//		for (cp = cpStart.getEnd(); cp != null; cp = cp.getNext()) {
//			/*
//			 * if it's on a hook curve, reverse the hookPos
//			 */
//			if (cp.getHookPos() != -1) {
//				cp.setHookPos(1 - cp.getHookPos());
//			}
//			/*
//			 * if we've got a child hook, reverse child hook curve and move childhook
//			 */
//			if (cp.getChildHook() != null) {
//				//System.out.println("cp   : " + cp);
//				//System.out.println("next : " + cp.getNext());
//				//System.out.println("child: " + cp.getChildHook());
//				cp.getNext().setChildHook(cp.getChildHook().getEnd());
//				cp.getChildHook().getCurve().reverse();
//				cp.setChildHook(null);
//			}
//			/*
//			 * swap cpNext <-> cpPrev
//			 */
//			cpDummy = cp.getNext();
//			cp.setNext(cp.getPrev());
//			cp.setPrev(cpDummy);
//			cp.setTangentsValid(false);
//		}
//		cpStart = cpStart.getStart();
//	}

	/**
	 * sets the curve attribute of all ControlPoints in this curve to this curve
	 */
	public void validate() {
		for (ControlPoint cp = cpStart; cp != null; cp = cp.getNextCheckNextLoop()) {
			cp.setCurve(this);
		}
	}

	/**
	 * returns a string representation of the curve, for debugging purposes only
	 *
	 * @return a string representation of this curve
	 */
	public String toString() {
		//String result = "";
		//for (ControlPoint cp = cpStart; cp != null; cp = cp.getNext()) {
		//	result += cp + " " + cp.getNext() + " " + cp.getPrev() + "\n";
		//}
		//return result;
		return "" + iNumber;
	}

	/**
	 * returns StringBuffer containing an XML representation of the curve, used to save
	 * models in XML format
	 *
	 * @return A StringBuffer containing an XML representation of this curve
	 */
	public StringBuffer xml(int tabs) {
		StringBuffer sbIndent = XMLutils.indent(tabs);
		StringBuffer sbLineBreak = XMLutils.lineBreak();
		StringBuffer sb = new StringBuffer();
		if (isClosed()) {
			sb.append(sbIndent).append("<curve closed=\"true\">").append(sbLineBreak);
		} else {
			sb.append(sbIndent).append("<curve>").append(sbLineBreak);
		}
		for (ControlPoint cp = getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
			sb.append(cp.xml(tabs + 1));
		}
		sb.append(sbIndent).append("</curve>").append(sbLineBreak);
		return sb;
	}
}

