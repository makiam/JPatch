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
package com.jpatch.boundary.tools;

import com.jpatch.entity.sds2.*;

import javax.vecmath.*;

public class ConstraintVertexTranslation {
	private final AbstractVertex vertex;
	private final Point3d startPosition = new Point3d();
	private final Vector3d vector = new Vector3d();
	
	public ConstraintVertexTranslation(AbstractVertex vertex, Vector3d vector) {
		this.vertex = vertex;
		vertex.getPosition(startPosition);
		this.vector.set(vector);
	}
	
	public ConstraintVertexTranslation(AbstractVertex vertex, Point3d target) {
		this.vertex = vertex;
		vertex.getPosition(startPosition);
		vector.sub(target, startPosition);
	}
	
	public void moveTo(double alpha) {
		vertex.setPosition(
				startPosition.x + vector.x * alpha,
				startPosition.y + vector.y * alpha,
				startPosition.z + vector.z * alpha
		);
	}
	
	public Vector3d getVector() {
		return vector;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ConstraintVertexTranslation) {
			return vertex == ((ConstraintVertexTranslation) o).vertex;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(vertex);
	}
}