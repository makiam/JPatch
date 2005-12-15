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
	
	public static class ColorSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/colors.png"));
		public Icon getIcon() {
			return icon;
		}
		public Color3f background = new Color3f(new Color(0x28,0x38,0x48));
		public Color3f curves = new Color3f(new Color(255,255,255));
		public Color3f points = new Color3f(new Color(255,255,0));
		public Color3f head_points = new Color3f(new Color(255,0,0));
		public Color3f multi_points = new Color3f(new Color(255,128,0));
		public Color3f selected_points = new Color3f(new Color(0,255,0));
		public Color3f hot_object = new Color3f(new Color(0,255,255));
		public Color3f tangents = new Color3f(new Color(255,255,0));
		public Color3f selection = new Color3f(new Color(255,255,0));
		public Color3f text = new Color3f(new Color(0x80,0x90,0xA0));
		public Color3f major_grid = new Color3f(new Color(0x08,0x18,0x28));
		public Color3f minor_grid = new Color3f(new Color(0x18,0x28,0x38));
		public Color3f x_axis = new Color3f(new Color(255,64,0));
		public Color3f y_axis = new Color3f(new Color(0,255,0));
		public Color3f z_axis = new Color3f(new Color(128,128,255));
		public Color3f grey = new Color3f(new Color(0x50,0x60,0x70));
		public Color3f backfacing_patches = new Color3f(new Color(255,0,0));
	}
	
	public static class RealtimeRendererSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/renderer.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum RealtimeRenderer { JAVA_2D, SOFTWARE_ZBUFFER, OPEN_GL };
		public static enum Backface { RENDER, HIDE, HIGHLIGHT };
		public static enum LightingMode { OFF, SIMPLE, HEADLIGHT, THREE_POINT };
		
		public RealtimeRenderer realtime_renderer = RealtimeRenderer.SOFTWARE_ZBUFFER;
		public int realtime_renerer_quality = 5;
		public LightingMode lighting_mode = LightingMode.THREE_POINT;
		public boolean light_follows_camera = false;
		public Backface backfacing_patches = Backface.RENDER;
		public boolean wireframe_fog_effect = true;
	}
	
	public static class Directories extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/directories.png"));
		public Icon getIcon() {
			return icon;
		}
		public boolean remember_last_directories = true;
		public File JPatch_files = new File(System.getProperty("user.dir"));
		public File sPatch_files = new File(System.getProperty("user.dir"));
		public File Animation_Master_files = new File(System.getProperty("user.dir"));
		public File PovRay_files = new File(System.getProperty("user.dir"));
		public File RenderMan_files = new File(System.getProperty("user.dir"));
		public File obj_files = new File(System.getProperty("user.dir"));
		public File rotoscope_image_files = new File(System.getProperty("user.dir"));
	}
	
	public static class Viewports extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/display.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum ScreenMode { SINGLE, HORIZONTAL_SPLIT, VERTICAL_SPLIT, QUAD };
		
		public ScreenMode viewport_mode = ScreenMode.SINGLE;
		public boolean synchronize_viewports = false;
		public boolean snap_to_grid = false;
		public float grid_spacing = 1.0f;
	}
	
	public static class RendererSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/export.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Renderer { POV_RAY, RENDERMAN, INYO };
		
		public Renderer renderer_to_use = Renderer.INYO;
		public int image_width = 640;
		public int image_height = 480;
		public float aspect_width = 4;
		public float aspect_height = 3;
		public Color3f background_color = new Color3f(0.5f, 0.5f, 0.5f);
		public File working_directory = new File(System.getProperty("user.dir"));
		public File model_directory = new File(System.getProperty("user.dir"));
		public boolean delete_per_frame_files_after_rendering = true;
		public PovraySettings Pov_Ray = new PovraySettings();
		public RendermanSettings RenderMan = new RendermanSettings();
		public InyoSettings Inyo = new InyoSettings();
		public AliasWavefrontSettings Alias_Wavefront_export = new AliasWavefrontSettings();
	}
	
	public static class AliasWavefrontSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/obj.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Mode { TRIANGLES, QUADRILATERALS };
		
		public Mode output_mode = Mode.TRIANGLES;
		public int subdivision_level = 2;
		public boolean export_normals = true;
		public boolean average_normals = true;
	}
	
	public static class PovraySettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/povray.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Mode { TRIANGLES, BICUBIC_PATCHES };
		public static enum Antialias { METHOD_1, METHOD_2 };
		public File PovRay_executable = new File("");
		public String environment_variables = "";
		public Mode output_mode = Mode.TRIANGLES;
		public int subdivision_level = 3;
		public Antialias antialiasing_method = Antialias.METHOD_1;
		public int antialiasing_level = 2;
		public float antialiasing_threshold = 0.3f;
		public float antialiasing_jitter = 1.0f;
		public File include_file = new File("");
	}
	
	public static class RendermanSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/renderman.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Mode { TRIANGLES, QUADRILATERALS, CATMULL_CLARK_SUBDIVISION_SURFACE, BICUBIC_PATCHES };
		public static enum Interpolation { CONSTANT, SMOOTH };
		public File RenderMan_executable = new File("");
		public String environment_variables = "";
		public Mode output_mode = Mode.TRIANGLES;
		public int subdivision_level = 3;
		public int pixel_samples_x = 2;
		public int pixel_samples_y = 2;
		public String pixel_filter = "gaussian";
		public int pixel_filter_x = 2;
		public int pixel_filter_y = 2;
		public float shading_rate = 1.0f;
		public Interpolation shading_interpolation = Interpolation.SMOOTH;
		public float exposure = 1.0f;
	}
	
	public static class InyoSettings extends JPatchSettings2 {
		private Icon icon = new ImageIcon(ClassLoader.getSystemResource("jpatch/images/prefs/inyo.png"));
		public Icon getIcon() {
			return icon;
		}
		public static enum Supersampling { ADAPTIVE, EVERYTHING };
		
		public File texture_directory = new File(System.getProperty("user.dir"));
		public int subdivision_level = 3;
		public Supersampling supersampling_mode = Supersampling.ADAPTIVE;
		public int supersampling_level = 3;
		public int recursion_depth = 12;
		public int shadow_samples = 8;
		public boolean transparent_shadows = false;
		public boolean caustics = false;
		public boolean oversample_caustics = false;
		public boolean ambient_occlusion = false;
		public float ambient_occlusion_distance = 1000.0f;
		public int ambient_occlusion_samples = 3;
		public float ambient_occlusion_colorbleed = 0.25f;	
	}
	
	public boolean new_installation = true;
	public boolean clean_exit = false;
	public int screen_position_x = 0;
	public int screen_position_y = 0;
	public int screen_width = 1024;
	public int screen_height = 768;
	public boolean save_screen_dimensions_on_exit = true;
	public String look_and_feel_classname = "javax.swing.plaf.metal.MetalLookAndFeel";
	
	public Directories directories = new Directories();
	public Viewports viewports = new Viewports();
	public ColorSettings colors = new ColorSettings();
	public RealtimeRendererSettings realtime_renderer = new RealtimeRendererSettings();
	public RendererSettings export_settings = new RendererSettings();
	
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
	
	public JPatchUserSettings() {
		storeDefaults();
//		load("");
	}
}
