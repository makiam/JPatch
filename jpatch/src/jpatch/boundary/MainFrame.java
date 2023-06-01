package jpatch.boundary;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.vecmath.*;

import jpatch.*;
import jpatch.entity.*;
import jpatch.boundary.settings.Settings;
import jpatch.boundary.sidebar.SideBar;
import jpatch.boundary.timeline.TimelineEditor;
import jpatch.boundary.tools.*;
//	import jpatch.boundary.mouse.*;		//remove
import jpatch.control.*;
import jpatch.boundary.mouse.TreeMouseAdapter;
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
	private Animation animation;
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
	private OLDSelection selection;
//	private JPatchUndoManager undoManager = new JPatchUndoManager(50);
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
	private JPanel animPanel;
	private VcrControls vcrControls;
	private TimelineEditor timelineEditor;
	private JSplitPane splitPaneV;
	
	private JMenu viewCameraMenu;
	
//	private javax.swing.Timer defaultToolTimer = new javax.swing.Timer(0, new ActionListener() {
//		public void actionPerformed(ActionEvent event) {
//			defaultToolTimer.stop();
//			meshToolBar.reset();
//		}
//	});
	
	public MainFrame() {
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
		try {
			//IndexColorModel icm = new IndexColorModel(8, 3, new byte[] {0, -127, 0}, new byte[] {0, -127, 0}, new byte[] {0, -127, 0}, 2);
			//BufferedImage im = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_INDEXED);
			
			Image im = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("jpatch/images/logo.gif"));
		//	BufferedImage im2 = ImageIO.read(ClassLoader.getSystemResource("jpatch/images/logo.gif"));
			//im.createGraphics().drawImage(im2, 0, 0, null);
			setIconImage(im);
		} catch (Exception e) { }
		
//		setTitle("JPatch " + VersionInfo.version);
		
