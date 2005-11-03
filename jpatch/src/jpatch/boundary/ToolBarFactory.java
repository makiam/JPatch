/*
 * $Id: ToolBarFactory.java,v 1.1 2005/11/03 16:59:37 sascha_l Exp $
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

import javax.swing.*;

import jpatch.boundary.action.*;

/**
 * @author sascha
 *
 */
public class ToolBarFactory {
	public static JToolBar createEditToolBar() {
		JToolBar tb = new JToolBar("edit", JToolBar.HORIZONTAL);
		
		tb.add(JPatchSeparator.createVerticalSeparator());
		
		tb.add(Command.getButtonFor("default tool"));
		tb.add(Command.getButtonFor("add curve segment"));
		tb.add(Command.getButtonFor("add bone"));
		tb.add(Command.getButtonFor("rotate"));
		tb.add(Command.getButtonFor("weight selection"));
		
		tb.add(JPatchSeparator.createVerticalSeparator());
		
		tb.add(Command.getButtonFor("rotoscope tool"));
		
		tb.add(JPatchSeparator.createVerticalSeparator());
		
		tb.add(Command.getButtonFor("tangent tool"));
		tb.add(Command.getButtonFor("peak tangents"));
		tb.add(Command.getButtonFor("round tangents"));
		
		tb.add(JPatchSeparator.createVerticalSeparator());
		
		tb.add(Command.getButtonFor("clone"));
		tb.add(Command.getButtonFor("extrude"));
		tb.add(Command.getButtonFor("lathe"));
		tb.add(Command.getButtonFor("lathe editor"));
		
		tb.add(JPatchSeparator.createVerticalSeparator());
		
		tb.add(Command.getButtonFor("make patch"));
		tb.add(Command.getButtonFor("compute patches"));
		
		return tb;
	}
}
