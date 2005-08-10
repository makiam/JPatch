package jpatch.boundary.mouse;

import java.awt.*;
import java.awt.event.*;
import jpatch.boundary.*;

public class PopupMouseListener extends MouseAdapter {
	private int iButton;
	private JPatchPopupMenu popupMenu;

	public PopupMouseListener(int button) {
		iButton = button;
	}

	public void mousePressed(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() == iButton) {
			if (popupMenu != null && popupMenu.isShowing()) {
				popupMenu.setVisible(false);
			} else {
				//Viewport viewport = (Viewport)mouseEvent.getSource();
				Component source = (Component)mouseEvent.getSource();
				Viewport viewport = (Viewport)mouseEvent.getSource();
				popupMenu = new JPatchPopupMenu(viewport.getViewDefinition());
				popupMenu.show(source,mouseEvent.getX(),mouseEvent.getY());
			}
		}
	}
}
