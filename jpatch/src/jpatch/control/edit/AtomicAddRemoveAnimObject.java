package jpatch.control.edit;

import jpatch.boundary.*;
import jpatch.entity.*;

/**
 * This edit adds or removes an AnimObject (Camera, AnimLight or AnimModel) from the animation.
 * @author sascha
 */

public final class AtomicAddRemoveAnimObject extends JPatchAtomicEdit implements JPatchRootEdit {

	/** the AnimObject to add/remove */
	private final AnimObject animObject;
	/** the AnimObject's MotionCurveSet */
	private MotionCurveSet motionCurveSet;
	/** flag indicating if the AnimObject should be added (false) or removed (true) */
	private final boolean remove;
	
	/**
	 * Constructor
	 * @param animObject the AnimObject to remove
	 * @param remove true if the AnimObjet should be removed, false if it should be added
	 * @throws NullPointerException if AnimObject is null or if no MotionCurveSet for the AnimObject was found.
	 */
	public AtomicAddRemoveAnimObject(AnimObject animObject, boolean remove) {
		/*
		 * initialize fields
		 */
		this.remove = remove;
		this.animObject = animObject;
		if (remove)
			motionCurveSet = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
		
		/*
		 * check for null
		 */
		if (animObject == null)
			throw new NullPointerException();
		if (remove && motionCurveSet == null)
			throw new NullPointerException();
		
		/*
		 * perform operation
		 */
		redo();
	}
	
	public String getName() {
		if (animObject instanceof Camera)
			return "add camera";
		else if (animObject instanceof AnimLight)
			return "add lightsoure";
		else if (animObject instanceof AnimModel)
			return "add model";
		else
			throw new IllegalStateException();
	}

	public void undo() {
		if (remove)
			add();
		else
			remove();
	}

	public void redo() {
		if (remove)
			remove();
		else
			add();
	}
	
	public int sizeOf() {
		return 8 + 4 + 4 + 1;
	}

	/**
	 * removes the AnimObject from the Animation
	 */
	private void remove() {
		if (animObject instanceof Camera)
			MainFrame.getInstance().getAnimation().removeCamera((Camera) animObject);
		else if (animObject instanceof AnimLight)
			MainFrame.getInstance().getAnimation().removeLight((AnimLight) animObject);
		else if (animObject instanceof AnimModel)
			MainFrame.getInstance().getAnimation().removeModel((AnimModel) animObject);
		else
			throw new IllegalStateException();
	}

	/**
	 * adds the AnimObject from the Animation
	 */
	private void add() {
		if (animObject instanceof Camera)
			MainFrame.getInstance().getAnimation().addCamera((Camera) animObject, motionCurveSet);
		else if (animObject instanceof AnimLight)
			MainFrame.getInstance().getAnimation().addLight((AnimLight) animObject, motionCurveSet);
		else if (animObject instanceof AnimModel)
			MainFrame.getInstance().getAnimation().addModel((AnimModel) animObject, motionCurveSet);
		else
			throw new IllegalStateException();
		if (motionCurveSet == null)
			motionCurveSet = MainFrame.getInstance().getAnimation().getCurvesetFor(animObject);
	}
}
