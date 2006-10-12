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

import sds.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import jpatch.entity.*;

import jpatch.boundary.newtools.*;
import jpatch.boundary.settings.*;


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
	
	private Layout layout = Layout.S1;
	
	private Robot robot;
	private JFrame frame;
	private JLabel statusLabel = new JLabel("status");
	
	private JPatchTool activeTool;
	private Model activeModel;
	private Sds activeSds;
	
	private int activeViewport = 0;
	
	private Screen screen = new Screen();
	
	private Inspector inspector = new Inspector();
	
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
//		try {
//			activeSds = new Sds(new FileInputStream("/home/sascha/off/hammerhead.off"));
//			activeSds = activeSds.subdivide().subdivide();		
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		
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

		frame = new JFrame("JPatch");
		frame.setSize(1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		activeModel = new Model();
		
		ControlPoint cp0 = new ControlPoint(activeModel);
		ControlPoint cp1 = new ControlPoint(activeModel);
		ControlPoint cp2 = new ControlPoint(activeModel);
		ControlPoint cp3 = new ControlPoint(activeModel);
		cp0.position.set(-5, 0, 0);
		cp1.position.set( 0, 5, 0);
		cp2.position.set( 5, 0, 0);
		cp3.position.set( 0,-5, 0);
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

		/*
		 * initialize viewports
		 */
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			viewports[i] = new ViewportGl(i + 1, Viewport.View.FRONT, models);
			screen.add(viewports[i].getComponent());
			final int viewportNumber = i;
			viewports[i].getComponent().addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					activeViewport = viewportNumber;
					validateActiveViewport();
					screen.paintBorder(screen.getGraphics());
					if (e.getClickCount() == 2 || inspector.getObject() instanceof Viewport) {
						if (inspector.getObject() != viewports[activeViewport]) {
							inspector.setObject(viewports[activeViewport]);
						}
					}
				}
			});
		}
		screen.setLayout(screenLayout);
		screen.setOpaque(false);
		
		Box statusBar = Box.createHorizontalBox();
		statusBar.add(statusLabel);
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(MemoryMonitor.createMemoryMonitor(), BorderLayout.EAST);
		statusBar.add(Box.createHorizontalStrut(16));

		/*
		 * initialize frame
		 */
		frame.setBackground(Settings.getInstance().colors.background.get());
		frame.setLayout(new BorderLayout());
		frame.add(screen, BorderLayout.CENTER);
		frame.add(statusBar, BorderLayout.SOUTH);
		frame.add(inspector.getComponent(), BorderLayout.WEST);
		
		UIFactory uiFactory = new UIFactory();
		uiFactory.parseLayout(this, ClassLoader.getSystemResource("jpatch/boundary/layout2.xml"));
		
		inspector.setObject(viewports[0]);
		frame.add(uiFactory.getComponent("main toolbar"), BorderLayout.NORTH);
		frame.add(uiFactory.getComponent("edit toolbar"), BorderLayout.EAST);
		frame.setJMenuBar((JMenuBar) uiFactory.getComponent("menubar"));
		frame.setVisible(true);
	}
	
	private void validateActiveViewport() {
		if (!viewports[activeViewport].getComponent().isVisible()) {
			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
				if (viewports[i].getComponent().isVisible()) {
					activeViewport = i;
					break;
				}
			}
		}
	}
	
	public static Main getInstance() {
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
	
	public Sds getActiveSds() {
		return activeSds;
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
			screen.doLayout();
			validateActiveViewport();
			if (inspector.getObject() instanceof Viewport) {
				if (inspector.getObject() != viewports[activeViewport]) {
					inspector.setObject(viewports[activeViewport]);
				}
			}
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
		activeTool = tool;
	}
	
	public JPatchTool getTool() {
		return activeTool;
	}
	
	private class Screen extends JComponent {
		@Override
		public void paintBorder(Graphics g) {
			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
				Component c = viewports[i].getComponent();
				if (!c.isVisible()) {
					continue;
				}
				g.setColor(i == activeViewport ? ACTIVE_VIEWPORT_BORDER_COLOR : VIEWPORT_BORDER_COLOR);
				g.drawRect(c.getX() - 1, c.getY() - 1, c.getWidth() + 1, c.getHeight() + 1);
			}
		}	
	}
}
