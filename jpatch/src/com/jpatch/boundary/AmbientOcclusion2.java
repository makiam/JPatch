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
	private Collection<Disk> disks = new ArrayList<Disk>();
	
	public void compute(SceneGraphNode rootNode) {
		disks.clear();
		computeDiscs(rootNode);
	}
	
	public void toRib(PrintStream out) {
		String n = Integer.toString(disks.size());
		out.println("Declare \"numberOfDiscs\" \"uniform float\"");
		out.println("Declare \"positions\" \"uniform point[" + n + "]\"");
		out.println("Declare \"normals\" \"uniform normal[" + n + "]\"");
		out.println("Declare \"areas\" \"uniform float[" + n + "]\"");
		out.print("Surface \"ao\" \"numberOfDiscs\" ");
		out.print(disks.size());
		out.print(" \"positions\" [");
		for (Disk disk : disks) {
			out.print(' ');
			out.print(disk.position.x);
			out.print(' ');
			out.print(disk.position.y);
			out.print(' ');
			out.print(disk.position.z);
			out.print(' ');
		}
		out.print("] \"normals\" [");
		for (Disk disk : disks) {
			out.print(' ');
			out.print(disk.normal.x);
			out.print(' ');
			out.print(disk.normal.y);
			out.print(' ');
			out.print(disk.normal.z);
			out.print(' ');
		}
		out.print("] \"areas\" [ ");
		for (Disk disk : disks) {
			out.print(disk.area);
			out.print(' ');
		}
		out.print("]");
		out.println();
	}
	
	public void dumpDiscs(PrintStream out) {
		Matrix3d m = new Matrix3d();
		for (Disk disk : disks) {
			Utils3d.reorientTransform(disk.normal, m);
			//out.println("TransformBegin");
			out.print("    Transform [ ");
			out.print(m.m00); out.print(' ');
			out.print(m.m10); out.print(' ');
			out.print(m.m20); out.print(' ');
			out.print("0 ");
			out.print(m.m01); out.print(' ');
			out.print(m.m11); out.print(' ');
			out.print(m.m21); out.print(' ');
			out.print("0 ");
			out.print(m.m02); out.print(' ');
			out.print(m.m12); out.print(' ');
			out.print(m.m22); out.print(' ');
			out.print("0 ");
			out.print(disk.position.x); out.print(' ');
			out.print(disk.position.y); out.print(' ');
			out.print(disk.position.z); out.print(' ');
			out.println("1 ]");
			out.print("    Disk 0 ");
			out.print(Math.sqrt(disk.area / Math.PI));
			out.println(" 360");
			//out.println("TransformEnd");
		}
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
			disks.add(new Disk(center, normal, area));
		}
	}
	
	private static class Disk {
		private final Point3d position;
		private final Vector3d normal;
		private final double area;
		private Disk(Point3d position, Vector3d normal, double area) {
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
