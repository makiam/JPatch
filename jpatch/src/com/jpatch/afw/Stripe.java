package com.jpatch.afw;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;


public class Stripe {
	public static enum Orientation { HORIZONTAL, VERTICAL }
	private final Orientation orientation;
	private int visibleItemCount;
	private final List<Component> components = new ArrayList<>();
	private final JComponent listView = new JPanel();
	private final JComponent component = new JPanel(new BorderLayout());
	private int position;
	private final JButton backButton = new JButton("<");
	private final JButton forwardButton = new JButton(">");
	
	private volatile Scroller scroller;
	
	public Stripe(Orientation orientation, int visibleItemCount) {
		this.orientation = orientation;
		setVisibleItemCount(visibleItemCount);
		backButton.addActionListener((ActionEvent arg0) -> {
                    scroll(position - 1);
                });
		forwardButton.addActionListener((ActionEvent arg0) -> {
                    scroll(position + 1);
                });
	}
	
	public void setVisibleItemCount(int visibleItemCount) {
		this.visibleItemCount = visibleItemCount;
		position = Math.max(0, components.size() - visibleItemCount);
		component.add(listView, BorderLayout.CENTER);
		if (orientation == Orientation.HORIZONTAL) {
			component.add(backButton, BorderLayout.WEST);
			component.add(forwardButton, BorderLayout.EAST);
			listView.setLayout(new GridLayout(1, visibleItemCount, 2, 2));
		} else {
			component.add(backButton, BorderLayout.NORTH);
			component.add(forwardButton, BorderLayout.SOUTH);
			listView.setLayout(new GridLayout(visibleItemCount, 1, 2, 2));
		}
		layoutListView();
	}
	
	public void addComponent(Component component) {
		components.add(component);
		position = Math.max(0, components.size() - visibleItemCount);
		layoutListView();
	}
	
	public void clear() {
		components.clear();
		position = 0;
		layoutListView();
	}
	
	private void setupButtons() {
		backButton.setEnabled(position > 0);
	}
	
	private void scroll(final int newPosition) {
		int delta = newPosition - position;
		int size = visibleItemCount + Math.min(visibleItemCount, Math.abs(delta));
		//System.out.println("position=" + position + " newPosition=" + newPosition + "size=" + size);
		int[] indices = new int[size];
		int start = Math.min(position, newPosition);
		if (size < visibleItemCount * 2) {
			for (int i = 0; i < size; i++) {
				indices[i] = start + i;
			}
		} else {
			int end = Math.max(position, newPosition);
			for (int i = 0; i < visibleItemCount; i++) {
				indices[i] = start + i;
				indices[i + visibleItemCount] = end + i;
			}
		}
		int startIndex = -1;
		int endIndex = -1;
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] == position) {
				startIndex = i;
			}
			if (indices[i] == newPosition) {
				endIndex = i;
			}
		}
		//System.out.println(Arrays.toString(indices));
		int pixelSize = -1;
		BufferedImage image = null;
		if (orientation == Orientation.HORIZONTAL) {
			pixelSize = listView.getWidth() / visibleItemCount;
			int width = pixelSize * size;
			int height = listView.getHeight();
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.setColor(component.getBackground());
			g.fillRect(0, 0, width, height);
			for (int i = 0; i < size; i++) {
				if (indices[i] < components.size()) {
					Component component = components.get(indices[i]);
					component.setBounds(0, 0, pixelSize, height);
					component.doLayout();
					component.paint(g);
					g.translate(pixelSize, 0);
				} else {
					break;
				}
			}
		} else {
			pixelSize = listView.getHeight() / visibleItemCount;
			int width = listView.getWidth();
			int height = pixelSize * size;
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.setColor(component.getBackground());
			g.fillRect(0, 0, width, height);
			for (int i = 0; i < size; i++) {
				if (indices[i] < components.size()) {
					Component component = components.get(indices[i]);
					component.setBounds(0, 0, width, pixelSize);
					component.doLayout();
					component.paint(g);
					g.translate(0, pixelSize);
				} else {
					break;
				}
			}
		}
		component.remove(listView);
		
		position = newPosition;
		setupButtons();
		
		final ImagePainter imagePainter = new ImagePainter(image);
		component.add(imagePainter, BorderLayout.CENTER);
		final int startPixel = startIndex * pixelSize;
		final int endPixel = endIndex * pixelSize;
		
