package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import jpatch.boundary.*;

public class ActiveViewportMouseAdapter extends JPatchMouseAdapter {
	
	public void mousePressed(MouseEvent mouseEvent) {
		MainFrame.getInstance().getJPatchScreen().setActiveViewport((Component) mouseEvent.getSource());
	}
	
	public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
		MainFrame.getInstance().getJPatchScreen().setActiveViewport((Component) mouseWheelEvent.getSource());
	}
}
