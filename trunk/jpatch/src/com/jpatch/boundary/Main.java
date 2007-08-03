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
package com.jpatch.boundary;




import com.jpatch.afw.attributes.*;
import com.jpatch.afw.control.Configuration;
import com.jpatch.afw.icons.IconSet;
import com.jpatch.afw.ui.AttributeManager;
import com.jpatch.afw.ui.Background;
import com.jpatch.afw.ui.ButtonUtils;
import com.jpatch.afw.ui.JPatchActionButton;
import com.jpatch.afw.ui.JPatchStateButton;
import com.jpatch.afw.ui.JPatchToggleButton;
import com.jpatch.afw.ui.JPatchToolBar;
import com.jpatch.afw.ui.UserInputListener;
import com.jpatch.boundary.actions.Actions;
import com.jpatch.boundary.actions.Actions.ViewportMode;
import com.jpatch.boundary.tools.JPatchTool;
import com.jpatch.boundary.tools.RotateTool;
import com.jpatch.entity.Camera;
import com.jpatch.entity.SceneGraphNode;
import com.jpatch.entity.SdsModel;
import com.jpatch.entity.Transform;
import com.jpatch.entity.TransformNode;
import com.jpatch.entity.sds.JptLoader;
import com.jpatch.entity.sds.Sds;
import com.jpatch.settings.Settings;
import com.jpatch.ui.ViewportSwitcher;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.Threading;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import jpatch.boundary.tools.Tools;


/**
 * @author sascha
 *
 */
public class Main {
	private static final Main INSTANCE = new Main();	// singleton pattern
	
	private static final int NUMBER_OF_VIEWPORTS = 4;
	private static Color VIEWPORT_BORDER_COLOR = Settings.getInstance().colors.text.get();
	private static Color ACTIVE_VIEWPORT_BORDER_COLOR = Settings.getInstance().colors.selection.get();
	
//	private Robot robot;
	private JFrame frame;
	private JLabel statusLabel = new JLabel("status");
	
	private Sds activeSds;

	private JComponent screen = new JPanel();
	
//	private JPatchTree tree = new JPatchTree();
//	private UIFactory uiFactory = new UIFactory();
	
	private Viewport[] viewports = new Viewport[4];
	private StateMachine<Viewport> activeViewport; 
	
	private Actions actions = new Actions();
	private boolean syncViewports = false;
	
	private CollectionAttr<ViewDirection> viewDirectionsAttr = new CollectionAttr<ViewDirection>(TreeSet.class, OrthoViewDirection.DIRECTIONS);
	
	private SceneGraphNode sceneGraphRoot = new SceneGraphNode() {
		@Override
		public Transform getTransform() {
			return null;
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
			switch (actions.viewportModeSM.getValue()) {
			case VIEWPORT_1:
				viewports[0].getComponent().setBounds(2, 2, width - 4, height - 4);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case VIEWPORT_2:
				viewports[1].getComponent().setBounds(2, 2, width - 4, height - 4);
				viewports[1].getComponent().setVisible(true);
				viewports[0].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case VIEWPORT_3:
				viewports[2].getComponent().setBounds(2, 2, width - 4, height - 4);
				viewports[2].getComponent().setVisible(true);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case VIEWPORT_4:
				viewports[3].getComponent().setBounds(2, 2, width - 4, height - 4);
				viewports[3].getComponent().setVisible(true);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(false);
				break;
			case SPLIT_1_3:
				viewports[0].getComponent().setBounds(2, 2, width - 4, hTop - 3);
				viewports[2].getComponent().setBounds(2, hTop + 1, width - 4, hBottom - 3);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(true);
				viewports[3].getComponent().setVisible(false);
				break;
			case SPLIT_2_4:
				viewports[1].getComponent().setBounds(2, 2, width - 4, hTop - 3);
				viewports[3].getComponent().setBounds(2, hTop + 1, width - 4, hBottom - 3);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(true);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(true);
				break;
			case SPLIT_1_2:
				viewports[0].getComponent().setBounds(2, 2, wLeft - 3, height - 4);
				viewports[1].getComponent().setBounds(wLeft + 1, 2, wRight - 3, height - 4);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(true);
				viewports[2].getComponent().setVisible(false);
				viewports[3].getComponent().setVisible(false);
				break;
			case SPLIT_3_4:
				viewports[2].getComponent().setBounds(2, 2, wLeft - 3, height - 4);
				viewports[3].getComponent().setBounds(wLeft + 1, 2, wRight - 3, height - 4);
				viewports[0].getComponent().setVisible(false);
				viewports[1].getComponent().setVisible(false);
				viewports[2].getComponent().setVisible(true);
				viewports[3].getComponent().setVisible(true);
				break;
			case QUAD:
				viewports[0].getComponent().setBounds(2, 2, wLeft - 3, hTop - 3);
				viewports[1].getComponent().setBounds(wLeft + 1, 2, wRight - 3, hTop - 3);
				viewports[2].getComponent().setBounds(2, hTop + 1, wLeft - 3, hBottom - 3);
				viewports[3].getComponent().setBounds(wLeft + 1, hTop + 1, wRight - 3, hBottom - 3);
				viewports[0].getComponent().setVisible(true);
				viewports[1].getComponent().setVisible(true);
				viewports[2].getComponent().setVisible(true);
				viewports[3].getComponent().setVisible(true);
				break;
			}
		}
	};
	
