/*
 * $Id$
 *
 * Copyright (c) 2004 Sascha Ledinsky
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
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jpatch.control.edit;

/**
 * An abstract class implementing the JPatchUndoableEdit interface<br>
 * It can be subclassed to create custom edits
 *
 * @version	$Revision$
 * @author	Sascha Ledinsky
 */
public abstract class JPatchAbstractUndoableEdit implements JPatchUndoableEdit {
	/**
	 * Override this method
	 */
	public abstract void undo();
	/**
	 * Override this method
	 */
	public abstract void redo();
	/**
	 * Returns an empty string
	 * @return an empty string
	 */
	public String name() {
		return "";
	}
	
	public boolean isAtomic() {
		return true;
	}
}
