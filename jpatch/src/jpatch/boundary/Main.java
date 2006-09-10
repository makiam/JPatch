/*
 * $Id:$
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import jpatch.entity.*;

import jpatch.boundary.newaction.Actions;
import jpatch.boundary.newtools.*;
import jpatch.boundary.settings.*;
import jpatch.boundary.ui.JPatchButton;


/**
 * @author sascha
 *
 */
public class Main {
	private static final Main INSTANCE = new Main();	// singleton pattern
	public static enum Layout { S1, S2, S3, S4, H12, H34, V13, V24, QUAD, SINGLE, H_SPLIT, V_SPLIT }
	
	private static final int NUMBER_OF_VIEWPORTS = 4;
	private static Color VIEWPORT_BORDER_COLOR = Settings.getInstance().colors.text.get();
	private static Color ACTIVE_VIEWPORT_BORDER_COLOR = Settings.getInstance().colors.selection.get();
	
	private Layout layout = Layout.QUAD;
	
	private Robot robot;
	private JFrame frame;
	private JToolBar primaryToolBar;
	private JToolBar secondaryToolBar;
	private JLabel statusLabel = new JLabel("status");
	
	private JPatchTool activeTool;
	private Model activeModel;
	private int activeViewport = 0;
	
	private Viewport[] viewports = new Viewport[4];
	private Iterable<Model> models = new Iterable<Model>() {
		public Iterator<Model> iterator() {
			return new Iterator<Model>() {
				private int i = 0;
				public boolean hasNext() {
					return i < 1;
				}

				public Model next() {
					if (i > 0) {
						throw new ArrayIndexOutOfBoundsException(i);
					}
					i++;
					return activeModel;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};

	private JComponent screen = new JComponent() {

		@Override
		public void paintComponent(Graphics g) {
			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
				g.setColor(i == 0 ? ACTIVE_VIEWPORT_BORDER_COLOR : VIEWPORT_BORDER_COLOR);
				Component c = viewports[i].getComponent();
				g.drawRect(c.getX() - 1, c.getY() - 1, c.getWidth() + 1, c.getHeight() + 1);
			}
		}
		
	};
	private LayoutManager2 screenLayout = new LayoutManager2() {
		private Dimension dim = new Dimension();
		
		public void addLayoutComponent(Component comp, Object constraints) {
			
		}

		public float getLayoutAlignmentX(Container target) {
			return 0;
		}

		public float getLayoutAlignmentY(Container target) {
			return 0;
		}

		public void invalidateLayout(Container target) {
		}

		public void addLayoutComponent(String name, Component comp) {
			
		}

		public void removeLayoutComponent(Component comp) {
			
		}

		public Dimension preferredLayoutSize(Container parent) {
			return dim;
		}

		public Dimension minimumLayoutSize(Container parent) {
			return dim;
		}

		public Dimension maximumLayoutSize(Container parent) {
			return dim;
		}
		
		public void layoutContainer(Container parent) {
			int height = parent.getHeight();
			int width = parent.getWidth();
			int hTop = height >> 1;
			int hBottom = height - hTop;
			int wLeft = width >> 1;
			int wRight = width - wLeft;
			switch (layout) {
			case S1:
				viewports[0].getComponent().setBounds(1, 1, width - 2, height - 2);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case S2:
				viewports[1].getComponent().setBounds(1, 1, width - 2, height - 2);
				viewports[1].getComponent().setVisible(true);
				viewports[0].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case S3:
				viewports[2].getComponent().setBounds(1, 1, width - 2, height - 2);
				viewports[2].getComponent().setVisible(true);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case S4:
				viewports[3].getComponent().setBounds(1, 1, width - 2, height - 2);
				viewports[3].getComponent().setVisible(true);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(false);
				break;
			case V13:
				viewports[0].getComponent().setBounds(1, 1, width - 2, hTop - 2);
				viewports[2].getComponent().setBounds(1, hTop + 1, width - 2, hBottom - 2);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(true);
				viewports[3].getComponent().setVisible(false);
				break;
			case V24:
				viewports[1].getComponent().setBounds(1, 1, width - 2, hTop - 2);
				viewports[3].getComponent().setBounds(1, hTop + 1, width - 2, hBottom - 2);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(true);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(true);
				break;
			case H12:
				viewports[0].getComponent().setBounds(1, 1, wLeft - 2, height - 2);
				viewports[1].getComponent().setBounds(wLeft + 1, 1, wRight - 2, height - 2);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(true);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case H34:
				viewports[2].getComponent().setBounds(1, 1, wLeft - 2, height - 2);
				viewports[3].getComponent().setBounds(wLeft + 1, 1, wRight - 2, height - 2);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(true);
				viewports[3].getComponent().setVisible(true);
				break;
			case QUAD:
				viewports[0].getComponent().setBounds(1, 1, wLeft - 2, hTop - 2);
				viewports[1].getComponent().setBounds(wLeft + 1, 1, wRight - 2, hTop - 2);
				viewports[2].getComponent().setBounds(1, hTop + 1, wLeft - 2, hBottom - 2);
				viewports[3].getComponent().setBounds(wLeft + 1, hTop + 1, wRight - 2, hBottom - 2);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(true);
				viewports[2].getComponent().setVisible(true);
				viewports[3].getComponent().setVisible(true);
				break;
			}
		}
	};
//	private Choreograpy activeChoreography;
	
	
	/**
	 * private constructor (singleton pattern)
	 */
	private Main() {
		System.out.println("1");
		WorkspaceManager workspaceManager;
		try {
			workspaceManager = new WorkspaceManager(Settings.getInstance().workspace);
			Project project = new Project(workspaceManager, "Test_Project");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		System.out.println("2");
//		frame.setTitle("JPatch");
		frame = new JFrame();
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		activeModel = new Model();
		
		ControlPoint cp0 = new ControlPoint(activeModel);
		ControlPoint cp1 = new ControlPoint(activeModel);
		ControlPoint cp2 = new ControlPoint(activeModel);
		ControlPoint cp3 = new ControlPoint(activeModel);
		cp0.position.set(-100, 0, 0);
		cp1.position.set( 0, 100, 0);
		cp2.position.set( 100, 0, 0);
		cp3.position.set( 0,-100, 0);
		cp0.setLoop(true);
		cp0.setNext(cp1);
		cp1.setNext(cp2);
		cp2.setNext(cp3);
		cp3.setNext(cp0);
		cp0.setPrev(cp3);
		cp1.setPrev(cp0);
		cp2.setPrev(cp1);
		cp3.setPrev(cp2);
		activeModel.addCurve(cp0);
		activeModel.initControlPoints();
		System.out.println("3");
		/*
		 * initialize viewports
		 */
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			viewports[i] = new ViewportGl(i + 1, Viewport.View.FRONT, models);
			screen.add(viewports[i].getComponent());
		}
		screen.setLayout(screenLayout);
		screen.setOpaque(false);
		
		Box statusBar = Box.createHorizontalBox();
		statusBar.add(statusLabel);
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(MemoryMonitor.createMemoryMonitor(), BorderLayout.EAST);
		statusBar.add(Box.createHorizontalStrut(16));
		System.out.println("4");
		/*
		 * initialize frame
		 */
		frame.setBackground(Settings.getInstance().colors.background.get());
		frame.setLayout(new BorderLayout());
		frame.add(screen, BorderLayout.CENTER);
		frame.add(statusBar, BorderLayout.SOUTH);
		frame.add(new JLabel("X"), BorderLayout.WEST);
		
		UIFactory uiFactory = new UIFactory();
		uiFactory.parseLayout(this, ClassLoader.getSystemResource("jpatch/boundary/layout2.xml"));
		
		secondaryToolBar = (JToolBar) uiFactory.getComponent("edit toolbar");
//		secondaryToolBar.add(new JButton(Actions.getInstance().getAction("add curve")));
		frame.add(secondaryToolBar, BorderLayout.EAST);
		System.out.println("5");
//		Actions.getInstance();
		
		frame.setVisible(true);
		System.out.println("6");
	}
	
	public static Main getInstance() {
		System.out.println("INSTANCE = " + INSTANCE);
		return INSTANCE;
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public JComponent getScreen() {
		return screen;
	}
	
	public Model getActiveModel() {
		return activeModel;
	}
	
	public void setLayout(Layout layout) {
		Layout newLayout = null;
		switch (layout) {
		case SINGLE:
			switch (activeViewport) {
			case 0:
				newLayout = Layout.S1;
				break;
			case 1:
				newLayout = Layout.S2;
				break;
			case 2:
				newLayout = Layout.S3;
				break;
			case 3:
				newLayout = Layout.S4;
				break;
			}
			break;
		case H_SPLIT:
			switch (activeViewport) {
			case 0:	// fallthrough intended
			case 1:
				newLayout = Layout.H12;
				break;
			case 2:	// fallthrough intended
			case 3:
				newLayout = Layout.H34;
				break;
			}
			break;
		case V_SPLIT:
			switch (activeViewport) {
			case 0:	// fallthrough intended
			case 2:
				newLayout = Layout.V13;
				break;
			case 1:	// fallthrough intended
			case 3:
				newLayout = Layout.V24;
				break;
			}
			break;
		default:
			newLayout = layout;
		}
		if (newLayout != this.layout) {
			this.layout = newLayout;
			screen.validate();
			screen.repaint();
		}
	}
	
	public void setTool(JPatchTool tool) {
		if (activeTool != null) {
			activeTool.unregisterListeners(viewports);
		}
		if (tool != null) {
			tool.registerListeners(viewports);
		}
		if (activeTool != null) {
			activeTool.unregisterListeners(viewports);
		}
	}
}
