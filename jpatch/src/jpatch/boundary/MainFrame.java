package jpatch.boundary;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
//import java.beans.*;
import javax.swing.*;
import javax.swing.tree.*;

import jpatch.*;
import jpatch.entity.*;
import jpatch.boundary.tools.*;
//	import jpatch.boundary.mouse.*;		//remove
import jpatch.control.*;
import jpatch.control.edit.*;
import jpatch.boundary.laf.*;

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
	private JPatchScreen jpatchScreen;
	private static MainFrame INSTANCE;
	private MainToolBar mainToolBar;
	private MeshToolBar meshToolBar;
//	private BoneToolBar boneToolBar;
	private MorphToolBar morphToolBar;
	private JPanel helpPanel;
	private JLabel helpLabel;
	private JMenuBar mainMenu;
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
	private JPatchTreeNode treenodeRoot;
	private Morph editedMorph;
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
				String plaf = JPatchSettings.getInstance().strPlafClassName;
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
			
			
			
			
			
			getContentPane().setLayout(new BorderLayout());
			
			/* -------------------------- */
			//JPanel panelTest = new JPatchScreen();
			//jpatchScreen.add(new JButton("test"));
			//getContentPane().add(panelTest,BorderLayout.CENTER);
			/* -------------------------- */
			getContentPane().add(jpatchScreen,BorderLayout.CENTER);
			
			JPatchSettings settings = JPatchSettings.getInstance();
			setLocation(settings.iScreenX,settings.iScreenY);
			setSize(settings.iScreenWidth,settings.iScreenHeight);
			
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			addWindowListener(new JPatchWindowAdapter());
			
			mainMenu = new MainMenu();
			
			setJMenuBar(mainMenu);
			
			mainToolBar = new MainToolBar(bgAction);
			//mainToolBar.setOrientation(SwingConstants.HORIZONTAL);
			getContentPane().add(mainToolBar,BorderLayout.NORTH);
			