//		for (int i = 0; i < 100; i++) {
//			int x = startX + (endX - startX) * i / 100;
//			imagePainter.setXOffset(x);
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		component.doLayout();
		component.repaint();
		new Thread(new Scroller(imagePainter, startPixel, endPixel)).start();
	}
	
	private class Scroller implements Runnable {
		final ImagePainter imagePainter;
		final int startPixel;
		final int endPixel;
		
		final int duration = 250; //ms
		
		int count = 0;
		
		volatile boolean stop;
		
		Scroller(ImagePainter imagePainter, int startPixel, int endPixel) {
			this.imagePainter = imagePainter;
			this.startPixel = startPixel;
			this.endPixel = endPixel;
		}
		
		public void run() {
			if (scroller != null) {
				scroller.stop = true;
			}
			scroller = this;
			int tt;
			long t = System.currentTimeMillis();
			long t0 = t;
			while (!stop && (tt = (int) (System.currentTimeMillis() - t)) < duration) {
				int dt = (int) (System.currentTimeMillis() - t0);
				t0 = System.currentTimeMillis();
				double s = (double) tt / duration;
				double ss = 0.5 - 0.5 * Math.cos(s * Math.PI);
				int pixel = startPixel + (int) ((endPixel - startPixel) * ss);
				imagePainter.setOffset(pixel);
				component.paintImmediately(component.getBounds());
				count++;
				try {
					Thread.sleep(Math.max(0, 1000/61 - dt));
				} catch (InterruptedException e) {
					break;
				}
			}
			component.remove(imagePainter);
			component.add(listView, BorderLayout.CENTER);
			layoutListView();
			component.paintImmediately(component.getBounds());
		}
	}
	
	private void layoutListView() {
		listView.removeAll();
		for (int i = position; i < position + visibleItemCount; i++) {
			if (i < components.size()) {
				listView.add(components.get(i));
			} else {
				listView.add(new JPanel());
			}
		}
		//listView.getLayout().layoutContainer(listView);
		//listView.invalidate();
		listView.validate();
	}
	
	public static void main(String[] args) {
		final Stripe hStripe = new Stripe(Stripe.Orientation.HORIZONTAL, 4);
		final JFrame frame = new JFrame("stripe");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 300);
		frame.add(hStripe.component);
		frame.setVisible(true);
		
		for (int i = 0; i < 10; i++) {
			final Stripe vStripe = new Stripe(Stripe.Orientation.VERTICAL, 1);
			for (int j = 0; j < 5; j++) {
				final JPanel panel = new JPanel();
				final JLabel label = new JLabel(Integer.toString(j)) {
					public void paint(Graphics g) {
						super.paint(g);
					}
				};
				panel.add(new JButton("x"));
				panel.add(label);
				label.setBorder(new EtchedBorder());
				label.setOpaque(true);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						vStripe.addComponent(label);
					}
				});
			}
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					hStripe.addComponent(vStripe.component);
				}
			});
		//	frame.repaint();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				hStripe.scroll(0);
			}
		});
	}
	
	private class ImagePainter extends JComponent {
		private final Image image;
		private int offset;
		
		private ImagePainter(Image image) {
			this.image = image;
		}
		
		private synchronized void setOffset(int offset) {
			this.offset = offset;
		}
		
		public void paint(Graphics g) {
			g.drawImage(image, orientation == Orientation.HORIZONTAL ? -offset : 0, orientation == Orientation.VERTICAL ? -offset : 0, null);
		}
	}
}
