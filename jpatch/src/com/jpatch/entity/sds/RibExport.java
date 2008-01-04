package com.jpatch.entity.sds;

import java.io.*;
import java.util.*;

public class RibExport {
	public void export(Sds sds, OutputStream out) {
		System.out.println("*RIB EXPORT*");
		Set<TopLevelVertex> vertexSet = new HashSet<TopLevelVertex>();
		for (Face face : sds.faceList) {
			for (HalfEdge edge : face.getEdges()) {
				vertexSet.add(edge.getVertex());
			}
		}
		List<TopLevelVertex> vertexList = new ArrayList<TopLevelVertex>(vertexSet);
		vertexSet = null;
		Map<TopLevelVertex, Integer> vertexIndexMap = new HashMap<TopLevelVertex, Integer>();
		for (int i = 0; i < vertexList.size(); i++) {
			vertexIndexMap.put(vertexList.get(i), i);
		}
		PrintWriter writer = new PrintWriter(out);
		writer.print("SubdivisionMesh \"catmull-clark\" [");
		boolean omitSpace = true;
		for (Face face : sds.faceList) {
			if (omitSpace) {
				omitSpace = false;
			} else {
				writer.print(" ");
			}
			writer.print(face.sides);
		}
		writer.print("] [");
		omitSpace = true;
		for (int i = 0; i < sds.faceList.size(); i++) {
			for (HalfEdge edge : sds.faceList.get(i).getEdges()) {
				if (omitSpace) {
					omitSpace = false;
				} else {
					writer.print(" ");
				}
				writer.print(vertexIndexMap.get(edge.getVertex()));
			}
		}
		writer.print("]\n");
		writer.print("\"P\" [");
		omitSpace = true;
		for (TopLevelVertex vertex : vertexList) {
			if (omitSpace) {
				omitSpace = false;
			} else {
				writer.print(" ");
			}
			writer.print(vertex.position.getX());
			writer.print(" ");
			writer.print(vertex.position.getY());
			writer.print(" ");
			writer.print(vertex.position.getZ());
		}
		writer.print("]\n");
		writer.close();
	}
}
