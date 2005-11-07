/*
 * $Id: AtomicModifySelection.java,v 1.8 2005/11/07 16:12:03 sascha_l Exp $
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
package jpatch.control.edit;

import java.util.*;
import javax.vecmath.*;

import jpatch.boundary.*;

/**
 * @author sascha
 *
 */
public abstract class AtomicModifySelection extends JPatchAtomicEdit {
	Selection selection;
	
	private AtomicModifySelection() { }
	
	void addObjects(Map objects) {
		selection.getMap().putAll(objects);
		MainFrame.getInstance().setSelection(selection);
	}
	
	void removeObjects(Map objects) {
		for (Iterator it = objects.keySet().iterator(); it.hasNext(); )
			selection.getMap().remove(it.next());
		MainFrame.getInstance().setSelection(selection);
	}

	public static final class AddObjects extends AtomicModifySelection {
		private final Map mapObjects;
		public AddObjects(Selection selection, Map objects) {
//			if (DEBUG)
				System.out.println(getClass().getName() + "(" + selection + ", " + objects + ")");
			this.selection = selection;
			mapObjects = new HashMap(objects);
			redo();
		}
		public void redo() {
			addObjects(mapObjects);
		}
		
		public void undo() {
			removeObjects(mapObjects);
		}
		
		public String getName() {
			return "add to selection";
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * mapObjects.size() * 2);
		}
	}
	
	public static final class RemoveObjects extends AtomicModifySelection {
		private final Map mapObjects;
		public RemoveObjects(Selection selection, Map objects) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + selection + ", " + objects + ")");
			this.selection = selection;
			mapObjects = new HashMap(objects);
			redo();
		}
		public void redo() {
			removeObjects(mapObjects);
		}
		
		public void undo() {
			addObjects(mapObjects);
		}
		
		public String getName() {
			return "add to selection";
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + (8 + 4 + 4 + 4 + 4 + 8 * mapObjects.size() * 2);
		}
	}
	
	public static final class Pivot extends AtomicModifySelection implements JPatchRootEdit {
		private float x, y, z;
		public Pivot(Selection selection, Point3f pivot) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + selection + ", " + pivot + ")");
			this.selection = selection;
			x = pivot.x;
			y = pivot.y;
			z = pivot.z;
		}
		
		private void swap() {
			Point3f p = selection.getPivot();
			float dummyX = p.x;
			float dummyY = p.y;
			float dummyZ = p.z;
			selection.getPivot().set(x, y, z);
			x = dummyX;
			y = dummyY;
			z = dummyZ;
		}
		
		public void undo() {
			swap();
		}
		
		public void redo() {
			swap();
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4 + 4 + 4;
		}
		
		public String getName() {
			return "move pivot";
		}
	}
	
	public static final class HotObject extends AtomicModifySelection {
		private Object hot;
		public HotObject(Selection selection, Object hot) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + selection + ", " + hot + ")");
			this.selection = selection;
			this.hot = hot;
			swap();
		}
		
		private void swap() {
			Object dummy = hot;
			hot = selection.getHotObject();
			selection.setHotObject(dummy);
		}
		
		public void undo() {
			swap();
		}
		
		public void redo() {
			swap();
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 4;
		}
	}
	
	public static final class Orientation extends AtomicModifySelection implements JPatchRootEdit {
		private final Matrix3f m3Orient = new Matrix3f();
		public Orientation(Selection selection, Matrix3f orientation) {
			if (DEBUG)
				System.out.println(getClass().getName() + "(" + selection + ", " + orientation + ")");
			this.selection = selection;
			m3Orient.set(orientation);
			swap();
		}
		
		private void swap() {
			Matrix3f dummy = new Matrix3f(m3Orient);
			m3Orient.set(selection.getOrientation());
			selection.getOrientation().set(dummy);
		}
		
		public void undo() {
			swap();
		}
		
		public void redo() {
			swap();
		}
		
		public String getName() {
			return "change tool orientation";
		}
		
		public int sizeOf() {
			return 8 + 4 + 4 + 8 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4;
		}
	}
}
