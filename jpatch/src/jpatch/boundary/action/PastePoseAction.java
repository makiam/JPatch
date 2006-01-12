/*
 * $Id: PastePoseAction.java,v 1.1 2006/01/12 17:05:37 sascha_l Exp $
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
package jpatch.boundary.action;

import javax.swing.*;
import java.awt.event.*;
import jpatch.boundary.*;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class PastePoseAction extends AbstractAction {
	private Model model;
	
	public PastePoseAction(Model model) {
		super("paste pose");
		this.model = model;
	}
	
	public void actionPerformed(ActionEvent e) {
		MainFrame.getInstance().getAnimation().getClipboardPose(model).applyPose();
		MainFrame.getInstance().getJPatchScreen().update_all();
	}
}
