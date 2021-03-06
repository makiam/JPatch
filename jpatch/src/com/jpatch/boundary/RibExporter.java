package com.jpatch.boundary;

import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;
import java.io.*;
import java.util.*;

import javax.vecmath.*;

public class RibExporter {
	private static final int LEVEL = 1;
	
	public void export(SdsModel sdsModel, PrintStream out) {
		Sds sds = sdsModel.getSds();
		
//		AmbientOcclusion ao = new AmbientOcclusion();
//		ao.computeAo(sds, LEVEL);
		
//		AmbientOcclusion2 ao = new AmbientOcclusion2();
//		ao.compute(sdsModel);
//		ao.toRibSpheres(out);
		
		out.print("SubdivisionMesh \"catmull-clark\" [");
		
//		/* count slates */
//		int numSlates = 0;
//		for (Face face : sds.faceList) {
//			numSlates += face.sides;
//		}
		
		/* enumerate (level 2) vertices */
		Map<AbstractVertex, Integer> vertexNumbers = new LinkedHashMap<AbstractVertex, Integer>();
		int n = 0;
		for (Face face : sds.getFaces(LEVEL)) {
			for (HalfEdge edge : face.getEdges()) {
				AbstractVertex v = edge.getVertex();
				if (!vertexNumbers.containsKey(v)) {
					vertexNumbers.put(v, n++);
				}
			}	
		}
		
		/* print sides per face array */
		for (int i = 0, s = sds.getFaces(LEVEL).size(); i < s; i++) {
			out.print("4 ");
		}
		out.println("]");
		
		/* print face vertex numbers */
		out.print("[");
		for (Face face : sds.getFaces(LEVEL)) {
			for (HalfEdge edge : face.getEdges()) {
				AbstractVertex v = edge.getVertex();
				out.print(vertexNumbers.get(v));
				out.print(' ');
			}
		}
		out.println("]");
		
		/* print sds parameters */
		out.println("[\"interpolateboundary\"] [0 0] [] []");
		
		/* print vertex positions */
		out.print("\"P\" [");
		Point3d p = new Point3d();
		for (AbstractVertex v : vertexNumbers.keySet()) {
			v.getPosition(p);
			out.print(p.x);
			out.print(' ');
			out.print(p.y);
			out.print(' ');
			out.print(p.z);
			out.print(' ');
		}
		out.println("]");
		
//		/* print vertex occlusion */
//		out.print("\"vertex float Occlusion\" [");
//		for (AbstractVertex v : vertexNumbers.keySet()) {
//			out.print(ao.getOcclusion(v));
//			out.print(' ');
//		}
//		out.println("]");
	}
}