	/**
	 * private constructor (singleton pattern)
	 */
	@SuppressWarnings("serial")
	private Main() {
		
		
//		System.out.println("opengl is single threaded: " + Threading.isSingleThreaded());
//		Threading.disableSingleThreading();
//		System.out.println("opengl is single threaded: " + Threading.isSingleThreaded());
		
//		try {
//			int[] ts = new int[7];
//			int n = 0;
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
			
//			activeSds = new Sds(ClassLoader.getSystemResourceAsStream("off/teapot.off"));
//			new RibExport().export(activeSds, System.out);
//			System.exit(0);

//			activeSds = new JptLoader().importModel(new FileInputStream("/Users/sascha/cartoonRabbit.jpt"));

//			activeSds = new JptLoader().importModel(new FileInputStream("/mnt/share/jpatch0.4/models/moai2.jpt"));

//			activeSds.makeSlates();
//			activeSds.verify();
//			activeSds.dump();
//			activeSds.smooth();
//			System.exit(0);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			System.exit(0);
//		}

		
//		WorkspaceManager workspaceManager;
//		try {
//			workspaceManager = new WorkspaceManager(Settings.getInstance().workspace);
//			Project project = new Project(workspaceManager, "MyProject");
//			workspaceManager.refresh();
//			for (Project p : workspaceManager.getProjects()) {
//				tree.getRoot().add(new JPatchTreeNode(p));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		
		screen.setBackground(Color.BLACK);
//		try {
//			robot = new Robot();
//		} catch (AWTException e) {
//			e.printStackTrace();
//		}

//		Color BACKGROUND = new Color(0xa0a0a0);
		frame = new JFrame("JPatch");
		frame.setBackground(Color.BLACK);
//		frame.setBackground(BACKGROUND);
		frame.setSize(1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPatchInspector inspector = new JPatchInspector();
		
		/*
		 * initialize viewports
		 */
		ObjectRegistry<Viewport> viewportRegistry = new ObjectRegistry<Viewport>();
		
		for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
			viewports[i] = new ViewportGl(i + 1, OrthoViewDirection.DIRECTIONS[i * 2], viewDirectionsAttr, inspector);
			screen.add(viewports[i].getComponent());
			final int viewportNumber = i;
			viewports[i].getComponent().addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					activeViewport.setValue(viewports[viewportNumber]);
				}
			});
			viewports[i].getNameAttribute().setValue("Viewport");
			viewportRegistry.addObject(viewports[i]);
		}
		
		activeViewport = new StateMachine<Viewport>(viewports, viewports[0]);
		screen.setLayout(screenLayout);
		
		activeViewport.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				Viewport active = ((StateMachine<Viewport>) source).getValue();
				for (Viewport vp : ((StateMachine<Viewport>) source).getStates()) {
					vp.setActive(vp == active);
				}
//				repaintViewports();
			}
		});
		
		/*
		 * Add a listener to the viewport switcher state-machine to relayout the
		 * screen when the viewport mode has changed
		 */
		actions.viewportModeSM.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				screen.doLayout();
			}
		});