//			boneToolBar = new BoneToolBar(bgAction);
			
			meshToolBar = new MeshToolBar(bgAction);
			meshToolBar.addKeyBindings();
			morphToolBar = new MorphToolBar(bgAction);
			
			//meshToolBar.setOrientation(SwingConstants.VERTICAL);
			getContentPane().add(meshToolBar,BorderLayout.WEST);
			
			//mainToolBar.setFloatable(false);
			//meshToolBar.setFloatable(false);
			
			initTree();
			sideBar = new SideBar(tree);
			getContentPane().add(sideBar,BorderLayout.EAST);
			
			getContentPane().add(helpPanel,BorderLayout.SOUTH);
			helpPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			helpPanel.add(helpLabel);
			jpatchScreen.enablePopupMenu(true);
			jpatchScreen.addMMBListener();
			//keyEventDispatcher = new JPatchKeyEventDispatcher();
			//KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
			addKeyListener(keyAdapter);
			
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
			
			mainToolBar.setScreenMode(jpatchScreen);
			
			if (JPatchSettings.getInstance().bFirstStart) {
				if (SplashScreen.instance != null)
					SplashScreen.instance.clearSplash();
				new About(this);
				JPatchSettings.getInstance().bFirstStart = false;
			}
			
			meshToolBar.reset();
			//JPatchScreen.setTool(new RotoscopeTool());
			requestFocus();
			//jpatchScreen.setMouseListeners(new ChangeViewListener(MouseEvent.BUTTON1,ChangeViewListener.ROTATE));
			//jpatchScreen.setMouseListeners(new ChangeViewListener(MouseEvent.BUTTON2,ChangeViewListener.ZOOM));
			//jpatchScreen.setMouseListeners(new ChangeViewListener(MouseEvent.BUTTON3,ChangeViewListener.MOVE));
			setVisible(true);
			
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
	
	public void setEditedMorph(Morph morph) {
		editedMorph = morph;
	}
	
	public Morph getEditedMorph() {
		return editedMorph;
	}
	
	public XYZLockConstraints getConstraints() {
		return constraints;
	}
	
	public void editMorph(Morph morph) {
		setEditedMorph(morph);
		if (morph != null) {
			switchMode(MORPH);
		} else {
			switchMode(MESH);
		}
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
	
	public void NEW() {
		//INSTANCE.dispose();
		//INSTANCE = null;
		//INSTANCE = new MainFrame(new Model());
		//getContentPane().remove(sideBar);
		model = new Model();
		selection = null;
		undoManager.clear();
		initTree();
		sideBar.setTree(tree);
		//getContentPane().add(sideBar,BorderLayout.EAST);
		meshToolBar.reset();
		//repaint();
		//sideBar.repaint();
		JPatchSettings.getInstance().strJPatchFile = "";
		validate();
	}
	
	public static MainFrame getInstance() {
		return INSTANCE;
	}
	
	public void initScreen() {
		int mode = JPatchSettings.getInstance().iScreenMode;
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
	
	public MeshToolBar getMeshToolBar() {
		return meshToolBar;
	}
	
	//>>>>> test-add
	public MainToolBar getMainToolBar() {
		return mainToolBar;
	}
	public MainMenu getMainMenu() {
		return (MainMenu)mainMenu;
	}
	public MorphToolBar getMorphToolBar() {
		return morphToolBar;
	}
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
		this.selection = selection;
		meshToolBar.getWeightButton().setEnabled(selection != null && !selection.isSingle());
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
		}
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
	
	public void switchMode(int mode) {
		if (mode != iMode) {
			switch(iMode) {
				case MESH:
					meshToolBar.removeKeyBindings();
					getContentPane().remove(meshToolBar);
					break;
//				case BONE:
//					getContentPane().remove(boneToolBar);
//					break;
				case MORPH:
					morphToolBar.removeKeyBindings();
					getContentPane().remove(morphToolBar);
					break;
			}
			iMode = mode;
			switch(iMode) {
				case MESH:
					getContentPane().add(meshToolBar,BorderLayout.WEST);
					meshToolBar.addKeyBindings();
					//meshToolBar.repaint();
					break;
//				case BONE:
//					getContentPane().add(boneToolBar,BorderLayout.WEST);
//					//boneToolBar.validate();
//					break;
				case MORPH:
					getContentPane().add(morphToolBar,BorderLayout.WEST);
					morphToolBar.addKeyBindings();
					//boneToolBar.repaint();
					break;
			}
			validate();
			repaint();
		}
	}
	
	public int getMode() {
		return iMode;
	}
	
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
	
	public JPatchTreeNode getRootTreenode() {
		return treenodeRoot;
	}
	
	private void initTree() {
		treenodeRoot = new JPatchTreeNode(JPatchTreeNode.ROOT,null,"ROOT");
		treenodeRoot.add(model);
		//JPatchTreeNode treenodeModel = new JPatchTreeNode(JPatchTreeNode.MODEL,treenodeRoot);
		
		//JPatchTreeNode treenodeMorphGroup1 = new JPatchTreeNode(JPatchTreeNode.MORPHGROUP,treenodeExpressions);
		//JPatchTreeNode treenodeMorphGroup2 = new JPatchTreeNode(JPatchTreeNode.MORPHGROUP,treenodeExpressions);
		//treenodeMaterials.add(new JPatchTreeLeaf(JPatchTreeLeaf.MATERIAL,null));
		//treenodeMaterials.add(new JPatchTreeLeaf(JPatchTreeLeaf.MATERIAL,null));
		//treenodeExpressions.add(new JPatchTreeLeaf(JPatchTreeLeaf.MORPH,null));
		//treenodeMorphGroup1.add(new JPatchTreeLeaf(JPatchTreeLeaf.MORPH,null));
		//treenodeMorphGroup2.add(new JPatchTreeLeaf(JPatchTreeLeaf.MORPH,null));
		
		tree = new JPatchTree(treenodeRoot);
		tree.setCellRenderer(new JPatchTreeCellRenderer());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		//tree.makeVisible(treenodeMaterials.getTreePath());
		//tree.setDragEnabled(true);
	}
}
