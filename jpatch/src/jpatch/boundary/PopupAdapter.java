package jpatch.boundary;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public abstract class PopupAdapter implements MouseListener {

	public void mouseClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			openPopup(e);
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			openPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			openPopup(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
		if (e.isPopupTrigger()) {
			openPopup(e);
		}
	}

	public void mouseExited(MouseEvent e) {
		if (e.isPopupTrigger()) {
			openPopup(e);
		}
	}

	protected abstract void openPopup(MouseEvent e);
}
