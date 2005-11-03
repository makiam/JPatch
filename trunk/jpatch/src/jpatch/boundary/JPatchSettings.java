package jpatch.boundary;

import javax.swing.*;
import java.util.*;
import java.util.prefs.*;
import java.awt.*;

import jpatch.auxilary.JPatchUtils;

public class JPatchSettings {
	
	public static final int OUTPUT_TRIANGULIZED_HASHPATCHES = 0;
	public static final int OUTPUT_BEZIERPATCHES = 1;
	public static final int OUTPUT_QUADS = 1;
	
	//public static final String[] OUTPUT_STRINGS = new String[] { "Triangulized Hash-Patches", "Bezier-Patches (experimental)" };
	//public int iOutputMode;
	//
	public static final int SUBDIV_LOW = 2;
	public static final int SUBDIV_MEDIUM = 3;
	public static final int SUBDIV_HIGH = 4;
	//public static final String[] SUBDIV_STRINGS = new String[] { "Low", "Medium", "Heigh" };
	//public int iSubdivMode;
	
	
	public static final int RENDERER_INYO = 0;
	public static final int RENDERER_POVRAY = 1;
	public static final int RENDERER_RIB = 2;
	
	public static final int POVRAY_UNIX = 0;
	public static final int POVRAY_WINDOWS = 1;
	
	//public static final int RENDERER_PIXIE = 3;
	//public static final int RENDERER_3DELIGHT = 4;
	//public static final int RENDERER_BMRT = 5;
	//public static final String[] RENDERER_STRINGS = new String[] { "POV-Ray", "Inyo", "Aqsis", "Pixie", "3Delight", "BMRT" };
	public int iRenderer;
	
	//public String strPovrayLauncher;
	//public String strInyoLauncher;
	//public String strAqsisLauncher;
	//public String strPixieLauncher;
	//public String str3DelightLauncher;
	//public String strBmrtLauncher;
	
	public boolean bFirstStart;
	
	
	public Color cBackground;
	public Color cCurve;
	public Color cPoint;
	public Color cHeadPoint;
	public Color cMultiPoint;
	//public Color cPointSelected;
	public Color cSelection;
	public Color cText;
	public Color cTangent;
	public Color cOrigin;
	public Color cGrid;
	public Color cGridMin;
	public Color cX;
	public Color cY;
	public Color cZ;
	public Color cSelected;
	public Color cHot;
	public Color cGrey;
	public Color cBackface;
	
	public int iRealtimeRenderer;
	
	public int iGhost = 0xA0;
	
	public int iBackground;
	public int iCurve;
	public int iPoint;
	public int iPointSelected;
	public int iGrid;
	public int iGridMin;
	public int iSelection;
	public int iX;
	public int iY;
	public int iZ;
	
	public int iScreenMode;
	public boolean bSyncWindows;
	
	public int iPatchSubdivisions;
	public int iCurveSubdivisions;
	public int iTesselationQuality;
	
	public int iScreenX;
	public int iScreenY;
	public int iScreenWidth;
	public int iScreenHeight;
	
	public int iLightingMode;
	public boolean bStickyLight;
	public boolean bFog;
	
	public boolean bGridSnap;
	public float fGridSpacing;
	
	public String strModelDir;
	
	public int iBackfaceMode;
	public int iBackfaceColor;
	
	public String strJPatchPath;
	public String strAMPath;
	public String strSPatchPath;
	public String strPovrayPath;
	public String strRendermanPath;
	public String strRotoscopePath;
	public String strPlafClassName;
	
	public String strJPatchFile = "";
	
	public String strWorkingDir = "";
	public boolean bDeleteSources;
	
	public Color cBackgroundColor;
	
	public final PovraySettings povraySettings = new PovraySettings();
	public final InyoSettings inyoSettings = new InyoSettings();
	public final RibSettings ribSettings = new RibSettings();
	public final WavefrontSettings wavefrontSettings = new WavefrontSettings();
	
	public int iRenderWidth;
	public int iRenderHeight;
	public float fRenderAspectWidth;
	public float fRenderAspectHeight;
	
	//>>>>> test-replace
//	public String[] astrKeyMap;
//	public String[] astrKeyMap = new String[] {
//	public String[] astrKeyMapDefault = new String[] {
	
	private Map defaultCommandKeyMap = new HashMap();
	public Map commandKeyMap = new HashMap();

	//<<<<< test-replace
	
	private static JPatchSettings INSTANCE;
	private Preferences userPrefs = Preferences.userRoot().node("/net/sf/jpatch/Preferences");
	private Map mapDefaults = new HashMap();
	
