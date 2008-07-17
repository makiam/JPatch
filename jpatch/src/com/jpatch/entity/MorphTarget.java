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
package com.jpatch.entity;

import java.util.*;

import com.jpatch.afw.attributes.*;

public class MorphTarget {
	private final List<DoubleAttr> attributeList = new ArrayList<DoubleAttr>();
	private final List<DoubleAttr> attributeListView = Collections.unmodifiableList(attributeList);
	
	public List<DoubleAttr> getAttributes() {
		return attributeListView;
	}
	
	public void addAttribute(DoubleAttr attr) {
		attributeList.add(attr);
	}
	
	public void addAttribute(Tuple2Attr attr) {
		attributeList.add(attr.getXAttr());
		attributeList.add(attr.getYAttr());
	}
	
	public void addAttribute(Tuple3Attr attr) {
		attributeList.add(attr.getXAttr());
		attributeList.add(attr.getYAttr());
		attributeList.add(attr.getZAttr());
	}
	
	public void removeAttribute(DoubleAttr attr) {
		attributeList.remove(attr);
	}
	
	public void removeAttribute(Tuple2Attr attr) {
		attributeList.remove(attr.getXAttr());
		attributeList.remove(attr.getYAttr());
	}
	
	public void removeAttribute(Tuple3Attr attr) {
		attributeList.remove(attr.getXAttr());
		attributeList.remove(attr.getYAttr());
		attributeList.remove(attr.getZAttr());
	}
	
	public int getSize() {
		return attributeList.size();
	}
	
	public void readValues(double[] values, int start) {
		int index = start;
		for (DoubleAttr attr : attributeList) {
			values[index++] = attr.getDouble();
		}
	}
	
	public void writeValues(double[] values, int start) {
		int index = start;
		for (DoubleAttr attr : attributeList) {
			attr.setDouble(values[index++]);
		}
	}
}