//		screen.setOpaque(false);
		
		try {
			activeSds = new JptLoader().importModel(new FileInputStream("/home/sascha/arrows.jpt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Box statusBar = new Box(SwingConstants.HORIZONTAL) {
			public void paintComponent(Graphics g) {
				Background.fillComponent(this, g);
			}
		};
		statusBar.add(statusLabel);
		statusBar.add(Box.createHorizontalGlue());
//		statusBar.add(MemoryMonitor.createMemoryMonitor(), BorderLayout.EAST);
		statusBar.add(Box.createHorizontalStrut(16));
		statusBar.setOpaque(true);
		statusBar.setBackground(new Color(0xb0b0b0));
//		tree.addMouseListener(new PopupAdapter() {
//			@Override
//			protected void openPopup(MouseEvent e) {
//				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
//				tree.setSelectionPath(path);
//				JPatchTreeNode treeNode = (JPatchTreeNode) path.getLastPathComponent();
//				JPatchObject userObject = treeNode.getUserObject();
//				if (userObject instanceof Project) {
//					Actions.getInstance().rethinkProjectActions();
//					((JPopupMenu) uiFactory.getComponent("project popup")).show(tree, e.getX(), e.getY());
//				}
//			}
//		});
//		
//		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
//			public void valueChanged(TreeSelectionEvent e) {
//				Actions.getInstance().rethinkProjectActions();
//			}
//		});
		/*
		 * initialize frame
		 */
		JSplitPane hSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
//		frame.setBackground(Settings.getInstance().colors.background.get());
		frame.setLayout(new BorderLayout());
		frame.add(statusBar, BorderLayout.SOUTH);
//		frame.setUndecorated(true);
		actions.toolSM.addAttributePreChangeListener(new AttributePreChangeAdapter() {
			@Override
			public Object attributeWillChange(ScalarAttribute source, Object value) {
				StateMachine<JPatchTool> sm = (StateMachine<JPatchTool>) source;
				sm.getValue().unregisterListeners(viewports);
				return value;
			}
		});
		actions.toolSM.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				StateMachine<JPatchTool> sm = (StateMachine<JPatchTool>) source;
				sm.getValue().registerListeners(viewports);
			}
		});
		
//		actions.toolSM.getValue().registerListeners(viewports);
		
		JPatchToolBar toolBar = new JPatchToolBar();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(toolBar, BorderLayout.CENTER);
		frame.add(panel, BorderLayout.NORTH);
		ViewportSwitcher viewportSwitcher = new ViewportSwitcher(actions.viewportModeSM);
		
		ButtonUtils buttonUtils = new ButtonUtils();
		
		JPatchActionButton undoButton = new JPatchActionButton(actions.undo);
		JPatchActionButton redoButton = new JPatchActionButton(actions.redo);
		
		JPatchStateButton moveView = new JPatchStateButton(actions.moveView);
		JPatchStateButton zoomView = new JPatchStateButton(actions.zoomView);
		JPatchStateButton rotateView = new JPatchStateButton(actions.rotateView);
		
		JPatchStateButton vertexMode = new JPatchStateButton(actions.vertexMode);
		JPatchStateButton edgeMode = new JPatchStateButton(actions.edgeMode);
		JPatchStateButton faceMode = new JPatchStateButton(actions.faceMode);
		JPatchStateButton objectMode = new JPatchStateButton(actions.objectMode);
		
		JPatchToggleButton snapToGrid = new JPatchToggleButton(actions.snapToGrid);
		
		JPatchStateButton defaultTool = new JPatchStateButton(actions.defaultTool);
		JPatchStateButton moveTool = new JPatchStateButton(actions.moveTool);
		JPatchStateButton scaleTool = new JPatchStateButton(actions.scaleTool);
		JPatchStateButton rotateTool = new JPatchStateButton(actions.rotateTool);
		
		JPatchStateButton extrudeTool = new JPatchStateButton(actions.extrudeTool);
		JPatchStateButton latheTool = new JPatchStateButton(actions.latheTool);
		
		buttonUtils.configureButtons(IconSet.Style.DARK, undoButton, redoButton);
		buttonUtils.configureButtons(IconSet.Style.GLOSSY, moveView, zoomView, rotateView);
		buttonUtils.configureButtons(IconSet.Style.FROSTED, vertexMode, edgeMode, faceMode, objectMode);
		buttonUtils.configureButtons(IconSet.Style.BRUSHED, snapToGrid);
		buttonUtils.configureButtons(IconSet.Style.BRUSHED, defaultTool, moveTool, scaleTool, rotateTool);
		buttonUtils.configureButtons(IconSet.Style.BRUSHED, extrudeTool, latheTool);
		
		toolBar.add(viewportSwitcher.getComponent());
		toolBar.add(Box.createHorizontalStrut(32));
		toolBar.add(undoButton);
		toolBar.add(redoButton);
		toolBar.add(Box.createHorizontalStrut(16));
		toolBar.add(moveView);
		toolBar.add(zoomView);
		toolBar.add(rotateView);
		toolBar.add(Box.createHorizontalStrut(16));
		toolBar.add(vertexMode);
		toolBar.add(edgeMode);
		toolBar.add(faceMode);
		toolBar.add(objectMode);
		toolBar.add(Box.createHorizontalStrut(16));
		toolBar.add(snapToGrid);
		toolBar.add(Box.createHorizontalStrut(4));
		toolBar.add(defaultTool);
		toolBar.add(moveTool);
		toolBar.add(scaleTool);
		toolBar.add(rotateTool);
		toolBar.add(Box.createHorizontalStrut(4));
		toolBar.add(extrudeTool);
		toolBar.add(latheTool);
		
		
		
		final JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		activeViewport.addAttributePostChangeListener(inspector.getViewportChangeListener());
		inspector.getComponent().setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		final DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		final TreeManager treeManager = new TreeManager(treeModel);
		
		final TransformNode node1 = new TransformNode();
		final TransformNode node2 = new TransformNode();
		final TransformNode node3 = new TransformNode();
		final TransformNode node4 = new TransformNode();
		final SdsModel model1 = new SdsModel(activeSds);
		final Camera camera1 = new Camera();
		final Camera camera2 = new Camera();
		
		node1.getNameAttribute().setValue("node 1");
		node1.getParentAttribute().setValue(sceneGraphRoot);
		node2.getParentAttribute().setValue(node1);
		node2.getNameAttribute().setValue("node 2");
		node3.getParentAttribute().setValue(node2);
		node3.getNameAttribute().setValue("node 3");
		node4.getParentAttribute().setValue(node2);
		node4.getNameAttribute().setValue("node 4");
		model1.getParentAttribute().setValue(node4);
		model1.getNameAttribute().setValue("model 1");
		camera1.getParentAttribute().setValue(node3);
		camera1.getNameAttribute().setValue("camera 1");
		camera2.getParentAttribute().setValue(node4);
		camera2.getNameAttribute().setValue("camera 2");
		
		viewDirectionsAttr.add(new PerspectiveViewDirection(camera1));
		viewDirectionsAttr.add(new PerspectiveViewDirection(camera2));
		System.out.println(viewDirectionsAttr.getElements());
		
		treeManager.createTreeNodeFor(sceneGraphRoot);
		JTree tree = new JTree(treeModel);
		
		JScrollPane scrollPane = new JScrollPane(inspector.getComponent(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xcccccc), 1));
		scrollPane.getViewport().setBorder(null);
		scrollPane.setViewportBorder(null);
		scrollPane.getVerticalScrollBar().setBorder(null);
		vSplit.setBorder(null);
		vSplit.setContinuousLayout(true);
