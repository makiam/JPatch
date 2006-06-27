package test;

import javax.vecmath.*;
import jpatch.auxilary.*;

public class RotationConversionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Matrix3d m = new Matrix3d();
		Rotation3d rot = new Rotation3d();
		for (Rotation3d.Order order : Rotation3d.Order.values()) {
			rot.order = order;
			System.out.println(rot.order);
			double x, y, z;
			for (int qx = 0; qx < 4; qx ++) {
				for (int qy = 0; qy < 4; qy ++) {
					for (int qz = 0; qz < 4; qz ++) {
						x = -90 + Math.random() * 180;// + 90 * qx;
						y = -90 + Math.random() * 180;// + 90 * qx;
						z = -90 + Math.random() * 180;// + 90 * qx;
//						x = -180 + Math.random() * 90 + 90 * qy;
//						y = -180 + Math.random() * 90 + 90 * qz;
						
						rot.set(x, y, z);
						rot.setMatrixRotation(m);
						rot.setRotation(m);
						double error = (x - rot.x) * (x - rot.x) + (y - rot.y) * (y - rot.y) + (z - rot.z) * (z - rot.z);
	//					System.out.println(rot.order + "   " + error);
						if (error > 0.001) {
							System.out.println("orig: x=" + x + " y=" + y + " z=" + z);
							System.out.println("rot : x=" + rot.x + " y=" + rot.y + " z=" + rot.z);
						}
	//					System.out.println(m);
					}
				}
			}
//			break;
		}
	}
}
