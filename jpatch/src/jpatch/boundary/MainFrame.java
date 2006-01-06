package jpatch.boundary;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
//import java.beans.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.tree.*;

import buoy.event.KeyPressedEvent;

import jpatch.*;
import jpatch.entity.*;
import jpatch.boundary.settings.Settings;
import jpatch.boundary.tools.*;
//	import jpatch.boundary.mouse.*;		//remove
import jpatch.control.*;
import jpatch.control.edit.*;
import jpatch.boundary.laf.*;
import jpatch.boundary.action.*;

public final class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int MESH = 1;
	public static final int MORPH = 2;
	public static final int BONE = 3;
	
	private int iMode = MESH;
	
	private Model model;
	private Anim animation;
	private JPatchScreen jpatchScreen;
	private static MainFrame INSTANCE;
//	private MainToolBar mainToolBar;
//	private MeshToolBar meshToolBar;
//	private BoneToolBar boneToolBar;
//	private MorphToolBar morphToolBar;
	private JPanel helpPanel;
	private JLabel helpLabel;
//	private JMenuBar mainMenu;
	private ButtonGroup bgAction;
	private Selection selection;
	private JPatchUndoManager undoManager = new JPatchUndoManager(50);
	private JPatchKeyAdapter keyAdapter = new JPatchKeyAdapter();
	private JDialog dialog;
	private Point pointDialogLocation = new Point(800,600);
	private XYZLockConstraints constraints = new XYZLockConstraints();
	private ViewDefinition[] aViewDef = new ViewDefinition[JPatchScreen.NUMBER_OF_VIEWPORTS];
	private JPatchTree tree;
	private SideBar sideBar;
	private MutableTreeNode treenodeRoot;
//	private MutableTreeNode treenodeModel;
	private MorphTarget editedMorph;
	
