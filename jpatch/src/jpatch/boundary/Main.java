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
package jpatch.boundary;

import java.io.*;
import java.util.*;

import javax.swing.*;

import jpatch.entity.Project;

import jpatch.boundary.settings.*;

/**
 * @author sascha
 *
 */
public class Main {
	public static final Main INSTANCE = new Main();	// singleton pattern
	
	private JFrame frame = new JFrame();
	private JToolBar primaryToolBar;
	private JToolBar secondaryToolBar;
	
	public static void main(String[] args) {

	}
	/**
	 * private constructor (singleton pattern)
	 */
	private Main() {
		try {
			WorkspaceManager workspaceManager = new WorkspaceManager(Settings.getInstance().workspace);
			Project project = new Project(workspaceManager, "Test_Project");
		} catch (IOException e) {
			e.printStackTrace();
		}
		frame.setTitle("JPatch");
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
