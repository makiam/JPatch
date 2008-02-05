package com.jpatch.boundary;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

import com.jpatch.afw.vecmath.*;
import com.jpatch.entity.*;
import com.jpatch.entity.sds2.*;

import static com.jpatch.afw.vecmath.TransformUtil.*;

public class AmbientOcclusion2 {
	private TransformUtil transformUtil = new TransformUtil();
	private Collection<Disc> discs = new ArrayList<Disc>();
	
	public void compute(SceneGraphNode rootNode) {
		discs.clear();
		computeDiscs(rootNode);
		for (Disc disc : discs) {
			System.out.println(disc);
		}
		System.out.println(discs.size());
	}
	
	public void toRib(PrintStream out) {
		String n = Integer.toString(discs.size());
		out.println("Declare \"numberOfDiscs\" \"uniform float\"");
		out.println("Declare \"positions\" \"uniform point[" + n + "]\"");
		out.println("Declare \"normals\" \"uniform normal[" + n + "]\"");
		out.println("Declare \"areas\" \"uniform float[" + n + "]\"");
		out.print("Surface \"ao\" \"numberOfDiscs\" ");
		out.print(discs.size());
		out.print(" \"positions\" [");
		for (Disc disc : discs) {
			out.print(' ');
			out.print(disc.position.x);
			out.print(' ');
			out.print(disc.position.y);
			out.print(' ');
			out.print(disc.position.z);
			out.print(' ');
		}
		out.print("] \"normals\" [");
		for (Disc disc : discs) {
			out.print(' ');
			out.print(disc.normal.x);
			out.print(' ');
			out.print(disc.normal.y);
			out.print(' ');
			out.print(disc.normal.z);
			out.print(' ');
		}
		out.print("] \"areas\" [ ");
		for (Disc disc : discs) {
			out.print(disc.area / Math.PI);
			out.print(' ');
		}
		out.print("]");
		out.println();
	}
	
	private void computeDiscs(SceneGraphNode node) {
		if (node instanceof SdsModel) {
			addDiscs((SdsModel) node);
		}
		for (SceneGraphNode child : node.getChildrenAttribute().getElements()) {
			computeDiscs(child);
		}
	}
	
	private void addDiscs(SdsModel sdsModel) {
		final int level = 0;
		sdsModel.getLocal2WorldTransform(transformUtil, LOCAL);
		Point3d center = new Point3d();
		Point3d p0 = new Point3d();
		Point3d p1 = new Point3d();
		Vector3d normal = new Vector3d();
		for (Face face : sdsModel.getSds().getFaces(level)) {
			face.getMidpointPosition(center);
			face.getMidpointNormal(normal);
			transformUtil.transform(LOCAL, center, WORLD, center);
			transformUtil.transform(LOCAL, normal, WORLD, normal);
			HalfEdge[] edges = face.getEdges();
			double area = 0;
			for (int i = 0; i < edges.length; i++) {
				int j = i + 1;
				if (j == edges.length) {
					j = 0;
				}
				edges[i].getVertex().getPosition(p0);
				edges[j].getVertex().getPosition(p1);
				transformUtil.transform(LOCAL, p0, WORLD, p0);
				transformUtil.transform(LOCAL, p1, WORLD, p1);
				
				// compute triangle area
				double a = center.distance(p0);
				double b = center.distance(p1);
				double c = p0.distance(p1);
				double p = (a + b + c) / 2;
				area += Math.sqrt(p * (p - a) * (p - b) * (p - c));
			}
			discs.add(new Disc(center, normal, area));
		}
	}
	
	private static class Disc {
		private final Point3d position;
		private final Vector3d normal;
		private final double area;
		private Disc(Point3d position, Vector3d normal, double area) {
			this.position = new Point3d(position);
			this.normal = new Vector3d(normal);
			normal.normalize();
			this.area = area;
		}
		
		public String toString() {
			return position + " " + normal + " " + area;
		}
	}
}
