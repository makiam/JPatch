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
package jpatch.boundary.action;

import java.awt.event.*;
import javax.swing.*;
import jpatch.boundary.*;

/**
 * This action is used to toggle states in respones to clicks
 * on togglebuttons. Undo/redo support is not needed, so the
 * actions performed are rather straightforward.
 * @author sascha
 */

@SuppressWarnings("serial")
public class ToggleAction extends AbstractAction {
	public static enum Type {
		LOCK_X, LOCK_Y, LOCK_Z,
		LOCK_POINTS, LOCK_BONES,
		SELECT_POINTS, SELECT_BONES,
		SNAP_TO_GRID,
		SHOW_POINTS, SHOW_CURVES, SHOW_PATCHES, SHOW_ROTOSCOPE
	}
	private final Type type;
	
	/**
	 * 
	 */
	public ToggleAction(Type type) {
		this.type = type;
	}
	
	public void actionPerformed(ActionEvent e) {
		switch (type) {
		case LOCK_X:
			MainFrame.getInstance().getConstraints().setXLock(Actions.getInstance().getButtonModel("lock x").isSelected());
			break;
		case LOCK_Y:
			MainFrame.getInstance().getConstraints().setYLock(Actions.getInstance().getButtonModel("lock y").isSelected());
			break;
		case LOCK_Z:
			MainFrame.getInstance().getConstraints().setZLock(Actions.getInstance().getButtonModel("lock z").isSelected());
			break;
		case LOCK_POINTS:
			MainFrame.getInstance().getJPatchScreen().setLockPoints(Actions.getInstance().getButtonModel("lock points").isSelected());
			break;
		case LOCK_BONES:
			MainFrame.getInstance().getJPatchScreen().setLockBones(Actions.getInstance().getButtonModel("lock bones").isSelected());
			break;
		case SELECT_POINTS:
			MainFrame.getInstance().getJPatchScreen().setSelectPoints(Actions.getInstance().getButtonModel("select points").isSelected());
			break;
		case SELECT_BONES:
			MainFrame.getInstance().getJPatchScreen().setSelectBones(Actions.getInstance().getButtonModel("select bones").isSelected());
			break;
		case SNAP_TO_GRID:
			MainFrame.getInstance().getJPatchScreen().snapToGrid(Actions.getInstance().getButtonModel("snap to grid").isSelected());
			break;
		case SHOW_POINTS:
			MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().renderPoints(Actions.getInstance().getButtonModel("show points").isSelected());
			break;
		case SHOW_CURVES:
			MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().renderCurves(Actions.getInstance().getButtonModel("show curves").isSelected());
			break;
		case SHOW_PATCHES:
			MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().renderPatches(Actions.getInstance().getButtonModel("show patches").isSelected());
			break;
		case SHOW_ROTOSCOPE:
			MainFrame.getInstance().getJPatchScreen().getActiveViewport().getViewDefinition().showRotoscope(Actions.getInstance().getButtonModel("show rotoscope").isSelected());
			break;
		}
	}
}
