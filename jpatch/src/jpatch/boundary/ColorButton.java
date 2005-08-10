package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ColorButton extends JButton
implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Insets insets = new Insets(2,2,2,2);
	
	public ColorButton() {
		super();
		init();
	}
	
	public ColorButton(Action action) {
		super(action);
		init();
	}
	
	private void init() {
		setFocusable(false);
		setMargin(insets);
		setBorderPainted(false);
		addMouseListener(this);
		setContentAreaFilled(false);
	}
	
	public void mouseClicked(MouseEvent mouseEvent) {
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
	}
	
	public void mouseEntered(MouseEvent mouseEvent) {
		setBorderPainted(true);
		repaint();
	}
	
	public void mouseExited(MouseEvent mouseEvent) {
		setBorderPainted(false);
		repaint();
	}
	
	public void paint(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(3,3,getWidth() - 6,getHeight() - 6);
		if (isBorderPainted()) {
			g.setColor(new Color(255,224,0));
			g.drawRect(1,1,getWidth() - 3,getHeight() - 3);
			g.drawRect(2,2,getWidth() - 5,getHeight() - 5);

		}
		g.setColor(Color.BLACK);
		g.drawLine(1,0,getWidth() - 2,0);
		g.drawLine(1,getHeight() - 1,getWidth() - 2,getHeight() - 1);
		g.drawLine(0,1,0,getHeight() - 2);
		g.drawLine(getWidth() - 1,1,getWidth() - 1,getHeight() - 2);
	}
}

