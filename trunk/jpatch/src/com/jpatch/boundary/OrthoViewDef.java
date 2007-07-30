package com.jpatch.boundary;

import java.util.*;
import javax.vecmath.*;
import com.jpatch.afw.attributes.*;

public class OrthoViewDef extends AbstractViewDef {
	private Matrix4d matrix = new Matrix4d();
	private Matrix4d inverseMatrix = new Matrix4d();
	private Tuple2Attr translationAttr = new Tuple2Attr();
	private Tuple2Attr rotationAttr = new Tuple2Attr();
	private DoubleAttr scaleAttr = new DoubleAttr();
	private Map<Viewport, State> stateMap = new HashMap<Viewport, State>();
	private final State initialState;
	private State state;
	
	public static final OrthoViewDef BIRDS_EYE = createBirdsEyeViewDef();
	
	public static OrthoViewDef createBirdsEyeViewDef() {
		final OrthoViewDef viewDef = new OrthoViewDef(new State(0, 0, 45, 45, 1));
		AttributePostChangeListener listener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				viewDef.computeMatrix();
			}
		};
		viewDef.translationAttr.addAttributePostChangeListener(listener);
		viewDef.rotationAttr.addAttributePostChangeListener(listener);
		viewDef.scaleAttr.addAttributePostChangeListener(listener);
		return viewDef;
	}
	
	public static OrthoViewDef createFixedViewDef(double rotationX, double rotationY) {
		final OrthoViewDef viewDef = new OrthoViewDef(new State(0, 0, rotationX, rotationY, 1));
		AttributePostChangeListener listener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				viewDef.computeMatrix();
			}
		};
		AttributePostChangeListener rotateListener = new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				viewDef.viewport.getViewTypeAttribute().setValue(BIRDS_EYE);
				viewDef.computeMatrix();
			}
		};
		viewDef.translationAttr.addAttributePostChangeListener(listener);
		viewDef.rotationAttr.addAttributePostChangeListener(rotateListener);
		viewDef.scaleAttr.addAttributePostChangeListener(listener);
		return viewDef;
	}
	
	private OrthoViewDef(State initialState) {
		this.initialState = initialState;
	}
	
	@Override
	public void setViewport(Viewport viewport) {
		super.setViewport(viewport);
		State state = stateMap.get(viewport);
		if (state == null) {
			state = new State(initialState);
		}
		state.set(this);
	}
	
	public Tuple2Attr getTranslationAttribute() {
		return translationAttr;
	}
	
	public Tuple2Attr getRotationAttribute() {
		return rotationAttr;
	}
	
	public DoubleAttr getScaleAttribute() {
		return scaleAttr;
	}

	public Matrix4d getMatrix(Matrix4d matrix) {
		matrix.set(this.matrix);
		return matrix;
	}
	
	public Matrix4d getInverseMatrix(Matrix4d inverseMatrix) {
		inverseMatrix.set(this.inverseMatrix);
		return inverseMatrix;
	}
	
	
	private void setState(State state) {
		this.state = state;
		translationAttr.setTuple(state.translateX, state.translateY);
		rotationAttr.setTuple(state.rotateX, state.rotateY);
		scaleAttr.setDouble(state.scale);
	}
	
	private class TranslateListener implements AttributePostChangeListener {
		public void attributeHasChanged(Attribute source) {
			state.translateX = translationAttr.getX();
			state.translateY = translationAttr.getY();
			state.computeMatrix(OrthoViewDef.this);
		}
	}
	
	private class RotateListener implements AttributePostChangeListener {
		public void attributeHasChanged(Attribute source) {
			state.rotateX = rotationAttr.getX();
			state.rotateY = rotationAttr.getY();
			state.computeMatrix(OrthoViewDef.this);
		}
	}
	
	private class NoRotateListener implements AttributePostChangeListener {
		public void attributeHasChanged(Attribute source) {
			BIRDS_EYE.stateMap.get(key)
			state.rotateX = rotationAttr.getX();
			state.rotateY = rotationAttr.getY();
			state.computeMatrix(OrthoViewDef.this);
		}
	}
	
	private class ScaleListener implements AttributePostChangeListener {
		public void attributeHasChanged(Attribute source) {
			state.scale = scaleAttr.getDouble();
			state.computeMatrix(OrthoViewDef.this);
		}
	}
	private static class State {
		private double translateX, translateY, rotateX, rotateY, scale;
		
		State(double translateX, double translateY, double rotateX, double rotateY, double scale) {
			this.translateX = translateX;
			this.translateY = translateY;
			this.rotateX = rotateX;
			this.rotateY = rotateY;
			this.scale = scale;
		}
		
		State(State state) {
			this.translateX = state.translateX;
			this.translateY = state.translateY;
			this.rotateX = state.rotateX;
			this.rotateY = state.rotateY;
			this.scale = state.scale;
		}
		
		void setViewDefAttributes(OrthoViewDef orthoViewDef) {
			orthoViewDef.getTranslationAttribute().setTuple(translateX, translateY);
			orthoViewDef.getRotationAttribute().setTuple(rotateX, rotateY);
			orthoViewDef.getScaleAttribute().setDouble(scale);
		}
		
		private void computeMatrix(OrthoViewDef viewDef) {
			double viewScale = scale / 20 * viewDef.viewport.getComponent().getWidth();
			double x = Math.toRadians(rotateX);
			double y = Math.toRadians(rotateY);
			double sx = Math.sin(x);
			double cx = Math.cos(x);
			double sy = Math.sin(y);
			double cy = Math.cos(y);
			
			viewDef.matrix.m00 = cy * viewScale;
			viewDef.matrix.m01 = 0;
			viewDef.matrix.m02 = -sy * viewScale;
			viewDef.matrix.m03 = 0;
			viewDef.matrix.m10 = sy * sx * viewScale;
			viewDef.matrix.m11 = cx * viewScale;
			viewDef.matrix.m12 = cy * sx * viewScale;
			viewDef.matrix.m20 = sy * cx * viewScale;
			viewDef.matrix.m21 = -sx * viewScale;
			viewDef.matrix.m22 = cy * cx * viewScale;
			viewDef.matrix.m03 = translateX * viewScale;
			viewDef.matrix.m13 = translateY * viewScale;
			viewDef.matrix.m23 = 0;
			
			viewDef.inverseMatrix.invert(viewDef.matrix);
		}
	}
}