//	private javax.swing.Timer defaultToolTimer = new javax.swing.Timer(0, new ActionListener() {
//		public void actionPerformed(ActionEvent event) {
//			defaultToolTimer.stop();
//			meshToolBar.reset();
//		}
//	});
	
	public MainFrame(Model model) {
//		Thread timerMonitor = new Thread() {
//			public void run() {
//				try {
//					for (;;) {
//						EventQueue.invokeAndWait(new Runnable() {
//							public void run() {
//								setTitle(defaultToolTimer.isRunning() ? "timer running" : "timer stopped");
//							} 
//						});
//						Thread.sleep(100);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		};
//		timerMonitor.start();
		
		
		if (VersionInfo.release) {
			setTitle("JPatch Modeler " + VersionInfo.version);
		} else {
			setTitle("JPatch Modeler " + VersionInfo.version + " compiled " + VersionInfo.compileTime);
		}
		this.model = model;
		if (INSTANCE == null) {
			INSTANCE = this;
			
			try {
				String plaf = Settings.getInstance().lookAndFeelClassname;
				if (plaf.equals("jpatch.boundary.laf.SmoothLookAndFeel"))
					if (jpatch.auxilary.JPatchUtils.isJvmVersionGreaterOrEqual(1, 5))
						UIManager.setLookAndFeel(new SmoothLookAndFeel());
					else
						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				else
					UIManager.setLookAndFeel(plaf);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			helpPanel = new JPanel();
			helpLabel = new JLabel("press right mousebutton (RMB) for popup-menu, click and drag with middle mousebutton (MMB) to move view, use mousewheel to zoom");
			helpLabel.setFont(new Font("SansSerif",Font.PLAIN,10));
			
			ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			
			bgAction = new ButtonGroup();
			
			aViewDef[0] = new ViewDefinition(ViewDefinition.FRONT);
			aViewDef[1] = new ViewDefinition(ViewDefinition.LEFT);
			aViewDef[2] = new ViewDefinition(ViewDefinition.TOP);
			aViewDef[3] = new ViewDefinition(ViewDefinition.BIRDS_EYE);
			
			initScreen();
			/*
			try {
				JPatchScreen.registerDisplayDriver(new DisplayDriver(Class.forName("jpatch.boundary.JPatchCanvas2D"),"Java2D"));
				JPatchScreen.registerDisplayDriver(new DisplayDriver(Class.forName("jpatch.boundary.JPatchCanvas3D"),"Java3D"));
			} catch(Exception exception) {
				System.err.println(exception);
			}
			*/
			/*
			try {
				jpatchScreen = new JPatchScreen(model,JPatchScreen.SINGLE,aViewDef,Class.forName("jpatch.boundary.JPatchCanvas2D"));
			} catch(Exception exception) {
				System.err.println(exception);
			}
			*/
			//setDisplayDriver(model,JPatchScreen.SINGLE,aViewDef,"Java2D");
			//setDisplayDriver("Java2D");
			
			
			
			UIFactory uiFactory = new UIFactory();
			uiFactory.parseLayout(ClassLoader.getSystemResource("jpatch/boundary/layout.xml"));
			
			getContentPane().setLayout(new BorderLayout());
			
			/* -------------------------- */
			//JPanel panelTest = new JPatchScreen();
			//jpatchScreen.add(new JButton("test"));
			//getContentPane().add(panelTest,BorderLayout.CENTER);
			/* -------------------------- */
//			getContentPane().add(jpatchScreen,BorderLayout.CENTER);
			
			Settings settings = Settings.getInstance();
			setLocation(settings.screenPositionX,settings.screenPositionY);
			setSize(settings.screenWidth,settings.screenHeight);
			
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			addWindowListener(new JPatchWindowAdapter());
			
//			mainMenu = new MainMenu();
			
//			setJMenuBar(mainMenu);
			setJMenuBar((JMenuBar) uiFactory.getComponent("menubar"));
			
			jpatchScreen.setPopupMenu((JPopupMenu) uiFactory.getComponent("viewport popup"));
			
//			mainToolBar = new MainToolBar(bgAction);
			//mainToolBar.setOrientation(SwingConstants.HORIZONTAL);
//			getContentPane().add(mainToolBar,BorderLayout.NORTH);
			getContentPane().add(uiFactory.getComponent("main toolbar"), uiFactory.getLayout("main toolbar"));
			
//			boneToolBar = new BoneToolBar(bgAction);
			
//			meshToolBar = new MeshToolBar(bgAction);
//			meshToolBar.addKeyBindings();
//			morphToolBar = new MorphToolBar(bgAction);
			
			//meshToolBar.setOrientation(SwingConstants.VERTICAL);
//			getContentPane().add(meshToolBar,BorderLayout.WEST);
			getContentPane().add(uiFactory.getComponent("edit toolbar"), uiFactory.getLayout("edit toolbar"));
			
			//mainToolBar.setFloatable(false);
			//meshToolBar.setFloatable(false);
			
			initTree(model);
			sideBar = new SideBar(tree);
//			getContentPane().add(sideBar,BorderLayout.EAST);
			
			getContentPane().add(helpPanel,BorderLayout.SOUTH);
			
			
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//			JPanel panel = new JPanel();
//			panel.setLayout(new BorderLayout());
//			panel.add(jpatchScreen, BorderLayout.CENTER);
			splitPane.add(jpatchScreen);
			splitPane.add(sideBar);
			splitPane.setOneTouchExpandable(false);
			splitPane.setContinuousLayout(true);
			splitPane.setResizeWeight(1);
			splitPane.setDividerSize(4);
			getContentPane().add(splitPane, BorderLayout.CENTER);
			
			helpPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			helpPanel.add(helpLabel);
			jpatchScreen.enablePopupMenu(true);
			jpatchScreen.addMMBListener();
			//keyEventDispatcher = new JPatchKeyEventDispatcher();
			//KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
//			addKeyListener(keyAdapter);
			KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
				public boolean dispatchKeyEvent(KeyEvent e) {
					Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
					if (focusOwner instanceof JTextComponent)
						return false;
					switch (e.getID()) {
					case KeyEvent.KEY_PRESSED:
			            Command.getInstance().keyPressed(e);
			          break;
			          case KeyEvent.KEY_RELEASED:
			        	  //
			          break;
			          case KeyEvent.KEY_TYPED:
			        	  //Command.getInstance().keyTyped(e);
			          break;
					}
					return true;
				}
			};
			
//			try {
//				final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/home/sascha/jpatch.out"));
//					java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
//						public void eventDispatched(AWTEvent event) {
//							try {
//								System.out.println(event);
//								out.writeObject(event);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}, AWTEvent.ACTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			try {
//				Robot robot = new Robot();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
			//mainMenu.setFocusable(false);
			//mainToolBar.setFocusable(false);
			//meshToolBar.setFocusable(false);
			//jpatchScreen.setFocusable(false);
			
			//validate();
			
			//disableFocus(this);
			//disableFocus(boneToolBar);
			setFocusable(true);
			//KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
			getRootPane().setFocusable(false);
			setFocusTraversalKeysEnabled(false);
			
//			mainToolBar.setScreenMode(jpatchScreen);
			
			if (Settings.getInstance().newInstallation) {
				if (SplashScreen.instance != null)
					SplashScreen.instance.clearSplash();
				new About(this);
				Settings.getInstance().newInstallation = false;
			}
			
//			meshToolBar.reset();
			//JPatchScreen.setTool(new RotoscopeTool());
			requestFocus();
			//jpatchScreen.setMouseListeners(new ChangeViewListener(MouseEvent.BUTTON1,ChangeViewListener.ROTATE));
			//jpatchScreen.setMouseListeners(new ChangeViewListener(MouseEvent.BUTTON2,ChangeViewListener.ZOOM));
			//jpatchScreen.setMouseListeners(new ChangeViewListener(MouseEvent.BUTTON3,ChangeViewListener.MOVE));
			setSelection(null);
			setVisible(true);
			jpatchScreen.setTool(new DefaultTool());
			if (!VersionInfo.release) {
				String warning = "This is a development version of JPatch.\n" +
						 "It has not been tested and may contain severe bugs!\n" +
						 "You can download a more stable release of JPatch\n" +
						 "from the JPatch homepage at http://www.jpatch.com";
				JOptionPane.showMessageDialog(this, warning, "WARNING", JOptionPane.WARNING_MESSAGE);
			}
		} else {
			throw new IllegalStateException("There can be only one instance of MainFrame");
		}
	}
	
	//public void propertyChange(PropertyChangeEvent event) {
	//	System.out.println(event.getPropertyName());
	//	System.out.println(event.getOldValue());
	//	System.out.println(event.getNewValue());
	//}
	//private void disableFocus(Container container) {
	//	
	//	for (int c = 0; c < container.getComponentCount(); c++) {
	//		Container child = (Container)container.getComponent(c);
	//		child.setFocusable(false);
	//		disableFocus(child);
	//	}
	//	
	//}
	
	public void clearDialog() {
		if (dialog != null) {
			pointDialogLocation = dialog.getLocation();
			dialog.dispose();
			dialog = null;
		}
	}
	
