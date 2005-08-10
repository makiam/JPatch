package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import jpatch.boundary.*;

public class RotateViewMotionListener extends MouseMotionAdapter {
	protected int iMouseX;
	protected int iMouseY;
	protected int iWidth;
	protected int iHeight;
	protected float fMin;
	
	public RotateViewMotionListener(int mouseX, int mouseY) {
		iMouseX = mouseX;
		iMouseY = mouseY;
	}
	
	public void mouseDragged(MouseEvent mouseEvent) {
		ViewDefinition viewDef = MainFrame.getInstance().getJPatchScreen().getViewDefinition((Component) mouseEvent.getSource());
		int deltaX = mouseEvent.getX() - iMouseX;
		int deltaY = mouseEvent.getY() - iMouseY;
		iMouseX = mouseEvent.getX();
		iMouseY = mouseEvent.getY();
		iWidth = ((Component)(mouseEvent.getSource())).getWidth();
		iHeight = ((Component)(mouseEvent.getSource())).getHeight();
		//fMin = (iWidth < iHeight) ? iWidth : iHeight;
		fMin = iWidth;
		viewDef.rotateView((float)deltaX/200,(float)deltaY/200);
	}
}
