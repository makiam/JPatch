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
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.vecmath.*;

/**
 * @author sascha
 *
 */
public class JPatchUserSettings extends JPatchSettings2 {
	public static class PovraySettings extends JPatchSettings2 {
		public int pov = 20;
	}
	public static class RendermanSettings extends JPatchSettings2 {
		public int rib = 20;
	}
	public static class RendererSettings extends JPatchSettings2 {
		public PovraySettings povray = new PovraySettings();
		public RendermanSettings renderman = new RendermanSettings();
	}
	
	public static enum TestEnum { YES, NO, ASK };
	public int testInteger = 10;
	public boolean delete_per_frame_files = true;
	public TestEnum testEnum = TestEnum.NO;
	public Color testColor = Color.CYAN;
	public Color3f testColor3f = new Color3f(1.0f, 0.5f, 0.0f);
	private int privateInt = 20;
	public String testString = "abcdef\"</xml>";
	public RendererSettings renderer = new RendererSettings();
	
	public static void main(String[] args) {
		JPatchUserSettings settings = new JPatchUserSettings();
		settings.dump("");
		settings.testInteger = 12;
		settings.save();
		settings.dump("");
		settings.load("");
		settings.dump("");
		JFrame frame = new JFrame(settings.toString());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		settings.initTree();
		JTree tree = new JTree(settings);
		splitPane.add(new JScrollPane(tree));
//		JPanel tablePanel = new JPanel();
		
		JTable table = new JTable(settings);
		table.getColumnModel().getColumn(0).setHeaderValue("Preference Name");
		table.getColumnModel().getColumn(1).setHeaderValue("Type");
		table.getColumnModel().getColumn(2).setHeaderValue("Value");
		table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));
		table.setDefaultEditor(Boolean.class, new DefaultCellEditor(new JCheckBox()));
		table.setDefaultRenderer(table.getColumnClass(0), settings.getTableCellRenderer());
		splitPane.add(new JScrollPane(table));
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
	}
	
	public JPatchUserSettings() {
		storeDefaults();
		load("");
	}
}