	/**
	 * constructor
	 */
	public JPatchSettings() {
		
		defaultCommandKeyMap.put("single view",					"1"				);
		defaultCommandKeyMap.put("horizontally split view",		"2"				);
		defaultCommandKeyMap.put("vertically split view",		"3"				);
		defaultCommandKeyMap.put("quad view",					"4"				);
		defaultCommandKeyMap.put("rotate view",					"shift R"		);
		defaultCommandKeyMap.put("move view",					"shift M"		);
		defaultCommandKeyMap.put("zoom view",					"shift Z"		);
		defaultCommandKeyMap.put("undo",						"ctrl Z"		);
		defaultCommandKeyMap.put("redo",						"ctrl Y"		);
		defaultCommandKeyMap.put("lock x",						"X"				);
		defaultCommandKeyMap.put("lock y",						"Y"				);
		defaultCommandKeyMap.put("lock z",						"Z"				);
		defaultCommandKeyMap.put("default tool",				"ESCAPE"		);
		defaultCommandKeyMap.put("rotate tool",					"R"				);
		defaultCommandKeyMap.put("add curve segment",			"A"				);
		defaultCommandKeyMap.put("add bone",					"B"				);
		defaultCommandKeyMap.put("remove",						"BACK_SPACE"	);
		defaultCommandKeyMap.put("delete",						"DELETE"		);
		defaultCommandKeyMap.put("insert point",				"INSERT"		);
		defaultCommandKeyMap.put("compute patches",				"F5"			);
		defaultCommandKeyMap.put("clone",						"ctrl C"		);
		defaultCommandKeyMap.put("extrude",						"E"				);
		defaultCommandKeyMap.put("lathe",						"L"				);
		defaultCommandKeyMap.put("expand selection",			"ENTER"			);
		defaultCommandKeyMap.put("next curve",					"TAB"			);
		defaultCommandKeyMap.put("prev curve",					"shift TAB"		);
		defaultCommandKeyMap.put("next point",					"RIGHT"			);
		defaultCommandKeyMap.put("prev point",					"LEFT"			);
		defaultCommandKeyMap.put("bottom view",					"NUM0"			);
		defaultCommandKeyMap.put("top view",					"NUM5"			);
		defaultCommandKeyMap.put("front view",					"NUM2"			);
		defaultCommandKeyMap.put("rear view",					"NUM8"			);
		defaultCommandKeyMap.put("left view",					"NUM4"			);
		defaultCommandKeyMap.put("right view",					"NUM6"			);
		defaultCommandKeyMap.put("bird's eye view",				"NUM7"			);
		defaultCommandKeyMap.put("snap to grid",				"G"			);
		defaultCommandKeyMap.put("hide",						"H"			);
		
//		"Single View				Single view					1",
//		"Horizontal Split View		Horizontal split view		2",
//		"Vertical Split View		Vertical split view			3",
//		"Quad View					Quad view					4",
//		"Rotate View				Rotate view					SHIFT-R",
//		"Move View					Move view					SHIFT-M",
//		"Zoom View					Zoom view					SHIFT-Z",
//		"Undo						Undo						CTRL-Z",
//		"Redo						Redo						CTRL-Y",
//		"Lock X						Lock/unlock X				X",
//		"Lock Y						Lock/unlock Y				Y",
//		"Lock Z						Lock/unlock Z				Z",
//		"Default Tool				Default tool				ESC",
//		"Add Point					Add point					A",
//		"Add Multiple Points		Add multiple points			SHIFT-A",
//		"Magnet Tool				Magnet tool					M",
//		"Rotate Tool				Rotate tool					R",
//		"Remove Points				Remove points				BACKSPACE",
//		"Delete Points				Delete points				DEL",
//		"Compute Patches			Compute patches				F5",
//		"Clone						Copy						CTRL-C",
//		"Extrude					Extrude						E",
//		"Lathe						Lathe						L",
//		"Extend Selection			Extend selection			ENTER",
//		"Next Curve					cycle through curves		TAB",
//		"Insert Point				Insert point				INS",
//		"Bottom View				Bottom view					NUM0",
//		"Front View					Front view					NUM2",
//		"Right View					Right view					NUM4",
//		"Top View					Top view					NUM5",
//		"Left View					Left view					NUM6",
//		"Bird's Eye View			Bird's eye view				NUM7",
//		"Rear View					Rear view					NUM8",
//		"Grid						Snap to grid				G",
//		"Hide						Hide/show unselected points	H"
		//>>>>> test-add
//		mapDefaults.put("keyMap", join("||", astrKeyMapDefault));
		//<<<<< test-add
		mapDefaults.put("firstStart", new Boolean(true));
		
		mapDefaults.put("backgroundColor",new Color(0x28,0x38,0x48));
		mapDefaults.put("curveColor",new Color(255,255,255));
		mapDefaults.put("pointColor",new Color(255,255,0));
		mapDefaults.put("headPointColor",new Color(255,0,0));
		mapDefaults.put("multiPointColor",new Color(255,128,0));
		mapDefaults.put("tangentColor",new Color(255,255,0));
		//mapDefaults.put("selectedPointColor",new Color(0,255,0));
		mapDefaults.put("selectionColor",new Color(255,255,0));
		mapDefaults.put("textColor",new Color(0x80,0x90,0xA0));
		mapDefaults.put("originColor",new Color(0xFF,0xFF,0xFF));
		mapDefaults.put("gridColor",new Color(0x08,0x18,0x28));
		mapDefaults.put("minorGridColor",new Color(0x18,0x28,0x38));
		mapDefaults.put("xColor",new Color(255,64,0));
		mapDefaults.put("yColor",new Color(0,255,0));
		mapDefaults.put("zColor",new Color(128,128,255));
		mapDefaults.put("selectedColor",new Color(0,255,0));
		mapDefaults.put("hotColor",new Color(0,255,255));
		mapDefaults.put("greyColor",new Color(0x50,0x60,0x70));
		
		mapDefaults.put("screenMode", new Integer(JPatchScreen.SINGLE));
		mapDefaults.put("syncWindows", new Boolean(false));
		
		mapDefaults.put("patchSubdivisions", new Integer(3));
		mapDefaults.put("curveSubdivisions", new Integer(5));
		mapDefaults.put("tesselationQuality", new Integer(2));
		
		mapDefaults.put("screenX", new Integer(0));
		mapDefaults.put("screenY", new Integer(0));
		mapDefaults.put("screenWidth", new Integer(1000));
		mapDefaults.put("screenHeight", new Integer(700));
		
		mapDefaults.put("lightingMode", new Integer(JPatchScreen.LIGHT_THREE_POINT));
		mapDefaults.put("stickyLight", new Boolean(true));
		mapDefaults.put("fogEffect", new Boolean(true));
		
		mapDefaults.put("gridSnap", new Boolean(false));
		mapDefaults.put("gridSpacing", new Float(1));
		
		mapDefaults.put("backfaceMode", new Integer(2));
		mapDefaults.put("backfaceColor", new Color(0xff, 0x00, 0x00));
		
		mapDefaults.put("jpatchPath", "");
		mapDefaults.put("spatchPath", "");
		mapDefaults.put("amPath", "");
		mapDefaults.put("povrayPath", "");
		mapDefaults.put("rendermanPath", "");
		mapDefaults.put("rotoscopePath", "");
		
		mapDefaults.put("lookAndFeel", "javax.swing.plaf.metal.MetalLookAndFeel");
		
		mapDefaults.put("renderer", new Integer(RENDERER_POVRAY));
		mapDefaults.put("rendererBackgroundColor",new Color(0x7f,0x7f,0x7f));
		
		mapDefaults.put("povrayVersion", new Integer(POVRAY_UNIX));
		mapDefaults.put("povrayOutputMode", new Integer(OUTPUT_TRIANGULIZED_HASHPATCHES));
		mapDefaults.put("povraySubdivMode", new Integer(SUBDIV_MEDIUM));
		mapDefaults.put("povrayExecutable", "");
		mapDefaults.put("povrayAaJitter", new Float(1));
		mapDefaults.put("povrayAaMethod", new Integer(1));
		mapDefaults.put("povrayAaLevel", new Integer(2));
		mapDefaults.put("povrayAaThreshold", new Float(0.3f));
		mapDefaults.put("povrayInclude", "");
		mapDefaults.put("povrayEnv", "");
		mapDefaults.put("povrayPath", "");
		
		mapDefaults.put("inyoTexturePath", "");
		mapDefaults.put("inyoSupersample", new Integer(3));
		mapDefaults.put("inyoSamplingMode", new Integer(0));
		mapDefaults.put("inyoSubdivMode", new Integer(3));
		mapDefaults.put("inyoRecursionDepth", new Integer(12));
		mapDefaults.put("inyoShadowSamples", new Integer(8));
		mapDefaults.put("inyoTransparentShadows", new Boolean(false));
		mapDefaults.put("inyoCaustics", new Boolean(false));
		mapDefaults.put("inyoOversampleCaustics", new Boolean(false));
		mapDefaults.put("inyoAmbientOcclusion", new Boolean(false));
		mapDefaults.put("inyoAmbientOcclusionDistance", new Float(1000));
		mapDefaults.put("inyoAmbientOcclusionSamples", new Integer(3));
		mapDefaults.put("inyoAmbientOcclusionColorbleed", new Float(0.25f));
		
		mapDefaults.put("ribExecutable", "");
		mapDefaults.put("ribOutputMode", new Integer(1));
		mapDefaults.put("ribSubdivMode", new Integer(2));
		mapDefaults.put("ribPixelSamplesX", new Integer(2));
		mapDefaults.put("ribPixelSamplesY", new Integer(2));
		mapDefaults.put("ribPixelFilterX", new Integer(2));
		mapDefaults.put("ribPixelFilterY", new Integer(2));
		mapDefaults.put("ribPixelFilter", new Integer(3));
		mapDefaults.put("ribShadingRate", new Float(1));
		mapDefaults.put("ribShadingInterpolation", new Integer(1));
		mapDefaults.put("ribExposure", new Float(1));
		mapDefaults.put("ribEnv", "");
		mapDefaults.put("ribPath", "");
		
		mapDefaults.put("wavefrontPath", "");
		mapDefaults.put("wavefrontOutputMode", new Integer(OUTPUT_QUADS));
		mapDefaults.put("wavefrontSubdivMode", new Integer(2));
		mapDefaults.put("wavefrontExportNormals", new Boolean(true));
		mapDefaults.put("wavefrontAverageNormals", new Boolean(false));
		
		mapDefaults.put("workingDir", "");
		mapDefaults.put("renderWidth", new Integer(640));
		mapDefaults.put("renderHeight", new Integer(360));
		mapDefaults.put("renderAspectWidth", new Float(16));
		mapDefaults.put("renderAspectHeight", new Float(9));
		
		mapDefaults.put("modelDir", "");
		mapDefaults.put("deleteSources", new Boolean(true));
		
		mapDefaults.put("realtimeRenderer", new Integer(JPatchScreen.SOFTWARE));
		
		//mapDefaults.put("povrayLauncher", "");
		//mapDefaults.put("inyoLauncher", "");
		//mapDefaults.put("aqsisLauncher", "");
		//mapDefaults.put("pixieLauncher", "");
		//mapDefaults.put("3delightLauncher", "");
		//mapDefaults.put("bmrtLauncher", "");
		
		loadSettings();
		saveSettings();
		
	}
	
