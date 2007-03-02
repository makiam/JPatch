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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import jpatch.entity.*;

import jpatch.boundary.action.OldActions;
import jpatch.boundary.newaction.Actions;
import jpatch.boundary.newtools.*;
import jpatch.boundary.settings.*;
import jpatch.boundary.tree.JPatchTree;
import jpatch.boundary.tree.JPatchTreeNode;


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
	
	private Inspector viewInspector = new Inspector();
	private Inspector toolInspector = new Inspector();
	private Inspector selectionInspector = new Inspector();
	
	private JPatchTree tree = new JPatchTree();
	private UIFactory uiFactory = new UIFactory();
	
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

		try {
			int[] ts = new int[7];
			int n = 0;
//			for (int i = 0; i < 1; i++) {
//				activeSds = new Sds(new FileInputStream("/home/sascha/off/grid.off"));
//			activeSds.dump();
////			activeSds = activeSds.subdivide();
////			activeSds = activeSds.subdivide();
////			activeSds = activeSds.subdivide();
//				long t0 = System.currentTimeMillis();
//				for (int j = 0; j < 5; j++) {
//					long t1 = System.currentTimeMillis();
//					activeSds.subdivide();
//					long t = System.currentTimeMillis();
//					if (i >= 5) {
//						ts[j] += (t - t1);
//					}
//					System.out.println(j + ": " + "t=" + (t - t0) + " dt=" + (t - t1) + " v=" + activeSds);
//				}
//				if (i >= 5) {
//					n++;
//				}
				
//			}
//			for (int i = 1; i < 7; i++) {
//				System.out.println(i + ": " + ts[i] + "/" + n + "=" + ts[i] / n + " " + ts[i] / ts[i - 1]);
//			}
//			System.exit(0);
			
			activeSds = new Sds(ClassLoader.getSystemResourceAsStream("off/opencube.off"));
//			activeSds.makeSlates();
//			activeSds.verify();
//			activeSds.dump();
//			activeSds.smooth();
//			System.exit(0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		
		WorkspaceManager workspaceManager;
		try {
			workspaceManager = new WorkspaceManager(Settings.getInstance().workspace);
			Project project = new Project(workspaceManager, "MyProject");
			workspaceManager.refresh();
			for (Project p : workspaceManager.getProjects()) {
				tree.getRoot().add(new JPatchTreeNode(p));
			}
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
			viewports[i] = new ViewportGl(i + 1, i * 2, models);
			screen.add(viewports[i].getComponent());
			final int viewportNumber = i;
			viewports[i].getComponent().addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					activeViewport = viewportNumber;
					validateActiveViewport();
					screen.paintBorder(screen.getGraphics());
//					if (e.getClickCount() == 2 || viewInspector.getObject() instanceof Viewport) {
						if (viewInspector.getObject() != viewports[activeViewport]) {
							viewInspector.setObject(viewports[activeViewport]);
						}
//					}
				}
			});
		}
		screen.setLayout(screenLayout);
//		screen.setOpaque(false);
		
		Box statusBar = Box.createHorizontalBox();
		statusBar.add(statusLabel);
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(MemoryMonitor.createMemoryMonitor(), BorderLayout.EAST);
		statusBar.add(Box.createHorizontalStrut(16));

		tree.addMouseListener(new PopupAdapter() {
			@Override
			protected void openPopup(MouseEvent e) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(path);
				JPatchTreeNode treeNode = (JPatchTreeNode) path.getLastPathComponent();
				JPatchObject userObject = treeNode.getUserObject();
				if (userObject instanceof Project) {
					Actions.getInstance().rethinkProjectActions();
					((JPopupMenu) uiFactory.getComponent("project popup")).show(tree, e.getX(), e.getY());
				}
			}
		});
		
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Actions.getInstance().rethinkProjectActions();
			}
		});
		/*
		 * initialize frame
		 */
		JSplitPane hSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
//		frame.setBackground(Settings.getInstance().colors.background.get());
		frame.setLayout(new BorderLayout());
		frame.add(statusBar, BorderLayout.SOUTH);
		
		JTabbedPane inspectorPane = new JTabbedPane();
		inspectorPane.add("View", new JScrollPane(viewInspector.getComponent()));
		inspectorPane.add("Tool", new JScrollPane(toolInspector.getComponent()));
		inspectorPane.add("Selection", new JScrollPane(selectionInspector.getComponent()));
		
		JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		vSplit.add(inspectorPane);
		vSplit.add(new JScrollPane(tree));
		
		hSplit.add(vSplit);
		hSplit.add(screen);
		hSplit.setContinuousLayout(true);
		hSplit.setOneTouchExpandable(true);
		
		vSplit.setContinuousLayout(true);
		vSplit.setOneTouchExpandable(true);
		frame.add(hSplit, BorderLayout.CENTER);
		vSplit.setDividerLocation(0.5);
		
		
		uiFactory.parseLayout(this, ClassLoader.getSystemResource("jpatch/boundary/layout2.xml"));
		
		viewInspector.setObject(viewports[0]);
		frame.add(uiFactory.getComponent("main toolbar"), BorderLayout.NORTH);
		frame.add(uiFactory.getComponent("edit toolbar"), BorderLayout.EAST);
		frame.setJMenuBar((JMenuBar) uiFactory.getComponent("menubar"));
		frame.setVisible(true);
	}
	
	public JPatchObject getSelectedTreeUserObject() {
		JPatchTreeNode selectedNode  = (JPatchTreeNode) tree.getSelectionPath().getLastPathComponent();
		if (selectedNode == null) {
			return null;
		}
		return selectedNode.getUserObject();
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
			if (viewInspector.getObject() != viewports[activeViewport]) {
				viewInspector.setObject(viewports[activeViewport]);
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
	
	public void repaintTree() {
		tree.repaint();
	}
	
	public void setSelectedObject(Object object) {
		selectionInspector.setObject(object);
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
