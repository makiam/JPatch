package com.jpatch.afw;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import jpatch.boundary.action.*;

public class Stripe {
	public static enum Orientation { HORIZONTAL, VERTICAL }
	private final Orientation orientation;
	private int visibleItemCount;
	private final List<Component> components = new ArrayList<Component>();
	private final JComponent listView = new JPanel();
	private final JComponent component = new JPanel(new BorderLayout());
	private int position;
	private final JButton backButton = new JButton("<");
	private final JButton forwardButton = new JButton(">");
	
	public Stripe(Orientation orientation, int visibleItemCount) {
		this.orientation = orientation;
		setVisibleItemCount(visibleItemCount);
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
	
	private void scroll(int newPosition) {
		int delta = newPosition - position;
		int size = visibleItemCount + Math.min(visibleItemCount, Math.abs(delta));
		System.out.println("size=" + size);
		int[] indices = new int[size];
		int start = Math.min(position, newPosition);
		if (size <= visibleItemCount * 2) {
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
		System.out.println(Arrays.toString(indices));
		int iwidth = listView.getWidth() / visibleItemCount;
		int width = iwidth * size;;
		int height = listView.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, width, height);
		for (int i = 0; i < size; i++) {
			Component component = components.get(indices[i]);
			component.setBounds(0, 0, iwidth, height);
			component.paint(g);
			System.out.println(component.isVisible());
			g.translate(iwidth, 0);
		}
		
		JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
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
		final Stripe stripe = new Stripe(Stripe.Orientation.HORIZONTAL, 3);
		final JFrame frame = new JFrame("stripe");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 300);
		frame.add(stripe.component);
		frame.setVisible(true);
		
		for (int i = 0; i < 5; i++) {
			final JPanel panel = new JPanel();
			final JLabel label = new JLabel(Integer.toString(i)) {
				public void paint(Graphics g) {
					System.out.println(this + ".paint(" + g + ")");
					super.paint(g);
				}
			};
			panel.add(new JButton("x"));
			panel.add(label);
			label.setBorder(new EtchedBorder());
			label.setOpaque(true);
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					stripe.addComponent(label);
					stripe.scroll(0);
				}
			});
		//	frame.repaint();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
