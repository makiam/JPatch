package com.jpatch.test;

import java.util.*;

import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

public class EdgeSetTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SdsModel sdsModel = new SdsModel(new Sds(null));
		
		Sds.EdgeSet edgeSet = new Sds.EdgeSet();
		AbstractVertex[] vertices = new AbstractVertex[20];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new BaseVertex(sdsModel);
		}
		
		Set<HalfEdge> edges = new HashSet<HalfEdge>();
		Random rnd = new Random();
		for (int i = 0; i < 1000; i++) {
			int i0, i1;
			i0 = rnd.nextInt(vertices.length);
			do {
				i1 = rnd.nextInt(vertices.length);
			} while (i0 == i1);
			System.out.println(i0 + "-" + i1 + ":");
			edges.add(edgeSet.getHalfEdge(vertices[i0], vertices[i1]));
		}
		
		for (HalfEdge edge : edges) {
			System.out.println("removing " + edge);
			edgeSet.removeHalfEdge(edge);
		}
	}

}
