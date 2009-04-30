package com.jpatch.afw;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

public class Browser2<T> {
	private final List<Node> nodeList = new ArrayList<Node>();
	private final int size;
	private final JButton[] scrollUpButtons;
	private final JButton[] scrollDownButtons;
	private final JButton scrollLeftButton = new JButton("<");
	private final JButton scrollRightButton = new JButton(">");
	private final BrowserNode[] browserNodes;
	private int firstIndex = 0;
	
	private final JComponent component = new JComponent() {
		static final int BUTTON_SIZE = 20;
		@Override
		public void doLayout() {
			final int cellWidth = (getWidth() - 2 * BUTTON_SIZE) / size;
			final int cellHeight = getHeight() - 2 * BUTTON_SIZE;
			scrollLeftButton.setBounds(0, BUTTON_SIZE, BUTTON_SIZE, cellHeight);
			scrollRightButton.setBounds(BUTTON_SIZE + size * cellWidth, BUTTON_SIZE, BUTTON_SIZE, cellHeight);
			for (int i = 0; i < size; i++) {
				final int x = BUTTON_SIZE + i * cellWidth;
				scrollUpButtons[i].setBounds(x, 0, cellWidth, BUTTON_SIZE);
				scrollDownButtons[i].setBounds(x, BUTTON_SIZE + cellHeight, cellWidth, BUTTON_SIZE);
				browserNodes[i].getComponent().setBounds(x, BUTTON_SIZE, cellWidth, cellHeight);
			}
		}
	};
	
	public Browser2(int size, T rootObject, Class<? extends BrowserNode> browserClass) throws InstantiationException, IllegalAccessException {
		nodeList.add(new Node(0, rootObject));
		this.size = size;
		scrollUpButtons = new JButton[size];
		scrollDownButtons = new JButton[size];
		browserNodes = new BrowserNode[size];
		component.add(scrollLeftButton);
		component.add(scrollRightButton);
		for (int i = 0; i < size; i++) {
			scrollUpButtons[i] = new JButton("/\\");
			scrollDownButtons[i] = new JButton("\\/");
			browserNodes[i] = browserClass.newInstance();
			component.add(browserNodes[i].getComponent());
			component.add(scrollUpButtons[i]);
			component.add(scrollDownButtons[i]);
		}
	}
	
	private void setupComponents() {
		for (int i = 0; i < size; i++) {
			final int index = firstIndex + i;
			final Object object = (index < nodeList.size()) ? nodeList.get(index).userData : null;
			browserNodes[i].setObject(object);
		}
	}
	
	private static interface BrowserNode {
		public Component getComponent();
		public void setObject(Object object);
	}
	
	private class Node {
		private final int level;
		private final List<Node> children = new ArrayList<Node>();
		private final T userData;
		private int selectedChildIndex = -1;
		
		private Node(int level, T userData) {
			this.level = level;
			this.userData = userData;
		}
		
		Node createChild(T userData) {
			Node child = new Node(level + 1, userData);
			children.add(child);
			selectedChildIndex = children.size() - 1;
			child.walk();
			return child;
		}
		
		T getUserData() {
			return userData;
		}
		
		void selectChildIndex(int index) {
			if (index < 0 || index >= children.size()) {
				throw new IndexOutOfBoundsException();
			}
			selectedChildIndex = index;
			children.get(selectedChildIndex).walk();
		}
		
		void walk() {
			if (level >= nodeList.size()) {
				nodeList.add(this);
			} else {
				nodeList.set(level, this);
			}
			if (selectedChildIndex >= 0) {
				children.get(selectedChildIndex).walk();
			} else {
				while(nodeList.size() > level + 1) {
					nodeList.remove(level + 1);
				}
			}
		}
	}
	
	/**
	 * @param args
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		class TestBrowserNode implements BrowserNode {
			private final JList list = new JList();
			private final JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			public Component getComponent() {
				return scrollPane;
			}

			public void setObject(Object object) {
				if (object == null) {
					list.setListData(new File[0]);
				} else {
					final File[] dirs = ((File) object).listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isDirectory();
						}
					});
					list.setListData(dirs);
				}
			}
			
		}
		Browser2<File> browser = new Browser2<File>(4, new File("/home/sascha/"), TestBrowserNode.class);
		//browser.nodeList.get(0).createChild("2").createChild("3");
		browser.setupComponents();
		JFrame frame = new JFrame("Browser Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 200);
		frame.setLayout(new BorderLayout());
		frame.add(browser.component, BorderLayout.CENTER);
		frame.setVisible(true);
	}

	
}
