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
package com.jpatch.entity.sds2;

import java.util.*;

public class Id {
	private final int[] id;
	
	Id() {
		topLevelId = count++;
		subdivId = null;
	}
	
	private Id(int topLevelId) {
		this.topLevelId = topLevelId;
		subdivId = null;
	}
	
	Id(Id parentId, byte index) {
		topLevelId = parentId.topLevelId;
		if (parentId.subdivId == null) {
			subdivId = new byte[] { index };
		} else {
			subdivId = new byte[parentId.subdivId.length + 1];
			System.arraycopy(parentId.subdivId, 0, subdivId, 0, parentId.subdivId.length);
			subdivId[parentId.subdivId.length] = index;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Id) {
			Id id = (Id) o;
			return topLevelId == id.topLevelId && Arrays.equals(subdivId, id.subdivId);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return topLevelId ^ (298375 + 3 * Arrays.hashCode(subdivId));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toString(topLevelId));
		if (subdivId != null) {
			for (byte b : subdivId) {
				sb.append(':');
				sb.append(Byte.toString(b));
			}
		}
		return sb.toString();
	}
}