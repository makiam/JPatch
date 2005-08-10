package jpatch.boundary;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

public class SmartScrollPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7148441356758539153L;
	private Color cFill = UIManager.getColor("Panel.background");
	private Color cLight = cFill.brighter();
	private Color cDark = cFill.darker();
	private Color cBack = new Color(0x000066);
	private Color cActive = cDark.darker();
	private Color cHover = new Color(0x7777FF);//UIManager.getColor("ToggleButton.focus");
	
	private ArrayList listButtons = new ArrayList();
	
	private Button buttonZoomOutH = new Button(0, -15, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_out.png"))).getImage());
	private Button buttonZoomInH = new Button(15, -15, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_in.png"))).getImage());
	private Button buttonScrollLeft = new Button(30, -15, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/arrow_left.png"))).getImage());
	private Button buttonScrollRight = new Button(-30, -15, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/arrow_right.png"))).getImage());
	private Button buttonZoomInV = new Button(-15, 0, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_in.png"))).getImage());
	private Button buttonZoomOutV = new Button(-15, 15, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/zoom_out.png"))).getImage());
	private Button buttonScrollUp = new Button(-15, 30, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/arrow_up.png"))).getImage());
	private Button buttonScrollDown = new Button(-15, -30, 15, 15, (new ImageIcon(ClassLoader.getSystemResource("jpatch/images/anim/arrow_down.png"))).getImage());
	private HSlider hSlider = new HSlider(45, -15, 30, 15, 0.25f, 0.5f);
	private VSlider vSlider = new VSlider(-15, 45, 15, 30, 0.25f, 0.25f);
	
	private MouseMotionListener sliderMotionListener;
	
	private VirtualCanvas vc;
	private Repeater repeater;
	
	private class Repeater extends Thread {
		boolean stop = false;
		Repeater() {
			start();
		}
		public void run() {
			action();
			try {
				sleep(500);
				while (!stop) {
					action();
					sleep(100);
				}
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}
		public synchronized void end() {
			stop = true;
		}
		void action() { }
	}
	
	private class HSlider {
		int x0, y, x1, h;
		float p, s;
		private int min = 10;
		private boolean pressed, hover = false;
		private HSlider(int x0, int y, int x1, int height, float position, float size) {
			this.x0 = x0;
			this.y = y;
			this.x1 = x1;
			this.h = height;
			this.p = position;
			this.s = size;
		}
		private void setPressedState(boolean state) {
			pressed = state;
			paint(getGraphics());
		}
		
		private void setHoverState(boolean state) {
			hover = state;
			paint(getGraphics());
		}
		
		private boolean getHoverState() {
			return hover;
		}
		
		private boolean isPressed() {
			return pressed;
		}
		
		private void move(int x) {
			int ww = getWidth() - x1 - x0;
			int dd = (int) (vc.getSliderSizeH() * (ww - 4));
			int dim = (dd < min) ? min : dd;
			if ((ww - 4 - dim) != 0) {
				p = (float) x / (ww - 4 - dim);
				vc.setViewLeft(vc.getViewLeft() + (vc.getWidth() - vc.getViewWidth()) * p);
				repaint();
			}
		}
		
		private void move2(int x) {
			vc.setViewLeft(vc.getViewLeft() + vc.getViewWidth() / 4 * x);
			repaint();
		}
		
		private void paint(Graphics g) {
			int xx = x0;
			int ww = getWidth() - x1 - x0;
			int yy = (y < 0) ? getHeight() + y : y;
			int dd = (int) (vc.getSliderSizeH() * (ww - 4));
			int dim = (dd < min) ? min : dd;
			int pos = (int) (vc.getSliderPosH() * (ww - 4 - dim) + 2);
			g.setColor(cBack);
			g.fillRect(xx, yy, pos, h);
			g.fillRect(xx + pos + dim, yy, ww - pos - dim, h);
			g.fillRect(xx, yy, ww, 2);
			g.fillRect(xx, yy + h - 2, ww, 2);
			draw3DWidget(g, cLight, cDark, pressed ? cActive : cFill, xx + pos, yy + 2, dim, h - 4);
			if (hover || pressed) {
				g.setColor(cHover);
				g.drawRect(xx + pos, yy + 2, dim - 1, h - 5);
				g.drawRect(xx + pos + 1, yy + 3, dim - 3, h - 7);
			}
		}
		private boolean isHit(int x, int y) {
			int xx = x0;
			int ww = getWidth() - x1 - x0;
			int yy = (this.y < 0) ? getHeight() + this.y : this.y;
			int dd = (int) (vc.getSliderSizeH() * (ww - 4));
			int dim = (dd < min) ? min : dd;
			int pos = (int) (vc.getSliderPosH() * (ww - 4 - dim) + 2);
			return (x > xx + pos && x < xx + pos + dim && y > yy && y < yy + h);
		}
		private boolean isHitRight(int x, int y) {
			int xx = x0;
			int ww = getWidth() - x1 - x0;
			int yy = (this.y < 0) ? getHeight() + this.y : this.y;
			int dd = (int) (vc.getSliderSizeH() * (ww - 4));
			int dim = (dd < min) ? min : dd;
			int pos = (int) (vc.getSliderPosH() * (ww - 4 - dim) + 2);
			return (x > xx + pos + dim && x < xx + ww && y > yy && y < yy + h);
		}
		private boolean isHitLeft(int x, int y) {
			int xx = x0;
			int ww = getWidth() - x1 - x0;
			int yy = (this.y < 0) ? getHeight() + this.y : this.y;
			int dd = (int) (vc.getSliderSizeH() * (ww - 4));
			int dim = (dd < min) ? min : dd;
			int pos = (int) (vc.getSliderPosH() * (ww - 4 - dim) + 2);
			return (x > xx && x < xx + pos && y > yy && y < yy + h);
		}
	}
	
	private class VSlider {
		int x, y0, w, y1;
		float p, s;
		private int min = 10;
		private boolean pressed, hover = false;
		private VSlider(int x, int y0, int width, int y1, float position, float size) {
			this.x = x;
			this.y0 = y0;
			this.w = width;
			this.y1 = y1;
			this.p = position;
			this.s = size;
		}
		private void setPressedState(boolean state) {
			pressed = state;
			paint(getGraphics());
		}
		
		private void setHoverState(boolean state) {
			hover = state;
			paint(getGraphics());
		}
		
		private boolean getHoverState() {
			return hover;
		}
		
		private boolean isPressed() {
			return pressed;
		}
		
		private void move(int y) {
			int hh = getHeight() - y1 - y0;
			int dd = (int) (vc.getSliderSizeV() * (hh - 4));
			int dim = (dd < min) ? min: dd;
			if ((hh - 4 - dim) != 0) {
				p = (float) y / (hh - 4 - dim);
				vc.setViewTop(vc.getViewTop() + (vc.getHeight() - vc.getViewHeight()) * p);
				repaint();
			}
		}
		
		private void move2(int y) {
			vc.setViewTop(vc.getViewTop() + vc.getViewHeight() / 4 * y);
			repaint();
		}
		
		private void paint(Graphics g) {
			int xx = (x < 0) ? getWidth() + x : x;
			int yy = y0;
			int hh = getHeight() - y1 - y0;
			int dd = (int) (vc.getSliderSizeV() * (hh - 4));
			int dim = (dd < min) ? min: dd;
			int pos = (int) (vc.getSliderPosV() * (hh - 4 - dim) + 2);
			g.setColor(cBack);
			g.fillRect(xx, yy, w, pos);
			g.fillRect(xx, yy + pos + dim, w, hh - pos - dim);
			g.fillRect(xx, yy, 2, hh);
			g.fillRect(xx + w - 2, yy, 2, hh);
			draw3DWidget(g, cLight, cDark, pressed ? cActive : cFill, xx + 2, yy + pos, w - 4, dim);
			if (hover || pressed) {
				g.setColor(cHover);
				g.drawRect(xx + 2, yy + pos, w - 5, dim - 1);
				g.drawRect(xx + 3, yy + pos + 1, w - 7, dim - 3);
			}
		}
		private boolean isHit(int x, int y) {
			int xx = (this.x < 0) ? getWidth() + this.x : this.x;
			int yy = y0;
			int hh = getHeight() - y1 - y0;
			int dd = (int) (vc.getSliderSizeV() * (hh - 4));
			int dim = (dd < min) ? min: dd;
			int pos = (int) (vc.getSliderPosV() * (hh - 4 - dim) + 2);
			return (x > xx && x < xx + w && y > yy + pos && y < yy + pos + dim);
		}
		private boolean isHitBottom(int x, int y) {
			int xx = (this.x < 0) ? getWidth() + this.x : this.x;
			int yy = y0;
			int hh = getHeight() - y1 - y0;
			int dd = (int) (vc.getSliderSizeV() * (hh - 4));
			int dim = (dd < min) ? min: dd;
			int pos = (int) (vc.getSliderPosV() * (hh - 4 - dim) + 2);
			return (x > xx && x < xx + w && y > yy + pos +dim && y < yy + hh);
		}
		private boolean isHitTop(int x, int y) {
			int xx = (this.x < 0) ? getWidth() + this.x : this.x;
			int yy = y0;
			int hh = getHeight() - y1 - y0;
			int dd = (int) (vc.getSliderSizeV() * (hh - 4));
			int dim = (dd < min) ? min: dd;
			int pos = (int) (vc.getSliderPosV() * (hh - 4 - dim) + 2);
			return (x > xx && x < xx + w && y > yy && y < yy + pos);
		}
	}
	
	private class Button {
		private int x, y, w, h;
		private BufferedImage imagePressed, imageReleased;
		private boolean pressed, hover = false;
		
		private Button(int x, int y, int width, int height, Image image) {
			this.x = x;
			this.y = y;
			this.w = width;
			this.h = height;
			imagePressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			draw3DWidget(imagePressed.getGraphics(), cDark, cLight, cActive, 0, 0, width, height);
			if (image != null) imagePressed.getGraphics().drawImage(image, 3, 3, null);
			imageReleased = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			draw3DWidget(imageReleased.getGraphics(), cLight, cDark, cFill, 0, 0, width, height);
			if (image != null) imageReleased.getGraphics().drawImage(image, 2, 2, null);
		}
		
		private void setPressedState(boolean state) {
			pressed = state;
			paint(getGraphics());
		}
		
		private void setHoverState(boolean state) {
			hover = state;
			paint(getGraphics());
		}
		
		private boolean getHoverState() {
			return hover;
		}
		
		private boolean isPressed() {
			return pressed;
		}
		
		private boolean isHit(int x, int y) {
			int xx = (this.x < 0) ? getWidth() + this.x : this.x;
			int yy = (this.y < 0) ? getHeight() + this.y : this.y;
			return (x > xx && x < xx + w && y > yy && y < yy + h);
		}
		
		private void paint(Graphics g) {
			int xx = (x < 0) ? getWidth() + x : x;
			int yy = (y < 0) ? getHeight() + y : y;
			if (pressed) g.drawImage(imagePressed, xx, yy, null);
			else g.drawImage(imageReleased, xx, yy, null);
			if (hover) {
				g.setColor(cHover);
				g.drawRect(xx, yy, w - 1, h - 1);
				g.drawRect(xx + 1, yy + 1, w - 3, h - 3);
			}
		}
	}
		
	public SmartScrollPane() {
		setBackground(new Color(0x000088));
		listButtons.add(buttonZoomOutH);
		listButtons.add(buttonZoomInH);
		listButtons.add(buttonZoomOutV);
		listButtons.add(buttonZoomInV);
		listButtons.add(buttonScrollLeft);
		listButtons.add(buttonScrollRight);
		listButtons.add(buttonScrollUp);
		listButtons.add(buttonScrollDown);
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent mouseEvent) {
				int x = mouseEvent.getX();
				int y = mouseEvent.getY();
				for (Iterator it = listButtons.iterator(); it.hasNext(); ) {
					Button button = (Button) it.next();
					if (button.isHit(x, y)) {
						if (!button.getHoverState()) button.setHoverState(true);
					} else {
						if (button.getHoverState()) button.setHoverState(false);
					}
				}
				if (hSlider.isHit(x, y)) {
					if (!hSlider.getHoverState()) hSlider.setHoverState(true);
				} else {
					if (hSlider.getHoverState()) hSlider.setHoverState(false);
				}
				if (vSlider.isHit(x, y)) {
					if (!vSlider.getHoverState()) vSlider.setHoverState(true);
				} else {
					if (vSlider.getHoverState()) vSlider.setHoverState(false);
				}
			}
			public void mouseDragged(MouseEvent mouseEvent) {
				int x = mouseEvent.getX();
				int y = mouseEvent.getY();
				for (Iterator it = listButtons.iterator(); it.hasNext(); ) {
					Button button = (Button) it.next();
					if (button.isHit(x, y)) {
						if (!button.getHoverState() && button.isPressed()) button.setHoverState(true);
					} else {
						if (button.getHoverState()) button.setHoverState(false);
					}
				}
				if (hSlider.isHit(x, y)) {
					if (!hSlider.getHoverState() && hSlider.isPressed()) hSlider.setHoverState(true);
				} else {
					if (hSlider.getHoverState()) hSlider.setHoverState(false);
				}
				if (vSlider.isHit(x, y)) {
					if (!vSlider.getHoverState() && vSlider.isPressed()) vSlider.setHoverState(true);
				} else {
					if (vSlider.getHoverState()) vSlider.setHoverState(false);
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent mouseEvent) {
				for (Iterator it = listButtons.iterator(); it.hasNext(); ) {
					Button button = (Button) it.next();
					if (button.getHoverState()) button.setHoverState(false);
				}
				if (hSlider.getHoverState()) hSlider.setHoverState(false);
				if (vSlider.getHoverState()) vSlider.setHoverState(false);
			}
			public void mousePressed(MouseEvent mouseEvent) {
				final int x = mouseEvent.getX();
				final int y = mouseEvent.getY();
				if (x < getWidth() - 15 && y < getHeight() - 15) {
					vc.mousePressed(mouseEvent);
				} else {
					for (Iterator it = listButtons.iterator(); it.hasNext(); ) {
						Button button = (Button) it.next();
						if (button.isHit(x, y)) {
							button.setPressedState(true);
							if (button == buttonScrollUp) {
								repeater = new Repeater() {
									void action() { vSlider.move2(-1); }
								};
							}
							else if (button == buttonScrollDown) {
								repeater = new Repeater() {
									void action() { vSlider.move2(1); }
								};
							}
							else if (button == buttonScrollLeft) {
								repeater = new Repeater() {
									void action() { hSlider.move2(-1); }
								};
							}
							else if (button == buttonScrollRight) {
								repeater = new Repeater() {
									void action() { hSlider.move2(1); }
								};
							}
							else if (button == buttonZoomInH) {
								repeater = new Repeater() {
									void action() {
										vc.setViewWidth(vc.getViewWidth() / 2);
										vc.setViewLeft(vc.getViewLeft() + vc.getViewWidth() / 2);
										repaint();
									}
								};
							}
							else if (button == buttonZoomOutH) {
								repeater = new Repeater() {
									void action() {
										vc.setViewWidth(vc.getViewWidth() * 2);
										vc.setViewLeft(vc.getViewLeft() - vc.getViewWidth() / 4);
										repaint();
									}
								};
							}
							else if (button == buttonZoomInV) {
								repeater = new Repeater() {
									void action() {
										//vc.setViewHeight(vc.getViewHeight() / 2);
										//vc.setViewTop(vc.getViewTop() + vc.getViewHeight() / 2);
										if (vc.getViewHeight() > 1) vc.setViewHeight(vc.getViewHeight() - 1);
										repaint();
									}
								};
							}
							else if (button == buttonZoomOutV) {
								repeater = new Repeater() {
									void action() {
										//vc.setViewHeight(vc.getViewHeight() * 2);
										//vc.setViewTop(vc.getViewTop() - vc.getViewHeight() / 4);
										if (vc.getViewHeight() < vc.getHeight()) vc.setViewHeight(vc.getViewHeight() + 1);
										if (vc.getViewTop() + vc.getViewHeight() > vc.getHeight()) vc.setViewTop(vc.getHeight() - vc.getViewHeight());
										repaint();
									}
								};
							}
						}
					}
				}
				if (vSlider.isHit(x, y)) {
					vSlider.setPressedState(true);
					sliderMotionListener = new MouseMotionAdapter() {
						private int Y = y;
						public void mouseDragged(MouseEvent mouseEvent) {
							int dy = mouseEvent.getY() - Y;
							Y = mouseEvent.getY();
							if (dy != 0) vSlider.move(dy);
						}
					};
					((Component)mouseEvent.getSource()).addMouseMotionListener(sliderMotionListener);
				}
				if (hSlider.isHit(x, y)) {
					hSlider.setPressedState(true);
					sliderMotionListener = new MouseMotionAdapter() {
						private int X = x;
						public void mouseDragged(MouseEvent mouseEvent) {
							int dx = mouseEvent.getX() - X;
							X = mouseEvent.getX();
							if (dx != 0) hSlider.move(dx);
						}
					};
					((Component)mouseEvent.getSource()).addMouseMotionListener(sliderMotionListener);
				}
				if (hSlider.isHitRight(x, y)) {
					repeater = new Repeater() {
						void action() { if (hSlider.isHitRight(x, y)) hSlider.move2(4); else end(); }
					};
				}
				if (hSlider.isHitLeft(x, y)) {
					repeater = new Repeater() {
						void action() { if (hSlider.isHitLeft(x, y)) hSlider.move2(-4); else end(); }
					};
				}
				if (vSlider.isHitBottom(x, y)) {
					repeater = new Repeater() {
						void action() { if (vSlider.isHitBottom(x, y)) vSlider.move2(4); else end(); }
					};
				}
				if (vSlider.isHitTop(x, y)) {
					repeater = new Repeater() {
						void action() { if (vSlider.isHitTop(x, y)) vSlider.move2(-4); else end(); }
					};
				}
			}
			public void mouseReleased(MouseEvent mouseEvent) {
				if (repeater != null) {
					repeater.end();
					repeater = null;
				}
				for (Iterator it = listButtons.iterator(); it.hasNext(); ) {
					Button button = (Button) it.next();
					if (button.isPressed()) button.setPressedState(false);
				}
				if (sliderMotionListener != null) {
					((Component)mouseEvent.getSource()).removeMouseMotionListener(sliderMotionListener);
					if (vSlider.isPressed()) vSlider.setPressedState(false);
					if (hSlider.isPressed()) hSlider.setPressedState(false);
				}
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
				vSlider.move2(mouseWheelEvent.getWheelRotation());
			}
		});
	}
	
	public void setVirtualCanvas(VirtualCanvas vc) {
		this.vc = vc;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		int width = getWidth();
		int height = getHeight();
		g.setColor(cFill);
		g.fillRect(width - 15, height - 15, 15, 15);
		for (Iterator it = listButtons.iterator(); it.hasNext(); ((Button) it.next()).paint(g));
		hSlider.paint(g);
		vSlider.paint(g);
		g.setClip(0, 0, getCanvasWidth(), getCanvasHeight());
		vc.paint(g);
	}
	
	private void draw3DWidget(Graphics g, Color c1, Color c2, Color c3, int x, int y, int width, int height) {
		g.setColor(c3);
		g.fillRect(x + 1, y + 1, width - 2, height - 2);
		g.setColor(c1);
		g.drawLine(x, y, x + width - 1, y);
		g.drawLine(x, y, x, y + height - 1);
		g.setColor(c2);
		g.drawLine(x + width - 1, y + height - 1, x + 1, y + height -1);
		g.drawLine(x + width - 1, y + height - 1, x + width - 1, y + 1);
	}
	
	int getCanvasWidth() {
		return getWidth() - 15;
	}
	
	int getCanvasHeight() {
		return getHeight() - 15;
	}
	
}
