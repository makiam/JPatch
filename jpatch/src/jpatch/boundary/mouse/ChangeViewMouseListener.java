package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import jpatch.boundary.*;

public class ChangeViewMouseListener extends MouseAdapter {
	public static final int MOVE = 1;
	public static final int ZOOM = 2;
	public static final int ROTATE = 3;
	
	protected int iButton;
	protected int iMode;
	
	protected MouseMotionAdapter mouseMotionListener;
	public ChangeViewMouseListener(int button, int mode) {
		iButton = button;
		iMode = mode;
		switch(iMode) {
			case MOVE:
				MainFrame.getInstance().setHelpText("drag to move view");
			break;
			case ZOOM:
				MainFrame.getInstance().setHelpText("drag to zoom view");
			break;
			case ROTATE:
				MainFrame.getInstance().setHelpText("drag to rotate view");
			break;
		}
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == iButton) {
//			MainFrame.getInstance().getDefaultToolTimer().stop();
			switch(iMode) {
				
				case MOVE:
					mouseMotionListener = new MoveViewMotionListener(mouseEvent.getX(),mouseEvent.getY());
				break;
				case ZOOM:
					mouseMotionListener = new ZoomViewMotionListener(mouseEvent.getX(),mouseEvent.getY());
				break;
				case ROTATE:
					mouseMotionListener = new RotateViewMotionListener(mouseEvent.getX(),mouseEvent.getY());
				break;
				
			}
			((Component)mouseEvent.getSource()).addMouseMotionListener(mouseMotionListener);
		} else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
			MainFrame.getInstance().getMeshToolBar().reset();
		}
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == iButton) {
			((Component)mouseEvent.getSource()).removeMouseMotionListener(mouseMotionListener);
//			MainFrame.getInstance().getDefaultToolTimer().restart();
		}
	}
}