//		vSplit.setBackground(BACKGROUND);
		vSplit.add(scrollPane);
		vSplit.setDividerSize(9);
//		vSplit.add(new JScrollPane(tree));
		
		JScrollPane scrollPane2 = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane2.setBorder(BorderFactory.createLineBorder(new Color(0xcccccc), 1));
		scrollPane2.getViewport().setBorder(null);
		scrollPane2.setViewportBorder(null);
		scrollPane2.getVerticalScrollBar().setBorder(null);
		
		vSplit.add(scrollPane2);
		vSplit.setOneTouchExpandable(true);
//		hSplit.setBackground(BACKGROUND);
		
		JComponent sideBar = new JPanel(new BorderLayout());
		JComponent leftStrut = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				Background.fillComponent(this, g);
			}
		};
		leftStrut.setPreferredSize(new Dimension(2, 2));
		sideBar.add(leftStrut, BorderLayout.WEST);
		sideBar.add(vSplit, BorderLayout.CENTER);
		
		JComponent screenArea = new JPanel(new BorderLayout());
		JComponent rightStrut = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				Background.fillComponent(this, g);
			}
		};
		rightStrut.setPreferredSize(new Dimension(2, 2));
		screenArea.add(screen, BorderLayout.CENTER);
		screenArea.add(rightStrut, BorderLayout.EAST);
		
		hSplit.add(sideBar);
		hSplit.add(screenArea);
		hSplit.setBorder(null);
		hSplit.setContinuousLayout(true);
		hSplit.setOneTouchExpandable(true);
		hSplit.setDividerLocation(300);
		hSplit.setDividerSize(9);
//		hSplit.setUI(new BasicSplitPaneUI());
		
		
//		vSplit.setContinuousLayout(true);
//		vSplit.setOneTouchExpandable(true);
		frame.add(hSplit, BorderLayout.CENTER);
//		vSplit.setDividerLocation(0.5);
		
		
//		viewInspector.setObject(viewports[0]);
//		frame.add(uiFactory.getComponent("main toolbar"), BorderLayout.NORTH);
//		frame.add(uiFactory.getComponent("edit toolbar"), BorderLayout.EAST);
//		frame.setJMenuBar((JMenuBar) uiFactory.getComponent("menubar"));
		
		actions.toolSM.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				repaintViewports();
			}
		});
		
