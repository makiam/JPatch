package com.jpatch.boundary;

import java.util.*;

import javax.vecmath.*;

import com.jpatch.entity.sds.*;

public class AmbientOcclusion {
	private Map<Level2Vertex, Disk> diskMap;
	
	public void computeAo(Sds sds) {
		long time = System.currentTimeMillis();
		diskMap = new HashMap<Level2Vertex, Disk>();
		for (Face face : sds.faceList) {
			for (Slate2 slate : face.getSlates()) {
				for (SlateEdge[] corner : slate.getCorners()) {
					Level2Vertex v = corner[0].getVertex();
					if (v == null) {
						throw new RuntimeException();
					}
					if (diskMap.get(v) == null) {				
						double radius = 0;
						int n = 0;
						for (int i = 0; i < corner.length; i++) {
							if (corner[i] != null) {
								if (corner[i].getPairVertex() == null) {
									throw new RuntimeException("i=" + i);
								}
								radius += v.limit.distance(corner[i].getPairVertex().limit);
								n++;
							}
						}
						radius /= n;
						Disk disk = new Disk(v.limit, v.normal, radius);
						diskMap.put(v, disk);
					}
				}
			}
		}
		Disk[] disks = new Disk[sds.level2Vertices.length];
		for (int i = 0; i < sds.level2Vertices.length; i++) {
			disks[i] = diskMap.get(sds.level2Vertices[i]);
		}
		
		Vector3d v = new Vector3d();
		for (int i = 0; i < disks.length; i++) {
			System.out.println(i * 100 / disks.length + "%");
			Disk disk1 = disks[i];
			for (int j = i + 1; j < disks.length; j++) {
				Disk disk2 = disks[j];
				v.sub(disk2.position, disk1.position);
				double distance = v.length();
				if (distance == 0) continue;
				v.scale(1 / distance);
				double dot1 = (v.dot(disk1.normal));
				double dot2 = -(v.dot(disk2.normal));
				if (Double.isNaN(dot1)) {
					dot1 = 1;
				}
				if (Double.isNaN(dot2)) {
					dot2 = 1;
				}
				if (false || (dot1 > 0 && dot2 > 0)) {
					double factor = dot1 * dot2;
					if (Double.isNaN(factor)) {
						System.out.println(v);
						System.out.println(disk1.normal);
						System.out.println(disk2.normal);
						System.out.println(v.dot(disk1.normal));
						System.out.println(v.dot(disk2.normal));
						System.exit(0);
					}
//					factor = 1;
					double ao1 = 1 / (Math.pow(2, distance / disk2.radius)) * factor;
					double ao2 = 1 / (Math.pow(2, distance / disk1.radius)) * factor;
					disk1.occlusion += ao1;
					disk2.occlusion += ao2;
				}
			}
		}
		for (Disk disk : disks) {
			System.out.println(disk.radius + "\t" + disk.occlusion);
		}
		System.out.println((System.currentTimeMillis() - time) + "ms");
	}
	
	public double getOcclusion(Level2Vertex vertex) {
		return diskMap.get(vertex).occlusion;
//		return vertex.normal.z;
	}
	
	static class Disk {
		final Point3d position = new Point3d();
		final Vector3d normal = new Vector3d();
		final double radius;
		double occlusion = 0;
		Disk(Point3d position, Vector3d normal, double radius) {
			this.position.set(position);
			this.normal.normalize(normal);
			this.radius = radius;
		}
	}
}
