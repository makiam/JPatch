/*
 * $Id: Settings.java,v 1.1 2005/12/29 16:13:48 sascha_l Exp $
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
package jpatch.boundary.settings;

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
public class Settings extends AbstractSettings {
	private static Settings INSTANCE;
	
	public boolean newInstallation = true;
	public boolean cleanExit = false;
	public int screenPositionX = 0;
	public int screenPositionY = 0;
	public int screenWidth = 1024;
	public int screenHeight = 768;
	public boolean saveScreenDimensionsOnExit = true;
	public String lookAndFeelClassname = "javax.swing.plaf.metal.MetalLookAndFeel";
	
	public final DirectorySettings directories = new DirectorySettings();
	public final ViewportSettings viewports = new ViewportSettings();
	public final ColorSettings colors = new ColorSettings();
	public final RealtimeRendererSettings realtimeRenderer = new RealtimeRendererSettings();
	public final RendererSettings export = new RendererSettings();
	
	public static void main(String[] args) {
		Settings settings = new Settings();
		settings.dump("");
		settings.save();
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
				AbstractSettings settings = (AbstractSettings) e.getPath().getLastPathComponent();
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
	
	private Settings() {
		storeDefaults();
		INSTANCE = this;
	}
	
	public static Settings getInstance() {
		if (INSTANCE == null)
			new Settings();
		return INSTANCE;
	}
}
