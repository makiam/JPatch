/*
 * $Id: JPatchUndoableEdit.java,v 1.12 2006/05/22 10:46:19 sascha_l Exp $
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
 * A basic interface all edits must implement
 *
 * @version	$Revision: 1.12 $
 * @author	Sascha Ledinsky
 */
public interface JPatchUndoableEdit {
	/**
	 * debug flag
	 */
	static final boolean DEBUG = false;
	
	/**
	 * undoes this edit
	 */
	public void undo();
	
	/**
	 * redoes this edit
	 */
	public void redo();
	
	/**
	 * returns fales if the edit is a compound edit
	 */
	public boolean isAtomic();
	
	/**
	 * returns the (guessed) size of the edit object
	 */
	public int sizeOf();
	
	public void debug(String prefix);
}