//	public javax.swing.Timer getDefaultToolTimer() {
//		return defaultToolTimer;
//	}
	
//	public void setEditedMorph(Morph morph) {
//		editedMorph = morph;
//	}
	
	public MorphTarget getEditedMorph() {
		return editedMorph;
	}
	
	public XYZLockConstraints getConstraints() {
		return constraints;
	}
	
	public void setEditedMorph(MorphTarget morph) {
		editedMorph = morph;
		if (morph != null) {
//			switchMode(MORPH);
			Command.getInstance().enableCommands(new String[] {
					"add curve segment",
					"add bone",
					"tangent tool",
					"peak tangents",
					"round tangents",
					"clone",
					"extrude",
					"lathe",
					"lathe editor",
					"append",
					"automirror",
					"add stubs",
					"remove stubs",
					"change tangents: round",
					"change tangents: peak",
					"change tangents: spatch"
			}, false);
			Command.getInstance().enableCommand("stop edit morph", true);
			if (selection != null)
				selection.applyMask(Selection.CONTROLPOINTS);
		} else {
//			switchMode(MESH);
			Command.getInstance().enableCommands(new String[] {
					"default tool",
					"rotate tool",
					"add curve segment",
					"add bone",
					"tangent tool",
					"peak tangents",
					"round tangents",
					"clone",
					"extrude",
					"lathe",
					"lathe editor",
					"append",
					"automirror",
					"add stubs",
					"remove stubs",
					"change tangents: round",
					"change tangents: peak",
					"change tangents: spatch"
			}, true);
			Command.getInstance().enableCommand("stop edit morph", false);
		}
		jpatchScreen.update_all();
	}
	
	public void setDialog(JDialog dialog) {
		clearDialog();
		this.dialog = dialog;
		dialog.setLocation(pointDialogLocation);
		dialog.setVisible(true);
	}
		
	public JPatchScreen getJPatchScreen() {
		return jpatchScreen;
	}
	
	public JPatchKeyAdapter getKeyAdapter() {
		return keyAdapter;
	}
	
	//public DisplayFactory getDisplayFactory() {
	//	return displayFactory;
	//}
	
	public Model getModel() {
		return model;
	}
	
	public Anim getAnimation() {
		return animation;
	}
	
	public void newModel() {
		model = new Model();
		animation = null;
		setSelection(null);
		undoManager.clear();
		initTree(model);
		sideBar.setTree(tree);
		validate();
	}
	
	public void newAnimation() {
		model = null;
		animation = new Anim();
		setSelection(null);
		undoManager.clear();
		initTree(animation);
		sideBar.setTree(tree);
		validate();
	}
	
	public static MainFrame getInstance() {
		return INSTANCE;
	}
	
	public void initScreen() {
		int mode = 0;
		switch (Settings.getInstance().viewports.viewportMode) {
		case SINGLE:
			mode = 1;
			break;
		case HORIZONTAL_SPLIT:
			mode = 2;
			break;
		case VERTICAL_SPLIT:
			mode = 3;
			break;
		case QUAD:
			mode = 4;
			break;
		}
		if (jpatchScreen != null) {
			getContentPane().remove(jpatchScreen);
			mode = jpatchScreen.getMode();
		}
		jpatchScreen = new JPatchScreen(model,mode,aViewDef);
		//getContentPane().add(jpatchScreen);
		//jpatchScreen.enablePopupMenu(true);
		//validate();
	}
	
	//public void setDisplayFactory(DisplayFactory displayFactory) {
	//	this.displayFactory = displayFactory;
	//	//model.reset();
	//	initScreen();
	//}
	
	/*
	public static void setRendererJava2D() {
		try {
			int mode = INSTANCE.jpatchScreen.getMode();
			//INSTANCE.jpatchScreen.free();
			INSTANCE.getContentPane().remove(INSTANCE.jpatchScreen);
			INSTANCE.jpatchScreen = new JPatchScreen(INSTANCE.model,mode,INSTANCE.aViewDef,Class.forName("jpatch.boundary.JPatchCanvas2D"));
			INSTANCE.getContentPane().add(INSTANCE.jpatchScreen,BorderLayout.CENTER);
			INSTANCE.jpatchScreen.enablePopupMenu(true);
			INSTANCE.validate();
		} catch(Exception exception) {
			System.err.println(exception);
		}
	}
	
	public static void setRendererJava3D() {
		try {
			int mode = INSTANCE.jpatchScreen.getMode();
			//INSTANCE.jpatchScreen.free();
			INSTANCE.getContentPane().remove(INSTANCE.jpatchScreen);
			INSTANCE.jpatchScreen = new JPatchScreen(INSTANCE.model,mode,INSTANCE.aViewDef,Class.forName("jpatch.boundary.JPatchCanvas3D"));
			INSTANCE.getContentPane().add(INSTANCE.jpatchScreen,BorderLayout.CENTER);
			INSTANCE.jpatchScreen.enablePopupMenu(true);
			INSTANCE.validate();
		} catch(Exception exception) {
			System.err.println(exception);
		}
	}
	
	*/
	public void setTangentDisplay(ControlPoint cp) {
		for (int v = 0; v < 4; v++) {
			aViewDef[v].setTangentHandles(cp);
		}
	}
	
