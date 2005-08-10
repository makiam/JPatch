package jpatch.boundary.mouse;

import java.awt.event.*;
import jpatch.boundary.*;

public class ZoomViewMotionListener extends MouseMotionAdapter {
	protected int iMouseX;
	protected int iMouseY;
	
	public ZoomViewMotionListener(int mouseX, int mouseY) {
		iMouseX = mouseX;
		iMouseY = mouseY;
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		int deltaX = mouseEvent.getX() - iMouseX;
		int deltaY = mouseEvent.getY() - iMouseY;
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		float scale = (1+((float)deltaX-(float)deltaY)/200);
		if (scale < 0.2) scale = (float)0.2;
		if (scale > 5) scale = (float)5;
		((Viewport)(mouseEvent.getSource())).getViewDefinition().scaleView(scale);
	}
}
