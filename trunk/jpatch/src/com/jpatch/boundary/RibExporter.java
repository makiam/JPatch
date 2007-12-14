package com.jpatch.boundary;

import com.jpatch.entity.sds.*;
import java.io.*;
import java.util.*;

public class RibExporter {

	public void export(Sds sds, PrintStream out) {
		AmbientOcclusion ao = new AmbientOcclusion();
		ao.computeAo(sds);
		out.print("SubdivisionMesh \"catmull-clark\" [");
		
		/* count slates */
		int numSlates = 0;
		for (Face face : sds.faceList) {
			numSlates += face.sides;
		}
		
		/* enumerate (level 2) vertices */
		Map<Level2Vertex, Integer> vertexNumbers = new HashMap<Level2Vertex, Integer>();
		int n = 0;
		for (Level2Vertex v : sds.level2Vertices) {
			vertexNumbers.put(v, n++);
		}
		
		/* print sides per face array */
		for (int i = 0; i < numSlates; i++) {
			out.print("4 ");
		}
		out.println("]");
		
		/* print face vertex numbers */
		out.print("[");
		for (Face face : sds.faceList) {
			for (Slate2 slate : face.getSlates()) {
				SlateEdge[][] edges = slate.getCorners();
				for (int i = 0; i < 4; i++) {
					out.print(vertexNumbers.get(edges[i][0].getVertex()));
					out.print(' ');
				}
			}
		}
		out.println("]");
		
		/* print sds parameters */
		out.println("[\"interpolateboundary\"] [0 0] [] []");
		
		/* print vertex positions */
		out.print("\"P\" [");
		for (Level2Vertex v : sds.level2Vertices) {
			out.print(v.getPosition().getX());
			out.print(' ');
			out.print(v.getPosition().getY());
			out.print(' ');
			out.print(v.getPosition().getZ());
			out.print(' ');
		}
		out.println("]");
		
		/* print vertex occlusion */
		out.print("\"vertex float Occlusion\" [");
		for (Level2Vertex v : sds.level2Vertices) {
			out.print(ao.getOcclusion(v));
			out.print(' ');
		}
		out.println("]");
	}
}