//	public MeshToolBar getMeshToolBar() {
//		return meshToolBar;
//	}
	
	//>>>>> test-add
//	public MainToolBar getMainToolBar() {
//		return mainToolBar;
//	}
//	public MainMenu getMainMenu() {
//		return (MainMenu)mainMenu;
//	}
//	public MorphToolBar getMorphToolBar() {
//		return morphToolBar;
//	}
	//<<<<< test-add
	
	public ButtonGroup getActionButtonGroup() {
		return bgAction;
	}
	
	public JPatchUndoManager getUndoManager() {
		return undoManager;
	}
	
	public Selection getSelection() {
		return selection;
	}
	
	public void setSelection(Selection selection) {
//		getModel().removeSelection(this.selection);
		boolean bonesAndPoints = false;
		boolean moreThanOnePoint = false;
		this.selection = selection;
//		meshToolBar.getWeightButton().setEnabled(selection != null && !selection.isSingle());
		/* unhide new selection */
		if (selection != null) {
			selection.setActive(true);
//			getModel().addSelection(selection);
			selection.setName("ACTIVE SELECTION");
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
				Object object = it.next();
				if (object instanceof ControlPoint) {
					ControlPoint[] stack = ((ControlPoint) object).getStack();
					for (int j = 0; j < stack.length; stack[j++].setHidden(false));
				}
			}
			JPatchTool tool = jpatchScreen.getTool();
			if (tool != null && tool instanceof RotateTool)
				((RotateTool) tool).reInit(selection);		
			int flags = Selection.CONTROLPOINTS;
			if (iMode != MORPH)
				flags |= Selection.MORPHS;
			selection.arm(flags);
			int points = 0, bones = 0;
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
				Object o = it.next();
				if (o instanceof ControlPoint)
					points++;
				else if (o instanceof Bone.BoneTransformable)
					bones++;
			}
			bonesAndPoints = (points > 0 && bones > 1);
			moreThanOnePoint = (points > 1);
		}
		Command.getInstance().enableCommand("select none", selection != null);
		Command.getInstance().enableCommand("invert selection", selection != null);
		Command.getInstance().enableCommand("expand selection", selection != null);
		Command.getInstance().enableCommand("align controlpoints", selection != null);
		Command.getInstance().enableCommand("change tangents: round", selection != null);
		Command.getInstance().enableCommand("change tangents: peak", selection != null);
		Command.getInstance().enableCommand("change tangents: spatch", selection != null);
		Command.getInstance().enableCommand("automirror", selection != null);
		Command.getInstance().enableCommand("flip x", moreThanOnePoint);
		Command.getInstance().enableCommand("flip y", moreThanOnePoint);
		Command.getInstance().enableCommand("flip z", moreThanOnePoint);
		Command.getInstance().enableCommand("flip patches", moreThanOnePoint);
		Command.getInstance().enableCommand("make patch", moreThanOnePoint);
		Command.getInstance().enableCommand("align patches", moreThanOnePoint);
		Command.getInstance().enableCommand("make patch", moreThanOnePoint);
		Command.getInstance().enableCommand("add stubs", moreThanOnePoint);
		Command.getInstance().enableCommand("remove stubs", moreThanOnePoint);
		Command.getInstance().enableCommand("make patch", MakeFivePointPatchAction.checkSelection() != null);
		Command.getInstance().enableCommand("weight selection tool", moreThanOnePoint);
		Command.getInstance().enableCommand("rotate tool", moreThanOnePoint);
		Command.getInstance().enableCommand("extrude", moreThanOnePoint);
		Command.getInstance().enableCommand("lathe", moreThanOnePoint);
		Command.getInstance().enableCommand("clone", moreThanOnePoint);
		Command.getInstance().enableCommand("assign controlpoints to bones", bonesAndPoints);
	}
	
	public java.util.List getSelectionsContaining(ControlPoint cp) {
		ArrayList list = new ArrayList();
		for (Iterator it = getModel().getSelections().iterator(); it.hasNext(); ) {
			Selection selection = (Selection) it.next();
			if (selection.contains(cp)) list.add(selection);
		}
		if (getSelection() != null && getSelection().contains(cp))
			list.add(getSelection());
		return list;
	}
	
	public void setHelpText(String text) {
		helpLabel.setText(text);
	}
	
	public void clearHelpText() {
		helpLabel.setText(" ");
	}
	
