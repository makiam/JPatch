package jpatch.control;

import java.util.List;

import jpatch.entity.ControlPoint;
import jpatch.entity.Attribute;

public class EditModel {

	private static final boolean DEBUG = true;
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which adds a curve to its model.
	 * The model is derived from the ControlPoint.
	 * @param curveStart the start ControlPoint of the curve to add.
	 */
	public static JPatchUndoableEdit addCurve(ControlPoint curveStart) {
		if (DEBUG) {
			System.out.println("EditModel.addCurve(" + curveStart + ")");
		}
		return new AddCurve(curveStart);
	}
	
	/**
	 * Facotory method that creates a new JPatchUndoableEdit which remove a curve from its model.
	 * The model is derived from the ControlPoint.
	 * @param curveStart the start ControlPoint of the curve to remove.
	 */
	public static JPatchUndoableEdit removeCurve(ControlPoint curveStart) {
		if (DEBUG) {
			System.out.println("EditModel.removeCurve(" + curveStart + ")");
		}
		return new RemoveCurve(curveStart);
	}
	
	public static void appendControlPoint(List<JPatchUndoableEdit> editList, ControlPoint a, ControlPoint b) {
		if (DEBUG) {
			System.out.println("EditModel.appendControlPoint(" + editList + ", " + a + ", " + b + ")");
		}
		assert a.isStart() || a.isEnd() : a + " is neither start nor end of curve.";
		assert b.isStart() || b.isEnd() : b + " is neither start nor end of curve.";
		assert !a.isLoop() : a + " is looped.";
		assert !b.isLoop() : b + " is looped.";
		assert a.isUnattached() || b.isUnattached() : "both points are attached.";
		if (!a.isStart()) {
			editList.add(EditControlPoint.reverse(a));	// ensure A is the start of the curve
		}
		if (!b.isEnd()) {
			editList.add(EditControlPoint.reverse(b));	// ensure B is the end of the curve
		}
		boolean loop = a.getStart() == b.getStart();	// if both points are on the same curve, a loop will be created
		assert !loop || a.getCurveLength() > 3 : a + " curve is too short to loop (length = " + a.getCurveLength() + ")";
				
		if (b.isUnattached()) {
			editList.add(EditAttribute.changeAttribute(
					a.position, b.position.x.get(),
					b.position.y.get(), b.position.z.get(), true));	// change A's position to B's	
			simpleAppendControlPoint(editList, a, b.getPrev()); 	// append A to B's previous point
			replaceControlPointInEntities(editList, b, a);			// replace B with A in all entities
			if (loop) {
				editList.add(EditControlPoint.changeLoop(a, true)); // set loop on A to true
			} else {
				editList.add(removeCurve(a));						// remove curve A from model
			}
		} else {
			simpleAppendControlPoint(editList, a.getNext(), b); 	// append A's next point to B
			replaceControlPointInEntities(editList, a, b);			// replace A with B in all entities
			if (loop) {
				editList.add(EditControlPoint.changeLoop(b, true)); // set loop on B to true
				editList.add(addCurve(b));							// add curve B to model
			}
			editList.add(removeCurve(a));							// remove curve A from model
		}
		test.GlTest.model.xml(System.out, ">");
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will simply append a ControlPoint to anotherone by modifying their cpNext and cpPrev
	 * values, respectivily.
	 * This is a private helper method intended to be used by appendControlPoint(List&lt;JPatchUndoableEdit&gt; editList, ControlPoint a, ControlPoint b)
	 * @param editList the List to add the edits to
	 * @param a the point to append to b (a must be a curve start and not looped)
	 * @param b the point a will be appended to (b must be a curve end)
	 */
	private static void simpleAppendControlPoint(List<JPatchUndoableEdit> editList, ControlPoint a, ControlPoint b) {
		if (DEBUG) {
			System.out.println("EditModel.simpleAppendControlPoint(" + editList + ", " + a + ", " + b + ")");
		}
//		assert a.isStart() : a + " is not a curve start";
//		assert !a.isLoop() : a + " is looped";
//		assert b.isEnd() : b + " is not a curve end";
		/* append A to B */
		System.out.println("before: b=" + b + " b.next=" + b.getNext());
		System.out.println("        a=" + a + " a.prev=" + a.getPrev());
		editList.add(EditControlPoint.changeNext(b, a));
		editList.add(EditControlPoint.changePrev(a, b));
		System.out.println("after: b=" + b + " b.next=" + b.getNext());
		System.out.println("        a=" + a + " a.prev=" + a.getPrev());
		b.getStart().xml(System.out, "\t");
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will attach a ControlPoint to anotherone by modifying their cpNext and cpPrev values.
	 * @param editList the List to add the edits to
	 * @param a the point to attach to b (a must be a head)
	 * @param b the point a will be attach to (b must be a tail)
	 */
	public static void attachControlPoint(List<JPatchUndoableEdit> editList, ControlPoint a, ControlPoint b) {
		if (DEBUG) {
			System.out.println("EditModel.attachControlPoint(" + editList + ", " + a + ", " + b + ")");
		}
		assert a.isHead() : a + " is not a head";
		assert b.isTail() : b + " is not a tail";
		/* attach A to B */
		editList.add(EditControlPoint.changeNextAttached(b, a));
		editList.add(EditControlPoint.changePrevAttached(a, b));
		replaceControlPointInEntities(editList, a, b.getHead());	// replace A with B's head in all entities
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will insert a ControlPoint after anotherone by modifying their cpNext and cpPrev values.
	 * @param editList the List to add the edits to
	 * @param prevCp the point after which a will be inserted
	 * @return a new ControlPoint, inserted after prevCp
	 */
	public static ControlPoint insertControlPoint(List<JPatchUndoableEdit> editList, ControlPoint prevCp) {
		if (DEBUG) {
			System.out.println("EditModel.insertControlPoint(" + editList + ", " + prevCp + ")");
		}
		ControlPoint newCp = new ControlPoint(prevCp.getModel());
		ControlPoint nextCp = prevCp.getNext();
		newCp.setNext(nextCp);
		newCp.setPrev(prevCp);
		editList.add(EditControlPoint.changeNext(prevCp, newCp));
		if (nextCp != null) {
			editList.add(EditControlPoint.changePrev(nextCp, newCp));
		}
		return newCp;
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will detach a ControlPoint. After the edit was applied, nextAttachedCp
	 * of the prevAttachedCp (if not null) will point to the nextAttachedCp of the specified
	 * (to be detached) ControlPoint and the prevAttachedCp of the nextAttachedCp (if not null)
	 * will point to the prevAttachedCp of the specified (to be detached) ControlPoint.
	 * @param editList the list to add the edits to
	 * @param cp the ControlPoint to detach
	 * @param changeNow true to apply the change now
	 */
	public static void detachControlPoint(List<JPatchUndoableEdit> editList, ControlPoint cp) {
		if (DEBUG) {
			System.out.println("EditModel.detachControlPoint(" + editList + ", " + cp + ")");
		}
		if (cp.getPrevAttached() != null) {
			/* set nextAttachedCp on prevAttachedCp (if prevAttachedCp != null) */
			editList.add(EditControlPoint.changeNextAttached(cp.getPrevAttached(), cp.getNextAttached()));
		}
		if (cp.getNextAttached() != null) {
			/* set prevAttachedCp on nextAttachedCp (if nextAttachedCp != null) */
			editList.add(EditControlPoint.changePrevAttached(cp.getNextAttached(), cp.getPrevAttached()));
		}
		// TODO remove patches containing this ControlPoint
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will detach each ControlPoint of the specified curve, remove each ControlPoint
	 * from all Entities (selections, patches, morphs) and finally remove the curve from
	 * the model.
	 * @param editList the list to add the edits to
	 * @param curveStart the first ControlPoint of the curve to delete
	 * @param changeNow true to apply the change now
	 */
	public static void deleteCurve(List<JPatchUndoableEdit> editList, ControlPoint curveStart, ControlPoint notToDetach) {
		if (DEBUG) {
			System.out.println("EditModel.deleteCurve(" + editList + ", " + curveStart + ", " + notToDetach + ")");
		}
		assert curveStart.isStart() : curveStart + " is not the start of a curve";
		ControlPoint cp = curveStart;
		do {
			if (cp != notToDetach) {							// detach all points of the curve
				detachControlPoint(editList, cp);				// except the one specified as
			}													// notToDetach (if any).
			removeControlPointFromEntities(editList, cp);		// remove ControlPoint from Entities.
			cp = cp.getNext();
		} while (cp != null && !cp.isLoop());
		editList.add(EditModel.removeCurve(curveStart));		// remote curve from model.
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * It must be called on a head (the first ControlPoint of a list of attached ControlPoints)
	 * and the edits will delete the specified and all attached ControlPoints.
	 * If any of the remaining curves becomes invalid (i.e. shorter than three ControlPoints or shorter than
	 * four ControlPoints if it is looped, respectively), the ControlPoints of the entire curve will be
	 * deleted and the curve will be removed from the model.
	 * Hooks preceding or following the specified ControlPoint will be converted into regular ControlPoints.
	 * @param editList the list to add the edits to.
	 * @param head the (head) ControlPoint to delete.
	 */ 
	public static void deleteHeadPoint(List<JPatchUndoableEdit> editList, ControlPoint head) {
		if (DEBUG) {
			System.out.println("EditModel.deleteHeadPoint(" + editList + ", " + head + ")");
		}
		assert head.isHead() : head + " is not a head.";
		for (ControlPoint cp = head; cp != null; cp = cp.getNextAttached()) {
			deleteSingleControlPointOrCurve(editList, cp, false);
		}
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will delete a single ControlPoint (i.e. it will not delete any ControlPoints the
	 * specified ControlPoint is attached to or any ControlPoints attached to the specified one).
	 * However, the specified ControlPoint will be detached (from prevAttached and/or nextAttached)
	 * ControlPoints.
	 * If the remaining curve becomes invalid (i.e. shorter than three ControlPoints or shorter than
	 * four ControlPoints if it is looped, respectively), the ControlPoints of the entire curve will be
	 * deleted and the curve will be removed from the model.
	 * Hooks preceding or following the specified ControlPoint will be converted into regular ControlPoints.
	 * @param editList the list to add the edits to.
	 * @param cp the ControlPoint to delete.
	 */
	public static void deleteSingleControlPoint(List<JPatchUndoableEdit> editList, ControlPoint cp) {
		if (DEBUG) {
			System.out.println("EditModel.deleteHeadPoint(" + editList + ", " + cp + ")");
		}
		deleteSingleControlPointOrCurve(editList, cp, true);
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * It first checks if the entire curve nees to be deleted. This is the case if the curve length < 3 or the curve length = 3
	 * and the curve is a loop or the curve length = 3 and the ControlPoint is the middel ControlPoint.
	 * If the curve has to be deleted,
	 * deleteCurve(List<JPatchUndoableEdit> editList, ControlPoint curveStart, ControlPoint notToDetach)
	 * is called.
	 * Otherwise, deleteSingleControlPointOnly(List<JPatchUndoableEdit> editList, ControlPoint cp)
	 * is called.
	 * The detach flag tells whether the specified ControlPoint should be detached or not. Note that even if this flag
	 * is set to false, all but this ContrlPoint will be detached if the entire curve is to be deleted.
	 * This method is a helper method intended to be called by either
	 * deleteHeadPoint(List<JPatchUndoableEdit> editList, ControlPoint head) or
	 * deleteSingleControlPointOrCurve(editList, cp, true).
	 * The former will want to set detach to false, the latter to true.
	 */
	private static void deleteSingleControlPointOrCurve(List<JPatchUndoableEdit> editList, ControlPoint cp, boolean detach) {
		if (DEBUG) {
			System.out.println("EditModel.deleteHeadPoint(" + editList + ", " + cp + ", " + detach + ")");
		}
		/*
		 * Check if the entire curve needs to be deleted. This is the case if the curve length < 3 or the curve length = 3
		 * and the curve is a loop or the curve length = 3 and the ControlPoint is the middel ControlPoint.
		 */
		ControlPoint start = cp.getStart();
		int curveLength = start.getCurveLength();
		if (curveLength < 3 || (curveLength == 3 && (start.isLoop() || (cp.getPrev() != null && cp.getNext() != null)))) {
			if (detach) {
				deleteCurve(editList, start, null);						// delete the entire curve
			} else {
				deleteCurve(editList, start, cp);						// don't detach this cp if not asked for
			}
		} else {
			deleteSingleControlPointOnly(editList, cp);					// delete the ControlPoint
			if (detach) {
				detachControlPoint(editList, cp);						// if asked for, detach the ControlPoint
			}
		}
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will delete a single ControlPoint. It does nothing else, in particular it
	 * does not delete attached ControlPoints of the specified ControlPoint and does not
	 * detach any ControlPoints.
	 * Here's what it does exactly:
	 * <ol>
	 * <li>If the specified ControlPoint or its previous ControlPoint is the start of a curve,
	 *     or if the curve is looped, the curve is removed from the model.</li>
	 * <li>If the curve is looped, the loop flag of the start ControlPoint is cleard.</li>
	 * <li>If the previous ControlPoint is the start of a curve, it is removed from all entities
	 *     (by calling removeControlPointFromEntities).</li>
	 * <li>Otherwise the curve on the previous ControlPoint is broken (nextCp of the previous cp is set to null).</li>
	 * <li>If the next ControlPoint is the end of the curve, it is removed from all entities
	 *     (by calling removeControlPointFromEntities).</li> 
	 * <li>Otherwise the curve on the next ControlPoint is broken (prevCp of the next cp is set to null) and
	 *     a new curve starting with the next ControlPoint is added to the model.</li>
	 * <li>The ControlPoint is removed from all entities by calling removeControlPointFromEntities.</li>
	 * <li>Prev and next ControlPoints are converted to non-hooks if they are hooks</li>
	 * </ol>
	 * This method is a helper method intended to be called by
	 * deleteSingleControlPointOrCurve(List<JPatchUndoableEdit> editList, ControlPoint cp, boolean detach).
	 * @param editList the list to add the edits to
	 * @param cp the ControlPoint to delete
	 */
	private static void deleteSingleControlPointOnly(List<JPatchUndoableEdit> editList, ControlPoint cp) {
		if (DEBUG) {
			System.out.println("EditModel.deleteHeadPoint(" + editList + ", " + cp + ")");
		}
		ControlPoint start = cp.getStart();
		ControlPoint prev = cp.getPrev();
		ControlPoint next = cp.getNext();
		if (cp.isStart() || start.isLoop() || prev == start) {
			editList.add(removeCurve(start));							// if cp or prev is start of curve or curve is looped, remove this curve
		}
		if (start.isLoop()) {
			editList.add(EditControlPoint.changeLoop(start, false));	// if curve is looped, clear loop flag on start of curve
		}
		if (prev != null) {
			if (prev.isStart()) {
				removeControlPointFromEntities(editList, prev);			// if prev is start of curve, remove it from entities.
			} else {
				editList.add(EditControlPoint.changeNext(prev, null));	// else break the curve (set nextCp of prevCp to null).
				if (prev.isHook()) {
					convertControlPointToNonHook(editList, prev);		// if prev is a hook, make it a non hook.
				}
			}
		}
		if (next != null) {
			if (next.isEnd()) {
				removeControlPointFromEntities(editList, next);			// if next cp is end of curve remove it from entities.
			} else {
				editList.add(EditControlPoint.changePrev(next, null));	// else break the curve (set prevCp of nextCp to null).
				editList.add(addCurve(next));							// and add new curve starting with next cp
				if (next.isHook()) {
					convertControlPointToNonHook(editList, next);		// if next is a hook, make it a non hook.
				}
			}
		}
		removeControlPointFromEntities(editList, cp);					// remove cp from entities
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will remove the specified ControlPoint from all entities that refer to it
	 * (Selections, Morphs and Patches).
	 * @param editList the list to add the edits to
	 * @param cp the ControlPoint to remove from all entities
	 */
	public static void removeControlPointFromEntities(List<JPatchUndoableEdit> editList, ControlPoint cp) {
		if (DEBUG) {
			System.out.println("EditModel.removeControlPointFromEntities(" + editList + ", " + cp + ")");
		}
		// TODO implement this edit!!!!
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will replace the specified "from" ControlPoint from all entities but patches
	 * (Selections and Morphs) that refer to it with the specified "to" ControlPoint.
	 * @param editList the list to add the edits to
	 * @param from the ControlPoint to be replaced
	 * @param to the replacement
	 */
	public static void replaceControlPointInEntities(List<JPatchUndoableEdit> editList, ControlPoint from, ControlPoint to) {
		if (DEBUG) {
			System.out.println("EditModel.replaceControlPointInEntities(" + editList + ", " + from + ", " + to + ")");
		}
		// TODO implement this edit!!!!
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * It must be called on a hook.
	 * The edits will convert the specified hook into a non-hook (i.e. set it's hookPos attribute to 0) and
	 * correct the hookPos values of all the directly preceding and following hooks.
	 * @param editList the list to add the edits to
	 * @param hook the hook to convert
	 */
	public static void convertControlPointToNonHook(List<JPatchUndoableEdit> editList, ControlPoint hook) {
		if (DEBUG) {
			System.out.println("EditModel.convertControlPointToNonHook(" + editList + ", " + hook + ")");
		}
		assert hook.isHook() : hook + " is not a hook.";
		double hookPos = hook.hookPos.get();
		double hookPos1 = 1 - hookPos;
		editList.add(EditAttribute.changeAttribute(hook.hookPos, 0, true));	// set hook's hookPos to 0 (making it a non hook)
		/* correct hookPos' of preceding hooks */
		for (ControlPoint cp = hook.getPrev(); cp != null && cp.isHook(); cp = cp.getPrev()) {
			editList.add(EditAttribute.changeAttribute(cp.hookPos, cp.hookPos.get() / hookPos, true));
		}
		/* correct hookPos' of following hooks */
		for (ControlPoint cp = hook.getNext(); cp != null && cp.isHook(); cp = cp.getNext()) {
			editList.add(EditAttribute.changeAttribute(cp.hookPos, (cp.hookPos.get() - hookPos) / hookPos1, true));
		}
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will weld a ControlPoint to another ControlPoint. If possible, the two ControlPoints will
	 * be appended, if not the two ControlPoints will be attached.
	 * @param editList the list to add the edits to
	 * @param cp the ControlPoint to weld to target
	 * @param target the ControlPoint cp will be welded to
	 */
	public static void weldControlPoint(List<JPatchUndoableEdit> editList, ControlPoint cp, ControlPoint target) {
		if (DEBUG) {
			System.out.println("EditModel.weldControlPoint(" + editList + ", " + cp + ", " + target + ")");
		}
		Attribute.Tuple3 cpPos = cp.getHead().position;
		Attribute.Tuple3 targetPos = target.getHead().position;
		ControlPoint cpEnd = getCurveEnd(cp.getHead(), targetPos.x.get(), targetPos.y.get(), targetPos.z.get());
		ControlPoint targetEnd = getCurveEnd(target.getHead(), cpPos.x.get(), cpPos.y.get(), cpPos.z.get());
		if (cpEnd != null && targetEnd != null && (cpEnd.isUnattached() || targetEnd.isUnattached())) {
			appendControlPoint(editList, cpEnd, targetEnd);
		} else {
			System.out.println("***attach***");
			attachControlPoint(editList, cp.getHead(), target.getTail());
		}
	}
	
	/**
	 * This method adds JPatchUndoableEdits to an editList (a list of JPatchUndoableEdits).
	 * The edits will create a new curve, welded to an existing one. If possible, the start of the new curve
	 * will be appended to the target curve, if not, the start of the new curve will be attached to the target
	 * curve.
	 * be appended, if not the two ControlPoints will be attached.
	 * @param editList the list to add the edits to
	 * @param cp the ControlPoint to weld to target
	 * @param target the ControlPoint cp will be welded to
	 */
	public static ControlPoint weldTo(List<JPatchUndoableEdit> editList, ControlPoint target, double x, double y, double z) {
		if (DEBUG) {
			System.out.println("EditModel.weldTo(" + editList + ", " + target + ", " + x + ", " + y + ", " + z + ")");
		}
		ControlPoint startCp = new ControlPoint(target.getModel());
		ControlPoint newCp = new ControlPoint(target.getModel());
		startCp.setNext(newCp);
		newCp.setPrev(startCp);
		newCp.position.set(x, y, z);
		editList.add(addCurve(startCp));
		weldControlPoint(editList, startCp, target);
		System.out.println("\treturning " + newCp);
		return newCp;
	}
	
	/**
	 * Returns the best curve end (or start), i.e. a ControlPoint that has a nextCp but no prevCp or has a prevCp but no nextCp,
	 * of the specified head point that points approximately into to the <u>point</u> specified in matchX, matchY, matchZ.
	 * @param head		the headpoint to search a curve end for
	 * @param matchX	the x coordinate of the point the curve should point at
	 * @param matchY	the x coordinate of the point the curve should point at
	 * @param matchZ	the x coordinate of the point the curve should point at
	 * @return the curve end (or start), null if none was found
	 */
	private static ControlPoint getCurveEnd(ControlPoint head, double matchX, double matchY, double matchZ) {
		assert head.isHead() : head + " is not a head.";
		Attribute.Tuple3 pos = head.position;
		double posX = pos.x.get();
		double posY = pos.y.get();
		double posZ = pos.z.get();
		matchX-= posX;
		matchX-= posY;						// matchX,matchY,matchZ is not the vector pointing from head's position to the
		matchX-= posZ;						// specified matchX,matchY,matchZ point						
		double testX, testY, testZ;
		double len = Math.sqrt(matchX * matchX + matchY * matchY + matchZ * matchZ);
		double dot;
		double error = Double.MAX_VALUE;
		ControlPoint curveEnd = null;
		matchX /= len;
		matchY /= len;
		matchZ /= len;						// normalize matchX,matchY,matchZ
		for (ControlPoint cp = head; cp != null; cp = cp.getNextAttached()) {
			ControlPoint next = cp.getNext();
			ControlPoint prev = cp.getPrev();
			if (next != null && prev == null) {
				Attribute.Tuple3 nPos = next.position;
				testX = posX - nPos.x.get();
				testY = posY - nPos.y.get();
				testZ = posZ - nPos.z.get();
			} else if (next == null && prev != null) {
				Attribute.Tuple3 pPos = prev.position;
				testX = posX - pPos.x.get();
				testY = posY - pPos.y.get();
				testZ = posZ - pPos.z.get();
			} else {
				continue;
			}
			len = Math.sqrt(testX * testX + testY * testY + testZ * testZ);
			dot = matchX * testX / len + matchY * testY / len + matchZ * testZ / len;	// dot product with norm(testX,testY,testZ)
			if (dot < error) {
				error = dot;
				curveEnd = cp;
			}
		}
		return curveEnd;
	}
	
	/**
	 * JPatchUndoableEdit that adds a curve to it's model.
	 */
	private static final class AddCurve extends AbstractUndoableEdit {
		ControlPoint curveStart;
		private AddCurve(ControlPoint curveStart) {
			this.curveStart = curveStart;
			applied = false;
			redo();
		}
		
		@Override
		public void undo() {
			super.undo();
			curveStart.getModel().removeCurve(curveStart);
		}
		
		@Override
		public void redo() {
			super.redo();
			curveStart.getModel().addCurve(curveStart);
		}
	}
	
	/**
	 * JPatchUndoableEdit that removes a curve from it's model.
	 */
	private static final class RemoveCurve extends AbstractUndoableEdit {
		ControlPoint curveStart;
		private RemoveCurve(ControlPoint curveStart) {
			this.curveStart = curveStart;
			applied = false;
			redo();
		}
		
		@Override
		public void undo() {
			super.undo();
			curveStart.getModel().addCurve(curveStart);
		}
		
		@Override
		public void redo() {
			super.redo();
			curveStart.getModel().removeCurve(curveStart);
		}
	}
}
