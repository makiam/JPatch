package jpatch.boundary;

import javax.swing.*;
import java.awt.*;

public class JPatchButton extends JButton {
//implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Insets insets = new Insets(2,2,2,2);
	
	public JPatchButton() {
		super();
		init();
	}
	
	public JPatchButton(Action action) {
		super(action);
		init();
	}
	
	private void init() {
		setFocusable(false);
		setMargin(insets);
		//setBorderPainted(false);
		//addMouseListener(this);
		//setContentAreaFilled(true);
	}
	
	//public void mouseClicked(MouseEvent mouseEvent) {
	//}
	//
	//public void mousePressed(MouseEvent mouseEvent) {
	//}
	//
	//public void mouseReleased(MouseEvent mouseEvent) {
	//}
	//
	//public void mouseEntered(MouseEvent mouseEvent) {
	//	setBorderPainted(true);
	//	repaint();
	//}
	//
	//public void mouseExited(MouseEvent mouseEvent) {
	//	setBorderPainted(false);
	//	repaint();
	//}
	
}