//	public void switchMode(int mode) {
//		if (mode != iMode) {
//			switch(iMode) {
//				case MESH:
//					meshToolBar.removeKeyBindings();
//					getContentPane().remove(meshToolBar);
//					break;
////				case BONE:
////					getContentPane().remove(boneToolBar);
////					break;
//				case MORPH:
//					morphToolBar.removeKeyBindings();
//					getContentPane().remove(morphToolBar);
//					break;
//			}
//			iMode = mode;
//			switch(iMode) {
//				case MESH:
//					getContentPane().add(meshToolBar,BorderLayout.WEST);
//					meshToolBar.addKeyBindings();
//					//meshToolBar.repaint();
//					break;
////				case BONE:
////					getContentPane().add(boneToolBar,BorderLayout.WEST);
////					//boneToolBar.validate();
////					break;
//				case MORPH:
//					getContentPane().add(morphToolBar,BorderLayout.WEST);
//					morphToolBar.addKeyBindings();
//					//boneToolBar.repaint();
//					break;
//			}
//			validate();
//			repaint();
//		}
//	}
//	
//	public int getMode() {
//		return iMode;
//	}
	
//	public PointSelection getPointSelection() {
//		Class classPointSelection = PointSelection.getPointSelectionClass();
//		PointSelection pointSelection = null;
//		if (selection != null && classPointSelection.isAssignableFrom(selection.getClass())) {
//			pointSelection = (PointSelection)selection;
//		}
//		return pointSelection;
//	}
	
	public JPatchTree getTree() {
		return tree;
	}
	
	public SideBar getSideBar() {
		return sideBar;
	}
	
	public void selectTreeNode(TreeNode treeNode) {
		tree.setSelectionPath(new TreePath(getTreeModel().getPathToRoot(treeNode)));
	}
	
	public MutableTreeNode getRootTreenode() {
		return treenodeRoot;
	}
	