	/**
	 * returns the instance (singleton pattern)
	 */
	public static JPatchSettings getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JPatchSettings();
		}
		return INSTANCE;
	}
	
	public void setColors(Color[] colors) {
		cBackground = colors[0];
		cPoint = colors[1];
		cCurve = colors[2];
		cHeadPoint = colors[3];
		cMultiPoint = colors[4];
		cSelected = colors[5];
		cHot = colors[6];
		cSelection = colors[7];
		cText = colors[8];
		cGrid = colors[9];
		cGridMin = colors[10];
		cX = colors[11];
		cY = colors[12];
		cZ = colors[13];
		cTangent = colors[14];
		cBackface = colors[15];
		
		iBackground = cBackground.getRGB();
		iCurve = cCurve.getRGB();
		iPoint = cPoint.getRGB();
		iGrid = cGrid.getRGB();
		iGridMin = cGridMin.getRGB();
		iSelection = cSelection.getRGB();
		iX = cX.getRGB();
		iY = cY.getRGB();
		iZ = cZ.getRGB();
		iBackfaceColor = cBackface.getRGB();
	}
	
	public Color[] getColors() {
		return new Color[] {
			cBackground,
			cPoint,
			cCurve,
			cHeadPoint,
			cMultiPoint,
			cSelected,
			cHot,
			cSelection,
			cText,
			cGrid,
			cGridMin,
			cX,
			cY,
			cZ,
			cTangent,
			cBackface
		};
	}
	
	public Color[] getDefaultColors() {
		return new Color[] {
			getColorDefault("backgroundColor"),
			getColorDefault("pointColor"),
			getColorDefault("curveColor"),
			getColorDefault("headPointColor"),
			getColorDefault("multiPointColor"),
			getColorDefault("selectedColor"),
			getColorDefault("hotColor"),
			getColorDefault("selectionColor"),
			getColorDefault("textColor"),
			getColorDefault("gridColor"),
			getColorDefault("minorGridColor"),
			getColorDefault("xColor"),
			getColorDefault("yColor"),
			getColorDefault("zColor"),
			getColorDefault("tangentColor"),
			getColorDefault("backfaceColor")
		};
	}
	
	/**
	 * load all setings from backings store
	 */
	public void loadSettings() {
		//>>>>> test-add
//		astrKeyMap = getString("keyMap").split("\\|\\|");
		//<<<<< test-add
		
		bFirstStart = getBoolean("firstStart");
		
		cBackground = getColor("backgroundColor");
		cCurve = getColor("curveColor");
		cPoint = getColor("pointColor");
		cTangent = getColor("tangentColor");
		cHeadPoint = getColor("headPointColor");
		cMultiPoint = getColor("multiPointColor");
		//cPointSelected = getColor("selectedPointColor");
		cSelection = getColor("selectionColor");
		cText = getColor("textColor");
		cOrigin = getColor("originColor");
		cGrid =	getColor("gridColor");
		cGridMin = getColor("minorGridColor");
		cX = getColor("xColor");
		cY = getColor("yColor");
		cZ = getColor("zColor");
		cSelected = getColor("selectedColor");
		cHot = getColor("hotColor");
		cGrey = getColor("greyColor");
		cBackface = getColor("backfaceColor");
		
		iBackground = cBackground.getRGB();
		iCurve = cCurve.getRGB();
		iPoint = cPoint.getRGB();
		//iPointSelected = cPointSelected.getRGB();
		iGrid = cGrid.getRGB();
		iGridMin = cGridMin.getRGB();
		iSelection = cSelection.getRGB();
		iX = cX.getRGB();
		iY = cY.getRGB();
		iZ = cZ.getRGB();
		iBackfaceColor = cBackface.getRGB();
		
		iScreenMode = getInt("screenMode");
		bSyncWindows = getBoolean("syncWindows");
		
		iPatchSubdivisions = getInt("patchSubdivisions");
		iCurveSubdivisions = getInt("curveSubdivisions");
		iTesselationQuality = getInt("tesselationQuality");
		
		iScreenX = getInt("screenX");
		iScreenY = getInt("screenY");
		iScreenWidth = getInt("screenWidth");
		iScreenHeight = getInt("screenHeight");
		
		iLightingMode = getInt("lightingMode");
		bStickyLight = getBoolean("stickyLight");
		bFog = getBoolean("fogEffect");
		
		iBackfaceMode = getInt("backfaceMode");
		
		bGridSnap = getBoolean("gridSnap");
		fGridSpacing = getFloat("gridSpacing");
		
		strJPatchPath = getString("jpatchPath");
		strSPatchPath = getString("spatchPath");
		strAMPath = getString("amPath");
		strPovrayPath = getString("povrayPath");
		strRendermanPath = getString("rendermanPath");
		strRotoscopePath = getString("rotoscopePath");
		strPlafClassName = getString("lookAndFeel");
		
		strWorkingDir = getString("workingDir");
		
		cBackgroundColor = getColor("rendererBackgroundColor");
		//iOutputMode = getInt("outputMode");
		//iSubdivMode = getInt("subdivMode");
		iRenderer = getInt("renderer");
		povraySettings.strExecutable = getString("povrayExecutable");
		povraySettings.strEnv = getString("povrayEnv");
		povraySettings.iVersion = getInt("povrayVersion");
		povraySettings.iOutputMode = getInt("povrayOutputMode");
		povraySettings.iSubdivMode = getInt("povraySubdivMode");
		povraySettings.fAaJitter = getFloat("povrayAaJitter");
		povraySettings.iAaMethod = getInt("povrayAaMethod");
		povraySettings.iAaLevel = getInt("povrayAaLevel");
		povraySettings.fAaThreshold = getFloat("povrayAaThreshold");
		povraySettings.strInclude = getString("povrayInclude");
		povraySettings.strPath = getString("povrayPath");
		
		inyoSettings.strTexturePath = getString("inyoTexturePath");
		inyoSettings.iSupersample = getInt("inyoSupersample");
		inyoSettings.iSamplingMode = getInt("inyoSamplingMode");
		inyoSettings.iSubdivMode = getInt("inyoSubdivMode");
		inyoSettings.iRecursion = getInt("inyoRecursionDepth");
		inyoSettings.iShadowSamples = getInt("inyoShadowSamples");
		inyoSettings.bTransparentShadows = getBoolean("inyoTransparentShadows");
		inyoSettings.bEnableCaustics = getBoolean("inyoCaustics");
		inyoSettings.bOversampleCaustics = getBoolean("inyoOversampleCaustics");
		inyoSettings.bEnableAmbientOcclusion = getBoolean("inyoAmbientOcclusion");
		inyoSettings.fAmbientOcclusionDistance = getFloat("inyoAmbientOcclusionDistance");
		inyoSettings.iAmbientOcclusionSamples = getInt("inyoAmbientOcclusionSamples");
		inyoSettings.fAmbientOcclusionColorbleed = getFloat("inyoAmbientOcclusionColorbleed");
		
		ribSettings.strExecutable = getString("ribExecutable");
		ribSettings.strEnv = getString("ribEnv");
		ribSettings.iOutputMode = getInt("ribOutputMode");
		ribSettings.iSubdivMode = getInt("ribSubdivMode");
		ribSettings.iPixelSamplesX = getInt("ribPixelSamplesX");
		ribSettings.iPixelSamplesY = getInt("ribPixelSamplesY");
		ribSettings.iPixelFilterX = getInt("ribPixelFilterX");
		ribSettings.iPixelFilterY = getInt("ribPixelFilterY");
		ribSettings.iPixelFilter = getInt("ribPixelFilter");
		ribSettings.fShadingRate = getFloat("ribShadingRate");
		ribSettings.iShadingInterpolation = getInt("ribShadingInterpolation");
		ribSettings.fExposure = getFloat("ribExposure");
		ribSettings.strPath = getString("ribPath");
		
		wavefrontSettings.strPath = getString("wavefrontPath");
		wavefrontSettings.iOutputMode = getInt("wavefrontOutputMode");
		wavefrontSettings.iSubdivMode = getInt("wavefrontSubdivMode");
		wavefrontSettings.bExportNormals = getBoolean("wavefrontExportNormals");
		wavefrontSettings.bAverageNormals = getBoolean("wavefrontAverageNormals");
		
		iRenderWidth = getInt("renderWidth");
		iRenderHeight = getInt("renderHeight");
		fRenderAspectWidth = getFloat("renderAspectWidth");
		fRenderAspectHeight = getFloat("renderAspectHeight");
		
		strModelDir = getString("modelDir");
		bDeleteSources = getBoolean("deleteSources");
		
		iRealtimeRenderer= getInt("realtimeRenderer");
		
		//strPovrayLauncher = getString("povrayLauncher");
		//strInyoLauncher = getString("inyoLauncher");
		//strAqsisLauncher = getString("aqsisLauncher");
		//strPixieLauncher = getString("pixieLauncher");
		//str3DelightLauncher = getString("3delightLauncher");
		//strBmrtLauncher = getString("bmrtLauncher");
		for (Iterator it = new ArrayList(defaultCommandKeyMap.keySet()).iterator(); it.hasNext(); ) {
			String command = (String) it.next();
			commandKeyMap.put(command, userPrefs.get(command, (String) defaultCommandKeyMap.get(command)));
		}
	}
	
	public void loadDefaults() {
		//>>>>> test-add
//		astrKeyMap = getStringDefault("keyMap").split("\\|\\|");
		//<<<<< test-add
		bFirstStart = getBooleanDefault("firstStart");
		
		cBackground = getColorDefault("backgroundColor");
		cCurve = getColorDefault("curveColor");
		cPoint = getColorDefault("pointColor");
		cTangent = getColorDefault("tangentColor");
		cHeadPoint = getColorDefault("headPointColor");
		cMultiPoint = getColorDefault("multiPointColor");
		//cPointSelected = getColorDefault("selectedPointColor");
		cSelection = getColorDefault("selectionColor");
		cText = getColorDefault("textColor");
		cOrigin = getColorDefault("originColor");
		cGrid =	getColorDefault("gridColor");
		cGridMin = getColorDefault("minorGridColor");
		cX = getColorDefault("xColor");
		cY = getColorDefault("yColor");
		cZ = getColorDefault("zColor");
		cSelected = getColorDefault("selectedColor");
		cHot = getColorDefault("hotColor");
		cGrey = getColorDefault("greyColor");
		cBackface = getColorDefault("backfaceColor");
		
		iBackground = cBackground.getRGB();
		iCurve = cCurve.getRGB();
		iPoint = cPoint.getRGB();
		//iPointSelected = cPointSelected.getRGB();
		iGrid = cGrid.getRGB();
		iGridMin = cGridMin.getRGB();
		iSelection = cSelection.getRGB();
		iX = cX.getRGB();
		iY = cY.getRGB();
		iZ = cZ.getRGB();
		iBackfaceColor = cBackface.getRGB();
		
		iScreenMode = getIntDefault("screenMode");
		bSyncWindows = getBooleanDefault("syncWindows");
		
		iPatchSubdivisions = getIntDefault("patchSubdivisions");
		iCurveSubdivisions = getIntDefault("curveSubdivisions");
		iTesselationQuality = getIntDefault("tesselationQuality");
		
		iScreenX = getIntDefault("screenX");
		iScreenY = getIntDefault("screenY");
		iScreenWidth = getIntDefault("screenWidth");
		iScreenHeight = getIntDefault("screenHeight");
		
		iLightingMode = getIntDefault("lightingMode");
		bStickyLight = getBooleanDefault("stickyLight");
		bFog = getBooleanDefault("fogEffect");
		
		iBackfaceMode = getIntDefault("backfaceMode");
		
		bGridSnap = getBooleanDefault("gridSnap");
		fGridSpacing = getFloatDefault("gridSpacing");
		
		strJPatchPath = getStringDefault("jpatchPath");
		strSPatchPath = getStringDefault("spatchPath");
		strAMPath = getStringDefault("amPath");
		strPovrayPath = getStringDefault("povrayPath");
		strRendermanPath = getStringDefault("rendermanPath");
		strRotoscopePath = getStringDefault("rotoscopePath");
		strPlafClassName = getStringDefault("lookAndFeel");
		
		strWorkingDir = getStringDefault("workingDir");
		
		cBackgroundColor = getColorDefault("rendererBackgroundColor");
		
		iRenderer = getIntDefault("renderer");
		povraySettings.strExecutable = getStringDefault("povrayExecutable");
		povraySettings.strEnv = getStringDefault("povrayEnv");
		povraySettings.iVersion = getIntDefault("povrayVersion");
		povraySettings.fAaJitter = getFloatDefault("povrayAaJitter");
		povraySettings.iOutputMode = getIntDefault("povrayOutputMode");
		povraySettings.iSubdivMode = getIntDefault("povraySubdivMode");
		povraySettings.iAaMethod = getIntDefault("povrayAaMethod");
		povraySettings.iAaLevel = getIntDefault("povrayAaLevel");
		povraySettings.fAaThreshold = getFloatDefault("povrayAaThreshold");
		povraySettings.strInclude = getStringDefault("povrayInclude");
		povraySettings.strPath = getStringDefault("povrayPath");
		
		inyoSettings.strTexturePath = getStringDefault("inyoTexturePath");
		inyoSettings.iSupersample = getIntDefault("inyoSupersample");
		inyoSettings.iSamplingMode = getIntDefault("inyoSamplingMode");
		inyoSettings.iSubdivMode = getIntDefault("inyoSubdivMode");
		inyoSettings.iRecursion = getIntDefault("inyoRecursionDepth");
		inyoSettings.iShadowSamples = getIntDefault("inyoShadowSamples");
		inyoSettings.bTransparentShadows = getBooleanDefault("inyoTransparentShadows");
		inyoSettings.bEnableCaustics = getBooleanDefault("inyoCaustics");
		inyoSettings.bOversampleCaustics = getBooleanDefault("inyoOversampleCaustics");
		inyoSettings.bEnableAmbientOcclusion = getBooleanDefault("inyoAmbientOcclusion");
		inyoSettings.fAmbientOcclusionDistance = getFloatDefault("inyoAmbientOcclusionDistance");
		inyoSettings.iAmbientOcclusionSamples = getIntDefault("inyoAmbientOcclusionSamples");
		inyoSettings.fAmbientOcclusionColorbleed = getFloatDefault("inyoAmbientOcclusionColorbleed");
		
		ribSettings.strExecutable = getStringDefault("ribExecutable");
		ribSettings.strEnv = getStringDefault("ribEnv");
		ribSettings.iOutputMode = getIntDefault("ribOutputMode");
		ribSettings.iSubdivMode = getIntDefault("ribSubdivMode");
		ribSettings.iPixelSamplesX = getIntDefault("ribPixelSamplesX");
		ribSettings.iPixelSamplesY = getIntDefault("ribPixelSamplesY");
		ribSettings.iPixelFilterX = getIntDefault("ribPixelFilterX");
		ribSettings.iPixelFilterY = getIntDefault("ribPixelFilterY");
		ribSettings.iPixelFilter = getIntDefault("ribPixelFilter");
		ribSettings.fShadingRate = getFloatDefault("ribShadingRate");
		ribSettings.iShadingInterpolation = getIntDefault("ribShadingInterpolation");
		ribSettings.fExposure = getFloatDefault("ribExposure");
		ribSettings.strPath = getStringDefault("ribPath");
		
		wavefrontSettings.strPath = getStringDefault("wavefrontPath");
		wavefrontSettings.iOutputMode = getIntDefault("wavefrontOutputMode");
		wavefrontSettings.iSubdivMode = getIntDefault("wavefrontSubdivMode");
		wavefrontSettings.bAverageNormals = getBooleanDefault("wavefrontAverageNormals");
		
		iRenderWidth = getIntDefault("renderWidth");
		iRenderHeight = getIntDefault("renderHeight");
		fRenderAspectWidth = getFloatDefault("renderAspectWidth");
		fRenderAspectHeight = getFloatDefault("renderAspectHeight");
		
		strModelDir = getStringDefault("modelDir");
		bDeleteSources = getBooleanDefault("deleteSources");
		
		iRealtimeRenderer= getIntDefault("realtimeRenderer");
		commandKeyMap.clear();
		commandKeyMap.putAll(defaultCommandKeyMap);
	}
	
	/**
	 * save all settings to backing store
	 */
	public void saveSettings() {
		//>>>>> test-add
//		putString("keyMap", join("||", astrKeyMap));
		//<<<<< test-add
		putBoolean("firstStart", bFirstStart);
		
		putColor("backgroundColor", cBackground);
		putColor("curveColor", cCurve);
		putColor("pointColor", cPoint);
		putColor("headPointColor", cHeadPoint);
		putColor("multiPointColor", cMultiPoint);
		//putColor("selectedPointColor", cPointSelected);
		putColor("selectionColor", cSelection);
		putColor("textColor", cText);
		putColor("originColor", cOrigin);
		putColor("gridColor", cGrid);
		putColor("minorGridColor", cGridMin);
		putColor("xColor", cX);
		putColor("yColor", cY);
		putColor("zColor", cZ);
		putColor("selectedColor", cSelected);
		putColor("hotColor", cHot);
		putColor("tangentColor", cTangent);
		putColor("backfaceColor", cBackface);
		
		putInt("screenMode", iScreenMode);
		putBoolean("syncWindows", bSyncWindows);
		
		putInt("curveSubdivisions", iCurveSubdivisions);
		putInt("patchSubdivisions", iPatchSubdivisions);
		putInt("tesselationQuality", iTesselationQuality);
		
		putInt("screenX", iScreenX);
		putInt("screenY", iScreenY);
		putInt("screenWidth", iScreenWidth);
		putInt("screenHeight", iScreenHeight);
		
		putInt("lightingMode", iLightingMode);
		putBoolean("stickyLight", bStickyLight);
		putBoolean("fogEffect", bFog);
		
		putBoolean("gridSnap", bGridSnap);
		putFloat("gridSpacing", fGridSpacing);
		putInt("backfaceMode", iBackfaceMode);
		
		putString("jpatchPath", strJPatchPath);
		putString("spatchPath", strSPatchPath);
		putString("amPath", strAMPath);
		putString("povrayPath", strPovrayPath);
		putString("rendermanPath", strRendermanPath);
		putString("rotoscopePath", strRotoscopePath);
		putString("lookAndFeel", strPlafClassName);
		
		//putInt("outputMode", iOutputMode);
		//putInt("subdivMode", iSubdivMode);
		putInt("renderer", iRenderer);
		
		putString("workingDir", strWorkingDir);
		
		//putString("povrayLauncher", strPovrayLauncher);
		//putString("inyoLauncher", strInyoLauncher);
		//putString("aqsisLauncher", strAqsisLauncher);
		//putString("pixieLauncher", strPixieLauncher);
		//putString("3delightLauncher", str3DelightLauncher);
		//putString("bmrtLauncher", strBmrtLauncher);
		
		putColor("rendererBackgroundColor", cBackgroundColor);
		
		putString("povrayExecutable", povraySettings.strExecutable);
		putString("povrayEnv", povraySettings.strEnv);
		putFloat("povrayAaJitter", povraySettings.fAaJitter);
		putInt("povrayVersion", povraySettings.iVersion);
		putInt("povrayOutputMode", povraySettings.iOutputMode);
		putInt("povraySubdivMode", povraySettings.iSubdivMode);
		putInt("povrayAaMethod", povraySettings.iAaMethod);
		putInt("povrayAaLevel", povraySettings.iAaLevel);
		putFloat("povrayAaThreshold", povraySettings.fAaThreshold);
		putString("povrayInclude", povraySettings.strInclude);
		putString("povrayPath", povraySettings.strPath);
		
		putString("inyoTexturePath", inyoSettings.strTexturePath);
		putInt("inyoSupersample", inyoSettings.iSupersample);
		putInt("inyoSamplingMode", inyoSettings.iSamplingMode);
		putInt("inyoSubdivMode", inyoSettings.iSubdivMode);
		putInt("inyoRecursionDepth", inyoSettings.iRecursion);
		putInt("inyoShadowSamples", inyoSettings.iShadowSamples);
		putBoolean("inyoTransparentShadows", inyoSettings.bTransparentShadows);
		putBoolean("inyoCaustics", inyoSettings.bEnableCaustics);
		putBoolean("inyoOversampleCaustics", inyoSettings.bOversampleCaustics);
		putBoolean("inyoAmbientOcclusion", inyoSettings.bEnableAmbientOcclusion);
		putFloat("inyoAmbientOcclusionDistance", inyoSettings.fAmbientOcclusionDistance);
		putInt("inyoAmbientOcclusionSamples", inyoSettings.iAmbientOcclusionSamples);
		putFloat("inyoAmbientOcclusionColorbleed", inyoSettings.fAmbientOcclusionColorbleed);
		
		putInt("ribOutputMode", ribSettings.iOutputMode);
		putInt("ribSubdivMode", ribSettings.iSubdivMode);
		putString("ribExecutable", ribSettings.strExecutable);
		putString("ribEnv", ribSettings.strEnv);
		putInt("ribPixelSamplesX", ribSettings.iPixelSamplesX);
		putInt("ribPixelSamplesY", ribSettings.iPixelSamplesY);
		putInt("ribPixelFilterX", ribSettings.iPixelFilterX);
		putInt("ribPixelFilterY", ribSettings.iPixelFilterY);
		putInt("ribPixelFilter", ribSettings.iPixelFilter);
		putFloat("ribShadingRate", ribSettings.fShadingRate);
		putInt("ribShadingInterpolation", ribSettings.iShadingInterpolation);
		putFloat("ribExposure", ribSettings.fExposure);
		putString("ribPath", ribSettings.strPath);
		
		putString("wavefrontPath", wavefrontSettings.strPath);
		putInt("wavefrontOutputMode", wavefrontSettings.iOutputMode);
		putInt("wavefrontSubdivMode", wavefrontSettings.iSubdivMode);
		putBoolean("wavefrontExportNormals", wavefrontSettings.bExportNormals);
		putBoolean("wavefrontAverageNormals", wavefrontSettings.bAverageNormals);
		
		putInt("renderWidth", iRenderWidth);
		putInt("renderHeight", iRenderHeight);
		putFloat("renderAspectWidth", fRenderAspectWidth);
		putFloat("renderAspectHeight", fRenderAspectHeight);
		
		putString("modelDir", strModelDir);
		putBoolean("deleteSources", bDeleteSources);
		
		putInt("realtimeRenderer", iRealtimeRenderer);
		
		for (Iterator it = new ArrayList(commandKeyMap.keySet()).iterator(); it.hasNext(); ) {
			String command = (String) it.next();
			putString(command, (String) commandKeyMap.get(command));
		}
		
		try {
			userPrefs.flush();
		} catch (BackingStoreException exception) {
		}
	}
	
	private void putInt(String key, int i) {
		userPrefs.putInt(key, i);
	}
	
	private int getInt(String key) {
		return userPrefs.getInt(key,((Integer) mapDefaults.get(key)).intValue());
	}
	
	private int getIntDefault(String key) {
		return ((Integer) mapDefaults.get(key)).intValue();
	}
	
	private void putFloat(String key, float f) {
		userPrefs.putFloat(key, f);
	}
	
	private float getFloat(String key) {
		return userPrefs.getFloat(key,((Float) mapDefaults.get(key)).floatValue());
	}
	
	private float getFloatDefault(String key) {
		return ((Float) mapDefaults.get(key)).floatValue();
	}
	
	private void putBoolean(String key, boolean b) {
		userPrefs.putBoolean(key, b);
	}
	
	private boolean getBoolean(String key) {
		return userPrefs.getBoolean(key,((Boolean) mapDefaults.get(key)).booleanValue());
	}
	
	private boolean getBooleanDefault(String key) {
		return ((Boolean) mapDefaults.get(key)).booleanValue();
	}
	
	private void putString(String key, String value) {
		userPrefs.put(key, value);
	}
	
	private String getString(String key) {
		return userPrefs.get(key, (String) mapDefaults.get(key));
	}
	
	private String getStringDefault(String key) {
		return (String) mapDefaults.get(key);
	}
	
		
	/**
	 * put an awt.Color to preferences node
	 */
	private void putColor(String key, Color color) {
		userPrefs.putInt(key, color.getRGB());
	}

	/**
	 * get an awt.Color from preferences node
	 */
	private Color getColor(String key) {
		return new Color(userPrefs.getInt(key,((Color) mapDefaults.get(key)).getRGB()));
	}
	
	private Color getColorDefault(String key) {
		return (Color) mapDefaults.get(key);
	}
	
	public void resetToDefaults() {
		loadDefaults();
		saveSettings();
	}
	
	public static void main(String[] args) {
		getInstance().resetToDefaults();
		System.out.println("All settings have been reset to their default values");
	}
	
	public static String[] getEnv(String env) {
		if (env.replaceAll("\\s", "").equals("")) return null;
		return env.replaceAll("\\s*;\\s*", ";").split(";");
	}
	
	//>>>>> test-add
	public static String join( String token, String[] strings ) {
	    StringBuffer sb = new StringBuffer();	       
	    for( int x = 0; x < ( strings.length - 1 ); x++ ) {
	            sb.append( strings[x] );
	            sb.append( token );
	    }
	    sb.append( strings[ strings.length - 1 ] );	       
	    return( sb.toString() );
	}
	//<<<<< test-add
	 
	public class PovraySettings {
		public String strPath;
		public String strExecutable;
		public String strEnv;
		public int iVersion;
		public int iOutputMode;
		public int iSubdivMode;
		public int iAaMethod;
		public int iAaLevel;
		public float fAaThreshold;
		public float fAaJitter;
		public String strInclude;
	}
	
	public class InyoSettings {
		public String strTexturePath;
		public int iSupersample;
		public int iSamplingMode;
		public int iSubdivMode;
		public int iRecursion;
		public int iShadowSamples;
		public boolean bTransparentShadows;
		public boolean bEnableCaustics;
		public boolean bOversampleCaustics;
		public boolean bEnableAmbientOcclusion;
		public float fAmbientOcclusionDistance;
		public int iAmbientOcclusionSamples;
		public float fAmbientOcclusionColorbleed;
	}
	
	public class RibSettings {
		public String strPath;
		public String strExecutable;
		public String strEnv;
		public int iOutputMode;
		public int iSubdivMode;
		public int iPixelSamplesX;
		public int iPixelSamplesY;
		public int iPixelFilterX;
		public int iPixelFilterY;
		public int iPixelFilter;
		public float fShadingRate;
		public int iShadingInterpolation;
		public float fExposure;
	}
	
	public class WavefrontSettings {
		public String strPath;
		public int iOutputMode;
		public int iSubdivMode;
		public boolean bExportNormals;
		public boolean bAverageNormals;
	}
	
	//public static void main(String[] args) {
	//	String[] e = getEnv(args[0]);
	//	for (int i = 0; i < e.length; System.out.println(e[i++]));
	//}
}
