package jpatch.boundary;

import javax.swing.*;
import jpatch.boundary.action.*;
import jpatch.boundary.laf.SmoothLookAndFeel;

public final class MainMenu extends JMenuBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JMenu menuFile;
	private JMenu menuImport;
	private JMenu menuExport;
	private JMenu menuTest;
	private JMenu menuHelp;
	private JMenu menuOptions;
	private JMenu menuLight;
	private JMenu menuPlaf;
	private JMenu menuTools;
	
	private JRadioButtonMenuItem miLightOff;
	private JRadioButtonMenuItem miLightSimple;
	private JRadioButtonMenuItem miLightHead;
	private JRadioButtonMenuItem miLightThreePoint;
	private JCheckBoxMenuItem miLightSticky;
	private JCheckBoxMenuItem miSync;
	private JCheckBoxMenuItem miFlip;
	//private JCheckBoxMenuItem miCull;
	
	//private Action actionComputePatches = new ComputePatchesAction();
	//private Action actionExtendCurve = new NextCurveAction();
	//private Action actionNextCurve = new NextCurveAction();
	
	public MainMenu() {

		
		menuFile = new JMenu("File");
		menuImport = new JMenu("Import");
		menuExport = new JMenu("Export");
		menuTest = new JMenu("Test");
		menuHelp = new JMenu("Help");
		menuOptions = new JMenu("Options");	
		menuLight = new JMenu("Light");
		menuTools = new JMenu("Tools");
		
		miLightOff = new JRadioButtonMenuItem(new LightingOffAction(MainFrame.getInstance().getJPatchScreen()));
		miLightSimple = new JRadioButtonMenuItem(new LightingSimpleAction(MainFrame.getInstance().getJPatchScreen()));
		miLightHead = new JRadioButtonMenuItem(new LightingHeadAction(MainFrame.getInstance().getJPatchScreen()));
		miLightThreePoint = new JRadioButtonMenuItem(new LightingThreePointAction(MainFrame.getInstance().getJPatchScreen()));
		miLightSticky = new JCheckBoxMenuItem(new LightingStickyAction(MainFrame.getInstance().getJPatchScreen()));
		miSync = new JCheckBoxMenuItem(new SyncScreensAction());
		miFlip = new JCheckBoxMenuItem(new BackfaceNormalFlipAction());
		//miCull = new JCheckBoxMenuItem(new BackfaceCullingAction());
		
		ButtonGroup bgLight = new ButtonGroup();
		bgLight.add(miLightOff);
		bgLight.add(miLightSimple);
		bgLight.add(miLightHead);
		bgLight.add(miLightThreePoint);
		
		miLightOff.setSelected(MainFrame.getInstance().getJPatchScreen().getLightingMode() == JPatchScreen.LIGHT_OFF);
		miLightSimple.setSelected(MainFrame.getInstance().getJPatchScreen().getLightingMode() == JPatchScreen.LIGHT_SIMPLE);
		miLightHead.setSelected(MainFrame.getInstance().getJPatchScreen().getLightingMode() == JPatchScreen.LIGHT_HEAD);
		miLightThreePoint.setSelected(MainFrame.getInstance().getJPatchScreen().getLightingMode() == JPatchScreen.LIGHT_THREE_POINT);
		miLightSticky.setState(MainFrame.getInstance().getJPatchScreen().isStickyLight());
		miSync.setState(MainFrame.getInstance().getJPatchScreen().isSynchronized());
		miFlip.setState(MainFrame.getInstance().getJPatchScreen().flipBackfacingNormals());
		
		menuLight.add(miLightOff);
		menuLight.add(miLightSimple);
		menuLight.add(miLightHead);
		menuLight.add(miLightThreePoint);
		menuLight.addSeparator();
		menuLight.add(miLightSticky);
		
		menuImport.add(new ImportSPatchAction());
		menuImport.add(new ImportAnimationMasterAction());
		//menuImport.add(new ImportJPatchAction());
		
		JMenuItem mi;
		mi = new JMenuItem(new NewAction());
		mi.setIcon(null);
		mi.setText("New");
		menuFile.add(mi);
		mi = new JMenuItem(new ImportJPatchAction(true));
		mi.setIcon(null);
		mi.setText("Open (new)");
		menuFile.add(mi);
		mi = new JMenuItem(new ImportJPatchAction(false));
		mi.setIcon(null);
		mi.setText("Open (append)");
		menuFile.add(mi);
		mi = new JMenuItem(new SaveAsAction(false));
		mi.setIcon(null);
		mi.setText("Save");
		menuFile.add(mi);
		mi = new JMenuItem(new SaveAsAction(true));
		mi.setIcon(null);
		mi.setText("Save As...");
		menuFile.add(mi);
		menuFile.addSeparator();
		menuFile.add(menuImport);
		
		menuTest.add(new DumpAction());
		menuTest.add(new DumpUndoStackAction());
		menuTest.add(new CheckModelAction());
		menuTest.add(new ControlPointBrowserAction());
		//menuTest.add(new TriangulateTestAction());
		
		//menuTest.add(actionComputePatches);
		//menuTest.add(new TestMakeFivePointPatchAction());
		//menuTest.add(new ExtendSelectionAction());
		//menuTest.add(new NextCurveAction());
		//menuTest.add(new CloneAction());
		
		//menuExport.add(new TestRenderPovRayAction());
		//menuExport.add(new TestRenderRibAction());
		//menuExport.add(new GenericOutputAction());
		menuExport.add(new ExportWavefrontAction());
		menuExport.add(new ExportPovrayAction());
		menuExport.add(new ExportRibAction());
		//menuExport.add(new TestRenderInyoAction());
		
		//menuExport.add(new TestRenderTest2Action());
		//menuTest.add(new TestXMLoutAction());
		//menuTest.add(new TestSetQualityAction());
		
		menuFile.add(menuExport);
		menuFile.addSeparator();
		menuFile.add(new QuitAction());
		
		menuHelp.add(new AboutAction());
		
		JMenu menuRenderer = new JMenu("Renderer");
		JRadioButtonMenuItem miJava2D = new JRadioButtonMenuItem(new SwitchRendererAction(0, "Java2D"));
		JRadioButtonMenuItem miSoftware = new JRadioButtonMenuItem(new SwitchRendererAction(1, "Software"));
		JRadioButtonMenuItem miOpenGl = new JRadioButtonMenuItem(new SwitchRendererAction(2, "OpenGL"));
		ButtonGroup bgRenderer = new ButtonGroup();
		bgRenderer.add(miJava2D);
		bgRenderer.add(miSoftware);
		bgRenderer.add(miOpenGl);
		menuRenderer.add(miJava2D);
		menuRenderer.add(miSoftware);
		menuRenderer.add(miOpenGl);
		switch (JPatchSettings.getInstance().iRealtimeRenderer) {
		case JPatchScreen.JAVA2D:
			miJava2D.setSelected(true);
		break;
		case JPatchScreen.SOFTWARE:
			miSoftware.setSelected(true);
		break;
		case JPatchScreen.OPENGL:
			miOpenGl.setSelected(true);
		break;
		}
		menuOptions.add(menuRenderer);
		
		menuOptions.add(miSync);
		//menuOptions.add(miFlip);
		//menuOptions.add(miCull);
		menuOptions.add(new ColorPreferencesAction());
		menuOptions.add(new SetGridSpacingAction());
		menuOptions.add(new ZBufferQualityAction());
		menuOptions.add(menuLight);
		
		
		menuPlaf = new JMenu("Look And Feel");
		ButtonGroup bgPlaf = new ButtonGroup();
//		UIManager.LookAndFeelInfo[] aLookAndFeelInfo = UIManager.getInstalledLookAndFeels();
//		for (int i = 0; i < aLookAndFeelInfo.length; i++) {
//			String plafName = aLookAndFeelInfo[i].getName();
//			String plafClassName = aLookAndFeelInfo[i].getClassName();
//			JRadioButtonMenuItem rmi = new JRadioButtonMenuItem(new SwitchLookAndFeelAction(plafName, plafClassName));
//			if (plafClassName.equals(JPatchSettings.getInstance().strPlafClassName)) {
//				rmi.setSelected(true);
//			} else {
//				rmi.setSelected(false);
//			}
//			bgPlaf.add(rmi);
//			menuPlaf.add(rmi);
//		}
		JRadioButtonMenuItem rmi;
		String plaf;
		try {
			plaf = "jpatch.boundary.laf.SmoothLookAndFeel";
			rmi = new JRadioButtonMenuItem(new SwitchLookAndFeelAction("JPatch", new SmoothLookAndFeel()));
			rmi.setSelected (plaf.equals(JPatchSettings.getInstance().strPlafClassName));
			bgPlaf.add(rmi);
			menuPlaf.add(rmi);
			
			plaf = UIManager.getCrossPlatformLookAndFeelClassName();
			rmi = new JRadioButtonMenuItem(new SwitchLookAndFeelAction("Metal", Class.forName(plaf).newInstance()));
			rmi.setSelected (plaf.equals(JPatchSettings.getInstance().strPlafClassName));
			bgPlaf.add(rmi);
			menuPlaf.add(rmi);
			
			plaf = UIManager.getSystemLookAndFeelClassName();
			rmi = new JRadioButtonMenuItem(new SwitchLookAndFeelAction("System", Class.forName(plaf).newInstance()));
			rmi.setSelected (plaf.equals(JPatchSettings.getInstance().strPlafClassName));
			bgPlaf.add(rmi);
			menuPlaf.add(rmi);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		menuOptions.add(menuPlaf);
		//>>>>> test-add
		menuOptions.add(new EditPhonemesAction());
		menuOptions.add(new KeyMapAction());
		//<<<<< test-add
		
		menuTools.add(new AlignAction());
		menuTools.add(new AutoMirrorAction());
		
		add(menuFile);
		add(menuOptions);
		//add(menuTools);
		add(menuTest);
		add(menuHelp);
		
		//MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("compute patches"), actionComputePatches);
		
		//>>>>> test-replace 
		/*
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("bottom view"), new ViewKeyAction(ViewDefinition.BOTTOM));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("front view"), new ViewKeyAction(ViewDefinition.FRONT));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("right view"), new ViewKeyAction(ViewDefinition.RIGHT));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("top view"), new ViewKeyAction(ViewDefinition.TOP));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("left view"), new ViewKeyAction(ViewDefinition.LEFT));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("bird's eye view"), new ViewKeyAction(ViewDefinition.BIRDS_EYE));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("rear view"), new ViewKeyAction(ViewDefinition.REAR));
		*/
		addKeyBindings();
		//<<<<< test-replace
		/*
		Font font = new Font("SansSerif",Font.PLAIN,12);
		setAllFonts(font,menuFile);
		setAllFonts(font,menuDisplay);
		setAllFonts(font,menuTest);
		*/
	}
	
	//>>>>> test-add
	public void addKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("bottom view"), new ViewKeyAction(ViewDefinition.BOTTOM));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("front view"), new ViewKeyAction(ViewDefinition.FRONT));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("right view"), new ViewKeyAction(ViewDefinition.RIGHT));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("top view"), new ViewKeyAction(ViewDefinition.TOP));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("left view"), new ViewKeyAction(ViewDefinition.LEFT));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("bird's eye view"), new ViewKeyAction(ViewDefinition.BIRDS_EYE));
		MainFrame.getInstance().getKeyAdapter().addKey(KeyMapping.getKey("rear view"), new ViewKeyAction(ViewDefinition.REAR));	
	}
	
	public void removeKeyBindings() {
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("bottom view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("front view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("right view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("top view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("left view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("bird's eye view"));
		MainFrame.getInstance().getKeyAdapter().removeKey(KeyMapping.getKey("rear view"));
	}
	//<<<<< test-add
	/*
	private void setAllFonts(Font font, JMenu menu) {
		MenuElement[] aMenuElement = menu.getSubElements();
		for (int e = 0; e < aMenuElement.length; e++) {
			try {
				if (aMenuElement[e].getClass() == Class.forName("JMenu")) {
					setAllFonts(font,(JMenu)aMenuElement[e]);
				} else {
					((JMenuItem)aMenuElement[e]).setFont(font);
				}
			} catch(Exception exception) {
			}
			menu.setFont(font);
		}
	}
	*/
}
		
		