//	public MutableTreeNode getModelTreenode() {
//		return treenodeModel;
//	}
	
	public DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) tree.getModel();
	}
	
	private void initTree(MutableTreeNode rootNode) {
//		treenodeRoot.insert(model, 0);
//		treenodeModel.insert(model.getTreenodeSelections(), 0);
//		treenodeModel.insert(model.getTreenodeMaterials(), 1);
//		treenodeModel.insert(model.getTreenodeExpressions(), 2);
//		treenodeModel.insert(model.getTreenodeBones(), 3);
		//JPatchTreeNode treenodeModel = new JPatchTreeNode(JPatchTreeNode.MODEL,treenodeRoot);
		
		//JPatchTreeNode treenodeMorphGroup1 = new JPatchTreeNode(JPatchTreeNode.MORPHGROUP,treenodeExpressions);
		//JPatchTreeNode treenodeMorphGroup2 = new JPatchTreeNode(JPatchTreeNode.MORPHGROUP,treenodeExpressions);
		//treenodeMaterials.add(new JPatchTreeLeaf(JPatchTreeLeaf.MATERIAL,null));
		//treenodeMaterials.add(new JPatchTreeLeaf(JPatchTreeLeaf.MATERIAL,null));
		//treenodeExpressions.add(new JPatchTreeLeaf(JPatchTreeLeaf.MORPH,null));
		//treenodeMorphGroup1.add(new JPatchTreeLeaf(JPatchTreeLeaf.MORPH,null));
		//treenodeMorphGroup2.add(new JPatchTreeLeaf(JPatchTreeLeaf.MORPH,null));
		
		treenodeRoot = new DefaultMutableTreeNode("ROOT");
		tree = new JPatchTree(treenodeRoot);
		((DefaultTreeModel) tree.getModel()).insertNodeInto(rootNode, treenodeRoot, 0);
		((DefaultTreeModel) tree.getModel()).reload();
		tree.setCellRenderer(new JPatchTreeCellRenderer());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.expandRow(0);
		//tree.makeVisible(treenodeMaterials.getTreePath());
		//tree.setDragEnabled(true);
	}
}