//		viewports[0].setActive(true);
//		activeViewport.setValue(viewports[0]);
		
		SelectionManager selectionManager = new SelectionManager(tree, treeManager);
		selectionManager.getSelectedObjectAttribute().addAttributePostChangeListener(inspector.getSelectionChangeListener());
		
		AttributeManager.getInstance().addUserInputListener(new UserInputListener() {
			public void userInput(Object source, Attribute attr) {
//				System.out.println("UserInput from " + source);
				/*
				 * if the input came from a viewport, repaint just this viewport,
				 * otherwise repaint all viewports
				 */
				if (source instanceof ViewDef) {
					repaintViewport(((ViewDef) source).getViewport());
				} else if (source instanceof SceneGraphNode) {
					System.out.println("recomputeing scene graph");
					computeSceneGraph((SceneGraphNode) source);
					repaintViewports();
				} else {
					repaintViewports();
				}
			}
		});
		
		frame.setVisible(true);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				vSplit.setDividerLocation(0.5);
				repaintViewports();
			}
		});
		
//		new BshConsole();
	}
	
//	public JPatchObject getSelectedTreeUserObject() {
//		JPatchTreeNode selectedNode  = (JPatchTreeNode) tree.getSelectionPath().getLastPathComponent();
//		if (selectedNode == null) {
//			return null;
//		}
//		return selectedNode.getUserObject();
//	}
	
//	private void validateActiveViewport() {
//		if (!viewports[activeViewport].getComponent().isVisible()) {
//			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
//				if (viewports[i].getComponent().isVisible()) {
//					activeViewport = i;
//					break;
//				}
//			}
//		}
//	}
	
	public static Main getInstance() {
		return INSTANCE;
	}
	
//	public Robot getRobot() {
//		return robot;
//	}
	
	public JComponent getScreen() {
		return screen;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public Sds getActiveSds() {
		return activeSds;
	}
	
	public SceneGraphNode getSceneGraphRoot() {
		return sceneGraphRoot;
	}
	
	public JPatchTool getActiveTool() {
//		return actions.toolSM.getValue();
		return new RotateTool();
	}
	
	public void setActiveSds(Sds sds) {
		activeSds = sds;
	}
	
	public void computeSceneGraph() {
		computeSceneGraph(sceneGraphRoot);
	}
	
	public void computeSceneGraph(SceneGraphNode node) {
		Transform transform = node.getTransform();
		if (transform != null) {
			transform.computeMatrix();
		}
		for (SceneGraphNode child : node.getChildrenAttribute().getElements()) {
			computeSceneGraph(child);
		}
	}
	
	public void repaintViewport(Viewport viewport) {
		Component component = viewport.getComponent();
		if (component.isVisible()) {
			viewport.getViewDef().computeMatrix();
			((GLAutoDrawable) component).display();
		}
	}
	
	public void repaintViewports() {
		for (Viewport viewport : viewports) {
			repaintViewport(viewport);
		}
	}
	
	public void syncRepaintViewport(Viewport viewport) {
		if (syncViewports) {
			repaintViewports();
		} else {
			repaintViewport(viewport);
		}
	}
	
	public void syncViewports(Viewport viewport) {
		if (!syncViewports) {
			for (Viewport v : viewports) {
				if (v != viewport) {
					repaintViewport(v);
				}
			}
		}
	}
	
//	public void setTool(JPatchTool tool) {
//		if (activeTool != null) {
//			activeTool.unregisterListeners(viewports);
//		}
//		if (tool != null) {
//			tool.registerListeners(viewports);
//		}
//		activeTool = tool;
//	}
//	
//	public JPatchTool getTool() {
//		return activeTool;
//	}
	
//	public void repaintTree() {
//		tree.repaint();
//	}
	
//	public void setSelectedObject(Object object) {
//		selectionInspector.setObject(object);
//	}
	
	private class Screen extends JComponent {
		@Override
		public void paintBorder(Graphics g) {
//			if (true) return;
			for (int i = 0; i < NUMBER_OF_VIEWPORTS; i++) {
				Component c = viewports[i].getComponent();
				if (!c.isVisible()) {
					continue;
				}
				g.setColor(viewports[i].active ? VIEWPORT_BORDER_COLOR : VIEWPORT_BORDER_COLOR);
				g.drawRect(c.getX() - 1, c.getY() - 1, c.getWidth() + 1, c.getHeight() + 1);
			}
		}	
	}

	public CollectionAttr<ViewDirection> getViewDirectionsAttribute() {
		return viewDirectionsAttr;
	}
	
	
}
