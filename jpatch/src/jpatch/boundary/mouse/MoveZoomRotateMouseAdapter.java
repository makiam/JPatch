package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;

import javax.vecmath.Quat4f;

import jpatch.boundary.*;
import jpatch.control.edit.AtomicChangeAnimObjectScale;
import jpatch.control.edit.AtomicModifyMotionCurve;
import jpatch.control.edit.JPatchActionEdit;
import jpatch.control.edit.JPatchRootEdit;
import jpatch.control.edit.ModifyAnimObject;
import jpatch.entity.Camera;
import jpatch.entity.MotionCurveSet;

public class MoveZoomRotateMouseAdapter extends JPatchMouseAdapter {
	private static final int MOVE = 1;
	private static final int ROTATE = 2;
	
	private int iMouseX;
	private int iMouseY;
	private float fMin;
	private int iState;
	
	private float fFocalLength;
	private Quat4f q4CameraOrient = new Quat4f();
	private long timestamp;
	private float pos;
	
	public void mousePressed(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
			ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
			((Component)mouseEvent.getSource()).addMouseMotionListener(this);
			iMouseX = mouseEvent.getX();
			iMouseY = mouseEvent.getY();
//			int dx = iMouseX - 40;
//			int dy = iMouseY - ((Component)mouseEvent.getSource()).getHeight() + 40;
//			if ((dx * dx + dy * dy) > 2500 && !viewDef.isLocked()) {
//				iState = MOVE;
//			} else {
//				iState = ROTATE;
//			}
			iState = (mouseEvent.isControlDown() || viewDef.isLocked()) ? ROTATE : MOVE;
			if (viewDef.getCamera() != null) {
				fFocalLength = viewDef.getCamera().getFocalLength();
				q4CameraOrient.set(viewDef.getCamera().getOrientation());
			}
		}
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
			((Component)mouseEvent.getSource()).removeMouseMotionListener(this);
			ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
			if (viewDef.getCamera() != null) {
				System.out.println("*");
				Camera camera = viewDef.getCamera();
				ModifyAnimObject edit = new ModifyAnimObject(camera);
				Quat4f newOrientation = camera.getOrientation();
				float newFocalLength = camera.getFocalLength();
				camera.setOrientation(q4CameraOrient);
				MotionCurveSet.Camera mcs = (MotionCurveSet.Camera) MainFrame.getInstance().getAnimation().getCurvesetFor(camera);
				float position = MainFrame.getInstance().getAnimation().getPosition();
				if (!newOrientation.equals(q4CameraOrient))
					edit.addEdit(new AtomicModifyMotionCurve.Quat4f(mcs.orientation, position, newOrientation));
//				if (fFocalLength != newFocalLength) {
//					edit.addEdit(new AtomicModifyMotionCurve.Float(mcs.focalLength, position, fFocalLength));
//				}
				mcs.setPosition(position);
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				MainFrame.getInstance().getTimelineEditor().repaint();
			}
		}
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		int deltaX = mouseEvent.getX() - iMouseX;
		int deltaY = mouseEvent.getY() - iMouseY;
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		switch (iState) {
			case MOVE:
				int iWidth = ((Component)(mouseEvent.getSource())).getWidth();
				//int iHeight = ((Component)(mouseEvent.getSource())).getHeight();
				//fMin = (iWidth < iHeight) ? iWidth : iHeight;
				fMin = iWidth/2;
				viewDef.moveView((float)deltaX/fMin,(float)deltaY/fMin);
				break;
			case ROTATE:
				viewDef.rotateView((float)deltaX/200,(float)deltaY/200);
				break;
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseWheelEvent.getSource());
//		if (viewDef.getCamera() != null) {
//			fFocalLength = viewDef.getCamera().getFocalLength();
//		}
		int wheelClicks = mouseWheelEvent.getWheelRotation();
		float scale = (1-((float)wheelClicks)/10);
		if (scale < 0.2) scale = (float)0.2;
		if (scale > 5) scale = (float)5;
		if (viewDef.getCamera() != null)
			fFocalLength = viewDef.getCamera().getFocalLength();
		viewDef.scaleView(scale);
		long currentTime = System.currentTimeMillis();
		if (viewDef.getCamera() != null) {
			if (currentTime > timestamp + 1000 || pos != MainFrame.getInstance().getAnimation().getPosition()) {
				timestamp = currentTime;
				Camera camera = viewDef.getCamera();
				ModifyAnimObject edit = new ModifyAnimObject(camera);
				MotionCurveSet.Camera mcs = (MotionCurveSet.Camera) MainFrame.getInstance().getAnimation().getCurvesetFor(camera);
				float position = MainFrame.getInstance().getAnimation().getPosition();
				float newFocalLength = camera.getFocalLength();
				camera.setFocalLength(fFocalLength);
				edit.addEdit(new AtomicModifyMotionCurve.Float(mcs.focalLength, position, newFocalLength));
				fFocalLength = camera.getFocalLength();
				mcs.setPosition(position);
				MainFrame.getInstance().getUndoManager().addEdit(edit);
				MainFrame.getInstance().getTimelineEditor().repaint();
				pos = position;
			} else {
				Camera camera = viewDef.getCamera();
				MotionCurveSet.Camera mcs = (MotionCurveSet.Camera) MainFrame.getInstance().getAnimation().getCurvesetFor(camera);
				float position = MainFrame.getInstance().getAnimation().getPosition();
				mcs.focalLength.setFloatAt(position, camera.getFocalLength());
				MainFrame.getInstance().getTimelineEditor().repaint();
			}
		}
//		if (viewDef.getCamera() != null) {
//			System.out.println("*");
//			Camera camera = viewDef.getCamera();
//			ModifyAnimObject edit = new ModifyAnimObject(camera);
//			float newScale = camera.getScale();
//			camera.setScale(fCameraScale);
//			MotionCurveSet mcs = MainFrame.getInstance().getAnimation().getCurvesetFor(camera);
//			float position = MainFrame.getInstance().getAnimation().getPosition();
//			edit.addEdit(new AtomicChangeAnimObjectScale(camera, newScale));
//			mcs.setPosition(position);
//			MainFrame.getInstance().getUndoManager().addEdit(edit);
//		}
	}
}