//		this.model = model;
		if (INSTANCE == null) {
			INSTANCE = this;
			
			
//			try {
//				String plaf = Settings.getInstance().lookAndFeelClassname;
//				if (plaf.equals("jpatch.boundary.laf.SmoothLookAndFeel"))
//					if (jpatch.auxilary.JPatchUtils.isJvmVersionGreaterOrEqual(1, 5))
//						UIManager.setLookAndFeel(new SmoothLookAndFeel());
//					else
//						UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//				else
//					UIManager.setLookAndFeel(plaf);
//				
//					
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			UIManager.put("ToolTip.hideAccelerator", true);
			
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
			viewCameraMenu = (JMenu) uiFactory.getComponent("view camera menu");
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
			
			if (SplashScreen.instance != null)
				SplashScreen.instance.setText("Initializing");
			
//			initTree(model);
			sideBar = new SideBar();
//			JPanel panel2 = new JPanel();
//			panel2.setLayout(new BorderLayout());
//			panel2.add(sideBar, BorderLayout.CENTER);
//			panel2.add(new MemoryMonitor(), BorderLayout.SOUTH);
			
//			getContentPane().add(sideBar,BorderLayout.EAST);
			
			getContentPane().add(helpPanel,BorderLayout.SOUTH);
			
			splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPaneV.add(jpatchScreen);
			splitPaneV.setOneTouchExpandable(false);
			splitPaneV.setContinuousLayout(true);
			splitPaneV.setResizeWeight(1);
			splitPaneV.setDividerSize(4);
			splitPaneV.setDividerLocation(getHeight() - 200);
			splitPaneV.setBackground(settings.colors.background.get());
			JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//			JPanel panel = new JPanel();
//			panel.setLayout(new BorderLayout());
//			panel.add(jpatchScreen, BorderLayout.CENTER);
			splitPaneH.add(splitPaneV);
			splitPaneH.add(sideBar);
			splitPaneH.setOneTouchExpandable(false);
			splitPaneH.setContinuousLayout(true);
			splitPaneH.setResizeWeight(1);
			splitPaneH.setDividerSize(4);
			splitPaneH.setDividerLocation(getWidth() - 310);
			splitPaneV.setBackground(settings.colors.background.get());
			getContentPane().add(splitPaneH, BorderLayout.CENTER);
			
			helpPanel.setLayout(new BorderLayout());
			helpPanel.add(helpLabel, BorderLayout.WEST);
			helpPanel.add(new MemoryMonitor(), BorderLayout.EAST);
			jpatchScreen.enablePopupMenu(true);
			jpatchScreen.addMMBListener();
			
//			Actions.getInstance().mapKeys(jpatchScreen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), jpatchScreen.getActionMap());
			
			//keyEventDispatcher = new JPatchKeyEventDispatcher();
			//KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
//			addKeyListener(keyAdapter);

//			KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
//				public boolean dispatchKeyEvent(KeyEvent e) {
//					Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//					if (focusOwner instanceof JTextComponent)
//						return false;
//					switch (e.getID()) {
//					case KeyEvent.KEY_PRESSED:
//			            Actions.getInstance().keyPressed(e);
//			          break;
//			          case KeyEvent.KEY_RELEASED:
//			        	  //
//			          break;
//			          case KeyEvent.KEY_TYPED:
//			        	  //Actions.getInstance().keyTyped(e);
//			          break;
//					}
//					return true;
//				}
//			};
			
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
			
//			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
			//mainMenu.setFocusable(false);
			//mainToolBar.setFocusable(false);
			//meshToolBar.setFocusable(false);
			//jpatchScreen.setFocusable(false);
			
			//validate();
			
			//disableFocus(this);
			//disableFocus(boneToolBar);
//			setFocusable(true);
			//KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
//			getRootPane().setFocusable(false);
//			setFocusTraversalKeysEnabled(false);
			
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
//			newModel();
			setVisible(true);
			jpatchScreen.setTool(Tools.defaultTool);
			
//			if (!VersionInfo.release) {
//				System.out.println("Displaying warning message...");
//				
//					
//				String warning = "This is a development version of JPatch.\n" +
//						 "It has not been tested and may contain severe bugs!\n" +
//						 "You can download a more stable release of JPatch\n" +
//						 "from the JPatch homepage at http://www.jpatch.com";
//				JOptionPane.showMessageDialog(this, warning, "WARNING", JOptionPane.WARNING_MESSAGE);
//			}
			System.out.println("JPatch is ready.");
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
	
	public JMenu getViewCameraMenu() {
		return viewCameraMenu;
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
			OldActions.getInstance().enableActions(new String[] {
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
					"change tangents: spatch",
					"save",
					"save as"
			}, false);
			OldActions.getInstance().enableAction("stop edit morph", true);
			if (selection != null)
				selection.applyMask(OLDSelection.CONTROLPOINTS);
		} else {
//			switchMode(MESH);
			OldActions.getInstance().enableActions(new String[] {
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
					"change tangents: spatch",
					"save",
					"save as"
			}, true);
			OldActions.getInstance().enableAction("stop edit morph", false);
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
	
	public Animation getAnimation() {
		return animation;
	}
	
	public void newModel() {
		model = new Model();
		animation = null;
		setEditedMorph(null);
		for (ViewDefinition viewDef : aViewDef)
			viewDef.setScale(0.03f);
		setSelection(null);
//		undoManager.clear();
//		initTree(model);
		sideBar.setTree(tree);
		validate();
		if (vcrControls != null)
			vcrControls.stop();
		if (animPanel != null) {
			splitPaneV.remove(animPanel);
		}
		
//		Actions.getInstance().enableAction("open", true);
		OldActions.getInstance().enableAction("append", true);
//		Actions.getInstance().enableAction("save", true);
//		Actions.getInstance().enableAction("save as", true);
		
		OldActions.getInstance().enableAction("add curve segment", true);
		OldActions.getInstance().enableAction("add bone", true);
		OldActions.getInstance().enableAction("weight selection tool", true);
		OldActions.getInstance().enableAction("rotoscope tool", true);
		OldActions.getInstance().enableAction("detach", true);
		OldActions.getInstance().enableAction("tangent tool", true);
		OldActions.getInstance().enableAction("peak tangents", true);
		OldActions.getInstance().enableAction("round tangents", true);
		OldActions.getInstance().enableAction("lathe editor", true);
		OldActions.getInstance().enableAction("compute patches", true);
		OldActions.getInstance().enableAction("zoom to fit", true);
		OldActions.getInstance().enableAction("select points", true);
		OldActions.getInstance().enableAction("select bones", true);
		OldActions.getInstance().enableAction("lock points", true);
		OldActions.getInstance().enableAction("lock bones", true);
		OldActions.getInstance().enableAction("hide", true);
		
		Settings.getInstance().startup = Settings.Startup.MODELER;
		
		jpatchScreen.update_all();
//		Actions.getInstance().enableAction("show anim controls", false);
		setFilename("");
	}
	
	public void setFilename(String filename) {
		if (!filename.equals(""))
			setTitle(filename + " - " + "JPatch " + VersionInfo.version);
		else
			setTitle("JPatch " + VersionInfo.version);
	}
	
	public void newAnimation() {
		model = null;
		animation = new Animation();
		setEditedMorph(null);
		for (ViewDefinition viewDef : aViewDef)
			viewDef.setScale(0.003f);
		setSelection(null);
//		undoManager.clear();
		initTree(animation);
		sideBar.setTree(tree);
		validate();
		if (vcrControls != null)
			vcrControls.stop();
		vcrControls = new VcrControls();
		if (animPanel != null)
			splitPaneV.remove(animPanel);
		animPanel = new JPanel();
		animPanel.setMinimumSize(new Dimension(320, 80));
		animPanel.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.add(vcrControls);
		animPanel.add(panel, BorderLayout.SOUTH);
//		SmartScrollPane smartScrollPane = new SmartScrollPane();
		//motionCurveDisplay = new MotionCurveDisplay(smartScrollPane);
		//smartScrollPane.setVirtualCanvas(motionCurveDisplay);
		timelineEditor = new TimelineEditor();
//		timelineEditor.test();
		animPanel.add(timelineEditor, BorderLayout.CENTER);
		splitPaneV.add(animPanel);
//		vcrDialog.setSize(800, 600);
//		vcrDialog.setVisible(true);
		OLDCamera camera = new OLDCamera("Camera 1");
		camera.setPosition(new Point3d(0, 150, -300));
		camera.setOrientation(0, Math.atan2(-150, 300), 0);
		AnimLight light = new AnimLight();
		light.setName("Light 1");
		light.setPosition(new Point3d(-300, 200, -300));
		
		animation.addCamera(camera, null);
		animation.addLight(light, null);
		
		
		
//		Actions.getInstance().enableAction("open", false);
		OldActions.getInstance().enableAction("append", false);
//		Actions.getInstance().enableAction("save", false);
//		Actions.getInstance().enableAction("save as", false);
		
		OldActions.getInstance().enableAction("add curve segment", false);
		OldActions.getInstance().enableAction("add bone", false);
		OldActions.getInstance().enableAction("weight selection tool", false);
		OldActions.getInstance().enableAction("rotoscope tool", false);
		OldActions.getInstance().enableAction("detach", false);
		OldActions.getInstance().enableAction("tangent tool", false);
		OldActions.getInstance().enableAction("peak tangents", false);
		OldActions.getInstance().enableAction("round tangents", false);
		OldActions.getInstance().enableAction("lathe editor", false);
		OldActions.getInstance().enableAction("compute patches", false);
		OldActions.getInstance().enableAction("zoom to fit", false);
		OldActions.getInstance().enableAction("select points", false);
		OldActions.getInstance().enableAction("select bones", false);
		OldActions.getInstance().enableAction("lock points", false);
		OldActions.getInstance().enableAction("lock bones", false);
		OldActions.getInstance().enableAction("hide", false);
		OldActions.getInstance().enableAction("stop edit morph", false);
		
		Settings.getInstance().startup = Settings.Startup.ANIMATOR;
		
		jpatchScreen.update_all();
//		Actions.getInstance().enableAction("show anim controls", true);
//		jpatchScreen.getActiveViewport().getViewDefinition().setCamera(animation.getCameras().get(0));
		setFilename("");
	}
	
	public void showAnimControls() {
//		if (vcrDialog != null)
//			vcrDialog.setVisible(true);
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
//		if (jpatchScreen != null) {
//			getContentPane().remove(jpatchScreen);
//			mode = jpatchScreen.getMode();
//		}
		jpatchScreen = new JPatchScreen(mode, aViewDef);
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
	public void setTangentDisplay(OLDControlPoint cp) {
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
	
//	public JPatchUndoManager getUndoManager() {
//		return undoManager;
//	}
	
	public OLDSelection getSelection() {
		return selection;
	}
	
	public void setSelection(OLDSelection selection) {
//		getModel().removeSelection(this.selection);
		boolean bonesAndPoints = false;
		boolean moreThanOnePoint = false;
		boolean bone = false;
		this.selection = selection;
//		meshToolBar.getWeightButton().setEnabled(selection != null && !selection.isSingle());
		/* unhide new selection */
		if (selection != null) {
			selection.setActive(true);
//			getModel().addSelection(selection);
			selection.setName("ACTIVE SELECTION");
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
				Object object = it.next();
				if (object instanceof OLDControlPoint) {
					OLDControlPoint[] stack = ((OLDControlPoint) object).getStack();
					for (int j = 0; j < stack.length; stack[j++].setHidden(false));
				}
			}
			JPatchTool tool = jpatchScreen.getTool();
			if (tool != null && tool instanceof RotateTool)
				((RotateTool) tool).reInit(selection);		
			int flags = OLDSelection.CONTROLPOINTS;
			if (iMode != MORPH)
				flags |= OLDSelection.MORPHS;
			if (animation == null)
				selection.arm(flags);
			int points = 0, bones = 0;
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); ) {
				Object o = it.next();
				if (o instanceof OLDControlPoint)
					points++;
				else if (o instanceof OLDBone.BoneTransformable)
					bones++;
			}
			bonesAndPoints = (points > 0 && bones > 0);
			moreThanOnePoint = (points > 1);
			bone = (bones > 0);
			jpatchScreen.setFocusTraversalKeysEnabled(!(selection.isSingle() && selection.getHotObject() instanceof OLDControlPoint));
		}
		OldActions.getInstance().enableAction("select none", selection != null);
		OldActions.getInstance().enableAction("invert selection", selection != null);
		OldActions.getInstance().enableAction("expand selection", selection != null);
		OldActions.getInstance().enableAction("align controlpoints", selection != null);
		OldActions.getInstance().enableAction("change tangents: round", selection != null);
		OldActions.getInstance().enableAction("change tangents: peak", selection != null);
		OldActions.getInstance().enableAction("change tangents: spatch", selection != null);
		OldActions.getInstance().enableAction("automirror", selection != null);
		OldActions.getInstance().enableAction("flip x", moreThanOnePoint);
		OldActions.getInstance().enableAction("flip y", moreThanOnePoint);
		OldActions.getInstance().enableAction("flip z", moreThanOnePoint);
		OldActions.getInstance().enableAction("flip patches", moreThanOnePoint);
		OldActions.getInstance().enableAction("make patch", moreThanOnePoint);
		OldActions.getInstance().enableAction("align patches", moreThanOnePoint);
		OldActions.getInstance().enableAction("make patch", moreThanOnePoint);
		OldActions.getInstance().enableAction("add stubs", moreThanOnePoint);
		OldActions.getInstance().enableAction("remove stubs", moreThanOnePoint);
		OldActions.getInstance().enableAction("make patch", MakeFivePointPatchAction.checkSelection() != null);
		OldActions.getInstance().enableAction("weight selection tool", moreThanOnePoint);
		OldActions.getInstance().enableAction("knife tool", moreThanOnePoint);
		OldActions.getInstance().enableAction("rotate tool", moreThanOnePoint || bone || (selection != null && selection.getHotObject() instanceof AnimObject));
		OldActions.getInstance().enableAction("extrude", moreThanOnePoint);
		OldActions.getInstance().enableAction("lathe", moreThanOnePoint);
		OldActions.getInstance().enableAction("clone", moreThanOnePoint);
		OldActions.getInstance().enableAction("assign controlpoints to bones", bonesAndPoints);
		if (selection != null && selection.getHotObject() instanceof AnimObject) {
			timelineEditor.setAnimObject((AnimObject) selection.getHotObject());
			selectTreeNode((AnimObject) selection.getHotObject());
		}
		if (selection == null) {
			jpatchScreen.setFocusTraversalKeysEnabled(true);
		}
	}
	
	public java.util.List getSelectionsContaining(OLDControlPoint cp) {
		ArrayList list = new ArrayList();
		for (Iterator it = getModel().getSelections().iterator(); it.hasNext(); ) {
			OLDSelection selection = (OLDSelection) it.next();
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
		TreePath treePath = new TreePath(getTreeModel().getPathToRoot(treeNode));
		tree.setSelectionPath(treePath);
		requestFocusInWindow();
//		tree.makeVisible(treePath);
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
		tree.setCellRenderer(new OLDJPatchTreeCellRenderer());
//		tree.setCellEditor(new DefaultCellEditor(new JTextField()));
//		tree.setEditable(true);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.expandRow(0);
		tree.addMouseListener(new TreeMouseAdapter());
		//tree.makeVisible(treenodeMaterials.getTreePath());
		//tree.setDragEnabled(true);
	}

	public TimelineEditor getTimelineEditor() {
		return timelineEditor;
	}
	
	public VcrControls getVcrControls() {
		return vcrControls;
	}
}
