package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.*;

public class JPatchButton extends JButton
implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Insets insets = new Insets(2,2,2,2);
	private boolean bRolloverState = false;
	private boolean bPressedState = false;
	
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
		//setOpaque(false);
//		setBorderPainted(false);
//		addMouseListener(this);
//		//addItemListener(this);
//		setContentAreaFilled(false);
//		setRolloverEnabled(false);
	}
	
	public void mouseClicked(MouseEvent mouseEvent) {
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
		bPressedState = true;
		repaint();
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
		bPressedState = false;
		repaint();
	}
	
	public void mouseEntered(MouseEvent mouseEvent) {
		bRolloverState = true;
		repaint();
	}
	
	public void mouseExited(MouseEvent mouseEvent) {
		bPressedState = false;
		bRolloverState = false;
		repaint();
	}
	
//	public void paint(Graphics g) {
//		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		if (bRolloverState) {
//			g.setColor(getBackground().brighter());
//			g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 9, 9);
//		} if (bPressedState) {
//			g.setColor(getBackground().darker());
//			g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 9, 9);
//		}
//		super.paint(g);
//		if (!isSelected() && !bRolloverState) {
////			float[] rgba = new float[4];
////			getBackground().getComponents(rgba);
////			rgba[3] = 0.8f;
////			g.setColor(new Color(rgba[0], rgba[1], rgba[2], rgba[3]));
////			g.fillRect(0, 0, getWidth(), getHeight());
//		} else if (bPressedState) {
//			if (!bRolloverState) {
//				g.setColor(getBackground().darker().darker());
//				g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 9, 9);
//			}
//		}
//		if (bRolloverState) {
//			super.paint(g);
//			g.setColor(Color.ORANGE);
//			g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
//			g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 9, 9);
//			//g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 9, 9);
//		}
//	}
	
}

