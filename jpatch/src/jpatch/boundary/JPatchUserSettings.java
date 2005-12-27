/*
 * $Id$
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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.vecmath.*;

/**
 * @author sascha
 *
 */
public class JPatchUserSettings extends JPatchSettings2 {
	private static JPatchUserSettings INSTANCE;
	
	public static class ColorSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/colors.png"));
		public Icon getIcon() {
			return icon;
		}
		public Color3f background = new Color3f(new Color(0x28,0x38,0x48));
		public Color3f curves = new Color3f(new Color(255,255,255));
		public Color3f points = new Color3f(new Color(255,255,0));
		public Color3f headPoints = new Color3f(new Color(255,0,0));
		public Color3f multiPoints = new Color3f(new Color(255,128,0));
		public Color3f selectedPoints = new Color3f(new Color(0,255,0));
		public Color3f hotObject = new Color3f(new Color(0,255,255));
		public Color3f tangents = new Color3f(new Color(255,255,0));
		public Color3f selection = new Color3f(new Color(255,255,0));
		public Color3f text = new Color3f(new Color(0x80,0x90,0xA0));
		public Color3f majorGrid = new Color3f(new Color(0x08,0x18,0x28));
		public Color3f minorGrid = new Color3f(new Color(0x18,0x28,0x38));
		public Color3f xAxis = new Color3f(new Color(255,64,0));
		public Color3f yAxis = new Color3f(new Color(0,255,0));
		public Color3f zAxis = new Color3f(new Color(128,128,255));
		public Color3f grey = new Color3f(new Color(0x50,0x60,0x70));
		public Color3f backfacingPatches = new Color3f(new Color(255,0,0));
		public float ghostFactor = 0.33f;
	}
	
	public static class RealtimeRendererSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/renderer.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum RealtimeRenderer { JAVA_2D, SOFTWARE_ZBUFFER, OPEN_GL };
		public static enum Backface { RENDER, HIDE, HIGHLIGHT };
		public static enum LightingMode { OFF, SIMPLE, HEADLIGHT, THREE_POINT };
		
		public RealtimeRenderer realtimeRenderer = RealtimeRenderer.SOFTWARE_ZBUFFER;
		public int realtimeRenererQuality = 5;
		public LightingMode lightingMode = LightingMode.THREE_POINT;
		public boolean lightFollowsCamera = false;
		public Backface backfacingPatches = Backface.RENDER;
		public boolean wireframeFogEffect = true;
	}
	
	public static class Directories extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/directories.png"));
		public Icon getIcon() {
			return icon;
		}
		public boolean rememberLastDirectories = true;
		public File jpatchFiles = new File(System.getProperty("user.dir"));
		public File spatchFiles = new File(System.getProperty("user.dir"));
		public File animationmasterFiles = new File(System.getProperty("user.dir"));
		public File povrayFiles = new File(System.getProperty("user.dir"));
		public File rendermanFiles = new File(System.getProperty("user.dir"));
		public File objFiles = new File(System.getProperty("user.dir"));
		public File rotoscopeFiles = new File(System.getProperty("user.dir"));
	}
	
	public static class Viewports extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/display.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum ScreenMode { SINGLE, HORIZONTAL_SPLIT, VERTICAL_SPLIT, QUAD };
		
		public ScreenMode viewportMode = ScreenMode.SINGLE;
		public boolean synchronizeViewports = false;
		public boolean snapToGrid = false;
		public float gridSpacing = 1.0f;
	}
	
	public static class RendererSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/export.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Renderer { POVRAY, RENDERMAN, INYO };
		
		public Renderer rendererToUse = Renderer.INYO;
		public int imageWidth = 640;
		public int imageHeight = 480;
		public float aspectWidth = 4;
		public float aspectHeight = 3;
		public Color3f backgroundColor = new Color3f(0.5f, 0.5f, 0.5f);
		public File workingDirectory = new File(System.getProperty("user.dir"));
		public File modelDirectory = new File(System.getProperty("user.dir"));
		public boolean deletePerFrameFilesAfterRendering = true;
		public final PovraySettings povray = new PovraySettings();
		public final RendermanSettings renderman = new RendermanSettings();
		public final InyoSettings inyo = new InyoSettings();
		public final AliasWavefrontSettings aliaswavefrontExport = new AliasWavefrontSettings();
	}
	
	public static class AliasWavefrontSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/obj.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Mode { TRIANGLES, QUADRILATERALS };
		
		public Mode outputMode = Mode.TRIANGLES;
		public int subdivisionLevel = 2;
		public boolean exportNormals = true;
		public boolean averageNormals = true;
	}
	
	public static class PovraySettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/povray.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Mode { TRIANGLES, BICUBIC_PATCHES };
		public static enum Antialias { OFF, METHOD_1, METHOD_2 };
		public static enum Version { UNIX, WINDOWS };
		public File executable = new File("");
		public String environmentVariables = "";
		public Version version = Version.UNIX;
		public Mode outputMode = Mode.TRIANGLES;
		public int subdivisionLevel = 3;
		public Antialias antialiasingMethod = Antialias.METHOD_1;
		public int antialiasingLevel = 2;
		public float antialiasingThreshold = 0.3f;
		public float antialiasingJitter = 1.0f;
		public File includeFile = new File("");
	}
	
	public static class RendermanSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/renderman.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Mode { TRIANGLES, QUADRILATERALS, CATMULL_CLARK_SUBDIVISION_SURFACE, BICUBIC_PATCHES };
		public static enum Interpolation { CONSTANT, SMOOTH };
		public File executable = new File("");
		public String environmentVariables = "";
		public Mode outputMode = Mode.TRIANGLES;
		public int subdivisionLevel = 3;
		public int pixelSamplesX = 2;
		public int pixelSamplesY = 2;
		public String pixelFilter = "gaussian";
		public int pixelFilterX = 2;
		public int pixelFilterY = 2;
		public float shadingRate = 1.0f;
		public Interpolation shadingInterpolation = Interpolation.SMOOTH;
		public float exposure = 1.0f;
	}
	
	public static class InyoSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/inyo.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Supersampling { ADAPTIVE, EVERYTHING };
		
		public File textureDirectory = new File(System.getProperty("user.dir"));
		public int subdivisionLevel = 3;
		public Supersampling supersamplingMode = Supersampling.ADAPTIVE;
		public int supersamplingLevel = 3;
		public int recursionDepth = 12;
		public int shadowSamples = 8;
		public boolean transparentShadows = false;
		public boolean caustics = false;
		public boolean oversampleCaustics = false;
		public boolean ambientOcclusion = false;
		public float ambientOcclusionDistance = 1000.0f;
		public int ambientOcclusionSamples = 3;
		public float ambientOcclusionColorbleed = 0.25f;	
	}
	
	public boolean newInstallation = true;
	public boolean cleanExit = false;
	public int screenPositionX = 0;
	public int screenPositionY = 0;
	public int screenWidth = 1024;
	public int screenHeight = 768;
	public boolean saveScreenDimensionsOnExit = true;
	public String lookAndFeelClassname = "javax.swing.plaf.metal.MetalLookAndFeel";
	
	public final Directories directories = new Directories();
	public final Viewports viewports = new Viewports();
	public final ColorSettings colors = new ColorSettings();
	public final RealtimeRendererSettings realtimeRenderer = new RealtimeRendererSettings();
	public final RendererSettings export = new RendererSettings();
	
	public static void main(String[] args) {
		JPatchUserSettings settings = new JPatchUserSettings();
		settings.dump("");
//		settings.testInteger = 12;
//		settings.save();
//		settings.dump("");
//		settings.load("");
		settings.dump("");
		JFrame frame = new JFrame(settings.toString());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		settings.initTree();
		final JTable table = settings.getTable();
		JTree tree = new JTree(settings);
		tree.setCellRenderer(settings.getTreeCellRenderer());
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				JPatchSettings2 settings = (JPatchSettings2) e.getPath().getLastPathComponent();
				table.setModel((TableModel) settings.getTableModel());
				table.getColumnModel().getColumn(0).setHeaderValue("Preference Name");
				table.getColumnModel().getColumn(1).setHeaderValue("Value");
				table.setDefaultEditor(Object.class, settings.getTableCellEditor());
			}
			
		});
		splitPane.add(new JScrollPane(tree));
//		JPanel tablePanel = new JPanel();
		
		
//		settings.getTableCellEditor().addCellEditorListener(new CellEditorListener() {
//
//			public void editingStopped(ChangeEvent e) {
//				System.out.println("editingStopped " + e.);
//			}
//
//			public void editingCanceled(ChangeEvent e) {
//				System.out.println("editingCanceled " + e);
//			}
//		});
		splitPane.add(new JScrollPane(settings.getTable()));
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
	}
	
	private JPatchUserSettings() {
		storeDefaults();
		INSTANCE = this;
	}
	
	public static JPatchUserSettings getInstance() {
		if (INSTANCE == null)
			new JPatchUserSettings();
		return INSTANCE;
	}
}
