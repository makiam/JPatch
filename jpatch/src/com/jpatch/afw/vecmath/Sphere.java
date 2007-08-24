package com.jpatch.afw.vecmath;

import javax.vecmath.*;

public class Sphere {
	private final Point3d center = new Point3d();
	private double radius;
	
	public Sphere(Point3d center, double radius) {
		this(center.x, center.y, center.z, radius);
	}
	
	public Sphere(double x, double y, double z, double radius) {
		setCenter(x, y, z);
		setRadius(radius);
	}
	
	public Point3d getCenter(Point3d center) {
		center.set(this.center);
		return center;
	}
	
	public void setCenter(Point3d center) {
		setCenter(center.x, center.y, center.z);
	}
	
	public void setCenter(double x, double y, double z) {
		center.set(x, y, z);
	}
	
	public double getRadius() {
		return radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
}
