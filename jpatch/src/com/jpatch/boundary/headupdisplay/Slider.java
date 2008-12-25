package com.jpatch.boundary.headupdisplay;

import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.*;

import jpatch.boundary.mouse.*;
import static javax.media.opengl.GL.*;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.ui.*;

public class Slider {
	private static final float[] BACKGROUND_COLOR = new float[] { 0.0f, 0.0f, 0.0f, 0.25f };
	private static final float[] BORDER_COLOR = new float[] { 1.0f, 1.0f, 1.0f, 0.50f };
	private static final float[] KNOB_COLOR = new float[] { 1.0f, 1.0f, 1.0f, 0.75f };
	private static final float KNOB_RADIUS = 3.0f;
	
	public static enum Axis { X_AXIS, Y_AXIS }; 
	private final Rectangle bounds = new Rectangle();
	
	private DoubleAttr xAttr;
	private DoubleAttr xMin;
	private DoubleAttr xMax;
	
	private DoubleAttr yAttr;
	private DoubleAttr yMin;
	private DoubleAttr yMax;
	
	private final HUD hud;
	
	private boolean fireEvents = true;
	
	private final AttributePostChangeListener listener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
//			if (fireEvents) {
//				hud.redraw();
//			}
		}	
	};
	
	
	public Slider(HUD hud) {
		this.hud = hud;
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	private final JPatchMouseAdapter mouseAdapter = new JPatchMouseAdapter() {
		private boolean dragging = false;
		
		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragging) {
//				fireEvents = false;
				setXPos(e.getX());
				setYPos(e.getY());
				e.consume();
//				fireEvents = true;
//				hud.redraw();
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			checkBounds(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (xAttr != null && yAttr != null && checkBounds(e)) {
				int dx = Math.abs(e.getX() - getXPos());
				int dy = Math.abs(e.getY() - getYPos());
				if (Math.max(dx, dy) <= 8) {
					dragging = true;
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragging) {
				e.consume();
			}
		}
		
		private boolean checkBounds(MouseEvent e) {
			if (bounds.contains(e.getX(), e.getY())) {
				e.consume();
				return true;
			} else {
				return false;
			}
		}
	};
	
	public void link(Axis axis, DoubleAttr target) {
		switch(axis) {
		case X_AXIS:
			if (xAttr != null) {
				xAttr.removeAttributePostChangeListener(listener);
				xMin.removeAttributePostChangeListener(listener);
				xMax.removeAttributePostChangeListener(listener);
			}
			xAttr = target;
			if (xAttr != null) {
				xMin = AttributeManager.getInstance().getLowerLimit(xAttr);
				xMax = AttributeManager.getInstance().getUpperLimit(xAttr);
				if (xMin == null || xMax == null) {
					throw new IllegalArgumentException("attribute must be bounded");
				}
				xAttr.addAttributePostChangeListener(listener);
				xMin.addAttributePostChangeListener(listener);
				xMax.addAttributePostChangeListener(listener);
			}
			break;
		case Y_AXIS:
			if (yAttr != null) {
				yAttr.removeAttributePostChangeListener(listener);
				yMin.removeAttributePostChangeListener(listener);
				yMax.removeAttributePostChangeListener(listener);
			}
			yAttr = target;
			if (yAttr != null) {
				yMin = AttributeManager.getInstance().getLowerLimit(yAttr);
				yMax = AttributeManager.getInstance().getUpperLimit(yAttr);
				if (yMin == null || yMax == null) {
					throw new IllegalArgumentException("attribute must be bounded");
				}
				yAttr.addAttributePostChangeListener(listener);
				yMin.addAttributePostChangeListener(listener);
				yMax.addAttributePostChangeListener(listener);
			}
			break;
		default:
			throw new AssertionError("should never get here");
		}
	}
	
	private int getXPos() {
		double xValue = (xAttr.getDouble() - xMin.getDouble()) / (xMax.getDouble() - xMin.getDouble());
		return bounds.x + (int) (KNOB_RADIUS + xValue * (bounds.width - 2 * KNOB_RADIUS));
	}
	
	private int getYPos() {
		double yValue = (yAttr.getDouble() - yMin.getDouble()) / (yMax.getDouble() - yMin.getDouble());
		return bounds.y + (int) (KNOB_RADIUS + yValue * (bounds.height - 2 * KNOB_RADIUS));
	}
	
	private void setXPos(int xPos) {
		double xValue = (xPos - bounds.x - KNOB_RADIUS) / (bounds.width - 2 * KNOB_RADIUS);
		xAttr.setDouble((xValue - xMin.getDouble()) / (xMax.getDouble() - xMin.getDouble()));
	}
	
	private void setYPos(int yPos) {
		double yValue = (yPos - bounds.y - KNOB_RADIUS) / (bounds.height - 2 * KNOB_RADIUS);
		yAttr.setDouble((yValue - yMin.getDouble()) / (yMax.getDouble() - yMin.getDouble()));
	}
	
	protected JPatchMouseAdapter getMouseAdapter() {
		return mouseAdapter;
	}
	
	protected void draw(GL gl) {
		gl.glColor4fv(BACKGROUND_COLOR, 0);
		gl.glBegin(GL_TRIANGLE_FAN);
		gl.glVertex2f(bounds.x, bounds.y);
		gl.glVertex2f(bounds.x + bounds.width, bounds.y);
		gl.glVertex2f(bounds.x + bounds.width, bounds.y + bounds.height);
		gl.glVertex2f(bounds.x, bounds.y + bounds.height);
		gl.glEnd();
		
		gl.glColor4fv(BORDER_COLOR, 0);
		gl.glBegin(GL_LINE_LOOP);
		gl.glVertex2f(bounds.x, bounds.y);
		gl.glVertex2f(bounds.x + bounds.width, bounds.y);
		gl.glVertex2f(bounds.x + bounds.width, bounds.y + bounds.height);
		gl.glVertex2f(bounds.x, bounds.y + bounds.height);
		gl.glEnd();
		
		if (xAttr != null && yAttr != null) {
			final int x = getXPos();
			final int y = getYPos();
			gl.glColor4fv(KNOB_COLOR, 0);
			gl.glBegin(GL_TRIANGLE_FAN);
			gl.glVertex2f(x - KNOB_RADIUS, y - KNOB_RADIUS);
			gl.glVertex2f(x + KNOB_RADIUS, y - KNOB_RADIUS);
			gl.glVertex2f(x + KNOB_RADIUS, y + KNOB_RADIUS);
			gl.glVertex2f(x - KNOB_RADIUS, y + KNOB_RADIUS);
			gl.glEnd();
		}
	}
}
