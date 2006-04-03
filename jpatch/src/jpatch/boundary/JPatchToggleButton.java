package jpatch.boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JPatchToggleButton extends JToggleButton
implements MouseListener, ItemListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Insets insets = new Insets(2,2,2,2);
	private boolean bRolloverState = false;
	
	public JPatchToggleButton() {
		super();
		init();
	}
	
	public JPatchToggleButton(Action action) {
		super(action);
		init();
	}
	
	private void init() {
//		setFocusable(false);
		setMargin(insets);
		//setOpaque(false);
//		setBorderPainted(false);
//		addMouseListener(this);
//		addItemListener(this);
//		setContentAreaFilled(false);
//		setRolloverEnabled(false);
	}
	
	public void mouseClicked(MouseEvent mouseEvent) {
	}
	
	public void mousePressed(MouseEvent mouseEvent) {
	}
	
	public void mouseReleased(MouseEvent mouseEvent) {
	}
	
	public void mouseEntered(MouseEvent mouseEvent) {
		bRolloverState = true;
		repaint();
	}
	
	public void mouseExited(MouseEvent mouseEvent) {
		bRolloverState = false;
		repaint();
	}
	
	public void itemStateChanged(ItemEvent itemEvent) {
		//setBorderPainted(isSelected());
		//repaint();
	}
	
//	public void paint(Graphics g) {
//		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		if (bRolloverState) {
//			g.setColor(getBackground().brighter());
//			g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 9, 9);
//		} else if (isSelected()) {
//			g.setColor(getBackground().darker());
//			g.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 9, 9);
//		}
//		super.paint(g);
//		if (!isSelected() && !bRolloverState) {
////			float[] rgba = new float[4];
////			getBackground().getComponents(rgba);
////			rgba[3] = 0.5f;
////			g.setColor(new Color(rgba[0], rgba[1], rgba[2], rgba[3]));
////			g.fillRect(0, 0, getWidth(), getHeight());
//		} else if (isSelected()) {
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

